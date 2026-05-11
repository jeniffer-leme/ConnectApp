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

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            binding.imgPost.setImageURI(it)
            prepareImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchLocation()

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnSelectImage.setOnClickListener {
            getImage.launch("image/*")
        }

        binding.btnPublish.setOnClickListener {
            publishPost()
        }
    }

    private fun prepareImage(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
            val byteArray = outputStream.toByteArray()

            imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    updateCityName(location.latitude, location.longitude)
                } else {
                    binding.txtCity.text = "Localização: GPS não fixado"
                }
            }
            .addOnFailureListener {
                binding.txtCity.text = "Localização: Erro de sensor"
            }
    }

    private fun updateCityName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val cityName = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Desconhecida"
                binding.txtCity.text = "Localização: $cityName"
            } else {
                binding.txtCity.text = "Localização: Coordenadas sem cidade"
            }
        } catch (e: Exception) {
            binding.txtCity.text = "Localização: Erro no serviço de mapas"
        }
    }

    private fun publishPost() {
        val descricao = binding.editDescription.text.toString()
        val localizacao = binding.txtCity.text.toString()

        if (imageBase64 == null) {
            Toast.makeText(this, "Por favor, selecione uma foto.", Toast.LENGTH_SHORT).show()
            return
        }
        if (descricao.isEmpty()) {
            Toast.makeText(this, "Escreva uma legenda para sua foto.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnPublish.isEnabled = false
        binding.btnPublish.text = "Publicando..."

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
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnPublish.isEnabled = true
                binding.btnPublish.text = "Publicar"
                Toast.makeText(this, "Erro ao publicar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
}