package com.example.androiddevhelper.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.androiddevhelper.App
import com.example.androiddevhelper.R
import com.example.androiddevhelper.utils.injectViewModel

class SettingsActivity : AppCompatActivity() {

    companion object {
        fun createIntent(context: Context) =
            Intent(context, SettingsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setTheme(R.style.PreferenceStyle)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        //Don't want to inject into view model since these are views
        private val vibrate: SwitchPreferenceCompat? = findPreference("vibrate")
        private val sound: SwitchPreferenceCompat? = findPreference("sound")
        private val viewModel by injectViewModel { App.applicationComponent.settingsViewModel }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            //When one preference is clicked the other gets disabled
            vibrate?.setOnPreferenceClickListener {
                sound?.isEnabled = !viewModel.sharedPrefs.getIsVibrateOn() // disabled
                true // enables the getIsVibrateOn preference
            }
            sound?.setOnPreferenceClickListener {
                vibrate?.isEnabled = !viewModel.sharedPrefs.getIsSoundOn() // disabled
                true
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            listView.setBackgroundColor(Color.parseColor("#585A57"))
        }
    }
}