package com.sedra.nearbyplacesapp.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.sedra.nearbyplacesapp.R
import com.sedra.nearbyplacesapp.data.model.Venue
import com.sedra.nearbyplacesapp.data.model.photo.PhotoItem
import com.sedra.nearbyplacesapp.databinding.AdapterPlaceItemBinding

class PlaceViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

class PlaceAdapter : ListAdapter<Venue, PlaceViewHolder>(PlaceDiffUtils()) {

    private var imageList: Array<PhotoItem?>? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AdapterPlaceItemBinding.inflate(inflater, parent, false)
        return PlaceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val itemBinding = holder.binding as AdapterPlaceItemBinding
        val context = itemBinding.root.context
        val currentItem = getItem(position)
        itemBinding.apply {
            placeName.text = currentItem.name
            placeAddress.text = currentItem.location.formattedAddress.joinToString(",")
            if (!imageList.isNullOrEmpty()) {
                if (imageList?.get(position) != null) {
                    val imageLink =
                        "${imageList?.get(position)?.prefix}500x500${imageList?.get(position)?.suffix}"
                    Glide.with(context)
                        .load(imageLink)
                        .placeholder(R.drawable.photo)
                        .into(placeImage)
                }
            }
        }
    }

    override fun onCurrentListChanged(
        previousList: MutableList<Venue>,
        currentList: MutableList<Venue>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        imageList = arrayOfNulls(currentList.size)
    }

    fun updateImageList(photoItem: PhotoItem, position: Int) {
        imageList?.set(position, photoItem)
        notifyItemChanged(position)
    }
}

class PlaceDiffUtils : DiffUtil.ItemCallback<Venue>() {
    override fun areItemsTheSame(oldItem: Venue, newItem: Venue): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Venue, newItem: Venue): Boolean {
        return oldItem == newItem
    }
}