package com.example.pr17_zakharov_new

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.util.UUID

private const val REQUEST_CONTACT = 0
private const val REQUEST_CONTACT_PERMISSION = 1
private const val DATE_FORMAT = "EEE, MMM, dd"

class FragmentCrime : Fragment() {
    private lateinit var crime: Crime
    private lateinit var title: EditText
    private lateinit var date: Button
    private lateinit var check1: CheckBox
    private lateinit var suspectButton: Button
    private lateinit var reportButton: Button
    private lateinit var callButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crimeId = UUID.fromString(requireArguments().getString("crime_id"))
        crime = CrimeLab.getCrime(requireContext(), crimeId) ?: Crime()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_fragment_crime, container, false)
        title = view.findViewById(R.id.crime_title)
        date = view.findViewById(R.id.crime_date)
        check1 = view.findViewById(R.id.crime_solved)
        suspectButton = view.findViewById(R.id.crime_suspect)
        reportButton = view.findViewById(R.id.crime_report)
        callButton = view.findViewById(R.id.crime_call)
        date.apply {
            text = crime.date.toString()
            isEnabled = false
        }
        title.setText(crime.title)
        check1.isChecked = crime.isSolved
        suspectButton.text = if (crime.suspect.isBlank()) {
            getString(R.string.crime_suspect_text)
        } else {
            crime.suspect
        }
        callButton.isEnabled = crime.suspectPhone.isNotBlank()
        return view
    }


    override fun onStart() {
        super.onStart()
        title.addTextChangedListener(object: android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        check1.setOnCheckedChangeListener {_, isChecked -> crime.isSolved=isChecked}

        reportButton.setOnClickListener {
            if (crime.title.isBlank()) {
                Toast.makeText(requireContext(), R.string.empty_title_warning, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }
            startActivity(Intent.createChooser(intent, getString(R.string.send_report)))
        }

        suspectButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CONTACT_PERMISSION)
                } else {
                    startActivityForResult(pickContactIntent, REQUEST_CONTACT)
                }
            }

            val packageManager = requireActivity().packageManager
            val resolvedActivity = packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            isEnabled = resolvedActivity != null
        }

        callButton.apply {
            isEnabled = crime.suspectPhone.isNotBlank()
            setOnClickListener {
                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${crime.suspectPhone}"))
                startActivity(callIntent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri = data.data ?: return
                val queryFields = arrayOf(
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID
                )
                val cursor = requireActivity().contentResolver
                    .query(contactUri, queryFields, null, null, null)
                cursor?.use {
                    if (it.count == 0) {
                        return
                    }
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    val contactId = it.getString(1)
                    crime.suspect = suspect
                    suspectButton.text = suspect
                    crime.suspectPhone = getPhoneNumber(contactId)
                    callButton.isEnabled = crime.suspectPhone.isNotBlank()
                    CrimeLab.save(requireContext())
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CONTACT_PERMISSION && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            startActivityForResult(pickContactIntent, REQUEST_CONTACT)
        } else {
            Toast.makeText(requireContext(), R.string.no_contacts_permission, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPhoneNumber(contactId: String): String {
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
        val cursor = requireActivity().contentResolver
            .query(uri, projection, selection, arrayOf(contactId), null)
        cursor?.use {
            if (it.count > 0) {
                it.moveToFirst()
                return it.getString(0)
            }
        }
        return ""
    }

    override fun onPause() {
        super.onPause()
        CrimeLab.save(requireContext())
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    companion object {
        fun newInstance(crimeId: UUID): FragmentCrime {
            val fragment = FragmentCrime()
            fragment.arguments = Bundle().apply {
                putString("crime_id", crimeId.toString())
            }
            return fragment
        }
    }
}
