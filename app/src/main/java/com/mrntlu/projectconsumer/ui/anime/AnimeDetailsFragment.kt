package com.mrntlu.projectconsumer.ui.anime

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.AnimeRecommendationsAdapter
import com.mrntlu.projectconsumer.adapters.AnimeRelationsAdapter
import com.mrntlu.projectconsumer.adapters.DetailsAdapter
import com.mrntlu.projectconsumer.adapters.GenreAdapter
import com.mrntlu.projectconsumer.adapters.NameUrlAdapter
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
import com.mrntlu.projectconsumer.ui.profile.UserListBottomSheet
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.capitalizeFirstLetter
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.isEmptyOrBlank
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.main.anime.AnimeDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private var recommendationsAdapter: AnimeRecommendationsAdapter? = null
    private var producerAdapter: NameUrlAdapter? = null
    private var streamingAdapter: NameUrlAdapter? = null
    private var relationAdapter: AnimeRelationsAdapter? = null

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

    private fun setToolbar() {
        binding.animeDetailsToolbar.apply {
            setNavigationIcon(R.drawable.ic_arrow_back_24)
            setNavigationContentDescription(R.string.back_cd)
            setNavigationOnClickListener {
                navController.popBackStack()
            }

            inflateMenu(R.menu.details_share_menu)
            setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.shareMenu -> {
                        val shareURL = "${Constants.BASE_DOMAIN_URL}/anime/${animeDetails?.id}"

                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareURL)
                            type = "text/plain"
                        }

                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(shareIntent)
                    }
                }
                true
            }
        }
    }

    private fun setObservers() {
        if (!(viewModel.animeDetails.hasObservers() || viewModel.animeDetails.value is NetworkResponse.Success || viewModel.animeDetails.value is NetworkResponse.Loading))
            viewModel.getAnimeDetails(args.animeId)

        viewModel.animeDetails.observe(viewLifecycleOwner) { response ->
            binding.apply {
                isResponseFailed = response is NetworkResponse.Failure
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
                        animeDetails = response.data.data

                        setUI()
                        setToolbar()
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
                                        Constants.ContentType.ANIME,
                                        if (watchList == null) BottomSheetState.EDIT else BottomSheetState.VIEW,
                                        animeDetails!!.id,
                                        animeDetails!!.malID.toString(),
                                        null, animeDetails?.episodes,
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
                val radiusInPx = root.context.dpToPxFloat(6f)

                previewCard.setGone()
                previewShimmerLayout.setVisible()
                previewShimmerCV.radius = radiusInPx
                previewIV.loadWithGlide(animeDetails?.imageURL ?: "", previewCard, previewShimmerLayout) {
                    transform(RoundedCorners(radiusInPx.toInt()))
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

            val seasonsStr = if (season != null) "${season.capitalizeFirstLetter()} $year" else getString(R.string.unknown)
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
            imageInclude.root.setSafeOnClickListener(interval = 750) {
                if (animeDetails?.imageURL?.isNotEmptyOrBlank() == true) {
                    val navWithAction = AnimeDetailsFragmentDirections.actionAnimeDetailsFragmentToImageFragment(
                        animeDetails!!.imageURL
                    )

                    navController.navigate(navWithAction)
                }
            }

            val trailerSplitStr = animeDetails?.trailer?.split("v=")
            val isTrailerAvailable = trailerSplitStr != null && trailerSplitStr.size > 1 && trailerSplitStr[1].isNotEmptyOrBlank()
            binding.trailerButton.setVisibilityByCondition(!isTrailerAvailable)

            trailerButton.setSafeOnClickListener {
                if (isTrailerAvailable) {
                    val navWithActions = AnimeDetailsFragmentDirections.actionAnimeDetailsFragmentToTrailerFragment(
                        trailerSplitStr!![1]
                    )
                    navController.navigate(navWithActions)
                }
            }

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
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val characterUIList = animeDetails!!.characters!!.filter {
                    it.name.isNotEmptyOrBlank()
                }.map {
                    DetailsUI(it.name, it.image, it.role)
                }

                withContext(Dispatchers.Main) {
                    createDetailsAdapter(
                        recyclerView = binding.detailsCharRV,
                        detailsList = characterUIList
                    ) {
                        characterAdapter = it
                        it
                    }
                }
            }
        } else {
            binding.detailsCharTV.setGone()
            binding.detailsCharRV.setGone()
        }

        if (!animeDetails?.genres.isNullOrEmpty()) {
            binding.detailsGenreRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                genreAdapter = GenreAdapter(animeDetails!!.genres!!.map { it.name }) {
                    val navWithAction = AnimeDetailsFragmentDirections.actionAnimeDetailsFragmentToDiscoverListFragment(Constants.ContentType.ANIME, animeDetails?.genres?.get(it)?.name)
                    navController.navigate(navWithAction)
                }
                adapter = genreAdapter
            }
        } else {
            binding.detailsGenreTV.setGone()
            binding.detailsGenreRV.setGone()
        }

        binding.detailsStudiosRV.apply {
            val flexboxLayout = FlexboxLayoutManager(context)
            flexboxLayout.apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
                alignItems = AlignItems.FLEX_START
                flexWrap = FlexWrap.WRAP
            }
            layoutManager = flexboxLayout

            studioAdapter = NameUrlAdapter(
                if (!animeDetails?.studios.isNullOrEmpty()) animeDetails!!.studios!!
                else listOf(AnimeNameURL(getString(R.string.unknown), ""))
            )
            adapter = studioAdapter
        }

        binding.detailsProducerRV.apply {
            val flexboxLayout = FlexboxLayoutManager(context)
            flexboxLayout.apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
                alignItems = AlignItems.FLEX_START
                flexWrap = FlexWrap.WRAP
            }
            layoutManager = flexboxLayout

            producerAdapter = NameUrlAdapter(
                if (!animeDetails?.producers.isNullOrEmpty()) animeDetails!!.producers!!
                else listOf(AnimeNameURL(getString(R.string.unknown), ""))
            )
            adapter = producerAdapter
        }

        if (!animeDetails?.streaming.isNullOrEmpty()) {
            binding.detailsStreamingRV.apply {
                val flexboxLayout = FlexboxLayoutManager(context)
                flexboxLayout.apply {
                    flexDirection = FlexDirection.ROW
                    justifyContent = JustifyContent.FLEX_START
                    alignItems = AlignItems.FLEX_START
                    flexWrap = FlexWrap.WRAP
                }
                layoutManager = flexboxLayout

                streamingAdapter = NameUrlAdapter(animeDetails!!.streaming!!)
                adapter = streamingAdapter
            }
        } else {
            binding.detailsStreamingRV.setGone()
            binding.detailsStreamingTV.setGone()
        }

        if (!animeDetails?.relations.isNullOrEmpty()) {
            binding.detailsRelationRV.apply {
                val linearLayout = LinearLayoutManager(context)
                layoutManager = linearLayout

                relationAdapter = AnimeRelationsAdapter(animeDetails!!.relations!!) { malID ->
                    val navWithAction = AnimeDetailsFragmentDirections.actionAnimeDetailsFragmentSelf(malID.toString())
                    navController.navigate(navWithAction)
                }
                adapter = relationAdapter
            }
        } else {
            binding.detailsRelationTV.setGone()
            binding.detailsRelationRV.setGone()
        }

        if (!animeDetails?.recommendations.isNullOrEmpty()) {
            binding.detailsRecommendationRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                recommendationsAdapter = AnimeRecommendationsAdapter(animeDetails!!.recommendations) {
                    val navWithAction = AnimeDetailsFragmentDirections.actionAnimeDetailsFragmentSelf(it.malID.toString())
                    navController.navigate(navWithAction)
                }
                adapter = recommendationsAdapter
            }
        } else {
            binding.detailsRecommendationTV.setGone()
            binding.detailsRecommendationRV.setGone()
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.animeDetails.removeObservers(this)
            detailsConsumeLaterViewModel.consumeLater.removeObservers(this)
        }

        characterAdapter = null
        genreAdapter = null
        recommendationsAdapter = null
        producerAdapter = null
        studioAdapter = null
        streamingAdapter = null
        relationAdapter = null

        super.onDestroyView()
    }
}