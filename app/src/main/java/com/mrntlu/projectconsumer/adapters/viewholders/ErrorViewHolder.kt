package com.mrntlu.projectconsumer.adapters.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellErrorBinding
import com.mrntlu.projectconsumer.interfaces.ErrorViewHolderBind
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition

class ErrorViewHolder<Model>(
    private val binding: CellErrorBinding,
): RecyclerView.ViewHolder(binding.root), ErrorViewHolderBind<Model> {
    override fun bind(errorMessage: String?, interaction: Interaction<Model>, shouldHideCancelButton: Boolean) {
        binding.errorText.text = errorMessage

        binding.refreshButton.setOnClickListener { interaction.onErrorRefreshPressed() }
        binding.cancelButton.setOnClickListener { interaction.onCancelPressed() }
        binding.cancelButton.setVisibilityByCondition(shouldHideCancelButton)
    }
}