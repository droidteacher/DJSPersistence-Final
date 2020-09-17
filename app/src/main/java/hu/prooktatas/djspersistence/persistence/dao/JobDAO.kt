package hu.prooktatas.djspersistence.persistence.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import hu.prooktatas.djspersistence.persistence.entity.Job

@Dao
interface JobDAO {

    @Insert
    fun insertJob(job: Job): Long

    @Query("select * from jobs")
    fun fetchAll(): List<Job>

    @Query("select count(*) from jobs where gitHubId = :refId")
    fun findById(refId: String): Int

    @Query("select * from jobs where createdAt > :refDate")
    fun fetchNewerThan(refDate: Long): List<Job>

    @Query("delete from jobs where gitHubId = :refId")
    fun deleteByGitHubId(refId: String);

}