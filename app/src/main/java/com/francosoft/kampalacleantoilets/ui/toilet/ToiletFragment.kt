package com.francosoft.kampalacleantoilets.ui.toilet

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.databinding.ToiletFragmentBinding
import com.francosoft.kampalacleantoilets.databinding.ToiletsFragmentBinding

class ToiletFragment : Fragment() {

    lateinit var binding: ToiletFragmentBinding

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
        viewModel = ViewModelProvider(this).get(ToiletViewModel::class.java)

        binding = ToiletFragmentBinding.bind(view)
        val navController = Navigation.findNavController(view)

        binding.allReview.setOnClickListener {
            navController.navigate(R.id.reviewsFragment)
        }

        binding.addReview.setOnClickListener {
            navController.navigate(R.id.reviewFragment)
        }
    }
}