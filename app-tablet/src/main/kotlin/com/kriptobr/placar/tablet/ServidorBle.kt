package com.kriptobr.placar.tablet

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.Build
import android.util.Log
import com.kriptobr.placar.core.Lado
import com.kriptobr.placar.core.Protocolo
import org.json.JSONObject
import java.util.Collections
import java.util.UUID

/**
 * Transporte Bluetooth de baixa energia, com o tablet como periferico.
 *
 * Existe para o relogio nao depender de rede sem fio. Em quadra raramente
 * ha Wi-Fi util, e obrigar o jogador a ligar roteamento no celular so para
 * marcar ponto e um preco alto demais.
 *
 * Roda em paralelo com o servidor Wi-Fi. Se o aparelho nao suportar modo
 * periferico, este transporte simplesmente nao sobe e o Wi-Fi continua.
 */
class ServidorBle(
    private val contexto: Context,
    private val aoReceberRally: (id: String, vencedor: Lado) -> Unit,
    private val aoReceberDesfazer: () -> Unit,
    private val aoReceberInverter: () -> Unit,
    private val estadoJson: () -> String,
    private val aoMudarClientes: (Int) -> Unit,
    private val aoReceberEco: (Long) -> Unit,
    private val aoTerContato: () -> Unit
) {

    companion object {
        private const val TAG = "ServidorBle"
        private const val FIM = "\n"
    }

    private val gerenciador: BluetoothManager? =
        contexto.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private var servidor: BluetoothGattServer? = null
    private var retorno: BluetoothGattCharacteristic? = null
    private val assinantes = Collections.synchronizedSet(LinkedHashSet<BluetoothDevice>())
    private val parciais = Collections.synchronizedMap(HashMap<String, StringBuilder>())

    private var anunciando = false
    private var callbackAnuncio: AdvertiseCallback? = null

    var disponivel: Boolean = false
        private set

    val conectados: Int get() = assinantes.size

    @SuppressLint("MissingPermission")
    fun iniciar(): Boolean {
        val adaptador = gerenciador?.adapter ?: return false
        if (!adaptador.isEnabled) {
            Log.w(TAG, "bluetooth desligado")
            return false
        }

        val anunciador = adaptador.bluetoothLeAdvertiser
        if (anunciador == null) {
            Log.w(TAG, "aparelho nao suporta modo periferico")
            return false
        }

        val servico = BluetoothGattService(
            UUID.fromString(Protocolo.BLE_SERVICO),
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        val envio = BluetoothGattCharacteristic(
            UUID.fromString(Protocolo.BLE_ENVIO),
            BluetoothGattCharacteristic.PROPERTY_WRITE or
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        val saida = BluetoothGattCharacteristic(
            UUID.fromString(Protocolo.BLE_RETORNO),
            BluetoothGattCharacteristic.PROPERTY_READ or
                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        saida.addDescriptor(
            BluetoothGattDescriptor(
                UUID.fromString(Protocolo.BLE_CCCD),
                BluetoothGattDescriptor.PERMISSION_READ or
                    BluetoothGattDescriptor.PERMISSION_WRITE
            )
        )

        servico.addCharacteristic(envio)
        servico.addCharacteristic(saida)
        retorno = saida

        val aberto = runCatching {
            gerenciador.openGattServer(contexto, retornoDoServidor)?.also {
                it.addService(servico)
            }
        }.getOrNull()

        if (aberto == null) {
            Log.e(TAG, "nao foi possivel abrir o servidor gatt")
            return false
        }
        servidor = aberto

        val configuracao = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val dados = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(android.os.ParcelUuid(UUID.fromString(Protocolo.BLE_SERVICO)))
            .build()

        val callback = object : AdvertiseCallback() {
            override fun onStartSuccess(efetivo: AdvertiseSettings?) {
                anunciando = true
                Log.i(TAG, "anunciando por bluetooth")
            }

            override fun onStartFailure(erro: Int) {
                anunciando = false
                Log.e(TAG, "falha ao anunciar, codigo $erro")
            }
        }
        callbackAnuncio = callback

        runCatching { anunciador.startAdvertising(configuracao, dados, callback) }
            .onFailure { Log.e(TAG, "anuncio recusado: ${it.message}") }

        disponivel = true
        return true
    }

    @SuppressLint("MissingPermission")
    fun parar() {
        runCatching {
            val adaptador = gerenciador?.adapter
            val callback = callbackAnuncio
            if (adaptador != null && callback != null) {
                adaptador.bluetoothLeAdvertiser?.stopAdvertising(callback)
            }
        }
        runCatching { servidor?.close() }
        servidor = null
        assinantes.clear()
        disponivel = false
        anunciando = false
    }

    /** Envia o estado para todos os relogios conectados por bluetooth. */
    @SuppressLint("MissingPermission")
    fun transmitir(json: String) {
        val caracteristica = retorno ?: return
        val alvo = servidor ?: return
        val dados = (json + FIM).toByteArray()

        val copia = synchronized(assinantes) { assinantes.toList() }
        copia.forEach { aparelho ->
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    alvo.notifyCharacteristicChanged(aparelho, caracteristica, false, dados)
                } else {
                    @Suppress("DEPRECATION")
                    caracteristica.value = dados
                    @Suppress("DEPRECATION")
                    alvo.notifyCharacteristicChanged(aparelho, caracteristica, false)
                }
            }
        }
    }

    private val retornoDoServidor = object : BluetoothGattServerCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(aparelho: BluetoothDevice, status: Int, novo: Int) {
            if (novo == BluetoothGatt.STATE_DISCONNECTED) {
                assinantes.remove(aparelho)
                parciais.remove(aparelho.address)
                aoMudarClientes(assinantes.size)
            }
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWriteRequest(
            aparelho: BluetoothDevice,
            requisicao: Int,
            descritor: BluetoothGattDescriptor,
            preparar: Boolean,
            responder: Boolean,
            deslocamento: Int,
            valor: ByteArray
        ) {
            // o relogio assinou as notificacoes
            assinantes.add(aparelho)
            aoMudarClientes(assinantes.size)
            if (responder) {
                servidor?.sendResponse(aparelho, requisicao, BluetoothGatt.GATT_SUCCESS, 0, null)
            }
            transmitir(estadoJson())
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWriteRequest(
            aparelho: BluetoothDevice,
            requisicao: Int,
            caracteristica: BluetoothGattCharacteristic,
            preparar: Boolean,
            responder: Boolean,
            deslocamento: Int,
            valor: ByteArray
        ) {
            if (responder) {
                servidor?.sendResponse(aparelho, requisicao, BluetoothGatt.GATT_SUCCESS, 0, null)
            }
            aoTerContato()

            // mensagens podem chegar partidas; junta ate a quebra de linha
            val acumulado = synchronized(parciais) {
                parciais.getOrPut(aparelho.address) { StringBuilder() }
            }
            acumulado.append(String(valor))

            var texto = acumulado.toString()
            while (texto.contains(FIM)) {
                val corte = texto.indexOf(FIM)
                val mensagem = texto.substring(0, corte)
                texto = texto.substring(corte + 1)
                if (mensagem.isNotBlank()) tratar(aparelho, mensagem)
            }
            acumulado.setLength(0)
            acumulado.append(texto)
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicReadRequest(
            aparelho: BluetoothDevice,
            requisicao: Int,
            deslocamento: Int,
            caracteristica: BluetoothGattCharacteristic
        ) {
            val dados = estadoJson().toByteArray()
            servidor?.sendResponse(
                aparelho, requisicao, BluetoothGatt.GATT_SUCCESS, deslocamento,
                if (deslocamento < dados.size) dados.copyOfRange(deslocamento, dados.size)
                else ByteArray(0)
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun tratar(aparelho: BluetoothDevice, texto: String) {
        runCatching {
            val objeto = JSONObject(texto)
            when (objeto.optString("tipo")) {
                Protocolo.TIPO_RALLY -> {
                    val id = objeto.optString("id")
                    if (id.isNotEmpty()) {
                        aoReceberRally(id, Lado.valueOf(objeto.optString("vencedor")))
                        transmitir(
                            JSONObject().apply {
                                put("tipo", Protocolo.TIPO_ACK)
                                put("id", id)
                            }.toString()
                        )
                    }
                }
                Protocolo.TIPO_DESFAZER -> aoReceberDesfazer()
                Protocolo.TIPO_INVERTER -> aoReceberInverter()
                Protocolo.TIPO_ECO_RESP -> aoReceberEco(objeto.optLong("ts"))
                Protocolo.TIPO_PING -> transmitir(estadoJson())
            }
        }.onFailure { Log.w(TAG, "mensagem invalida por bluetooth") }
    }
}
