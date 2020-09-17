package hu.prooktatas.djspersistence

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import hu.prooktatas.djspersistence.persistence.TogglingTask

class FavoritesActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        val togglingTask = TogglingTask(this) {
            Log.d(TAG, "Your favorites:")
            it.forEach { aFavorite ->
                Log.d(TAG, "$aFavorite")
            }
        }
        togglingTask.execute()
    }
}