package com.mrntlu.projectconsumer.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.FilterAdapter
import com.mrntlu.projectconsumer.databinding.LayoutDiscoverBottomSheetBinding
import com.mrntlu.projectconsumer.interfaces.DiscoverOnBottomSheet
import com.mrntlu.projectconsumer.models.common.BackendRequestMapper
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.Constants.AnimeGenreList
import com.mrntlu.projectconsumer.utils.Constants.AnimeStatusRequests
import com.mrntlu.projectconsumer.utils.Constants.AnimeThemeList
import com.mrntlu.projectconsumer.utils.Constants.DecadeList
import com.mrntlu.projectconsumer.utils.Constants.GameGenreList
import com.mrntlu.projectconsumer.utils.Constants.GamePlatformRequests
import com.mrntlu.projectconsumer.utils.Constants.MovieGenreList
import com.mrntlu.projectconsumer.utils.Constants.MovieStatusRequests
import com.mrntlu.projectconsumer.utils.Constants.SortRequests
import com.mrntlu.projectconsumer.utils.Constants.TVGenreList
import com.mrntlu.projectconsumer.utils.Constants.TVSeriesStatusRequests
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiscoverBottomSheet(
    private val initialSort: String,
    private val initialGenre: String? = null,
    private val initialStatus: String? = null,
    private val initialDecade: String? = null,
    private val initialAnimeTheme: String? = null,
    private val initialGameTBA: Boolean? = null,
    private val initialGamePlatform: String? = null,
    private val contentType: Constants.ContentType,
    private val discoverOnBottomSheet: DiscoverOnBottomSheet,
): BottomSheetDialogFragment() {
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
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
        setRecyclerView()
    }

    /*override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
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
    }*/

    private fun setListeners() {
        binding.applyButton.setOnClickListener {
            discoverOnBottomSheet.onApply(
                genre = genreAdapter?.getSelected(),
                status = if (contentType != Constants.ContentType.GAME)
                    statusAdapter?.getSelected()
                else null,
                sort = sortAdapter?.getSelected()!!,
                from = if (contentType == Constants.ContentType.MOVIE || contentType == Constants.ContentType.TV) {
                    releaseDateAdapter?.getSelected()?.toIntOrNull()
                } else null,
                to = if (contentType == Constants.ContentType.MOVIE || contentType == Constants.ContentType.TV) {
                    releaseDateAdapter?.getSelected()?.toIntOrNull()?.plus(10)
                } else null,
                animeTheme = if (contentType == Constants.ContentType.ANIME)
                    releaseDateAdapter?.getSelected()
                else null,
                gameTBA = if (contentType == Constants.ContentType.GAME)
                    releaseDateAdapter?.getSelected() != null
                else null,
                gamePlatform = if (contentType == Constants.ContentType.GAME)
                    statusAdapter?.getSelected()
                else null
            )

            dismiss()
        }

        binding.resetButton.setOnClickListener {
            genreAdapter?.deselect()
            statusAdapter?.deselect()
            sortAdapter?.deselect()
            releaseDateAdapter?.deselect()
        }
    }

    private fun setRecyclerView() {
        binding.sortRV.apply {
            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout

            sortAdapter = FilterAdapter(SortRequests, false)
            adapter = sortAdapter

            val index = SortRequests.indexOfFirst {
                it.name == initialSort || it.request == initialSort
            }
            sortAdapter?.setSelectedIndex(index)
            scrollToPosition(index)
        }

        binding.genreRV.apply {
            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout

            val list = when(contentType) {
                Constants.ContentType.ANIME -> AnimeGenreList.subList(1, AnimeGenreList.size).map { BackendRequestMapper(it.genre, it.genre) }
                Constants.ContentType.MOVIE -> MovieGenreList.subList(1, MovieGenreList.size).map { BackendRequestMapper(it.genre, it.genre) }
                Constants.ContentType.TV -> TVGenreList.subList(1, TVGenreList.size).map { BackendRequestMapper(it.genre, it.genre) }
                Constants.ContentType.GAME -> GameGenreList.subList(1, GameGenreList.size).map { BackendRequestMapper(it.genre, it.genre) }
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
            if (contentType == Constants.ContentType.GAME)
                binding.statusTV.text = context.getString(R.string.platforms)

            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout

            val list = when(contentType) {
                Constants.ContentType.ANIME -> AnimeStatusRequests
                Constants.ContentType.MOVIE -> MovieStatusRequests
                Constants.ContentType.TV -> TVSeriesStatusRequests
                Constants.ContentType.GAME -> GamePlatformRequests
            }
            statusAdapter = FilterAdapter(list)
            adapter = statusAdapter

            if (
                (contentType == Constants.ContentType.GAME && initialGamePlatform != null) ||
                (contentType != Constants.ContentType.GAME && initialStatus != null)
            ) {
                val condition = if (contentType == Constants.ContentType.GAME) initialGamePlatform else initialStatus

                val index = list.indexOfFirst {
                    it.name == condition || it.request == condition
                }

                statusAdapter?.setSelectedIndex(index)
                scrollToPosition(index)
            }
        }

        binding.releaseDateRV.apply {
            binding.releaseDateTV.text = when(contentType) {
                Constants.ContentType.ANIME -> getString(R.string.themes)
                else -> getString(R.string.release_date)
            }

            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout

            val list = when(contentType) {
                Constants.ContentType.ANIME -> AnimeThemeList
                Constants.ContentType.MOVIE -> DecadeList
                Constants.ContentType.TV -> DecadeList
                Constants.ContentType.GAME -> listOf(BackendRequestMapper("TBA", "TBA"))
            }
            releaseDateAdapter = FilterAdapter(list)
            adapter = releaseDateAdapter

            if (
                (contentType == Constants.ContentType.ANIME && initialAnimeTheme != null) ||
                (contentType == Constants.ContentType.GAME && initialGameTBA != null) ||
                ((contentType == Constants.ContentType.MOVIE || contentType == Constants.ContentType.TV) && initialDecade != null)
            ) {
                val index = when(contentType) {
                    Constants.ContentType.ANIME -> AnimeThemeList.indexOfFirst { it.name == initialAnimeTheme }
                    Constants.ContentType.MOVIE, Constants.ContentType.TV -> DecadeList.indexOfFirst {
                        it.name == "${initialDecade}s"
                    }
                    Constants.ContentType.GAME -> 0
                }

                releaseDateAdapter?.setSelectedIndex(index)
                scrollToPosition(index)
            }
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