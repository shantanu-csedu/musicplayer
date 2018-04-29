package com.simplesln.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import com.simplesln.data.Genre
import com.simplesln.interfaces.OnIMenuItemClickListener
import com.simplesln.simpleplayer.R

class GenreListAdapter(val context : Context, val menuItemClickListener: OnIMenuItemClickListener? = null) : RecyclerView.Adapter<GenreListAdapter.ViewHolder>() {

    val values = ArrayList<Genre>()
    private var onItemClickListener: AdapterView.OnItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_genre_list,parent,false))
    }

    override fun getItemCount(): Int {
        return values.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = values[position]
        holder.name.text = album.name
        holder.icon.setImageResource(album.icon)

        holder.itemView.setOnClickListener(View.OnClickListener {
            onItemClickListener?.onItemClick(null,holder.itemView,position,0)
        })

        holder.menu.setOnClickListener(View.OnClickListener {
            menuItemClickListener?.onMenuClicked(holder.menu,position)
        })
    }

    fun setOnItemClickListener(onItemClickListener: AdapterView.OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    inner class ViewHolder(itemView: View ) : RecyclerView.ViewHolder(itemView){
        val icon : ImageView = itemView.findViewById(R.id.icon)
        val name : TextView = itemView.findViewById(R.id.name)
        val menu : ImageView = itemView.findViewById(R.id.menuOverflow)
    }
}