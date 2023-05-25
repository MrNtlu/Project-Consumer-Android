package com.mrntlu.projectconsumer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.FragmentSettingsBinding
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    private val countryList = Locale.getISOCountries().filter { it.length == 2 }.map {
        val locale = Locale("", it)
        Pair(locale.displayCountry, locale.country.uppercase())
    }.sortedBy {
        it.first
    }

    private val languageList = Locale.getISOLanguages().filter { it.length == 2 }.map {
        val locale = Locale(it)
        Pair(locale.displayLanguage, locale.language.uppercase())
    }.sortedBy {
        it.first
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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

    private fun setUI() {
        //TODO If not logged in hide/show
        binding.apply {

            accountTitleTV.setVisibilityByCondition(!sharedViewModel.isLoggedIn())
            accountSettingsCard.setVisibilityByCondition(!sharedViewModel.isLoggedIn())

            themeSwitch.isChecked = !sharedViewModel.isLightTheme()
            themeSwitchTV.text = getString(if (sharedViewModel.isLightTheme()) R.string.light_theme else R.string.dark_theme)

            applicationFirstClickTile.settingsClickTileTV.text = "Placeholder"

            settingsSpinnerTileTV.text = getString(R.string.change_country)

            val spinnerAdapter = ArrayAdapter(this.root.context, android.R.layout.simple_spinner_item, countryList.map { it.first })
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            settingsSpinner.adapter = spinnerAdapter
            settingsSpinner.setSelection(
                countryList.indexOfFirst {
                    it.second == sharedViewModel.getCountryCode()
                }
            )

            applicationSecondSpinnerTileTV.text = getString(R.string.change_language)

            val languageSpinnerAdapter = ArrayAdapter(this.root.context, android.R.layout.simple_spinner_item, languageList.map { it.first })
            languageSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            applicationSecondSpinner.adapter = languageSpinnerAdapter
            applicationSecondSpinner.setSelection(
                languageList.indexOfFirst {
                    it.second == sharedViewModel.getLanguageCode()
                }
            )
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

            settingsSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    sharedViewModel.setCountryCode(countryList[position].second)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            applicationSecondSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    sharedViewModel.setLanguageCode(languageList[position].second)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }
}