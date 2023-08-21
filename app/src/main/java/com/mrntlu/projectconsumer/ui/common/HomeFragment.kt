package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.mrntlu.projectconsumer.adapters.HomeFragmentFactory
import com.mrntlu.projectconsumer.adapters.HomePagerAdapter
import com.mrntlu.projectconsumer.databinding.FragmentHomeBinding
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.viewmodels.shared.ViewPagerSharedViewModel

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: ViewPagerSharedViewModel by activityViewModels()

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
        setObservers()
    }

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

            mediator = TabLayoutMediator(binding.homeTabLayout, this) { tab, position ->
                tab.text = Constants.TabList[position]
            }
            mediator?.attach()
        }
    }

    private fun setObservers() {
        val layoutParams = binding.homeTabLayout.layoutParams as ViewGroup.MarginLayoutParams

        viewModel.scrollYPosition.observe(viewLifecycleOwner) { hideTabLayout ->
            if (hideTabLayout in 0..300) {
                layoutParams.topMargin = -hideTabLayout / 2
                binding.homeTabLayout.layoutParams = layoutParams
            } else {
                layoutParams.topMargin = -150
                binding.homeTabLayout.layoutParams = layoutParams
            }
        }
    }

    override fun onDestroyView() {
        viewModel.scrollYPosition.removeObservers(viewLifecycleOwner)

        mediator?.detach()
        mediator = null

        viewPagerAdapter = null

        super.onDestroyView()
    }
}