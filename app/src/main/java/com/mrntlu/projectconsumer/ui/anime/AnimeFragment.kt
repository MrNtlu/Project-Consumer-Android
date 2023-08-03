package com.mrntlu.projectconsumer.ui.anime

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.mrntlu.projectconsumer.models.main.anime.Anime
import com.mrntlu.projectconsumer.ui.BasePreviewFragment
import com.mrntlu.projectconsumer.ui.common.HomeFragmentDirections
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.viewmodels.main.anime.AnimePreviewViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnimeFragment : BasePreviewFragment<Anime>() {

    private val viewModel: AnimePreviewViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
        setShowcaseRecyclerView(
            onItemClicked = { id ->
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToAnimeDetailsFragment(id)

                navController.navigate(navWithAction)
            },
            onRefreshPressed = { viewModel.fetchPreviewAnimes() }
        )
        setRecyclerView(
            firstOnItemSelected = { id ->
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToAnimeDetailsFragment(id)

                navController.navigate(navWithAction)
            },
            firstOnRefreshPressed = { viewModel.fetchPreviewAnimes() },
            secondOnItemSelected = { id ->
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToAnimeDetailsFragment(id)

                navController.navigate(navWithAction)
            },
            secondOnRefreshPressed = { viewModel.fetchPreviewAnimes() }
        )
        setObservers()
    }

    private fun setListeners() {
        binding.apply {
            setScrollListener()

            seeAllButtonFirst.setOnClickListener {
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToAnimeListFragment(FetchType.UPCOMING.tag)

                navController.navigate(navWithAction)
            }

            seeAllButtonSecond.setOnClickListener {
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToAnimeListFragment(FetchType.TOP.tag)

                navController.navigate(navWithAction)
            }
        }
    }

    private fun setObservers() {
        viewModel.previewList.observe(viewLifecycleOwner) { handleObserver(it) }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (
                (upcomingAdapter?.errorMessage != null && topRatedAdapter?.errorMessage != null) && it
            ) {
                viewModel.fetchPreviewAnimes()
            }
        }
    }

    override fun onDestroyView() {
        viewModel.previewList.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}