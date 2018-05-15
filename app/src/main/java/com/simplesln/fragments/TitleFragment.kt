package com.simplesln.fragments

import android.os.Bundle
import android.support.v4.app.Fragment

open class TitleFragment : Fragment() {
    private lateinit var title : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = arguments?.getString(TITLE)!!
    }

    fun getTitle() : String{
        return title
    }
}

const val TITLE = "fragment_title"