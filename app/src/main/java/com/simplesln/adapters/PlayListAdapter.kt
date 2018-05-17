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
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import com.simplesln.data.PlayList
import com.simplesln.simpleplayer.R

class PlayListAdapter(val context : Context) : RecyclerView.Adapter<PlayListAdapter.ViewHolder>() {

    val values = ArrayList<PlayList>()
    private var onItemClickListener: AdapterView.OnItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_category_library,parent,false))
    }

    override fun getItemCount(): Int {
        return values.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = values[position]
        holder.albumName.text = category.name
        holder.albumIcon.setImageResource(category.icon)

        holder.itemView.setOnClickListener(View.OnClickListener {
            onItemClickListener?.onItemClick(null,holder.itemView,position,0)
        })
    }

    fun setOnItemClickListener(onItemClickListener: AdapterView.OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    inner class ViewHolder(itemView: View ) : RecyclerView.ViewHolder(itemView){
        val albumIcon : ImageView = itemView.findViewById(R.id.icon)
        val albumName : TextView = itemView.findViewById(R.id.name)
    }
}