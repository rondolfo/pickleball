package com.kriptobr.placar.watch

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.kriptobr.placar.core.Lado
import com.kriptobr.placar.core.Protocolo
import org.json.JSONObject
import java.util.Collections
import java.util.UUID

/**
 * Transporte Bluetooth do relogio.
 *
 * Procura o tablet pelo identificador do servico, conecta e conversa
 * direto, sem rede sem fio no meio. E o caminho preferido em quadra.
 *
 * Mesma garantia do Wi-Fi: pontos ficam na fila ate a confirmacao chegar,
 * e o reenvio e seguro porque o tablet ignora identificadores repetidos.
 */
class ClienteBle(
    private val contexto: Context,
    private val aoReceberEstado: (JSONObject) -> Unit,
    private val aoMudarConexao: (Boolean) -> Unit,
    private val aoConfirmarPonto: () -> Unit
) {

    companion object {
        private const val TAG = "ClienteBle"
        private const val FIM = "\n"
        private const val MTU = 247
    }

    private val gerenciador: BluetoothManager? =
        contexto.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private var gatt: BluetoothGatt? = null
    private var envio: BluetoothGattCharacteristic? = null
    private val pendentes = Collections.synchronizedMap(LinkedHashMap<String, String>())
    private val acumulado = StringBuilder()

    private var procurando = false
    private var callbackBusca: ScanCallback? = null

    @Volatile
    var conectado: Boolean = false
        private set

    val naFila: Int get() = pendentes.size

    fun suportado(): Boolean {
        val adaptador = gerenciador?.adapter ?: return false
        return adaptador.isEnabled && adaptador.bluetoothLeScanner != null
    }

    @SuppressLint("MissingPermission")
    fun iniciar() {
        if (procurando) return
        val buscador = gerenciador?.adapter?.bluetoothLeScanner ?: return

        val filtro = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(UUID.fromString(Protocolo.BLE_SERVICO)))
            .build()

        val configuracao = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val callback = object : ScanCallback() {
            override fun onScanResult(tipo: Int, resultado: ScanResult) {
                val aparelho = resultado.device ?: return
                Log.i(TAG, "tablet encontrado por bluetooth")
                pararBusca()
                conectar(aparelho)
            }

            override fun onScanFailed(erro: Int) {
                Log.e(TAG, "falha na busca bluetooth, codigo $erro")
                procurando = false
            }
        }

        callbackBusca = callback
        runCatching {
            buscador.startScan(listOf(filtro), configuracao, callback)
            procurando = true
        }.onFailure { Log.e(TAG, "busca recusada: ${it.message}") }
    }

    @SuppressLint("MissingPermission")
    fun parar() {
        pararBusca()
        runCatching { gatt?.close() }
        gatt = null
        envio = null
        conectado = false
    }

    @SuppressLint("MissingPermission")
    private fun pararBusca() {
        val callback = callbackBusca ?: return
        runCatching { gerenciador?.adapter?.bluetoothLeScanner?.stopScan(callback) }
        procurando = false
    }

    fun enviarRally(vencedor: Lado) {
        val id = UUID.randomUUID().toString()
        val mensagem = JSONObject().apply {
            put("tipo", Protocolo.TIPO_RALLY)
            put("id", id)
            put("vencedor", vencedor.name)
            put("ts", System.currentTimeMillis())
        }.toString()
        pendentes[id] = mensagem
        escrever(mensagem)
    }

    fun enviarDesfazer() {
        escrever(JSONObject().apply { put("tipo", Protocolo.TIPO_DESFAZER) }.toString())
    }

    fun enviarInverter() {
        escrever(JSONObject().apply { put("tipo", Protocolo.TIPO_INVERTER) }.toString())
    }

    /** Reenvia o que ainda nao foi confirmado. Chamado periodicamente. */
    fun reenviarPendentes() {
        if (!conectado) return
        val copia = synchronized(pendentes) { pendentes.values.toList() }
        copia.forEach { escrever(it) }
    }

    @SuppressLint("MissingPermission")
    private fun escrever(mensagem: String) {
        val alvo = gatt ?: return
        val caracteristica = envio ?: return
        val dados = (mensagem + FIM).toByteArray()

        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                alvo.writeCharacteristic(
                    caracteristica,
                    dados,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                )
            } else {
                @Suppress("DEPRECATION")
                caracteristica.value = dados
                @Suppress("DEPRECATION")
                caracteristica.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                @Suppress("DEPRECATION")
                alvo.writeCharacteristic(caracteristica)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun conectar(aparelho: BluetoothDevice) {
        gatt = aparelho.connectGatt(contexto, false, retornoGatt, BluetoothDevice.TRANSPORT_LE)
    }

    private val retornoGatt = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(alvo: BluetoothGatt, status: Int, novo: Int) {
            if (novo == BluetoothGatt.STATE_CONNECTED) {
                alvo.requestMtu(MTU)
            } else {
                conectado = false
                envio = null
                aoMudarConexao(false)
                runCatching { alvo.close() }
                gatt = null
                iniciar()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(alvo: BluetoothGatt, mtu: Int, status: Int) {
            alvo.discoverServices()
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(alvo: BluetoothGatt, status: Int) {
            val servico = alvo.getService(UUID.fromString(Protocolo.BLE_SERVICO)) ?: return
            envio = servico.getCharacteristic(UUID.fromString(Protocolo.BLE_ENVIO))

            val retorno = servico.getCharacteristic(UUID.fromString(Protocolo.BLE_RETORNO))
            if (retorno != null) {
                alvo.setCharacteristicNotification(retorno, true)
                val descritor = retorno.getDescriptor(UUID.fromString(Protocolo.BLE_CCCD))
                if (descritor != null) {
                    runCatching {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            alvo.writeDescriptor(
                                descritor,
                                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            descritor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            @Suppress("DEPRECATION")
                            alvo.writeDescriptor(descritor)
                        }
                    }
                }
            }

            conectado = true
            aoMudarConexao(true)
            reenviarPendentes()
        }

        override fun onCharacteristicChanged(
            alvo: BluetoothGatt,
            caracteristica: BluetoothGattCharacteristic,
            valor: ByteArray
        ) {
            receber(String(valor))
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            alvo: BluetoothGatt,
            caracteristica: BluetoothGattCharacteristic
        ) {
            val valor = caracteristica.value ?: return
            receber(String(valor))
        }
    }

    private fun receber(pedaco: String) {
        synchronized(acumulado) {
            acumulado.append(pedaco)
            var texto = acumulado.toString()
            while (texto.contains(FIM)) {
                val corte = texto.indexOf(FIM)
                val mensagem = texto.substring(0, corte)
                texto = texto.substring(corte + 1)
                if (mensagem.isNotBlank()) tratar(mensagem)
            }
            acumulado.setLength(0)
            acumulado.append(texto)
        }
    }

    private fun tratar(texto: String) {
        runCatching {
            val objeto = JSONObject(texto)
            when (objeto.optString("tipo")) {
                Protocolo.TIPO_ACK -> {
                    val id = objeto.optString("id")
                    if (pendentes.remove(id) != null) aoConfirmarPonto()
                }
                Protocolo.TIPO_ECO -> {
                    escrever(
                        JSONObject().apply {
                            put("tipo", Protocolo.TIPO_ECO_RESP)
                            put("ts", objeto.optLong("ts"))
                        }.toString()
                    )
                }
                else -> aoReceberEstado(objeto)
            }
        }
    }
}
