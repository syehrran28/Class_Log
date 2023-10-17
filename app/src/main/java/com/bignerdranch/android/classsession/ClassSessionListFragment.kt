package com.bignerdranch.android.classsession

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ClassListFragment"

class ClassSessionListFragment : Fragment() {
    /**
     * Required interface for hosting activities
     *
     */
    interface Callbacks {
        fun onClassSessionSelected(classSessionId: UUID);
    }

    private var callbacks: Callbacks? = null

    private val classSessionListViewModel: ClassSessionListViewModel by lazy {
        ViewModelProviders.of(this).get(ClassSessionListViewModel::class.java)
    }

    private lateinit var classRecyclerView: RecyclerView;

    private var adapter: ClassAdapter? = ClassAdapter(emptyList())

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_classsession_list, container, false)
        classRecyclerView = view.findViewById(R.id.class_recycler_view) as RecyclerView
        classRecyclerView.layoutManager = LinearLayoutManager(context)
        classRecyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        classSessionListViewModel.sessionListLiveData.observe(
            viewLifecycleOwner,
            Observer { classSessions ->
                classSessions?.let { Log.i(TAG, "Got class ${classSessions.size}") }
                updateUI(classSessions)
            })
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_classsession_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_class_session -> {
                val classSession = ClassSession()
                classSessionListViewModel.addClassSession(classSession)
                callbacks?.onClassSessionSelected(classSession.id)
                true
            }
            R.id.restart_class_session -> {
                classSessionListViewModel.removeAllClassSessions() // this function should be implemented
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }

    private fun updateUI(classSessions: List<ClassSession>) {
        adapter = ClassAdapter(classSessions)
        classRecyclerView.adapter = adapter
    }

    companion object {
        fun newInstance(): ClassSessionListFragment {
            return ClassSessionListFragment()
        }
    }

    private inner class ClassHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private val titleTextView: TextView = itemView.findViewById(R.id.class_session_title) as TextView
        private val dateTextView: TextView = itemView.findViewById(R.id.class_session_date) as TextView
        private val solvedImageView: ImageView =
            itemView.findViewById(R.id.session_completed) as ImageView
        private lateinit var classSession: ClassSession

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(classSession: ClassSession) {
            this.classSession = classSession
            titleTextView.text = classSession.student

            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val dateString = dateFormat.format(classSession.date)

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val timeString = timeFormat.format(classSession.time)

            dateTextView.text = dateString + " " + timeString
//            dateTextView.text = classSession.date.toString()
            solvedImageView.visibility = if (classSession.isCompleted) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        override fun onClick(v: View?) {
            callbacks?.onClassSessionSelected(classSession.id)
        }


    }

    private inner class ClassAdapter(var classSessions: List<ClassSession>) :
        RecyclerView.Adapter<ClassHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassHolder {
            val view = layoutInflater.inflate(R.layout.class_list_item_class, parent, false)
            return ClassHolder(view)
        }

        override fun onBindViewHolder(holder: ClassHolder, position: Int) {
            val session = classSessions[position]
            holder.bind(session)
        }

        override fun getItemCount(): Int = classSessions.size

    }
}