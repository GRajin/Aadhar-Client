package com.bulletproof.aadhar_client

import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import com.bulletproof.aadhar_client.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    var intFinger = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        binding!!.cardView.setOnClickListener { v: View? -> }
    }

    override fun onStart() {
        super.onStart()
        checkBiometric()
    }

    private var fingerIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Enable Fingerprint", Toast.LENGTH_LONG).show()
            }
        }

    private fun checkBiometric() {
        val manager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        if (!manager.isKeyguardSecure) {
            Toast.makeText(this, "Please enable fingerprint in settings.", Toast.LENGTH_LONG).show()
            finish()
        } else {
            val biometricManager = BiometricManager.from(this)
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    val executor = ContextCompat.getMainExecutor(this)
                    val biometricPrompt = BiometricPrompt(
                        this@MainActivity,
                        executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationError(
                                errorCode: Int,
                                errString: CharSequence
                            ) {
                                super.onAuthenticationError(errorCode, errString)
                                Toast.makeText(
                                    applicationContext,
                                    "Authentication error: $errString",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }

                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)
                                Toast.makeText(
                                    applicationContext,
                                    "Authentication succeeded!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onAuthenticationFailed() {
                                super.onAuthenticationFailed()
                                Toast.makeText(
                                    applicationContext,
                                    "Authentication failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                                intFinger += 1
                                if (intFinger > 5) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Please try after sometime...",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
                            }
                        })
                    val promptInfo = PromptInfo.Builder()
                        .setTitle("Biometric Login")
                        .setSubtitle("Log in using your biometric credential")
                        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                        .build()
                    biometricPrompt.authenticate(promptInfo)
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    // Prompts the user to create credentials that your app accepts.
                    var enrollIntent: Intent? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                        enrollIntent.putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )
                    }
                    fingerIntent.launch(enrollIntent)
                }
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                }
                BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                }
                BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                }
            }
        }
    }
}