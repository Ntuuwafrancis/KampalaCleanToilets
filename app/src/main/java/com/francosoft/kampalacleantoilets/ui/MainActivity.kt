package com.francosoft.kampalacleantoilets.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.firebase.ui.auth.*
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.databinding.ActivityMainBinding
import com.francosoft.kampalacleantoilets.databinding.NavHeaderBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var navController: NavController
    private lateinit var etUsername: TextView
    private lateinit var etEmail: TextView
    private lateinit var imgBadge: ImageView
    private lateinit var navViewHeaderBinding : NavHeaderBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var fbListener: FirebaseAuth.AuthStateListener

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        this.onSignInResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // navView is NavigationView
        val viewHeader = binding.navView.getHeaderView(0)

        // nav_header.xml is headerLayout
         navViewHeaderBinding = NavHeaderBinding.bind(viewHeader)

        // title is Children of nav_header
        navViewHeaderBinding.tvEmail
        navViewHeaderBinding.apply {
            this@MainActivity.etEmail = tvEmail
            this@MainActivity.etUsername = tvUsername
            this@MainActivity.imgBadge = imgGmailBadge
        }

        val navHostFragment = supportFragmentManager.findFragmentById(androidx.navigation.fragment.R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        setSupportActionBar(binding.appBarMain.contentMain.toolbarMain.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mapFragment, R.id.toiletsFragment, R.id.favoritesFragment2,R.id.userFragment2, R.id.rateApp, R.id.share , R.id.aboutFragment2
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.rateApp -> {}
                R.id.share -> {}
                else -> {
                    NavigationUI.onNavDestinationSelected(it, navController)
                    drawerLayout.closeDrawers()
                }
            }
            true
        }

        auth = Firebase.auth
        fbListener = FirebaseAuth.AuthStateListener {
            val providers = arrayListOf(
                AuthUI.IdpConfig.GoogleBuilder().build()
            )


            if (auth.currentUser == null) {
                // Create and launch sign-in intent
                val signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setLogo(R.drawable.kct_icon) // Set logo drawable
                    .setTheme(R.style.Theme_KampalaCleanToilets)
                    .build()
                // Register your launcher here

                signInLauncher.launch(signInIntent)
            }

        }

    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser == null){
            auth.addAuthStateListener(fbListener)
        } else {

            imgBadge.visibility = View.VISIBLE
            etEmail.text = currentUser.email
            etUsername.text = currentUser.displayName
        }
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener { fbListener }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                etUsername.text = user.displayName
                etEmail.text = user.email
                imgBadge.visibility = View.VISIBLE
                Toast.makeText(this, "Signed In Successfully!", Toast.LENGTH_LONG).show()

            }
            // ...
        } else {
            if (response == null) {
                etUsername.text = ""
                etEmail.text = ""
                imgBadge.visibility = View.INVISIBLE
                Toast.makeText(this, "Signing in cancelled. Note, you cannot save your changes while signed out!", Toast.LENGTH_LONG).show()
            }
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }

    override fun onResume() {
        super.onResume()
        val user = auth.currentUser

        if (user != null) {
            etUsername.text = user.displayName
            etEmail.text = user.email
            imgBadge.visibility = View.VISIBLE
            Toast.makeText(this, "Welcome Back ${etUsername.text}", Toast.LENGTH_LONG).show()
        } else {
            etUsername.text = ""
            etEmail.text = ""
            imgBadge.visibility = View.INVISIBLE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_container)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}