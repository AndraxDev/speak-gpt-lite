/**************************************************************************
 * Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **************************************************************************/

package org.teslasoft.assistant.ui.activities

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.navigation.NavigationBarView
import org.teslasoft.assistant.lite.R
import org.teslasoft.assistant.preferences.ApiEndpointPreferences
import org.teslasoft.assistant.preferences.GlobalPreferences
import org.teslasoft.assistant.preferences.Preferences
import org.teslasoft.assistant.theme.ThemeManager
import org.teslasoft.assistant.ui.fragments.tabs.ChatsListFragment
import org.teslasoft.assistant.ui.fragments.tabs.PlaygroundFragment
import org.teslasoft.assistant.ui.fragments.tabs.ToolsFragment
import org.teslasoft.assistant.ui.onboarding.WelcomeActivity
import org.teslasoft.assistant.util.WindowInsetsUtil
import java.util.EnumSet
import androidx.core.graphics.drawable.toDrawable
import org.teslasoft.assistant.preferences.EncryptedPreferences

class MainActivity : FragmentActivity(), Preferences.PreferencesChangedListener {

    private var navigationBar: BottomNavigationView? = null
    private var fragmentContainer: ConstraintLayout? = null
    private var threadLoader: LinearLayout? = null
    private var frameChats: Fragment? = null
    private var framePlayground: Fragment? = null
    private var frameTools: Fragment? = null
    private var root: ConstraintLayout? = null
    private var preferences: Preferences? = null
    private var btnDebugActivity: MaterialButton? = null
    private var needsRestart: Boolean = false
    private var selectedTab: Int = 1
    private var isInitialized: Boolean = false
    private var splashScreen: SplashScreen? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= 30) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            )
        }

        super.onCreate(savedInstanceState)

        splashScreen = installSplashScreen()
        splashScreen?.setKeepOnScreenCondition { true }

        val consent: SharedPreferences = getSharedPreferences("consent", MODE_PRIVATE)

        if (!consent.getBoolean("consent", false)) {
            startActivity(Intent(this, DataSafety::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        preferences = Preferences.getPreferences(this, "").addOnPreferencesChangedListener(this)

        navigationBar = findViewById(R.id.navigation_bar)

        fragmentContainer = findViewById(R.id.fragment)
        root = findViewById(R.id.root)
        btnDebugActivity = findViewById(R.id.btn_debug_activity)
        threadLoader = findViewById(R.id.thread_loader)

        threadLoader?.visibility = View.VISIBLE

        preloadAmoled()

        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle(R.string.label_confirm_exit)
                    .setMessage(R.string.msg_confirm_exit)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        finish()
                    }
                    .setNegativeButton(R.string.no) { _, _ -> }
                    .show()
            }
        } else {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle(R.string.label_confirm_exit)
                        .setMessage(R.string.msg_confirm_exit)
                        .setPositiveButton(R.string.yes) { _, _ ->
                            finish()
                        }
                        .setNegativeButton(R.string.no) { _, _ -> }
                        .show()
                }
            })
        }

        Thread {
            runOnUiThread {
                navigationBar!!.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.menu_chat -> {
                            menuChats()
                            return@OnItemSelectedListener true
                        }
                        R.id.menu_playground -> {
                            menuPlayground()
                            return@OnItemSelectedListener true
                        }
                        R.id.menu_tools -> {
                            menuTools()
                            return@OnItemSelectedListener true
                        }
                    }

                    return@OnItemSelectedListener false
                })

                preInit()

                if (savedInstanceState != null) {
                    adjustPaddings()
                    onRestoredState(savedInstanceState)
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    val fadeOut: Animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                    threadLoader?.startAnimation(fadeOut)

                    fadeOut.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) { /* UNUSED */ }
                        override fun onAnimationEnd(animation: Animation) {
                            runOnUiThread {
                                threadLoader?.visibility = View.GONE
                                threadLoader?.elevation = 0.0f

                                isInitialized = true
                            }
                        }

                        override fun onAnimationRepeat(animation: Animation) { /* UNUSED */ }
                    })
                }, 50)
            }
        }.start()
    }

    private fun preInit() {
        val apiEndpointPreferences = ApiEndpointPreferences.getApiEndpointPreferences(this)

        if (apiEndpointPreferences.getApiEndpoint(this, preferences!!.getApiEndpointId()).apiKey == "") {
            if (preferences!!.getApiKey(this) == "") {
                if (preferences!!.getOldApiKey() == "") {
                    startActivity(Intent(this, WelcomeActivity::class.java).setAction(Intent.ACTION_VIEW))
                    EncryptedPreferences.setEncryptedPreference(this, "chat_list", "data", "[]")
                    finish()
                } else {
                    preferences!!.secureApiKey(this)
                    apiEndpointPreferences.migrateFromLegacyEndpoint(this)
                    initUI()
                }
            } else {
                apiEndpointPreferences.migrateFromLegacyEndpoint(this)
                initUI()
            }
        } else {
            initUI()
        }
    }

    private fun initUI() {
        frameChats = ChatsListFragment()
        framePlayground = PlaygroundFragment()
        frameTools = ToolsFragment()

        loadFragment(frameChats, 1, 1)
        reloadAmoled()
        splashScreen?.setKeepOnScreenCondition { false }
    }

    private fun restartActivity() {
        recreate()
    }

    override fun onResume() {
        if (needsRestart) {
            restartActivity()
        }

        super.onResume()

        if (isInitialized) {
            // Reset preferences singleton to global settings
            preferences = Preferences.getPreferences(this, "")

            reloadAmoled()
        }
    }

    @Suppress("DEPRECATION")
    private fun reloadAmoled() {
        if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack()!!) {
            if (Build.VERSION.SDK_INT < 30) {
                window.navigationBarColor = ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme)
                window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
            }
            window.setBackgroundDrawableResource(R.color.amoled_window_background)
            navigationBar!!.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme))
        } else {
            if (Build.VERSION.SDK_INT < 30) {
                window.navigationBarColor = SurfaceColors.SURFACE_3.getColor(this)
                window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)
            }
            val colorDrawable = SurfaceColors.SURFACE_0.getColor(this).toDrawable()
            window.setBackgroundDrawable(colorDrawable)
            navigationBar!!.setBackgroundColor(SurfaceColors.SURFACE_3.getColor(this))
        }

        (frameChats as ChatsListFragment).reloadAmoled(this)
    }

    @Suppress("DEPRECATION")
    private fun preloadAmoled() {
        ThemeManager.getThemeManager().applyTheme(this, isDarkThemeEnabled() && GlobalPreferences.getPreferences(this).getAmoledPitchBlack())
        if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack()!!) {
            if (Build.VERSION.SDK_INT < 30) {
                window.navigationBarColor = SurfaceColors.SURFACE_0.getColor(this)
                window.statusBarColor = ResourcesCompat.getColor(resources, R.color.amoled_window_background, theme)
            }
            threadLoader?.background = ResourcesCompat.getDrawable(resources, R.color.amoled_window_background, null)
        } else {
            if (Build.VERSION.SDK_INT < 30) {
                window.navigationBarColor = SurfaceColors.SURFACE_3.getColor(this)
                window.statusBarColor = SurfaceColors.SURFACE_0.getColor(this)
            }
            threadLoader?.setBackgroundColor(SurfaceColors.SURFACE_0.getColor(this))
        }
    }

    private fun getDisabledDrawable(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getDisabledColor())
        return drawable
    }

    private fun getDisabledColor() : Int {
        return if (isDarkThemeEnabled() && preferences?.getAmoledPitchBlack()!!) {
            ResourcesCompat.getColor(resources, R.color.amoled_accent_100, theme)
        } else {
            SurfaceColors.SURFACE_5.getColor(this)
        }
    }

    private fun isDarkThemeEnabled(): Boolean {
        return when (resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("tab", selectedTab)
        super.onSaveInstanceState(outState)
    }

    private fun menuChats() {
        val st = selectedTab
        selectedTab = 1
        loadFragment(frameChats, st, selectedTab)
    }

    private fun menuPlayground() {
        val st = selectedTab
        selectedTab = 2
        loadFragment(framePlayground, st, selectedTab)
    }

    private fun menuTools() {
        val st = selectedTab
        selectedTab = 3
        loadFragment(frameTools, st, selectedTab)
    }

    private fun onRestoredState(savedInstanceState: Bundle?) {
        selectedTab = savedInstanceState!!.getInt("tab")

        when (selectedTab) {
            1 -> {
                navigationBar?.selectedItemId = R.id.menu_chat
                loadFragment(frameChats, 1, 1)
            }
            2 -> {
                navigationBar?.selectedItemId = R.id.menu_playground
                loadFragment(framePlayground, 1, 1)
            }
            3 -> {
                navigationBar?.selectedItemId = R.id.menu_tools
                loadFragment(frameTools, 1, 1)
            }
        }
    }

    override fun onPreferencesChanged(key: String, value: String) {
        if (key == "debug_mode" || key == "amoled_pitch_black" || key == "hide_model_names" || key == "monochrome_background_for_chat_list") {
            needsRestart = true
        }
    }

    private fun loadFragment(fragment: Fragment?, newTab: Int, prevTab: Int): Boolean {
        if (fragment != null) {
            try {
                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                if (newTab < prevTab) {
                    transaction.setCustomAnimations(R.anim.mtrl_fragment_open_enter, R.anim.mtrl_fragment_open_exit)
                } else if (newTab > prevTab) {
                    transaction.setCustomAnimations(R.anim.mtrl_fragment_close_enter, R.anim.mtrl_fragment_close_exit)
                } else {
                    transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                }
                transaction.replace(R.id.fragment, fragment)
                transaction.commit()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
        return false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        adjustPaddings()
    }

    private fun adjustPaddings() {
        WindowInsetsUtil.adjustPaddings(this, R.id.root, EnumSet.of(WindowInsetsUtil.Companion.Flags.STATUS_BAR, WindowInsetsUtil.Companion.Flags.IGNORE_PADDINGS))
        WindowInsetsUtil.adjustPaddings(this, R.id.navigation_bar, EnumSet.of(WindowInsetsUtil.Companion.Flags.STATUS_BAR, WindowInsetsUtil.Companion.Flags.IGNORE_PADDINGS))
    }
}
