package com.example.pr17_zakharov_new

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import java.util.UUID

object CrimeLab {
    private const val PREFS_NAME = "crimes"
    private const val KEY_CRIMES = "crimes_list"

    private val crimes = mutableListOf<Crime>()
    private var loaded = false

    fun getCrimes(context: Context): List<Crime> {
        load(context)
        return crimes
    }

    fun getCrime(context: Context, id: UUID): Crime? {
        load(context)
        return crimes.find { it.id == id }
    }

    fun addCrime(context: Context, crime: Crime) {
        load(context)
        crimes.add(crime)
        save(context)
    }

    fun clearCrimes(context: Context) {
        load(context)
        crimes.clear()
        save(context)
    }

    fun save(context: Context) {
        val array = JSONArray()
        for (crime in crimes) {
            array.put(JSONObject().apply {
                put("id", crime.id.toString())
                put("title", crime.title)
                put("date", crime.date.time)
                put("solved", crime.isSolved)
                put("suspect", crime.suspect)
                put("phone", crime.suspectPhone)
            })
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CRIMES, array.toString())
            .apply()
    }

    private fun load(context: Context) {
        if (loaded) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CRIMES, null)

        if (json != null) {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                crimes.add(Crime(
                    id = UUID.fromString(item.getString("id")),
                    title = item.getString("title"),
                    date = Date(item.getLong("date")),
                    isSolved = item.getBoolean("solved"),
                    suspect = item.getString("suspect"),
                    suspectPhone = item.getString("phone")
                ))
            }
        }
        loaded = true
    }
}
