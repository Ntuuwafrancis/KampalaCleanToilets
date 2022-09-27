package com.francosoft.kampalacleantoilets.ui.user

import android.Manifest
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.firebase.ui.auth.AuthUI
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.data.models.User
import com.francosoft.kampalacleantoilets.databinding.UserFragmentBinding
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.All_TOILETS_REPORT
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.DIVISIONS_REPORT
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.REPORT_TYPE
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.STATUS_REPORT
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.TYPE_REPORT
import com.francosoft.kampalacleantoilets.utilities.helpers.FirebaseUtil
import com.francosoft.kampalacleantoilets.workers.ReportsWorker
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import pub.devrel.easypermissions.EasyPermissions

class UserFragment : Fragment() {

    companion object {
        fun newInstance() = UserFragment()
        const val REQUEST_CODE_LOCATION_PERMISSION = 11
    }

    private lateinit var viewModel: UserViewModel
    private lateinit var binding: UserFragmentBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var dbListener: ValueEventListener
    private var auth: FirebaseAuth? = null
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSignOut: Button
    private lateinit var btnTutorial: Button
    private lateinit var btnAllToiletsReports: Button
    private lateinit var btnStatusReports: Button
    private lateinit var btnTypeReports: Button
    private lateinit var btnDivisionReports: Button
    private lateinit var navController: NavController
    private lateinit var switchFences: SwitchMaterial
    private lateinit var geofencingClient: GeofencingClient
    private var toilets: MutableList<Toilet> = mutableListOf()
    private var permissionsNeeded: MutableList<String> = mutableListOf()
    private var admin: Boolean = false
    private lateinit var workManager: WorkManager
//    private lateinit var radio500m: RadioButton
//    private lateinit var radio1km: RadioButton
//    private lateinit var radio100m: RadioButton
    private var user: User = User()
//    private val pendingIntent: PendingIntent by lazy {
//        val intent = Intent(
//            requireContext(),
//            GeofenceBroadcastReceiver::class.java
//        )
//        PendingIntent.getBroadcast(requireContext(), 0,
//            intent, PendingIntent.FLAG_UPDATE_CURRENT
//        )
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        binding = UserFragmentBinding.bind(view)
        navController = Navigation.findNavController(view)
        workManager = WorkManager.getInstance(view.context)

        FirebaseUtil.openFbReference("toilet", requireActivity())
        firebaseDb = FirebaseUtil.firebaseDatabase
        databaseRef = FirebaseUtil.databaseReference
        auth = FirebaseUtil.firebaseAuth

        binding.apply {
            this@UserFragment.etUsername = username
            this@UserFragment.etEmail = email
            this@UserFragment.btnSignOut = btnSignOutAccount
            this@UserFragment.switchFences = switchOnNearby
            this@UserFragment.btnTutorial = btnTutorial
            this@UserFragment.btnAllToiletsReports = btnCreateAllToiletsReport
            this@UserFragment.btnDivisionReports = btnCreateDivisionsReport
            this@UserFragment.btnStatusReports = btnCreateStatusReport
            this@UserFragment.btnTypeReports = btnCreateTypesReport

//            this@UserFragment.radio1km = oneKmBtn
//            this@UserFragment.radio100m = oneHundredMBtn
//            this@UserFragment.radio500m = fiveHundredMBtn
        }

        if (auth != null) {
            populateFields()
        }

        getUser()
        getAllToilets()
        generateReports()

        permissionsNeeded = checkPermissions()
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        signInOrOut(view, requireActivity())
        switchFences.setOnClickListener {
            if (switchFences.isChecked) {
                user.fencesOn = true
                user.fences = 100.0
                user.uid?.let { it1 -> firebaseDb.getReference("user").child(it1).setValue(user) }
            } else if (!switchFences.isChecked) {
                user.fencesOn = false
                user.uid?.let { it1 -> firebaseDb.getReference("user").child(it1).setValue(user) }
            }
        }

