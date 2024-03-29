package com.francosoft.kampalacleantoilets.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.databinding.AboutFragmentBinding

class AboutFragment : Fragment() {
    private lateinit var binding: AboutFragmentBinding

    companion object {
        fun newInstance() = AboutFragment()
    }

    private lateinit var viewModel: AboutViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.about_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AboutViewModel::class.java]
        binding = AboutFragmentBinding.bind(view)
        val navController = Navigation.findNavController(view)

        binding.btnTerms.setOnClickListener {
            val action = AboutFragmentDirections.actionAboutFragment2ToTermsFragment()
            navController.navigate(action)
        }

    }
}