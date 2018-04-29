package com.simplesln.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import com.simplesln.data.Category
import com.simplesln.simpleplayer.R

class CategoryListAdapter(val context : Context) : RecyclerView.Adapter<CategoryListAdapter.ViewHolder>() {

    val values = ArrayList<Category>()
    private var onItemClickListener: AdapterView.OnItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_category_library,parent,false))
    }

    override fun getItemCount(): Int {
        return values.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = values[position]
        holder.categoryName.text = category.name
        holder.categoryIcon.setImageResource(category.icon)

        holder.itemView.setOnClickListener(View.OnClickListener {
            onItemClickListener?.onItemClick(null,holder.itemView,position,0)
        })
    }

    fun setOnItemClickListener(onItemClickListener: AdapterView.OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    inner class ViewHolder(itemView: View ) : RecyclerView.ViewHolder(itemView){
        val categoryIcon : ImageView
        val categoryName : TextView

        init {
            categoryIcon = itemView.findViewById(R.id.icon)
            categoryName = itemView.findViewById(R.id.name)
        }
    }
}