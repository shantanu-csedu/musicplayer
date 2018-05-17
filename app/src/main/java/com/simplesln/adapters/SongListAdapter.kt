/*
 * Copyright (c) 2018.  shantanu saha <shantanu.csedu@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.simplesln.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Base64
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
import java.io.ByteArrayInputStream

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
            holder.musicName.setHorizontallyScrolling(true)
            holder.musicName.isSelected = true
        }
        else{
            if(mediaFile.art.isNotEmpty()) {
                holder.musicArt.setImageBitmap(
                        BitmapFactory.decodeStream(ByteArrayInputStream(Base64.decode(mediaFile.art, Base64.DEFAULT)))
                )
            }
            else {
                holder.musicArt.setImageResource(R.mipmap.ic_default_music)
            }
            holder.musicName.setHorizontallyScrolling(false)
            holder.musicName.isSelected = false
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