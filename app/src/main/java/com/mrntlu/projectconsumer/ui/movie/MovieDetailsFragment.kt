package com.mrntlu.projectconsumer.ui.movie

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.tabs.TabLayoutMediator
import com.mrntlu.projectconsumer.adapters.MovieDetailsFragmentFactory
import com.mrntlu.projectconsumer.adapters.MovieDetailsPagerAdapter
import com.mrntlu.projectconsumer.adapters.StreamingAdapter
import com.mrntlu.projectconsumer.databinding.FragmentMovieDetailsBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetOperation
import com.mrntlu.projectconsumer.interfaces.BottomSheetState
import com.mrntlu.projectconsumer.interfaces.OnBottomSheetClosed
import com.mrntlu.projectconsumer.interfaces.UserListModel
import com.mrntlu.projectconsumer.interfaces.toMovieWatchList
import com.mrntlu.projectconsumer.models.main.movie.MovieDetails
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import com.mrntlu.projectconsumer.ui.BaseDetailsFragment
import com.mrntlu.projectconsumer.ui.profile.UserListBottomSheet
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.Constants.BASE_DOMAIN_URL
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.roundSingleDecimal
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.main.movie.MovieDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MovieDetailsFragment : BaseDetailsFragment<FragmentMovieDetailsBinding>() {

    companion object {
        private const val CT_STATE = "collapsing_toolbar_state"

        private const val TYPE = "movie"
    }

    private val detailsTabList = arrayListOf("About", "Cast", "Streaming")
    //TODO Screenshots Trailers(?)

    private val viewModel: MovieDetailsViewModel by viewModels()
    private val args: MovieDetailsFragmentArgs by navArgs()

    private var viewPagerAdapter: MovieDetailsPagerAdapter? = null
    private var mediator: TabLayoutMediator? = null

    private var isAppBarLifted: Boolean? = null
    private var movieDetails: MovieDetails? = null

    private val onBottomSheetClosedCallback = object: OnBottomSheetClosed {
        override fun onSuccess(data: UserListModel?, operation: BottomSheetOperation) {
            movieDetails?.watchList = if (operation == BottomSheetOperation.DELETE)
                null
            else
                data?.toMovieWatchList()

            if (operation != BottomSheetOperation.UPDATE)
                handleWatchListLottie(
                    binding.detailsInclude,
                    movieDetails?.watchList == null
                )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            isAppBarLifted = it.getBoolean(CT_STATE)
        }

        setObservers()
    }

    private fun setObservers() {
        if (!(viewModel.movieDetails.hasObservers() || viewModel.movieDetails.value is NetworkResponse.Success || viewModel.movieDetails.value is NetworkResponse.Loading))
            viewModel.getMovieDetails(args.movieId)

        viewModel.movieDetails.observe(viewLifecycleOwner) { response ->
            binding.apply {
                isResponseFailed = response is NetworkResponse.Failure
                toggleCollapsingLayoutScroll(detailsCollapsingToolbar, response !is NetworkResponse.Loading)
                loadingLayout.setVisibilityByCondition(response !is NetworkResponse.Loading)
                errorLayout.setVisibilityByCondition(response !is NetworkResponse.Failure)

                when(response) {
                    is NetworkResponse.Failure -> {
                        errorLayoutInc.apply {
                            errorText.text = response.errorMessage

                            setListeners()
                        }
                    }
                    is NetworkResponse.Success -> {
                        movieDetails = response.data.data

                        setUI()
                        setLottieUI(
                            detailsInclude,
                            movieDetails,
                            createConsumeLater = {
                                movieDetails!!.apply {
                                    detailsConsumeLaterViewModel.createConsumeLater(
                                        ConsumeLaterBody(id, tmdbID, null, TYPE, null)
                                    )
                                }
                            },
                            showBottomSheet = {
                                val watchList = movieDetails!!.watchList

                                activity?.let {
                                    val listBottomSheet = UserListBottomSheet(
                                        watchList,
                                        Constants.ContentType.MOVIE,
                                        if (watchList == null) BottomSheetState.EDIT else BottomSheetState.VIEW,
                                        movieDetails!!.id,
                                        movieDetails!!.tmdbID,
                                        null, null,
                                        onBottomSheetClosedCallback,
                                    )
                                    listBottomSheet.show(it.supportFragmentManager, UserListBottomSheet.TAG)
                                }
                            }
                        )
                        setListeners()
                        if (movieDetails?.watchList != null)
                            handleWatchListLottie(
                                binding.detailsInclude,
                                movieDetails?.watchList == null
                            )

                        if (movieDetails?.consumeLater != null)
                            handleConsumeLaterLottie(
                                binding.detailsInclude,
                                movieDetails?.consumeLater == null
                            )
                    }
                    else -> {}
                }
            }
        }

        detailsConsumeLaterViewModel.consumeLater.observe(viewLifecycleOwner) { response ->
            handleUserInteractionLoading(response, binding.detailsInclude)

            if (response is NetworkResponse.Success)
                movieDetails?.consumeLater = response.data.data

            if (movieDetails?.consumeLater != null)
                handleConsumeLaterLottie(
                    binding.detailsInclude,
                    movieDetails?.consumeLater == null
                )

            if (response is NetworkResponse.Failure)
                context?.showErrorDialog(response.errorMessage)
        }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (isResponseFailed && it)
                viewModel.getMovieDetails(args.movieId)
        }
    }

    private fun setUI() {
        if (isAppBarLifted != null)
            binding.detailsAppBarLayout.setExpanded(!isAppBarLifted!!)

        movieDetails!!.apply {
            binding.detailsPager.apply {
                val fragmentFactory = MovieDetailsFragmentFactory(movieDetails!!)

                viewPagerAdapter = MovieDetailsPagerAdapter(
                    childFragmentManager,
                    viewLifecycleOwner.lifecycle,
                    fragmentFactory,
                )
                adapter = viewPagerAdapter
                isUserInputEnabled = false

                mediator = TabLayoutMediator(binding.movieDetailsTab, this) { tab, position ->
                    tab.text = detailsTabList[position]
                }
                mediator?.attach()
            }

            binding.detailsToolbarProgress.setVisible()
            Glide.with(requireContext()).load(backdrop ?: imageURL).addListener(object:
                RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                    binding.detailsToolbarProgress.setGone()
                    binding.detailsAppBarLayout.setExpanded(false)

                    onImageFailedHandler(
                        binding.detailsCollapsingToolbar,
                        binding.detailsNestedSV
                    )

                    return false
                }

                override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    _binding?.detailsToolbarProgress?.setGone()
                    return false
                }
            }).into(binding.detailsToolbarIV)

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val titleStr = if (!translations.isNullOrEmpty()) {
                    val translation = translations.firstOrNull { it.lanCode == sharedViewModel.getLanguageCode() }?.title
                    if (translation?.isNotEmptyOrBlank() == true)
                        translation
                    else title
                } else title

                withContext(Dispatchers.Main) {
                    binding.detailsTitleTV.text = titleStr
                }
            }

            binding.detailsOriginalTV.text = titleOriginal

            binding.detailsInclude.apply {
                interactionRateTV.text = tmdbVote.roundSingleDecimal().toString()
                val rateCountText = " | $tmdbVoteCount"
                interactionsRateCountTV.text = rateCountText
            }

            val lengthStr = if (length > 10) {
                val hours = length / 60
                val minutes = length % 60
                String.format("%02dh %02dm • ", hours, minutes)
            } else null

            val releaseStr = "${lengthStr ?: ""}${if (releaseDate.isNotEmptyOrBlank()) releaseDate.convertToHumanReadableDateString(true) else ""}"
            binding.detailsReleaseTV.text = releaseStr
            binding.detailsStatusTV.text = status
        }
    }

    private fun setListeners() {
        binding.apply {
            detailsToolbarIV.setSafeOnClickListener(interval = 750) {
                if (movieDetails?.imageURL?.isNotEmptyOrBlank() == true) {
                    val navWithAction = MovieDetailsFragmentDirections.actionMovieDetailsFragmentToImageFragment(
                        movieDetails!!.imageURL
                    )

                    navController.navigate(navWithAction)
                }
            }

            detailsToolbarBackButton.setSafeOnClickListener {
                navController.popBackStack()
            }

            detailsToolbarShareButton.setSafeOnClickListener {
                val shareURL = "$BASE_DOMAIN_URL/movie/${movieDetails?.id}"

                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareURL)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }

//            detailsDescriptionTV.setOnClickListener {
//                detailsDescriptionTV.toggle()
//            }
//
//            imdbButton.setOnClickListener {
//                movieDetails?.imdbID?.let {
//                    val url = "${Constants.BASE_IMDB_URL}$it"
//
//                    context?.openInBrowser(url)
//                }
//            }
//
//            tmdbButton.setOnClickListener {
//                movieDetails?.tmdbID?.let {
//                    val url = "${Constants.BASE_TMDB_URL}movie/$it"
//
//                    context?.openInBrowser(url)
//                }
//            }
//
//            detailsStreamingCountrySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                    onCountrySpinnerSelected(
//                        position, movieDetails?.streaming,
//                        streamingAdapter, buyAdapter, rentAdapter
//                    )
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>?) {}
//            }

            errorLayoutInc.refreshButton.setOnClickListener {
                viewModel.getMovieDetails(args.movieId)
            }

            errorLayoutInc.cancelButton.setOnClickListener {
                navController.popBackStack()
            }
        }
    }

