package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.mrntlu.projectconsumer.adapters.DetailsActorAdapter
import com.mrntlu.projectconsumer.databinding.FragmentDetailsCastBinding
import com.mrntlu.projectconsumer.models.common.DetailsUI
import com.mrntlu.projectconsumer.models.main.movie.MovieDetails
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.setGone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsCastFragment(
    private val movieDetails: MovieDetails,
): BaseFragment<FragmentDetailsCastBinding>() {

    private var actorAdapter: DetailsActorAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsCastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerView()
    }

    private fun setRecyclerView() {
        binding.detailsCastRV.apply {
            if (!movieDetails.actors.isNullOrEmpty()) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    val actorUIList = movieDetails.actors.filter {
                        it.name.isNotEmptyOrBlank()
                    }.map {
                        DetailsUI(
                            it.name,
                            it.image,
                            it.character
                        )
                    }

                    withContext(Dispatchers.Main) {
                        val linearLayout = LinearLayoutManager(context)
                        layoutManager = linearLayout

                        setHasFixedSize(true)

                        actorAdapter = DetailsActorAdapter(
                            actorUIList
                        ) { transform(CenterCrop()) }

                        adapter = actorAdapter
                    }
                }
            } else {
                setGone()
            }
        }
    }

    override fun onDestroyView() {
        actorAdapter = null

        super.onDestroyView()
    }
}