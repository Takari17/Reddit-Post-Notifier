package com.takari.redditpostnotifier.ui.settings

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import com.takari.redditpostnotifier.App
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.misc.injectViewModel
import com.takari.redditpostnotifier.ui.post.service.NewPostService
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.settings_activity.*

/*
Preference Settings was WAY too complicated and over engineered so I said fuck it and made my
own implementation.
 */
class SettingsActivity : AppCompatActivity() {

    companion object {
        const val API_REQUEST_RATE_KEY = "apiRequestRate"
    }

    private val viewModel: SettingsViewModel by injectViewModel { App.applicationComponent().settingsViewModel }
    private val apiRequestRateDialog = ApiRequestRateDialog()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.navigationBarColor = Color.parseColor("#171A23")

        val initialApiRequestRate = viewModel.getIntFromSharedPrefs(API_REQUEST_RATE_KEY, 1)

        displayedApiRequestRateTextView.text = "Every $initialApiRequestRate min"

        cardViewBackground.setOnClickListener {
            if (!apiRequestRateDialog.isAdded)
                apiRequestRateDialog.show(supportFragmentManager, "ApiRequestRateDialog")
        }

        apiRequestRateDialog.onItemSelected = { apiRequestRateInMinutes ->
            displayedApiRequestRateTextView.text = "Every $apiRequestRateInMinutes min"
            viewModel.saveIntToSharedPrefs(API_REQUEST_RATE_KEY, apiRequestRateInMinutes)

            if(NewPostService.isRunning())
                Toasty.warning(this, "Stop observing to take effect", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }

    class ApiRequestRateDialog : AppCompatDialogFragment() {

        var onItemSelected: ((Int) -> Unit)? = null

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =

            AlertDialog.Builder(activity).apply {

                setTitle("Select a Time")

                val options = arrayOf(
                    "Every 1 min",
                    "Every 2 min",
                    "Every 3 min",
                    "Every 4 min",
                    "Every 5 min"
                )

                setSingleChoiceItems(options, -1) { _: Any, index: Int ->
                    onItemSelected?.let { it(options[index].extractInts()) }
                    dismiss()
                }

                setNegativeButton("Cancel") { _, _ -> }

            }.create()

        private fun String.extractInts(): Int =
            Integer.valueOf(this.replace("[^0-9]".toRegex(), ""))
    }
}