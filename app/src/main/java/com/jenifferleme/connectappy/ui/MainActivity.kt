package com.jenifferleme.connectappy.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jenifferleme.connectappy.databinding.ActivityMainBinding
import com.jenifferleme.connectappy.model.Post
import com.jenifferleme.connectappy.ui.adapter.PostAdapter
import com.jenifferleme.connectappy.ui.login.LoginActivity
import com.jenifferleme.connectappy.ui.post.PostActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var adapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        setupRecyclerView()
        fetchPosts()

        binding.btnCreatePost.setOnClickListener {
            startActivity(Intent(this, PostActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Lógica da Busca [RF3-1]
        binding.btnSearch.setOnClickListener {
            val cidade = binding.editSearchCity.text.toString().trim()
            searchByCity(cidade)
        }

        binding.btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(postList)
        binding.rvFeed.adapter = adapter
    }

    // 1. Busca inicial (Feed completo)
    private fun fetchPosts() {
        db.collection("postagens")
            .orderBy("data", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Erro ao carregar feed", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                postList.clear()
                value?.let {
                    for (doc in it) {
                        val post = doc.toObject(Post::class.java)
                        post.id = doc.id
                        postList.add(post)
                    }
                    adapter.notifyDataSetChanged()
                    updateEmptyState() // Atualiza se está vazio ou não
                }
            }
    }

    // 2. Função de Busca por Cidade [RF3-1]
    private fun searchByCity(cityName: String) {
        val query = if (cityName.isEmpty()) {
            db.collection("postagens").orderBy("data", Query.Direction.DESCENDING)
        } else {
            db.collection("postagens")
                .whereEqualTo("localizacao", "Localização: $cityName")
                .orderBy("data", Query.Direction.DESCENDING)
        }

        query.addSnapshotListener { value, error ->
            if (error != null) {
                Toast.makeText(this, "Erro na busca", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            postList.clear()
            value?.let {
                for (doc in it) {
                    val post = doc.toObject(Post::class.java)
                    post.id = doc.id
                    postList.add(post)
                }
                adapter.notifyDataSetChanged()
                updateEmptyState() // Atualiza se a busca retornou algo
            }
        }
    }

    // 3. Gerencia o aviso de "Nada encontrado"
    private fun updateEmptyState() {
        if (postList.isEmpty()) {
            binding.txtEmptyFeed.visibility = View.VISIBLE
            binding.rvFeed.visibility = View.GONE
        } else {
            binding.txtEmptyFeed.visibility = View.GONE
            binding.rvFeed.visibility = View.VISIBLE
        }
    }
}