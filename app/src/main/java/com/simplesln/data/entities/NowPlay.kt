package com.simplesln.data.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "media_now_play",
        foreignKeys = [
            (ForeignKey(
                entity = MediaQueue::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("nowPlayId"),
                onDelete = ForeignKey.CASCADE
        ))
        ],
        indices = [(Index(value = arrayOf("nowPlayId"), unique = true))]
)
class NowPlay(id: Long = 0, nowPlayId : Long) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = id
    var nowPlayId : Long = nowPlayId
}