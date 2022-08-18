package com.francosoft.kampalacleantoilets.ui.toilet

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.databinding.ToiletFragmentBinding
import com.francosoft.kampalacleantoilets.utilities.extensions.enableOrDisable
import com.francosoft.kampalacleantoilets.utilities.helpers.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

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

    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private val args: ToiletFragmentArgs by navArgs()
    private var newToilet: Boolean = false
    private lateinit var navController: NavController
    private var toilet: Toilet? = null

    companion object {
        fun newInstance() = ToiletFragment()
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

        binding.allReview.setOnClickListener {
            navController.navigate(R.id.reviewsFragment)
        }

        binding.addReview.setOnClickListener {
            navController.navigate(R.id.reviewFragment)
        }

        FirebaseUtil.openFbReference("toilet", requireActivity())
        firebaseDb = FirebaseUtil.firebaseDatabase
        databaseRef = FirebaseUtil.databaseReference
        auth = FirebaseUtil.firebaseAuth

        newToilet = args.createToilet
        toilet = args.toilet

        if (newToilet){
            changeFields(getString(R.string.save))
        } else {
            toilet?.let { populateToiletDetails(it) }
        }

        onClickSaveButton()
        onClickDeleteButton()
    }

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

    private fun saveNewToilet() {
        if (auth.currentUser != null) {
            val toilet = captureEditTextFields()
            toilet.rating = 3

            val toiletId = databaseRef.push().key
            if (!toiletId.isNullOrEmpty()){
                toilet.id = toiletId
                databaseRef.child(toiletId).setValue(toilet)
            }

            val action = ToiletFragmentDirections.actionToiletFragmentToToiletsFragment()
            navController.navigate(action)
            Toast.makeText(activity, "New Toilet Added",Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
        }

    }

    private fun editToilet() {
        if (auth.currentUser != null) {
            changeFields(getString(R.string.suggest_edit))
            val toilet = captureEditTextFields()
            toilet.id = this.toilet?.id
            toilet.rating = this.toilet?.rating!!
            toilet.id?.let { databaseRef.child(it).setValue(toilet) }
            editing = false
            Toast.makeText(activity, "Toilet Edited",Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(activity, "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()

        }

    }

    private fun onClickDeleteButton() {
        btnDelete.setOnClickListener {
            if (auth.currentUser != null) {
                toilet?.id?.let { it1 -> databaseRef.child(it1).removeValue() }

                val action = ToiletFragmentDirections.actionToiletFragmentToToiletsFragment()
                navController.navigate(action)
                Toast.makeText(activity, "Toilet Deleted",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Access Denied! Please Login To Make Changes",Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun onClickSaveButton() {

        btnEdit.setOnClickListener {
            if (!editing && !newToilet) {
                changeFields(getString(R.string.save))
                editing = true
            } else if (newToilet) {
                saveNewToilet()
            } else {
                editToilet()
            }
        }
    }

    private fun changeFields(btnEditTxt: String) {
        enableOrDisableEditText()
        binding.toiletDetailContainer.fullScroll(ScrollView.FOCUS_UP)
        binding.btnEditToilet.text = btnEditTxt
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
}