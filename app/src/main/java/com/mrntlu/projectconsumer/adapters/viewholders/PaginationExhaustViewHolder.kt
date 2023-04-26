package com.mrntlu.projectconsumer.adapters.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.databinding.CellPaginationExhaustBinding
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.interfaces.PaginationExhaustViewHolderBind
import com.mrntlu.projectconsumer.models.main.movie.Movie

class PaginationExhaustViewHolder(
    private val binding: CellPaginationExhaustBinding
): RecyclerView.ViewHolder(binding.root), PaginationExhaustViewHolderBind<Movie> {
    override fun bind(interaction: Interaction<Movie>) {
        binding.backToTopButton.setOnClickListener { interaction.onExhaustButtonPressed() }
    }
}