package com.francosoft.kampalacleantoilets.ui.review

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.data.models.Review
import com.francosoft.kampalacleantoilets.databinding.ReviewFragmentBinding
import com.francosoft.kampalacleantoilets.utilities.helpers.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ReviewFragment : Fragment() {

    lateinit var binding: ReviewFragmentBinding
    private val args: ReviewFragmentArgs by navArgs()
    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var dbListener: ValueEventListener
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var formatedDate: String
    private var reviewRating: Double? = null

    companion object {
        fun newInstance() = ReviewFragment()
    }

//    private lateinit var viewModel: ReviewViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.review_fragment, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewModel = ViewModelProvider(this)[ReviewViewModel::class.java]

        FirebaseUtil.openFbReference("review", requireActivity())
        firebaseDb = FirebaseUtil.firebaseDatabase
        databaseRef = FirebaseUtil.databaseReference
        auth = FirebaseUtil.firebaseAuth

        binding = ReviewFragmentBinding.bind(view)
        navController = Navigation.findNavController(view)

        val date = Calendar.getInstance().time
//        val sdf = SimpleDateFormat("yyyy.MM.dd")
        val formatter = SimpleDateFormat.getDateInstance() //or use getDateInstance()
        formatedDate = formatter.format(date).toString()

        if (auth.currentUser != null) {
            binding.tvName.text = auth.currentUser?.displayName.toString()
            binding.tvReviewDate.text = formatedDate
        }

        binding.toiletUpdate.setOnClickListener {
            saveReview()
        }
    }

//    private fun saveRating() {
//        var toilet = Toilet()
//        args.toilet?.let {
//            toilet.id = it.id
//            toilet.rating = it.rating
//            toilet.title = it.title
//            toilet.type = it.type
//            toilet.approved = it.approved
//            toilet.address = it.address
//            toilet.phone = it.phone
//            toilet.latitude = it.latitude
//            toilet.longitude = it.longitude
//            toilet.openTime = it.openTime
//            toilet.closeTime = it.closeTime
//            toilet.charge = it.charge
//            toilet.extraInfo = it.extraInfo
//        }
//        var toiletRating = 0.0
//        var totalReviews = 0
//
//        dbListener = firebaseDb.getReference("review").addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//                for (postSnapshot: DataSnapshot in snapshot.children) {
//                    val review = postSnapshot.getValue(Review::class.java) as Review
//                    review.id = postSnapshot.key
//
//                    if (review.id != null){
//                        if (review.toiletId == toilet.id){
//                            toiletRating = toiletRating.plus(review.rating)
//                            totalReviews++
//                        }
//                    }
//
//                }
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
//            }
//        })
//
//        if (reviewRating != null) {
//            toilet.rating = toiletRating.div(totalReviews)
//            val toiletId: String = toilet.id.toString()
////            firebaseDb.getReference("toilet").child(toiletId).setValue(toilet)
//        }
//
//    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun saveReview() {
        if (auth.currentUser != null) {
            val review = captureEditTextFields()
            val userId = auth.currentUser!!.uid
            review.id = userId

//            val reviewId = firebaseDb.getReference("review/$userId/${args.toilet?.id}").push().key
            val reviewId = firebaseDb.getReference("review").push().key

            if (!reviewId.isNullOrEmpty()){
                review.id = reviewId
                reviewRating = review.rating
                databaseRef.child(reviewId).setValue(review)
//                saveRating()
            }

            val action = ReviewFragmentDirections.actionReviewFragmentToToiletFragment("edit", false, args.toilet)
            navController.navigate(action)
            Toast.makeText(activity, "New Review Added", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(activity, "Access Denied! Please Login To Make Changes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun captureEditTextFields(): Review {
        val review = binding.tvReview.text.toString()
        val rating = binding.ratingBar1.rating.toDouble()

        return Review(
            args.toilet?.id,
            args.userEmail,
            args.userName,
            rating,
            review,
            formatedDate,
        )
    }

}