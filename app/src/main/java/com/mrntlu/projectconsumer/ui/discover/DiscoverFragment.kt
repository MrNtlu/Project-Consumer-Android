package com.mrntlu.projectconsumer.ui.discover

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.core.view.GestureDetectorCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.tabs.TabLayout
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.GridAdapter
import com.mrntlu.projectconsumer.databinding.FragmentDiscoverBinding
import com.mrntlu.projectconsumer.ui.BaseToolbarAuthFragment
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.getColorFromAttr
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition

class DiscoverFragment : BaseToolbarAuthFragment<FragmentDiscoverBinding>() {
    private companion object {
        private const val KEY_VALUE = "content_type"

        const val SWIPE_THRESHOLD = 100
    }

    private var gestureDetector: GestureDetectorCompat? = null
    private var gridAdapter: GridAdapter? = null
    private var contentType: Constants.ContentType = Constants.ContentType.MOVIE

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val e1X = e1?.x ?: 0f
            if (e2.x - e1X > SWIPE_THRESHOLD) {
                selectTab(true)
            } else if (e1X - e2.x > SWIPE_THRESHOLD) {
                selectTab(false)
            }
            return true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            contentType = Constants.ContentType.fromStringValue(
                it.getString(KEY_VALUE, Constants.ContentType.MOVIE.value)
            )
        }

        setUI()
        setListeners()
        setObservers()
        setXMLGridLayout()
    }

    private fun selectTab(isRight: Boolean) {
        binding.discoverTabLayout.tabLayout.apply {
            val newSelectedIndex: Int = if (selectedTabPosition == 0 && isRight)
                tabCount.minus(1)
            else if (selectedTabPosition == tabCount.minus(1) && !isRight)
                0
            else
                selectedTabPosition.plus(if (isRight) -1 else 1)

            viewModel.setSelectedTabIndex(newSelectedIndex)

            val tabToSelect = getTabAt(newSelectedIndex)
            tabToSelect?.select()
        }
    }

    @SuppressLint("InflateParams")
    private fun setUI() {
        binding.apply {
            gestureDetector = GestureDetectorCompat(this.root.context, GestureListener())

            discoverTabLayout.tabLayout.apply {
                for (tab in Constants.TabList) {
                    addTab(
                        newTab().setText(tab),
                        tab == contentType.value
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
                        tabLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        layoutParams = tabLayoutParams

                        tabIV?.setGone()
                    }

                    getTabAt(position)?.customView = layout
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        binding.apply {
            aiRecommendationLayout.root.setSafeOnClickListener {
                navController.navigate(R.id.action_navigation_discover_to_AISuggestionsFragment)
            }

            discoverConstraintLayout.setOnTouchListener { _, event ->
                gestureDetector?.onTouchEvent(event)
                true
            }

            discoverTabLayout.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    hideKeyboard()

                    if (tab != null && tab.position > -1) {
                        viewModel.setSelectedTabIndex(tab.position)
                    }

                    when(tab?.position) {
                        0 -> {
                            contentType = Constants.ContentType.MOVIE
                        }
                        1 -> {
                            contentType = Constants.ContentType.TV
                        }
                        2 -> {
                            contentType = Constants.ContentType.ANIME
                        }
                        3 -> {
                            contentType = Constants.ContentType.GAME
                        }
                       else -> {}
                    }

                    setXMLGridLayout()
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })

            discoverScrollView.setOnTouchListener { _, event ->
                if (event != null && event.action == MotionEvent.ACTION_MOVE) {
                    if (discoverSearchView.hasFocus())
                        discoverSearchView.clearFocus()

                    hideKeyboard()
                }

                false
            }

            discoverSearchView.apply {
                setOnClickListener {
                    isIconified = false
                }

                setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        if (query?.isNotEmptyOrBlank() == true) {
                            discoverSearchView.isIconified = true
                            discoverSearchView.isIconified = true

                            if (navController.currentDestination?.id == R.id.navigation_discover) {
                                val navWithAction = DiscoverFragmentDirections.actionNavigationDiscoverToMovieSearchFragment(
                                    query, contentType,
                                )

                                navController.navigate(navWithAction)
                            }
                        }

                        return true
                    }

                    override fun onQueryTextChange(newText: String?) = true
                })
            }
        }
    }

    private fun setObservers() {
        viewModel.selectedTabIndex.observe(viewLifecycleOwner) { index ->
            binding.discoverTabLayout.tabLayout.getTabAt(index)?.select()

            binding.discoverTabLayout.tabLayout.apply {
                post {
                    val tabView = (getChildAt(0) as ViewGroup).getChildAt(index)
                    val scrollToX = tabView.left - (width - tabView.width) / 2
                    scrollTo(scrollToX, 0)
                }
            }
        }
    }

    private fun setXMLGridLayout() {
        binding.discoverSearchView.setVisibilityByCondition(contentType == Constants.ContentType.GAME)

        val cellHeight = 100f
        val padding = 3

        val list = when(contentType) {
            Constants.ContentType.ANIME -> Constants.AnimeGenreList
            Constants.ContentType.MOVIE -> Constants.MovieGenreList
            Constants.ContentType.TV -> Constants.TVGenreList
            Constants.ContentType.GAME -> Constants.GameGenreList
        }

        val layoutParams = binding.gridView.layoutParams
        val columnCount = if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 3 else 2

        layoutParams.height = list.size.div(columnCount).plus(
            if (list.size.rem(columnCount) > 0) 1 else 0
        ).times(
            binding.root.context.dpToPx(cellHeight).plus(padding.times(2))
        ).plus(12)

        binding.gridView.layoutParams = layoutParams

        binding.aiRecommendationLayout.apply {
            Glide.with(root.context).load("https://www.techrepublic.com/wp-content/uploads/2023/07/tr71123-ai-art.jpeg").addListener(object: RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                    genreIV.setGone()
                    return false
                }

                override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean { return false }

            }).into(genreIV)
            genreTV.text = getString(R.string.suggestions)
        }

        gridAdapter = GridAdapter(
            requireContext(), list,
            onDiscoveryClicked = {
                if (navController.currentDestination?.id == R.id.navigation_discover) {
                    val navWithAction = DiscoverFragmentDirections.actionNavigationDiscoverToDiscoverListFragment(
                        contentType, null
                    )

                    navController.navigate(navWithAction)
                }
            },
            onGenreClicked = {
                if (navController.currentDestination?.id == R.id.navigation_discover) {
                    val navWithAction = DiscoverFragmentDirections.actionNavigationDiscoverToDiscoverListFragment(
                        contentType, it
                    )

                    navController.navigate(navWithAction)
                }
            }
        )
        binding.gridView.numColumns = columnCount
        binding.gridView.adapter = gridAdapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(KEY_VALUE, contentType.value)
    }

    override fun onDestroyView() {
        gestureDetector = null
        gridAdapter = null
        super.onDestroyView()
    }
}