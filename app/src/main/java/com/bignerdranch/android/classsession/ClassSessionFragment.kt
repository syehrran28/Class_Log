package com.bignerdranch.android.classsession

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_STUDENT_ID = "student_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHONE = 2
private const val DATE_FORMAT = "EEE, MM, dd"

class ClassSessionFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {
    private lateinit var classSession: ClassSession
    private lateinit var titleField: EditText
    private lateinit var classSessionDescriptionField:EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var studentButton: Button
    private lateinit var callButton: Button
    private lateinit var backButton: Button

    private val classSessionDetailViewModel: ClassSessionDetailViewModel by lazy {
        ViewModelProviders.of(this).get(ClassSessionDetailViewModel::class.java)
    }

    companion object {
        fun newInstance(studentId: UUID): ClassSessionFragment {
            val args = Bundle().apply {
                putSerializable(ARG_STUDENT_ID, studentId)
            }
            return ClassSessionFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classSession = ClassSession()
        val studentId: UUID = arguments?.getSerializable(ARG_STUDENT_ID) as UUID
        classSessionDetailViewModel.loadSession(studentId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_classsession, container, false)
        titleField = view.findViewById(R.id.class_session_title) as EditText
        classSessionDescriptionField= view.findViewById(R.id.class_session_description)
        dateButton = view.findViewById(R.id.class_session_date) as Button
        timeButton = view.findViewById(R.id.class_session_time) as Button
        solvedCheckBox = view.findViewById(R.id.session_completed) as CheckBox
        reportButton = view.findViewById(R.id.class_session_report) as Button
        studentButton = view.findViewById(R.id.class_session_student) as Button
        callButton = view.findViewById(R.id.call_student) as Button
        backButton = view.findViewById(R.id.back_button) as Button

        dateButton.text = "EDIT DATE"
        timeButton.text = "EDIT TIME"
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        classSessionDetailViewModel.classSessionLiveData.observe(
            viewLifecycleOwner
        ) { student ->
            student?.let {
                this.classSession =  student
                updateUI()
            }
        }
    }

    private fun updateUI() {
        titleField.setText(classSession.title)
//        dateButton.text = classSession.date.toString()

        classSessionDescriptionField.setText(classSession.description)


        if(dateButton.text != "EDIT DATE"){
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val dateString = dateFormat.format(classSession.date)
            dateButton.text = dateString
        }

        if(timeButton.text != "EDIT TIME"){
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val timeString = timeFormat.format(classSession.time)
            timeButton.text = timeString
        }

        solvedCheckBox.apply {
            isChecked = classSession.isCompleted
            jumpDrawablesToCurrentState()
        }

        if (classSession.student.isNotEmpty()) {
            studentButton.text = classSession.student
        }
    }

    private fun getClassReport(): String {
        val solvedString = if (classSession.isCompleted) {
            getString(R.string.class_session_completed)
        } else {
            getString(R.string.class_session_not_completed)
        }
        val dateString = DateFormat.format(DATE_FORMAT, classSession.date).toString()
        val suspect = if (classSession.student.isBlank()) {
            getString(R.string.class_session_report_no_student)
        } else {
            getString(R.string.class_session_report_student, classSession.student)
        }
        return getString(R.string.class_session_report, classSession.title, dateString, solvedString, suspect)
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                classSession.title = sequence.toString()

            }

            override fun afterTextChanged(s: Editable?) {
                // This space intentionally left blank
            }
        }

        titleField.addTextChangedListener(titleWatcher)

