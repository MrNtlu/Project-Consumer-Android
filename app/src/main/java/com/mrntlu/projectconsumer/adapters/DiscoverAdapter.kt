package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mrntlu.projectconsumer.databinding.CellGridBinding
import com.mrntlu.projectconsumer.models.common.GenreUI
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener

class DiscoverAdapter(
    private val genreList: ArrayList<GenreUI>,
    private val onDiscoveryClicked: () -> Unit,
    private val onGenreClicked: (String) -> Unit,
): RecyclerView.Adapter<DiscoverAdapter.ItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(CellGridBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = genreList.count()

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = genreList[position]

        holder.binding.apply {
            genreTV.text = item.genre
            Glide.with(root.context).load(item.image).addListener(object: RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                    genreIV.setGone()
                    return false
                }

                override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean { return false }

            }).sizeMultiplier(0.75f).into(genreIV)
        }

        holder.binding.root.setSafeOnClickListener {
            if (item.genre == "Discover") {
                onDiscoveryClicked()
            } else {
                onGenreClicked(item.genre)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun changeList(newList: ArrayList<GenreUI>) {
        if (genreList != newList) {
            genreList.clear()
            genreList.addAll(newList)
            notifyDataSetChanged()
        }
    }

    inner class ItemViewHolder(
        val binding: CellGridBinding,
    ): RecyclerView.ViewHolder(binding.root)
}