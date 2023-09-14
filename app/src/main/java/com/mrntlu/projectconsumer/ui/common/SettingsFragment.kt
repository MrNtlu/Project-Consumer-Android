package com.mrntlu.projectconsumer.ui.common

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.play.core.review.ReviewManagerFactory
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
import com.mrntlu.projectconsumer.utils.showInfoDialog
import com.mrntlu.projectconsumer.viewmodels.main.common.SettingsViewModel
import com.mrntlu.projectconsumer.viewmodels.shared.UserSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val userSharedViewModel: UserSharedViewModel by activityViewModels()

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
                    setNavigationContentDescription(R.string.back_cd)

                    setNavigationOnClickListener { navController.popBackStack() }
                }
            }
        }
    }

    private fun setUI() {
        binding.apply {
            accountInfoTitleTV.setVisibilityByCondition(!sharedViewModel.isLoggedIn())
            accountInfoSettingsCard.setVisibilityByCondition(!sharedViewModel.isLoggedIn())
            deleteAccountButton.setVisibilityByCondition(!sharedViewModel.isLoggedIn())

            accountFirstClickTile.root.setGone()
            accountFirstTileDivider.setGone()
            accountSwitchTileDivider.setGone()
            accountSwitchTile.setGone()

            if (sharedViewModel.isLoggedIn()) {
                userSharedViewModel.userInfo?.apply {
                    accountInfoFirstTile.settingsInfoTitleTV.text = getString(R.string.email)
                    accountInfoFirstTile.settingsInfoTV.text = email

                    accountInfoSecondTile.settingsInfoTitleTV.text = getString(R.string.username)
                    accountInfoSecondTile.settingsInfoTV.text = username

                    accountInfoThirdTile.settingsInfoTitleTV.text = getString(R.string.membership)
                    accountInfoThirdTile.settingsInfoTV.text = when(membershipType) {
                        1 -> "Premium"
                        2 -> "Premium Supporter"
                        else -> "Basic"
                    }
                }

                accountSecondClickTile.apply {
                    settingsClickTileTV.text = getString(R.string.log_out)
                    settingsClickTileTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_logout_24, 0)
                    settingsTileIV.setGone()
                    arrowIV.setGone()

                    root.setSafeOnClickListener {
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
            } else {
                accountSecondClickTile.apply {
                    settingsClickTileTV.text = getString(R.string.sign_in)
                    settingsClickTileTV.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_login_24, 0)
                    arrowIV.setGone()
                    settingsTileIV.setGone()

                    root.setSafeOnClickListener(interval = 750) {
                        if (navController.currentDestination?.id == R.id.navigation_settings) {
                            navController.navigate(R.id.action_global_authFragment)
                        }
                    }
                }
            }

            //Application settings
            applicationChangeTabClickTile.apply {
                settingsClickTileTV.text = getString(R.string.change_tab_design)
                settingsTileIV.setImageResource(R.drawable.ic_tab_24)

                root.setSafeOnClickListener {
                    activity?.let {
                        val bottomSheet = ChangeTabLayoutBottomSheet()
                        bottomSheet.show(it.supportFragmentManager, ChangeTabLayoutBottomSheet.TAG)
                    }
                }
            }

            applicationChangeLayoutClickTile.apply {
                settingsClickTileTV.text = getString(R.string.change_layout)
                settingsTileIV.setImageResource(R.drawable.ic_grid)

                root.setSafeOnClickListener {
                    activity?.let {
                        val bottomSheet = ChangeLayoutBottomSheet()
                        bottomSheet.show(it.supportFragmentManager, ChangeLayoutBottomSheet.TAG)
                    }
                }
            }

            applicationFirstClickTile.apply {
                settingsClickTileTV.text = getString(R.string.clear_image_cache)
                settingsTileIV.setImageResource(R.drawable.ic_delete)

                root.setSafeOnClickListener {
                    context?.showConfirmationDialog(getString(R.string.do_you_want_clear_image_cache)) {
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            Glide.get(root.context).clearDiskCache()

                            withContext(Dispatchers.Main) {
                                context?.showInfoDialog(getString(R.string.caches_are_deleted))
                            }
                        }
                    }
                }
            }

            val manager = ReviewManagerFactory.create(root.context)
            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    othersFirstClickTile.apply {

                        settingsClickTileTV.text = getString(R.string.rate_review)
                        settingsTileIV.setImageResource(R.drawable.ic_rate)

                        root.setSafeOnClickListener {
                            activity?.let { activity ->
                                manager.launchReviewFlow(activity, task.result)
                            }
                        }
                    }
                } else {
                    othersFirstClickTile.root.setGone()
                    othersFirstDivider.setGone()
                }
            }

            othersSecondClickTile.apply {
                settingsClickTileTV.text = getString(R.string.feedback_suggestions)
                settingsTileIV.setImageResource(R.drawable.ic_feedback)

                root.setSafeOnClickListener {
                    try {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "message/rfc822"
                        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("mrntlu@gmail.com"))
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(root.context, "No mail app found", Toast.LENGTH_SHORT).show()
                    } catch (t: Throwable) {
                        Toast.makeText(root.context, "$t", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            othersThirdClickTile.apply {
                settingsClickTileTV.text = "Follow Us"
                settingsTileIV.setImageResource(R.drawable.ic_x)

                root.setSafeOnClickListener {
                    val urlIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://twitter.com/watchlistfy")
                    )
                    startActivity(urlIntent)
                }
            }

            themeSwitch.isChecked = !sharedViewModel.isLightTheme()
            themeSwitchTV.text = getString(if (sharedViewModel.isLightTheme()) R.string.light_theme else R.string.dark_theme)

            val countryArray = countryList.map { it.first }.toTypedArray()
            (settingsSpinner.editText as? MaterialAutoCompleteTextView)?.setSimpleItems(countryArray)
            setACTVSelection(binding.settingsSelectionACTV, countryList.indexOfFirst {
                it.second == sharedViewModel.getCountryCode()
            })

            val languageArray = languageList.map { it.first }.toTypedArray()
            (applicationSecondSpinner.editText as? MaterialAutoCompleteTextView)?.setSimpleItems(languageArray)
            setACTVSelection(binding.settingsSecondSelectionACTV, languageList.indexOfFirst {
                it.second == sharedViewModel.getLanguageCode()
            })
        }
    }

    private fun setACTVSelection(actv: AutoCompleteTextView, position: Int) {
        actv.apply {
            if (position >= 0)
                setText(adapter.getItem(position).toString(), false)
            dismissDropDown()
        }
    }

    private fun setListeners() {
        binding.apply {
            settingsSelectionACTV.setOnDismissListener {
                if (settingsSelectionACTV.text.toString().run { isEmpty() || isBlank() })
                    setACTVSelection(settingsSelectionACTV, 0)
                else {
                    val selectedIndex = countryList.indexOfFirst {
                        it.first == settingsSelectionACTV.text.toString()
                    }

                    if (selectedIndex >= 0)
                        sharedViewModel.setCountryCode(countryList[selectedIndex].second)

                    settingsSelectionACTV.clearFocus()
                }
            }

            settingsSecondSelectionACTV.setOnDismissListener {
                if (settingsSecondSelectionACTV.text.toString().run { isEmpty() || isBlank() })
                    setACTVSelection(settingsSecondSelectionACTV, 0)
                else {
                    val selectedIndex = languageList.indexOfFirst {
                        it.first == settingsSecondSelectionACTV.text.toString()
                    }

                    if (selectedIndex >= 0)
                        sharedViewModel.setLanguageCode(languageList[selectedIndex].second)

                    settingsSecondSelectionACTV.clearFocus()
                }
            }

            themeSwitch.setOnCheckedChangeListener { _, isChecked ->
                sharedViewModel.toggleTheme()

                themeSwitchTV.text = getString(if (isChecked) R.string.dark_theme else R.string.light_theme)
            }

            privacyButton.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_settings) {
                    navController.navigate(SettingsFragmentDirections.actionNavigationSettingsToPolicyFragment())
                }
            }

            termsButton.setOnClickListener {
                if (navController.currentDestination?.id == R.id.navigation_settings) {
                    navController.navigate(SettingsFragmentDirections.actionNavigationSettingsToPolicyFragment(false))
                }
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