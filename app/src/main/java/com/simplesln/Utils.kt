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