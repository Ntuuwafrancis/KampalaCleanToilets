package com.francosoft.kampalacleantoilets.utilities.helpers

import android.content.Context
import android.content.Intent

object ShareApp {

    fun share(context: Context) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Clean Toilet Locator")
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage =
                """
                        ${shareMessage}https://play.google.com/store/apps/details?id=com.app.cleantoiletlocator


                        """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            context.startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            //e.toString();
        }

    //                ShareCompat.IntentBuilder(this)
//                    .setType("text/plain")
//                    .setChooserTitle("choose one")
//                    .setText("http://play.google.com/store/apps/details?id=" + "com.android.kampalacleantoilets")
//                    .startChooser()
    }
}