        val descriptionWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                classSession.description = sequence.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                // This space intentionally left blank
            }
        }

        classSessionDescriptionField.addTextChangedListener(descriptionWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked -> classSession.isCompleted = isChecked }
        }


        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(classSession.date).apply {
                setTargetFragment(this@ClassSessionFragment, REQUEST_DATE)
                show(this@ClassSessionFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(classSession.time).apply {
                setTargetFragment(this@ClassSessionFragment, REQUEST_TIME)
                show(this@ClassSessionFragment.requireFragmentManager(), DIALOG_TIME)
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getClassReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.class_session_report_subject))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_class_session_report))
                startActivity(chooserIntent)
            }
        }

        studentButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)

            Log.d("SuspectButton", resolvedActivity.toString())
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }
        backButton.setOnClickListener {
            // Handle the back button click event
            activity?.onBackPressed()
        }

        callButton.apply {

            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_CONTACTS
                ) -> {
                    val pickPhoneIntent =
                        Intent(
                            Intent.ACTION_PICK,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                        )
                    setOnClickListener {
                        startActivityForResult(pickPhoneIntent, REQUEST_PHONE)
                    }
                }
                else -> {
                    // You can directly ask for the permission.
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_CONTACTS),
                        REQUEST_PHONE
                    )
                }
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PHONE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    val pickPhoneIntent =
                        Intent(
                            Intent.ACTION_PICK
                        ).apply {
                            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                        }
                    startActivityForResult(pickPhoneIntent, REQUEST_PHONE)

                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    Log.e("ClassSessionFragment", "Unavailable permissions CONTACTS")
                }
                return
            }
            else -> {
            }
        }
    }

    override fun onStop() {
        super.onStop()
        classSessionDetailViewModel.saveSession(classSession)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                // Specify which fields you want your query to return values for
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                // Perform your query - the contactUri is like a "where" clause here
                val cursor = requireActivity().contentResolver
                    .query(contactUri!!, queryFields, null, null, null)
                cursor?.use {
                    // Verify that the cursor contains at least one result
                    if (it.count == 0) {
                        return
                    }
                    // Pull out the first column of the first row of data -
                    // that is your suspect's name
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    classSession.student = suspect
                    classSessionDetailViewModel.saveSession(classSession)
                    studentButton.text = suspect
                }
            }
            requestCode == REQUEST_PHONE && data != null -> {
                Log.i("Request Phone", "URI: ${data.data}")

                val contactUri = data.data
                val queries = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val cursor = requireActivity()
                    .contentResolver
                    .query(
                        contactUri!!,
                        queries,
                        null,
                        null,
                        null
                    )

                cursor?.use { it ->
                    if (it.count == 0) {
                        return
                    }
                    // Pull out the first column of the first row of data -
                    // that is your suspect's name
                    it.moveToFirst()

                    val phoneIndex =
                        cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    Log.d("Phone index", phoneIndex.toString())

                    val phone = it.getString(phoneIndex)

                    Log.d("ClassSessionPhone", phone)
                    val number: Uri = Uri.parse("tel:$phone")

                    startActivity(Intent(Intent.ACTION_DIAL, number))

                }


            }
        }
    }

    override fun onDateSelected(date: Date) {
        classSession.date = date
        dateButton.text = "UPDATE"
        classSessionDetailViewModel.saveSession(classSession)
        updateUI()
    }

    override fun onTimeSelected(time: Date) {
        classSession.time = time
        timeButton.text = "UPDATE"
        classSessionDetailViewModel.saveSession(classSession)
        updateUI()
    }

//    override fun onDateSelected(date: Date) {
//        // Preserve time from classSession.time
//        val calendar = Calendar.getInstance()
//        calendar.time = date
//        calendar.set(Calendar.HOUR_OF_DAY, classSession.time.hours)
//        calendar.set(Calendar.MINUTE, classSession.time.minutes)
//
//        classSession.date = calendar.time
//        dateButton.text = "UPDATE"
//        classSessionDetailViewModel.saveSession(classSession)
//        updateUI()
//    }
//
//    override fun onTimeSelected(time: Date) {
//        // Preserve date from classSession.date
//        val calendar = Calendar.getInstance()
//        calendar.time = classSession.date
//        calendar.set(Calendar.HOUR_OF_DAY, time.hours)
//        calendar.set(Calendar.MINUTE, time.minutes)
//
//        classSession.time = calendar.time
//        timeButton.text = "UPDATE"
//        classSessionDetailViewModel.saveSession(classSession)
//        updateUI()
//    }
}