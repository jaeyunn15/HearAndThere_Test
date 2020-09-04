package com.example.hearandthere_test.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hearandthere_test.R
import com.example.hearandthere_test.model.response.ResAudioTrackInfoItemDto
import com.example.hearandthere_test.ui.map.MapsFragment

class MapsViewPagerAdapter (private val activity: MapsFragment, private val data : List<ResAudioTrackInfoItemDto>)
    : RecyclerView.Adapter<MapsViewPagerAdapter.PagerViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder =
        PagerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_maps_audio_content, parent, false))

    override fun onBindViewHolder(holder: MapsViewPagerAdapter.PagerViewHolder, position: Int) {
        holder.bind(data, position)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class PagerViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView){
        private val imgs : ImageView = itemView.findViewById(R.id.iv_maps_audioInfo)
        private val title : TextView = itemView.findViewById(R.id.tv_maps_audioInfo_title)
        private val num : TextView = itemView.findViewById(R.id.tv_maps_audioInfo_num)
        private val address : TextView = itemView.findViewById(R.id.tv_maps_audioInfo_address)

        fun bind(data : List<ResAudioTrackInfoItemDto>, position: Int){
            Glide.with(activity)
                .load(data[position].images?.get(0))
                .into(imgs)
            title.text = data[position].title
            num.text = (position+1).toString()
            address.text = data[position].placeAddress
        }

    }
}