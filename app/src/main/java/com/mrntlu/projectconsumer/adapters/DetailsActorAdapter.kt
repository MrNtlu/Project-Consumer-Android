package com.mrntlu.projectconsumer.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.CellDetailsActorBinding
import com.mrntlu.projectconsumer.models.common.DetailsUI
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisible

class DetailsActorAdapter(
    private val detailsList: List<DetailsUI>,
    private val transformImage: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable> = { centerCrop() }
): RecyclerView.Adapter<DetailsActorAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(CellDetailsActorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = detailsList.count()

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = detailsList[position]

        holder.binding.apply {
            circleIVProgressBar.setVisible()
            circleIV.setVisible()
            circlePlaceholderIV.setGone()

            circlePlaceholderIV.setImageDrawable(ContextCompat.getDrawable(root.context, R.drawable.ic_person_75))
            circleIV.loadWithGlide(item.image,circlePlaceholderIV, circleIVProgressBar, transformImage)

            nameTV.text = item.title
            subTV.text = item.subTitle
        }

        holder.binding.root.setOnClickListener {
            Toast.makeText(it.context, "Details Soon", Toast.LENGTH_SHORT).show()
        }
    }

    inner class ItemHolder(
        val binding: CellDetailsActorBinding,
    ): RecyclerView.ViewHolder(binding.root)
}