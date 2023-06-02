package com.mrntlu.projectconsumer.ui.tv

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.mrntlu.projectconsumer.models.main.tv.TVSeries
import com.mrntlu.projectconsumer.ui.BaseListFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TVSeriesListFragment: BaseListFragment<TVSeries>() {

    private val args: TVSeriesListFragmentArgs by navArgs()

    //TODO Viewmodel factory

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setMenu(args.fetchType) {
            //TODO Start fetch
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        viewModel.didOrientationChange = true
    }

    override fun onDestroyView() {
//        viewModel.movies.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}