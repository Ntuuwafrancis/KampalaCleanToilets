package com.francosoft.kampalacleantoilets.ui.toilets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.adapters.ToiletsAdapter
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.databinding.ToiletsFragmentBinding
import com.francosoft.kampalacleantoilets.utilities.helpers.FirebaseUtil
import com.google.firebase.database.*

class ToiletsFragment : Fragment(), ToiletsAdapter.OnItemClickListener {

    lateinit var binding: ToiletsFragmentBinding
    lateinit var navController: NavController
    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var dbListener: ValueEventListener

    private lateinit var recyclerView: RecyclerView
    private lateinit var toiletsAdapter: ToiletsAdapter
    private var toilets: MutableList<Toilet> = mutableListOf()


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
        navController = Navigation.findNavController(view)

        binding.apply {
            this@ToiletsFragment.recyclerView = rvToilets
        }

        FirebaseUtil.openFbReference("toilet", requireActivity())
        firebaseDb = FirebaseUtil.firebaseDatabase
        databaseRef = FirebaseUtil.databaseReference

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        toiletsAdapter = ToiletsAdapter(requireContext())
        recyclerView.adapter = toiletsAdapter
        toiletsAdapter.setOnItemClickListener(this@ToiletsFragment)

        binding.fabAddToilet.setOnClickListener {
            val action = ToiletsFragmentDirections.actionToiletsFragmentToToiletFragment(true, null)
            navController.navigate(action)
        }

        setUpDbListener()
    }

    private fun setUpDbListener() {
        dbListener = databaseRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                toilets.clear()

                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val toilet = postSnapshot.getValue(Toilet::class.java) as Toilet
                    toilet.id = postSnapshot.key
//                    toilet.id?.let { databaseRef.child(it).setValue(toilet) }
                    toilets.add(toilet)
                    toiletsAdapter.notifyItemInserted(toilets.size - 1)
                }

                toiletsAdapter.submitList(toilets)

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onItemClick(toilet: Toilet) {
        val action = ToiletsFragmentDirections.actionToiletsFragmentToToiletFragment(false, toilet)
        navController.navigate(action)
    }

}