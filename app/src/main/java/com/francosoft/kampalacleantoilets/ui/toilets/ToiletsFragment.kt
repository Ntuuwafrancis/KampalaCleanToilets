package com.francosoft.kampalacleantoilets.ui.toilets

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.databinding.ToiletsFragmentBinding

class ToiletsFragment : Fragment() {

    lateinit var binding: ToiletsFragmentBinding

    companion object {
        fun newInstance() = ToiletsFragment()
    }

    private lateinit var viewModel: ToiletsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.toilets_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ToiletsViewModel::class.java)

        binding = ToiletsFragmentBinding.bind(view)
        val navController = Navigation.findNavController(view)

        binding.cvToilet1.setOnClickListener {
            navController.navigate(R.id.toiletFragment)
        }

        binding.cvToilet2.setOnClickListener {
            navController.navigate(R.id.toiletFragment)
        }

        binding.cvToilet3.setOnClickListener {
            navController.navigate(R.id.toiletFragment)
        }

        binding.cvToilet4.setOnClickListener {
            navController.navigate(R.id.toiletFragment)
        }

        binding.cvToilet5.setOnClickListener {
            navController.navigate(R.id.toiletFragment)
        }
    }

}