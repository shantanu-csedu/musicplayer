package com.simplesln.data
import com.simplesln.data.entities.PlayList
import com.simplesln.simpleplayer.R

class PlayList (name : String ,id : Long = 0, icon : Int = R.mipmap.ic_default_music){
    val name : String = name
    val icon : Int = icon
    val id : Long = id

    constructor(playListEntity : PlayList) : this(playListEntity.name,playListEntity.id) {

    }
}