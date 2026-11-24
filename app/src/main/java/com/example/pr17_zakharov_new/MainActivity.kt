package com.example.pr17_zakharov_new

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.UUID

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, CrimeListFragment())
                .commit()
        }
    }

    fun showCrime(crimeId: UUID) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, FragmentCrime.newInstance(crimeId))
            .addToBackStack(null)
            .commit()
    }
}
