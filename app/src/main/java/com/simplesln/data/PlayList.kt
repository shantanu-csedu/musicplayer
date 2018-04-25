package com.simplesln.data
import com.simplesln.data.entities.PlayList
import com.simplesln.simpleplayer.R

class PlayList (name : String , icon : Int = R.mipmap.ic_default_music){
    val name : String = name
    val icon : Int = icon

    constructor(playListEntity : PlayList) : this(playListEntity.name) {

    }
}