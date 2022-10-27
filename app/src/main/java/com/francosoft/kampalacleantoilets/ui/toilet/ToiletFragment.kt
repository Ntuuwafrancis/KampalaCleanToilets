package com.francosoft.kampalacleantoilets.ui.toilet

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.databinding.ToiletFragmentBinding
import com.francosoft.kampalacleantoilets.utilities.extensions.enableOrDisable
import com.francosoft.kampalacleantoilets.utilities.extensions.enableOrDisableSp
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
    private lateinit var etDivision: EditText
    private lateinit var spType: Spinner
    private lateinit var spTypeAdapter: ArrayAdapter<CharSequence>
    private lateinit var spStatusAdapter: ArrayAdapter<CharSequence>
//    private lateinit var etType: EditText
    private lateinit var etOpeningHours: EditText
    private lateinit var etClosingHours: EditText
    private lateinit var spOpStatus: Spinner
//    private lateinit var etOperationalStatus: EditText
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
    private lateinit var fragment: String

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
            this@ToiletFragment.spType = spType
            this@ToiletFragment.etOpeningHours = openingHours
            this@ToiletFragment.etClosingHours = closingHours
            this@ToiletFragment.spOpStatus = spStatus
            this@ToiletFragment.etCharge = charge
            this@ToiletFragment.etExtraInfo = extraInfo
            this@ToiletFragment.etDivision = division
            this@ToiletFragment.btnEdit = btnEditToilet
            this@ToiletFragment.btnDelete = btnDeleteToilet
        }

        setUpSpinner()
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

    private fun setUpSpinner() {
        spTypeAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.type,
            android.R.layout.simple_spinner_item
        ).also {adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spType.adapter = adapter
        }

        spStatusAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.status,
            android.R.layout.simple_spinner_item
        ).also {adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spOpStatus.adapter = adapter
        }
    }

    private fun initializeSetup(){
        isAdmin = args.isAdmin
        toilet = args.toilet
        purpose = args.approveToilet
        fragment = args.fragment
        spType.isEnabled = false
        spOpStatus.isEnabled = false

        if (purpose.equals("approve")){
            binding.tvLatestReviews.visibility = View.INVISIBLE
            binding.allReview.visibility = View.INVISIBLE
            binding.addReview.visibility = View.INVISIBLE
            changeFields("Approve", "Reject")
            toilet?.let { populateToiletDetails(it) }
            approveTvText(true)
        } else if (purpose.equals("edit")) {
            binding.tvLatestReviews.visibility = View.VISIBLE
            binding.allReview.visibility = View.VISIBLE
            binding.addReview.visibility = View.VISIBLE
            toilet?.let { populateToiletDetails(it) }
        } else if (purpose.equals("new")){
            binding.tvLatestReviews.visibility = View.INVISIBLE
            binding.allReview.visibility = View.INVISIBLE
            binding.addReview.visibility = View.INVISIBLE
            changeFields(getString(R.string.save), getString(R.string.cancel))
            toilet?.let { populateToiletDetails(it) }
        }

    }

    private fun approveTvText(isApprove: Boolean) {
        if (isApprove) {
            var builder1: SpannableStringBuilder
            binding.tvPurpose.visibility = View.VISIBLE
            when(toilet?.approved) {
                "delete" -> {
                    builder1 = setPurposeLabel(getString(R.string.delete_tv), Color.RED)
                    binding.tvPurpose.text = builder1
                }
                "edit" -> {
                    builder1 = setPurposeLabel(getString(R.string.edit_tv), Color.BLUE)
                    binding.tvPurpose.text = builder1
                }
                "new" -> {
                    builder1 = setPurposeLabel(getString(R.string.new_tv), Color.GREEN)
                    binding.tvPurpose.text = builder1
                }
            }
        } else {
            binding.tvPurpose.visibility = View.GONE
        }

    }

    @NonNull
    private fun  setPurposeLabel(text: String, @ColorInt color: Int) : SpannableStringBuilder {
        val builder = SpannableStringBuilder()
        val start = builder.length
        builder.append(text)
        val end = builder.length
        builder.setSpan( ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder
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

                        when(fragment){
                            "map" -> {
                                val navOptions =  NavOptions.Builder().setPopUpTo(R.id.mapFragment, true).build()
                                navController.navigate(R.id.action_toiletFragment_to_mapFragment, null, navOptions)
                            }
                            "toilets" -> {
                                val navOptions =  NavOptions.Builder().setPopUpTo(R.id.toiletsFragment, true).build()
                                navController.navigate(R.id.action_toiletFragment_to_toiletsFragment, null, navOptions)
                            }
                            "newToilets" -> {
                                val navOptions =  NavOptions.Builder().setPopUpTo(R.id.newToiletsFragment, true).build()
                                navController.navigate(R.id.action_toiletFragment_to_newToiletsFragment, null, navOptions)
                            }
                        }

//                        navController.navigate(R.id.newToiletsFragment)
//                        val action = ToiletFragmentDirections.actionToiletFragmentToNewToiletsFragment()
//                        navController.navigate(action)
                        Toast.makeText(requireContext(), "Toilet Delete Requested",Toast.LENGTH_SHORT).show()
                    } else {
                        editing = false
                        Toast.makeText(requireContext(), "Toilet Edit Cancelled",Toast.LENGTH_SHORT).show()
                        changeFields(getString(R.string.suggest_edit), getString(R.string.delete))
                    }

                } else {
                    Toast.makeText(requireContext(), "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                }
            } else if(purpose.equals("approve")) {
                if (toilet != null){
                    when(toilet?.approved){
                        "delete" ->{
                            if (auth.currentUser != null) {

                                if (isAdmin) {
                                    toilet?.approved = "approved"
                                    toilet?.id?.let { databaseRef.child(it).setValue(toilet) }
                                    Toast.makeText(requireContext(), "Toilet Delete Rejected",Toast.LENGTH_SHORT).show()

                                    when(fragment){
                                        "map" -> {
                                            val navOptions =  NavOptions.Builder().setPopUpTo(R.id.mapFragment, true).build()
                                            navController.navigate(R.id.action_toiletFragment_to_mapFragment, null, navOptions)
                                        }
                                        "toilets" -> {
                                            val navOptions =  NavOptions.Builder().setPopUpTo(R.id.toiletsFragment, true).build()
                                            navController.navigate(R.id.action_toiletFragment_to_toiletsFragment, null, navOptions)
                                        }
                                        "newToilets" -> {
                                            val navOptions =  NavOptions.Builder().setPopUpTo(R.id.newToiletsFragment, true).build()
                                            navController.navigate(R.id.action_toiletFragment_to_newToiletsFragment, null, navOptions)
                                        }
                                    }
                                    approveTvText(false)
//                                    val action = ToiletFragmentDirections.actionToiletFragmentToNewToiletsFragment()
//                                    val navOptions =  NavOptions.Builder().setPopUpTo(R.id.newToiletsFragment, true).build()
//                                    navController.navigate(R.id.action_toiletFragment_to_newToiletsFragment, null, navOptions)
                                } else {
                                    Toast.makeText(requireContext(), "Access Denied! Only Admin Allowed Make This Change",Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                            }
                        }
                        "edit" ->{
                            if (auth.currentUser != null) {

                                if (isAdmin) {
                                    toilet?.approved = "approved"

                                    toilet?.id?.let { firebaseDb.getReference("editedToilet").child(it).removeValue() }
//                                    toilet?.id?.let { databaseRef.child(it).setValue(toilet) }
                                    Toast.makeText(requireContext(), "Toilet Edit Rejected",Toast.LENGTH_SHORT).show()

                                    when(fragment){
                                        "map" -> {
                                            val navOptions =  NavOptions.Builder().setPopUpTo(R.id.mapFragment, true).build()
                                            navController.navigate(R.id.action_toiletFragment_to_mapFragment, null, navOptions)
                                        }
                                        "toilets" -> {
                                            val navOptions =  NavOptions.Builder().setPopUpTo(R.id.toiletsFragment, true).build()
                                            navController.navigate(R.id.action_toiletFragment_to_toiletsFragment, null, navOptions)
                                        }
                                        "newToilets" -> {
                                            val navOptions =  NavOptions.Builder().setPopUpTo(R.id.newToiletsFragment, true).build()
                                            navController.navigate(R.id.action_toiletFragment_to_newToiletsFragment, null, navOptions)
                                        }
                                    }
                                    approveTvText(false)
//                                    val navOptions =  NavOptions.Builder().setPopUpTo(R.id.toiletsFragment, true).build()
//                                    navController.navigate(R.id.action_toiletFragment_to_toiletsFragment, null, navOptions)
//                                    val action = ToiletFragmentDirections.actionToiletFragmentToNewToiletsFragment()
//                                    navController.navigate(action)
                                } else {
                                    Toast.makeText(requireContext(), "Access Denied! Only Admin Allowed Make This Change",Toast.LENGTH_SHORT).show()
                                }

                            } else {
                                Toast.makeText(requireContext(), "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                            }
                        }
                        "new" -> {
                            if (auth.currentUser != null) {

                                if (isAdmin) {
                                    toilet?.id?.let { it1 -> databaseRef.child(it1).removeValue() }
                                    Toast.makeText(requireContext(), "New Toilet Rejected",Toast.LENGTH_SHORT).show()

                                    when(fragment){
                                        "map" -> {
                                            val navOptions =  NavOptions.Builder().setPopUpTo(R.id.mapFragment, true).build()
                                            navController.navigate(R.id.action_toiletFragment_to_mapFragment, null, navOptions)
                                        }
                                        "toilets" -> {
                                            val navOptions =  NavOptions.Builder().setPopUpTo(R.id.toiletsFragment, true).build()
                                            navController.navigate(R.id.action_toiletFragment_to_toiletsFragment, null, navOptions)
                                        }
                                        "newToilets" -> {
                                            val navOptions =  NavOptions.Builder().setPopUpTo(R.id.newToiletsFragment, true).build()
                                            navController.navigate(R.id.action_toiletFragment_to_newToiletsFragment, null, navOptions)
                                        }
                                    }
                                    approveTvText(false)
//                                    val navOptions =  NavOptions.Builder().setPopUpTo(R.id.newToiletsFragment, true).build()
//                                    navController.navigate(R.id.action_toiletFragment_to_newToiletsFragment, null, navOptions)
//                                    val action = ToiletFragmentDirections.actionToiletFragmentToNewToiletsFragment()
//                                    navController.navigate(action)
                                } else {
                                    Toast.makeText(requireContext(), "Access Denied! Only Admin Allowed Make This Change",Toast.LENGTH_SHORT).show()
                                }

                            } else {
                                Toast.makeText(requireContext(), "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else if (purpose.equals("new")) {
                val navOptions =  NavOptions.Builder().setPopUpTo(R.id.mapFragment, true).build()
                navController.navigate(R.id.action_toiletFragment_to_mapFragment, null, navOptions)
                Toast.makeText(requireContext(), "Toilet Creation Cancelled",Toast.LENGTH_SHORT).show()
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

                                    when(fragment){
                                        "map" -> {
                                            val navOptions =  NavOptions.Builder().setPopUpTo(R.id.mapFragment, true).build()
                                            navController.navigate(R.id.action_toiletFragment_to_mapFragment, null, navOptions)
                                        }
                                        "toilets" -> {
                                            val navOptions =  NavOptions.Builder().setPopUpTo(R.id.toiletsFragment, true).build()
                                            navController.navigate(R.id.action_toiletFragment_to_toiletsFragment, null, navOptions)
                                        }
                                        "newToilets" -> {
                                            val navOptions =  NavOptions.Builder().setPopUpTo(R.id.newToiletsFragment, true).build()
                                            navController.navigate(R.id.action_toiletFragment_to_newToiletsFragment, null, navOptions)
                                        }
                                    }
                                    approveTvText(false)
//                                    val navOptions =  NavOptions.Builder().setPopUpTo(R.id.newToiletsFragment, true).build()
//                                    navController.navigate(R.id.action_toiletFragment_to_newToiletsFragment, null, navOptions)
//                                    val action = ToiletFragmentDirections.actionToiletFragmentToNewToiletsFragment()
//                                    navController.navigate(action)
                                    Toast.makeText(requireContext(), "Toilet Removed",Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), "Access Denied! Only Admin Allowed Make This Change",Toast.LENGTH_SHORT).show()
                                }

                            } else {
                                Toast.makeText(requireContext(), "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                            }
                        }
                        "edit" ->{
                            if (auth.currentUser != null) {
                                if (isAdmin) {
                                    if (validateInput()){
                                        val toilet2 = captureEditTextFields()
                                        toilet2.id = toilet?.id
                                        toilet2.rating = toilet?.rating!!
                                        toilet2.ratingTotal = toilet?.ratingTotal!!
                                        toilet2.totalRating = toilet?.totalRating!!
                                        toilet2.approved = "approved"
                                        if (toilet2.id != null) {
                                            databaseRef.child(toilet2.id!!).setValue(toilet2)
                                            firebaseDb.getReference("editedToilet").child(toilet2.id!!).removeValue()
                                        }
                                        changeFields(getString(R.string.suggest_edit), getString(R.string.delete))
                                        purpose = "edit"
                                        when(fragment){
                                            "map" -> {
                                                val navOptions =  NavOptions.Builder().setPopUpTo(R.id.mapFragment, true).build()
                                                navController.navigate(R.id.action_toiletFragment_to_mapFragment, null, navOptions)
                                            }
                                            "toilets" -> {
                                                val navOptions =  NavOptions.Builder().setPopUpTo(R.id.toiletsFragment, true).build()
                                                navController.navigate(R.id.action_toiletFragment_to_toiletsFragment, null, navOptions)
                                            }
                                            "newToilets" -> {
                                                val navOptions =  NavOptions.Builder().setPopUpTo(R.id.newToiletsFragment, true).build()
                                                navController.navigate(R.id.action_toiletFragment_to_newToiletsFragment, null, navOptions)
                                            }
                                        }
                                        approveTvText(false)
                                        Toast.makeText(requireContext(), "Toilet Edit Approved",Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(requireContext(), "Please fill in all the required fields!",Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "Access Denied! Only Admin Allowed Make This Change",Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                            }
                        }
                        "new" -> {
                            if (auth.currentUser != null) {
                                if (isAdmin) {
                                    if (validateInput()){
                                        val toilet2 = captureEditTextFields()
                                        toilet2.id = toilet?.id
                                        toilet2.rating = toilet?.rating!!
                                        toilet2.ratingTotal = toilet?.ratingTotal!!
                                        toilet2.totalRating = toilet?.totalRating!!
                                        toilet2.approved = "approved"
                                        databaseRef.child(toilet2.id!!).setValue(toilet2)
                                        changeFields(getString(R.string.suggest_edit), getString(R.string.delete))
                                        purpose = "edit"
                                        approveTvText(false)

                                        Toast.makeText(requireContext(), "Toilet Approved",Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(requireContext(), "Please fill in all the required fields!",Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "Access Denied! Only Admin Allowed Make This Change",Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

            } else if (purpose.equals("edit")) {
                approveTvText(false)
                if (auth.currentUser != null) {
                    if (editing) {
                        if (validateInput()){
                            val toilet2 = captureEditTextFields()
                            toilet2.id = toilet?.id
                            toilet2.rating = toilet?.rating!!
                            toilet2.ratingTotal = toilet?.ratingTotal!!
                            toilet2.totalRating = toilet?.totalRating!!
                            toilet2.approved = "edit"
                            saveEditedToilet(toilet2)
//                        databaseRef.child(toilet2.id!!).setValue(toilet2)
                            editing = false

                            when(fragment){
                                "map" -> {
                                    val navOptions =  NavOptions.Builder().setPopUpTo(R.id.mapFragment, true).build()
                                    navController.navigate(R.id.action_toiletFragment_to_mapFragment, null, navOptions)
                                }
                                "toilets" -> {
                                    val navOptions =  NavOptions.Builder().setPopUpTo(R.id.toiletsFragment, true).build()
                                    navController.navigate(R.id.action_toiletFragment_to_toiletsFragment, null, navOptions)
                                }
                                "newToilets" -> {
                                    val navOptions =  NavOptions.Builder().setPopUpTo(R.id.newToiletsFragment, true).build()
                                    navController.navigate(R.id.action_toiletFragment_to_newToiletsFragment, null, navOptions)
                                }
                            }

                            Toast.makeText(requireActivity(), "Toilet Edit Requested",Toast.LENGTH_SHORT).show()
//                        changeFields(getString(R.string.suggest_edit), getString(R.string.delete))
                        } else {
                            Toast.makeText(requireContext(), "Please fill in all the required fields!",Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        editing = true
                        changeFields(getString(R.string.save), getString(R.string.cancel))
                    }
                }else {
                    Toast.makeText(requireContext(), "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                }

            } else if (purpose.equals("new")){
                approveTvText(false)
                if (auth.currentUser != null) {
                    if (validateInput()){
                        val toilet = captureEditTextFields()
//                    val toiletId = databaseRef.push().key
//                    toilet.id = toiletId
                        toilet.approved = "new"
                        toilet.rating = 0.0
                        toilet.totalRating = 0
                        toilet.ratingTotal = 0.0
                        if(toilet.charge?.isEmpty() == true)
                            toilet.charge = "0"
                        saveToilet(toilet)
                        when(fragment){
                            "map" -> {
                                val navOptions =  NavOptions.Builder().setPopUpTo(R.id.mapFragment, true).build()
                                navController.navigate(R.id.action_toiletFragment_to_mapFragment, null, navOptions)
                            }
                            "toilets" -> {
                                val navOptions =  NavOptions.Builder().setPopUpTo(R.id.toiletsFragment, true).build()
                                navController.navigate(R.id.action_toiletFragment_to_toiletsFragment, null, navOptions)
                            }
                            "newToilets" -> {
                                val navOptions =  NavOptions.Builder().setPopUpTo(R.id.newToiletsFragment, true).build()
                                navController.navigate(R.id.action_toiletFragment_to_newToiletsFragment, null, navOptions)
                            }
                        }
                        Toast.makeText(requireContext(), "New Toilet Requested", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(requireContext(), "Please fill in all the required fields!",Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveEditedToilet(editedToilet: Toilet) {
        if (editedToilet.id != null ) {
            firebaseDb.getReference("editedToilet/"+ editedToilet.id).setValue(editedToilet)
        } else {
            val toiletId = firebaseDb.getReference("editedToilet").push().key
            if (toiletId != null) {
                editedToilet.id = toiletId
                firebaseDb.getReference("editedToilet/"+ editedToilet.id).setValue(editedToilet)
            }
        }
    }

    private fun saveToilet(toilet: Toilet) {
        if (toilet.id != null) {
            databaseRef.child(toilet.id!!).setValue(toilet)
        } else {
            val toiletId = databaseRef.push().key
            if (toiletId != null) {
                toilet.id = toiletId
                databaseRef.child(toilet.id!!).setValue(toilet)
            }
        }
    }

    // Checking if the input in form is valid
    private fun validateInput(): Boolean {
        val lat = etLatitude.text.toString().toDouble()
        val long = etLongitude.text.toString().toDouble()

        if (etToiletTitle.text.toString().trim() == "") {
            etToiletTitle.error = "Please Enter Toilet Title"
            return false
        }
        if (etAddress.text.toString().trim() == "") {
            etAddress.error = "Please Enter Toilet Address"
            return false
        }
        if (etDivision.text.toString().trim() == "") {
            etDivision.error = "Please Enter Toilet Division"
            return false
        }


        // checking the proper latitude and longitude format
        if(lat < -90 || lat > 90)
        {
            etLatitude.error = "Invalid Latitude"
//            Toast.makeText(requireContext(), "Invalid Latitude", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(long < -180 || long > 180)
        {
            etLongitude.error = "Invalid Longitude"
//            Toast.makeText(requireContext(), "Invalid Longitude", Toast.LENGTH_SHORT).show();

            return false;
        }

        return true
    }

    private fun changeFields(btnEditTxt: String, btnDeleteTxt: String) {
        enableOrDisableEditText()
        binding.toiletDetailContainer.fullScroll(ScrollView.FOCUS_UP)
        binding.btnEditToilet.text = btnEditTxt
        binding.btnDeleteToilet.text = btnDeleteTxt
    }

    private fun populateToiletDetails(toilet: Toilet) {
        etToiletTitle.setText(toilet.stitle)
        etAddress.setText(toilet.address)
        etDivision.setText(toilet.division)
        etPhone.setText(toilet.phone)
        etLatitude.setText(toilet.latitude.toString())
        etLongitude.setText(toilet.longitude.toString())

        when(toilet.type.toString()) {
            "public" -> {
                val type = "public"
                val spTypePosition = spTypeAdapter.getPosition(type)
                spType.setSelection(spTypePosition)
            }
            "private" -> {
                val type = "private"
                val spTypePosition = spTypeAdapter.getPosition(type)
                spType.setSelection(spTypePosition)
            }
        }

        when(toilet.status) {
            "operating" -> {
                val status = "operating"
                val spStatusPosition = spStatusAdapter.getPosition(status)
                spOpStatus.setSelection(spStatusPosition)
            }
            "under repair" -> {
                val status = "under repair"
                val spStatusPosition = spStatusAdapter.getPosition(status)
                spOpStatus.setSelection(spStatusPosition)
            }
            "closed" -> {
                val status = "closed"
                val spStatusPosition = spStatusAdapter.getPosition(status)
                spOpStatus.setSelection(spStatusPosition)
            }
        }
//        etOperationalStatus.setText(toilet.status)
        etOpeningHours.setText(toilet.openTime)
        etClosingHours.setText(toilet.closeTime)
//        etType.setText(toilet.type)
        etCharge.setText(toilet.charge)
        etExtraInfo.setText(toilet.extraInfo)
    }

    private fun captureEditTextFields(): Toilet {
        val title = etToiletTitle.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val type = spType.selectedItem.toString().trim()
        val status = spOpStatus.selectedItem.toString().trim()
        val openingHours = etOpeningHours.text.toString().trim()
        val closingHours = etClosingHours.text.toString().trim()
        val charge = etCharge.text.toString().trim()
        val latitude = etLatitude.text.toString().toDouble()
        val longitude = etLongitude.text.toString().toDouble()
        val phone = etPhone.text.toString().trim()
        val extraInfo = etExtraInfo.text.toString().trim()
        val division = etDivision.text.toString().trim()

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
            extraInfo,
            division,
        )
    }

    private fun enableOrDisableEditText() {
        enableOrDisable(etToiletTitle)
        enableOrDisable(etAddress)
        enableOrDisable(etDivision)
        enableOrDisable(etPhone)
        enableOrDisableSp(spType)
        enableOrDisable(etLatitude)
        enableOrDisable(etLongitude)
        enableOrDisable(etOpeningHours)
        enableOrDisable(etClosingHours)
        enableOrDisableSp(spOpStatus)
        enableOrDisable(etCharge)
        enableOrDisable(etExtraInfo)
    }

    override fun onPause() {
        super.onPause()
        FirebaseUtil.detachListener()
    }

}