//    private fun setRecyclerView() {
//        val radiusInPx = binding.root.context.dpToPxFloat(12f)
//
//        if (!movieDetails?.actors.isNullOrEmpty()) {
//            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
//                val actorUIList = movieDetails!!.actors!!.filter {
//                    it.name.isNotEmptyOrBlank()
//                }.map {
//                    DetailsUI(
//                        it.name,
//                        it.image,
//                        it.character
//                    )
//                }
//
//                withContext(Dispatchers.Main) {
//                    createDetailsAdapter(
//                        recyclerView = binding.detailsActorsRV,
//                        detailsList = actorUIList,
//                        cardCornerRadius = radiusInPx,
//                        transformImage = { transform(CenterCrop()) }
//                    ) {
//                        actorAdapter = it
//                        it
//                    }
//                }
//            }
//        } else {
//            binding.detailsActorsTV.setGone()
//            binding.detailsActorsRV.setGone()
//        }
//
//        if (!movieDetails?.productionCompanies.isNullOrEmpty()) {
//            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
//                val productionAndCompanyUIList = movieDetails!!.productionCompanies!!.filter {
//                    it.name.isNotEmptyOrBlank()
//                }.map {
//                    DetailsUI(
//                        it.name,
//                        it.logo ?: "",
//                        it.originCountry
//                    )
//                }
//
//                withContext(Dispatchers.Main) {
//                    createDetailsAdapter(
//                        recyclerView = binding.detailsProductionRV,
//                        detailsList = productionAndCompanyUIList,
//                        placeHolderImage = R.drawable.ic_company_75,
//                        cardCornerRadius = radiusInPx,
//                        transformImage = { transform(CenterInside()) }
//                    ) {
//                        companiesAdapter = it
//                        it
//                    }
//                }
//            }
//        } else {
//            binding.detailsProductionTV.setGone()
//            binding.detailsProductionRV.setGone()
//        }
//
//        if (!movieDetails?.genres.isNullOrEmpty()) {
//            binding.detailsGenreRV.apply {
//                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//                layoutManager = linearLayout
//                val bulletDecoration = BulletItemDecoration(context)
//                addItemDecoration(bulletDecoration)
//
//                genreAdapter = GenreAdapter(movieDetails!!.genres) {
//                    val navWithAction = MovieDetailsFragmentDirections.actionMovieDetailsFragmentToDiscoverListFragment(Constants.ContentType.MOVIE, movieDetails?.genres?.get(it))
//                    navController.navigate(navWithAction)
//                }
//                setHasFixedSize(true)
//                adapter = genreAdapter
//            }
//        } else {
//            binding.genreDivider.setGone()
//            binding.detailsGenreRV.setGone()
//        }
//
//        if (!movieDetails?.streaming.isNullOrEmpty()) {
//            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
//                val streaming = movieDetails!!.streaming!!
//
//                val streamingList = streaming.firstOrNull { it.countryCode == countryCode }?.streamingPlatforms
//                val buyList = streaming.firstOrNull { it.countryCode == countryCode }?.buyOptions
//                val rentList = streaming.firstOrNull { it.countryCode == countryCode }?.rentOptions
//
//                withContext(Dispatchers.Main) {
//                    createStreamingAdapter(
//                        binding.detailsStreamingRV,
//                        streamingList
//                    ) {
//                        streamingAdapter = it
//                        it
//                    }
//
//                    createStreamingAdapter(
//                        binding.detailsBuyRV,
//                        buyList
//                    ) {
//                        buyAdapter = it
//                        it
//                    }
//
//                    createStreamingAdapter(
//                        binding.detailsRentRV,
//                        rentList
//                    ) {
//                        rentAdapter = it
//                        it
//                    }
//                }
//            }
//        }
//
//        if (!movieDetails?.recommendations.isNullOrEmpty()) {
//            binding.detailsRecommendationRV.apply {
//                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//                layoutManager = linearLayout
//
//                recommendationsAdapter = RecommendationsAdapter(movieDetails!!.recommendations) { position, recommendation ->
//                    isAppBarLifted = binding.detailsAppBarLayout.isLifted
//                    recommendationPosition = position
//
//                    val navWithAction = MovieDetailsFragmentDirections.actionMovieDetailsFragmentSelf(recommendation.tmdbID)
//                    navController.navigate(navWithAction)
//                }
//                adapter = recommendationsAdapter
//
//                if (recommendationPosition != null)
//                    scrollToPosition(recommendationPosition!!)
//            }
//        } else {
//            binding.detailsRecommendationTV.setGone()
//            binding.detailsRecommendationRV.setGone()
//        }
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (isAppBarLifted != null)
            outState.putBoolean(CT_STATE, isAppBarLifted!!)
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.movieDetails.removeObservers(this)
            detailsConsumeLaterViewModel.consumeLater.removeObservers(this)
        }

        mediator?.detach()
        mediator = null

        viewPagerAdapter = null
        super.onDestroyView()
    }
}