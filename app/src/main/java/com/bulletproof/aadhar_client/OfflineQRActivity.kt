package com.bulletproof.aadhar_client

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bulletproof.aadhar_client.databinding.ActivityOfflineQractivityBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

class OfflineQRActivity : AppCompatActivity() {

    var binding: ActivityOfflineQractivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineQractivityBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val shp: SharedPreferences = getSharedPreferences("Aadhar-Client", Context.MODE_PRIVATE)
        val str = shp.getString("data", "null")
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            binding?.imgQr?.setImageBitmap(bmp)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}