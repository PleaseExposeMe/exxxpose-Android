package de.marcel.exxxposeme



import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // in your method, just use:
        val versionName = findViewById<TextView>(R.id.version)
        versionName.text = BuildConfig.VERSION_NAME

        Handler(Looper.getMainLooper()).postDelayed(
            {
                val intent = Intent(this, Main::class.java)

                startActivity(intent)
                finish()
            },
            600 // value in milliseconds
        )

    }
}