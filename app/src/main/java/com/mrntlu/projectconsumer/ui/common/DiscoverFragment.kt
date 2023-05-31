package com.mrntlu.projectconsumer.ui.common

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.tabs.TabLayout
import com.mrntlu.projectconsumer.databinding.FragmentDiscoverBinding
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.compose.GenreGrid
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.printLog

class DiscoverFragment : BaseFragment<FragmentDiscoverBinding>() {

    private companion object {
        private const val KEY_VALUE = "content_type"
    }

    private var contentType: Constants.ContentType = Constants.ContentType.MOVIE

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
                savedInstanceState.getString(KEY_VALUE, Constants.ContentType.MOVIE.value)
            )
        }

        setUI()
        setListeners()
        setGridLayout()
    }

    private fun setUI() {
        binding.apply {
            discoverTabLayout.apply {
                for (tab in Constants.TabList) {
                    addTab(
                        discoverTabLayout.newTab().setText(tab),
                        tab == contentType.value
                    )
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        binding.apply {
            discoverTabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
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

                    setGridLayout()
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
            }

            discoverSearchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query?.isNotEmptyOrBlank() == true) {
                        discoverSearchView.isIconified = true
                        discoverSearchView.isIconified = true

                        //TODO Let user select
                        val navWithAction = DiscoverFragmentDirections.actionNavigationDiscoverToMovieSearchFragment(
                            query, contentType,
                        )

                        navController.navigate(navWithAction)
                    }

                    return true
                }

                override fun onQueryTextChange(newText: String?) = true
            })
        }
    }

    private fun setGridLayout() {
        binding.gridLayoutCompose.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                GenreGrid(
                    !sharedViewModel.isLightTheme(),
                    when(contentType) {
                        Constants.ContentType.ANIME -> Constants.AnimeGenreList
                        Constants.ContentType.MOVIE -> Constants.MovieGenreList
                        Constants.ContentType.TV -> Constants.TVGenreList
                        Constants.ContentType.GAME -> TODO()
                    },
                    onDiscoveryClicked = {
                        printLog("Discover")
                    },
                    onGenreClicked = {
                        printLog("Genre clicked $it")
                    }
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(KEY_VALUE, contentType.value)
    }
}