package com.bulletproof.aadhar_client

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.nfc.NdefMessage
import android.nfc.NdefRecord.createMime
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bulletproof.aadhar_client.databinding.ActivityOfflineQractivityBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class OfflineQRActivity : AppCompatActivity(), NfcAdapter.CreateNdefMessageCallback {

    private var binding: ActivityOfflineQractivityBinding? = null
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var str: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineQractivityBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar?.hide()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if(nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show()
        }

        nfcAdapter?.setNdefPushMessageCallback(this, this)

        val shp: SharedPreferences = getSharedPreferences("Aadhar-Client", Context.MODE_PRIVATE)
        str = shp.getString("data", "null").toString()
        if(str == "null") {
            binding?.txtInfo?.visibility = View.VISIBLE
        } else {
            binding?.txtInfo?.visibility = View.INVISIBLE
            val writer = QRCodeWriter()
            try {
                val bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for(x in 0 until width) {
                    for(y in 0 until height) {
                        bmp.setPixel(x, y, if(bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                    }
                }
                binding?.imgQr?.setImageBitmap(bmp)
            } catch(e: WriterException) {
                e.printStackTrace()
            }
        }
    }

    override fun createNdefMessage(event: NfcEvent?): NdefMessage {
        return NdefMessage(arrayOf(createMime("application/com.bulletproof.aadhar_verifier", str.toByteArray())))
    }
}