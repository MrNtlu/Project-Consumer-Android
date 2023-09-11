package com.mrntlu.projectconsumer

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import androidx.window.layout.WindowMetricsCalculator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.mrntlu.projectconsumer.databinding.ActivityMainBinding
import com.mrntlu.projectconsumer.interfaces.ConnectivityObserver
import com.mrntlu.projectconsumer.service.TokenManager
import com.mrntlu.projectconsumer.service.notification.FirebaseMessagingService.Companion.DATA_EXTRA
import com.mrntlu.projectconsumer.service.notification.FirebaseMessagingService.Companion.DEEPLINK_EXTRA
import com.mrntlu.projectconsumer.service.notification.FirebaseMessagingService.Companion.PATH_EXTRA
import com.mrntlu.projectconsumer.ui.anime.AnimeDetailsFragmentDirections
import com.mrntlu.projectconsumer.ui.common.BoardingBottomSheet
import com.mrntlu.projectconsumer.ui.game.GameDetailsFragmentDirections
import com.mrntlu.projectconsumer.ui.movie.MovieDetailsFragmentDirections
import com.mrntlu.projectconsumer.ui.tv.TVSeriesDetailsFragmentDirections
import com.mrntlu.projectconsumer.utils.Constants.BOARDING_PREF
import com.mrntlu.projectconsumer.utils.Constants.COUNTRY_PREF
import com.mrntlu.projectconsumer.utils.Constants.DARK_THEME
import com.mrntlu.projectconsumer.utils.Constants.LAN_PREF
import com.mrntlu.projectconsumer.utils.Constants.LAYOUT_PREF
import com.mrntlu.projectconsumer.utils.Constants.LIGHT_THEME
import com.mrntlu.projectconsumer.utils.Constants.NOTIFICATION_PREF
import com.mrntlu.projectconsumer.utils.Constants.PREF_NAME
import com.mrntlu.projectconsumer.utils.Constants.THEME_PREF
import com.mrntlu.projectconsumer.utils.MessageBoxType
import com.mrntlu.projectconsumer.utils.NetworkConnectivityObserver
import com.mrntlu.projectconsumer.utils.getColorFromAttr
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showNotificationInfoDialog
import com.mrntlu.projectconsumer.viewmodels.shared.ActivitySharedViewModel
import com.mrntlu.projectconsumer.viewmodels.shared.UserSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

