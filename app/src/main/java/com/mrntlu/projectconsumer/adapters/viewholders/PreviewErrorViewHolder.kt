package com.mrntlu.projectconsumer.adapters.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellPreviewErrorBinding
import com.mrntlu.projectconsumer.interfaces.ErrorViewHolderBind
import com.mrntlu.projectconsumer.interfaces.Interaction

class PreviewErrorViewHolder<Model>(
    private val binding: CellPreviewErrorBinding,
): RecyclerView.ViewHolder(binding.root), ErrorViewHolderBind<Model> {
    override fun bind(errorMessage: String?, interaction: Interaction<Model>, shouldHideCancelButton: Boolean) {
        binding.errorText.text = errorMessage

        binding.refreshButton.setOnClickListener { interaction.onErrorRefreshPressed() }
    }
}