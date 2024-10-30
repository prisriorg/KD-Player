package com.prisri.kdplayer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.prisri.kdplayer.activity.FoldersActivity
import com.prisri.kdplayer.databinding.FoldersViewBinding
import com.prisri.kdplayer.helper.Folder

class FoldersAdapter(private val context: Context, private var foldersList: ArrayList<Folder>) : RecyclerView.Adapter<FoldersAdapter.MyHolder>() {
    class MyHolder(binding: FoldersViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val folderName = binding.folderNameFV
        val folderSize = binding.textView2
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(FoldersViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.folderName.text = foldersList[position].folderName
        holder.folderSize.text = "" + foldersList[position].folderSize

        holder.root.setOnClickListener {
            val intent = Intent(context, FoldersActivity::class.java)
            intent.putExtra("position", position)
            ContextCompat.startActivity(context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return foldersList.size
    }
}