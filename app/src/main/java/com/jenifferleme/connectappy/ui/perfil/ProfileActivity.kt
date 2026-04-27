package com.jenifferleme.connectappy.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jenifferleme.connectappy.databinding.ActivityProfileBinding
import java.io.ByteArrayOutputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val db = Firebase.firestore
    private var base64Image: String? = null // Para guardar a nova imagem

    // Launcher para abrir a galeria
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // Mostra na tela
                binding.imgProfile.setImageBitmap(bitmap)

                // Converte para Base64 para salvar
                base64Image = bitmapToBase64(bitmap)

            } catch (e: Exception) {
                Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = Firebase.auth.currentUser
        if (user == null) { finish(); return }

        // 1. Carregar Dados Existentes (Nome do Auth)
        binding.editProfileName.setText(user.displayName)

        // 2. Carregar Foto Existente (do Firestore)
        loadExistingPhoto(user.uid)

        // 3. Clique para Mudar Foto
        binding.imgProfile.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
        binding.txtChangePhoto.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        // 4. Salvar Alterações
        binding.btnSaveProfile.setOnClickListener {
            val novoNome = binding.editProfileName.text.toString()
            val novaSenha = binding.editProfilePassword.text.toString()

            if (novoNome.isEmpty()) {
                Toast.makeText(this, "O nome não pode estar vazio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // A) Atualizar Nome no Auth
            val profileUpdates = userProfileChangeRequest {
                displayName = novoNome
            }

            user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    // B) Atualizar Foto no Firestore (se houver uma nova)
                    base64Image?.let {
                        val userMap = hashMapOf(
                            "uid" to user.uid,
                            "fotoPerfil" to it
                        )
                        db.collection("usuarios").document(user.uid).set(userMap)
                    }

                    // C) Atualizar Senha (opcional)
                    if (novaSenha.isNotEmpty()) {
                        if (novaSenha.length < 6) {
                            Toast.makeText(this, "Senha muito curta!", Toast.LENGTH_SHORT).show()
                        } else {
                            user.updatePassword(novaSenha).addOnCompleteListener { passTask ->
                                if (passTask.isSuccessful) {
                                    Toast.makeText(this, "Perfil completo atualizado!", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(this, "Erro na senha (requer login recente)", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Nome e Foto atualizados!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
        }

        binding.btnBackProfile.setOnClickListener { finish() }
    }

    private fun loadExistingPhoto(uid: String) {
        db.collection("usuarios").document(uid).get().addOnSuccessListener { document ->
            if (document != null && document.contains("fotoPerfil")) {
                val photoBase64 = document.getString("fotoPerfil")
                if (!photoBase64.isNullOrEmpty()) {
                    try {
                        val imageBytes = Base64.decode(photoBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        binding.imgProfile.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        // Tratar erro de conversão
                    }
                }
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream) // 70% para não ficar pesado
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}