package de.marcel.exxxposeme

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class NewUpdateAvailableView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_update_available_view)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener(){
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/exxxposeApp/exxxpose-Android"))
            startActivity(browserIntent)
        }

        val button2 = findViewById<Button>(R.id.button2)
        button2.setOnClickListener(){
            val intent = Intent(this, Main::class.java)
            startActivity(intent)
            finish()
        }

    }
}