package com.francosoft.kampalacleantoilets.adapters

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.francosoft.kampalacleantoilets.data.models.Review
import com.francosoft.kampalacleantoilets.databinding.ReviewsListItemBinding

class ReviewsAdapter(private val context: Context) :
    ListAdapter<Review, ReviewsAdapter.ViewHolder>(DIFF_CALLBACK) {
    private lateinit var onItemClickListener: OnItemClickListener

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
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ReviewsAdapter.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    inner class ViewHolder(private val binding: ReviewsListItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

                fun bindTo(review: Review) {}
            }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }


}