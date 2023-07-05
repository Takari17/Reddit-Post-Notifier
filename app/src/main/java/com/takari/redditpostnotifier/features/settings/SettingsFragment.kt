package com.takari.redditpostnotifier.features.settings

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.takari.redditpostnotifier.R
import com.takari.redditpostnotifier.databinding.SettingsActivityBinding
import com.takari.redditpostnotifier.features.reddit.newPost.service.NewPostService
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    companion object {
        const val API_REQUEST_RATE_KEY = "apiRequestRate"
        const val TAG = "Settings Fragment"
    }

    private val viewModel: SettingsViewModel by viewModels()
    private val apiRequestRateDialog = ApiRequestRateDialog()
    private val binding by lazy { SettingsActivityBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Adds the back button to the action bar and changes it's name

        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar

        supportActionBar?.title = resources.getString(R.string.title_activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialApiRequestRate = viewModel.getIntFromSharedPrefs(
            API_REQUEST_RATE_KEY, 1
        )

        binding.displayedApiRequestRateTextView.text = "Every $initialApiRequestRate min"

        binding.cardViewBackground.setOnClickListener {
            if (!apiRequestRateDialog.isAdded)
                apiRequestRateDialog.show(
                    requireActivity().supportFragmentManager,
                    "ApiRequestRateDialog"
                )
        }

        apiRequestRateDialog.onItemSelected = { apiRequestRateInMinutes ->
            binding.displayedApiRequestRateTextView.text = "Every $apiRequestRateInMinutes min"
            viewModel.saveIntToSharedPrefs(
                API_REQUEST_RATE_KEY,
                apiRequestRateInMinutes
            )

            if (NewPostService.isRunning())
                Toasty.warning(
                    requireContext(),
                    "Stop observing to take effect",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Reverts action bar
        val supportActionBar = (requireActivity() as AppCompatActivity).supportActionBar

        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        supportActionBar?.title = resources.getString(R.string.app_name)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    class ApiRequestRateDialog : AppCompatDialogFragment() {

        var onItemSelected: ((Int) -> Unit) = {}

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
                    onItemSelected(options[index].extractInts())
                    dismiss()
                }

                setNegativeButton("Cancel") { _, _ -> }

            }.create()

        private fun String.extractInts(): Int =
            Integer.valueOf(this.replace("[^0-9]".toRegex(), ""))
    }
}