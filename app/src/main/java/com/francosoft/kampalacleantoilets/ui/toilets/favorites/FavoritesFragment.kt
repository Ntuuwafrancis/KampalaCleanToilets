package com.francosoft.kampalacleantoilets.ui.toilets.favorites

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
import androidx.recyclerview.widget.RecyclerView
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.adapters.ToiletsAdapter
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.data.models.User
import com.francosoft.kampalacleantoilets.databinding.FavoritesFragmentBinding
import com.francosoft.kampalacleantoilets.ui.toilets.ToiletsFragmentDirections
import com.francosoft.kampalacleantoilets.utilities.helpers.FirebaseUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavoritesFragment : Fragment(), ToiletsAdapter.OnItemClickListener{
    private lateinit var binding: FavoritesFragmentBinding
    lateinit var navController: NavController
    private lateinit var databaseRef: DatabaseReference
    private lateinit var firebaseDb: FirebaseDatabase
    private lateinit var dbListener: ValueEventListener
    //    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var toiletsAdapter: ToiletsAdapter
    private var toilets: MutableList<Toilet> = mutableListOf()
    private lateinit var itemTouchHelper: ItemTouchHelper

    companion object {
        fun newInstance() = FavoritesFragment()
    }

    private lateinit var viewModel: FavoritesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.favorites_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(FavoritesViewModel::class.java)

        binding = FavoritesFragmentBinding.bind(view)
        navController = Navigation.findNavController(view)

        binding.apply {
            this@FavoritesFragment.recyclerView = listLocations
//            this@FavoritesFragment.swipeRefreshLayout = swipeLayout
        }

        FirebaseUtil.openFbReference("user", requireActivity())
        firebaseDb = FirebaseUtil.firebaseDatabase
        databaseRef = FirebaseUtil.databaseReference
        auth = FirebaseUtil.firebaseAuth

        setUpDbListener()

    }

    private fun setUpDbListener() {

        if (auth.currentUser?.uid != null) {
            val userId = auth.currentUser?.uid
            dbListener = firebaseDb.getReference("user/$userId/favorite").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    toilets.clear()

                    for (postSnapshot: DataSnapshot in snapshot.children) {
                        val toilet = postSnapshot.getValue(Toilet::class.java) as Toilet
                        toilet.id = postSnapshot.key
//                    toilet.id?.let { databaseRef.child(it).setValue(toilet) }
                        if(toilet.approved.equals("approved") ){
                            toilets.add(toilet)
                            toiletsAdapter.notifyItemInserted(toilets.size - 1)
                        }

//                    toilets.add(toilet)
//                    toiletsAdapter.notifyItemInserted(toilets.size - 1)
                    }

                toiletsAdapter.submitList(toilets)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }

            })

        }
           }

//    private fun removeFav() {
//        if (auth.currentUser != null) {
//            val userId = auth.currentUser!!.uid
////            toiletsAdapter.
//
////            Toast.makeText(activity, "Toilet Removed From Favorites", Toast.LENGTH_SHORT).show()
//        } else {
////            Toast.makeText(activity, "Access Denied! Please Login To Make Changes", Toast.LENGTH_SHORT).show()
//        }
//    }

//    private fun addFav() {
//        if (auth.currentUser != null) {
//            val userId = auth.currentUser!!.uid
//
//            Toast.makeText(activity, "Toilet added To Favorites", Toast.LENGTH_SHORT).show()
//        } else {
////            Toast.makeText(activity, "Access Denied! Please Login To Make Changes", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun isUserAdmin() : Boolean{
        var isAdmin = false
        firebaseDb.getReference("user").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (postSnapshot: DataSnapshot in snapshot.children) {
                    val user = postSnapshot.getValue(User::class.java) as User

//                    user.id = postSnapshot.key
//                    users.add(userId)
                    if (user.role.equals("admin")){
                        isAdmin = true
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }

        })
        return isAdmin
    }

    override fun onPause() {
        super.onPause()
        FirebaseUtil.detachListener()
    }

    override fun onItemClick(toilet: Toilet) {

    }

    override fun onItemViewClick(toilet: Toilet) {
        val action = ToiletsFragmentDirections.actionToiletsFragmentToToiletFragment("","favorites",false, toilet)
        navController.navigate(action)
    }

    override fun onItemEditClick(toilet: Toilet) {
        TODO("Not yet implemented")
    }
}