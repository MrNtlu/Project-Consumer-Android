package com.mrntlu.projectconsumer

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.window.layout.WindowMetricsCalculator
import com.mrntlu.projectconsumer.databinding.ActivityMainBinding
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.viewmodels.shared.ActivitySharedViewModel
import dagger.hilt.android.AndroidEntryPoint

enum class WindowSizeClass { COMPACT, MEDIUM, EXPANDED }

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by viewModels()
    private val sharedViewModel: ActivitySharedViewModel by viewModels()

    private val navController: NavController by lazy {
        findNavController(R.id.nav_host_fragment_activity_main)
    }

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences(Constants.THEME_PREF, 0)
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        //TODO Implement floating bottom app bar with hide/show function on scroll
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_movie, R.id.navigation_tv, R.id.navigation_anime, R.id.navigation_game
            )
        )

        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        setObservers()
        setToolbar()

        //TODO
        // Status bar when white texts are not visible
        // Find font
        // Setup firebase analytics and crashlytics
        // Toggle BottomAppBar on Scroll
        // Custom BottomAppBar
        // Custom Toolbar/ActionBar
        // Toolbar Settings Left & Profile on Right
        // On Pressed hide BottomAppBar

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

        val heightDp = metrics.bounds.height() /
                resources.displayMetrics.density
        val heightWindowSizeClass = when {
            heightDp < 480f -> WindowSizeClass.COMPACT
            heightDp < 900f -> WindowSizeClass.MEDIUM
            else -> WindowSizeClass.EXPANDED
        }

        sharedViewModel.setWindowSize(widthWindowSizeClass, heightWindowSizeClass, heightDp)
    }

    private fun setToolbar() {
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.anonymousInc.root.setOnClickListener {
            printLog("Clicked")
        }
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        viewModel.setThemeCode(prefs.getInt(Constants.THEME_PREF, Constants.LIGHT_THEME))

        AppCompatDelegate.setDefaultNightMode(if (viewModel.isLightTheme()) MODE_NIGHT_NO else MODE_NIGHT_YES)

        return super.onCreateView(name, context, attrs)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.settingsMenu -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setObservers() {
        //TODO Implement dark theme and get from the system
        viewModel.themeCode.observe(this) {
            AppCompatDelegate.setDefaultNightMode(if (it == Constants.LIGHT_THEME) MODE_NIGHT_NO else MODE_NIGHT_YES)
            setThemePref(it)
        }
    }

    private fun setThemePref(value: Int){
        val editor = prefs.edit()
        editor.putInt(Constants.THEME_PREF, value)
        editor.apply()
    }

    override fun onDestroy() {
        if (viewModel.themeCode.hasObservers())
            viewModel.themeCode.removeObservers(this)
        super.onDestroy()
    }
}