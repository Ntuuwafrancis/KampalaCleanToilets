package com.francosoft.kampalacleantoilets.ui.toilet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.databinding.ToiletFragmentBinding
import com.francosoft.kampalacleantoilets.utilities.extensions.enableOrDisable
import com.francosoft.kampalacleantoilets.utilities.helpers.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ToiletFragment : Fragment() {

    private lateinit var binding: ToiletFragmentBinding
    private var editing: Boolean = false
    private lateinit var btnEdit: Button
    private lateinit var btnDelete: Button
    private lateinit var etToiletTitle: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPhone: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var etType: EditText
    private lateinit var etOpeningHours: EditText
    private lateinit var etClosingHours: EditText
    private lateinit var etOperationalStatus: EditText
    private lateinit var etCharge: EditText
    private lateinit var etExtraInfo: EditText
    private lateinit var dbListener: ValueEventListener
    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private val args: ToiletFragmentArgs by navArgs()
    private var newToilet: Boolean = false
    private lateinit var navController: NavController
    private var toilet: Toilet? = null
    private var purpose: String? = null
    private var isAdmin = false

    companion object {
//        fun newInstance() = ToiletFragment()
    }

    private lateinit var viewModel: ToiletViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.toilet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ToiletViewModel::class.java]

        binding = ToiletFragmentBinding.bind(view)
        navController = Navigation.findNavController(view)

        binding.apply {

            this@ToiletFragment.etToiletTitle = toiletName
            this@ToiletFragment.etAddress = address
            this@ToiletFragment.etPhone = phone
            this@ToiletFragment.etLatitude = latitude
            this@ToiletFragment.etLongitude = longitude
            this@ToiletFragment.etType = type
            this@ToiletFragment.etOpeningHours = openingHours
            this@ToiletFragment.etClosingHours = closingHours
            this@ToiletFragment.etOperationalStatus = status
            this@ToiletFragment.etCharge = charge
            this@ToiletFragment.etExtraInfo = extraInfo
            this@ToiletFragment.btnEdit = btnEditToilet
            this@ToiletFragment.btnDelete = btnDeleteToilet
        }

        initializeSetup()

        FirebaseUtil.openFbReference("toilet", requireActivity())
        firebaseDb = FirebaseUtil.firebaseDatabase
        databaseRef = FirebaseUtil.databaseReference
        auth = FirebaseUtil.firebaseAuth

