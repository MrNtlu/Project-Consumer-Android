package com.mrntlu.projectconsumer.ui.common

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mrntlu.projectconsumer.adapters.FilterAdapter
import com.mrntlu.projectconsumer.databinding.LayoutDiscoverBottomSheetBinding
import com.mrntlu.projectconsumer.interfaces.DiscoverOnBottomSheet
import com.mrntlu.projectconsumer.models.common.BackendRequestMapper
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.Constants.AnimeGenreList
import com.mrntlu.projectconsumer.utils.Constants.MovieGenreList
import com.mrntlu.projectconsumer.utils.Constants.MovieStatusRequests
import com.mrntlu.projectconsumer.utils.Constants.SortRequests
import com.mrntlu.projectconsumer.utils.Constants.TVGenreList
import com.mrntlu.projectconsumer.utils.Constants.TVSeriesStatusRequests
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiscoverBottomSheet(
    private val initialGenre: String? = null,
    private val contentType: Constants.ContentType,
    private val discoverOnBottomSheet: DiscoverOnBottomSheet,
): BottomSheetDialogFragment() {

    //TODO OnApply, OnReset and OnClose Callbacks, create interface
    //TODO Full page bottom sheet

    companion object {
        const val TAG = "DiscoverBottomSheet"
    }

    private var _binding: LayoutDiscoverBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var sortAdapter: FilterAdapter? = null
    private var genreAdapter: FilterAdapter? = null
    private var statusAdapter: FilterAdapter? = null
    private var releaseDateAdapter: FilterAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutDiscoverBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
        setRecyclerView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { layout ->
                val behaviour = BottomSheetBehavior.from(layout)
                setupFullHeight(layout)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return dialog

    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    private fun setListeners() {
        binding.applyButton.setOnClickListener {
            discoverOnBottomSheet.onApply(
                genreAdapter?.getSelected(),
                statusAdapter?.getSelected(),
                sortAdapter?.getSelected()!!,
                null,
                null //TODO Implement, 90's etc. and input 1990 2000
            )
        }

        binding.resetButton.setOnClickListener {
            discoverOnBottomSheet.onReset()
        }

        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun setRecyclerView() {
        binding.sortRV.apply {
            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout

            sortAdapter = FilterAdapter(SortRequests, false)
            adapter = sortAdapter
        }

        binding.genreRV.apply {
            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout

            val list = when(contentType) {
                Constants.ContentType.ANIME -> AnimeGenreList.subList(1, AnimeGenreList.size).map { BackendRequestMapper(it.genre, it.genre.lowercase()) }
                Constants.ContentType.MOVIE -> MovieGenreList.subList(1, MovieGenreList.size).map { BackendRequestMapper(it.genre, it.genre.lowercase()) }
                Constants.ContentType.TV -> TVGenreList.subList(1, TVGenreList.size).map { BackendRequestMapper(it.genre, it.genre.lowercase()) }
                Constants.ContentType.GAME -> TODO()
            }

            genreAdapter = FilterAdapter(list)
            adapter = genreAdapter

            if (initialGenre != null) {
                val index = list.indexOfFirst {
                    it.name == initialGenre
                }

                genreAdapter?.setSelectedIndex(index)
                scrollToPosition(index)
            }
        }

        binding.statusRV.apply {
            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout

            statusAdapter = FilterAdapter(
                when(contentType) {
                    Constants.ContentType.ANIME -> TODO()
                    Constants.ContentType.MOVIE -> MovieStatusRequests
                    Constants.ContentType.TV -> TVSeriesStatusRequests
                    Constants.ContentType.GAME -> TODO()
                }
            )
            adapter = statusAdapter
        }

        binding.releaseDateRV.apply {
            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout


        }
    }

    override fun onDestroyView() {
        sortAdapter = null
        genreAdapter = null
        statusAdapter = null
        releaseDateAdapter = null

        super.onDestroyView()
        _binding = null
    }
}