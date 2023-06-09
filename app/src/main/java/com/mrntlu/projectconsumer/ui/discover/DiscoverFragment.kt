package com.mrntlu.projectconsumer.ui.discover

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import com.google.android.material.tabs.TabLayout
import com.mrntlu.projectconsumer.adapters.GridAdapter
import com.mrntlu.projectconsumer.databinding.FragmentDiscoverBinding
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank

class DiscoverFragment : BaseFragment<FragmentDiscoverBinding>() {

    private companion object {
        private const val KEY_VALUE = "content_type"
    }

    private var gridAdapter: GridAdapter? = null
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
                it.getString(KEY_VALUE, Constants.ContentType.MOVIE.value)
            )
        }

        setUI()
        setListeners()
        setXMLGridLayout()
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
            }

            discoverSearchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query?.isNotEmptyOrBlank() == true) {
                        discoverSearchView.isIconified = true
                        discoverSearchView.isIconified = true

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

    private fun setXMLGridLayout() {
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

        gridAdapter = GridAdapter(
            requireContext(), list,
            onDiscoveryClicked = {
                val navWithAction =
                    DiscoverFragmentDirections.actionNavigationDiscoverToDiscoverListFragment(
                        contentType, null
                    )

                navController.navigate(navWithAction)
            },
            onGenreClicked = {
                val navWithAction =
                    DiscoverFragmentDirections.actionNavigationDiscoverToDiscoverListFragment(
                        contentType, it
                    )

                navController.navigate(navWithAction)
            }
        )
        binding.gridView.numColumns = if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 3 else 2
        binding.gridView.adapter = gridAdapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(KEY_VALUE, contentType.value)
    }

    override fun onDestroyView() {
        gridAdapter = null
        super.onDestroyView()
    }
}