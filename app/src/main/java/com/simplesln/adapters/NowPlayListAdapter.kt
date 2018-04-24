package com.simplesln.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import com.simplesln.data.entities.MediaFile
import com.simplesln.formatDuration
import com.simplesln.fragments.NowPlayingFragment
import com.simplesln.simpleplayer.R
import kotlinx.android.synthetic.main.item_now_playing.view.*

class NowPlayListAdapter(val context : Context) : RecyclerView.Adapter<NowPlayListAdapter.ViewHolder>() {
    val values = ArrayList<MediaFile>()
    private var onItemClickListener: AdapterView.OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_now_playing,parent,false))
    }

    override fun getItemCount(): Int {
        return values.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaFile = values[position]
        holder.musicName.text = mediaFile.name
        holder.musicArtist.text = mediaFile.artist
        holder.musicDuration.text = formatDuration(mediaFile.duration)

        holder.itemView.setOnClickListener(View.OnClickListener {
            onItemClickListener?.onItemClick(null,holder.itemView,position,0)
        })
    }

    fun setOnItemClickListener(onItemClickListener: AdapterView.OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val musicName : TextView
        val musicArtist : TextView
        val musicDuration : TextView
        init {
            musicName = itemView.findViewById(R.id.musicName)
            musicArtist = itemView.findViewById(R.id.musicArtist)
            musicDuration = itemView.findViewById(R.id.musicDuration)
        }
    }
}