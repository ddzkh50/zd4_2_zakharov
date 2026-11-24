package com.example.pr17_zakharov_new

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment

class CrimeListFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var adapter: CrimeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        listView = view.findViewById(R.id.crime_list)
        adapter = CrimeAdapter(requireContext(), CrimeLab.getCrimes(requireContext()))
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val crime = adapter.getItem(position)
            (activity as MainActivity).showCrime(crime.id)
        }

        view.findViewById<Button>(R.id.crime_add).setOnClickListener {
            val crime = Crime()
            CrimeLab.addCrime(requireContext(), crime)
            (activity as MainActivity).showCrime(crime.id)
        }

        view.findViewById<Button>(R.id.crime_clear).setOnClickListener {
            CrimeLab.clearCrimes(requireContext())
            adapter.notifyDataSetChanged()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
}
