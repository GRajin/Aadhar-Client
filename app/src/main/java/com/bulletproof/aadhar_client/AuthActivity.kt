package com.bulletproof.aadhar_client

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.bulletproof.aadhar_client.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private var binding: ActivityAuthBinding? = null
    lateinit var ekycFile: Uri

    private val fileSelect = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let {
            binding?.progress?.visibility = View.VISIBLE
            ekycFile = it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnUpload?.setOnClickListener {
            fileSelect.launch("*/*")
        }

    }
}