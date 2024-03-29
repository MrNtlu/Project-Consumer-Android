package com.mrntlu.projectconsumer.ui.game

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.GamePlatformAdapter
import com.mrntlu.projectconsumer.adapters.GameRelationsAdapter
import com.mrntlu.projectconsumer.adapters.GenreAdapter
import com.mrntlu.projectconsumer.adapters.NameUrlAdapter
import com.mrntlu.projectconsumer.databinding.FragmentGameDetailsBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetOperation
import com.mrntlu.projectconsumer.interfaces.BottomSheetState
import com.mrntlu.projectconsumer.interfaces.OnBottomSheetClosed
import com.mrntlu.projectconsumer.interfaces.UserListModel
import com.mrntlu.projectconsumer.interfaces.toGamePlayList
import com.mrntlu.projectconsumer.models.common.BackendRequestMapper
import com.mrntlu.projectconsumer.models.common.GamePlatformUI
import com.mrntlu.projectconsumer.models.main.anime.AnimeNameURL
import com.mrntlu.projectconsumer.models.main.game.GameDetails
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import com.mrntlu.projectconsumer.ui.BaseDetailsFragment
import com.mrntlu.projectconsumer.ui.profile.UserListBottomSheet
import com.mrntlu.projectconsumer.utils.Constants.BASE_DOMAIN_URL
import com.mrntlu.projectconsumer.utils.Constants.ContentType
import com.mrntlu.projectconsumer.utils.Constants.GamePlatformUIList
import com.mrntlu.projectconsumer.utils.Constants.GameStoreList
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.main.game.GameDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class GameDetailsFragment : BaseDetailsFragment<FragmentGameDetailsBinding>() {

    companion object {
        private const val CT_STATE = "collapsing_toolbar_state"
        private const val RECOMMENDATION_POSITION = "recommendation_position"

        private const val TYPE = "game"
    }

    private val viewModel: GameDetailsViewModel by viewModels()
    private val args: GameDetailsFragmentArgs by navArgs()

    private var storeAdapter: NameUrlAdapter? = null
    private var genreAdapter: GenreAdapter? = null
    private var relationAdapter: GameRelationsAdapter? = null
    private var platformAdapter: GamePlatformAdapter? = null

    private var recommendationPosition: Int? = null
    private var isAppBarLifted: Boolean? = null
    private var gameDetails: GameDetails? = null

    private val onBottomSheetClosedCallback = object: OnBottomSheetClosed {
        override fun onSuccess(data: UserListModel?, operation: BottomSheetOperation) {
            gameDetails?.watchList = if (operation == BottomSheetOperation.DELETE)
                null
            else
                data?.toGamePlayList()

            if (operation != BottomSheetOperation.UPDATE)
                handleWatchListLottie(
                    binding.detailsInclude,
                    gameDetails?.watchList == null
                )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameDetailsBinding.inflate(inflater, container, false)
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
        if (gameDetails != null)
            setImage(gameDetails!!.imageURL)

        if (shouldFetchData)
            viewModel.getGameDetails(args.gameId)

        super.onStart()
    }

    override fun onStop() {
        if (gameDetails?.imageURL != null)
            Glide.with(this).clear(binding.detailsToolbarIV)
        super.onStop()
    }

    private fun setObservers() {
        if (!(viewModel.gameDetails.hasObservers() || viewModel.gameDetails.value is NetworkResponse.Success || viewModel.gameDetails.value is NetworkResponse.Loading))
            viewModel.getGameDetails(args.gameId)

        viewModel.gameDetails.observe(viewLifecycleOwner) { response ->
            binding.apply {
                shouldFetchData = false
                isResponseFailed = response is NetworkResponse.Failure
                toggleCollapsingLayoutScroll(detailsCollapsingToolbar, response !is NetworkResponse.Loading)
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
                        gameDetails = response.data.data

                        viewModel.viewModelScope.launch {
                            setUI()
                            setLottieUI(
                                detailsInclude,
                                gameDetails,
                                createConsumeLater = {
                                    gameDetails!!.apply {
                                        detailsConsumeLaterViewModel.createConsumeLater(
                                            ConsumeLaterBody(id, rawgID.toString(), null, TYPE, null)
                                        )
                                    }
                                },
                                showBottomSheet = {
                                    val watchList = gameDetails!!.watchList

                                    activity?.let {
                                        val listBottomSheet = UserListBottomSheet(
                                            watchList,
                                            ContentType.GAME,
                                            if (watchList == null) BottomSheetState.EDIT else BottomSheetState.VIEW,
                                            gameDetails!!.id,
                                            gameDetails!!.rawgID.toString(),
                                            null, null,
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

                        if (gameDetails?.watchList != null)
                            handleWatchListLottie(
                                binding.detailsInclude,
                                gameDetails?.watchList == null
                            )

                        if (gameDetails?.consumeLater != null)
                            handleConsumeLaterLottie(
                                binding.detailsInclude,
                                gameDetails?.consumeLater == null
                            )
                    }
                    NetworkResponse.Loading -> loadingLayout.setVisible()
                }
            }
        }

        detailsConsumeLaterViewModel.consumeLater.observe(viewLifecycleOwner) { response ->
            handleUserInteractionLoading(response, binding.detailsInclude)

            if (response is NetworkResponse.Success)
                gameDetails?.consumeLater = response.data.data

            if (gameDetails?.consumeLater != null)
                handleConsumeLaterLottie(
                    binding.detailsInclude,
                    gameDetails?.consumeLater == null
                )

            if (response is NetworkResponse.Failure)
                context?.showErrorDialog(response.errorMessage)
        }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (isResponseFailed && it)
                viewModel.getGameDetails(args.gameId)
        }
    }

    private suspend fun setUI() {
        if (isAppBarLifted != null)
            binding.detailsAppBarLayout.setExpanded(!isAppBarLifted!!)

        gameDetails!!.apply {
            setReviewSummary(binding.reviewSummaryLayout, reviews)
            setImage(imageURL)

            withContext(Dispatchers.Default) {
                val releaseDateStr = if (releaseDate != null && releaseDate.isNotEmptyOrBlank())
                    releaseDate.convertToHumanReadableDateString(isAlt = true)
                else if (tba)
                    "TBA"
                else
                    ageRating ?: ""

                val descriptionStr = Html.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)

                val ratingStr = rawgRating.toString()
                val rateCountStr = " | $rawgRatingCount"

                val metacriticStr = if (metacriticScore != null && !tba && metacriticScore > 0)
                    "Metacritic $metacriticScore"
                else
                    getString(R.string.not_rated_yet)

                withContext(Dispatchers.Main) {
                    binding.detailsReleaseTV.text = releaseDateStr
                    binding.detailsTitleTV.text = title
                    binding.detailsOriginalTV.text = titleOriginal
                    binding.detailsDescriptionTV.text = descriptionStr

                    binding.detailsInclude.apply {
                        interactionRateTV.text = ratingStr
                        interactionsRateCountTV.text = rateCountStr
                    }

                    binding.detailsMetacriticTV.text = metacriticStr
                }
            }

            if (!screenshots.isNullOrEmpty()) {
                binding.detailsMediaImageSlider.apply {
                    val imageSliderList = screenshots.map {
                        SlideModel(imageUrl = it)
                    }

                    setImageList(imageSliderList)

                    setItemClickListener(object: ItemClickListener {
                        override fun doubleClick(position: Int) {}

                        override fun onItemSelected(position: Int) {
                            if (imageSliderList[position].imageUrl?.isNotEmptyOrBlank() == true && navController.currentDestination?.id == R.id.gameDetailsFragment) {
                                isAppBarLifted = binding.detailsAppBarLayout.isLifted

                                val navWithAction = GameDetailsFragmentDirections.actionGameDetailsFragmentToImageFragment(
                                    imageSliderList[position].imageUrl!!,
                                    isRatioDifferent = true
                                )

                                navController.navigate(navWithAction)
                            }
                        }
                    })
                }
            }

            binding.detailsMediaTV.setVisibilityByCondition(screenshots.isNullOrEmpty())
            binding.detailsMediaImageSliderCV.setVisibilityByCondition(screenshots.isNullOrEmpty())
        }
    }

    private fun setImage(imageURL: String) {
        binding.detailsToolbarProgress.setVisible()
        Glide.with(binding.root.context).load(imageURL).addListener(object: RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                _binding?.detailsToolbarProgress?.setGone()
                _binding?.detailsAppBarLayout?.setExpanded(false)

                if (_binding != null) {
                    onImageFailedHandler(
                        binding.detailsCollapsingToolbar,
                        binding.detailsNestedSV
                    )
                }

                return false
            }

            override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                _binding?.detailsToolbarProgress?.setGone()
                return false
            }
        }).thumbnail(
            Glide.with(binding.root.context)
                .load(imageURL)
                .sizeMultiplier(0.25f)
        ).into(binding.detailsToolbarIV)
    }

    private fun setListeners() {
        binding.apply {
            detailsToolbarIV.setSafeOnClickListener(interval = 750) {
                if (gameDetails?.imageURL?.isNotEmptyOrBlank() == true && navController.currentDestination?.id == R.id.gameDetailsFragment) {
                    val navWithAction = GameDetailsFragmentDirections.actionGameDetailsFragmentToImageFragment(
                        gameDetails!!.imageURL,
                        isRatioDifferent = true
                    )

                    navController.navigate(navWithAction)
                }
            }

            detailsToolbarBackButton.setSafeOnClickListener {
                if (!navController.popBackStack()) {
                    navController.navigate(GameDetailsFragmentDirections.actionGlobalNavigationDiscover())
                }
            }

            detailsToolbarShareButton.setSafeOnClickListener {
                val shareURL = "$BASE_DOMAIN_URL/game/${gameDetails?.id}"

                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TITLE, getString(R.string.share_game))
                    putExtra(Intent.EXTRA_TEXT, shareURL)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }

            detailsDescriptionTV.setOnClickListener {
                detailsDescriptionTV.toggle()
            }

            reviewSummaryLayout.seeAllButton.setSafeOnClickListener {
                if (gameDetails != null) {
                    isAppBarLifted = binding.detailsAppBarLayout.isLifted
                    shouldFetchData = true

                    val navWithAction = GameDetailsFragmentDirections.actionGameDetailsFragmentToReviewFragment(
                        contentId = gameDetails!!.id,
                        contentExternalId = null,
                        contentExternalIntId = gameDetails!!.rawgID,
                        contentType = ContentType.GAME.request,
                        contentTitle = gameDetails!!.title,
                        isAlreadyReviewed = gameDetails!!.reviews.isReviewed
                    )
                    navController.navigate(navWithAction)
                }
            }

            reviewSummaryLayout.writeReviewButton.setSafeOnClickListener {
                if (gameDetails != null) {
                    isAppBarLifted = binding.detailsAppBarLayout.isLifted
                    shouldFetchData = true

                    val navWithAction = GameDetailsFragmentDirections.actionGameDetailsFragmentToReviewCreateFragment(
                        review = gameDetails!!.reviews.review,
                        contentId = gameDetails!!.id,
                        contentExternalId = null,
                        contentExternalIntId = gameDetails!!.rawgID,
                        contentType = ContentType.GAME.request,
                        contentTitle = gameDetails!!.title
                    )
                    navController.navigate(navWithAction)
                }
            }

            errorLayoutInc.refreshButton.setOnClickListener {
                viewModel.getGameDetails(args.gameId)
            }

            errorLayoutInc.cancelButton.setOnClickListener {
                navController.popBackStack()
            }
        }
    }

    private suspend fun setRecyclerView() {
        if (!gameDetails?.genres.isNullOrEmpty()) {
            binding.detailsGenreRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                genreAdapter = GenreAdapter(gameDetails!!.genres) {
                    if (navController.currentDestination?.id == R.id.gameDetailsFragment) {
                        val navWithAction = GameDetailsFragmentDirections.actionGameDetailsFragmentToDiscoverListFragment(
                            ContentType.GAME, gameDetails?.genres?.get(it))
                        navController.navigate(navWithAction)
                    }
                }
                setHasFixedSize(true)
                adapter = genreAdapter
            }
        } else {
            binding.detailsGenreTV.setGone()
            binding.detailsGenreRV.setGone()
        }

        withContext(Dispatchers.Default) {
            val developersStr: String = if (gameDetails?.developers.isNullOrEmpty())
                getString(R.string.unknown)
            else
                gameDetails!!.developers.joinToString(separator = getString(R.string.bullet_point))

            val publishersStr: String = if (gameDetails?.publishers.isNullOrEmpty())
                getString(R.string.unknown)
            else
                gameDetails!!.publishers.joinToString(separator = getString(R.string.bullet_point))

            withContext(Dispatchers.Main) {
                binding.detailsDevelopersTV.text = developersStr

                binding.detailsPublishersTV.text = publishersStr
            }
        }

        withContext(Dispatchers.Default) {
            val gameStoreList = gameDetails?.stores?.map { gameStore ->
                AnimeNameURL(
                    GameStoreList.firstOrNull { it.second == gameStore.storeId }?.first ?: getString(R.string.unknown),
                    gameStore.url
                )
            }

            withContext(Dispatchers.Main) {
                if (!gameStoreList.isNullOrEmpty()) {
                    binding.detailsStoreRV.apply {
                        val flexboxLayout = FlexboxLayoutManager(context)
                        flexboxLayout.apply {
                            flexDirection = FlexDirection.ROW
                            justifyContent = JustifyContent.FLEX_START
                            alignItems = AlignItems.FLEX_START
                            flexWrap = FlexWrap.WRAP
                        }
                        layoutManager = flexboxLayout

                        storeAdapter = NameUrlAdapter(gameStoreList)
                        setHasFixedSize(true)
                        adapter = storeAdapter
                    }
                } else {
                    binding.detailsStoreTV.setGone()
                    binding.detailsStoreRV.setGone()
                }
            }
        }

        if (!gameDetails?.relatedGames.isNullOrEmpty()) {
            binding.detailsRelationRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                relationAdapter = GameRelationsAdapter(
                    gameDetails!!.relatedGames,
                    binding.root.context.dpToPxFloat(8f),
                    binding.root.context.dpToPx(130f),
                ) { rawgId, position ->
                    if (navController.currentDestination?.id == R.id.gameDetailsFragment) {
                        isAppBarLifted = binding.detailsAppBarLayout.isLifted
                        recommendationPosition = position

                        val navWithAction = GameDetailsFragmentDirections.actionGameDetailsFragmentSelf(rawgId.toString())
                        navController.navigate(navWithAction)
                    }
                }
                setHasFixedSize(true)
                adapter = relationAdapter

                if (recommendationPosition != null)
                    scrollToPosition(recommendationPosition!!)
            }
        } else {
            binding.detailsRelationTV.setGone()
            binding.detailsRelationRV.setGone()
        }

        withContext(Dispatchers.Default) {
            val platformUIList = gameDetails?.platforms?.map { platform ->
                GamePlatformUIList.firstOrNull {
                    it.requestMapper.name == platform
                } ?: GamePlatformUI(
                    BackendRequestMapper(platform, platform),
                    R.drawable.ic_game_24,
                )
            }

            withContext(Dispatchers.Main) {
                if (!platformUIList.isNullOrEmpty()) {
                    binding.detailsPlatformRV.apply {
                        val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                        layoutManager = linearLayout

                        platformAdapter = GamePlatformAdapter(platformUIList)

                        setHasFixedSize(true)
                        adapter = platformAdapter
                    }
                } else {
                    binding.detailsPlatformTV.setGone()
                    binding.detailsPlatformRV.setGone()
                }
            }
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
            viewModel.gameDetails.removeObservers(this)
            detailsConsumeLaterViewModel.consumeLater.removeObservers(this)
        }

        storeAdapter = null
        genreAdapter = null
        relationAdapter = null
        platformAdapter = null

        super.onDestroyView()
    }
}