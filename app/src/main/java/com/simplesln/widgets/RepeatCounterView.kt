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

package com.simplesln.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.simplesln.simpleplayer.R

class RepeatCounterView(context: Context?, attrs: AttributeSet?) : TextView(context, attrs) {
    private var count = 1
    init {
        isClickable = true
        setBackgroundResource(R.mipmap.ic_repeat_bg)
    }
    fun toggle(){
        count++
        if(count > 3){
            count = 1
        }
        text = count.toString()
    }

    fun setCount(count : Int){
        this.count = count
        text = this.count.toString()
    }

    fun getCount(): Int{
        return count
    }
}