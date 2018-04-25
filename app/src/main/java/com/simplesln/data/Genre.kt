package com.simplesln.data
import com.simplesln.simpleplayer.R

class Genre(name : String, icon : Int = R.mipmap.ic_default_music) {
    val name : String = if(name.isEmpty()) "unknown genre" else name
    val icon : Int = icon
}