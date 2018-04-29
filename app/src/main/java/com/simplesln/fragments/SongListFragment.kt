package com.simplesln.fragments

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.AdapterView
import com.simplesln.adapters.SongListAdapter
import com.simplesln.data.entities.MediaFile
import com.simplesln.simpleplayer.MainActivity
import com.simplesln.simpleplayer.R
import kotlinx.android.synthetic.main.fragment_song_list.*

class SongListFragment : Fragment(), AdapterView.OnItemClickListener {
    private lateinit var mAdapter : SongListAdapter
    private lateinit var listView : RecyclerView
    private var groupType = TYPE_ALBUM
    private var groupName = ""
    private var addedToNowPlayList = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mAdapter = SongListAdapter(activity!!)
        mAdapter.setOnItemClickListener(this)
        if(arguments != null) {
            if(arguments!!.get(GROUP_TYPE) != null && arguments!!.get(GROUP_NAME) != null) {
                groupType = arguments!!.getInt(GROUP_TYPE)
                groupName = arguments!!.getString(GROUP_NAME)
            }
        }
        observe()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_song_list,container,false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fragment_song_list_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_shuffle ->
                if(mAdapter.values.size > 0) {
                    val shuffledList = mAdapter.values.shuffled()
                    val addNowPlayLiveData = (activity as MainActivity).getDataProvider().addNowPlaying(shuffledList, true)
                    addNowPlayLiveData.observe(this,object : Observer<Boolean>{
                        override fun onChanged(t: Boolean?) {
                            addNowPlayLiveData.removeObserver(this)
                            (activity as MainActivity).getDataProvider().setNowPlaying(shuffledList[0].id)
                        }
                    })
                }
            R.id.menu_add_queue ->
                if(mAdapter.values.size > 0) {
                    (activity as MainActivity).getDataProvider().addNowPlaying(mAdapter.values, false)
                }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.listView)
        listView.layoutManager = LinearLayoutManager(activity)
        listView.adapter = mAdapter
    }

    private fun observe(){
        when(groupType){
            TYPE_ALBUM -> (activity as MainActivity).getDataProvider().getMediaFilesByAlbum(groupName).observe(this,mediaFileObserver)
            TYPE_ARTIST -> (activity as MainActivity).getDataProvider().getMediaFilesByArtist(groupName).observe(this,mediaFileObserver)
            TYPE_GENRE -> (activity as MainActivity).getDataProvider().getMediaFilesByGenre(groupName).observe(this,mediaFileObserver)
            TYPE_ALL -> (activity as MainActivity).getDataProvider().getMediaFiles().observe(this,mediaFileObserver)
        }
    }

    private val mediaFileObserver =  Observer<List<MediaFile>>{
        addedToNowPlayList = false
        mAdapter.values.clear()
        mAdapter.values.addAll(it!!)
        mAdapter.notifyDataSetChanged()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val mediaFile = mAdapter.values[position]
        if(!addedToNowPlayList) {
            val nowPlayingLiveData = (activity as MainActivity).getDataProvider().addNowPlaying(mAdapter.values, true)
            nowPlayingLiveData.observe(this, object : Observer<Boolean> {
                override fun onChanged(it: Boolean?) {
                    if (it == true) {
                        nowPlayingLiveData.removeObserver(this)
                        (activity as MainActivity).getDataProvider().setNowPlaying(mediaFile.id)
                        addedToNowPlayList = true
                    }
                }
            })
        }
        else{
            (activity as MainActivity).getDataProvider().setNowPlaying(mediaFile.id)
        }
    }

}
const val TYPE_ALBUM = 1
const val TYPE_ARTIST = 2
const val TYPE_GENRE = 3
const val TYPE_ALL = 4
private const val GROUP_TYPE = "group_type"
private const val GROUP_NAME = "group_name"

fun createInstance(type : Int,name : String = "") : SongListFragment{
    val bundle = Bundle()
    bundle.putInt(GROUP_TYPE,type)
    bundle.putString(GROUP_NAME,name)
    val songListFragment = SongListFragment()
    songListFragment.arguments = bundle
    return songListFragment
}