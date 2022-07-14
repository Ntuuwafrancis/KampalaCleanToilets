package com.francosoft.kampalacleantoilets.ui.reviews

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.databinding.ReviewsFragmentBinding
import com.francosoft.kampalacleantoilets.databinding.ToiletsFragmentBinding

class ReviewsFragment : Fragment(){

    lateinit var binding: ReviewsFragmentBinding

    companion object {
        fun newInstance() = ReviewsFragment()
    }

    private lateinit var viewModel: ReviewsViewModel

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
        val navController = Navigation.findNavController(view)

//        binding.cvToilet.setOnClickListener {
//            navController.navigate(R.id.reviewFragment)
//        }
//
//        binding.cvToilet2.setOnClickListener {
//            navController.navigate(R.id.reviewFragment)
//        }
//
//        binding.cvToilet3.setOnClickListener {
//            navController.navigate(R.id.reviewFragment)
//        }
//
//        binding.cvToilet4.setOnClickListener {
//            navController.navigate(R.id.reviewFragment)
//        }
//
//        binding.cvToilet5.setOnClickListener {
//            navController.navigate(R.id.reviewFragment)
//        }

    }

}