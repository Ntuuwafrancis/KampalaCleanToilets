package com.francosoft.kampalacleantoilets.utilities.helpers

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.data.models.User
import com.francosoft.kampalacleantoilets.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

object FirebaseUtil {
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private var firebaseUtil: FirebaseUtil? = null
    private const val RC_SIGN_IN: Int = 123
    var toilets: MutableList<Toilet>? = null
    private var isAdmin: Boolean = false
//    private lateinit var user: FirebaseUser
    private val mainActivity: MainActivity by lazy { MainActivity() }

    fun openFbReference(ref: String, activity: Activity ) {
        if (firebaseUtil == null) {
            firebaseUtil = FirebaseUtil
            firebaseDatabase = FirebaseDatabase.getInstance()
            firebaseAuth = FirebaseAuth.getInstance()

            authStateListener = FirebaseAuth.AuthStateListener {
                if (firebaseAuth.currentUser == null){
                    signIn(activity)
                } else {
                    checkAdmin()
                    firebaseAuth.currentUser?.let { addNewUser(it, activity) }

                }
            }
        }

        toilets = mutableListOf()
        databaseReference = firebaseDatabase.reference.child(ref)
    }

    private fun addNewUser(user: FirebaseUser, context: Context) {
        val newUser = User(user.displayName,
            user.email, "normal", user.uid)
        val exists = checkIfUserExists(user, context)
        if (newUser.uid != null && !exists) {
            firebaseDatabase.getReference("user/"+ user.uid).setValue(newUser)
        }
    }

    private fun checkAdmin(): Boolean {
        isAdmin = false
        firebaseDatabase.getReference("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val user = postSnapshot.getValue(User::class.java) as User

                    if (user.role.equals("admin")) {
                        isAdmin = true
                        mainActivity.bottomNavigationView.visibility = View.VISIBLE

                    }else {
                        mainActivity.bottomNavigationView.visibility = View.GONE

                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(mainActivity, error.message, Toast.LENGTH_SHORT).show()
            }

        })
        return isAdmin
    }

    private fun checkIfUserExists(fbuser: FirebaseUser, context: Context) : Boolean{

        var userExists = false
        firebaseDatabase.getReference("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val user = postSnapshot.getValue(User::class.java) as User

                    if ( fbuser.uid == user.uid) {
                        userExists = true
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

        })
        return userExists
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

//    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
//        val response = result.idpResponse
//        if (result.resultCode == RESULT_OK) {
//            // Successfully signed in
//            val user = FirebaseAuth.getInstance().currentUser
//            // ...
//        } else {
//            // Sign in failed. If response is null the user canceled the
//            // sign-in flow using the back button. Otherwise check
//            // response.getError().getErrorCode() and handle the error.
//            // ...
//        }
//    }


    fun attachListener() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    fun detachListener() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}