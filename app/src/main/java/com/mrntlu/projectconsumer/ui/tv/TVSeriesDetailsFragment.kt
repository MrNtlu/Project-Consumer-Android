package com.mrntlu.projectconsumer.ui.tv

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.DetailsAdapter
import com.mrntlu.projectconsumer.adapters.GenreAdapter
import com.mrntlu.projectconsumer.adapters.StreamingAdapter
import com.mrntlu.projectconsumer.databinding.FragmentTvDetailsBinding
import com.mrntlu.projectconsumer.models.main.tv.TVSeriesDetails
import com.mrntlu.projectconsumer.ui.BaseDetailsFragment
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.viewmodels.main.tv.TVDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TVSeriesDetailsFragment : BaseDetailsFragment<FragmentTvDetailsBinding>() {

    private val viewModel: TVDetailsViewModel by viewModels()
    private val args: TVSeriesDetailsFragmentArgs by navArgs()

    //TODO Season adapter
    private var genreAdapter: GenreAdapter? = null
    private var actorAdapter: DetailsAdapter? = null
    private var networkAdapter: DetailsAdapter? = null
    private var companiesAdapter: DetailsAdapter? = null
    private var streamingAdapter: StreamingAdapter? = null
    private var buyAdapter: StreamingAdapter? = null
    private var rentAdapter: StreamingAdapter? = null

    private var tvDetails: TVSeriesDetails? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTvDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
    }

    private fun setObservers() {
        if (!viewModel.tvDetails.hasObservers())
            viewModel.getTVDetails(args.tvId)

        viewModel.tvDetails.observe(viewLifecycleOwner) { response ->
            binding.tvDetailsInclude.apply {
                isResponseFailed = response is NetworkResponse.Failure


            }
        }

        consumeLaterViewModel.consumeLater.observe(viewLifecycleOwner) { response ->

        }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (isResponseFailed && it)
                viewModel.getTVDetails(args.tvId)
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            consumeLaterViewModel.consumeLater.removeObservers(this)
            consumeLaterDeleteLiveData?.removeObservers(this)
            sharedViewModel.networkStatus.removeObservers(this)
        }
        activity?.let {
            it.window.statusBarColor = ContextCompat.getColor(it, if (sharedViewModel.isLightTheme()) R.color.darkWhite else R.color.androidBlack)
        }
        genreAdapter = null
        actorAdapter = null
        networkAdapter = null
        companiesAdapter = null
        streamingAdapter = null
        buyAdapter = null
        rentAdapter = null
        super.onDestroyView()
    }
}