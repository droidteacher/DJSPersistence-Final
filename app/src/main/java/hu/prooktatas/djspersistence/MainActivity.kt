package hu.prooktatas.djspersistence

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
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
    }
}

const val TAG = "KZs"
const val BASE_URL = "https://jobs.github.com/positions.json?"