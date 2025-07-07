package com.example.dtaquito.forgotPassword

import Beans.auth.forgotPassword.ForgotPasswordRequest
import Interface.PlaceHolder
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.dtaquito.R
import com.example.dtaquito.databinding.ActivityForgotPasswordBinding
import com.example.dtaquito.login.LoginActivity
import com.example.dtaquito.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.RetrofitClient

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val service by lazy { RetrofitClient.instance.create(PlaceHolder::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.recoverBtn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            if (email.isNotEmpty()) {
                sendForgotPasswordRequest(email)
            } else {
                binding.emailInput.error = getString(R.string.enter_valid_email)
            }
        }

        val signInText = binding.signIn.text.toString()
        val signInClickable = getString(R.string.sign_in_clickable_forgot)
        val signInStart = signInText.indexOf(signInClickable)
        val signInEnd = signInStart + signInClickable.length

        if (signInStart >= 0 && signInEnd <= signInText.length) {
            val signInSpannable = SpannableString(signInText)
            signInSpannable.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    navigateToLogin()
                }
            }, signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            signInSpannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.green)),
                signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            signInSpannable.setSpan(UnderlineSpan(), signInStart, signInEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.signIn.text = signInSpannable
            binding.signIn.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun sendForgotPasswordRequest(email: String) {
        lifecycleScope.launch {
            binding.recoverBtn.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            val request = ForgotPasswordRequest(email)
            try {
                val response = withContext(Dispatchers.IO) { service.forgotPassword(request) }
                binding.progressBar.visibility = View.GONE
                binding.recoverBtn.isEnabled = true

                if (response.isSuccessful) {
                    showToast(getString(R.string.email_sent_success))
                    navigateToLogin()
                } else {
                    showToast(getString(R.string.email_send_error))
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.recoverBtn.isEnabled = true
                showToast(getString(R.string.network_error, e.message))
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}