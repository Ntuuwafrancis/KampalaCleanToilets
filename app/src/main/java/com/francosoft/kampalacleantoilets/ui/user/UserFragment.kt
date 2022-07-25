package com.francosoft.kampalacleantoilets.ui.user

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.firebase.ui.auth.AuthUI
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.data.models.User
import com.francosoft.kampalacleantoilets.databinding.UserFragmentBinding
import com.francosoft.kampalacleantoilets.ui.MainActivity
import com.francosoft.kampalacleantoilets.utilities.helpers.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlin.system.exitProcess

class UserFragment : Fragment() {

    companion object {
        fun newInstance() = UserFragment()
    }

    private lateinit var viewModel: UserViewModel
    private lateinit var binding: UserFragmentBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseDb: FirebaseDatabase
    private var auth: FirebaseAuth? = null
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSignOut: Button
    private lateinit var navController: NavController

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

        FirebaseUtil.openFbReference("user", requireActivity())
        firebaseDb = FirebaseUtil.firebaseDatabase
        databaseRef = FirebaseUtil.databaseReference
        auth = FirebaseUtil.firebaseAuth

        binding.apply {
            this@UserFragment.etUsername = username
            this@UserFragment.etEmail = email
            this@UserFragment.btnSignOut = btnSignOutAccount
        }

        if (auth != null) {
            populateFields()
        }

        signInOrOut(view, requireActivity())

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

    override fun onResume() {
        super.onResume()
        populateFields()
    }

    override fun onPause() {
        super.onPause()
        FirebaseUtil.detachListener()
    }
}