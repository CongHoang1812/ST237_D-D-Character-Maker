package com.fantasy.oc.maker.customize

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import com.fantasy.oc.maker.customize.core.common.AppLanguage
import com.fantasy.oc.maker.customize.core.ui.applyNavigationBarMode
import com.fantasy.oc.maker.customize.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var uiLanguage: AppLanguage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        uiLanguage = AppLanguage.from(resources.configuration)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Nội dung vẽ dưới status bar trong suốt: chỉ chừa inset trái/phải/dưới.
        // Màn nào cần tránh status bar thì tự xử lý top inset.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(left = systemBars.left, right = systemBars.right, bottom = systemBars.bottom)
            insets
        }
        window.applyNavigationBarMode()
    }

    /**
     * Manifest khai báo configChanges locale để đổi ngôn ngữ trong app không relaunch (gây đen màn).
     * Đổi từ ngoài app (cài đặt ngôn ngữ của hệ thống) thì màn đang mở vẫn giữ chữ cũ, nên recreate
     * lúc app chưa hiện lại. Trong app thì màn Language tự điều hướng sang màn inflate mới.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val language = AppLanguage.from(newConfig)
        if (language == uiLanguage) return
        uiLanguage = language
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) recreate()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Đóng dialog / bàn phím / quay lại app có thể làm thanh điều hướng hiện lại
        if (hasFocus) window.applyNavigationBarMode()
    }
}
