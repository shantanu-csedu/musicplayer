package com.simplesln.interfaces

interface MediaPlayer {
    fun play()
    fun stop()
    fun next()
    fun prev()
    fun duration() : Int
    fun seek(position : Int)
    fun shortForward();
    fun longForward()
    fun shortBackward()
    fun longBackward()
}