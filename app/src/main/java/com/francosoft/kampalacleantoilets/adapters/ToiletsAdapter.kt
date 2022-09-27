package com.francosoft.kampalacleantoilets.adapters

import android.content.Context
import android.location.Location
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.francosoft.kampalacleantoilets.R
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.databinding.ToiletsListItemBinding


class ToiletsAdapter(private val context: Context) :
    ListAdapter<Toilet, ToiletsAdapter.ViewHolder>(DIFF_CALLBACK) {
    private lateinit var onItemClickListener: OnItemClickListener
    private var currentLocation: Location? = null
//    private var fav = false

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<Toilet> =
            object : DiffUtil.ItemCallback<Toilet>() {
                override fun areItemsTheSame(oldItem: Toilet, newItem: Toilet): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: Toilet, newItem: Toilet): Boolean {
                    return oldItem == newItem
                }

            }
    }

//    fun getFav(isFav: Boolean){
//        fav = isFav
//    }

    fun getToilet(position: Int): Toilet {
        return getItem(position)
    }

    fun setCurrentLocation(location: Location) {
        currentLocation = location
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToiletsAdapter.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ToiletsListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToiletsAdapter.ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
        holder.itemView.setOnClickListener {
            if (position != RecyclerView.NO_POSITION) {
//                onItemClickListener.onItemViewClick(getItem(position))
//                onItemClickListener.onItemEditClick(getItem(position))
                onItemClickListener.onItemClick(getItem(position))
            }
        }
    }

    inner class ViewHolder(private val binding: ToiletsListItemBinding) :
            RecyclerView.ViewHolder(binding.root), MenuItem.OnMenuItemClickListener {

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemViewClick(getToilet(position))
                }
            }

            itemView.setOnCreateContextMenuListener { menu, _, _ ->
                menu.setHeaderTitle("Select Action")
                val toiletView = menu.add(Menu.NONE, 1,1, "View Toilet")
                val editToilet = menu.add(Menu.NONE, 2,2, "Edit Toilet")

                toiletView.setOnMenuItemClickListener(this)
                editToilet.setOnMenuItemClickListener(this)
            }
        }
        fun bindTo(toilet: Toilet) {
            binding.tvTitle.text = toilet.stitle
            (toilet.openTime + " - " + toilet.closeTime).also { binding.tvOpenStatus.text = it }
            binding.tvOpStatus.text = toilet.status
            binding.ratingBar1.rating = toilet.rating.toFloat()
            binding.tvTotalRatings.text = buildString {
        append("(")
        append(toilet.totalRating.toString())
        append(")")
    }
            binding.tvType.text = toilet.type
            binding.tvAddress.text = toilet.address
            binding.tvDivision.text = toilet.division
            if (currentLocation != null) {
                binding.tvDistance.visibility = View.VISIBLE
                binding.tvDistance.text = context.getString(
                    R.string.distance_value,
                    toilet.getDistanceInMiles(currentLocation!!)
                )
            }
            if (toilet.approved != null) {
                binding.tvApproved.text = toilet.approved
            }

//                    if (fav) {
//                        binding.imgFavorite.setImageResource(com.francosoft.kampalacleantoilets.R.drawable.ic_baseline_favorite_red);
//                    } else {
//                        binding.imgFavorite.setImageResource(com.francosoft.kampalacleantoilets.R.drawable.ic_baseline_favorite_border);
//                    }

//                    binding.imgFavorite.setOnClickListener{
//                        if (fav) {
//                            binding.imgFavorite.setImageResource(com.francosoft.kampalacleantoilets.R.drawable.ic_baseline_favorite_border);
//                            setFavorites(true)
//                        } else {
//                            binding.imgFavorite.setImageResource(com.francosoft.kampalacleantoilets.R.drawable.ic_baseline_favorite_red);
//                            setFavorites(false)
//                        }
//                    }
                }

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                if (item != null) {
                    when(item.itemId){
                        1 -> onItemClickListener.onItemViewClick(getToilet(position))
                        2 -> onItemClickListener.onItemEditClick(getToilet(position))
                    }
                }
            }
            return false
        }
    }

//    private fun setFavorites(fav: Boolean): Boolean {
//        return fav
//    }

    interface OnItemClickListener {
        fun onItemClick(toilet: Toilet)

        fun onItemViewClick(toilet: Toilet)

        fun onItemEditClick(toilet: Toilet)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }


}