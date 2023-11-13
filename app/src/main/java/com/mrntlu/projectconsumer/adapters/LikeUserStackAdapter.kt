package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.mrntlu.projectconsumer.databinding.CellUserStackBinding
import com.mrntlu.projectconsumer.models.main.review.Author
import com.mrntlu.projectconsumer.utils.loadWithGlide

class LikeUserStackAdapter(
    private var authorList: ArrayList<Author>,
): RecyclerView.Adapter<LikeUserStackAdapter.ItemHolder>() {

    private fun handleDiffUtil(newList: ArrayList<Author>) {
        val diffUtil = DiffUtilCallback(
            authorList,
            newList
        )
        val diffResults = DiffUtil.calculateDiff(diffUtil, true)

        authorList = newList.toCollection(ArrayList())

        diffResults.dispatchUpdatesTo(this@LikeUserStackAdapter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(CellUserStackBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = authorList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val user = authorList[position]

        holder.binding.userImage.loadWithGlide(user.image, null, holder.binding.userProgressBar, sizeMultiplier = 0.8f) {
            transform(CenterCrop())
        }
    }

    fun handleOperation(newList: ArrayList<Author>) {
        handleDiffUtil(newList.toMutableList() as ArrayList<Author>)
    }

    inner class ItemHolder(
        val binding: CellUserStackBinding
    ): RecyclerView.ViewHolder(binding.root)
}