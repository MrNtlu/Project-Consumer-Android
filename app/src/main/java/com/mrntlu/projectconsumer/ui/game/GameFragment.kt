package com.mrntlu.projectconsumer.ui.game

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.mrntlu.projectconsumer.models.main.game.Game
import com.mrntlu.projectconsumer.ui.BasePreviewFragment
import com.mrntlu.projectconsumer.ui.common.HomeFragmentDirections
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.viewmodels.main.game.GamePreviewViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GameFragment : BasePreviewFragment<Game>() {

    private val viewModel: GamePreviewViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.guideline14.setGuidelinePercent(0.25f)
        binding.upcomingPreviewRV.layoutParams.height = view.context.dpToPx(150f)
        binding.topRatedPreviewRV.layoutParams.height = view.context.dpToPx(150f)

        setListeners()
        setShowcaseRecyclerView(
            isRatioDifferent = true,
            onItemClicked = { id ->
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToGameDetailsFragment(id)

                navController.navigate(navWithAction)
            },
            onRefreshPressed = { viewModel.fetchPreviewGames() }
        )
        setRecyclerView(
            isRatioDifferent = true,
            firstOnItemSelected = { id ->
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToGameDetailsFragment(id)

                navController.navigate(navWithAction)
            },
            firstOnRefreshPressed = { viewModel.fetchPreviewGames() },
            secondOnItemSelected = { id ->
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToGameDetailsFragment(id)

                navController.navigate(navWithAction)
            },
            secondOnRefreshPressed = { viewModel.fetchPreviewGames() }
        )
        setObservers()
    }

    private fun setListeners() {
        binding.apply {
            setScrollListener()

            seeAllButtonFirst.setOnClickListener {
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToGameListFragment(FetchType.UPCOMING.tag)

                navController.navigate(navWithAction)
            }

            seeAllButtonSecond.setOnClickListener {
                val navWithAction = HomeFragmentDirections.actionNavigationHomeToGameListFragment(FetchType.TOP.tag)

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
                viewModel.fetchPreviewGames()
            }
        }
    }

    override fun onDestroyView() {
        viewModel.previewList.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}