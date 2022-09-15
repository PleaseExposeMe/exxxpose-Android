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
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.sql.Time

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

    var currentSelectedBookmark = 0;

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
        val recyclerview = findViewById<RecyclerView>(R.id.bookmarkView)

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
        var btn = findViewById<ImageView>(R.id.btn)
        btn.setOnClickListener {
                finish()
        }

        var deleteBtn = findViewById<ImageView>(R.id.delete)
        deleteBtn.setOnClickListener {
            var msg = findViewById<TextView>(R.id.msg)
            if(delete){
                delete = false
                msg.visibility = View.GONE
            }else{
                delete = true
                msg.visibility = View.VISIBLE
            }
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
        val recyclerview = findViewById<RecyclerView>(R.id.bookmarkView)


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

    fun Toast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
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
            val recyclerview = findViewById<RecyclerView>(R.id.bookmarkView)
        }
        postOpened = false
    }

}

class CustomAdapter(private val dataSet: MutableList<String>) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private lateinit var mListner: onItemClickListner

    interface onItemClickListner{

        fun onItemClick(position: Int)

    }

    fun setOnItemClickListener(listener: onItemClickListner){

        mListner = listener

    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View, listener : onItemClickListner) : RecyclerView.ViewHolder(view) {
        val textView: TextView


        init {
            // Define click listener for the ViewHolder's View.
            textView = view.findViewById(R.id.textView)
            view.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.text_row_item, viewGroup, false)

        return ViewHolder(view, mListner)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = dataSet[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}