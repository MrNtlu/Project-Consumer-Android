package com.mrntlu.projectconsumer.ui.common

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.mrntlu.projectconsumer.MainActivity
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.DiscoverAdapter
import com.mrntlu.projectconsumer.adapters.PreviewAdapter
import com.mrntlu.projectconsumer.databinding.FragmentHomeBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateMembershipBody
import com.mrntlu.projectconsumer.models.common.GenreUI
import com.mrntlu.projectconsumer.models.common.retrofit.Preview
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.Constants.AnimeGenreList
import com.mrntlu.projectconsumer.utils.Constants.ContentType.*
import com.mrntlu.projectconsumer.utils.Constants.GameGenreList
import com.mrntlu.projectconsumer.utils.Constants.MovieGenreList
import com.mrntlu.projectconsumer.utils.Constants.TVGenreList
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.openInBrowser
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByConditionWithAnimation
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.viewmodels.main.common.PreviewViewModel
import com.mrntlu.projectconsumer.viewmodels.shared.HomeDiscoverSharedViewModel
import com.mrntlu.projectconsumer.viewmodels.shared.UserSharedViewModel
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.logInWith
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: PreviewViewModel by viewModels()
    private val homeDiscoverViewModel: HomeDiscoverSharedViewModel by viewModels()
    private val userSharedViewModel: UserSharedViewModel by activityViewModels()

    private var discoverAdapter: DiscoverAdapter? = null
    private var upcomingAdapter: PreviewAdapter<ContentModel>? = null
    private var topRatedAdapter: PreviewAdapter<ContentModel>? = null
    private var extraAdapter: PreviewAdapter<ContentModel>? = null
    private var showCaseAdapter: PreviewAdapter<ContentModel>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTabLayout()
        setRecyclerView()
        setListeners()
        setObservers()
    }

    @SuppressLint("InflateParams")
    private fun setTabLayout() {
        binding.homeTabLayout.tabLayout.apply {
            for (tab in Constants.TabList) {
                addTab(
                    newTab().setText(tab),
                    tab == homeDiscoverViewModel.contentType().value
                )
            }

            for (position in 0..tabCount.minus(1)) {
                val layout = LayoutInflater.from(context).inflate(R.layout.layout_tab_title, null) as? LinearLayout

                val tabIV = layout?.findViewById<ImageView>(R.id.tabIV)
                val tabLayoutParams = layoutParams
                if (sharedViewModel.isTabIconsEnabled()) {
                    tabLayoutParams.height = context.dpToPx(65f)
                    layoutParams = tabLayoutParams

                    tabIV?.setImageResource(Constants.TabIconList[position])
                } else {
                    tabLayoutParams.height = LayoutParams.WRAP_CONTENT
                    layoutParams = tabLayoutParams

                    tabIV?.setGone()
                }

                getTabAt(position)?.customView = layout
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        binding.apply {
            // TODO Implement
//            aiRecommendationLayout.root.setSafeOnClickListener {
//                navController.navigate(R.id.action_navigation_home_to_AISuggestionsFragment)
//            }

            homeSearchView.apply {
                setOnClickListener {
                    isIconified = false
                }

                setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        if (query?.isNotEmptyOrBlank() == true) {
                            homeSearchView.isIconified = true
                            homeSearchView.isIconified = true

                            if (navController.currentDestination?.id == R.id.navigation_home) {
                                val navWithAction = HomeFragmentDirections.actionNavigationHomeToMovieSearchFragment(
                                    query, homeDiscoverViewModel.contentType(),
                                )

                                navController.navigate(navWithAction)
                            }
                        }

                        return true
                    }

                    override fun onQueryTextChange(newText: String?) = true
                })
            }

            homeScrollView.setOnTouchListener { _, event ->
                if (event != null && event.action == MotionEvent.ACTION_MOVE) {
                    if (homeSearchView.hasFocus())
                        homeSearchView.clearFocus()

                    hideKeyboard()
                }

                false
            }

            homeTabLayout.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab != null && tab.position > -1) {
                        homeDiscoverViewModel.setSelectedTabIndex(tab.position)

                        homeDiscoverViewModel.setContentType(
                            when(tab.position) {
                                0 -> MOVIE
                                1 -> TV
                                2 -> ANIME
                                else -> GAME
                            }
                        )

                        discoverAdapter?.changeList(handleGenreList())
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })

            seeAllButtonPopular.setOnClickListener {
                navigateList(FetchType.POPULAR)
            }

            seeAllButtonFirst.setOnClickListener {
                navigateList(FetchType.UPCOMING)
            }

            seeAllButtonSecond.setOnClickListener {
                navigateList(FetchType.TOP)
            }

            seeAllButtonExtra.setOnClickListener {
                navigateList(FetchType.EXTRA)
            }
        }
    }

    private fun setObservers() {
        viewModel.previewList.observe(viewLifecycleOwner) {
            handleObserver(it)
        }

        homeDiscoverViewModel.currentContentType.observe(viewLifecycleOwner) {
            if (viewModel.previewList.value != null && viewModel.previewList.value is NetworkResponse.Success)
                setUI((viewModel.previewList.value!! as NetworkResponse.Success).data)
        }

        homeDiscoverViewModel.selectedTabIndex.observe(viewLifecycleOwner) { index ->
            binding.homeTabLayout.tabLayout.getTabAt(index)?.select()

            binding.homeTabLayout.tabLayout.apply {
                post {
                    val tabView = (getChildAt(0) as ViewGroup).getChildAt(index)
                    val scrollToX = tabView.left - (width - tabView.width) / 2
                    scrollTo(scrollToX, 0)
                }
            }
        }

        userSharedViewModel.userInfoResponse.observe(viewLifecycleOwner) { response ->
            if (response is NetworkResponse.Success) {
                userSharedViewModel.userInfo = response.data.data

                if (Purchases.sharedInstance.isAnonymous) {
                    Purchases.sharedInstance.logInWith(
                        response.data.data.email,
                        onError = {},
                        onSuccess = { customerInfo, _ ->
                            handleCustomerInfo(customerInfo, response.data.data.isPremium)
                        }
                    )
                } else {
                    Purchases.sharedInstance.getCustomerInfoWith(onError = {}, onSuccess = {
                        handleCustomerInfo(it, response.data.data.isPremium)
                    })
                }

                (activity as? MainActivity)?.setBottomNavProfile(response.data.data.image ?: "")
            }
        }

        sharedViewModel.networkStatus.observe(viewLifecycleOwner) {
            if (
                (upcomingAdapter?.errorMessage != null && topRatedAdapter?.errorMessage != null && extraAdapter?.errorMessage != null) && it
            ) {
                viewModel.getPreview()
            }
        }

        sharedViewModel.isAuthenticated.observe(viewLifecycleOwner) {
            setHomeCardView()
        }
    }

    private fun setRecyclerView() {
        val isRatioDifferent = homeDiscoverViewModel.currentContentType.value == GAME
        val radiusInPx = binding.root.context.dpToPxFloat(8f)

        binding.discoverRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            discoverAdapter = DiscoverAdapter(
                handleGenreList(),
                onDiscoveryClicked = {
                    if (navController.currentDestination?.id == R.id.navigation_home) {
                        val navWithAction = HomeFragmentDirections.actionNavigationHomeToDiscoverListFragment(
                            homeDiscoverViewModel.contentType(), null
                        )

                        navController.navigate(navWithAction)
                    }
                },
                onGenreClicked = {
                    if (navController.currentDestination?.id == R.id.navigation_home) {
                        val navWithAction = HomeFragmentDirections.actionNavigationHomeToDiscoverListFragment(
                            homeDiscoverViewModel.contentType(), it
                        )

                        navController.navigate(navWithAction)
                    }
                }
            )
            adapter = discoverAdapter
        }

        binding.previewShowcaseRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            showCaseAdapter = PreviewAdapter(
                object: Interaction<ContentModel> {
                    override fun onItemSelected(item: ContentModel, position: Int) {
                        navigateDetails(item.id)
                    }

                    override fun onErrorRefreshPressed() {
                        viewModel.getPreview()
                    }

                    override fun onCancelPressed() {
                        navController.popBackStack()
                    }

                    override fun onExhaustButtonPressed() {}
                },
                isRatioDifferent = isRatioDifferent,
                isDarkTheme = !sharedViewModel.isLightTheme(),
                radiusInPx = radiusInPx,
            )
            setHasFixedSize(true)
            adapter = showCaseAdapter
        }

        binding.upcomingPreviewRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            upcomingAdapter = PreviewAdapter(
                object: Interaction<ContentModel> {
                    override fun onItemSelected(item: ContentModel, position: Int) {
                        navigateDetails(item.id)
                    }

                    override fun onCancelPressed() {
                        navController.popBackStack()
                    }

                    override fun onErrorRefreshPressed() {
                        viewModel.getPreview()
                    }

                    override fun onExhaustButtonPressed() {}
                },
                isRatioDifferent = isRatioDifferent,
                isDarkTheme = !sharedViewModel.isLightTheme(),
                radiusInPx = radiusInPx,
            )
            setHasFixedSize(true)
            adapter = upcomingAdapter
        }

        binding.topRatedPreviewRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            topRatedAdapter = PreviewAdapter(
                object: Interaction<ContentModel> {
                    override fun onItemSelected(item: ContentModel, position: Int) {
                        navigateDetails(item.id)
                    }

                    override fun onCancelPressed() {
                        navController.popBackStack()
                    }

                    override fun onErrorRefreshPressed() {
                        viewModel.getPreview()
                    }

                    override fun onExhaustButtonPressed() {}
                },
                isRatioDifferent = isRatioDifferent,
                isDarkTheme = !sharedViewModel.isLightTheme(),
                radiusInPx = radiusInPx,
            )
            setHasFixedSize(true)
            adapter = topRatedAdapter
        }

        binding.extraPreviewRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            extraAdapter = PreviewAdapter(
                object: Interaction<ContentModel> {
                    override fun onItemSelected(item: ContentModel, position: Int) {
                        navigateDetails(item.id)
                    }

                    override fun onCancelPressed() {
                        navController.popBackStack()
                    }

                    override fun onErrorRefreshPressed() {
                        viewModel.getPreview()
                    }

                    override fun onExhaustButtonPressed() {}
                },
                isRatioDifferent = isRatioDifferent,
                isDarkTheme = !sharedViewModel.isLightTheme(),
                radiusInPx = radiusInPx,
            )
            setHasFixedSize(true)
            adapter = extraAdapter
        }
    }

    private fun setUI(response: Preview) {
        val isGame = homeDiscoverViewModel.currentContentType.value == GAME
        val extraStr = if (homeDiscoverViewModel.contentType() == MOVIE)
            getString(R.string.in_theaters)
        else
            getString(R.string.airing_today)

        if (isGame) {
            binding.upcomingPreviewRV.apply {
                adapter = null
                adapter = upcomingAdapter
            }

            binding.previewShowcaseRV.apply {
                adapter = null
                adapter = showCaseAdapter
            }

            binding.topRatedPreviewRV.apply {
                adapter = null
                adapter = topRatedAdapter
            }
        }

        binding.extraRVTV.text = extraStr

        upcomingAdapter?.setData(
            when (homeDiscoverViewModel.contentType()) {
                ANIME -> response.anime.upcoming
                MOVIE -> response.movie.upcoming
                TV -> response.tv.upcoming
                GAME -> response.game.upcoming
            },
            isGame,
        )
        showCaseAdapter?.setData(
            when (homeDiscoverViewModel.contentType()) {
                ANIME -> response.anime.popular
                MOVIE -> response.movie.popular
                TV -> response.tv.popular
                GAME -> response.game.popular
            },
            isGame,
        )
        topRatedAdapter?.setData(
            when (homeDiscoverViewModel.contentType()) {
                ANIME -> response.anime.top
                MOVIE -> response.movie.top
                TV -> response.tv.top
                GAME -> response.game.top
            },
            isGame,
        )

        if (homeDiscoverViewModel.contentType() != GAME) {
            extraAdapter?.setData(
                when (homeDiscoverViewModel.contentType()) {
                    ANIME -> response.anime.extra
                    MOVIE -> response.movie.extra
                    else -> response.tv.extra
                }!!,
            )
        }
        binding.extraPreviewRV.setVisibilityByConditionWithAnimation(isGame)
    }

    private fun setHomeCardView() {
        binding.apply {
            homeInfoTV.text = if (sharedViewModel.isLoggedIn())
                getString(R.string.authenticated_home_info)
            else
                getString(R.string.authenticate_home_info)

            homeIV.setVisible()

            homeIV.setImageDrawable(
                ContextCompat.getDrawable(binding.root.context, if (sharedViewModel.isLoggedIn())
                    R.drawable.ic_feedback
                else
                    R.drawable.ic_login_24)
            )

            homeCV.setSafeOnClickListener {
                if (sharedViewModel.isLoggedIn()) {
                    context?.openInBrowser("https://watchlistfy.canny.io/feature-requests")
                } else {
                    if (navController.currentDestination?.id == R.id.navigation_home) {
                        navController.navigate(R.id.action_global_authFragment)
                    }
                }
            }
        }
    }

    private fun navigateList(fetchType: FetchType) {
        if (navController.currentDestination?.id == R.id.navigation_home) {
            val navWithAction = when(homeDiscoverViewModel.contentType()) {
                ANIME -> HomeFragmentDirections.actionNavigationHomeToAnimeListFragment(fetchType.tag)
                MOVIE -> HomeFragmentDirections.actionNavigationHomeToMovieListFragment(fetchType.tag)
                TV -> HomeFragmentDirections.actionNavigationHomeToTvListFragment(fetchType.tag)
                GAME -> HomeFragmentDirections.actionNavigationHomeToGameListFragment(fetchType.tag)
            }

            navController.navigate(navWithAction)
        }
    }

    private fun navigateDetails(id: String) {
        if (navController.currentDestination?.id == R.id.navigation_home) {
            val navWithAction = when(homeDiscoverViewModel.contentType()) {
                ANIME -> HomeFragmentDirections.actionNavigationHomeToAnimeDetailsFragment(id)
                MOVIE -> HomeFragmentDirections.actionNavigationHomeToMovieDetailsFragment(id)
                TV -> HomeFragmentDirections.actionNavigationHomeToTvDetailsFragment(id)
                GAME -> HomeFragmentDirections.actionNavigationHomeToGameDetailsFragment(id)
            }

            navController.navigate(navWithAction)
        }
    }

    private fun handleCustomerInfo(customerInfo: CustomerInfo, isPremium: Boolean) {
        val isMembershipActive = customerInfo.entitlements["premium_membership"]?.isActive

        if (isMembershipActive == true && !isPremium) {
            userSharedViewModel.updateMembership(
                UpdateMembershipBody(
                true,
                if (customerInfo.entitlements["premium_membership"]?.productIdentifier?.equals("watchlistfy_premium_1mo") == true)
                    1
                else
                    2
            )).observe(viewLifecycleOwner) { response ->
                if (response is NetworkResponse.Success)
                    userSharedViewModel.getBasicInfo()
            }
        } else if (
            (isMembershipActive == false || isMembershipActive == null) && isPremium
        ) {
            userSharedViewModel.updateMembership(UpdateMembershipBody(false, 0)).observe(viewLifecycleOwner) { response ->
                if (response is NetworkResponse.Success)
                    userSharedViewModel.getBasicInfo()
            }
        }
    }

    private fun handleGenreList(): ArrayList<GenreUI> {
        return when(homeDiscoverViewModel.contentType()) {
            ANIME -> AnimeGenreList
            MOVIE -> MovieGenreList
            TV -> TVGenreList
            GAME -> GameGenreList
        }.toCollection(ArrayList())
    }

    private fun handleObserver(response: NetworkResponse<Preview>) {
        when(response) {
            is NetworkResponse.Failure -> {
                upcomingAdapter?.setErrorView(response.errorMessage)
                showCaseAdapter?.setErrorView(response.errorMessage)
                topRatedAdapter?.setErrorView(response.errorMessage)
                extraAdapter?.setErrorView(response.errorMessage)
            }
            NetworkResponse.Loading -> {
                upcomingAdapter?.setLoadingView()
                showCaseAdapter?.setLoadingView()
                topRatedAdapter?.setLoadingView()
                extraAdapter?.setLoadingView()
            }
            is NetworkResponse.Success -> {
                viewModel.viewModelScope.launch {
                    setUI(response.data)
                }
            }
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.previewList.removeObservers(this)
            homeDiscoverViewModel.selectedTabIndex.removeObservers(this)
            homeDiscoverViewModel.currentContentType.removeObservers(this)
            userSharedViewModel.userInfoResponse.removeObservers(this)
            sharedViewModel.networkStatus.removeObservers(this)
            sharedViewModel.isAuthenticated.removeObservers(this)
        }

        discoverAdapter = null
        showCaseAdapter = null
        upcomingAdapter = null
        topRatedAdapter = null
        extraAdapter = null
        super.onDestroyView()
    }
}