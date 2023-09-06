package com.mrntlu.projectconsumer.ui.tv

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.WindowSizeClass
import com.mrntlu.projectconsumer.models.main.tv.TVSeries
import com.mrntlu.projectconsumer.ui.BaseListFragment
import com.mrntlu.projectconsumer.viewmodels.main.tv.TVSeriesViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TVSeriesListFragment: BaseListFragment<TVSeries>() {

    private val args: TVSeriesListFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: TVSeriesViewModel.Factory
    private val viewModel: TVSeriesViewModel by viewModels {
        TVSeriesViewModel.provideTVSeriesViewModelFactory(viewModelFactory, this, arguments, args.fetchType, sharedViewModel.isNetworkAvailable())
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

        viewModel.tvList.observe(viewLifecycleOwner) { response ->
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
            startFetch = { viewModel.startTVSeriesFetch() },
            onItemSelected = { itemId ->
                if (navController.currentDestination?.id == R.id.tvListFragment) {
                    val navWithAction = TVSeriesListFragmentDirections.actionTvListFragmentToTvDetailsFragment(itemId)

                    navController.navigate(navWithAction)
                }
            },
            scrollViewModel = { position -> viewModel.setScrollPosition(position) },
            fetch = { viewModel.fetchTVSeries() }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.didOrientationChange = true
    }

    override fun onDestroyView() {
        viewModel.tvList.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}