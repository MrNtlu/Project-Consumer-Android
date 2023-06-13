package com.mrntlu.projectconsumer.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mrntlu.projectconsumer.databinding.CellGridBinding
import com.mrntlu.projectconsumer.models.common.GenreUI
import com.mrntlu.projectconsumer.utils.setGone

class GridAdapter(
    private val context: Context,
    private val items: List<GenreUI>,
    private val onDiscoveryClicked: () -> Unit,
    private val onGenreClicked: (String) -> Unit,
) : BaseAdapter() {

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: CellGridBinding

        if (convertView == null) {
            binding = CellGridBinding.inflate(LayoutInflater.from(context), parent, false)
            binding.root.tag = binding
        } else {
            binding = convertView.tag as CellGridBinding
        }

        val item = getItem(position) as GenreUI

        binding.genreTV.text = item.genre
        Glide.with(context).load(item.image).addListener(object: RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                binding.genreIV.setGone()
                return false
            }

            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean { return false }

        }).into(binding.genreIV)

        binding.root.setOnClickListener {
            if (item.genre == "Discover") {
                onDiscoveryClicked()
            } else {
                onGenreClicked(item.genre)
            }
        }

        return binding.root
    }
}
