package hu.prooktatas.djspersistence.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import hu.prooktatas.djspersistence.persistence.dao.JobDAO
import hu.prooktatas.djspersistence.persistence.entity.Job

@Database(entities = [Job::class], version = 1)
abstract class DJSDatabase: RoomDatabase() {

    abstract fun jobDao(): JobDAO

    companion object {
        private var dbInstance: DJSDatabase? = null

        internal fun getDatabase(context: Context): DJSDatabase? {
            if (dbInstance == null) {
                synchronized(DJSDatabase::class.java) {
                    if (dbInstance == null) {
                        dbInstance = Room.databaseBuilder(context.applicationContext, DJSDatabase::class.java, "djs_database").build()
                    }
                }
            }

            return dbInstance
        }
    }
}