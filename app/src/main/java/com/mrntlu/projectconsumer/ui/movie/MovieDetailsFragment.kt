package com.mrntlu.projectconsumer.ui.movie

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLater
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import com.mrntlu.projectconsumer.models.main.userList.MovieWatchList
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.MessageBoxType
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.utils.roundSingleDecimal
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.viewmodels.movie.MovieDetailsViewModel
import com.mrntlu.projectconsumer.viewmodels.shared.ActivitySharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MovieDetailsFragment : BaseFragment<FragmentMovieDetailsBinding>() {

    private val viewModel: MovieDetailsViewModel by viewModels()
    private val sharedViewModel: ActivitySharedViewModel by activityViewModels()
    private val args: MovieDetailsFragmentArgs by navArgs()

    private var actorAdapter: DetailsAdapter? = null
    private var companiesAdapter: DetailsAdapter? = null
    private var genreAdapter: GenreAdapter? = null
    private var streamingAdapter: StreamingAdapter? = null
    private var buyAdapter: StreamingAdapter? = null
    private var rentAdapter: StreamingAdapter? = null

    private var watchList: MovieWatchList? = null
    private var consumeLater: ConsumeLater? = null
    private lateinit var countryCode: String

    private var consumeLaterDeleteLiveData: LiveData<NetworkResponse<MessageResponse>>? = null

    private val countryList = Locale.getISOCountries().filter { it.length == 2 }.map {
        val locale = Locale("", it)
        Pair(locale.displayCountry, locale.country.uppercase())
    }.sortedBy {
        it.first
    }

    /* TODO Flow
    * WatchLater
    * - Start animation and make request
    * - Listen via observer, if success show message else show error message and reset button
    * Add List
    * - Delete
    *   - Show loading bottom sheet and prevent closure on outsite pressed.
    *       - If closed somehow, listen changes and on success start animation.
    *   - On success close bottom sheet and start animation
     */

    private val onBottomSheetClosedCallback = object: OnButtomSheetClosed<MovieWatchList> {
        override fun onSuccess(data: MovieWatchList?, operation: BottomSheetOperation) {
            watchList = data

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

        setUI()
        setLottieUI()
        setListeners()
        setRecyclerView()
        setObservers()
    }

    private fun setLottieUI() {
        binding.detailsInclude.watchLaterLottie.apply {
            setAnimation(if(sharedViewModel.isLightTheme()) R.raw.bookmark else R.raw.bookmark_night)

            setOnClickListener {
                if (consumeLater == null) {
                    args.movieArgs.apply {
                        viewModel.createConsumeLater(
                            ConsumeLaterBody(id, tmdbID,null, "movie", null)
                        )
                    }
                } else {
                    if (consumeLaterDeleteLiveData != null && consumeLaterDeleteLiveData?.hasActiveObservers() == true)
                        consumeLaterDeleteLiveData?.removeObservers(viewLifecycleOwner)

                    consumeLaterDeleteLiveData = viewModel.deleteConsumeLater(IDBody(consumeLater!!.id))


                    consumeLaterDeleteLiveData?.observe(viewLifecycleOwner) { response ->
                        handleUserInteractionLoading(response)

                        if (response is NetworkResponse.Success)
                            consumeLater = null

                        if (consumeLater == null)
                            handleWatchLaterLottie()

                        if (response is NetworkResponse.Failure)
                            showErrorDialog(response.errorMessage)
                    }
                }
            }
        }

        binding.detailsInclude.addListLottie.apply {
            setAnimation(if(sharedViewModel.isLightTheme()) R.raw.like else R.raw.like_night)

            setOnClickListener {
                activity?.let {
                    val listBottomSheet = MovieDetailsBottomSheet(
                        onBottomSheetClosedCallback,
                        watchList,
                        args.movieArgs.id,
                        args.movieArgs.tmdbID,
                    )
                    listBottomSheet.show(it.supportFragmentManager, MovieDetailsBottomSheet.TAG)
                }
            }
        }
    }

    private fun handleWatchListLottie() {
        binding.detailsInclude.addListLottie.apply {
            frame = if (watchList == null) 130 else 0

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
            frame = if (consumeLater == null) 60 else 0

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
        binding.detailsInclude.apply {
            userInteractionLoadingLayout.setVisibilityByCondition(response != NetworkResponse.Loading)

            if (response != NetworkResponse.Loading)
                userInteractionLoading.cancelAnimation()
            else
                userInteractionLoading.playAnimation()
        }
    }

    private fun setUI() {
        //TODO if both images are null, hide.
        Glide.with(requireContext()).load("https://image.tmdb.org/t/p/original/wxgD3fB5lQ2sGJLog0rvXW049Pf.jpg").addListener(object: RequestListener<Drawable> {
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

        val spinnerAdapter = ArrayAdapter(binding.root.context, android.R.layout.simple_spinner_item, countryList.map { it.first })
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.detailsStreamingCountrySpinner.adapter = spinnerAdapter
        binding.detailsStreamingCountrySpinner.setSelection(
            countryList.indexOfFirst {
                it.second == countryCode
            }
        )

        args.movieArgs.apply {
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
                //Open in web with id
            }

            tmdbButton.setOnClickListener {
                //Open in web with id
            }

            detailsStreamingCountrySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    countryCode = countryList[position].second
                    val streaming = args.movieArgs.streaming!!
                    val streamingList = streaming.firstOrNull { it.countryCode == countryCode }

                    streamingAdapter?.setNewList(streamingList?.streamingPlatforms ?: listOf())
                    buyAdapter?.setNewList(streamingList?.buyOptions ?: listOf())
                    rentAdapter?.setNewList(streamingList?.rentOptions ?: listOf())
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setRecyclerView() {
        if (!args.movieArgs.actors.isNullOrEmpty()) {
            binding.detailsActorsRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout
                actorAdapter = DetailsAdapter(detailsList = args.movieArgs.actors!!.filter {
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

        if (!args.movieArgs.productionCompanies.isNullOrEmpty()) {
            binding.detailsProductionRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                actorAdapter = DetailsAdapter(R.drawable.ic_company_75, 18F, args.movieArgs.productionCompanies!!.filter {
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

        if (args.movieArgs.genres.isNotEmpty()) {
            binding.detailsGenreRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                genreAdapter = GenreAdapter(args.movieArgs.genres.map { it.name }) {
                    printLog("Genre ${args.movieArgs.genres[it]}")
                }
                adapter = genreAdapter
            }
        } else {
            binding.detailsGenreTV.setGone()
            binding.detailsGenreRV.setGone()
        }

        if (!args.movieArgs.streaming.isNullOrEmpty()) {
            val streaming = args.movieArgs.streaming!!

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

    private fun setObservers() {
        if (!viewModel.movieDetails.hasObservers())
            viewModel.getMovieDetails(args.movieArgs.id)

        //TODO if is logged in
        viewModel.movieDetails.observe(viewLifecycleOwner) { response ->
            binding.detailsInclude.apply {
                handleUserInteractionLoading(response)

                if (response is NetworkResponse.Success) {
                    watchList = response.data.data.movieWatchList
                    consumeLater = response.data.data.watchLater
                }

                if (watchList != null)
                    handleWatchListLottie()

                if (consumeLater != null)
                    handleWatchLaterLottie()
            }
        }

        viewModel.consumeLater.observe(viewLifecycleOwner) { response ->
            handleUserInteractionLoading(response)

            if (response is NetworkResponse.Success)
                consumeLater = response.data.data

            if (consumeLater != null)
                handleWatchLaterLottie()

            if (response is NetworkResponse.Failure)
                showErrorDialog(response.errorMessage)
        }
    }

    private fun showErrorDialog(message: String) {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(resources.getString(R.string.dismiss)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
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