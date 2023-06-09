package com.mrntlu.projectconsumer

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.window.layout.WindowMetricsCalculator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.mrntlu.projectconsumer.databinding.ActivityMainBinding
import com.mrntlu.projectconsumer.interfaces.ConnectivityObserver
import com.mrntlu.projectconsumer.service.TokenManager
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.MessageBoxType
import com.mrntlu.projectconsumer.utils.NetworkConnectivityObserver
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.loadWithGlide
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

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val navController: NavController by lazy {
        findNavController(R.id.nav_host_fragment_activity_main)
    }

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences(Constants.THEME_PREF, 0)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        //Granted or not
    }

    private var isUserInfoFailed = false
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAnalytics = Firebase.analytics

        val navView: BottomNavigationView = binding.navView


        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        setAppBarConfiguration()
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

        computeWindowSizeClasses()
    }

    private fun setAppBarConfiguration() {
        val appBarConfiguration = AppBarConfiguration(
            if (sharedViewModel.isLoggedIn())
                setOf(
                    R.id.navigation_home, R.id.navigation_discover,
                    R.id.navigation_profile, R.id.navigation_later,
                )
            else
                setOf(
                    R.id.navigation_home, R.id.navigation_discover, R.id.navigation_settings
                )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun setListeners() {
        binding.apply {
            messageBoxButton.setOnClickListener {
                binding.messageBoxLayout.setGone()
            }

            anonymousInc.root.setOnClickListener {
                navController.navigate(R.id.action_global_authFragment)
            }

            userInc.root.setOnClickListener {
                navController.navigate(R.id.action_global_navigation_profile)
            }
        }

        navController.addOnDestinationChangedListener { _, destination, args ->
            invalidateOptionsMenu()

            when(destination.id) {
                R.id.navigation_home, R.id.navigation_discover -> {
                    binding.toolbar.setVisible()
                    binding.navView.setVisible()
                    handleUserIncVisibility(false)
                }
                R.id.navigation_settings -> {
                    binding.toolbar.setVisible()
                    binding.navView.setVisibilityByCondition(sharedViewModel.isLoggedIn())
                    handleUserIncVisibility(true)
                }
                R.id.navigation_profile, R.id.navigation_later -> {
                    binding.toolbar.setVisible()
                    binding.navView.setVisible()
                    handleUserIncVisibility(true)
                }
                R.id.movieDetailsFragment, R.id.tvDetailsFragment -> {
                    binding.toolbar.setGone()
                    binding.navView.setGone()
                    handleUserIncVisibility(true)
                }
                else -> {
                    binding.toolbar.setVisible()
                    binding.navView.setGone()
                    handleUserIncVisibility(true)
                }
            }

            binding.toolbar.title = when(destination.id) {
                R.id.movieListFragment -> {
                    if (args?.getString("fetchType") == FetchType.UPCOMING.tag)
                        "Upcoming Movies"
                    else if (args?.getString("fetchType") == FetchType.TOP.tag)
                        "Top Movies"
                    else ""
                }
                R.id.tvListFragment -> {
                    if (args?.getString("fetchType") == FetchType.UPCOMING.tag)
                        "Upcoming TV Series"
                    else if (args?.getString("fetchType") == FetchType.TOP.tag)
                        "Top TV Series"
                    else ""
                }
                R.id.navigation_settings -> "Settings"
                R.id.discoverListFragment -> "Discover"
                else -> ""
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
        askNotificationPermission()
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        sharedViewModel.setCountryCode(prefs.getString(Constants.COUNTRY_PREF, Locale.getDefault().country.uppercase()))
        sharedViewModel.setLanguageCode(prefs.getString(Constants.LAN_PREF, Locale.getDefault().language.uppercase()))
        sharedViewModel.setThemeCode(prefs.getInt(Constants.THEME_PREF, Constants.LIGHT_THEME))

        AppCompatDelegate.setDefaultNightMode(if (sharedViewModel.isLightTheme()) MODE_NIGHT_NO else MODE_NIGHT_YES)

        return super.onCreateView(name, context, attrs)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val currentItem = navController.currentDestination?.id

        menu?.findItem(R.id.settingsMenu)?.isVisible = !(
                currentItem != R.id.navigation_home &&
                currentItem != R.id.navigation_discover &&
                currentItem != R.id.navigation_profile &&
                currentItem != R.id.navigation_later) &&
                sharedViewModel.isLoggedIn()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.settingsMenu -> {
                navController.navigate(R.id.action_global_settingsFragment)
            }
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
                        if (sharedViewModel.isLoggedIn() && isUserInfoFailed && userSharedViewModel.userInfo == null)
                            userSharedViewModel.getBasicInfo()

                        sharedViewModel.setNetworkStatus(true)

                        sharedViewModel.setMessageBox(Triple(MessageBoxType.NOTHING, null, null))
                    }
                }
            }
        }

        userSharedViewModel.userInfoResponse.observe(this) { response ->
            binding.apply {
                val currentItem = navController.currentDestination?.id
                val shouldShow = currentItem == R.id.navigation_home ||
                        currentItem == R.id.navigation_discover

                userLoadingProgressBar.setVisibilityByCondition(!(response == NetworkResponse.Loading && shouldShow))
                userInc.root.setVisibilityByCondition(!(response is NetworkResponse.Success && shouldShow))
            }

            isUserInfoFailed = response is NetworkResponse.Failure

            if (response is NetworkResponse.Success) {
                userSharedViewModel.userInfo = response.data.data

                binding.userInc.userIV.loadWithGlide(
                    response.data.data.image ?: "",
                    binding.userInc.userPlaceHolderIV,
                    binding.userInc.userIVProgressBar,
                ) {
                    centerCrop()
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
            setAppBarConfiguration()

            if (!it) {
                binding.userInc.root.setGone()
                binding.userLoadingProgressBar.setGone()
            }
            binding.anonymousInc.root.setVisibilityByCondition(it)

            if (it) {
                if (userSharedViewModel.userInfo == null) {
                    userSharedViewModel.getBasicInfo()
                }
            } else {
                userSharedViewModel.userInfo = null
            }
        }

        sharedViewModel.countryCode.observe(this) {
            setCodePref(Constants.COUNTRY_PREF, it)
        }

        sharedViewModel.languageCode.observe(this) {
            setCodePref(Constants.LAN_PREF, it)
        }

        sharedViewModel.themeCode.observe(this) {
            AppCompatDelegate.setDefaultNightMode(if (it == Constants.LIGHT_THEME) MODE_NIGHT_NO else MODE_NIGHT_YES)
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
            } else if (shouldShowRequestPermissionRationale(POST_NOTIFICATIONS)) {

                //TODO If no, save via pref and don't show again!
                showNotificationInfoDialog(getString(R.string.notification_permission_info)) {
                    requestPermissionLauncher.launch(POST_NOTIFICATIONS)
                }
            } else {
                requestPermissionLauncher.launch(POST_NOTIFICATIONS)
            }
        }
    }

    private fun handleUserIncVisibility(shouldHide: Boolean) {
        binding.apply {
            if (sharedViewModel.isLoggedIn()) {
                anonymousInc.root.setGone()
                userLoadingProgressBar.setGone()

                if (userSharedViewModel.userInfo != null || sharedViewModel.isNetworkAvailable())
                    userInc.root.setVisibilityByCondition(shouldHide)
                else
                    userInc.root.setGone()
            } else {
                anonymousInc.root.setVisibilityByCondition(shouldHide)
                userInc.root.setGone()
                userLoadingProgressBar.setGone()
            }
        }
    }

    private fun setThemePref(value: Int){
        val editor = prefs.edit()
        editor.putInt(Constants.THEME_PREF, value)
        editor.apply()
    }

    private fun setCodePref(key: String, value: String){
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    override fun onDestroy() {
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