        btnTutorial.setOnClickListener {
            val action = UserFragmentDirections.actionUserFragment2ToTutorialFragment()
            navController.navigate(action)
        }

    }

    private fun generateReports() {

        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            PackageManager.PERMISSION_GRANTED
        )

        btnAllToiletsReports.setOnClickListener {
            val data = workDataOf(REPORT_TYPE to All_TOILETS_REPORT)
            val workRequest = OneTimeWorkRequestBuilder<ReportsWorker>()
                .setInputData(data)
                .build()
            workManager.enqueue(workRequest)
            Toast.makeText(context, "All Toilets report created in Documents", Toast.LENGTH_SHORT).show()
        }

        btnStatusReports.setOnClickListener {
            val data = workDataOf(REPORT_TYPE to STATUS_REPORT)
            val workRequest = OneTimeWorkRequestBuilder<ReportsWorker>()
                .setInputData(data)
                .build()
            workManager.enqueue(workRequest)
            Toast.makeText(context, "Toilet Status reports created in Documents", Toast.LENGTH_SHORT).show()
        }
        btnDivisionReports.setOnClickListener {
            val data = workDataOf(REPORT_TYPE to DIVISIONS_REPORT)
            val workRequest = OneTimeWorkRequestBuilder<ReportsWorker>()
                .setInputData(data)
                .build()
            workManager.enqueue(workRequest)
            Toast.makeText(context, "Divisions reports created in Documents", Toast.LENGTH_SHORT).show()
        }
        btnTypeReports.setOnClickListener {
            val data = workDataOf(REPORT_TYPE to TYPE_REPORT)
            val workRequest = OneTimeWorkRequestBuilder<ReportsWorker>()
                .setInputData(data)
                .build()
            workManager.enqueue(workRequest)
            Toast.makeText(context, "Toilet Types reports created in Documents", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUser() {
        firebaseDb.getReference("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val user1 = postSnapshot.getValue(User::class.java) as User
                    if (user1.uid == auth?.currentUser?.uid) {
                        user = user1

                        if (user1.role.equals("admin")) {
                            admin = true
                            btnAllToiletsReports.visibility = View.VISIBLE
                            btnTypeReports.visibility = View.VISIBLE
                            btnDivisionReports.visibility = View.VISIBLE
                            btnStatusReports.visibility = View.VISIBLE
                        }
                    }

                }
                switchFences.isChecked = user.fencesOn
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun getAllToilets() {

        dbListener = databaseRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                toilets.clear()

                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val toilet = postSnapshot.getValue(Toilet::class.java) as Toilet
                    toilet.id = postSnapshot.key

                    if(toilet.approved.equals("delete") || toilet.approved.equals("approved")){
                        toilets.add(toilet)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun checkPermissions() : MutableList<String> {
        if (!EasyPermissions.hasPermissions(
                requireContext(),
                ACCESS_FINE_LOCATION
            )
        ) {
            permissionsNeeded.add(ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            !EasyPermissions.hasPermissions(
                requireContext(),
                ACCESS_BACKGROUND_LOCATION
            )
        ) {
            permissionsNeeded.add(ACCESS_BACKGROUND_LOCATION)
        }
        return permissionsNeeded
    }

    private fun signInOrOut(view: View, activity: Activity) {
        btnSignOut.setOnClickListener {
            if (FirebaseUtil.firebaseAuth.currentUser != null) {

                AuthUI.getInstance()
                    .signOut(activity)
                    .addOnCompleteListener {
                        // ...
                        Toast.makeText(activity, "User Logged Out", Toast.LENGTH_LONG).show()
                    }
            }
            else {
                FirebaseUtil.attachListener()
                if (auth != null) {
                    view.postInvalidate()
                    activity.invalidateOptionsMenu()
                }
            }
            val action = UserFragmentDirections.actionUserFragment2ToMapFragment(false)
            navController.navigate(action)
//            activity.recreate()
        }
    }

    private fun populateFields() {
         val user = auth?.currentUser
         if (user != null) {
             binding.username.setText(user.displayName.toString())
             etEmail.setText(user.email.toString())
             btnSignOut.text = getString(R.string.sign_out)
         } else {
             btnSignOut.text = getString(R.string.sign_in)
             etEmail.setText("")
             etUsername.setText("")
         }
    }

    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onResume() {
        super.onResume()
        populateFields()
    }

    override fun onPause() {
        super.onPause()
        FirebaseUtil.detachListener()
    }
}