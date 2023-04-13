package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.adapters.viewholders.ErrorViewHolder
import com.mrntlu.projectconsumer.adapters.viewholders.LoadingViewHolder
import com.mrntlu.projectconsumer.databinding.CellErrorBinding
import com.mrntlu.projectconsumer.databinding.CellLoadingBinding
import com.mrntlu.projectconsumer.databinding.CellPreviewItemBinding
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.interfaces.ItemViewHolderBind
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setVisible

class MovieAdapter(
    override val interaction: Interaction<Movie>
): BaseAdapter<Movie>(interaction) {
    override fun handleDiffUtil(newList: ArrayList<Movie>) {
        val diffUtil = DiffUtilCallback<Movie>(
            arrayList,
            newList
        )

        val diffResults = DiffUtil.calculateDiff(diffUtil, true)

        arrayList = newList.toList() as ArrayList<Movie>
        diffResults.dispatchUpdatesTo(this)
    }

    //TODO https://github.com/MrNtlu/Mobillium-Case/blob/master/app/src/main/java/com/example/mobilliumcase/adapters/UpcomingMovieAdapter.kt
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Loading.value -> LoadingViewHolder(CellLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Error.value ->ErrorViewHolder<Movie>(CellErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> return ItemViewHolder(CellPreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    //TODO Handle null image error
    //https://image.tmdb.org/t/p/original/null
    inner class ItemViewHolder(
        private val binding: CellPreviewItemBinding,
    ): RecyclerView.ViewHolder(binding.root), ItemViewHolderBind<Movie> {
        override fun bind(item: Movie, position: Int, interaction: Interaction<Movie>) {
            binding.apply {
                previewIVProgress.setVisible()
                previewIV.loadWithGlide(item.imageURL, binding.previewIVProgress) {
                    centerCrop().transform(RoundedCorners(16))
                }

                root.setOnClickListener {
                    interaction.onItemSelected(item, position)
                }
            }
        }
    }
}