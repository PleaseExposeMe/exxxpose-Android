package de.marcel.exxxposeme

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLlite(context: Context, factory: SQLiteDatabase.CursorFactory?) :
        SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

        // below is the method for creating a database by a sqlite query
        override fun onCreate(db: SQLiteDatabase) {
            // below is a sqlite query, where column names
            // along with their data types is given
            val query = ("CREATE TABLE " + TABLE_NAME + " ("
                    + ID_COL + " INTEGER PRIMARY KEY, " +
                    LINK_COl + " TEXT," +
                    TITLE_COL + " TEXT," +
                    Type_COL + " TEXT," +
                    State_COL + " TEXT" + ")")

            // we are calling sqlite
            // method for executing our query
            db.execSQL(query)
        }

        override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
            // this method is to check if table already exists
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
            onCreate(db)
        }

        // This method is for adding data in our database
        fun addBookmark(Link : String, Title : String, Type: String ){

            // below we are creating
            // a content values variable
            val values = ContentValues()

            // we are inserting our values
            // in the form of key-value pair
            values.put(LINK_COl, Link)
            values.put(TITLE_COL, Title)
            values.put(Type_COL, Type)
            values.put(State_COL, "unknown")

            // here we are creating a
            // writable variable of
            // our database as we want to
            // insert value in our database
            val db = this.writableDatabase

            // all values are inserted into database
            db.insert(TABLE_NAME, null, values)

            // at last we are
            // closing our database
            db.close()
        }

        // below method is to get
        // all data from our database
        fun getBookmarks(): Cursor? {

            // here we are creating a readable
            // variable of our database
            // as we want to read value from it
            val db = this.readableDatabase

            // below code returns a cursor to
            // read data from the database
            return db.rawQuery("SELECT * FROM " + TABLE_NAME, null)

        }


    fun updateExpiredBookmark(link: String): Cursor?{
        // here we are creating a readable
        // variable of our database
        // as we want to read value from it
        val db = this.readableDatabase

        // below code returns a cursor to
        // read data from the database
        return db.rawQuery("Update " + TABLE_NAME + " SET " + State_COL + " = 'expired' WHERE " + LINK_COl + "= '" + link + "'", null)

        //UPDATE table
        //SET column_1 = new_value_1,
        //    column_2 = new_value_2
        //WHERE
        //    search_condition
    }

    fun deleteBookmark(link: String) {
        // here we are creating a readable
        // variable of our database
        // as we want to read value from it
        val db = this.readableDatabase

        // below code returns a cursor to
        // read data from the database
       // db.delete("DELETE * FROM " + TABLE_NAME,"WHERE link=gf")
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + LINK_COl + "= '" + link + "'");
    }

        companion object{
            // here we have defined variables for our database

            // below is variable for database name
            private val DATABASE_NAME = "bookmarks"

            // below is the variable for database version
            private val DATABASE_VERSION = 1

            // below is the variable for table name
            val TABLE_NAME = "gfg_table"

            // below is the variable for id column
            val ID_COL = "id"

            // below is the variable for link column
            val LINK_COl = "link"

            // below is the variable for title column
            val TITLE_COL = "title"

            val Type_COL = "type"

            val State_COL = "state"
        }

}