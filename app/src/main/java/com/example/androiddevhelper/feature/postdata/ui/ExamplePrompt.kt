package com.example.androiddevhelper.feature.postdata.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.androiddevhelper.R

class ExamplePrompt : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(context!!).apply {
            setTitle("Example")

            setPositiveButton("Got it!") { _, _ -> }

            setMessage("Type in the SubReddit's name with no spaces. It's Case-insensitive.  \n \nGood: askreddit \nBad: Ask Reddit \nGood: InstantRegret \nBad: Instant Regret")

            setIcon(R.drawable.help_icon)

        }.create()
}
