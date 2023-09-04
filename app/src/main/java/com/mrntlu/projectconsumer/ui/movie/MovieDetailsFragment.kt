package com.mrntlu.projectconsumer.ui.movie

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.DetailsAdapter
import com.mrntlu.projectconsumer.adapters.GenreAdapter
import com.mrntlu.projectconsumer.adapters.StreamingAdapter
import com.mrntlu.projectconsumer.adapters.decorations.BulletItemDecoration
import com.mrntlu.projectconsumer.databinding.FragmentMovieDetailsBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetOperation
import com.mrntlu.projectconsumer.interfaces.BottomSheetState
import com.mrntlu.projectconsumer.interfaces.OnBottomSheetClosed
import com.mrntlu.projectconsumer.interfaces.UserListModel
import com.mrntlu.projectconsumer.interfaces.toMovieWatchList
import com.mrntlu.projectconsumer.models.common.DetailsUI
import com.mrntlu.projectconsumer.models.main.movie.MovieDetails
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import com.mrntlu.projectconsumer.ui.BaseDetailsFragment
import com.mrntlu.projectconsumer.ui.profile.UserListBottomSheet
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.Constants.BASE_DOMAIN_URL
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.openInBrowser
import com.mrntlu.projectconsumer.utils.roundSingleDecimal
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.main.movie.MovieDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieDetailsFragment : BaseDetailsFragment<FragmentMovieDetailsBinding>() {

    companion object {
        private const val TYPE = "movie"
    }

    private val viewModel: MovieDetailsViewModel by viewModels()
    private val args: MovieDetailsFragmentArgs by navArgs()

    private var actorAdapter: DetailsAdapter? = null
    private var companiesAdapter: DetailsAdapter? = null
    private var genreAdapter: GenreAdapter? = null
    private var streamingAdapter: StreamingAdapter? = null
    private var buyAdapter: StreamingAdapter? = null
    private var rentAdapter: StreamingAdapter? = null

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
                        setRecyclerView()

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
        setSpinner(binding.detailsStreamingCountrySpinner)

        movieDetails!!.apply {
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

            val titleStr = if (!translations.isNullOrEmpty()) {
                translations.firstOrNull { it.lanCode == sharedViewModel.getLanguageCode() }?.title ?: title
            } else title

            val descriptionStr = if (!translations.isNullOrEmpty()) {
                translations.firstOrNull { it.lanCode == sharedViewModel.getLanguageCode() }?.description ?: description
            } else description

            binding.detailsTitleTV.text = titleStr
            binding.detailsDescriptionTV.text = descriptionStr
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

            binding.imdbButton.setVisibilityByCondition(imdbID == null)
            binding.detailsAvailableTV.setVisibilityByCondition(streaming.isNullOrEmpty())
            binding.detailsStreamingCountrySpinner.setVisibilityByCondition(streaming.isNullOrEmpty())
            binding.detailsStreamingTV.setVisibilityByCondition(streaming.isNullOrEmpty())
            binding.detailsBuyTV.setVisibilityByCondition(streaming.isNullOrEmpty())
            binding.detailsRentTV.setVisibilityByCondition(streaming.isNullOrEmpty())
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

            detailsDescriptionTV.setOnClickListener {
                detailsDescriptionTV.toggle()
            }

            imdbButton.setOnClickListener {
                movieDetails?.imdbID?.let {
                    val url = "${Constants.BASE_IMDB_URL}$it"

                    context?.openInBrowser(url)
                }
            }

            tmdbButton.setOnClickListener {
                movieDetails?.tmdbID?.let {
                    val url = "${Constants.BASE_TMDB_URL}movie/$it"

                    context?.openInBrowser(url)
                }
            }

            detailsStreamingCountrySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    onCountrySpinnerSelected(
                        position, movieDetails?.streaming,
                        streamingAdapter, buyAdapter, rentAdapter
                    )
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            errorLayoutInc.refreshButton.setOnClickListener {
                viewModel.getMovieDetails(args.movieId)
            }

            errorLayoutInc.cancelButton.setOnClickListener {
                navController.popBackStack()
            }
        }
    }

    private fun setRecyclerView() {
        val radiusInPx = binding.root.context.dpToPxFloat(12f)

        if (!movieDetails?.actors.isNullOrEmpty()) {
            createDetailsAdapter(
                recyclerView = binding.detailsActorsRV,
                detailsList = movieDetails!!.actors!!.filter {
                    it.name.isNotEmptyOrBlank()
                }.map {
                    DetailsUI(
                        it.name,
                        it.image,
                        it.character
                    )
                },
                cardCornerRadius = radiusInPx,
                transformImage = { transform(CenterCrop()) }
            ) {
                actorAdapter = it
                it
            }
        } else {
            binding.detailsActorsTV.setGone()
            binding.detailsActorsRV.setGone()
        }

        if (!movieDetails?.productionCompanies.isNullOrEmpty()) {
            createDetailsAdapter(
                recyclerView = binding.detailsProductionRV,
                detailsList = movieDetails!!.productionCompanies!!.filter {
                    it.name.isNotEmptyOrBlank()
                }.map {
                    DetailsUI(
                        it.name,
                        it.logo ?: "",
                        it.originCountry
                    )
                },
                placeHolderImage = R.drawable.ic_company_75,
                cardCornerRadius = radiusInPx,
                transformImage = { transform(CenterInside()) }
            ) {
                companiesAdapter = it
                it
            }
        } else {
            binding.detailsProductionTV.setGone()
            binding.detailsProductionRV.setGone()
        }

        if (!movieDetails?.genres.isNullOrEmpty()) {
            binding.detailsGenreRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout
                val bulletDecoration = BulletItemDecoration(context)
                addItemDecoration(bulletDecoration)

                genreAdapter = GenreAdapter(movieDetails!!.genres) {
                    val navWithAction = MovieDetailsFragmentDirections.actionMovieDetailsFragmentToDiscoverListFragment(Constants.ContentType.MOVIE, movieDetails?.genres?.get(it))
                    navController.navigate(navWithAction)
                }
                adapter = genreAdapter
            }
        } else {
            binding.genreDivider.setGone()
            binding.detailsGenreRV.setGone()
        }

        if (!movieDetails?.streaming.isNullOrEmpty()) {
            val streaming = movieDetails!!.streaming!!

            createStreamingAdapter(
                binding.detailsStreamingRV, streaming.firstOrNull { it.countryCode == countryCode }?.streamingPlatforms
            ) {
                streamingAdapter = it
                it
            }

            createStreamingAdapter(
                binding.detailsBuyRV, streaming.firstOrNull { it.countryCode == countryCode }?.buyOptions
            ) {
                buyAdapter = it
                it
            }

            createStreamingAdapter(
                binding.detailsRentRV, streaming.firstOrNull { it.countryCode == countryCode }?.rentOptions
            ) {
                rentAdapter = it
                it
            }
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.movieDetails.removeObservers(this)
            detailsConsumeLaterViewModel.consumeLater.removeObservers(this)
        }
        actorAdapter = null
        companiesAdapter = null
        genreAdapter = null
        streamingAdapter = null
        buyAdapter = null
        rentAdapter = null
        super.onDestroyView()
    }
}