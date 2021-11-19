package com.bulletproof.aadhar_client

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bulletproof.aadhar_client.databinding.ActivityQrcodeBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject


class QRCodeActivity : AppCompatActivity() {

    private lateinit var aadhar: String
    private var binding: ActivityQrcodeBinding? = null
    private var obj: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrcodeBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        aadhar = intent.extras?.getString("aadhar").toString()
        val strEkyc = intent.extras?.getString("ekyc").toString()

        binding?.btnAuth?.setOnClickListener {
            try {
                createQR(strEkyc)
            } catch (e: Exception) {
                Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show()
                Log.d("Auth", e.message.toString())
            }
        }

        binding?.btnSave?.setOnClickListener {
            val shp: SharedPreferences = getSharedPreferences("Aadhar-Client", Context.MODE_PRIVATE)
            val ed = shp.edit()
            ed.putString("data", obj.toString())
            ed.apply()
            Toast.makeText(this, "Saved Successfully", Toast.LENGTH_LONG).show()
        }
    }

    private fun createQR(strEkyc: String) {
        val jsonObj = JSONObject(strEkyc)
        val kycRes = jsonObj.getJSONObject("KycRes")
        val uidData = kycRes.getJSONObject("UidData")
        val poi = uidData.getJSONObject("Poi")
        val dob = poi.getString("dob")
        val gender = poi.getString("gender")
        val name = poi.getString("name")
        val phone = poi.getString("phone")

        obj = JSONObject()
        obj?.put("name", name)
        obj?.put("gender", gender)
        obj?.put("phone", phone)
        obj?.put("dob", dob)

        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(obj.toString(), BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            binding?.qrImg?.setImageBitmap(bmp)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        binding?.btnSave?.visibility = View.VISIBLE
    }
}