enum class WindowSizeClass { COMPACT, MEDIUM, EXPANDED }

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var connectivityObserver: NetworkConnectivityObserver
    @Inject lateinit var tokenManager: TokenManager

    private val sharedViewModel: ActivitySharedViewModel by viewModels()
    private val userSharedViewModel: UserSharedViewModel by viewModels()

    private var notificationDialog: AlertDialog? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val navController: NavController by lazy {
        findNavController(R.id.nav_host_fragment_activity_main)
    }

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it)
            setNotificationPref()
    }

    private lateinit var binding: ActivityMainBinding

    fun navigateToDiscover() {
        binding.navView.selectedItemId = R.id.navigation_discover
    }

    fun navigateToProfile() {
        binding.navView.selectedItemId = R.id.navigation_profile
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (!navController.popBackStack())
                finish()
            else
                sharedViewModel.setShouldPreventBottomSelection(true)
            return
        }
        super.onBackPressed()
    }

    private fun handleIntentData(intent: Intent?) {
        val extras = intent?.extras
        if (extras != null) {
            val data = extras.getString(DATA_EXTRA)
            val path = extras.getString(PATH_EXTRA)
            val deepLink = extras.getString(DEEPLINK_EXTRA)

            try {
                if (deepLink != null) {
                    val deepLinkUri = Uri.parse(deepLink)

                    navController.navigate(deepLink = deepLinkUri)
                } else {
                    navController.handleDeepLink(intent)
                }
            } catch (_: Exception) {
                if (data != null && path != null) {
                    when (path) {
                        "movie" -> {
                            val navWithAction = MovieDetailsFragmentDirections.actionGlobalMovieDetailsFragment(data)
                            navController.navigate(navWithAction)
                        }

                        "tv" -> {
                            val navWithAction = TVSeriesDetailsFragmentDirections.actionGlobalTvDetailsFragment(data)
                            navController.navigate(navWithAction)
                        }

                        "anime" -> {
                            val navWithAction = AnimeDetailsFragmentDirections.actionGlobalAnimeDetailsFragment(data)
                            navController.navigate(navWithAction)
                        }

                        "game" -> {
                            val navWithAction = GameDetailsFragmentDirections.actionGlobalGameDetailsFragment(data)
                            navController.navigate(navWithAction)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        handleIntentData(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedViewModel.setCountryCode(prefs.getString(COUNTRY_PREF, Locale.getDefault().country.uppercase()))
        sharedViewModel.setLanguageCode(prefs.getString(LAN_PREF, Locale.getDefault().language.uppercase()))
        sharedViewModel.setLayoutSelection(prefs.getBoolean(LAYOUT_PREF, false))
        sharedViewModel.setIsDisplayed(prefs.getBoolean(BOARDING_PREF, false))
        sharedViewModel.setThemeCode(prefs.getInt(THEME_PREF, DARK_THEME))
        AppCompatDelegate.setDefaultNightMode(if (sharedViewModel.isLightTheme()) MODE_NIGHT_NO else MODE_NIGHT_YES)

        super.onCreate(savedInstanceState)
        installSplashScreen()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAnalytics = Firebase.analytics

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!navController.popBackStack())
                        finish()
                    else
                        sharedViewModel.setShouldPreventBottomSelection(true)
                }
            })
        }

        val navView: BottomNavigationView = binding.navView

        navView.setupWithNavController(navController)

        coroutineScope.launch {
            val token = tokenManager.getToken().first()

            withContext(Dispatchers.Main) {
                sharedViewModel.setAuthentication(token != null && token.isNotEmptyOrBlank())
            }
        }

        setListeners()
        setObservers()

        val container: ViewGroup = binding.container
        container.addView(object : View(this) {
            override fun onConfigurationChanged(newConfig: Configuration?) {
                super.onConfigurationChanged(newConfig)
                computeWindowSizeClasses()
            }
        })

        if (sharedViewModel.shouldDisplayBoarding()) {
            val boardingSheet = BoardingBottomSheet()
            boardingSheet.show(supportFragmentManager, BoardingBottomSheet.TAG)
        }

        computeWindowSizeClasses()

        handleIntentData(intent)
    }

    private fun setListeners() {
        binding.apply {
            messageBoxButton.setOnClickListener {
                binding.messageBoxLayout.setGone()
            }

            navView.setOnItemSelectedListener {
                if (!sharedViewModel.shouldPreventBottomSelection()) {
                    NavigationUI.onNavDestinationSelected(it, navController)
                    sharedViewModel.setShouldPreventBottomSelection(true)

                    return@setOnItemSelectedListener true
                }

                false
            }

            navView.setOnItemReselectedListener {}
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id) {
                R.id.navigation_home, R.id.navigation_discover, R.id.navigation_profile -> {
                    binding.navView.setVisible()
                    window.navigationBarColor = getColorFromAttr(R.attr.bottomNavBackgroundColor)
                }
                R.id.navigation_settings -> {
                    binding.navView.setVisibilityByCondition(sharedViewModel.isLoggedIn())
                    window.navigationBarColor = getColorFromAttr(if (sharedViewModel.isLoggedIn()) R.attr.mainBackgroundColor else R.attr.bottomNavBackgroundColor)
                }
                else -> {
                    binding.navView.setGone()
                    window.navigationBarColor = getColorFromAttr(R.attr.mainBackgroundColor)
                }
            }
        }
    }

    private fun computeWindowSizeClasses() {
        val metrics = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(this)

        val widthDp = metrics.bounds.width() /
                resources.displayMetrics.density
        val widthWindowSizeClass = when {
            widthDp < 600f -> WindowSizeClass.COMPACT
            widthDp < 840f -> WindowSizeClass.MEDIUM
            else -> WindowSizeClass.EXPANDED
        }

        sharedViewModel.setWindowSize(widthWindowSizeClass)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                navController.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setObservers() {
        lifecycleScope.launch {
            connectivityObserver.observe().collect { status ->
                when(status) {
                    ConnectivityObserver.Status.Unavailable -> {
                        sharedViewModel.setNetworkStatus(false)

                        sharedViewModel.setMessageBox(
                            Triple(
                                MessageBoxType.ERROR,
                                getString(R.string.no_internet_connection),
                                R.drawable.ic_no_internet_24
                            )
                        )
                    }

                    ConnectivityObserver.Status.Lost -> {
                        sharedViewModel.setNetworkStatus(false)

                        sharedViewModel.setMessageBox(
                            Triple(
                                MessageBoxType.ERROR,
                                getString(R.string.connection_lost_no_internet_connection),
                                R.drawable.ic_no_internet_24
                            )
                        )
                    }

                    ConnectivityObserver.Status.Available -> {
                        if (sharedViewModel.isLoggedIn() && userSharedViewModel.userInfo == null)
                            userSharedViewModel.getBasicInfo()

                        sharedViewModel.setNetworkStatus(true)

                        sharedViewModel.setMessageBox(Triple(MessageBoxType.NOTHING, null, null))
                    }
                }
            }
        }

        sharedViewModel.shouldPreventBottomSelection.observe(this) {
            if (it) {
                coroutineScope.launch {
                    delay(500)
                    withContext(Dispatchers.Main) {
                        sharedViewModel.setShouldPreventBottomSelection(false)
                    }
                }
            }
        }

        sharedViewModel.isAuthenticated.observe(this) {
            if (it && binding.navView.menu[2].itemId != R.id.navigation_profile) {
                binding.navView.menu.clear()
                binding.navView.inflateMenu(R.menu.bottom_nav_auth_menu)
            } else if (!it && binding.navView.menu[2].itemId != R.id.navigation_settings) {
                binding.navView.menu.clear()
                binding.navView.inflateMenu(R.menu.bottom_nav_menu)
            }

            if (it) {
                if (userSharedViewModel.userInfo == null)
                    userSharedViewModel.getBasicInfo()

                askNotificationPermission()
            } else {
                userSharedViewModel.userInfo = null
            }
        }

        sharedViewModel.isDisplayed.observe(this) {
            setBoardingPref()
        }

        sharedViewModel.layoutSelection.observe(this) { isAltLayout ->
            setLayoutSelectionPref(isAltLayout)
        }

        sharedViewModel.countryCode.observe(this) {
            setCodePref(COUNTRY_PREF, it)
        }

        sharedViewModel.languageCode.observe(this) {
            setCodePref(LAN_PREF, it)
        }

        sharedViewModel.themeCode.observe(this) {
            AppCompatDelegate.setDefaultNightMode(if (it == LIGHT_THEME) MODE_NIGHT_NO else MODE_NIGHT_YES)
            setThemePref(it)
        }

        sharedViewModel.globalMessageBox.observe(this) {
            if (it.first != MessageBoxType.NOTHING && it.second != null) {
                binding.messageBoxLayout.setVisible()
                binding.messageBoxTV.text = it.second
                if (it.third != null)
                    binding.messageBoxIcon.setBackgroundResource(it.third!!)
                else
                    binding.messageBoxIcon.setGone()
            } else {
                binding.messageBoxLayout.setGone()
            }
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                //Granted
            } else if (sharedViewModel.isLoggedIn()) {
                if (prefs.getBoolean(NOTIFICATION_PREF, true) && (notificationDialog == null || notificationDialog?.isShowing == false)) {
                    notificationDialog = showNotificationInfoDialog(getString(R.string.notification_permission_info), onPositive = {
                        requestPermissionLauncher.launch(POST_NOTIFICATIONS)
                    }) {
                        setNotificationPref()
                    }
                }
            }
        }
    }

    private fun setBoardingPref(){
        val editor = prefs.edit()
        editor.putBoolean(BOARDING_PREF, true)
        editor.apply()
    }

    private fun setThemePref(value: Int){
        val editor = prefs.edit()
        editor.putInt(THEME_PREF, value)
        editor.apply()
    }

    private fun setNotificationPref(){
        val editor = prefs.edit()
        editor.putBoolean(NOTIFICATION_PREF, false)
        editor.apply()
    }

    private fun setCodePref(key: String, value: String){
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun setLayoutSelectionPref(value: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(LAYOUT_PREF, value)
        editor.apply()
    }

    override fun onPause() {
        sharedViewModel.setShouldPreventBottomSelection(false)
        super.onPause()
    }

    override fun onDestroy() {
        notificationDialog?.dismiss()
        notificationDialog = null

        sharedViewModel.layoutSelection.removeObservers(this)
        sharedViewModel.shouldPreventBottomSelection.removeObservers(this)
        sharedViewModel.isAuthenticated.removeObservers(this)
        sharedViewModel.countryCode.removeObservers(this)
        sharedViewModel.languageCode.removeObservers(this)
        sharedViewModel.themeCode.removeObservers(this)
        sharedViewModel.globalMessageBox.removeObservers(this)
        sharedViewModel.networkStatus.removeObservers(this)
        userSharedViewModel.userInfoResponse.removeObservers(this)
        coroutineScope.cancel()

        super.onDestroy()
    }
}