package hu.prooktatas.djspersistence.persistence

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import hu.prooktatas.djspersistence.TAG
import hu.prooktatas.djspersistence.persistence.dao.JobDAO
import hu.prooktatas.djspersistence.persistence.entity.Job

class TogglingTask(private val context: Context, private val callback: (List<Job>) -> Unit): AsyncTask<Job, Unit, List<Job>>() {

    private var dao: JobDAO? = null

    override fun doInBackground(vararg params: Job): List<Job> {
        Log.d(TAG, "doInBackground")

        dao = DJSDatabase.getDatabase(context)?.jobDao()

        var listOfFavorites: List<Job>

        if (params.isNotEmpty()) {
            val job = params.first()

            listOfFavorites = dao?.let {
                Log.d(TAG, "Searching for record with GitHub id of ${job.gitHubId}")
                val count = dao!!.findById(job.gitHubId)

                Log.d(TAG, "findById: $count")

                if (count == 0) {
                    val generatedId = dao!!.insertJob(job)
                    job.id = generatedId
                } else {
                    Log.d(TAG, "Trying to remove: $job")
                    dao!!.deleteByGitHubId(job.gitHubId)
                }

                dao!!.fetchAll()
            } ?: emptyList()
        } else {
            listOfFavorites = dao!!.fetchAll()
        }

        val gitHubIds = listOfFavorites.map {
            it.gitHubId
        }

        Log.d(TAG, "Favorites now: $gitHubIds")

        return listOfFavorites
    }

    override fun onPostExecute(result: List<Job>?) {
        Log.d(TAG, "onPostExecute")
        result?.let {
            callback(it)
        }
    }

}