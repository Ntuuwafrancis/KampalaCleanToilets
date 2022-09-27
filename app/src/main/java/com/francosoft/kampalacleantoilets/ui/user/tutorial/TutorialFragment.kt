package com.francosoft.kampalacleantoilets.ui.user.tutorial

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.francosoft.kampalacleantoilets.databinding.FragmentTutorialBinding
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import java.util.*


/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class TutorialFragment : Fragment() {
    private val hideHandler = Handler(Looper.myLooper()!!)

    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        val flags =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity?.window?.decorView?.systemUiVisibility = flags
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        fullscreenContentControls?.visibility = View.VISIBLE
    }
    private var visible: Boolean = false
    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    @SuppressLint("ClickableViewAccessibility")
    private val delayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    private var nearBtn: ImageButton? = null
    private var locationBtn: ImageButton? = null
    private var myPinBtn: ImageButton? = null
    private var directionBtn: ImageButton? = null
    private var googlePinBtn: ImageButton? = null
    private var zoomBtn: ImageButton? = null
    private var cardBtn: ImageButton? = null
    private var toiletBtn: ImageButton? = null
    private var fullscreenContent: View? = null
    private var fullscreenContentControls: View? = null

    private var _binding: FragmentTutorialBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTutorialBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true

        binding.apply {
            this@TutorialFragment.cardBtn = card
            this@TutorialFragment.locationBtn = myLocation
            this@TutorialFragment.myPinBtn = myPinBtn
            this@TutorialFragment.zoomBtn = zoom
            this@TutorialFragment.toiletBtn = toiletBtn
            this@TutorialFragment.directionBtn = directions
            this@TutorialFragment.googlePinBtn = pinOnMap
            this@TutorialFragment.nearBtn = nearest

        }
//        fullscreenContent = binding.fullscreenContent
        fullscreenContentControls = binding.fullscreenContentControls

        val showId = Calendar.getInstance().get(Calendar.MILLISECOND)
        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent?.setOnClickListener { toggle() }

        // sequence
        val config = ShowcaseConfig()
        config.delay = 500 // half second between each showcase view
        val sequence = MaterialShowcaseSequence(requireActivity(), showId.toString())
        sequence.setConfig(config)

        sequence.addSequenceItem(
            nearBtn,
            "Click this button to show the nearest toilet to your location",
            "GOT IT"
        )
        sequence.addSequenceItem(
            locationBtn,
            "Click this button to show your location pin on the map",
            "GOT IT"
        )
        sequence.addSequenceItem(
            myPinBtn,
            "This pin shows your precise location on map",
            "GOT IT"
        )
        sequence.addSequenceItem(
            toiletBtn,
            "This pin shows a toilet location. Click it to show more options",
            "GOT IT"
        )
        sequence.addSequenceItem(
            directionBtn,
            "After clicking on toilet button, click this button to open directions on google maps to toilet from your location",
            "GOT IT"
        )
        sequence.addSequenceItem(
            googlePinBtn,
            "After clicking on toilet button, Click this button show the toilet on google maps with distance and image details",
            "GOT IT"
        )
        sequence.addSequenceItem(
            cardBtn,
            "After clicking on toilet button, click this details card to show full toilet info and edit options",
            "GOT IT"
        )
        sequence.addSequenceItem(
            zoomBtn,
            "Click these buttons to zoom on map",
            "GOT IT"
        )
        sequence.start()

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        dummyButton?.setOnTouchListener(delayHideTouchListener)
    }

    override fun onResume() {
        super.onResume()
//        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
//        delayedHide(100)
    }

    override fun onPause() {
        super.onPause()
//        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

//        WindowInsetsController.BEHAVIOR_DEFAULT
//         Clear the systemUiVisibility flag
//        activity?.window?.decorView?.systemUiVisibility = 0
//        show()
    }

    override fun onDestroy() {
        super.onDestroy()
//        dummyButton = null
//        fullscreenContent = null
//        fullscreenContentControls = null
    }

    private fun toggle() {
        if (visible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        fullscreenContentControls?.visibility = View.GONE
        visible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @Suppress("InlinedApi")
    private fun show() {
        // Show the system bar
        fullscreenContent?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        visible = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}