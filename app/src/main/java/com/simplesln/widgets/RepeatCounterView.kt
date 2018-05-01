package com.simplesln.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

class RepeatCounterView(context: Context?, attrs: AttributeSet?) : TextView(context, attrs) {
    private var count = 1
    init {
        isClickable = true
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