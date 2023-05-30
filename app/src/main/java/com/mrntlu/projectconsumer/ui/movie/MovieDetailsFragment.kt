package com.mrntlu.projectconsumer.ui.movie

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.AppBarLayout.LayoutParams
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.DetailsAdapter
import com.mrntlu.projectconsumer.adapters.GenreAdapter
import com.mrntlu.projectconsumer.adapters.StreamingAdapter
import com.mrntlu.projectconsumer.databinding.FragmentMovieDetailsBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetOperation
import com.mrntlu.projectconsumer.interfaces.OnButtomSheetClosed
import com.mrntlu.projectconsumer.models.common.DetailsUI
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.models.main.movie.MovieDetails
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchList
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.utils.roundSingleDecimal
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.main.movie.MovieDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MovieDetailsFragment : BaseFragment<FragmentMovieDetailsBinding>() {

    private val viewModel: MovieDetailsViewModel by viewModels()
    private val args: MovieDetailsFragmentArgs by navArgs()

    private var actorAdapter: DetailsAdapter? = null
    private var companiesAdapter: DetailsAdapter? = null
    private var genreAdapter: GenreAdapter? = null
    private var streamingAdapter: StreamingAdapter? = null
    private var buyAdapter: StreamingAdapter? = null
    private var rentAdapter: StreamingAdapter? = null

    private var movieDetails: MovieDetails? = null

    private var isResponseFailed = false
    private lateinit var countryCode: String

    private var consumeLaterDeleteLiveData: LiveData<NetworkResponse<MessageResponse>>? = null

    private val countryList = Locale.getISOCountries().filter { it.length == 2 }.map {
        val locale = Locale("", it)
        Pair(locale.displayCountry, locale.country.uppercase())
    }.sortedBy {
        it.first
    }

    /* TODO!!!!
    * TODO Check for process death
    *  TODO!!!!!
     */

    private val onBottomSheetClosedCallback = object: OnButtomSheetClosed<MovieWatchList> {
        override fun onSuccess(data: MovieWatchList?, operation: BottomSheetOperation) {
            movieDetails?.movieWatchList = data

            if (operation != BottomSheetOperation.UPDATE)
                handleWatchListLottie()
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
        activity?.window?.statusBarColor = Color.TRANSPARENT
        countryCode = sharedViewModel.getCountryCode()

        setObservers()
    }

    private fun setLottieUI() {
        binding.detailsInclude.watchLaterLottie.apply {
            setAnimation(if(sharedViewModel.isLightTheme()) R.raw.bookmark else R.raw.bookmark_night)

            setOnClickListener {
                if (sharedViewModel.isNetworkAvailable() && sharedViewModel.isLoggedIn()) {
                    if (movieDetails != null && movieDetails?.watchLater == null) {
                        movieDetails!!.apply {
                            viewModel.createConsumeLater(
                                ConsumeLaterBody(id, tmdbID, null, "movie", null)
                            )
                        }
                    } else if (movieDetails != null) {
                        if (consumeLaterDeleteLiveData != null && consumeLaterDeleteLiveData?.hasActiveObservers() == true)
                            consumeLaterDeleteLiveData?.removeObservers(viewLifecycleOwner)

                        consumeLaterDeleteLiveData =
                            viewModel.deleteConsumeLater(IDBody(movieDetails!!.watchLater!!.id))


                        consumeLaterDeleteLiveData?.observe(viewLifecycleOwner) { response ->
                            handleUserInteractionLoading(response)

                            if (response is NetworkResponse.Success)
                                movieDetails!!.watchLater = null

                            if (movieDetails!!.watchLater == null)
                                handleWatchLaterLottie()

                            if (response is NetworkResponse.Failure)
                                context?.showErrorDialog(response.errorMessage)
                        }
                    }
                } else {
                    if (!sharedViewModel.isNetworkAvailable())
                        context?.showErrorDialog(getString(R.string.no_internet_connection))

                    //TODO Else show login/register dialog!
                }
            }
        }

        binding.detailsInclude.addListLottie.apply {
            setAnimation(if(sharedViewModel.isLightTheme()) R.raw.like else R.raw.like_night)

            setOnClickListener {
                if (sharedViewModel.isNetworkAvailable() && sharedViewModel.isLoggedIn()) {
                    if (movieDetails != null) {
                        activity?.let {
                            val listBottomSheet = MovieDetailsBottomSheet(
                                onBottomSheetClosedCallback,
                                movieDetails!!.movieWatchList,
                                args.movieId,
                                movieDetails!!.tmdbID,
                            )
                            listBottomSheet.show(it.supportFragmentManager, MovieDetailsBottomSheet.TAG)
                        }
                    }
                } else {
                    if (!sharedViewModel.isNetworkAvailable())
                        context?.showErrorDialog(getString(R.string.no_internet_connection))

                    //TODO Else show login/register dialog!
                }
            }
        }
    }

    private fun handleWatchListLottie() {
        binding.detailsInclude.addListLottie.apply {
            frame = if (movieDetails?.movieWatchList == null) 130 else 0

            if (frame != 0) {
                setMinAndMaxFrame(75, 129)
            } else {
                setMinAndMaxFrame(0, 75)
            }
            playAnimation()
        }
    }

    private fun handleWatchLaterLottie() {
        binding.detailsInclude.watchLaterLottie.apply {
            frame = if (movieDetails?.watchLater == null) 60 else 0

            if (frame != 0) {
                reverseAnimationSpeed()
                setMinAndMaxFrame(0, 60)
            } else {
                speed = 1.4f
                setMinAndMaxFrame(0, 120)
            }
            playAnimation()
        }
    }

    private fun handleUserInteractionLoading(response: NetworkResponse<*>) {
        isResponseFailed = response is NetworkResponse.Failure

        binding.detailsInclude.apply {
            userInteractionLoading.setAnimation(if (response is NetworkResponse.Failure) R.raw.error_small else R.raw.loading)
            userInteractionLoadingLayout.setVisibilityByCondition(shouldHide = response is NetworkResponse.Success)

            userInteractionLoading.apply {
                if (response is NetworkResponse.Failure) {
                    repeatCount = 1
                    scaleX = 1.2f
                    scaleY = 1.2f
                } else if (response == NetworkResponse.Loading) {
                    repeatCount = ValueAnimator.INFINITE
                    scaleX = 1.7f
                    scaleY = 1.7f
                }

                if (response != NetworkResponse.Loading)
                    cancelAnimation()
                else
                    playAnimation()
            }
        }
    }

    private fun setUI() {
        val spinnerAdapter = ArrayAdapter(binding.root.context, android.R.layout.simple_spinner_item, countryList.map { it.first })
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.detailsStreamingCountrySpinner.adapter = spinnerAdapter
        binding.detailsStreamingCountrySpinner.setSelection(
            countryList.indexOfFirst {
                it.second == countryCode
            }
        )

        movieDetails!!.apply {
            binding.detailsToolbarProgress.setVisible()
            Glide.with(requireContext()).load(backdrop ?: imageURL).addListener(object:
                RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    binding.detailsToolbarProgress.setGone()
                    binding.detailsAppBarLayout.setExpanded(false)
                    val collapsingLayoutParams: LayoutParams = binding.detailsCollapsingToolbar.layoutParams as LayoutParams
                    collapsingLayoutParams.scrollFlags = -1
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    binding.detailsToolbarProgress.setGone()
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

            val releaseText = "${lengthStr ?: ""}${if (releaseDate.isNotEmptyOrBlank()) releaseDate.take(4) else status}"
            binding.detailsReleaseTV.text = releaseText

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
            detailsToolbarBackButton.setOnClickListener {
                navController.popBackStack()
            }

            detailsDescriptionTV.setOnClickListener {
                detailsDescriptionTV.toggle()
            }

            imdbButton.setOnClickListener {
                //TODO Open in web with id
            }

            tmdbButton.setOnClickListener {
                //TODO Open in web with id
            }

            detailsStreamingCountrySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    countryCode = countryList[position].second
                    val streaming = movieDetails?.streaming
                    val streamingList = streaming?.firstOrNull { it.countryCode == countryCode }

                    streamingAdapter?.setNewList(streamingList?.streamingPlatforms ?: listOf())
                    buyAdapter?.setNewList(streamingList?.buyOptions ?: listOf())
                    rentAdapter?.setNewList(streamingList?.rentOptions ?: listOf())
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setRecyclerView() {
        if (!movieDetails?.actors.isNullOrEmpty()) {
            binding.detailsActorsRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout
                actorAdapter = DetailsAdapter(detailsList = movieDetails!!.actors!!.filter {
                    it.name.isNotEmptyOrBlank()
                }.map {
                    DetailsUI(
                        it.name,
                        it.image,
                        it.character
                    )
                })
                adapter = actorAdapter
            }
        } else {
            binding.detailsActorsTV.setGone()
            binding.detailsActorsRV.setGone()
        }

        if (!movieDetails?.productionCompanies.isNullOrEmpty()) {
            binding.detailsProductionRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                actorAdapter = DetailsAdapter(R.drawable.ic_company_75, 18F, movieDetails!!.productionCompanies!!.filter {
                    it.name.isNotEmptyOrBlank()
                }.map {
                    DetailsUI(
                        it.name,
                        it.logo ?: "",
                        it.originCountry
                    )
                }) {
                    centerCrop().transform(RoundedCorners(12))
                }
                adapter = actorAdapter
            }
        } else {
            binding.detailsProductionTV.setGone()
            binding.detailsProductionRV.setGone()
        }

        if (!movieDetails?.genres.isNullOrEmpty()) {
            binding.detailsGenreRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                genreAdapter = GenreAdapter(movieDetails!!.genres.map { it.name }) {
                    printLog("Genre ${movieDetails!!.genres[it]}")
                }
                adapter = genreAdapter
            }
        } else {
            binding.detailsGenreTV.setGone()
            binding.detailsGenreRV.setGone()
        }

        if (!movieDetails?.streaming.isNullOrEmpty()) {
            val streaming = movieDetails!!.streaming!!

            binding.detailsStreamingRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                streamingAdapter = StreamingAdapter(
                    streaming.firstOrNull { it.countryCode == countryCode }?.streamingPlatforms ?: listOf()
                )
                adapter = streamingAdapter
            }

            binding.detailsBuyRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                buyAdapter = StreamingAdapter(
                    streaming.firstOrNull { it.countryCode == countryCode }?.buyOptions ?: listOf()
                )
                adapter = buyAdapter
            }

            binding.detailsRentRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                rentAdapter = StreamingAdapter(
                    streaming.firstOrNull { it.countryCode == countryCode }?.rentOptions ?: listOf()
                )
                adapter = rentAdapter
            }
        }
    }

    private fun toggleLayoutScroll(isEnabled: Boolean) {
        binding.detailsCollapsingToolbar.apply {
            val layoutParams = layoutParams as LayoutParams
            if (isEnabled)
                layoutParams.scrollFlags = layoutParams.scrollFlags or LayoutParams.SCROLL_FLAG_SCROLL
            else
                layoutParams.scrollFlags = layoutParams.scrollFlags and LayoutParams.SCROLL_FLAG_SCROLL.inv()

            binding.detailsCollapsingToolbar.layoutParams = layoutParams
        }
    }

    private fun setObservers() {
        if (!viewModel.movieDetails.hasObservers())
            viewModel.getMovieDetails(args.movieId)

        viewModel.movieDetails.observe(viewLifecycleOwner) { response ->
            binding.detailsInclude.apply {
                isResponseFailed = response is NetworkResponse.Failure
                toggleLayoutScroll(response !is NetworkResponse.Loading)
                binding.loadingLayout.setVisibilityByCondition(response !is NetworkResponse.Loading)

                when(response) {
                    is NetworkResponse.Failure -> TODO()
                    is NetworkResponse.Success -> {
                        movieDetails = response.data.data

                        setUI()
                        setLottieUI()
                        setListeners()
                        setRecyclerView()

                        if (movieDetails?.movieWatchList != null)
                            handleWatchListLottie()

                        if (movieDetails?.watchLater != null)
                            handleWatchLaterLottie()
                    }
                    else -> {}
                }
                //TODO Loading
            }
        }

        viewModel.consumeLater.observe(viewLifecycleOwner) { response ->
            handleUserInteractionLoading(response)

            if (response is NetworkResponse.Success)
                movieDetails?.watchLater = response.data.data

            if (movieDetails?.watchLater != null)
                handleWatchLaterLottie()

            if (response is NetworkResponse.Failure)
                context?.showErrorDialog(response.errorMessage)
        }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (isResponseFailed && it)
                viewModel.getMovieDetails(args.movieId)
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.movieDetails.removeObservers(this)
            viewModel.consumeLater.removeObservers(this)
            consumeLaterDeleteLiveData?.removeObservers(this)
        }
        activity?.let {
            it.window.statusBarColor = ContextCompat.getColor(it, if (sharedViewModel.isLightTheme()) R.color.darkWhite else R.color.androidBlack)
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