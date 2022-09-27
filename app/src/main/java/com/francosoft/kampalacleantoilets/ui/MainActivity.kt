package com.francosoft.kampalacleantoilets.ui

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import androidx.work.WorkManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.data.models.User
import com.francosoft.kampalacleantoilets.databinding.ActivityMainBinding
import com.francosoft.kampalacleantoilets.databinding.NavHeaderBinding
import com.francosoft.kampalacleantoilets.utilities.helpers.*
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.REQUEST_TURN_DEVICE_LOCATION_ON
import com.francosoft.kampalacleantoilets.utilities.helpers.GeofenceUtils.addGeofence
import com.francosoft.kampalacleantoilets.utilities.helpers.GeofenceUtils.askLocationPermission
import com.francosoft.kampalacleantoilets.utilities.helpers.GeofenceUtils.authorizedLocation
import com.francosoft.kampalacleantoilets.utilities.helpers.ShareApp.share
import com.francosoft.kampalacleantoilets.utilities.receivers.GeofenceBroadcastReceiver
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
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
    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var dbListener: ValueEventListener
    private var currentuser: User? = null
    private var isAdmin: Boolean = false
    private var userExists: Boolean = false
    private var toilets: MutableList<Toilet> = mutableListOf()
    lateinit var bottomNavigationView: BottomNavigationView
    private val gadgetQ = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private lateinit var geoClient: GeofencingClient
    private var isFencesOn: Boolean = false
    private var geofenceList: MutableList<Geofence> = mutableListOf()
    private lateinit var workManager: WorkManager

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        this.onSignInResult(result)
    }

    private val geofenceIntent: PendingIntent by lazy {
        val intent = Intent(this.applicationContext, GeofenceBroadcastReceiver::class.java)
        intent.action = Constants.ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // navView is NavigationView
        val viewHeader = binding.navView.getHeaderView(0)

        // nav_header.xml is headerLayout
         navViewHeaderBinding = NavHeaderBinding.bind(viewHeader)

        // instantiate work manager
        workManager = WorkManager.getInstance(this)

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
                R.id.mapFragment, R.id.toiletsFragment, R.id.favoritesFragment2,R.id.userFragment2, R.id.shareApp, R.id.rateAppFragment, R.id.aboutFragment2
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.rateAppFragment -> { RateItDialogFragement.show(supportFragmentManager, "Rate App")}
                R.id.shareApp -> {
                    share(this)
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(it, navController)
                    drawerLayout.closeDrawers()
                }
            }
            true
        }
        val contentMainBinding = binding.appBarMain.contentMain
        bottomNavigationView = contentMainBinding.btAppbarToilets
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnItemReselectedListener {
            when(it.itemId) {
                R.id.toiletsFragment -> {
                    navController.navigate(R.id.toiletsFragment)
                }

                R.id.newToiletsFragment -> {
                    navController.navigate(R.id.newToiletsFragment)
                }

                R.id.editedToiletsFragment -> {
                    navController.navigate(R.id.editedToiletsFragment)
                }
            }
        }
        navController.addOnDestinationChangedListener { _, nd: NavDestination, _ ->
            if (nd.id == R.id.toiletsFragment || nd.id == R.id.newToiletsFragment || nd.id == R.id.editedToiletsFragment) {
                bottomNavigationView.visibility = View.VISIBLE
            } else {
                bottomNavigationView.visibility = View.GONE
            }
        }

        FirebaseUtil.openFbReference("user", this)
        firebaseDb = FirebaseUtil.firebaseDatabase
        databaseRef = FirebaseUtil.databaseReference
//        auth = FirebaseUtil.firebaseAuth
        auth = Firebase.auth

        geoClient = LocationServices.getGeofencingClient(this)

