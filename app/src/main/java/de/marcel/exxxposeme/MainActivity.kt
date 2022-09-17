package de.marcel.exxxposeme



import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.beust.klaxon.Klaxon


class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // in your method, just use:
        val versionName = findViewById<TextView>(R.id.version)
        versionName.text = BuildConfig.VERSION_NAME

        //Tracking
        var trackingID: String

        //Retrieve from SharedPreference
        val preference=getSharedPreferences(resources.getString(R.string.app_name), Context.MODE_PRIVATE)
        trackingID = preference.getString("trackingID","").toString()

        if(trackingID==""){
            val newTrackingID = getRandomString(16)

            //Store in SharedPreference
            val editor=preference.edit()
            editor.putString("trackingID",newTrackingID)
            editor.apply()

            trackingID = newTrackingID;
        }


        val thread = Thread {
            try {
                val response = Request().run("http://exxxpose-extend.bplaced.net/api/tracking/?id=$trackingID")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        thread.start()

        Handler(Looper.getMainLooper()).postDelayed(
            {
                val intent = Intent(this, Main::class.java)

                startActivity(intent)
                finish()
            },
            600 // value in milliseconds
        )

    }

    fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}