package com.mrntlu.projectconsumer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.mrntlu.projectconsumer.databinding.CellPreviewItemBinding
import com.mrntlu.projectconsumer.models.main.game.GameDetailsRelation
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.isEmptyOrBlank
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisible

class GameRelationsAdapter(
    private val relationList: List<GameDetailsRelation>,
    private val onClick: (Int, Int) -> Unit,
): RecyclerView.Adapter<GameRelationsAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameRelationsAdapter.ItemHolder {
        return ItemHolder(CellPreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount() = relationList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val relation = relationList[position]

        holder.binding.apply {
            val radiusInPx = root.context.dpToPxFloat(8f)

            previewCard.setGone()
            previewShimmerLayout.setVisible()
            previewShimmerCV.radius = radiusInPx
            previewGameCV.setVisible()

            (previewIV.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "16:9"
            (previewCard.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "16:9"
            (previewShimmerLayout.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "16:9"

            val layoutParams = root.layoutParams
            layoutParams.height = root.context.dpToPx(130f)
            layoutParams.width = LayoutParams.WRAP_CONTENT

            val marginParams = root.layoutParams as ViewGroup.MarginLayoutParams
            marginParams.setMargins(0, 4, 16, 4)

            val shapeAppearanceModelBuilder = ShapeAppearanceModel.Builder().apply {
                setBottomLeftCorner(CornerFamily.ROUNDED, radiusInPx)
                setBottomRightCorner(CornerFamily.ROUNDED, radiusInPx)
            }
            val shapeAppearanceModel = shapeAppearanceModelBuilder.build()
            previewGameCV.shapeAppearanceModel = shapeAppearanceModel

            previewIV.scaleType = ImageView.ScaleType.CENTER_CROP
            previewIV.loadWithGlide(relation.imageURL, previewCard, previewShimmerLayout) {
                transform(CenterCrop(), RoundedCorners(radiusInPx.toInt()))
            }

            val titleStr = if(!relation.title.isEmptyOrBlank()) relation.title else relation.titleOriginal
            previewTV.text = titleStr
            previewGameTitleTV.text = titleStr

            root.setSafeOnClickListener {
                onClick(relation.rawgID, position)
            }
        }
    }

    inner class ItemHolder(
        val binding: CellPreviewItemBinding,
    ): RecyclerView.ViewHolder(binding.root)
}