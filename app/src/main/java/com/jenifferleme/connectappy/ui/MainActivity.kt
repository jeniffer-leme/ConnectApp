package com.jenifferleme.connectappy.ui

import android.content.Intent
import android.os.Bundle
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

    // Variáveis do Feed e Banco de Dados
    private lateinit var adapter: PostAdapter
    private val postList = mutableListOf<Post>()
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // Inicializa o RecyclerView e busca os posts
        setupRecyclerView()
        fetchPosts()

        binding.btnCreatePost.setOnClickListener {
            val intent = Intent(this, PostActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = PostAdapter(postList)
        binding.rvFeed.adapter = adapter
    }

    private fun fetchPosts() {
        // Busca no Firestore ordenando pelas mais recentes (RF3-1)
        db.collection("postagens")
            .orderBy("data", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                // 1. Se houver erro, avisa e para a execução
                if (error != null) {
                    Toast.makeText(this, "Erro ao carregar feed", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // 2. Se não houver erro, processa os dados (DENTRO do listener)
                postList.clear()
                if (value != null) {
                    for (doc in value) {
                        val post = doc.toObject(Post::class.java)
                        postList.add(post)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}