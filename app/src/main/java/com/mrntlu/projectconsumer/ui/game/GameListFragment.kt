package com.mrntlu.projectconsumer.ui.game

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.WindowSizeClass
import com.mrntlu.projectconsumer.models.main.game.Game
import com.mrntlu.projectconsumer.ui.BaseListFragment
import com.mrntlu.projectconsumer.viewmodels.main.game.GameViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GameListFragment : BaseListFragment<Game>() {

    private val args: GameListFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: GameViewModel.Factory
    private val viewModel: GameViewModel by viewModels {
        GameViewModel.provideGameViewModelFactory(viewModelFactory, this, arguments, args.fetchType, sharedViewModel.isNetworkAvailable())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar(args.fetchType)
        setObservers()
        setListeners()
    }

    private fun setObservers() {
        if (sharedViewModel.isAltLayout()) {
            gridCount = 1

            setRV()
        } else {
            sharedViewModel.windowSize.observe(viewLifecycleOwner) {
                val widthSize: WindowSizeClass = it

                gridCount = when(widthSize) {
                    WindowSizeClass.COMPACT -> 2
                    WindowSizeClass.MEDIUM -> 3
                    WindowSizeClass.EXPANDED -> 5
                }

                setRV()
            }
        }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            viewModel.isNetworkAvailable = it
        }

        viewModel.games.observe(viewLifecycleOwner) { response ->
            onListObserveHandler(response) {
                if (viewModel.isRestoringData || viewModel.didOrientationChange) {
                    binding.listRV.scrollToPosition(viewModel.scrollPosition - 1)

                    if (viewModel.isRestoringData) {
                        viewModel.isRestoringData = false
                    } else {
                        viewModel.didOrientationChange = false
                    }
                } else if (!response.isPaginating) {
                    binding.listRV.scrollToPosition(viewModel.scrollPosition - 1)
                    isNavigatingBack = false
                }
            }
        }
    }

    private fun setRV() {
        setRecyclerView(
            isRatioDifferent = true,
            startFetch = { viewModel.startGamesFetch() },
            onItemSelected = { itemId ->
                if (navController.currentDestination?.id == R.id.gameListFragment) {
                    val navWithAction = GameListFragmentDirections.actionGameListFragmentToGameDetailsFragment(itemId)

                    navController.navigate(navWithAction)
                }
            },
            scrollViewModel = { position -> viewModel.setScrollPosition(position) },
            fetch = { viewModel.fetchGames() }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.didOrientationChange = true
    }

    override fun onDestroyView() {
        viewModel.games.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}