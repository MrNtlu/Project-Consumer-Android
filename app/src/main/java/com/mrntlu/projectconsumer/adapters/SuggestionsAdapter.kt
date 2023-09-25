package com.mrntlu.projectconsumer.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.CellSuggestionBinding
import com.mrntlu.projectconsumer.databinding.CellSuggestionErrorBinding
import com.mrntlu.projectconsumer.databinding.CellSuggestionLoadingBinding
import com.mrntlu.projectconsumer.interfaces.AISuggestionsInteraction
import com.mrntlu.projectconsumer.models.common.AISuggestion
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLater
import com.mrntlu.projectconsumer.utils.Constants.ContentType
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.roundSingleDecimal
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisible

@SuppressLint("NotifyDataSetChanged")
class SuggestionsAdapter(
    val isDarkTheme: Boolean,
    val interaction: AISuggestionsInteraction
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var errorMessage: String? = null
    var isLoading = true

    private var arrayList: ArrayList<AISuggestion> = arrayListOf()

    private fun handleDiffUtil(newList: ArrayList<AISuggestion>) {
        val diffUtil = DiffUtilCallback(
            arrayList,
            newList
        )
        val diffResults = DiffUtil.calculateDiff(diffUtil, false)

        arrayList = newList.toCollection(ArrayList())

        diffResults.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RecyclerViewEnum.Error.value -> ErrorViewHolder(CellSuggestionErrorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RecyclerViewEnum.Loading.value -> LoadingViewHolder(CellSuggestionLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> return ItemViewHolder(CellSuggestionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            RecyclerViewEnum.View.value -> {
                (holder as ItemViewHolder).bind(arrayList[holder.adapterPosition], holder.adapterPosition, interaction)
            }
            RecyclerViewEnum.Error.value -> {
                (holder as ErrorViewHolder).bind()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoading)
            RecyclerViewEnum.Loading.value
        else if (errorMessage != null)
            RecyclerViewEnum.Error.value
        else
            RecyclerViewEnum.View.value
    }

    override fun getItemCount(): Int {
        return if (isLoading)
            1
        else if (errorMessage != null)
            1
        else
            arrayList.size
    }

    fun setErrorView(errorMessage: String) {
        setState(RecyclerViewEnum.Error)
        this.errorMessage = errorMessage
        notifyDataSetChanged()
    }

    fun setLoadingView() {
        if (arrayList.isNotEmpty())
            arrayList.clear()
        setState(RecyclerViewEnum.Loading)
        notifyDataSetChanged()
    }

    fun setData(newList: ArrayList<AISuggestion>) {
        setState(RecyclerViewEnum.View)

        if (arrayList.isNotEmpty())
            arrayList.clear()

        arrayList.addAll(newList)
        notifyDataSetChanged()
    }

    private fun setState(rvEnum: RecyclerViewEnum) {
        when(rvEnum) {
            RecyclerViewEnum.Empty -> {
                isLoading = false
                errorMessage = null
            }
            RecyclerViewEnum.Loading -> {
                isLoading = true
                errorMessage = null
            }
            RecyclerViewEnum.Error -> {
                isLoading = false
            }
            RecyclerViewEnum.View -> {
                isLoading = false
                errorMessage = null
            }
            else -> {}
        }
    }

    fun handleOperation(operation: Operation<ConsumeLater>) {
        val newList = arrayList.toMutableList().map { it.copy() }.toCollection(ArrayList())
        val index = operation.position

        when(operation.operationEnum) {
            OperationEnum.Delete -> {}
            OperationEnum.Update -> {
                newList[index].consumeLater = operation.data
            }
        }

        handleDiffUtil(newList)
    }

    inner class ItemViewHolder(
        private val binding: CellSuggestionBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AISuggestion, position: Int, suggestionsInteraction: AISuggestionsInteraction) {
            binding.apply {
                watchLaterLottie.setAnimation(if(isDarkTheme) R.raw.bookmark_night else R.raw.bookmark)
                watchLaterLottie.frame = if (item.consumeLater == null) 0 else 120

                val radiusInPx = root.context.dpToPxFloat(6f)

                imageInclude.apply {
                    previewCard.setGone()
                    previewShimmerLayout.setVisible()
                    previewShimmerCV.radius = radiusInPx
                    previewGameCV.setGone()
                    previewTV.setGone()

                    previewIV.scaleType = if (item.contentType == "game")
                        ImageView.ScaleType.CENTER_CROP
                    else
                        ImageView.ScaleType.FIT_XY

                    previewIV.loadWithGlide(item.imageURL, previewCard, previewShimmerLayout) {
                        if (item.contentType == "game")
                            transform(CenterCrop(), RoundedCorners(radiusInPx.toInt()))
                        else
                            transform(RoundedCorners(radiusInPx.toInt()))
                    }

                    previewCard.radius = radiusInPx
                    previewIV.contentDescription = item.titleOriginal
                }

                if (item.titleEn.isNotEmptyOrBlank() && item.titleEn != item.titleOriginal) {
                    titleTV.text = item.titleEn
                    titleOriginalTV.text = item.titleOriginal
                } else {
                    titleTV.text = item.titleOriginal
                    titleOriginalTV.setGone()
                }

                contentTypeTV.text = ContentType.fromStringRequest(item.contentType).value
                scoreTV.text = item.score.toDouble().roundSingleDecimal().toString()

                contentTypeIV.setImageDrawable(ContextCompat.getDrawable(
                    root.context,
                    when(ContentType.fromStringRequest(item.contentType)) {
                        ContentType.ANIME -> R.drawable.ic_anime
                        ContentType.MOVIE -> R.drawable.ic_content_type_24
                        ContentType.TV -> R.drawable.ic_tv
                        ContentType.GAME -> R.drawable.ic_game_24
                    }
                ))

                watchLaterLottie.setSafeOnClickListener {
                    suggestionsInteraction.onAddToListPressed(item, position)
                }

                root.setSafeOnClickListener {
                    interaction.onItemSelected(item, position)
                }
            }
        }
    }

    inner class ErrorViewHolder(private val binding: CellSuggestionErrorBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.apply {
                errorText.text = errorMessage

                refreshButton.setSafeOnClickListener {
                    interaction.onErrorRefreshPressed()
                }

                cancelButton.setSafeOnClickListener {
                    interaction.onCancelPressed()
                }
            }
        }
    }

    inner class LoadingViewHolder(binding: CellSuggestionLoadingBinding): RecyclerView.ViewHolder(binding.root)
}