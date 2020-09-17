package hu.prooktatas.djspersistence.persistence.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import hu.prooktatas.djspersistence.model.JobSearchResult
import java.text.SimpleDateFormat

@Entity(tableName = "jobs")
class Job() {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    var id: Long = 0

    var gitHubId: String = ""

    var type: String? = null

    var title: String = ""

    var company: String = ""

    var location: String = ""

    @ColumnInfo(name = "advertisementUrl")
    var url: String = ""

    var createdAt: Long = 0
    constructor(result: JobSearchResult): this() {
        this.gitHubId = result.id
        this.type = result.type
        this.title = result.title
        this.company = result.company
        this.location = result.location
        this.url = result.url

        val formatter = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
        createdAt = formatter.parse(result.created_at)?.let {
            it.time
        } ?: 0
    }
}