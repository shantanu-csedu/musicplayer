package com.simplesln

import java.text.DecimalFormat

fun getProgress(current : Int, duration : Int) : Int {
    if(duration == 0) return  0
    return current * 100 / duration
}

fun formatDuration(duration : Int) : String{
    var fstring = StringBuilder()
    var decimalFormat = DecimalFormat("#00")
    var sec =(duration / 1000)
    if( (sec / 3600) > 0) {
        fstring.append(decimalFormat.format(sec / 3600))
        fstring.append(":")
        sec %= 3600
    }
    fstring.append(decimalFormat.format(sec/60))
    sec %= 60
    fstring.append(":")
    fstring.append(decimalFormat.format(sec))
    return fstring.toString()
}