package com.francosoft.kampalacleantoilets.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.francosoft.kampalacleantoilets.databinding.ToiletsListItemBinding
import java.util.*

class ToiletsAdapter(private val context: Context) :
    ListAdapter<Toilet, ToiletsAdapter.ViewHolder>(DIFF_CALLBACK) {
    private lateinit var onItemClickListener: OnItemClickListener

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToiletsAdapter.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ToiletsListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ToiletsAdapter.ViewHolder, position: Int) {
        holder.bindTo(getItem(position))
        holder.itemView.setOnClickListener {
            if (position != RecyclerView.NO_POSITION) {
                onItemClickListener.onItemClick(getItem(position))
            }
        }
    }

    inner class ViewHolder(private val binding: ToiletsListItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

                fun bindTo(toilet: Toilet) {
                    binding.tvTitle.text = toilet.title
                    ("Open: " + toilet.openTime + " to " + toilet.closeTime).also { binding.tvOpenStatus.text = it }
                    binding.tvOpStatus.text = toilet.status
                    binding.ratingBar1.numStars = toilet.rating
                    binding.tvType.text = toilet.type
                    binding.imgFavorite.setOnClickListener{
                        addToFavorites()
                    }
                }

        private fun addToFavorites() {
            TODO("Not yet implemented")
        }

    }
    interface OnItemClickListener {
        fun onItemClick(toilet: Toilet)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }


}