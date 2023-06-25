package com.mrntlu.projectconsumer.ui.tv

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.DetailsAdapter
import com.mrntlu.projectconsumer.adapters.GenreAdapter
import com.mrntlu.projectconsumer.adapters.SeasonAdapter
import com.mrntlu.projectconsumer.adapters.StreamingAdapter
import com.mrntlu.projectconsumer.adapters.decorations.BulletItemDecoration
import com.mrntlu.projectconsumer.databinding.FragmentTvDetailsBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetOperation
import com.mrntlu.projectconsumer.interfaces.OnBottomSheetClosed
import com.mrntlu.projectconsumer.models.common.DetailsUI
import com.mrntlu.projectconsumer.models.main.tv.TVSeriesDetails
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import com.mrntlu.projectconsumer.models.main.userList.TVSeriesWatchList
import com.mrntlu.projectconsumer.ui.BaseDetailsFragment
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.roundSingleDecimal
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.main.tv.TVDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TVSeriesDetailsFragment : BaseDetailsFragment<FragmentTvDetailsBinding>() {

    companion object {
        const val TYPE = "tv"
    }

    private val viewModel: TVDetailsViewModel by viewModels()
    private val args: TVSeriesDetailsFragmentArgs by navArgs()

    private var seasonAdapter: SeasonAdapter? = null
    private var genreAdapter: GenreAdapter? = null
    private var actorAdapter: DetailsAdapter? = null
    private var networkAdapter: DetailsAdapter? = null
    private var companiesAdapter: DetailsAdapter? = null
    private var streamingAdapter: StreamingAdapter? = null
    private var buyAdapter: StreamingAdapter? = null
    private var rentAdapter: StreamingAdapter? = null

    private var tvDetails: TVSeriesDetails? = null

    private val onBottomSheetClosedCallback = object: OnBottomSheetClosed<TVSeriesWatchList> {
        override fun onSuccess(data: TVSeriesWatchList?, operation: BottomSheetOperation) {
            tvDetails?.watchList = data

            if (operation != BottomSheetOperation.UPDATE)
                handleWatchListLottie(
                    binding.tvDetailsInclude,
                    tvDetails?.watchList == null
                )
        }
    }

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
        if (!(viewModel.tvDetails.hasObservers() || viewModel.tvDetails.value is NetworkResponse.Success || viewModel.tvDetails.value is NetworkResponse.Loading))
            viewModel.getTVDetails(args.tvId)

        viewModel.tvDetails.observe(viewLifecycleOwner) { response ->
            binding.tvDetailsInclude.apply {
                isResponseFailed = response is NetworkResponse.Failure
                toggleCollapsingLayoutScroll(binding.tvDetailsCollapsingToolbar, response !is NetworkResponse.Loading)
                binding.loadingLayout.setVisibilityByCondition(response !is NetworkResponse.Loading)
                binding.errorLayout.setVisibilityByCondition(response !is NetworkResponse.Failure)

                when(response) {
                    is NetworkResponse.Failure -> {
                        binding.errorLayoutInc.apply {
                            errorText.text = response.errorMessage

                            setListeners()
                        }
                    }
                    is NetworkResponse.Success -> {
                        tvDetails = response.data.data

                        setUI()
                        setLottieUI(
                            binding.tvDetailsInclude,
                            tvDetails,
                            createConsumeLater = {
                                tvDetails!!.apply {
                                    detailsConsumeLaterViewModel.createConsumeLater(
                                        ConsumeLaterBody(id, tmdbID, null, TYPE, null)
                                    )
                                }
                            },
                            showBottomSheet = {
                                activity?.let {
                                    val listBottomSheet = TVSeriesUserListBottomSheet(
                                        onBottomSheetClosedCallback,
                                        tvDetails!!.watchList,
                                        args.tvId,
                                        tvDetails!!.tmdbID,
                                        tvDetails?.totalSeasons,
                                        tvDetails?.totalEpisodes,
                                    )
                                    listBottomSheet.show(it.supportFragmentManager, TVSeriesUserListBottomSheet.TAG)
                                }
                            }
                        )
                        setListeners()
                        setRecyclerView()

                        if (tvDetails?.watchList != null)
                            handleWatchListLottie(
                                binding.tvDetailsInclude,
                                tvDetails?.watchList == null
                            )

                        if (tvDetails?.consumeLater != null)
                            handleConsumeLaterLottie(
                                binding.tvDetailsInclude,
                                tvDetails?.consumeLater == null
                            )
                    }
                    else -> {}
                }
            }
        }

        detailsConsumeLaterViewModel.consumeLater.observe(viewLifecycleOwner) { response ->
            handleUserInteractionLoading(response, binding.tvDetailsInclude)

            if (response is NetworkResponse.Success)
                tvDetails?.consumeLater = response.data.data

            if (tvDetails?.consumeLater != null)
                handleConsumeLaterLottie(
                    binding.tvDetailsInclude,
                    tvDetails?.consumeLater == null
                )

            if (response is NetworkResponse.Failure)
                context?.showErrorDialog(response.errorMessage)
        }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (isResponseFailed && it)
                viewModel.getTVDetails(args.tvId)
        }
    }

    private fun setUI() {
        setSpinner(binding.tvDetailsStreamingCountrySpinner)

        tvDetails!!.apply {
            binding.tvDetailsToolbarProgress.setVisible()
            Glide.with(requireContext()).load(backdrop ?: imageURL).addListener(object:
                RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    binding.tvDetailsToolbarProgress.setGone()
                    binding.tvDetailsAppBarLayout.setExpanded(false)

                    onImageFailedHandler(
                        binding.tvDetailsCollapsingToolbar,
                        binding.tvDetailsNestedSV
                    )

                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    binding.tvDetailsToolbarProgress.setGone()
                    return false
                }
            }).into(binding.tvDetailsToolbarIV)

            val titleStr = if (!translations.isNullOrEmpty()) {
                translations.firstOrNull { it.lanCode == sharedViewModel.getLanguageCode() }?.title ?: title
            } else title

            val descriptionStr = if (!translations.isNullOrEmpty()) {
                translations.firstOrNull { it.lanCode == sharedViewModel.getLanguageCode() }?.description ?: description
            } else description

            binding.tvDetailsTitleTV.text = titleStr
            binding.tvDetailsDescriptionTV.text = descriptionStr
            binding.tvDetailsOriginalTV.text = titleOriginal

            binding.tvDetailsInclude.apply {
                interactionRateTV.text = tmdbVote.roundSingleDecimal().toString()
                val rateCountText = " | $tmdbVoteCount"
                interactionsRateCountTV.text = rateCountText
            }

            binding.tvDetailsInfoTV.text = firstAirDate.take(4)
            binding.tvDetailsStatusTV.text = status

            val totalSeasonsStr = "$totalSeasons Season${if (totalSeasons > 1) "s" else ""}"
            val totalEpisodesStr = "$totalEpisodes Episode${if (totalEpisodes > 1) "s" else ""}"
            val seasonEpsStr = "$totalSeasonsStr • $totalEpisodesStr"
            binding.tvDetailsSeasonEpsTV.text = seasonEpsStr

            binding.tvDetailsAvailableTV.setVisibilityByCondition(streaming.isNullOrEmpty())
            binding.tvDetailsStreamingCountrySpinner.setVisibilityByCondition(streaming.isNullOrEmpty())
            binding.tvDetailsStreamingTV.setVisibilityByCondition(streaming.isNullOrEmpty())
            binding.tvDetailsBuyTV.setVisibilityByCondition(streaming.isNullOrEmpty())
            binding.tvDetailsRentTV.setVisibilityByCondition(streaming.isNullOrEmpty())
        }
    }

    private fun setListeners() {
        binding.apply {
            tvDetailsToolbarBackButton.setOnClickListener {
                navController.popBackStack()
            }

            tvDetailsDescriptionTV.setOnClickListener {
                tvDetailsDescriptionTV.toggle()
            }

            tmdbButton.setOnClickListener {
                //TODO Open in web with id
            }

            tvDetailsStreamingCountrySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    onCountrySpinnerSelected(
                        position, tvDetails?.streaming,
                        streamingAdapter, buyAdapter, rentAdapter
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            errorLayoutInc.refreshButton.setOnClickListener {
                viewModel.getTVDetails(args.tvId)
            }

            errorLayoutInc.cancelButton.setOnClickListener {
                navController.popBackStack()
            }
        }
    }

    private fun setRecyclerView() {
        if (!tvDetails?.seasons.isNullOrEmpty()) {
            binding.tvDetailsSeasonRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                seasonAdapter = SeasonAdapter(tvDetails!!.seasons)
                adapter = seasonAdapter
            }
        } else {
            binding.tvDetailsSeasonRV.setGone()
        }

        if (!tvDetails?.genres.isNullOrEmpty()) {
            binding.tvDetailsGenreRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout
                val bulletDecoration = BulletItemDecoration(context)
                addItemDecoration(bulletDecoration)

                genreAdapter = GenreAdapter(tvDetails!!.genres.map { it.name }) {
                    val navWithAction = TVSeriesDetailsFragmentDirections.actionTvDetailsFragmentToDiscoverListFragment(Constants.ContentType.TV, tvDetails?.genres?.get(it)?.name)
                    navController.navigate(navWithAction)
                }
                adapter = genreAdapter
            }
        } else {
            binding.genreDivider.setGone()
            binding.tvDetailsGenreRV.setGone()
        }

        if (!tvDetails?.actors.isNullOrEmpty()) {
            createDetailsAdapter(
                recyclerView = binding.tvDetailsActorsRV,
                detailsList = tvDetails!!.actors!!.filter {
                    it.name.isNotEmptyOrBlank()
                }.map {
                    DetailsUI(
                        it.name,
                        it.image,
                        it.character
                    )
                }
            ) {
                actorAdapter = it
                it
            }
        } else {
            binding.tvDetailsActorsTV.setGone()
            binding.tvDetailsActorsRV.setGone()
        }

        if (!tvDetails?.networks.isNullOrEmpty()) {
            createDetailsAdapter(
                recyclerView = binding.tvDetailsNetworkRV,
                detailsList = tvDetails!!.networks!!.filter {
                    it.name.isNotEmptyOrBlank()
                }.map {
                    DetailsUI(
                        it.name,
                        it.logo ?: "",
                        it.originCountry ?: ""
                    )
                },
                placeHolderImage = R.drawable.ic_company_75, cardCornerRadius = 18F,
                transformImage = { centerCrop().transform(RoundedCorners(12)) }
            ) {
                networkAdapter = it
                it
            }
        } else {
            binding.tvDetailsNetworkTV.setGone()
            binding.tvDetailsNetworkRV.setGone()
        }

        if (!tvDetails?.productionCompanies.isNullOrEmpty()) {
            createDetailsAdapter(
                recyclerView = binding.tvDetailsProductionRV,
                detailsList = tvDetails!!.productionCompanies.filter {
                    it.name.isNotEmptyOrBlank()
                }.map {
                    DetailsUI(
                        it.name,
                        it.logo ?: "",
                        it.originCountry
                    )
                },
                placeHolderImage = R.drawable.ic_company_75, cardCornerRadius = 18F,
                transformImage = { centerCrop().transform(RoundedCorners(12)) }
            ) {
                companiesAdapter = it
                it
            }
        } else {
            binding.tvDetailsProductionTV.setGone()
            binding.tvDetailsProductionRV.setGone()
        }

        if (!tvDetails?.streaming.isNullOrEmpty()) {
            val streaming = tvDetails!!.streaming!!

            createStreamingAdapter(
                binding.tvDetailsStreamingRV, streaming.firstOrNull { it.countryCode == countryCode }?.streamingPlatforms
            ) {
                streamingAdapter = it
                it
            }

            createStreamingAdapter(
                binding.tvDetailsBuyRV, streaming.firstOrNull { it.countryCode == countryCode }?.buyOptions
            ) {
                buyAdapter = it
                it
            }

            createStreamingAdapter(
                binding.tvDetailsRentRV, streaming.firstOrNull { it.countryCode == countryCode }?.rentOptions
            ) {
                rentAdapter = it
                it
            }
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            detailsConsumeLaterViewModel.consumeLater.removeObservers(this)
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