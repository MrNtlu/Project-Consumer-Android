package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.FragmentSettingsBinding
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.service.TokenManager
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.showConfirmationDialog
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.main.common.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject lateinit var tokenManager: TokenManager

    private lateinit var dialog: LoadingDialog
    private var deleteUserLiveData: LiveData<NetworkResponse<MessageResponse>>? = null

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
        activity?.let {
            dialog = LoadingDialog(it)
        }

        setToolbar()
        setUI()
        setListeners()
    }

    private fun setToolbar() {
        binding.apply {
            settingsToolbar.apply {
                if (sharedViewModel.isLoggedIn()) {
                    setNavigationIcon(R.drawable.ic_arrow_back_24)

                    setNavigationOnClickListener { navController.popBackStack() }
                }
            }

            anonymousInc.root.setVisibilityByCondition(sharedViewModel.isLoggedIn())
        }
    }

    private fun setUI() {
        binding.apply {
            accountTitleTV.setVisibilityByCondition(!sharedViewModel.isLoggedIn())
            accountSettingsCard.setVisibilityByCondition(!sharedViewModel.isLoggedIn())
            deleteAccountButton.setVisibilityByCondition(!sharedViewModel.isLoggedIn())

            applicationFirstClickTile.root.setGone()
            applicationFirstTileDivider.setGone()
            accountFirstClickTile.root.setGone()
            accountFirstTileDivider.setGone()
            accountSwitchTileDivider.setGone()
            accountSwitchTile.setGone()

            if (sharedViewModel.isLoggedIn()) {
                accountSecondClickTile.settingsClickTileTV.text = getString(R.string.log_out)

                accountSecondClickTile.root.setSafeOnClickListener {
                    context?.showConfirmationDialog(getString(R.string.do_you_want_to_log_out_)) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            tokenManager.deleteToken()
                        }
                        GoogleSignIn.getClient(it.context, GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .signOut()
                        sharedViewModel.setAuthentication(false)
                        navController.popBackStack(R.id.navigation_home, false)
                    }
                }
            }

            //Application settings
            themeSwitch.isChecked = !sharedViewModel.isLightTheme()
            themeSwitchTV.text = getString(if (sharedViewModel.isLightTheme()) R.string.light_theme else R.string.dark_theme)

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
            anonymousInc.root.setOnClickListener {
                navController.navigate(R.id.action_global_authFragment)
            }

            themeSwitch.setOnCheckedChangeListener { _, isChecked ->
                sharedViewModel.toggleTheme()

                themeSwitchTV.text = getString(if (isChecked) R.string.dark_theme else R.string.light_theme)
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

            privacyButton.setOnClickListener {
                navController.navigate(SettingsFragmentDirections.actionNavigationSettingsToPolicyFragment())
            }

            termsButton.setOnClickListener {
                navController.navigate(SettingsFragmentDirections.actionNavigationSettingsToPolicyFragment(false))
            }

            deleteAccountButton.setSafeOnClickListener {
                context?.showConfirmationDialog(getString(R.string.delete_user_info)) {
                    context?.showConfirmationDialog(getString(R.string.do_you_want_to_delete)) {
                        if (deleteUserLiveData != null && deleteUserLiveData?.hasActiveObservers() == true)
                            deleteUserLiveData?.removeObservers(viewLifecycleOwner)

                        deleteUserLiveData = settingsViewModel.deleteUser()

                        deleteUserLiveData?.observe(viewLifecycleOwner) { response ->
                            when(response) {
                                is NetworkResponse.Failure -> {
                                    if (::dialog.isInitialized)
                                        dialog.dismissDialog()

                                    context?.showErrorDialog(response.errorMessage)
                                }
                                NetworkResponse.Loading -> {
                                    if (::dialog.isInitialized)
                                        dialog.showLoadingDialog()
                                }
                                is NetworkResponse.Success -> {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        tokenManager.deleteToken()
                                        GoogleSignIn.getClient(it.context, GoogleSignInOptions.DEFAULT_SIGN_IN)
                                            .signOut()
                                        withContext(Dispatchers.Main) {
                                            sharedViewModel.setAuthentication(false)
                                            navController.popBackStack(R.id.navigation_home, false)
                                        }
                                    }

                                    if (::dialog.isInitialized)
                                        dialog.dismissDialog()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        deleteUserLiveData?.removeObservers(viewLifecycleOwner)
        super.onDestroyView()
    }
}