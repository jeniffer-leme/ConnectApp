package com.jenifferleme.connectappy.ui.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.jenifferleme.connectappy.databinding.ItemPostBinding
import com.jenifferleme.connectappy.model.Post
import java.text.SimpleDateFormat
import java.util.Locale

class PostAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        val context = holder.itemView.context

        // Dados básicos
        holder.binding.txtPostAutor.text = post.autor
        holder.binding.txtPostLocal.text = post.localizacao
        holder.binding.txtPostDescricao.text = post.texto

        // 1. Formatar a Data
        post.data?.let { timestamp ->
            val date = timestamp.toDate()
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            holder.binding.txtPostData.text = sdf.format(date)
        }

        // 2. Conversão de Base64 para Imagem
        holder.binding.imgPostFeed.setImageBitmap(null) // Limpa para evitar imagem errada ao rolar
        if (post.imagem.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(post.imagem, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.binding.imgPostFeed.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                // Em caso de erro, pode definir uma imagem de erro aqui
            }
        }

        // 3. Lógica de Exclusão (Segurança)
        val currentUserEmail = Firebase.auth.currentUser?.email
        if (post.autor == currentUserEmail) {
            holder.binding.btnDeletePost.visibility = View.VISIBLE
            holder.binding.btnDeletePost.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Excluir Postagem")
                    .setMessage("Tem certeza que deseja apagar esta foto?")
                    .setPositiveButton("Sim") { _, _ ->
                        Firebase.firestore.collection("postagens")
                            .document(post.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "Excluído com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Erro ao excluir", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Não", null)
                    .show()
            }
        } else {
            holder.binding.btnDeletePost.visibility = View.GONE
        }
    }

    override fun getItemCount() = posts.size
}