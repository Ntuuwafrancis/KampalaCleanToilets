package com.francosoft.kampalacleantoilets.ui.review

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.databinding.ReviewFragmentBinding
import com.francosoft.kampalacleantoilets.databinding.ReviewsFragmentBinding

class ReviewFragment : Fragment() {

    lateinit var binding: ReviewFragmentBinding

    companion object {
        fun newInstance() = ReviewFragment()
    }

    private lateinit var viewModel: ReviewViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.review_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ReviewViewModel::class.java)


        binding = ReviewFragmentBinding.bind(view)
        val navController = Navigation.findNavController(view)

//        binding.toiletUpdate.setOnClickListener {
//            navController.navigate(R.id.toiletFragment)
//        }
    }
}