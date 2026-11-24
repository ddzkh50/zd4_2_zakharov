package com.example.pr17_zakharov_new

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

private const val LIST_DATE_FORMAT = "EEE, MMM dd"

class CrimeAdapter(
    private val context: Context,
    private val crimes: List<Crime>
) : BaseAdapter() {

    private val inflater = LayoutInflater.from(context)

    override fun getCount(): Int = crimes.size

    override fun getItem(position: Int): Crime = crimes[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: inflater.inflate(R.layout.list_item_crime, parent, false)
        val crime = getItem(position)

        val titleView = view.findViewById<TextView>(R.id.crime_item_title)
        val dateView = view.findViewById<TextView>(R.id.crime_item_date)
        val solvedView = view.findViewById<TextView>(R.id.crime_item_solved)
        val suspectView = view.findViewById<TextView>(R.id.crime_item_suspect)

        titleView.text = crime.title.ifBlank { context.getString(R.string.crime_no_title) }
        dateView.text = DateFormat.format(LIST_DATE_FORMAT, crime.date).toString()
        solvedView.text = if (crime.isSolved) {
            context.getString(R.string.crime_list_solved)
        } else {
            context.getString(R.string.crime_list_unsolved)
        }
        suspectView.text = if (crime.suspect.isBlank()) {
            context.getString(R.string.crime_list_no_suspect)
        } else {
            context.getString(R.string.crime_list_suspect, crime.suspect)
        }

        return view
    }
}
