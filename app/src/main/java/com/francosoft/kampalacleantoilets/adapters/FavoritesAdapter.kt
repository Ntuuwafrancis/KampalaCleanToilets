package com.francosoft.kampalacleantoilets.adapters
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.francosoft.kampalacleantoilets.R
//import com.francosoft.kampalacleantoilets.data.models.Favorite
//import com.francosoft.kampalacleantoilets.data.models.Review
//import com.francosoft.kampalacleantoilets.data.models.Toilet
//import com.francosoft.kampalacleantoilets.databinding.ReviewsListItemBinding
//import com.francosoft.kampalacleantoilets.databinding.ToiletsListItemBinding
//
//class FavoritesAdapter ( private val context: Context) :
//ListAdapter<Favorite, FavoritesAdapter.ViewHolder>(DIFF_CALLBACK) {
//    private lateinit var onItemClickListener: OnItemClickListener
//
//    companion object{
//        private val DIFF_CALLBACK: DiffUtil.ItemCallback<Favorite> =
//            object : DiffUtil.ItemCallback<Favorite>() {
//                override fun areItemsTheSame(oldItem: Favorite, newItem: Favorite): Boolean {
//                    return oldItem.id == newItem.id
//                }
//
//                override fun areContentsTheSame(oldItem: Favorite, newItem: Favorite): Boolean {
//                    return oldItem == newItem
//                }
//
//            }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesAdapter.ViewHolder {
//        val inflater = LayoutInflater.from(context)
//        val binding = ToiletsListItemBinding.inflate(inflater, parent, false)
//        return ViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: FavoritesAdapter.ViewHolder, position: Int) {
//        holder.bindTo(getItem(position))
//        holder.itemView.setOnClickListener {
//            if (position != RecyclerView.NO_POSITION) {
////                onItemClickListener.onItemClick(getItem(position))
//            }
//        }
//    }
//
//    inner class ViewHolder(private val binding: ToiletsListItemBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bindTo(favorite: Favorite) {
////            binding.tvTitle.text = toilet.title
////            ("Open: " + toilet.openTime + " to " + toilet.closeTime).also { binding.tvOpenStatus.text = it }
////            binding.tvOpStatus.text = toilet.status
////            binding.ratingBar1.rating = toilet.rating.toFloat()
////            binding.tvType.text = toilet.type
////            if (currentLocation != null) {
////                binding.tvDistance.visibility = View.VISIBLE
////                binding.tvDistance.text = context.getString(
////                    R.string.distance_value,
////                    toilet.getDistanceInMiles(currentLocation!!)
////                )
////            }
////            if (toilet.approved != null) {
////                binding.tvApproved.text = toilet.approved
////            }
////
////            if (fav) {
////                binding.imgFavorite.setImageResource(com.francosoft.kampalacleantoilets.R.drawable.ic_baseline_favorite_red);
////            } else {
////                binding.imgFavorite.setImageResource(com.francosoft.kampalacleantoilets.R.drawable.ic_baseline_favorite_border);
////            }
//
////                    binding.imgFavorite.setOnClickListener{
////                        if (fav) {
////                            binding.imgFavorite.setImageResource(com.francosoft.kampalacleantoilets.R.drawable.ic_baseline_favorite_border);
////                            setFavorites(true)
////                        } else {
////                            binding.imgFavorite.setImageResource(com.francosoft.kampalacleantoilets.R.drawable.ic_baseline_favorite_red);
////                            setFavorites(false)
////                        }
////                    }
//        }
//
//    }
//
////    private fun setFavorites(fav: Boolean): Boolean {
////        return fav
////    }
//
//    interface OnItemClickListener {
//        fun onItemClick(review: Review)
//    }
//
//    fun setOnItemClickListener(listener: OnItemClickListener) {
//        onItemClickListener = listener
//    }
//
//}