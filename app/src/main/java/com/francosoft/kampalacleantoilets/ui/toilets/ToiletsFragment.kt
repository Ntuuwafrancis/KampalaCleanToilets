package com.francosoft.kampalacleantoilets.ui.toilets

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.adapters.ToiletsAdapter
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.databinding.ToiletsFragmentBinding
import com.francosoft.kampalacleantoilets.utilities.extensions.onQueryTextChanged
import com.francosoft.kampalacleantoilets.utilities.helpers.FirebaseUtil
import com.francosoft.kampalacleantoilets.utilities.helpers.TrackingUtility.isLocationEnabled
import com.francosoft.kampalacleantoilets.utilities.helpers.TrackingUtility.pleaseEnableLocation
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class ToiletsFragment : Fragment(), ToiletsAdapter.OnItemClickListener {

    private lateinit var binding: ToiletsFragmentBinding
    lateinit var navController: NavController
    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var dbListener: ValueEventListener
    private lateinit var auth: FirebaseAuth
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var toiletsAdapter: ToiletsAdapter
    private lateinit var searchView: SearchView
    private var toilets: MutableList<Toilet> = mutableListOf()

    companion object {
//        fun newInstance() = ToiletsFragment()
        const val REQUEST_CODE_LOCATION_PERMISSION = 11
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
            this@ToiletsFragment.swipeRefreshLayout = swipeLayout
            this@ToiletsFragment.searchView = toiletsSearchView
        }

        FirebaseUtil.openFbReference("toilet", requireActivity())
        firebaseDb = FirebaseUtil.firebaseDatabase
        databaseRef = FirebaseUtil.databaseReference
        auth = FirebaseUtil.firebaseAuth

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity() )
        toiletsAdapter = ToiletsAdapter(requireContext())
        recyclerView.adapter = toiletsAdapter
        toiletsAdapter.setOnItemClickListener(this@ToiletsFragment)

        binding.fabAddToilet.setOnClickListener {
//            val action = ToiletsFragmentDirections.actionToiletsFragmentToToiletFragment(true, null)
            val action = ToiletsFragmentDirections.actionToiletsFragmentToMapFragment(true)
            navController.navigate(action)

        }

        pleaseEnableLocation(requireContext())
        setUpDbListener()
        getToiletsByProximity(toilets)
        pullToRefresh()
        searchToilets()
    }

    private fun searchToilets() {
        val stoilets = mutableListOf<Toilet>()
        searchView.onQueryTextChanged { text -> 
            if (text.trim().isEmpty()) {
                getToiletsByProximity(toilets)
                recyclerView.scrollToPosition(0)
            }

//            for (toilet in toilets) {
//                if((toilet.stitle?.contains(text.trim(), true) == true)
//                    || (toilet.division?.equals(text.trim(), true) == true)
//                    || (toilet.type?.equals(text.trim(), true) == true)
//                    || (toilet.status?.equals(text.trim(), true) == true)
//                ){
//                    stoilets.add(toilet)
//                }
//            }
//            toiletsAdapter.submitList(stoilets)
//            toiletsAdapter.notifyDataSetChanged()

            val mySearchQuery = databaseRef.orderByValue()
            mySearchQuery.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    stoilets.clear()
                    for (postSnapshot : DataSnapshot in snapshot.children) {
                        val toilet = postSnapshot.getValue(Toilet::class.java) as Toilet
                        toilet.id = postSnapshot.key

                        if((toilet.stitle?.contains(text, true) == true)
                            || (toilet.division?.contains(text, true) == true)
                            || (toilet.type?.contains(text, true) == true)
                            || (toilet.status?.contains(text, true) == true)
                        ){
                            stoilets.add(toilet)
                        }
                    }
                    toiletsAdapter.submitList(stoilets)
                    toiletsAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }

            })

        }
    }

    private fun pullToRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            if (isLocationEnabled(requireContext()) && toilets.isNotEmpty()) {
               getToiletsByProximity(toilets)
                searchView.setQuery("", false)
                searchView.clearFocus()
            }
        }
    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(REQUEST_CODE_LOCATION_PERMISSION)
    private fun getToiletsByProximity(toilets: MutableList<Toilet>){
        if(EasyPermissions.hasPermissions(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)){
            val fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())

            fusedLocationClient.lastLocation.addOnSuccessListener {currentLocation ->
                if (currentLocation != null && toilets.isNotEmpty()) {
                    toiletsAdapter.setCurrentLocation(currentLocation)
                    val sortedToilets = toilets.sortedBy { toilet ->
                        toilet.getDistanceInMiles(currentLocation).toDouble()
                    } as MutableList<Toilet>
                    toiletsAdapter.submitList(sortedToilets)
                    toiletsAdapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(0)
                }
            }

        } else {
            Snackbar.make(
                requireView(),
                getString(R.string.snackbar_toilets),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(getString(R.string.ok)) {
                    EasyPermissions.requestPermissions(
                        requireActivity(),
                        getString(R.string.location_rationale),
                        REQUEST_CODE_LOCATION_PERMISSION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )
                }
                .show()
        }
    }

    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun setUpDbListener() {

        dbListener = databaseRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                toilets.clear()

                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val toilet = postSnapshot.getValue(Toilet::class.java) as Toilet
                    toilet.id = postSnapshot.key
                    if(toilet.approved.equals("approved") || toilet.approved.equals("delete") ){

                        toilets.add(toilet)
                        toiletsAdapter.notifyItemInserted(toilets.size - 1)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    override fun onResume() {
        super.onResume()
        searchView.setQuery("", false)
        searchView.clearFocus()
        toiletsAdapter.notifyDataSetChanged()
    }

    override fun onItemClick(toilet: Toilet) {
//        val action = ToiletsFragmentDirections.actionToiletsFragmentToToiletFragment("edit", "toilets",false, toilet)
//        navController.navigate(action)
    }

    override fun onItemViewClick(toilet: Toilet) {
        val action = ToiletsFragmentDirections.actionToiletsFragmentToMapFragment(false, toilet )
        navController.navigate(action)
    }

    override fun onItemEditClick(toilet: Toilet) {
        val action = ToiletsFragmentDirections.actionToiletsFragmentToToiletFragment("edit", "toilets",false, toilet)
        navController.navigate(action)
    }

    override fun onPause() {
        super.onPause()
        FirebaseUtil.detachListener()
    }

}