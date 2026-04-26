package com.jenifferleme.connectappy.ui.register

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jenifferleme.connectappy.databinding.ActivityRegisterBinding
import com.jenifferleme.connectappy.R

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btnRegister.setOnClickListener {
            val nome = binding.editFullName.text.toString()
            val email = binding.editEmail.text.toString()
            val senha = binding.editPassword.text.toString()
            val confirma = binding.editConfirmPassword.text.toString()

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
            } else if (senha != confirma) {
                Toast.makeText(this, getString(R.string.error_password_mismatch), Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show()


                            finish()
                        } else {
                            val erro = task.exception?.message ?: "Erro desconhecido"
                            Toast.makeText(this, "Erro: $erro", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }
}