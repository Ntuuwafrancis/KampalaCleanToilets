package com.francosoft.kampalacleantoilets.utilities.extensions

import android.widget.EditText

fun enableOrDisable(
    editText: EditText
) : Boolean {
    return if (editText.isEnabled){
        editText.isEnabled = false
        true
    } else {
        editText.isEnabled = true
        false
    }
}