package com.mrntlu.projectconsumer.ui.common

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.HomeFragmentFactory
import com.mrntlu.projectconsumer.adapters.HomePagerAdapter
import com.mrntlu.projectconsumer.databinding.FragmentHomeBinding
import com.mrntlu.projectconsumer.ui.BaseToolbarAuthFragment
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.setGone

class HomeFragment : BaseToolbarAuthFragment<FragmentHomeBinding>() {
    private var viewPagerAdapter: HomePagerAdapter? = null
    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUI()
        setListeners()
        setSharedObservers()
        setObservers()
    }

    @SuppressLint("InflateParams")
    private fun setUI() {
        binding.homeViewPager.apply {
            val fragmentFactory = HomeFragmentFactory()

            viewPagerAdapter = HomePagerAdapter(
                this@HomeFragment.childFragmentManager,
                viewLifecycleOwner.lifecycle,
                fragmentFactory,
            )
            adapter = viewPagerAdapter
            isUserInputEnabled = false

            mediator = TabLayoutMediator(binding.homeTabLayout.root, this) { tab, position ->
                tab.text = Constants.TabList[position]
            }
            mediator?.attach()
        }

        binding.homeTabLayout.tabLayout.apply {
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

    private fun setListeners() {
        binding.homeTabLayout.tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null && tab.position > -1) {
                    viewModel.setSelectedTabIndex(tab.position)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })
    }

    private fun setObservers() {
        viewModel.selectedTabIndex.observe(viewLifecycleOwner) { index ->
            binding.homeTabLayout.tabLayout.getTabAt(index)?.select()
            binding.homeViewPager.currentItem = index

            binding.homeTabLayout.tabLayout.apply {
                post {
                    val tabView = (getChildAt(0) as ViewGroup).getChildAt(index)
                    val scrollToX = tabView.left - (width - tabView.width) / 2
                    scrollTo(scrollToX, 0)
                }
            }
        }
    }

    override fun onDestroyView() {
        mediator?.detach()
        mediator = null

        viewPagerAdapter = null

        super.onDestroyView()
    }
}