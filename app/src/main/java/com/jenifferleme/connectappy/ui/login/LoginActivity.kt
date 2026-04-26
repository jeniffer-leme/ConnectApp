package com.jenifferleme.connectappy.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jenifferleme.connectappy.databinding.ActivityLoginBinding
import com.jenifferleme.connectappy.ui.register.RegisterActivity

class LoginActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            // Lógica de login (Validação de campos)
        }

        binding.txtCreateUser.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}