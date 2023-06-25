package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellProfileImageBinding
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible

class ProfileImageAdapter: RecyclerView.Adapter<ProfileImageAdapter.ItemHolder>() {

    private var selectedItemIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(CellProfileImageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = Constants.ProfileImageList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val image = Constants.ProfileImageList[position]

        holder.binding.apply {
            highlightView.setVisibilityByCondition(position != selectedItemIndex)
            circleIVProgressBar.setVisible()
            circleIV.setVisible()

            circleIV.loadWithGlide(image, null, circleIVProgressBar) { centerCrop() }
        }

        holder.binding.root.setOnClickListener {
            onClick(holder.adapterPosition)
        }
    }

    fun getSelectedImage() = Constants.ProfileImageList[selectedItemIndex]

    fun setSelectedImage(newImage: String) {
        val index = Constants.ProfileImageList.indexOfFirst {
            it == newImage
        }

        if (index >= 0 && index < Constants.ProfileImageList.size)
            onClick(index)
    }

    private fun onClick(adapterPosition: Int) {
        val oldPosition = selectedItemIndex
        selectedItemIndex = adapterPosition

        notifyItemChanged(oldPosition)
        notifyItemChanged(selectedItemIndex)
    }

    inner class ItemHolder(
        val binding: CellProfileImageBinding
    ): RecyclerView.ViewHolder(binding.root)
}