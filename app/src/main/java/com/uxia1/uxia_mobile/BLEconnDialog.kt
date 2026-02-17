package com.uxia1.uxia_mobile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Base64OutputStream
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.uxia1.uxia_mobile.services.HttpClientService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class BLEconnDialog(

    context: Context,
    private val device: BluetoothDevice,
    private val connectionCallback: BLEConnectionCallback
) : Dialog(context) {

    interface BLEConnectionCallback {
        fun onConnectionSuccess(gatt: BluetoothGatt)
        fun onConnectionFailed(error: String)
        fun onConnectionCancelled()
        fun onReceivedResponse(img : String, response : String)

        fun onReceivedImage(file: File)
    }

    // Views

    private lateinit var tvDeviceName: TextView
    private lateinit var tvDeviceAddress: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvImage: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnConnect: Button
    private lateinit var btnCancel: Button

    // BLE
    // UUIDs
    private val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private val CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
    private var bluetoothGatt: BluetoothGatt? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isConnecting = false
    private val CONNECTION_TIMEOUT = 10000L // 10 segons
    private val SCAN_PERIOD: Long = 10000
    private val RECEIVE_TIMEOUT: Long = 30000  // 30 segons timeout

    // Variables per foto
    private val receivedData = ByteArrayOutputStream()
    lateinit private var receivedFile : File
    private var totalSize = 0
    private var isReceiving = false
    private var received = false
    private var lastPacketTime = 0L
    private var packetCount = 0

    private lateinit var decodedData : ByteArray

    // Callback de timeout
    @SuppressLint("MissingPermission")
    private val connectionTimeoutRunnable = Runnable {
        if (isConnecting) {
            disconnect()
            connectionCallback.onConnectionFailed("Timeout de connexió")
            dismiss()
        }
    }

    // Convertir File en una string de Base64
    fun convertImageFileToBase64(imageFile: File): String {
        return ByteArrayOutputStream().use { outputStream ->
            Base64OutputStream(outputStream, Base64.NO_WRAP).use { base64FilterStream ->
                imageFile.inputStream().use { inputStream ->
                    inputStream.copyTo(base64FilterStream)
                }
            }
            return@use outputStream.toString()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_ble_conn)

        // Inicialitzar views
        tvDeviceName = findViewById(R.id.tvDeviceName)
        tvDeviceAddress = findViewById(R.id.tvDeviceAddress)
        tvStatus = findViewById(R.id.tvStatus)
        tvImage = findViewById(R.id.imageView)
        progressBar = findViewById(R.id.progressBar)
        btnConnect = findViewById(R.id.btnConnect)
        btnCancel = findViewById(R.id.btnCancel)

        // Configurar dades del dispositiu
        val deviceName = device.name ?: "Dispositiu desconegut"
        tvDeviceName.text = deviceName
        tvDeviceAddress.text = device.address

        // Configurar botons
        btnConnect.setOnClickListener {
            if( received ) {
                savePhoto(decodedData)
                connectionCallback.onReceivedImage(receivedFile)

                tvStatus.text = "Enviant imatge a UXIA..."
                btnConnect.isEnabled = false

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        var base64Image =""
                        // Ejecutamos la parte pesada en Dispatchers.IO
                        val response = withContext(Dispatchers.IO) {
                            base64Image = convertImageFileToBase64(receivedFile)
                            HttpClientService.sendImage(base64Image)
                        }

                        Log.d("HTTP Service", response)

                        TTS.speakResponse(response)

                        connectionCallback.onReceivedImage(receivedFile)
                        connectionCallback.onReceivedResponse(base64Image,response)

                        dismiss()

                    } catch (e: Exception) {
                        Log.e("HTTP Error", "Error al enviar: ${e.message}")
                        tvStatus.text = "Error en l'enviament"
                        btnConnect.isEnabled = true
                    }
                }
                disconnect()
                dismiss()
            }
            else if (!isConnecting) {
                connectToDevice()
//                disconnect()
//                dismiss()
            }
        }

        btnCancel.setOnClickListener {
            cancelConnection()
        }

        // Connectar automàticament al mostrar el dialog
        connectToDevice()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectToDevice() {
        if (isConnecting) return

        isConnecting = true
        updateUIForConnecting()

        // Iniciar timeout
        handler.postDelayed(connectionTimeoutRunnable, CONNECTION_TIMEOUT)

        // Connectar al dispositiu BLE
        // Nota: Necessites tenir el context de l'activitat o un context vàlid
        val context = context.applicationContext ?: context
        bluetoothGatt = device.connectGatt(context, false, gattCallback)

        // En alguns dispositius, cal fer un connect explícit
        // bluetoothGatt?.connect()
    }

    private fun updateUIForConnecting() {
        tvStatus.text = "Connectant..."
        btnConnect.isEnabled = false
        btnConnect.text = "Connectant..."
        progressBar.isIndeterminate = true
    }

    private fun updateUIForConnected() {
        tvStatus.text = "Connectat"
        btnConnect.isEnabled = false
        btnConnect.text = "Connectat"
        progressBar.isIndeterminate = false
        progressBar.progress = 100
    }

    private fun updateUIForDisconnected() {
        tvStatus.text = "Desconnectat"
        btnConnect.isEnabled = true
        btnConnect.text = "Connectar"
        progressBar.isIndeterminate = false
        progressBar.progress = 0
    }

    private fun updateUIForReceived() {
        //tvStatus.text = "Desconnectat"
        btnConnect.isEnabled = true
        btnConnect.text = "Fet!"
        //progressBar.isIndeterminate = false
        //progressBar.progress = 0
    }


    @SuppressLint("MissingPermission")
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            handler.post {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        // Cancel·lar timeout
                        handler.removeCallbacks(connectionTimeoutRunnable)
                        isConnecting = false

                        // Actualitzar UI
                        updateUIForConnected()

                        // Sol·licitar MTU més gran
                        gatt.requestMtu(517)

                        // Descobrir serveis
                        //gatt.discoverServices()
                        // Descobrir serveis després d'un breu retard
                        handler.postDelayed({
                            gatt.discoverServices()
                        }, 500)
                    }

                    BluetoothProfile.STATE_DISCONNECTED -> {
                        handler.removeCallbacks(connectionTimeoutRunnable)
                        isConnecting = false

                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            updateUIForDisconnected()
                        } else {
                            // Error de connexió
                            val errorMsg = when (status) {
                                0x08 -> "Timeout"
                                0x13 -> "Terminat per host local"
                                0x16 -> "Terminat per host remot"
                                0x3E -> "No connectat"
                                else -> "Error desconegut: $status"
                            }

                            tvStatus.text = "Error: $errorMsg"
                            connectionCallback.onConnectionFailed(errorMsg)
                        }
                    }
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            handler.post {
                Log.d("BLE", "MTU canviat a: $mtu")
                tvStatus.text = "MTU: $mtu"
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                handler.post {
                    //binding.statusText.text = "Serveis descoberts"
                }

                val service = gatt.getService(SERVICE_UUID)
                service?.let {
                    val photoCharacteristic = it.getCharacteristic(CHARACTERISTIC_UUID)
                    photoCharacteristic?.let { characteristic ->
                        // Habilitar notificacions
                        gatt.setCharacteristicNotification(characteristic, true)

                        val descriptor = characteristic.getDescriptor(
                            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                        )
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)

                        /*handler.post {
                            binding.statusText.text = "Llest per rebre fotos!"
                            binding.photoStatus.text = "Esperant foto..."
                            Toast.makeText(this@MainActivity,
                                "Connectat! Prem BOOT a l'ESP32",
                                Toast.LENGTH_SHORT).show()
                        }*/
                    } ?: run {
                        handler.post {
                            Log.d("BT","Error: Característica no trobada")
                            //binding.statusText.text = "Error: Característica no trobada"
                        }
                    }
                } ?: run {
                    handler.post {
                        Log.d("BT","Error: Servei no trobat")
                        //binding.statusText.text = "Error: Servei no trobat"
                    }
                }
            } else {
                handler.post {
                    Log.d("BT","Error descobrint serveis: $status")
                    //binding.statusText.text = "Error descobrint serveis: $status"
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt,
                                             characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)

            if (characteristic.uuid == CHARACTERISTIC_UUID) {
                handleIncomingData(characteristic.value)
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt,
                                          characteristic: BluetoothGattCharacteristic,
                                          status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Log.d("BLE", "Característica llegida: ${characteristic.uuid}")
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt,
                                           characteristic: BluetoothGattCharacteristic,
                                           status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.d("BLE", "Característica escrita: $status")
        }



    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        isConnecting = false
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun cancelConnection() {
        handler.removeCallbacks(connectionTimeoutRunnable)
        disconnect()
        connectionCallback.onConnectionCancelled()
        dismiss()
    }

    override fun dismiss() {
        handler.removeCallbacks(connectionTimeoutRunnable)
        super.dismiss()
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(connectionTimeoutRunnable)
        super.onDetachedFromWindow()
    }

    companion object {
        const val TAG = "BLEconnDialog"
    }


    // BLE data receive
    ///////////////////////
    private fun handleIncomingData(data: ByteArray) {
        handler.post {
            packetCount++
            lastPacketTime = System.currentTimeMillis()

            Log.d("BLE", "Paquet $packetCount rebut: ${data.size} bytes")
            Log.d("BLE", "Primers bytes: ${data.take(4).joinToString("") { "%02X".format(it) }}")

            // Verificar si és paquet de finalització
            if (data.size == 4 && data.contentEquals(byteArrayOf(0xFF.toByte(),
                    0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()
                ))) {
                if (isReceiving && receivedData.size() > 0) {
                    Log.d("P_354","complete Photo")
                    completePhotoTransfer()
                } else {
                    tvStatus.text = "Finalització rebuda sense dades"
                }
                return@post
            }

            // Primer paquet (pot ser la mida)
            if (!isReceiving && data.size == 4) {
                // Intentar interpretar com a mida de 32 bits
                try {
                    totalSize = (data[0].toInt() and 0xFF) +
                            ((data[1].toInt() and 0xFF) shl 8) +
                            ((data[2].toInt() and 0xFF) shl 16) +
                            ((data[3].toInt() and 0xFF) shl 24)

                    isReceiving = true
                    receivedData.reset()

                    tvStatus.text = "Rebent foto ($totalSize bytes)..."
                    progressBar.max = totalSize
                    progressBar.progress = 0

                    Log.d("BLE", "Mida anunciada: $totalSize bytes")

                    // Iniciar timeout
                    startReceiveTimeout()

                } catch (e: Exception) {
                    Log.e("BLE", "Error interpretant mida: ${e.message}")
                }
                return@post
            }

            // Si estem rebent, afegir dades
            if (isReceiving) {
                receivedData.write(data)

                val currentSize = receivedData.size()
                progressBar.progress = currentSize

                // Actualitzar estat cada certs paquets
                if (packetCount % 10 == 0 || currentSize == totalSize) {
                    val percent = if (totalSize > 0)
                        (currentSize * 100) / totalSize else 0

                    tvStatus.text =
                        "Rebent: $currentSize/$totalSize bytes ($percent%)"

                    Log.d("BLE", "Progrés: $currentSize/$totalSize ($percent%)")
                }

                // Reiniciar timeout amb cada paquet
                resetReceiveTimeout()

                // Si hem arribat a la mida esperada, completar
                if (totalSize > 0 && currentSize >= totalSize) {
                    Log.d("P_412","complete Photo")

                    completePhotoTransfer()
                }
            }
        }
    }

    private fun startReceiveTimeout() {
        handler.removeCallbacks(receiveTimeoutRunnable)
        handler.postDelayed(receiveTimeoutRunnable, RECEIVE_TIMEOUT)
    }

    private fun resetReceiveTimeout() {
        handler.removeCallbacks(receiveTimeoutRunnable)
        handler.postDelayed(receiveTimeoutRunnable, RECEIVE_TIMEOUT)
    }

    private val receiveTimeoutRunnable = Runnable {
        handler.post {
            if (isReceiving) {
                tvStatus.text = "⏰ Timeout! Transferència incompleta"
                Log.e("BLE", "Timeout en recepció de foto")
                resetPhotoTransfer()
            }
        }
    }

    private fun completePhotoTransfer() {
        val finalSize = receivedData.size()

        handler.post {
            tvStatus.text = "✅ Foto rebuda: $finalSize bytes"
            progressBar.progress = finalSize

            val dataStr = receivedData.toString().trim()
            Log.v("FOTO",dataStr)
            // Guardar foto
            try {
                decodedData = Base64.decode(dataStr, Base64.DEFAULT)
                received = true
                val bitmap = BitmapFactory.decodeByteArray(decodedData, 0, decodedData.size)
                tvImage.setImageBitmap(bitmap)
                updateUIForReceived()
                //savePhoto(decodedData)
                // Notificació
                //Toast.makeText(this, "Foto rebuda: $finalSize bytes",
                //    Toast.LENGTH_LONG).show()

                Log.v("BT","Foto rebuda: $finalSize bytes")
            } catch (e : Exception) {
                Log.v("ERROR","Error en descodificació base64")
                e.printStackTrace()
            }

            // Reset
            resetPhotoTransfer()
        }
    }

    private fun resetPhotoTransfer() {
        isReceiving = false
        totalSize = 0
        receivedData.reset()
        packetCount = 0
        handler.removeCallbacks(receiveTimeoutRunnable)
    }

    private fun savePhoto(imageData: ByteArray) {
        try {
            val timestamp = System.currentTimeMillis()
            val filename = "UXIA_Image_${timestamp}.jpg"

            // Guardar al directori de Pictures
            val picturesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES)
            val fri3dDir = File(picturesDir, "UXIA")

            if (!fri3dDir.exists()) {
                fri3dDir.mkdirs()
            }

            receivedFile = File(fri3dDir, filename)
            FileOutputStream(receivedFile).use { fos ->
                fos.write(imageData)
                fos.flush()
            }

            Log.d("Photo", "Foto guardada: ${receivedFile.absolutePath}")

            // Notificar galeria
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(receivedFile)
            context.sendBroadcast(mediaScanIntent)

            tvStatus.text = "💾 Guardat: $filename"

            //dismiss()

        } catch (e: Exception) {
            Log.e("Photo", "Error guardant foto: ${e.message}")
            tvStatus.text = "❌ Error guardant foto"
        }
    }

}