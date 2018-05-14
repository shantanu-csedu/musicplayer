package com.simplesln.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import com.simplesln.data.MediaFile
import com.simplesln.formatDuration
import com.simplesln.interfaces.OnIMenuItemClickListener
import com.simplesln.simpleplayer.R

class SongListAdapter(val context : Context,private val menuItemClickListener: OnIMenuItemClickListener? = null) : RecyclerView.Adapter<SongListAdapter.ViewHolder>() {
    val values = ArrayList<MediaFile>()
    private var onItemClickListener: AdapterView.OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_song_list,parent,false))
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
            onItemClickListener?.onItemClick(null,holder.overflowMenu,position,0)
        })
        holder.overflowMenu.setOnClickListener(View.OnClickListener {
            menuItemClickListener?.onMenuClicked(holder.overflowMenu,position)
        })
        if(mediaFile.playing){
            holder.musicArt.setImageResource(R.mipmap.ic_album)
        }
        else{
            holder.musicArt.setImageResource(R.mipmap.ic_default_music)
        }
    }

    fun setOnItemClickListener(onItemClickListener: AdapterView.OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val musicName : TextView = itemView.findViewById(R.id.musicName)
        val musicArtist : TextView = itemView.findViewById(R.id.musicArtist)
        val musicDuration : TextView = itemView.findViewById(R.id.musicDuration)
        val overflowMenu : ImageView = itemView.findViewById(R.id.menuOverflow)
        val musicArt : ImageView = itemView.findViewById(R.id.musicArt)
    }
}