package com.example.androiddevhelper.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.androiddevhelper.R
import com.example.androiddevhelper.injection.App
import com.example.androiddevhelper.injection.injectViewModel

class SettingsActivity : AppCompatActivity() {

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
        private var vibrate: SwitchPreferenceCompat? = findPreference("vibrate")
        private var sound: SwitchPreferenceCompat? = findPreference("sound")
        private val settingsVm by injectViewModel { App.applicationComponent.settingsViewModel }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            //Will enable/disable the other preference on click so both is never active at the same time
            vibrate?.setOnPreferenceClickListener {
                sound?.isEnabled = !settingsVm.sharedPrefs.vibrate()
                true
            }
            sound?.setOnPreferenceClickListener {
                vibrate?.isEnabled = !settingsVm.sharedPrefs.sound()
                true
            }
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            listView.setBackgroundColor(Color.parseColor("#585A57"))
        }
    }
}