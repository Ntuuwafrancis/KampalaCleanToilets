package com.francosoft.kampalacleantoilets.utilities.helpers

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.francosoft.kampalacleantoilets.R


object RateItDialogFragement: DialogFragment() {
    private val LAUNCHES_UNTIL_PROMPT = 10
    private val DAYS_UNTIL_PROMPT = 3
    private val MILLIS_UNTIL_PROMPT = DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000
    private val PREF_NAME = "APP_RATER"
    private val LAST_PROMPT = "LAST_PROMPT"
    private val LAUNCHES = "LAUNCHES"
    private val DISABLED = "DISABLED"

//    override fun show(manager: FragmentManager, tag: String?) {
//        super.show(manager, tag)
//
//    }

    fun show(context: Context, manager: FragmentManager) {
        var shouldShow = false
        val sharedPreferences = getSharedPreferences(context)
        val editor = sharedPreferences.edit()
        val currentTime = System.currentTimeMillis()
        var lastPromptTime = sharedPreferences.getLong(LAST_PROMPT, 0)
        if (lastPromptTime == 0L) {
            lastPromptTime = currentTime
            editor.putLong(LAST_PROMPT, lastPromptTime)
        }
        if (!sharedPreferences.getBoolean(DISABLED, false)) {
            val launches = sharedPreferences.getInt(LAUNCHES, 0) + 1
            if (launches > LAUNCHES_UNTIL_PROMPT) {
                if (currentTime > lastPromptTime + MILLIS_UNTIL_PROMPT) {
                    shouldShow = true
                }
            }
            editor.putInt(LAUNCHES, launches)
        }
        if (shouldShow) {
            editor.putInt(LAUNCHES, 0).putLong(LAST_PROMPT, System.currentTimeMillis()).apply()
            this.show(context ,manager)
        } else {
            editor.apply()
        }
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, 0)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
            .setTitle("Rate Kampala Clean Toilets" )
            .setMessage("Give a 5 star if you like this app")
            .setPositiveButton(
                R.string.ok,
                DialogInterface.OnClickListener { _, _ ->
                    context?.let {
                        ContextCompat.startActivity(
                            it,
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + requireActivity().packageName)
                            ), null
                        )
                    }
                    getSharedPreferences(requireActivity()).edit().putBoolean(DISABLED, true).apply()
                    dismiss()
                })
            .setNeutralButton("Remind me later",
                DialogInterface.OnClickListener { _, _ -> dismiss() })
            .setNegativeButton("Not Now",
                DialogInterface.OnClickListener { _, _ ->
                    getSharedPreferences(requireActivity()).edit().putBoolean(DISABLED, true).apply()
                    dismiss()
                }).create()
    }
}