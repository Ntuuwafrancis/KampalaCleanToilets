package com.francosoft.kampalacleantoilets.utilities.extensions

import android.widget.EditText
import android.widget.Spinner

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

fun enableOrDisableSp(
    sp : Spinner
) : Boolean{
    return if (sp.isEnabled){
        sp.isEnabled = false
        true
    }else {
        sp.isEnabled = true
        false
    }
}

