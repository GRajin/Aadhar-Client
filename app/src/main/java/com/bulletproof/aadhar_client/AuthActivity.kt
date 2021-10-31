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
import com.bulletproof.aadhar_client.databinding.ActivityAuthBinding
import com.bulletproof.aadhar_client.models.Auth
import com.bulletproof.aadhar_client.models.OTP
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject
import java.util.*


class AuthActivity : AppCompatActivity() {

    private var binding: ActivityAuthBinding? = null
    private lateinit var otpModel: OTP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnSend?.setOnClickListener {
            binding?.progress?.visibility = View.VISIBLE
            val aadhar = (binding?.layPhone?.editText as TextInputEditText).text.toString()
            sendOtp(aadhar)
            binding?.progress?.visibility = View.INVISIBLE
        }

        binding?.btnAuth?.setOnClickListener {
            binding?.progress?.visibility = View.VISIBLE
            authOtp()
            binding?.progress?.visibility = View.INVISIBLE
        }

    }

    private fun sendOtp(aadhar: String) {
        try {
            val url = "https://stage1.uidai.gov.in/onlineekyc/getOtp/"
            val requestQueue = Volley.newRequestQueue(this)
            val txnID = UUID.randomUUID().toString()
            otpModel = OTP(aadhar, txnID)
            val params = JSONObject()
            params.put("uid", otpModel.uid)
            params.put("txnId", otpModel.txnId)
            val objRequest = JsonObjectRequest(Request.Method.POST, url, params, { response ->
                if (response.getString("status") == "y" || response.getString("errCode") == "null") {
                    Toast.makeText(this, "Otp sent successfully", Toast.LENGTH_LONG).show()
                    binding?.layOtp?.isEnabled = true
                    binding?.btnAuth?.isEnabled = true
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

    private fun authOtp() {
        try {
            val url = "https://stage1.uidai.gov.in/onlineekyc/getAuth/"
            val requestQueue = Volley.newRequestQueue(this)
            val otp = (binding?.layOtp?.editText as TextInputEditText).text.toString()
            val authModel = Auth(otpModel.uid, otpModel.txnId, otp)
            val params = JSONObject()
            params.put("uid", authModel.uid)
            params.put("txnId", authModel.txnId)
            params.put("otp", authModel.otp)
            val objReq = JsonObjectRequest(Request.Method.POST, url, params, { response ->
                if (response.getString("status") == "y" || response.getString("errCode") == "null") {
                    Toast.makeText(this, "Authenticated Successfully", Toast.LENGTH_LONG).show()
                    binding?.progress?.visibility = View.INVISIBLE
                    val kycIntent = Intent(this, EkycActivity::class.java)
                    kycIntent.putExtra("aadhar", authModel.uid)
                    startActivity(kycIntent)
                    finish()
                } else {
                    if(response.getString("errCode") == "400") {
                        Toast.makeText(this, "Invalid OTP value", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show()
                        Log.d("Auth", response.getString("errCode"))
                    }
                    binding?.progress?.visibility = View.INVISIBLE
                }
            }, { error ->
                Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show()
                Log.d("Auth", error.message.toString())
                binding?.progress?.visibility = View.INVISIBLE
            })
            requestQueue.add(objReq)
        } catch (e: Exception) {
            Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show()
            Log.d("Auth", e.message.toString())
            binding?.progress?.visibility = View.INVISIBLE
        }
    }
}