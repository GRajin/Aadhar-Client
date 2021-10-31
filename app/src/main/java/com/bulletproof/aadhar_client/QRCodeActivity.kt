package com.bulletproof.aadhar_client

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bulletproof.aadhar_client.databinding.ActivityQrcodeBinding
import com.bulletproof.aadhar_client.models.Auth
import com.bulletproof.aadhar_client.models.OTP
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import java.util.*
import com.google.zxing.WriterException

import android.graphics.Bitmap

import com.google.zxing.BarcodeFormat
import android.graphics.Color
import android.view.View

import com.google.zxing.qrcode.QRCodeWriter
import android.content.SharedPreferences
import android.content.Context


class QRCodeActivity : AppCompatActivity() {

    private lateinit var aadhar: String
    private var otpModel: OTP? = null
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
                val url = "https://stage1.uidai.gov.in/onlineekyc/getAuth/"
                val requestQueue = Volley.newRequestQueue(this)
                val otp = (binding?.layOtp?.editText as TextInputEditText).text.toString()
                val authModel = Auth(otpModel?.uid.toString(), otpModel?.txnId.toString(), otp)
                val params = JSONObject()
                params.put("uid", authModel.uid)
                params.put("txnId", authModel.txnId)
                params.put("otp", authModel.otp)
                val objReq = JsonObjectRequest(Request.Method.POST, url, params, { response ->
                    if (response.getString("status") == "y" || response.getString("errCode") == "null") {
                        createQR(strEkyc)
                    } else {
                        if(response.getString("errCode") == "400") {
                            Toast.makeText(this, "Invalid OTP value", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show()
                            Log.d("Auth", response.getString("errCode"))
                        }
                    }
                }, { error ->
                    Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show()
                    Log.d("Auth", error.message.toString())
                })
                requestQueue.add(objReq)
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

    override fun onStart() {
        super.onStart()
        try {
            val url = "https://stage1.uidai.gov.in/onlineekyc/getOtp/"
            val requestQueue = Volley.newRequestQueue(this)
            val txnID = UUID.randomUUID().toString()
            otpModel = OTP(aadhar, txnID)
            val params = JSONObject()
            params.put("uid", otpModel?.uid)
            params.put("txnId", otpModel?.txnId)
            val objRequest = JsonObjectRequest(Request.Method.POST, url, params, { response ->
                if (response.getString("status") == "y" || response.getString("errCode") == "null") {
                    Toast.makeText(this, "Otp sent successfully", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show()
                    Log.d("OTP", response.getString("errCode"))
                }
            }, { error ->
                Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show()
                Log.d("OTP", error.message.toString())
            })
            requestQueue.add(objRequest)
        } catch (e: Exception) {
            Log.d("OTP", e.message.toString())
        }
    }
}