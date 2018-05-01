package com.simplesln.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.simplesln.data.PlayList

class PlaylistDialogListAdapter(context : Context) : ArrayAdapter<PlayList>(context,0) {

    class ViewHolder(textView: TextView){
        val text : TextView = textView
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var holder: ViewHolder?
        var myConvertView = convertView
        if(myConvertView == null){
            myConvertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1,parent,false)
            holder = ViewHolder(myConvertView.findViewById(android.R.id.text1))
            myConvertView.tag = holder
        }
        else{
            holder = myConvertView.tag as ViewHolder?
        }
        holder?.text?.text = getItem(position).name
        return myConvertView!!
    }
}