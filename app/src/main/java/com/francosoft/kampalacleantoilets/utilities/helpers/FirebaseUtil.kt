package com.francosoft.kampalacleantoilets.utilities.helpers

import android.app.Activity
import android.app.Activity.RESULT_OK
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.francosoft.kampalacleantoilets.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.google.firebase.auth.FirebaseAuth

object FirebaseUtil {
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private var firebaseUtil: FirebaseUtil? = null
    private const val RC_SIGN_IN: Int = 123
    var toilets: MutableList<Toilet>? = null

    fun openFbReference(ref: String, activity: Activity, ) {
        if (firebaseUtil == null) {
            firebaseUtil = FirebaseUtil
            firebaseDatabase = FirebaseDatabase.getInstance()
            firebaseAuth = FirebaseAuth.getInstance()

            authStateListener = FirebaseAuth.AuthStateListener {
                if (firebaseAuth.currentUser == null){
                    signIn(activity)
                }
            }
        }

        toilets = mutableListOf()
        databaseReference = firebaseDatabase.reference.child(ref)
    }

    private fun signIn(caller: Activity) {
        // Choose authentication providers
        val providers = arrayListOf(
//                    AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.kct_icon) // Set logo drawable
            .setTheme(R.style.Theme_KampalaCleanToilets)
            .build()

        val signInLauncher = caller.startActivityForResult(
            signInIntent, RC_SIGN_IN
        )

    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }


    fun attachListener() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    fun detachListener() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}