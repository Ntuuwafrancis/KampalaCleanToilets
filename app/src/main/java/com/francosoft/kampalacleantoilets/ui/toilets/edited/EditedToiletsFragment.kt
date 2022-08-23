package com.francosoft.kampalacleantoilets.ui.toilets.edited

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.francosoft.kampalacleantoilets.R

class EditedToiletsFragment : Fragment() {

    companion object {
        fun newInstance() = EditedToiletsFragment()
    }

    private lateinit var viewModel: EditedToiletsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edited_toilets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[EditedToiletsViewModel::class.java]
    }
}