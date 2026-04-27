package com.jenifferleme.connectappy.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jenifferleme.connectappy.ui.MainActivity
import com.jenifferleme.connectappy.databinding.ActivityLoginBinding
import com.jenifferleme.connectappy.ui.register.RegisterActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val currentUser = auth.currentUser
        if (currentUser != null) {
            irParaMain()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.editEmail.text.toString()
            val senha = binding.editPassword.text.toString()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            irParaMain()
                        } else {
                            val erro = task.exception?.message ?: "Erro ao fazer login"
                            Toast.makeText(this, erro, Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        binding.txtCreateUser.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun irParaMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}