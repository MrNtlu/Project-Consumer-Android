package com.mrntlu.projectconsumer.ui.anime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.DetailsAdapter
import com.mrntlu.projectconsumer.adapters.GenreAdapter
import com.mrntlu.projectconsumer.adapters.NameUrlAdapter
import com.mrntlu.projectconsumer.adapters.decorations.BulletItemDecoration
import com.mrntlu.projectconsumer.databinding.FragmentAnimeDetailsBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetOperation
import com.mrntlu.projectconsumer.interfaces.BottomSheetState
import com.mrntlu.projectconsumer.interfaces.OnBottomSheetClosed
import com.mrntlu.projectconsumer.interfaces.UserListModel
import com.mrntlu.projectconsumer.interfaces.toAnimeWatchList
import com.mrntlu.projectconsumer.models.common.DetailsUI
import com.mrntlu.projectconsumer.models.main.anime.AnimeDetails
import com.mrntlu.projectconsumer.models.main.anime.AnimeNameURL
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import com.mrntlu.projectconsumer.ui.BaseDetailsFragment
import com.mrntlu.projectconsumer.ui.compose.LoadingShimmer
import com.mrntlu.projectconsumer.ui.profile.UserListBottomSheet
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.isEmptyOrBlank
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.main.anime.AnimeDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnimeDetailsFragment : BaseDetailsFragment<FragmentAnimeDetailsBinding>() {

    companion object {
        private const val TYPE = "anime"
    }

    private val viewModel: AnimeDetailsViewModel by viewModels()
    private val args: AnimeDetailsFragmentArgs by navArgs()

    private var characterAdapter: DetailsAdapter? = null
    private var genreAdapter: GenreAdapter? = null
    private var studioAdapter: NameUrlAdapter? = null
    private var producerAdapter: NameUrlAdapter? = null
    private var streamingAdapter: NameUrlAdapter? = null
    //TODO RelationsAdapter recyclerview in static recyclerview

    private var animeDetails: AnimeDetails? = null

    private val onBottomSheetClosedCallback = object: OnBottomSheetClosed {
        override fun onSuccess(data: UserListModel?, operation: BottomSheetOperation) {
            animeDetails?.watchList = if (operation == BottomSheetOperation.DELETE)
                null
            else
                data?.toAnimeWatchList()

            if (operation != BottomSheetOperation.UPDATE)
                handleWatchListLottie(
                    binding.detailsInclude,
                    animeDetails?.watchList == null
                )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnimeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setObservers()
    }

    private fun setObservers() {
        if (!(viewModel.animeDetails.hasObservers() || viewModel.animeDetails.value is NetworkResponse.Success || viewModel.animeDetails.value is NetworkResponse.Loading))
            viewModel.getAnimeDetails(args.animeId)

        viewModel.animeDetails.observe(viewLifecycleOwner) { response ->
            binding.apply {
                isResponseFailed = response is NetworkResponse.Failure
//                loadingLayout.setVisibilityByCondition(response !is NetworkResponse.Loading)
                errorLayout.setVisibilityByCondition(response !is NetworkResponse.Failure)

                when(response) {
                    is NetworkResponse.Failure -> {
                        errorLayoutInc.apply {
                            errorText.text = response.errorMessage

                            setListeners()
                        }
                    }
                    is NetworkResponse.Success -> {
                        animeDetails = response.data.data

                        setUI()
                        setLottieUI(
                            detailsInclude,
                            animeDetails,
                            createConsumeLater = {
                                animeDetails!!.apply {
                                    detailsConsumeLaterViewModel.createConsumeLater(
                                        ConsumeLaterBody(id, malID.toString(), null, TYPE, null)
                                    )
                                }
                            },
                            showBottomSheet = {
                                val watchList = animeDetails!!.watchList

                                activity?.let {
                                    val listBottomSheet = UserListBottomSheet(
                                        watchList,
                                        Constants.ContentType.MOVIE,
                                        if (watchList == null) BottomSheetState.EDIT else BottomSheetState.VIEW,
                                        animeDetails!!.id,
                                        animeDetails!!.malID.toString(),
                                        null, null,
                                        onBottomSheetClosedCallback,
                                    )
                                    listBottomSheet.show(it.supportFragmentManager, UserListBottomSheet.TAG)
                                }
                            }
                        )
                        setListeners()
                        setRecyclerView()

                        if (animeDetails?.watchList != null)
                            handleWatchListLottie(
                                binding.detailsInclude,
                                animeDetails?.watchList == null
                            )

                        if (animeDetails?.consumeLater != null)
                            handleConsumeLaterLottie(
                                binding.detailsInclude,
                                animeDetails?.consumeLater == null
                            )
                    }
                    else -> {}
                }
            }
        }

        detailsConsumeLaterViewModel.consumeLater.observe(viewLifecycleOwner) { response ->
            handleUserInteractionLoading(response, binding.detailsInclude)

            if (response is NetworkResponse.Success)
                animeDetails?.consumeLater = response.data.data

            if (animeDetails?.consumeLater != null)
                handleConsumeLaterLottie(
                    binding.detailsInclude,
                    animeDetails?.consumeLater == null
                )

            if (response is NetworkResponse.Failure)
                context?.showErrorDialog(response.errorMessage)
        }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (isResponseFailed && it)
                viewModel.getAnimeDetails(args.animeId)
        }
    }

    private fun setUI() {
        animeDetails!!.apply {
            binding.imageInclude.apply {
                previewComposeView.apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    setContent {
                        LoadingShimmer(isDarkTheme = false, roundedCornerSize = 6.dp) {
                            fillMaxHeight()
                        }
                    }
                }

                previewCard.setGone()
                previewComposeView.setVisible()
                previewIV.loadWithGlide(animeDetails?.imageURL ?: "", previewCard, previewComposeView) {
                    transform(RoundedCorners(12))
                }
            }

            binding.detailsTitleTV.text = if (titleEn.isEmptyOrBlank()) titleOriginal else titleEn
            binding.detailsDescriptionTV.text = description
            binding.detailsOriginalTV.text = titleOriginal

            binding.detailsInclude.apply {
                interactionRateTV.text = malScore.toString()
                val rateCountText = " | $malScoredBy"
                interactionsRateCountTV.text = rateCountText
            }

            binding.detailsTypeTV.text = type
            binding.detailsStatusTV.text = status
            binding.detailsSourceTV.text = source

            val seasonsStr = if (season != null) "${season.replaceFirstChar { it.uppercase() }} $year" else getString(R.string.unknown)
            binding.detailsSeasonTV.text = seasonsStr

            binding.detailsEpisodeTV.text = if (episodes != null) "$episodes eps." else getString(R.string.unknown)

            val fromDate = if (aired.from != null && aired.from.isNotEmptyOrBlank()) aired.from.convertToHumanReadableDateString(true) else "?"
            val toDate = if (aired.to != null && aired.to.isNotEmptyOrBlank()) aired.to.convertToHumanReadableDateString(true) else "?"
            val airedStr = "$fromDate to $toDate"
            binding.detailsAiredTV.text = airedStr
        }
    }

    private fun setListeners() {
        binding.apply {
            detailsDescriptionTV.setOnClickListener {
                detailsDescriptionTV.toggle()
            }

            errorLayoutInc.refreshButton.setOnClickListener {
                viewModel.getAnimeDetails(args.animeId)
            }

            errorLayoutInc.cancelButton.setOnClickListener {
                navController.popBackStack()
            }
        }
    }

    private fun setRecyclerView() {
        if (!animeDetails?.characters.isNullOrEmpty()) {
            createDetailsAdapter(
                recyclerView = binding.detailsCharRV,
                detailsList = animeDetails!!.characters!!.filter {
                    it.name.isNotEmptyOrBlank()
                }.map {
                    DetailsUI(it.name, it.image, it.role)
                }
            ) {
                characterAdapter = it
                it
            }
        } else {
            binding.detailsCharTV.setGone()
            binding.detailsCharRV.setGone()
        }

        if (!animeDetails?.genres.isNullOrEmpty()) {
            binding.detailsGenreRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout
                val bulletDecoration = BulletItemDecoration(context)
                addItemDecoration(bulletDecoration)

                genreAdapter = GenreAdapter(animeDetails!!.genres!!.map { it.name }) {
                    val navWithAction = AnimeDetailsFragmentDirections.actionAnimeDetailsFragmentToDiscoverListFragment(Constants.ContentType.ANIME, animeDetails?.genres?.get(it)?.name)
                    navController.navigate(navWithAction)
                }
                adapter = genreAdapter
            }
        } else {
            binding.genreDivider.setGone()
            binding.detailsGenreRV.setGone()
        }

        binding.detailsStudiosRV.apply {
            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout
            val bulletDecoration = BulletItemDecoration(context)
            addItemDecoration(bulletDecoration)

            studioAdapter = NameUrlAdapter(
                if (!animeDetails?.studios.isNullOrEmpty()) animeDetails!!.studios!!
                else listOf(AnimeNameURL(getString(R.string.unknown), ""))
            )
            adapter = studioAdapter
        }

        binding.detailsProducerRV.apply {
            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout
            val bulletDecoration = BulletItemDecoration(context)
            addItemDecoration(bulletDecoration)

            producerAdapter = NameUrlAdapter(
                if (!animeDetails?.producers.isNullOrEmpty()) animeDetails!!.producers!!
                else listOf(AnimeNameURL(getString(R.string.unknown), ""))
            )
            adapter = producerAdapter
        }

        if (!animeDetails?.streaming.isNullOrEmpty()) {
            binding.detailsStreamingRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout
                val bulletDecoration = BulletItemDecoration(context)
                addItemDecoration(bulletDecoration)

                streamingAdapter = NameUrlAdapter(animeDetails!!.streaming!!)
                adapter = streamingAdapter
            }
        } else {
            binding.detailsStreamingRV.setGone()
            binding.detailsStreamingTV.setGone()
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.animeDetails.removeObservers(this)
            detailsConsumeLaterViewModel.consumeLater.removeObservers(this)
        }

        characterAdapter = null
        genreAdapter = null
        producerAdapter = null
        studioAdapter = null
        streamingAdapter = null

        super.onDestroyView()
    }
}