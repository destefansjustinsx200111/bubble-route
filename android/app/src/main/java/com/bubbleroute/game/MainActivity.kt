package com.bubbleroute.game

import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.bubbleroute.game.core.di.ServiceLocator
import com.bubbleroute.game.core.navigation.Navigator
import com.bubbleroute.game.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var navigatorRef: Navigator? = null
    private var lastExitRequestAt = 0L
    private var exitToast: Toast? = null

    private val exitGuard = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val now = SystemClock.elapsedRealtime()
            val elapsed = now - lastExitRequestAt
            if (lastExitRequestAt > 0L && elapsed <= EXIT_CONFIRM_WINDOW_MS) {
                isEnabled = false
                exitToast?.cancel()
                exitToast = null
                onBackPressedDispatcher.onBackPressed()
                return
            }
            lastExitRequestAt = now
            exitToast?.cancel()
            val toast = Toast.makeText(
                this@MainActivity,
                R.string.exit_confirm,
                Toast.LENGTH_SHORT
            )
            exitToast = toast
            toast.show()
        }
    }

    private val backStackListener = FragmentManager.OnBackStackChangedListener {
        syncExitGuard()
    }

    val navigator: Navigator
        get() {
            val existing = navigatorRef
            if (existing != null) {
                return existing
            }
            val created = Navigator(supportFragmentManager, R.id.fragment_container)
            navigatorRef = created
            return created
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.init(applicationContext)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, exitGuard)
        supportFragmentManager.addOnBackStackChangedListener(backStackListener)
        syncExitGuard()
        if (savedInstanceState == null) {
            navigator.showSplash()
        }
    }

    private fun syncExitGuard() {
        exitGuard.isEnabled = supportFragmentManager.backStackEntryCount == 0
        if (exitGuard.isEnabled) {
            return
        }
        lastExitRequestAt = 0L
    }

    override fun onDestroy() {
        supportFragmentManager.removeOnBackStackChangedListener(backStackListener)
        exitToast?.cancel()
        exitToast = null
        navigatorRef = null
        super.onDestroy()
    }

    companion object {
        private const val EXIT_CONFIRM_WINDOW_MS = 2500L
    }
}
