package com.francosoft.kampalacleantoilets.ui.review.rateapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.utilities.helpers.RateItDialogFragement

class RateAppFragment : Fragment() {

    companion object {
        fun newInstance() = RateAppFragment()
    }

    private lateinit var viewModel: RateAppViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rate_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(RateAppViewModel::class.java)

//        RateItDialogFragement.show( parentFragmentManager, "Rate App");

    }
}