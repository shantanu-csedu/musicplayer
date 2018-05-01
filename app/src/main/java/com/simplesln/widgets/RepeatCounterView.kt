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