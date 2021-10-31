package com.bulletproof.aadhar_client

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bulletproof.aadhar_client.databinding.ActivityResidentDetailsBinding
import com.bulletproof.aadhar_client.models.Ekyc
import org.json.JSONObject
import java.util.*


class ResidentDetailsActivity : AppCompatActivity() {

    private var binding: ActivityResidentDetailsBinding? = null
    private lateinit var ekycXml: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResidentDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val strEkyc = intent.extras?.getString("EKYC").toString()
        ekycXml = intent.extras?.getString("EKYCXML").toString()

        val ekyc = parseJson(strEkyc)

        binding?.txtName?.text = ekyc.name
        val aadharNum = "XXXX XXXX ${ekyc.uid.substring(8, 11)}"
        binding?.txtAadhar?.text = aadharNum
        binding?.txtDob?.text = ekyc.dob
        if (ekyc.gender == "M") {
            val m = "Male"
            binding?.txtGen?.text = m
        } else if (ekyc.gender == "F") {
            val f = "Female"
            binding?.txtGen?.text = f
        }
        binding?.txtPhone?.text = ekyc.phone
        binding?.imgProf?.setImageBitmap(convertBase64ToBitmap(ekyc.photo))

        try {
            binding?.btnStateless?.setOnClickListener {
                val intent = Intent(this, QRCodeActivity::class.java)
                intent.putExtra("aadhar", ekyc.uid)
                intent.putExtra("ekyc", strEkyc)
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
            Log.d("Stateless", "$e")
        }
    }

    private fun parseJson(strEkyc: String): Ekyc {
        val jsonObj = JSONObject(strEkyc)
        val kycRes = jsonObj.getJSONObject("KycRes")
        val uidData = kycRes.getJSONObject("UidData")
        val uid = uidData.getString("uid")
        val poi = uidData.getJSONObject("Poi")
        val dob = poi.getString("dob")
        val gender = poi.getString("gender")
        val name = poi.getString("name")
        val phone = poi.getString("phone")
        val photo = uidData.getString("Pht")
        return Ekyc(uid, name, dob, gender, phone, photo)
    }

    private fun convertBase64ToBitmap(b64: String): Bitmap? {
        val imageAsBytes: ByteArray = Base64.decode(b64.toByteArray(), Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
    }
}