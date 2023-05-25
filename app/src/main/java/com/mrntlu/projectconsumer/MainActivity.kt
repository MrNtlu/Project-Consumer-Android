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
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.FetchType
import com.mrntlu.projectconsumer.utils.MessageBoxType
import com.mrntlu.projectconsumer.utils.NetworkConnectivityObserver
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showInfoDialog
import com.mrntlu.projectconsumer.viewmodels.shared.ActivitySharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

enum class WindowSizeClass { COMPACT, MEDIUM, EXPANDED }

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var connectivityObserver: NetworkConnectivityObserver

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val sharedViewModel: ActivitySharedViewModel by viewModels()

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

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAnalytics = Firebase.analytics

        val navView: BottomNavigationView = binding.navView

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_movie, R.id.navigation_tv, R.id.navigation_anime, R.id.navigation_game
            )
        )

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        setListeners()
        setObservers()
        setToolbar()

        //TODO Login Flow
        // MainActivity should be the controller
        // SharedViewModel should keep isLoggedIn value and present necessary UI elements accordingly.
        // If logged in show different layout on toolbar if not show image and login button

        val container: ViewGroup = binding.container

        container.addView(object : View(this) {
            override fun onConfigurationChanged(newConfig: Configuration?) {
                super.onConfigurationChanged(newConfig)
                computeWindowSizeClasses()
            }
        })

        computeWindowSizeClasses()
    }

    private fun setListeners() {
        binding.messageBoxButton.setOnClickListener {
            binding.messageBoxLayout.setGone()
        }

        navController.addOnDestinationChangedListener { _, destination, args ->
            supportActionBar?.title = when(destination.id) {
                R.id.movieListFragment -> {
                    if (args?.getString("fetchType") == FetchType.UPCOMING.tag)
                        "Upcoming Movies"
                    else if (args?.getString("fetchType") == FetchType.POPULAR.tag)
                        "Popular Movies"
                    else ""
                }
                R.id.settingsFragment -> {
                    "Settings"
                }
                else -> ""
            }

            when(destination.id) {
                R.id.navigation_movie, R.id.navigation_tv, R.id.navigation_anime, R.id.navigation_game -> {
                    binding.toolbar.setVisible()
                    supportActionBar?.setDisplayShowTitleEnabled(false)
                    binding.navView.setVisible()
                    binding.anonymousInc.root.setVisibilityByCondition(sharedViewModel.isLoggedIn())
                }
                R.id.movieDetailsFragment -> {
                    binding.toolbar.setGone()
                    binding.navView.setGone()
                    binding.anonymousInc.root.setGone()
                }
                else -> {
                    binding.toolbar.setVisible()
                    supportActionBar?.setDisplayShowTitleEnabled(true)
                    binding.navView.setGone()
                    binding.anonymousInc.root.setGone()
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
        askNotificationPermission()
    }

    private fun setToolbar() {
        binding.anonymousInc.root.setOnClickListener {
            navController.navigate(R.id.action_global_authFragment)
        }
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
                        sharedViewModel.setNetworkStatus(true)

                        sharedViewModel.setMessageBox(Triple(MessageBoxType.NOTHING, null, null))
                    }
                }
            }
        }

        sharedViewModel.isAuthenticated.observe(this) {
            //TODO Change bottom nav
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
                showInfoDialog("Enable notifications to receive personalized recommendations, updates, and news about movies, tv shows, games and anime.") {
                    requestPermissionLauncher.launch(POST_NOTIFICATIONS)
                }
            } else {
                requestPermissionLauncher.launch(POST_NOTIFICATIONS)
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

        super.onDestroy()
    }
}