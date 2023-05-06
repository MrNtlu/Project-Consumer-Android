package com.mrntlu.projectconsumer.ui

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
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.FragmentSettingsBinding
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.viewmodels.shared.ActivitySharedViewModel

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    private val sharedViewModel: ActivitySharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                menu.clear()
            }
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}
            override fun onMenuItemSelected(menuItem: MenuItem) = false
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setUI()
        setListeners()
    }

    //TODO Implement https://developer.android.com/develop/ui/views/components/spinner
    private fun setUI() {
        //TODO If not logged in hide/show
        binding.apply {
            accountTitleTV.setGone()
            accountSettingsCard.setGone()

            themeSwitch.isChecked = !sharedViewModel.isLightTheme()
            themeSwitchTV.text = getString(if (sharedViewModel.isLightTheme()) R.string.light_theme else R.string.dark_theme)

            applicationFirstClickTile.settingsClickTileTV.text = "Placeholder"
        }
    }

    private fun setListeners() {
        binding.apply {
            themeSwitch.setOnCheckedChangeListener { _, isChecked ->
                sharedViewModel.toggleTheme()

                themeSwitchTV.text = getString(if (isChecked) R.string.dark_theme else R.string.light_theme)
            }

            applicationFirstClickTile.root.setOnClickListener {
                accountTitleTV.setVisible()
                accountSettingsCard.setVisible()
            }
        }
    }
}