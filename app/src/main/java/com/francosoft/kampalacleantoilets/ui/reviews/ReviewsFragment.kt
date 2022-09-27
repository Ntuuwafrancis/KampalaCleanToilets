package com.francosoft.kampalacleantoilets.ui.reviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.adapters.ReviewsAdapter
import com.francosoft.kampalacleantoilets.data.models.Review
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.databinding.ReviewsFragmentBinding
import com.francosoft.kampalacleantoilets.utilities.helpers.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ReviewsFragment : Fragment(){

    lateinit var binding: ReviewsFragmentBinding

    companion object {
        fun newInstance() = ReviewsFragment()
    }

    private lateinit var dbListener: ValueEventListener
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewsAdapter: ReviewsAdapter
    private var reviews: MutableList<Review> = mutableListOf()
    private lateinit var viewModel: ReviewsViewModel
    private lateinit var navController: NavController
    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private val args: ReviewsFragmentArgs by navArgs()
    private var toilet: Toilet? = null
    private var toiletRating: Double? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.reviews_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ReviewsViewModel::class.java)

        binding = ReviewsFragmentBinding.bind(view)
        navController = Navigation.findNavController(view)

        binding.apply {
            this@ReviewsFragment.recyclerView = rvReviews
        }

        FirebaseUtil.openFbReference("review", requireActivity())
        firebaseDb = FirebaseUtil.firebaseDatabase
        databaseRef = FirebaseUtil.databaseReference
        auth = FirebaseUtil.firebaseAuth

        toilet = args.toilet

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        reviewsAdapter = ReviewsAdapter(requireContext())
        recyclerView.adapter = reviewsAdapter
        getReviews()
    }

    private fun getReviews() {
        dbListener = databaseRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                reviews.clear()
                var reviewsTotal = 0.0

                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val review = postSnapshot.getValue(Review::class.java) as Review
                    review.id = postSnapshot.key

                    if (review.toiletId == args.toilet?.id){
                        toiletRating = toiletRating?.plus(review.rating)
                        reviews.add(review)
                        reviewsTotal += review.rating
                        if (reviews.isNotEmpty())
                            reviewsAdapter.notifyItemInserted(reviews.size - 1)
                    }
                }

                reviewsAdapter.submitList(reviews)
                reviewsAdapter.notifyDataSetChanged()
                toilet?.totalRating = reviews.size
                toilet?.let { getToiletRating(it, reviewsTotal) }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun getToiletRating(toilet: Toilet, reviewTotal: Double) {

        if (toilet.id != null) {
            val rating = toilet.totalRating
            if (rating > 0 && reviewTotal > 0.0){
                toilet.rating = reviewTotal.div(rating)
                toilet.id?.let { firebaseDb.getReference("toilet").child(it).setValue(toilet) }

//                if (activity != null) {
//                    Toast.makeText(activity,"total rating = $rating", Toast.LENGTH_SHORT ).show()
//                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        reviewsAdapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        FirebaseUtil.detachListener()
    }

}