package com.francosoft.kampalacleantoilets.ui.tips

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.francosoft.kampalacleantoilets.R

class ToiletTipsFragment : Fragment() {

    companion object {
        fun newInstance() = ToiletTipsFragment()
    }

    private lateinit var viewModel: ToiletTipsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.toilet_tips_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ToiletTipsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}