package com.example.androiddevhelper

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ExamplePrompt : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(context!!).apply {
            setTitle("Example")

            setPositiveButton("Got it!") { _, _ -> }

            setMessage("Type in lowercase with no space. \nGood: \"askreddit\" \nBad: \"Ask Reddit\"")

            setIcon(R.drawable.question_mark)

        }.create()
}
