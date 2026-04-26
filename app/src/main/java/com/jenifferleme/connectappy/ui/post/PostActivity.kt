package com.jenifferleme.connectappy.ui.post

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jenifferleme.connectappy.databinding.ActivityPostBinding
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Locale

class PostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostBinding
    private var imageBase64: String? = null

    // Inicialização do Firebase
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // 1. Lógica para abrir a galeria e selecionar a imagem
    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.imgPost.setImageURI(it) // Mostra a imagem no ImageView
            prepareImage(it) // Inicia a conversão para Base64
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tenta buscar a localização assim que a tela abre
        fetchLocation()

        binding.btnSelectImage.setOnClickListener {
            getImage.launch("image/*")
        }

        binding.btnPublish.setOnClickListener {
            publishPost()
        }
    }

    // 2. Converte a imagem em Base64 com compressão (Dica do Professor)
    private fun prepareImage(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val outputStream = ByteArrayOutputStream()
            // Comprimimos em 40% para garantir que fique bem abaixo de 1MB
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
            val byteArray = outputStream.toByteArray()

            imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Implementa a Localização Automática via GPS [RF2-1]
    private fun fetchLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Verifica permissões de localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val geocoder = Geocoder(this, Locale.getDefault())
                try {
                    // Transforma coordenadas em nome de cidade
                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val cityName = addresses[0].locality ?: "Cidade desconhecida"
                        binding.txtCity.text = "Localização: $cityName"
                    }
                } catch (e: Exception) {
                    binding.txtCity.text = "Localização indisponível"
                }
            }
        }
    }

    // 4. Salva os dados no Firestore [RF2-2]
    private fun publishPost() {
        val descricao = binding.editDescription.text.toString()
        val localizacao = binding.txtCity.text.toString()

        // Validações
        if (imageBase64 == null) {
            Toast.makeText(this, "Por favor, selecione uma foto.", Toast.LENGTH_SHORT).show()
            return
        }
        if (descricao.isEmpty()) {
            Toast.makeText(this, "Escreva uma legenda para sua foto.", Toast.LENGTH_SHORT).show()
            return
        }

        // Bloqueia o botão para evitar múltiplos cliques
        binding.btnPublish.isEnabled = false
        binding.btnPublish.text = "Publicando..."

        // Cria o mapa de dados para o Firestore
        val post = hashMapOf(
            "texto" to descricao,
            "imagem" to imageBase64,
            "localizacao" to localizacao,
            "autor" to (auth.currentUser?.email ?: "Usuário desconhecido"),
            "data" to FieldValue.serverTimestamp()
        )

        db.collection("postagens")
            .add(post)
            .addOnSuccessListener {
                Toast.makeText(this, "Postagem realizada com sucesso!", Toast.LENGTH_SHORT).show()
                finish() // Volta para a tela principal
            }
            .addOnFailureListener { e ->
                binding.btnPublish.isEnabled = true
                binding.btnPublish.text = "Publicar"
                Toast.makeText(this, "Erro ao publicar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}