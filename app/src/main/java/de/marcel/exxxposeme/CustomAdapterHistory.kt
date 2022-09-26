package de.marcel.exxxposeme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.marcel.exxxposeme.R

class CustomAdapterHistory(private val dataSet: MutableList<String>,private val dateSet: MutableList<String>) :
    RecyclerView.Adapter<CustomAdapterHistory.ViewHolder>() {

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
        val textViewDate: TextView


        init {
            // Define click listener for the ViewHolder's View.
            textView = view.findViewById(R.id.textView)
            textViewDate = view.findViewById(R.id.textView_date)
            view.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.text_row_item_history, viewGroup, false)

        return ViewHolder(view, mListner)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = dataSet[position]
        viewHolder.textViewDate.text = dateSet[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}