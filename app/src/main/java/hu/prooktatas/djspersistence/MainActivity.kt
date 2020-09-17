package hu.prooktatas.djspersistence

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hu.prooktatas.djspersistence.adapter.JobItemClickHandler
import hu.prooktatas.djspersistence.adapter.JobListAdapter
import hu.prooktatas.djspersistence.model.JobSearchResult
import hu.prooktatas.djspersistence.persistence.TogglingTask
import hu.prooktatas.djspersistence.persistence.entity.Job
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.IOException
import java.lang.reflect.Type

class MainActivity : AppCompatActivity(), JobItemClickHandler {

    private lateinit var editText: EditText
    private lateinit var button: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var textView: TextView

    private val okHttpClient = OkHttpClient()

    private val jobListAdapter = JobListAdapter(emptyList(), this)

    private var updater: Handler? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updater = Handler(this.mainLooper)

        editText = findViewById(R.id.etPosition)
        textView = findViewById(R.id.tvNoResults)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = jobListAdapter

        button = findViewById(R.id.btnFetch)

        button.setOnClickListener {
            fetchUsingOKHttpAsyncWithUserInput()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_item_favorites -> {
                Intent(this, FavoritesActivity::class.java).let {
                    startActivity(it)
                }
            }
        }

        return true
    }

    fun consumeRawResponse(response: String) {

        val typeForDesarialization: Type = object : TypeToken<List<JobSearchResult>>() {}.type
        val list = Gson().fromJson<List<JobSearchResult>>(response, typeForDesarialization)

        list.forEach {
            Log.d(TAG, "${it.title}: ${it.location}")
        }

        updater?.post(object: Runnable {
            override fun run() {
                when(list.isEmpty()) {
                    true -> textView.visibility = View.VISIBLE
                    else -> textView.visibility = View.GONE
                }
                jobListAdapter.searchResults = list
                jobListAdapter.notifyDataSetChanged()
            }

        })

    }

    private fun fetchUsingOKHttpAsyncWithUserInput() {
        val position = editText.text.toString() ?: ""


        val urlBuilder = BASE_URL.toHttpUrlOrNull()?.newBuilder()?.also {
            it.addQueryParameter("description", position)

        }

        val fullyConfiguredUrl = urlBuilder?.let {
            it.build()
        }

        val request = fullyConfiguredUrl?.let {
            Request.Builder().url(it).build()
        }


        request?.let {

            Log.d(TAG, "request: $it")

            okHttpClient.newCall(it).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        for ((name, value) in response.headers) {
                            Log.d(TAG, "$name: $value")
                        }

                        consumeRawResponse(response.body!!.string())


                    }
                }
            })

        }

    }

    override fun toggleFavoriteState(item: JobSearchResult) {
        item.favorite = !item.favorite
        recyclerView.adapter?.notifyDataSetChanged()

        val job = Job(item)
        val togglingTask = TogglingTask(this) {
            Log.d(TAG, "We have ${it.size} item(s) in jobs table");
        }
        togglingTask.execute(job)
    }
}

const val TAG = "KZs"
const val BASE_URL = "https://jobs.github.com/positions.json?"