package com.mrntlu.projectconsumer.ui.game

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.models.main.game.Game
import com.mrntlu.projectconsumer.ui.BasePreviewFragment
import com.mrntlu.projectconsumer.ui.common.HomeFragmentDirections
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.viewmodels.main.game.GamePreviewViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GameFragment : BasePreviewFragment<Game>() {

    private val viewModel: GamePreviewViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding.guideline14.setGuidelinePercent(0.25f)
        setGuidelineHeight(true)

        setUI()
        setListeners()
        setShowcaseRecyclerView(
            isGame = true,
            onItemClicked = { id ->
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToGameDetailsFragment(id)

                    navController.navigate(navWithAction)
                }
            },
            onRefreshPressed = { viewModel.fetchPreviewGames() }
        )
        setRecyclerView(
            isRatioDifferent = true,
            firstOnItemSelected = { id ->
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToGameDetailsFragment(id)

                    navController.navigate(navWithAction)
                }
            },
            firstOnRefreshPressed = { viewModel.fetchPreviewGames() },
            secondOnItemSelected = { id ->
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToGameDetailsFragment(id)

                    navController.navigate(navWithAction)
                }
            },
            secondOnRefreshPressed = { viewModel.fetchPreviewGames() },
            extraOnItemSelected = {},
            extraOnRefreshPressed = {},
        )
        setObservers()
    }

    private fun setUI() {
        binding.apply {
            upcomingPreviewRV.layoutParams.height = root.context.dpToPx(150f)
            topRatedPreviewRV.layoutParams.height = root.context.dpToPx(150f)

            extraRVTV.setGone()
            seeAllButtonExtra.setGone()
            extraPreviewRV.setGone()

            val marginParams = topRatedPreviewRV.layoutParams as ViewGroup.MarginLayoutParams
            marginParams.bottomMargin = root.context.dpToPx(72f)
            topRatedPreviewRV.layoutParams = marginParams
        }
    }

    private fun setListeners() {
        binding.apply {
            seeAllButtonPopular.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToGameListFragment(FetchType.POPULAR.tag)

                    navController.navigate(navWithAction)
                }
            }

            seeAllButtonFirst.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToGameListFragment(FetchType.UPCOMING.tag)

                    navController.navigate(navWithAction)
                }
            }

            seeAllButtonSecond.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_home) {
                    val navWithAction = HomeFragmentDirections.actionNavigationHomeToGameListFragment(FetchType.TOP.tag)

                    navController.navigate(navWithAction)
                }
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