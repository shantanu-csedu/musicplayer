package com.simplesln.data.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "media_queue",
        foreignKeys = [
            ForeignKey(
                    entity = MediaFile::class,
                    parentColumns = arrayOf("id"),
                    childColumns = arrayOf("media_file_id"),
                    onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [Index(value = arrayOf("media_file_id"), unique = true)]
)
class MediaQueue(media_file_id: Long, rank : Double) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var media_file_id: Long = media_file_id
    var rank: Double = rank
    var timestamp: Long = System.currentTimeMillis()
}