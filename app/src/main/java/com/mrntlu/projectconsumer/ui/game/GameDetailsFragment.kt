package com.mrntlu.projectconsumer.ui.game

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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
import com.mrntlu.projectconsumer.adapters.decorations.BulletItemDecoration
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
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.Constants.GamePlatformUIList
import com.mrntlu.projectconsumer.utils.Constants.GameStoreList
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.main.game.GameDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GameDetailsFragment : BaseDetailsFragment<FragmentGameDetailsBinding>() {

    companion object {
        private const val TYPE = "game"
    }

    private val viewModel: GameDetailsViewModel by viewModels()
    private val args: GameDetailsFragmentArgs by navArgs()

    private var storeAdapter: NameUrlAdapter? = null
    private var genreAdapter: GenreAdapter? = null
    private var relationAdapter: GameRelationsAdapter? = null
    private var platformAdapter: GamePlatformAdapter? = null

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

        setObservers()
    }

    private fun setObservers() {
        if (!(viewModel.gameDetails.hasObservers() || viewModel.gameDetails.value is NetworkResponse.Success || viewModel.gameDetails.value is NetworkResponse.Loading))
            viewModel.getGameDetails(args.gameId)

        viewModel.gameDetails.observe(viewLifecycleOwner) { response ->
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
                        gameDetails = response.data.data

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
                                        Constants.ContentType.GAME,
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
                    else -> {}
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

    private fun setUI() {
        gameDetails!!.apply {
            binding.detailsToolbarProgress.setVisible()
            Glide.with(requireContext()).load(imageURL).addListener(object:
                RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    binding.detailsToolbarProgress.setGone()
                    binding.detailsAppBarLayout.setExpanded(false)

                    onImageFailedHandler(
                        binding.detailsCollapsingToolbar,
                        binding.detailsNestedSV
                    )

                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    _binding?.detailsToolbarProgress?.setGone()
                    return false
                }
            }).into(binding.detailsToolbarIV)

            binding.detailsTitleTV.text = title
            binding.detailsOriginalTV.text = titleOriginal
            binding.detailsDescriptionTV.text = Html.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)

            binding.detailsInclude.apply {
                interactionRateTV.text = rawgRating.toString()
                val rateCountText = " | $rawgRatingCount"
                interactionsRateCountTV.text = rateCountText
            }

            val releaseDateStr = if (releaseDate != null && releaseDate.isNotEmptyOrBlank())
                releaseDate.convertToHumanReadableDateString(isAlt = true)
            else if (tba)
                "TBA"
            else
                ageRating ?: ""
            binding.detailsReleaseTV.text = releaseDateStr

            val metacriticStr = if (metacriticScore != null && !tba && metacriticScore > 0)
                "Metacritic $metacriticScore"
            else
                getString(R.string.not_rated_yet)
            binding.detailsMetacriticTV.text = metacriticStr
        }
    }

    private fun setListeners() {
        binding.apply {
            detailsToolbarIV.setOnClickListener {
                if (gameDetails?.imageURL?.isNotEmptyOrBlank() == true) {
                    val navWithAction = GameDetailsFragmentDirections.actionGameDetailsFragmentToImageFragment(
                        gameDetails!!.imageURL,
                        isRatioDifferent = true
                    )

                    navController.navigate(navWithAction)
                }
            }

            detailsToolbarBackButton.setSafeOnClickListener {
                navController.popBackStack()
            }

            detailsDescriptionTV.setOnClickListener {
                detailsDescriptionTV.toggle()
            }

            errorLayoutInc.refreshButton.setOnClickListener {
                viewModel.getGameDetails(args.gameId)
            }

            errorLayoutInc.cancelButton.setOnClickListener {
                navController.popBackStack()
            }
        }
    }

    private fun setRecyclerView() {
        if (!gameDetails?.genres.isNullOrEmpty()) {
            binding.detailsGenreRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout
                val bulletDecoration = BulletItemDecoration(context)
                addItemDecoration(bulletDecoration)

                genreAdapter = GenreAdapter(gameDetails!!.genres) {
                    val navWithAction = GameDetailsFragmentDirections.actionGameDetailsFragmentToDiscoverListFragment(Constants.ContentType.GAME, gameDetails?.genres?.get(it))
                    navController.navigate(navWithAction)
                }
                adapter = genreAdapter
            }
        } else {
            binding.genreDivider.setGone()
            binding.detailsGenreRV.setGone()
        }

        val developersStr: String = if (gameDetails?.developers.isNullOrEmpty())
            getString(R.string.unknown)
        else
            gameDetails!!.developers.joinToString(separator = getString(R.string.bullet_point))

        binding.detailsDevelopersTV.text = developersStr

        val publishersStr: String = if (gameDetails?.publishers.isNullOrEmpty())
            getString(R.string.unknown)
        else
            gameDetails!!.publishers.joinToString(separator = getString(R.string.bullet_point))

        binding.detailsPublishersTV.text = publishersStr

        val gameStoreList = gameDetails?.stores?.map { gameStore ->
            AnimeNameURL(
                GameStoreList.firstOrNull { it.second == gameStore.storeId }?.first ?: getString(R.string.unknown),
                gameStore.url
            )
        }

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
                adapter = storeAdapter
            }
        } else {
            binding.detailsStoreTV.setGone()
            binding.detailsStoreRV.setGone()
        }

        if (!gameDetails?.relatedGames.isNullOrEmpty()) {
            binding.detailsRelationRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                relationAdapter = GameRelationsAdapter(
                    gameDetails!!.relatedGames,
                    !sharedViewModel.isLightTheme()
                ) { rawgId ->
                    val navWithAction = GameDetailsFragmentDirections.actionGameDetailsFragmentSelf(rawgId.toString())
                    navController.navigate(navWithAction)
                }
                adapter = relationAdapter
            }
        } else {
            binding.detailsRelationTV.setGone()
            binding.detailsRelationRV.setGone()
        }

        val platformUIList = gameDetails?.platforms?.map { platform ->
            GamePlatformUIList.firstOrNull {
                it.requestMapper.name == platform
            } ?: GamePlatformUI(
                BackendRequestMapper(platform, platform),
                R.drawable.ic_game_24,
            )
        }

        if (!platformUIList.isNullOrEmpty()) {
            binding.detailsPlatformRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                platformAdapter = GamePlatformAdapter(platformUIList)
                adapter = platformAdapter
            }
        } else {
            binding.detailsPlatformTV.setGone()
            binding.detailsPlatformRV.setGone()
        }
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