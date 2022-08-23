package com.francosoft.kampalacleantoilets.utilities.extensions

import java.util.*

fun Float.roundTo(n : Int) : Float {
    return "%.${n}f".format(Locale.ENGLISH,this).toFloat()
}

fun Double.roundTo(n : Int) : Double {
    return "%.${n}f".format(Locale.ENGLISH,this).toDouble()
}