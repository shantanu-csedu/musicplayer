package com.simplesln.data.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "media_playlist_data",
        foreignKeys = [
            (android.arch.persistence.room.ForeignKey(
                    entity = MediaFile::class,
                    parentColumns = arrayOf("id"),
                    childColumns = arrayOf("media_file_id"),
                    onDelete = ForeignKey.CASCADE
            )),
            (android.arch.persistence.room.ForeignKey(
                    entity = PlayList::class,
                    parentColumns = arrayOf("id"),
                    childColumns = arrayOf("media_playlist_id"),
                    onDelete = ForeignKey.CASCADE
            ))
        ],
        indices = [(Index(value = arrayOf("media_file_id","media_playlist_id"), unique = true))]
)
class PlayListData(media_file_id: Long, media_playlist_id : Long) {
    @PrimaryKey(autoGenerate = true)
    var id : Long = 0
    var media_file_id : Long = media_file_id
    var media_playlist_id : Long = media_playlist_id
}