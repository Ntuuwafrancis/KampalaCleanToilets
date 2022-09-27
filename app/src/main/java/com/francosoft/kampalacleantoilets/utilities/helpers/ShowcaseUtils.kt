package com.francosoft.kampalacleantoilets.utilities.helpers

import android.app.Activity
import android.view.View
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig

object ShowcaseUtils {
    fun newShowcase(view: View, activity: Activity, dismissTxt: String, text: String, showcaseId: String) {
        // single example
        MaterialShowcaseView.Builder(activity)
            .setTarget(view)
            .setDismissText(dismissTxt)
            .setContentText(text)
            .setDelay(1000) // optional but starting animations immediately in onCreate can make them choppy
            .singleUse(showcaseId) // provide a unique ID used to ensure it is only shown once
            .show()
    }

    fun addShowcaseSequence(view: View, activity: Activity, dismissTxt: String, text: String, showcaseId: String) {
        // sequence example
        val config = ShowcaseConfig()
        config.delay = 500; // half second between each showcase view
        val sequence = MaterialShowcaseSequence(activity, showcaseId);

        sequence.setConfig(config)
        sequence.addSequenceItem(view,
            text, dismissTxt)
        sequence.start();
    }
}