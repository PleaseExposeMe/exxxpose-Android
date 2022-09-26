package de.marcel.exxxposeme

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class History : AppCompatActivity() {
    val Link: MutableList<String> = ArrayList()
    val Title: MutableList<String> = ArrayList()
    val Date: MutableList<String> = ArrayList()


    var delete = false
    var leaveTimeStemp =  System.currentTimeMillis()
    var postOpened = false

    var currentSelectedBookmark = 0;
    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)


        val db = SQLlite(this, null)

        // below is the variable for cursor
        // we have called method to get
        // all names from our database
        // and add to name text view
        val cursor = db.getHistory()

        // moving the cursor to first position and
        // appending value in the text view
        cursor!!.moveToFirst()


        var i = 0;

        cursor.moveToFirst();
        while (!cursor.isAfterLast) {
            Link.add(cursor.getString(cursor.getColumnIndex(SQLlite.LINK_COl)))
            Title.add(cursor.getString(cursor.getColumnIndex(SQLlite.TITLE_COL)))
            Date.add(cursor.getString(cursor.getColumnIndex(SQLlite.Date)))
            cursor.moveToNext();
        }

        // at last we close our cursor
        cursor.close()

        //Set data to RecyclerView
        // getting the recyclerview by its id
        val recyclerview = findViewById<RecyclerView>(R.id.historyView)

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(this)

        // This will pass the ArrayList to our Adapter
        val adapter = CustomAdapterHistory(Title, Date)

        adapter.setOnItemClickListener(object : CustomAdapterHistory.onItemClickListner{
            override fun onItemClick(position: Int){

                if(delete){
                    currentSelectedBookmark = position
                    //deleteBookmark(position)
                }else{
                    loadViewer(Link[position])
                }
            }
        })

        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter

        val deleteBtn = findViewById<ImageView>(R.id.delete)
        deleteBtn.setOnClickListener {
            deleteHistory()
        }

        //close btn
        val btn = findViewById<ImageView>(R.id.btn)
        btn.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("Range")
    fun updateRecycleView(){

        Link.clear()
        Title.clear()

        val db = SQLlite(this, null)

        // below is the variable for cursor
        // we have called method to get
        // all names from our database
        // and add to name text view
        val cursor = db.getHistory()

        // moving the cursor to first position and
        // appending value in the text view
        cursor!!.moveToFirst()


        var i = 0;

        cursor.moveToFirst();
        while (!cursor.isAfterLast) {
            Link.add(cursor.getString(cursor.getColumnIndex(SQLlite.LINK_COl)))
            Title.add(cursor.getString(cursor.getColumnIndex(SQLlite.TITLE_COL)))
            Date.add(cursor.getString(cursor.getColumnIndex(SQLlite.Date)))
            cursor.moveToNext();
        }

        // at last we close our cursor
        cursor.close()

        //Set data to RecyclerView
        // getting the recyclerview by its id
        val recyclerview = findViewById<RecyclerView>(R.id.historyView)


        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(this)

        // This will pass the ArrayList to our Adapter
        CustomAdapterHistory(Title, Date)
    }

    fun deleteHistory(){
        val db = SQLlite(this, null)
        AlertDialog.Builder(this)
            .setTitle("Are you sure you want to delete the history?")
            .setNegativeButton("No", null)
            .setPositiveButton(
                "Yes"
            ) { _, _ ->
                db.deleteHistory()
                updateRecycleView()
                Toast("History deleted")
            }.create().show()
    }

    @SuppressLint("ResourceAsColor")
    fun Toast(message: String) {
        val contextView = findViewById<View>(R.id.historyView)
        Snackbar.make(contextView, message, Snackbar.LENGTH_SHORT)
            .setAction("Dismiss") {
                // Responds to click on the action
            }
            .setActionTextColor(ContextCompat.getColor(this, R.color.main_green))
            .show()
    }

    fun loadViewer(url: String){
        leaveTimeStemp = System.currentTimeMillis()
        postOpened = true
        //Check Internet Connection
        var webViewhelper = WebViewHelper()
        if(webViewhelper.isOnline(applicationContext)){
            val intent = Intent(this, Viewer::class.java)
            intent.putExtra("url", url)
            intent.putExtra("history", "false")
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }else{
            Toast("No Internet connection")
        }
    }

    override fun onResume() {
        super.onResume()
        val difference = System.currentTimeMillis() - leaveTimeStemp
        if(difference < 250 && postOpened){
            Toast("Post is expired ")
        }
        postOpened = false
        updateRecycleView()
    }

}