//        startLocation()

        fbListener = FirebaseAuth.AuthStateListener {
            val providers = arrayListOf(
                AuthUI.IdpConfig.GoogleBuilder().build()
            )

            if (auth.currentUser == null) {
                etUsername.text = ""
                etEmail.text = ""
                imgBadge.visibility = View.INVISIBLE
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

        startGeofences()
    }

    private fun startGeofences(){
        AppExecutors.instance?.diskIO()?.execute{
            val database = FirebaseDatabase.getInstance()
            val dbref = database.getReference("toilet")
            val valueEventListener: ValueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    toilets.clear()
                    for (ds in dataSnapshot.children) {
                        val toilet = ds.getValue(Toilet::class.java) as Toilet

                        if (toilet.approved.equals("approved")
                            || toilet.approved.equals("delete")){
                            toilets.add(toilet)
                            geofenceList.add(GeofenceUtils.createGeofence(toilet, 100f))
                        }
                    }
                    Log.d(TAG, toilets.size.toString())
                    getUser(binding.root, toilets)
                    //Do what you need to do with your violations list
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(TAG, databaseError.message)
                }
            }
            dbref.addListenerForSingleValueEvent(valueEventListener)
        }

    }

    private fun switchOnFences(view: View, toilets: MutableList<Toilet>) {
        if (isFencesOn) {
            examinePermissionAndInitiateGeofence(view)
        } else {
            GeofenceUtils.removeGeofence(view, geoClient, geofenceIntent)
        }
    }

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) === PackageManager.PERMISSION_GRANTED
    }

    private fun examinePermissionAndInitiateGeofence(view: View) {
        if (authorizedLocation(view)) {
            if (currentuser?.triggeringGeofenceId != null){
                var geofence: Geofence? = null
                geofenceList.forEach {
                    if (it.requestId == currentuser?.triggeringGeofenceId){
                        geofence = it
                    }
                }
                geofence?.let {
                    geofenceList.remove(geofence)
                }
            }
            validateGadgetAreaInitiateGeofence()
        } else {
            askLocationPermission(view, this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE ||
            requestCode == REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                validateGadgetAreaInitiateGeofence()
            }
        }
    }

    private fun validateGadgetAreaInitiateGeofence(resolve: Boolean = true) {

        // create a location request that request for the quality of service to update the location
        val locationRequest = LocationRequest.create().apply {
            priority =  Priority.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        // check if the client location settings are satisfied
        val client = LocationServices.getSettingsClient(this)

        // create a location response that acts as a listener for the device location if enabled
        val locationResponses = client.checkLocationSettings(builder.build())

        locationResponses.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(
                        this, REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: ${sendEx.message}")
                }
            } else {
                Toast.makeText(this, "Enable your location", Toast.LENGTH_SHORT).show()
            }
        }

        locationResponses.addOnCompleteListener {it ->
            if (it.isSuccessful) {
                addGeofence(binding.root, geoClient, geofenceIntent, geofenceList
                ,toilets, 100f)
            }
        }
    }

    private fun getUser(view: View, toilets: MutableList<Toilet>) {
        firebaseDb.getReference("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val user1 = postSnapshot.getValue(User::class.java) as User
                    if (user1.uid == auth.currentUser?.uid) {
                        isFencesOn = user1.fencesOn
                        currentuser = user1
                    }
                }
                switchOnFences(view, toilets)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(view.context, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        validateGadgetAreaInitiateGeofence(false)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }

    override fun onStart() {
        super.onStart()
        if (toilets.isNotEmpty()) {
            getUser(binding.root,toilets)
        }
//        examinePermissionAndInitiateGeofence(binding.root)
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser == null){
            auth.addAuthStateListener(fbListener)
        } else {

            imgBadge.visibility = View.VISIBLE
            etEmail.text = currentUser.email
            etUsername.text = currentUser.displayName
            Toast.makeText(this, "Welcome Back ${etUsername.text}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val user = auth.currentUser

        if (user != null) {
            etUsername.text = user.displayName
            etEmail.text = user.email
            imgBadge.visibility = View.VISIBLE
        } else {
            etUsername.text = ""
            etEmail.text = ""
            imgBadge.visibility = View.INVISIBLE
        }
    }

    private fun checkIfUserExists( currentUser: FirebaseUser) : Boolean{
        dbListener = databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val user = postSnapshot.getValue(User::class.java) as User

                    if (currentUser.uid == user.uid) {
                        userExists = true
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
            }

        })
        return userExists
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

//                val key = firebaseDb.getReference("user/"+ user.uid).push().key
                val newUser = User(user.displayName,
                    user.email, "normal", user.uid)

                if (newUser.uid != null && !checkIfUserExists( user)) {
                    firebaseDb.getReference("user/"+ user.uid).setValue(newUser)
                }


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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_container)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener (fbListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseUtil.detachListener()
    }
}