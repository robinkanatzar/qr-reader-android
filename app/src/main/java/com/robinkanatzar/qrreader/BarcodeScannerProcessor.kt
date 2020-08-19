package com.robinkanatzar.qrreader

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage


class BarcodeScannerProcessor(private val context: Context) : VisionProcessorBase<List<Barcode>>(context) {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    private val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(options)

    override fun stop() {
        super.stop()
        barcodeScanner.close()
    }

    override fun detectInImage(image: InputImage): Task<List<Barcode>> {
        return barcodeScanner.process(image)
    }

    override fun onSuccess(barcodes: List<Barcode>, graphicOverlay: GraphicOverlay) {
        if (barcodes.isEmpty()) {
            Log.v(MANUAL_TESTING_LOG, "No barcode has been detected")
        }
        for (i in barcodes.indices) {
            val barcode = barcodes[i]
            // TODO start wifi connection on success

            when (barcode.valueType) {
                Barcode.TYPE_WIFI -> {
                    val ssid = barcode.wifi!!.ssid
                    val password = barcode.wifi!!.password
                    val type = barcode.wifi!!.encryptionType

                    Log.d("RCK", "SSID = $ssid, password = $password, type = $type")

                    barcodeScanner.close()
                    val builder =
                        AlertDialog.Builder(context)
                    builder.setMessage("Do you want to connect to this wifi network?")
                        .setCancelable(false)
                        .setPositiveButton(
                            "Yes"
                        ) { dialog, id ->
                            // TODO start wifi connection
                            checkIsCharging()
                            //connectWifi(ssid, password, type)
                            dialog.dismiss()
                        }
                        .setNegativeButton(
                            "No"
                        ) { dialog, id ->
                            // TODO restart barcode scanner
                            dialog.cancel()
                        }
                    val alert = builder.create()
                    alert.show()
                }
                /*Barcode.TYPE_URL -> {
                    val title = barcode.url!!.title
                    val url = barcode.url!!.url
                }*/
            }

            //logExtrasForTesting(barcode)
        }
    }

    private fun connectWifi(ssid: String?, password: String?, type: Int) {
        /*val wifiManager: WifiManager = (WifiManager)
            context.applicationContext.getSystemService(WIFI_SERVICE)
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }*/


    }

    private fun checkIsCharging() {
        val batteryManager =
            context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        Log.d("RCK", "isCharging? ${batteryManager.isCharging}")
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Barcode detection failed $e")
    }

    companion object {
        private const val TAG = "BarcodeProcessor"

        private fun logExtrasForTesting(barcode: Barcode?) {
            if (barcode != null) {
                when (barcode.valueType) {
                    Barcode.TYPE_WIFI -> {
                        val ssid = barcode.wifi!!.ssid
                        val password = barcode.wifi!!.password
                        val type = barcode.wifi!!.encryptionType

                        Log.d("RCK", "SSID = $ssid, password = $password, type = $type")
                        // TODO exit scanning once this is found


                    }
                    Barcode.TYPE_URL -> {
                        val title = barcode.url!!.title
                        val url = barcode.url!!.url
                        // TODO open url, exit scanning
                    }
                }
            }
        }
    }
}