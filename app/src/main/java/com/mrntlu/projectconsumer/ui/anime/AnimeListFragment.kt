package com.mrntlu.projectconsumer.ui.anime

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.WindowSizeClass
import com.mrntlu.projectconsumer.models.main.anime.Anime
import com.mrntlu.projectconsumer.ui.BaseListFragment
import com.mrntlu.projectconsumer.viewmodels.main.anime.AnimeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AnimeListFragment : BaseListFragment<Anime>() {

    private val args: AnimeListFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: AnimeViewModel.Factory
    private val viewModel: AnimeViewModel by viewModels {
        AnimeViewModel.provideAnimeViewModelFactory(viewModelFactory, this, arguments, args.fetchType, sharedViewModel.isNetworkAvailable())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar(args.fetchType)
        setObservers()
        setListeners()
    }

    private fun setObservers() {
        sharedViewModel.windowSize.observe(viewLifecycleOwner) {
            val widthSize: WindowSizeClass = it

            gridCount = when(widthSize) {
                WindowSizeClass.COMPACT -> 2
                WindowSizeClass.MEDIUM -> 3
                WindowSizeClass.EXPANDED -> 5
            }

            setRecyclerView(
                startFetch = { viewModel.startAnimeFetch() },
                onItemSelected = { itemId ->
                    if (navController.currentDestination?.id == R.id.animeListFragment) {
                        val navWithAction = AnimeListFragmentDirections.actionAnimeListFragmentToAnimeDetailsFragment(itemId)

                        navController.navigate(navWithAction)
                    }

                },
                scrollViewModel = { position -> viewModel.setScrollPosition(position) },
                fetch = { viewModel.fetchAnime() }
            )
        }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            viewModel.isNetworkAvailable = it
        }

        viewModel.animes.observe(viewLifecycleOwner) { response ->
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.didOrientationChange = true
    }

    override fun onDestroyView() {
        viewModel.animes.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}