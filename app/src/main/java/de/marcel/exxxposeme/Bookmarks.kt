package de.marcel.exxxposeme

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class Bookmarks : AppCompatActivity() {
    val Link: MutableList<String> = ArrayList()
    val Title: MutableList<String> = ArrayList()
    val Type: MutableList<String> = ArrayList()

    val User: MutableList<String> = ArrayList()
    val User_URL: MutableList<String> = ArrayList()

    val Posts: MutableList<String> = ArrayList()
    val Posts_URL: MutableList<String> = ArrayList()

    var delete = false
    var leaveTimeStemp =  System.currentTimeMillis()
    var postOpened = false

    var currentSelectedBookmark = 0

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)

        val db = SQLlite(this, null)

        // below is the variable for cursor
        // we have called method to get
        // all names from our database
        // and add to name text view
        val cursor = db.getBookmarks()

        // moving the cursor to first position and
        // appending value in the text view
        cursor!!.moveToFirst()


        var i = 0;

        cursor.moveToFirst();
        while (!cursor.isAfterLast) {
            Link.add(cursor.getString(cursor.getColumnIndex(SQLlite.LINK_COl)))
            Title.add(cursor.getString(cursor.getColumnIndex(SQLlite.TITLE_COL)))
            Type.add(cursor.getString(cursor.getColumnIndex(SQLlite.Type_COL)))
            cursor.moveToNext();
        }

        // at last we close our cursor
        cursor.close()

        //Sort profile and accounts
        Title.forEachIndexed { index, element ->
            if(Type[index] == "post"){
                Posts.add(element)
                Posts_URL.add(Link[index])
            }else{
                User.add(element)
                User_URL.add(Link[index])
            }
        }

        //Set data to RecyclerView
        // getting the recyclerview by its id
        val recyclerview = findViewById<RecyclerView>(R.id.historyView)

        // this creates a vertical layout Manager
        recyclerview.layoutManager = LinearLayoutManager(this)

        // This will pass the ArrayList to our Adapter
        val adapter = CustomAdapter(Title)

        adapter.setOnItemClickListener(object : CustomAdapter.onItemClickListner{
            override fun onItemClick(position: Int){

                if(delete){
                    currentSelectedBookmark = position
                    deleteBookmark(position)
                }else{
                    loadViewer(Link[position])
                }
            }
        })

        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter



        //TODO: Remove bookmark on longclick

        //close btn
        val btn = findViewById<ImageView>(R.id.btn)
        btn.setOnClickListener {
                finish()
        }

        val deleteBtn = findViewById<ImageView>(R.id.delete)
        deleteBtn.setOnClickListener {
            val msg = findViewById<TextView>(R.id.msg)
            if(delete){
                delete = false
                msg.visibility = View.GONE
            }else{
                delete = true
                msg.visibility = View.VISIBLE
            }
        }

        val export = findViewById<ImageView>(R.id.export)
        export.setOnClickListener {
            var exportStr = ""
            Link.forEachIndexed { index, element ->
                Title[index]
                exportStr += element + " - " + Title[index] + "\n"
            }
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_SUBJECT, "Export Bookmarks")
            i.putExtra(Intent.EXTRA_TEXT, exportStr)
            startActivity(Intent.createChooser(i, "Export Bookmarks"))
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
        val cursor = db.getBookmarks()

        // moving the cursor to first position and
        // appending value in the text view
        cursor!!.moveToFirst()


        var i = 0;

        cursor.moveToFirst();
        while (!cursor.isAfterLast) {
            Link.add(cursor.getString(cursor.getColumnIndex(SQLlite.LINK_COl)))
            Title.add(cursor.getString(cursor.getColumnIndex(SQLlite.TITLE_COL)))
            Type.add(cursor.getString(cursor.getColumnIndex(SQLlite.Type_COL)))
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
        val adapter = CustomAdapter(Title)
    }

    fun deleteBookmark(position : Int){
        val db = SQLlite(this, null)
        AlertDialog.Builder(this)
            .setTitle("Are you sure you want to delete the bookmark " + Title[position] +"?")
            .setNegativeButton("No", null)
            .setPositiveButton(
                "Yes"
            ) { _, _ ->
                db.deleteBookmark(Link[position])
                updateRecycleView()
                Toast("Bookmark deleted")
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
            val recyclerview = findViewById<RecyclerView>(R.id.historyView)
        }
        postOpened = false
    }

}

