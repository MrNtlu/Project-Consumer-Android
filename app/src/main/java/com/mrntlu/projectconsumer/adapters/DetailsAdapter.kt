package com.mrntlu.projectconsumer.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.CellDetailsBinding
import com.mrntlu.projectconsumer.models.common.DetailsUI
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisible

class DetailsAdapter(
    private val placeHolderImage: Int = R.drawable.ic_person_75,
    private val cardCornerRadius: Float = 93F,
    private val detailsList: List<DetailsUI>,
    private val transformImage: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable> = { centerCrop() }
): RecyclerView.Adapter<DetailsAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(CellDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = detailsList.count()

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = detailsList[position]

        holder.binding.apply {
            detailsCircleIVProgressBar.setVisible()
            detailsCircleIV.setVisible()
            detailsCirclePlaceholderIV.setGone()

            detailsIVCard.radius = cardCornerRadius
            detailsCirclePlaceholderIV.setImageDrawable(ContextCompat.getDrawable(root.context, placeHolderImage))
            detailsCircleIV.loadWithGlide(item.image, detailsCirclePlaceholderIV, detailsCircleIVProgressBar, transformImage)

            detailsNameTV.text = item.title
            detailsSubTV.text = item.subTitle
        }

        holder.binding.root.setOnClickListener {
            Toast.makeText(it.context, item.title, Toast.LENGTH_SHORT).show()
        }
    }

    inner class ItemHolder(
        val binding: CellDetailsBinding,
    ): RecyclerView.ViewHolder(binding.root)
}