//        isUserAdmin(requireActivity())

        binding.allReview.setOnClickListener {
            val action = ToiletFragmentDirections.actionToiletFragmentToReviewsFragment(toilet)
            navController.navigate(action)
        }

        binding.addReview.setOnClickListener {
            val action = ToiletFragmentDirections.actionToiletFragmentToReviewFragment(
                toilet,
                auth.currentUser?.email,
                auth.currentUser?.displayName
            )
            navController.navigate(action)
        }

        onClickSaveButton()
        onClickDeleteButton()
    }

    private fun initializeSetup(){
        isAdmin = args.isAdmin
        toilet = args.toilet
        purpose = args.approveToilet

        if (purpose.equals("approve")){
            binding.allReview.visibility = View.INVISIBLE
            binding.addReview.visibility = View.INVISIBLE
            changeFields("Approve", "Reject")
            toilet?.let { populateToiletDetails(it) }
        } else if (purpose.equals("edit")) {
            binding.allReview.visibility = View.VISIBLE
            binding.addReview.visibility = View.VISIBLE
            toilet?.let { populateToiletDetails(it) }
        } else if (purpose.equals("new")){
            binding.allReview.visibility = View.INVISIBLE
            binding.addReview.visibility = View.INVISIBLE
            changeFields(getString(R.string.save), getString(R.string.cancel))
            toilet?.let { populateToiletDetails(it) }
        }
    }

    private fun onClickDeleteButton() {
        btnDelete.setOnClickListener {
            if (purpose.equals("edit")) {
                if (auth.currentUser != null) {
                    if (!editing) {
                        editing = true
                        toilet?.approved = "delete"
                        toilet?.id?.let { databaseRef.child(it).setValue(toilet) }
//                    toilet?.id?.let { it1 -> databaseRef.child(it1).removeValue() }

                        navController.navigate(R.id.newToiletsFragment)
//                        val action = ToiletFragmentDirections.actionToiletFragmentToNewToiletsFragment()
//                        navController.navigate(action)
                        Toast.makeText(activity, "Toilet Delete Requested",Toast.LENGTH_SHORT).show()
                    } else {
                        editing = false
                        Toast.makeText(activity, "Toilet Edit Cancelled",Toast.LENGTH_SHORT).show()
                        changeFields(getString(R.string.suggest_edit), getString(R.string.delete))
                    }

                } else {
                    Toast.makeText(activity, "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                }
            } else if(purpose.equals("approve")) {
                if (toilet != null){
                    when(toilet?.approved){
                        "delete" ->{
                            if (auth.currentUser != null) {

                                if (isAdmin) {
                                    toilet?.approved = "approved"
                                    toilet?.id?.let { databaseRef.child(it).setValue(toilet) }
                                    Toast.makeText(activity, "Toilet Delete Rejected",Toast.LENGTH_SHORT).show()

                                    val action = ToiletFragmentDirections.actionToiletFragmentToNewToiletsFragment()
                                    navController.navigate(action)
                                } else {
                                    Toast.makeText(activity, "Access Denied! Only Admin Allowed Make This Change",Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(activity, "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                            }
                        }
                        "edit" ->{
                            if (auth.currentUser != null) {

                                if (isAdmin) {
                                    toilet?.approved = "approved"
                                    toilet?.id?.let { databaseRef.child(it).setValue(toilet) }
                                    Toast.makeText(activity, "Toilet Edit Rejected",Toast.LENGTH_SHORT).show()

                                    val action = ToiletFragmentDirections.actionToiletFragmentToNewToiletsFragment()
                                    navController.navigate(action)
                                } else {
                                    Toast.makeText(activity, "Access Denied! Only Admin Allowed Make This Change",Toast.LENGTH_SHORT).show()
                                }

                            } else {
                                Toast.makeText(activity, "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                            }
                        }
                        "new" -> {
                            if (auth.currentUser != null) {

                                if (isAdmin) {
                                    toilet?.id?.let { it1 -> databaseRef.child(it1).removeValue() }
                                    Toast.makeText(activity, "New Toilet Rejected",Toast.LENGTH_SHORT).show()

                                    val action = ToiletFragmentDirections.actionToiletFragmentToNewToiletsFragment()
                                    navController.navigate(action)
                                } else {
                                    Toast.makeText(activity, "Access Denied! Only Admin Allowed Make This Change",Toast.LENGTH_SHORT).show()
                                }

                            } else {
                                Toast.makeText(activity, "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }

    }

    private fun onClickSaveButton() {

        btnEdit.setOnClickListener {

            if (purpose.equals("approve")){
                if (toilet != null){
                    when(toilet?.approved){
                        "delete" ->{
                            if (auth.currentUser != null) {

                                if (isAdmin) {
                                    toilet?.id?.let { it1 -> databaseRef.child(it1).removeValue() }
//                                changeFields(getString(R.string.suggest_edit), getString(R.string.delete))
//                                purpose = "edit"

                                    val action = ToiletFragmentDirections.actionToiletFragmentToNewToiletsFragment()
                                    navController.navigate(action)
                                    Toast.makeText(activity, "Toilet Removed",Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(activity, "Access Denied! Only Admin Allowed Make This Change",Toast.LENGTH_SHORT).show()
                                }

                            } else {
                                Toast.makeText(activity, "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                            }
                        }
                        "edit" ->{
                            if (auth.currentUser != null) {
                                if (isAdmin) {
                                    val toilet2 = captureEditTextFields()
                                    toilet2.id = toilet?.id
                                    toilet2.rating = toilet?.rating!!
                                    toilet2.approved = "approved"
                                    databaseRef.child(toilet2.id!!).setValue(toilet2)
                                    changeFields(getString(R.string.suggest_edit), getString(R.string.delete))
                                    purpose = "edit"

//                                val action = ToiletFragmentDirections.actionToiletFragmentToNewToiletsFragment()
//                                navController.navigate(action)
                                    Toast.makeText(activity, "Toilet Edit Approved",Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(activity, "Access Denied! Only Admin Allowed Make This Change",Toast.LENGTH_SHORT).show()
                                }

                            } else {
                                Toast.makeText(activity, "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                            }
                        }
                        "new" -> {
                            if (auth.currentUser != null) {

                                if (isAdmin) {
                                    val toilet2 = captureEditTextFields()
                                    toilet2.id = toilet?.id
                                    toilet2.rating = toilet?.rating!!
                                    toilet2.approved = "approved"
                                    databaseRef.child(toilet2.id!!).setValue(toilet2)
                                    changeFields(getString(R.string.suggest_edit), getString(R.string.delete))
                                    purpose = "edit"

//                                val action = ToiletFragmentDirections.actionToiletFragmentToNewToiletsFragment()
//                                navController.navigate(action)
                                    Toast.makeText(activity, "Toilet Approved",Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(activity, "Access Denied! Only Admin Allowed Make This Change",Toast.LENGTH_SHORT).show()
                                }


                            } else {
                                Toast.makeText(activity, "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

            } else if (purpose.equals("edit")) {

                if (auth.currentUser != null) {
                    if (editing) {
                        val toilet2 = captureEditTextFields()
                        toilet2.id = toilet?.id
                        toilet2.rating = toilet?.rating!!
                        toilet2.approved = "edit"
                        databaseRef.child(toilet2.id!!).setValue(toilet2)
                        editing = false

                        Toast.makeText(activity, "Toilet Edit Requested",Toast.LENGTH_SHORT).show()
                        changeFields(getString(R.string.suggest_edit), getString(R.string.delete))
                    } else {
                        editing = true
                        changeFields(getString(R.string.save), getString(R.string.cancel))
                    }

//                editToilet()
                }else {
                    Toast.makeText(activity, "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                }

            } else if (purpose.equals("new")){
                if (auth.currentUser != null) {
                    val toilet = captureEditTextFields()
                    val toiletId = databaseRef.push().key
                    if (!toiletId.isNullOrEmpty()) {
                        toilet.id = toiletId
                        toilet.approved = "new"
                        databaseRef.child(toiletId).setValue(toilet)

                        val action = ToiletFragmentDirections.actionToiletFragmentToNewToiletsFragment()
                        navController.navigate(action)
                        Toast.makeText(activity, "New Toilet Requested", Toast.LENGTH_SHORT)
                            .show()
                    }

                } else {
                    Toast.makeText(activity, "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()

                }
            }
        }
    }

    private fun changeFields(btnEditTxt: String, btnDeleteTxt: String) {
        enableOrDisableEditText()
        binding.toiletDetailContainer.fullScroll(ScrollView.FOCUS_UP)
        binding.btnEditToilet.text = btnEditTxt
        binding.btnDeleteToilet.text = btnDeleteTxt
    }

    private fun populateToiletDetails(toilet: Toilet) {
        etToiletTitle.setText(toilet.title)
        etAddress.setText(toilet.address)
        etPhone.setText(toilet.phone)
        etLatitude.setText(toilet.latitude.toString())
        etLongitude.setText(toilet.longitude.toString())
        etOperationalStatus.setText(toilet.status)
        etOpeningHours.setText(toilet.openTime)
        etClosingHours.setText(toilet.closeTime)
        etType.setText(toilet.type)
        etCharge.setText(toilet.charge)
        etExtraInfo.setText(toilet.extraInfo)
    }

    private fun captureEditTextFields(): Toilet {
        val title = etToiletTitle.text.toString()
        val address = etAddress.text.toString()
        val type = etType.text.toString()
        val status = etOperationalStatus.text.toString()
        val openingHours = etOpeningHours.text.toString()
        val closingHours = etClosingHours.text.toString()
        val charge = etCharge.text.toString()
        val latitude = etLatitude.text.toString().toDouble()
        val longitude = etLongitude.text.toString().toDouble()
        val phone = etPhone.text.toString()
        val extraInfo = etExtraInfo.text.toString()

        return Toilet(
            title,
            address,
            phone,
            latitude,
            longitude,
            type,
            openingHours,
            closingHours,
            status,
            charge,
            extraInfo
        )
    }

    private fun enableOrDisableEditText() {
        enableOrDisable(etToiletTitle)
        enableOrDisable(etAddress)
        enableOrDisable(etPhone)
        enableOrDisable(etType)
        enableOrDisable(etLatitude)
        enableOrDisable(etLongitude)
        enableOrDisable(etOpeningHours)
        enableOrDisable(etClosingHours)
        enableOrDisable(etOperationalStatus)
        enableOrDisable(etCharge)
        enableOrDisable(etExtraInfo)
    }

//    private fun isUserAdmin(context: Context){
//
//        dbListener = firebaseDb.getReference("user").addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//                for (postSnapshot: DataSnapshot in snapshot.children) {
//                    val user = postSnapshot.getValue(User::class.java) as User
//
////                    user.id = postSnapshot.key
////                    users.add(userId)
//                    if (user.role.equals("admin")){
//                        isAdmin = true
//                    }
//                }
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
//            }
//
//        })
//    }

    //    private fun getEditToilet(toiletId: String){
//        val dbListener = databaseRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (postSnapshot: DataSnapshot in snapshot.children) {
//                    val editToilet = postSnapshot.getValue(Toilet::class.java) as Toilet
//
//                    if (toiletId == editToilet.id) {
//                        toilet = editToilet
//                        toilet?.let { populateToiletDetails(it) }
//
//                    }
//
//                }
//
//            }
//            override fun onCancelled(error: DatabaseError) {
//            }
//        })
//
//    }
}