package com.mrntlu.projectconsumer.ui.tv

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.DetailsAdapter
import com.mrntlu.projectconsumer.adapters.GenreAdapter
import com.mrntlu.projectconsumer.adapters.RecommendationsAdapter
import com.mrntlu.projectconsumer.adapters.SeasonAdapter
import com.mrntlu.projectconsumer.adapters.StreamingAdapter
import com.mrntlu.projectconsumer.databinding.FragmentTvDetailsBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetOperation
import com.mrntlu.projectconsumer.interfaces.BottomSheetState
import com.mrntlu.projectconsumer.interfaces.OnBottomSheetClosed
import com.mrntlu.projectconsumer.interfaces.UserListModel
import com.mrntlu.projectconsumer.interfaces.toTvWatchList
import com.mrntlu.projectconsumer.models.common.DetailsUI
import com.mrntlu.projectconsumer.models.main.tv.TVSeriesDetails
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import com.mrntlu.projectconsumer.ui.BaseDetailsFragment
import com.mrntlu.projectconsumer.ui.profile.UserListBottomSheet
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.Constants.ContentType
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.openInBrowser
import com.mrntlu.projectconsumer.utils.roundSingleDecimal
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.main.tv.TVDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class TVSeriesDetailsFragment : BaseDetailsFragment<FragmentTvDetailsBinding>() {

    companion object {
        private const val CT_STATE = "collapsing_toolbar_state"
        private const val RECOMMENDATION_POSITION = "recommendation_position"

        private const val TYPE = "tv"
    }

    private val viewModel: TVDetailsViewModel by viewModels()
    private val args: TVSeriesDetailsFragmentArgs by navArgs()

    private var seasonAdapter: SeasonAdapter? = null
    private var genreAdapter: GenreAdapter? = null
    private var recommendationsAdapter: RecommendationsAdapter? = null
    private var actorAdapter: DetailsAdapter? = null
    private var networkAdapter: DetailsAdapter? = null
    private var companiesAdapter: DetailsAdapter? = null
    private var streamingAdapter: StreamingAdapter? = null
    private var buyAdapter: StreamingAdapter? = null
    private var rentAdapter: StreamingAdapter? = null

    private var recommendationPosition: Int? = null
    private var isAppBarLifted: Boolean? = null
    private var tvDetails: TVSeriesDetails? = null

    private val onBottomSheetClosedCallback = object: OnBottomSheetClosed {
        override fun onSuccess(data: UserListModel?, operation: BottomSheetOperation) {
            tvDetails?.watchList = if (operation == BottomSheetOperation.DELETE)
                null
            else
                 data?.toTvWatchList()

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

        savedInstanceState?.let {
            recommendationPosition = it.getInt(RECOMMENDATION_POSITION)
            isAppBarLifted = it.getBoolean(CT_STATE)
        }

        setObservers()
    }

    override fun onStart() {
        if (tvDetails != null)
            setImage(tvDetails!!.backdrop, tvDetails!!.imageURL)
        super.onStart()
    }

    override fun onStop() {
        if (tvDetails?.imageURL != null)
            Glide.with(this).clear(binding.tvDetailsToolbarIV)
        super.onStop()
    }

    private fun setObservers() {
        if (!(viewModel.tvDetails.hasObservers() || viewModel.tvDetails.value is NetworkResponse.Success || viewModel.tvDetails.value is NetworkResponse.Loading))
            viewModel.getTVDetails(args.tvId)

        viewModel.tvDetails.observe(viewLifecycleOwner) { response ->
            binding.apply {
                isResponseFailed = response is NetworkResponse.Failure
                toggleCollapsingLayoutScroll(tvDetailsCollapsingToolbar, response !is NetworkResponse.Loading)
                errorLayout.setVisibilityByCondition(response !is NetworkResponse.Failure)

                when(response) {
                    is NetworkResponse.Failure -> {
                        loadingLayout.setGone()

                        errorLayoutInc.apply {
                            errorText.text = response.errorMessage

                            setListeners()
                        }
                    }
                    is NetworkResponse.Success -> {
                        tvDetails = response.data.data

                        viewModel.viewModelScope.launch {
                            setUI()
                            setLottieUI(
                                tvDetailsInclude,
                                tvDetails,
                                createConsumeLater = {
                                    tvDetails!!.apply {
                                        detailsConsumeLaterViewModel.createConsumeLater(
                                            ConsumeLaterBody(id, tmdbID, null, TYPE, null)
                                        )
                                    }
                                },
                                showBottomSheet = {
                                    val watchList = tvDetails!!.watchList

                                    activity?.let {
                                        val listBottomSheet = UserListBottomSheet(
                                            watchList,
                                            ContentType.TV,
                                            if (watchList == null) BottomSheetState.EDIT else BottomSheetState.VIEW,
                                            args.tvId,
                                            tvDetails!!.tmdbID,
                                            tvDetails?.totalSeasons,
                                            tvDetails?.totalEpisodes,
                                            onBottomSheetClosedCallback,
                                        )
                                        listBottomSheet.show(it.supportFragmentManager, UserListBottomSheet.TAG)
                                    }
                                }
                            )
                            setListeners()
                            setRecyclerView()
                            loadingLayout.setGone()
                        }

                        if (tvDetails?.watchList != null)
                            handleWatchListLottie(
                                tvDetailsInclude,
                                false
                            )

                        if (tvDetails?.consumeLater != null)
                            handleConsumeLaterLottie(
                                tvDetailsInclude,
                                false
                            )
                    }
                    NetworkResponse.Loading -> loadingLayout.setVisible()
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

    private suspend fun setUI() {
        setSpinner(binding.tvDetailsStreamingCountrySpinner)

        if (isAppBarLifted != null)
            binding.tvDetailsAppBarLayout.setExpanded(!isAppBarLifted!!)

        tvDetails!!.apply {
            setReviewSummary(binding.tvReviewSummaryLayout, reviews)
            setImage(backdrop, imageURL)

            withContext(Dispatchers.Default) {
                val titleStr = if (!translations.isNullOrEmpty()) {
                    val translation = translations.firstOrNull { it.lanCode == sharedViewModel.getLanguageCode() }?.title
                    if (translation?.isNotEmptyOrBlank() == true)
                        translation
                    else title
                } else title

                val descriptionStr = if (!translations.isNullOrEmpty()) {
                    val translation = translations.firstOrNull { it.lanCode == sharedViewModel.getLanguageCode() }?.description
                    if (translation?.isNotEmptyOrBlank() == true)
                        translation
                    else description
                } else description

                val totalSeasonsStr = "$totalSeasons Season${if (totalSeasons > 1) "s" else ""}"
                val totalEpisodesStr = "$totalEpisodes Episode${if (totalEpisodes > 1) "s" else ""}"
                val seasonEpsStr = "$totalSeasonsStr • $totalEpisodesStr"

                val interactionRateStr = tmdbVote.roundSingleDecimal().toString()
                val rateCountStr = " | $tmdbVoteCount"

                val firstAirDateStr = firstAirDate.convertToHumanReadableDateString(true)

                val imageSliderList = images.map {
                    SlideModel(imageUrl = it)
                }

                withContext(Dispatchers.Main) {
                    binding.tvDetailsTitleTV.text = titleStr
                    binding.tvDetailsDescriptionTV.text = descriptionStr
                    binding.tvDetailsSeasonEpsTV.text = seasonEpsStr
                    binding.tvDetailsOriginalTV.text = titleOriginal

                    binding.tvDetailsInclude.apply {
                        interactionRateTV.text = interactionRateStr
                        interactionsRateCountTV.text = rateCountStr
                    }

                    binding.tvDetailsInfoTV.text = firstAirDateStr
                    binding.tvDetailsStatusTV.text = status

                    binding.tvDetailsMediaImageSlider.apply {
                        setImageList(imageSliderList)

                        setItemClickListener(object: ItemClickListener {
                            override fun doubleClick(position: Int) {}

                            override fun onItemSelected(position: Int) {
                                if (tvDetails?.imageURL?.isNotEmptyOrBlank() == true && navController.currentDestination?.id == R.id.tvDetailsFragment) {
                                    isAppBarLifted = binding.tvDetailsAppBarLayout.isLifted

                                    val navWithAction = TVSeriesDetailsFragmentDirections.actionTvDetailsFragmentToImageFragment(
                                        tvDetails!!.imageURL,
                                        isRatioDifferent = true
                                    )

                                    navController.navigate(navWithAction)
                                }
                            }
                        })
                    }
                }
            }

            binding.tvDetailsMediaTV.setVisibilityByCondition(images.isEmpty())
            binding.tvDetailsMediaImageSliderCV.setVisibilityByCondition(images.isEmpty())
            binding.tvDetailsAvailableTV.setVisibilityByCondition(streaming.isNullOrEmpty())
            binding.tvDetailsStreamingCountrySpinner.setVisibilityByCondition(streaming.isNullOrEmpty())
            binding.tvDetailsStreamingTV.setVisibilityByCondition(streaming.isNullOrEmpty())
            binding.tvDetailsBuyTV.setVisibilityByCondition(streaming.isNullOrEmpty())
            binding.tvDetailsRentTV.setVisibilityByCondition(streaming.isNullOrEmpty())
        }
    }

    private fun setImage(backdrop: String?, imageURL: String) {
        binding.tvDetailsToolbarProgress.setVisible()
        Glide.with(binding.root.context).load(backdrop ?: imageURL).addListener(object:
            RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                _binding?.tvDetailsToolbarProgress?.setGone()
                _binding?.tvDetailsAppBarLayout?.setExpanded(false)

                if (_binding != null) {
                    onImageFailedHandler(
                        binding.tvDetailsCollapsingToolbar,
                        binding.tvDetailsNestedSV
                    )
                }

                return false
            }

            override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                _binding?.tvDetailsToolbarProgress?.setGone()
                return false
            }
        }).thumbnail(
            Glide.with(binding.root.context)
                .load(backdrop ?: imageURL)
                .sizeMultiplier(0.25f)
        ).into(binding.tvDetailsToolbarIV)
    }

    private fun setListeners() {
        binding.apply {
            tvDetailsToolbarIV.setSafeOnClickListener(interval = 750) {
                if (tvDetails?.imageURL?.isNotEmptyOrBlank() == true && navController.currentDestination?.id == R.id.tvDetailsFragment) {
                    val navWithAction = TVSeriesDetailsFragmentDirections.actionTvDetailsFragmentToImageFragment(
                        tvDetails!!.imageURL
                    )

                    navController.navigate(navWithAction)
                }
            }

            tvDetailsToolbarBackButton.setSafeOnClickListener {
                navController.popBackStack()
            }

            detailsToolbarShareButton.setSafeOnClickListener {
                val shareURL = "${Constants.BASE_DOMAIN_URL}/tv/${tvDetails?.id}"

                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareURL)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }

            tvDetailsDescriptionTV.setOnClickListener {
                tvDetailsDescriptionTV.toggle()
            }

            tvReviewSummaryLayout.seeAllButton.setSafeOnClickListener {
                if (tvDetails != null) {
                    isAppBarLifted = binding.tvDetailsAppBarLayout.isLifted

                    val navWithAction = TVSeriesDetailsFragmentDirections.actionTvDetailsFragmentToReviewFragment(
                        contentId = tvDetails!!.id,
                        contentExternalId = tvDetails!!.tmdbID,
                        contentExternalIntId = -1,
                        contentType = ContentType.TV.request,
                        contentTitle = tvDetails!!.title,
                    )
                    navController.navigate(navWithAction)
                }
            }

            tvReviewSummaryLayout.writeReviewButton.setSafeOnClickListener {
                if (tvDetails != null) {
                    isAppBarLifted = binding.tvDetailsAppBarLayout.isLifted

                    val navWithAction = TVSeriesDetailsFragmentDirections.actionTvDetailsFragmentToReviewCreateFragment(
                        review = null,
                        contentId = tvDetails!!.id,
                        contentExternalId = tvDetails!!.tmdbID,
                        contentExternalIntId = -1,
                        contentType = ContentType.TV.request,
                        contentTitle = tvDetails!!.title
                    )
                    navController.navigate(navWithAction)
                }
            }

            tmdbButton.setOnClickListener {
                tvDetails?.tmdbID?.let {
                    val url = "${Constants.BASE_TMDB_URL}tv/$it"

                    context?.openInBrowser(url)
                }
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

    private suspend fun setRecyclerView() {
        val radiusInPx = withContext(Dispatchers.Default) {
            binding.root.context.dpToPxFloat(12f)
        }

        if (!tvDetails?.seasons.isNullOrEmpty()) {
            binding.tvDetailsSeasonRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                seasonAdapter = SeasonAdapter(tvDetails!!.seasons, binding.root.context.dpToPxFloat(6f))
                setHasFixedSize(true)
                adapter = seasonAdapter
            }
        } else {
            binding.tvDetailsSeasonRV.setGone()
        }

        if (!tvDetails?.genres.isNullOrEmpty()) {
            binding.tvDetailsGenreRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                genreAdapter = GenreAdapter(tvDetails!!.genres) {
                    if (navController.currentDestination?.id == R.id.tvDetailsFragment) {
                        val navWithAction = TVSeriesDetailsFragmentDirections.actionTvDetailsFragmentToDiscoverListFragment(
                            ContentType.TV, tvDetails?.genres?.get(it))
                        navController.navigate(navWithAction)
                    }
                }
                setHasFixedSize(true)
                adapter = genreAdapter
            }
        } else {
            binding.tvDetailsGenreTV.setGone()
            binding.tvDetailsGenreRV.setGone()
        }

        if (!tvDetails?.actors.isNullOrEmpty()) {
            withContext(Dispatchers.Default) {
                val actorsUIList = tvDetails!!.actors!!.filter {
                    it.name.isNotEmptyOrBlank()
                }.map {
                    DetailsUI(
                        it.name,
                        it.image ?: "",
                        it.character
                    )
                }

                withContext(Dispatchers.Main) {
                    createDetailsAdapter(
                        recyclerView = binding.tvDetailsActorsRV,
                        detailsList = actorsUIList,
                        cardCornerRadius = radiusInPx,
                        transformImage = { transform(CenterCrop()) }
                    ) {
                        actorAdapter = it
                        it
                    }
                }
            }
        } else {
            binding.tvDetailsActorsTV.setGone()
            binding.tvDetailsActorsRV.setGone()
        }

        if (!tvDetails?.networks.isNullOrEmpty()) {
            withContext(Dispatchers.Default) {
                val networksUIList = tvDetails!!.networks!!.filter {
                    it.name.isNotEmptyOrBlank()
                }.map {
                    DetailsUI(
                        it.name,
                        it.logo ?: "",
                        it.originCountry ?: ""
                    )
                }

                withContext(Dispatchers.Main) {
                    createDetailsAdapter(
                        recyclerView = binding.tvDetailsNetworkRV,
                        detailsList = networksUIList,
                        placeHolderImage = R.drawable.ic_company_75,
                        cardCornerRadius = radiusInPx,
                        transformImage = { transform(CenterInside()) }
                    ) {
                        networkAdapter = it
                        it
                    }
                }
            }
        } else {
            binding.tvDetailsNetworkTV.setGone()
            binding.tvDetailsNetworkRV.setGone()
        }

        if (!tvDetails?.productionCompanies.isNullOrEmpty()) {
            withContext(Dispatchers.Default) {
                val productionAndCompanyUIList = tvDetails!!.productionCompanies.filter {
                    it.name.isNotEmptyOrBlank()
                }.map {
                    DetailsUI(
                        it.name,
                        it.logo ?: "",
                        it.originCountry
                    )
                }

                withContext(Dispatchers.Main) {
                    createDetailsAdapter(
                        recyclerView = binding.tvDetailsProductionRV,
                        detailsList = productionAndCompanyUIList,
                        placeHolderImage = R.drawable.ic_company_75,
                        cardCornerRadius = radiusInPx,
                        transformImage = { transform(CenterInside()) }
                    ) {
                        companiesAdapter = it
                        it
                    }
                }
            }
        } else {
            binding.tvDetailsProductionTV.setGone()
            binding.tvDetailsProductionRV.setGone()
        }

        if (!tvDetails?.streaming.isNullOrEmpty()) {
            withContext(Dispatchers.Default) {
                val streaming = tvDetails!!.streaming!!

                val streamingList = streaming.firstOrNull { it.countryCode == countryCode }?.streamingPlatforms
                val buyList = streaming.firstOrNull { it.countryCode == countryCode }?.buyOptions
                val rentList = streaming.firstOrNull { it.countryCode == countryCode }?.rentOptions

                withContext(Dispatchers.Main) {
                    createStreamingAdapter(
                        binding.tvDetailsStreamingRV,
                        streamingList
                    ) {
                        streamingAdapter = it
                        it
                    }

                    createStreamingAdapter(
                        binding.tvDetailsBuyRV,
                        buyList
                    ) {
                        buyAdapter = it
                        it
                    }

                    createStreamingAdapter(
                        binding.tvDetailsRentRV,
                        rentList
                    ) {
                        rentAdapter = it
                        it
                    }
                }
            }
        }

        if (!tvDetails?.recommendations.isNullOrEmpty()) {
            binding.tvDetailsRecommendationRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                recommendationsAdapter = RecommendationsAdapter(
                    tvDetails!!.recommendations,
                    binding.root.context.dpToPxFloat(8f),
                    binding.root.context.dpToPx(150f),
                ) { position, recommendation ->
                    if (navController.currentDestination?.id == R.id.tvDetailsFragment) {
                        isAppBarLifted = binding.tvDetailsAppBarLayout.isLifted
                        recommendationPosition = position

                        val navWithAction = TVSeriesDetailsFragmentDirections.actionTvDetailsFragmentSelf(recommendation.tmdbID)
                        navController.navigate(navWithAction)
                    }
                }
                setHasFixedSize(true)
                adapter = recommendationsAdapter

                if (recommendationPosition != null)
                    scrollToPosition(recommendationPosition!!)
            }
        } else {
            binding.tvDetailsRecommendationTV.setGone()
            binding.tvDetailsRecommendationRV.setGone()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (recommendationPosition != null)
            outState.putInt(RECOMMENDATION_POSITION, recommendationPosition!!)

        if (isAppBarLifted != null)
            outState.putBoolean(CT_STATE, isAppBarLifted!!)
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            detailsConsumeLaterViewModel.consumeLater.removeObservers(this)
            sharedViewModel.networkStatus.removeObservers(this)
        }

        seasonAdapter = null
        genreAdapter = null
        recommendationsAdapter = null
        actorAdapter = null
        networkAdapter = null
        companiesAdapter = null
        streamingAdapter = null
        buyAdapter = null
        rentAdapter = null

        super.onDestroyView()
    }
}