package com.francosoft.kampalacleantoilets.ui.map

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.utilities.helpers.FirebaseUtil

class MapFragment : Fragment() {

    companion object {
        fun newInstance() = MapFragment()
    }

    private lateinit var viewModel: MapViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.map_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(MapViewModel::class.java)

    }

//    override fun onPause() {
//        super.onPause()
//        FirebaseUtil.detachListener()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        FirebaseUtil.attachListener()
//    }
}