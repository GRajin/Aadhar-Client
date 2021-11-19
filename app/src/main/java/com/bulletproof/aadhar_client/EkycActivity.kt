package com.bulletproof.aadhar_client

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bulletproof.aadhar_client.databinding.ActivityEkycBinding
import com.bulletproof.aadhar_client.models.Auth
import com.bulletproof.aadhar_client.models.OTP
import com.google.android.material.textfield.TextInputEditText
import org.apache.commons.lang3.StringEscapeUtils
import org.json.JSONObject
import org.json.XML
import java.util.*


class EkycActivity : AppCompatActivity() {

    private var binding: ActivityEkycBinding? = null
    private lateinit var aadhar: String
    private lateinit var otpModel: OTP
    private var ekycString: String = ""
    private var escapedStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEkycBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        aadhar = intent.extras?.getString("aadhar").toString()


        binding?.btnDownload?.setOnClickListener {
            binding?.layOtp?.visibility = View.VISIBLE
            binding?.btnAuth?.visibility = View.VISIBLE
            sendOtp()
        }

        binding?.btnAuth?.setOnClickListener {
            authOtp()
        }
    }

    private fun sendOtp() {
        try {
            val url = "https://stage1.uidai.gov.in/onlineekyc/getOtp/"
            val requestQueue = Volley.newRequestQueue(this)
            val txnID = UUID.randomUUID().toString()
            otpModel = OTP(aadhar, txnID)
            val params = JSONObject()
            params.put("uid", otpModel.uid)
            params.put("txnId", otpModel.txnId)
            val objRequest = JsonObjectRequest(Request.Method.POST, url, params, { response ->
                if(response.getString("status") == "y" || response.getString("errCode") == "null") {
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
        } catch(e: Exception) {
            Log.d("OTP", e.message.toString())
        }
    }

    private fun authOtp() {
        try {
            val url = "https://stage1.uidai.gov.in/onlineekyc/getEkyc/"
            val requestQueue = Volley.newRequestQueue(this)
            val otp = (binding?.layOtp?.editText as TextInputEditText).text.toString()
            val authModel = Auth(otpModel.uid, otpModel.txnId, otp)
            val params = JSONObject()
            params.put("uid", authModel.uid)
            params.put("txnId", authModel.txnId)
            params.put("otp", authModel.otp)

            val ekycRequest = JsonObjectRequest(Request.Method.POST, url, params, { response ->
                if(response.getString("status") == "Y" || response.getString("errCode") == "null") {
                    Toast.makeText(this, "Authenticated Successfully", Toast.LENGTH_LONG).show()
                    ekycString = response.getString("eKycString")
                    escapedStr = StringEscapeUtils.escapeXml11(ekycString)
                    val jsonObj = XML.toJSONObject(ekycString)
                    val detailsIntent = Intent(this, ResidentDetailsActivity::class.java)
                    detailsIntent.putExtra("EKYC", jsonObj.toString())
                    detailsIntent.putExtra("EKYCXML", ekycString)
                    startActivity(detailsIntent)
                    finish()
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
            requestQueue.add(ekycRequest)
        } catch(e: Exception) {
            Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show()
            Log.d("Auth", e.message.toString())
        }
    }
}