package com.simplesln.adapters

import android.content.Context
import android.os.Build
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import com.simplesln.adapters.helper.ItemTouchHelperViewHolder
import com.simplesln.data.MediaFile
import com.simplesln.formatDuration
import com.simplesln.interfaces.DataProvider
import com.simplesln.interfaces.OnIMenuItemClickListener
import com.simplesln.simpleplayer.R
import com.simplesln.simpleplayer.getDataProvider
import com.simplesln.widgets.RepeatCounterView

class SongListAdapter(val context : Context) : RecyclerView.Adapter<SongListAdapter.ViewHolder>() {
    private val dataProvider : DataProvider = getDataProvider(context)
    val values = ArrayList<MediaFile>()
    var moveToIndex = -1

    private var onItemClickListener: AdapterView.OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_song_list,parent,false))
    }

    override fun getItemCount(): Int {
        return values.size
    }

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        val prev = values[fromPosition]
        values.remove(values[fromPosition])
        values.add(toPosition,prev)
        notifyItemMoved(fromPosition, toPosition)
        moveToIndex = toPosition
    }

    fun onItemDismiss(position: Int) {
        values.remove(values[position])
        notifyItemRemoved(position)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaFile = values[position]
        holder.musicName.text = mediaFile.name
        holder.musicArtist.text = mediaFile.artist
        holder.musicDuration.text = formatDuration(mediaFile.duration)
        holder.repeatCounter.setCount(mediaFile.repeatCount)

        holder.itemView.setOnClickListener(View.OnClickListener {
            onItemClickListener?.onItemClick(null,holder.repeatCounter,position,0)
        })

        if(mediaFile.playing){
            holder.musicArt.setImageResource(R.mipmap.ic_album)
        }
        else{
            holder.musicArt.setImageResource(R.mipmap.ic_default_music)
        }

        holder.repeatCounter.setOnClickListener {
            holder.repeatCounter.toggle()
            mediaFile.repeatCount = holder.repeatCounter.getCount()
            dataProvider.updateMediaFile(mediaFile.getEntity())
        }
    }

    fun setOnItemClickListener(onItemClickListener: AdapterView.OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) , ItemTouchHelperViewHolder {
        override fun onItemSelected() {
            if(Build.VERSION.SDK_INT >= 21) {
                val cardView = (itemView as CardView)
                cardView.translationZ = 10f
                cardView.invalidate()
            }
        }

        override fun onItemClear() {
            if(Build.VERSION.SDK_INT >= 21) {
                val cardView = (itemView as CardView)
                cardView.translationZ = 0f
                cardView.invalidate()
            }
        }

        val musicName : TextView = itemView.findViewById(R.id.musicName)
        val musicArtist : TextView = itemView.findViewById(R.id.musicArtist)
        val musicDuration : TextView = itemView.findViewById(R.id.musicDuration)
        val repeatCounter : RepeatCounterView = itemView.findViewById(R.id.repeatCounter)
        val musicArt : ImageView = itemView.findViewById(R.id.musicArt)
    }
}