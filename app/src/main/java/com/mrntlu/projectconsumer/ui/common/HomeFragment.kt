package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.google.android.material.tabs.TabLayoutMediator
import com.mrntlu.projectconsumer.R
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
        setMenu()
        setObservers()
    }

    private fun setUI() {
        binding.homeViewPager.apply {
            viewPagerAdapter = HomePagerAdapter(
                this@HomeFragment.childFragmentManager,
                viewLifecycleOwner.lifecycle
            )
            adapter = viewPagerAdapter
            isUserInputEnabled = false

            mediator = TabLayoutMediator(binding.homeTabLayout, this) { tab, position ->
                tab.text = Constants.TabList[position]
            }
            mediator?.attach()
        }
    }

    private fun setMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                if (!sharedViewModel.isLoggedIn())
                    menu.removeItem(R.id.settingsMenu)
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    R.id.settingsMenu -> {
                        navController.navigate(R.id.action_global_settingsFragment)
                    }
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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