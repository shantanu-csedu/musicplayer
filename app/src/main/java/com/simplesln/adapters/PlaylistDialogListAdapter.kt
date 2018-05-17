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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.simplesln.data.PlayList
import com.simplesln.simpleplayer.R

class PlaylistDialogListAdapter(context : Context) : ArrayAdapter<PlayList>(context,0) {

    class ViewHolder(textView: TextView){
        val text : TextView = textView
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var holder: ViewHolder?
        var myConvertView = convertView
        if(myConvertView == null){
            myConvertView = LayoutInflater.from(context).inflate(R.layout.simple_list_item_1,parent,false)
            holder = ViewHolder(myConvertView.findViewById(R.id.text1))
            myConvertView.tag = holder
        }
        else{
            holder = myConvertView.tag as ViewHolder?
        }
        holder?.text?.text = getItem(position).name
        return myConvertView!!
    }
}