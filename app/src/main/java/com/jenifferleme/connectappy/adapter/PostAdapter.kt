package com.jenifferleme.connectappy.ui.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jenifferleme.connectappy.databinding.ItemPostBinding
import com.jenifferleme.connectappy.model.Post

class PostAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.binding.txtPostAutor.text = post.autor
        holder.binding.txtPostLocal.text = post.localizacao
        holder.binding.txtPostDescricao.text = post.texto

        // Conversão de Base64 para Imagem (O pulo do gato)
        if (post.imagem.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(post.imagem, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.binding.imgPostFeed.setImageBitmap(decodedImage)
            } catch (e: Exception) {
                // Se der erro, mantém uma imagem padrão ou vazio
            }
        }
    }

    override fun getItemCount() = posts.size
}