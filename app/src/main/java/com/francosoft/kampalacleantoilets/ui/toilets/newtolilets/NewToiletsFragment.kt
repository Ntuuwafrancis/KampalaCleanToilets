package com.francosoft.kampalacleantoilets.ui.toilets.newtolilets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.adapters.ToiletsAdapter
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.data.models.User
import com.francosoft.kampalacleantoilets.databinding.FragmentNewToiletsBinding
import com.francosoft.kampalacleantoilets.utilities.helpers.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class NewToiletsFragment : Fragment(), ToiletsAdapter.OnItemClickListener {
    private lateinit var binding: FragmentNewToiletsBinding
    private lateinit var navController: NavController
    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var dbListener: ValueEventListener
//    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var toiletsAdapter: ToiletsAdapter
    private var toilets: MutableList<Toilet> = mutableListOf()
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var admin: Boolean = false

    companion object {
        fun newInstance() = NewToiletsFragment()
    }

    private lateinit var viewModel: NewToiletsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_toilets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[NewToiletsViewModel::class.java]

        binding = FragmentNewToiletsBinding.bind(view)
        navController = Navigation.findNavController(view)

        binding.apply {
            this@NewToiletsFragment.recyclerView = rvToilets
        }

        FirebaseUtil.openFbReference("toilet", requireActivity())
        firebaseDb = FirebaseUtil.firebaseDatabase
        databaseRef = FirebaseUtil.databaseReference
        auth = FirebaseUtil.firebaseAuth

       isUserAdmin()

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        toiletsAdapter = ToiletsAdapter(requireContext())
        recyclerView.adapter = toiletsAdapter
        toiletsAdapter.setOnItemClickListener(this)

        binding.fabAddToilet.setOnClickListener {
//            val action = ToiletsFragmentDirections.actionToiletsFragmentToToiletFragment(true, null)
            val action = NewToiletsFragmentDirections.actionNewToiletsFragmentToMapFragment(true)
            navController.navigate(action)

        }

        setUpDbListener()
//        pullToRefresh()

    }

    private fun setUpDbListener() {

        if (auth.currentUser != null) {

            dbListener = databaseRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    toilets.clear()

                    for (postSnapshot: DataSnapshot in snapshot.children) {
                        val toilet = postSnapshot.getValue(Toilet::class.java) as Toilet
                        toilet.id = postSnapshot.key
//                    toilet.id?.let { databaseRef.child(it).setValue(toilet) }
                        if(toilet.approved.equals("new") || toilet.approved.equals("delete") || toilet.approved.equals("edit")){
                            toilets.add(toilet)
                            toiletsAdapter.notifyItemInserted(toilets.size - 1)
                        }

//                        if (isUserAdmin()){
//                            toilets.add(toilet)
//                            toiletsAdapter.notifyItemInserted(toilets.size - 1)
//                        } else {
//                            if (toilet.uid == auth.currentUser!!.uid){
//                                toilets.add(toilet)
//                                toiletsAdapter.notifyItemInserted(toilets.size - 1)
//                            }
//                        }
                    }

                toiletsAdapter.submitList(toilets)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }

            })
        }

    }

//    private fun pullToRefresh() {
//        swipeRefreshLayout.setOnRefreshListener {
//            swipeRefreshLayout.isRefreshing = false
//            if (TrackingUtility.isLocationEnabled(requireContext())) {
//                toiletsAdapter.submitList(toiletsAdapter.currentList)
//                toiletsAdapter.notifyDataSetChanged()
//            }
//        }
//    }

    private fun isUserAdmin(){
        firebaseDb.getReference("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val user = postSnapshot.getValue(User::class.java) as User

//                    user.id = postSnapshot.key
//                    users.add(userId)
                    if (user.uid == auth.currentUser?.uid) {
                        if (user.role == "admin")
                            admin = true
                    }

                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onItemClick(toilet: Toilet) {
        val action = NewToiletsFragmentDirections.actionNewToiletsFragmentToToiletFragment("approve","newToilets",admin , toilet)
        navController.navigate(action)

    }

    override fun onResume() {
        super.onResume()
        toiletsAdapter.notifyDataSetChanged()
    }
}