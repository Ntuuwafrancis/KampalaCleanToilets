package com.francosoft.kampalacleantoilets.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.francosoft.kampalacleantoilets.data.models.Review
import com.francosoft.kampalacleantoilets.databinding.ReviewsListItemBinding
import com.francosoft.kampalacleantoilets.databinding.ToiletsListItemBinding

class ReviewsAdapter(private val context: Context) :
    ListAdapter<Review, ReviewsAdapter.ViewHolder>(DIFF_CALLBACK) {
//    private lateinit var onItemClickListener: OnItemClickListener

    companion object{
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<Review> =
            object : DiffUtil.ItemCallback<Review>() {
                override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
                    return oldItem == newItem
                }

            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewsAdapter.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ReviewsListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewsAdapter.ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
        holder.itemView.setOnClickListener {
            if (position != RecyclerView.NO_POSITION) {
//                onItemClickListener.onItemClick(getItem(position))
            }
        }
    }

    inner class ViewHolder(private val binding: ReviewsListItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

                fun bindTo(review: Review) {
                    binding.tvReview.text = review.review
                    binding.tvReviewDate.text = review.date
                    binding.tvUserName.text = review.userName
                    binding.ratingBar.rating = review.rating.toFloat()
                }
            }

//    interface OnItemClickListener {
//        fun onItemClick(review: Review)
//    }

//    fun setOnItemClickListener(listener: OnItemClickListener) {
//        onItemClickListener = listener
//    }

}