package com.example.hearandthere_test.ui.error

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hearandthere_test.R
import com.example.hearandthere_test.ui.map.MapActivity
import com.example.hearandthere_test.ui.map.MapsFragment
import kotlinx.android.synthetic.main.fragment_error.view.*

class ErrorFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val errorView : View =  inflater.inflate(R.layout.fragment_error, container, false)

        errorView.cv_goBack.setOnClickListener {
            (activity as MapActivity).backToMain()
        }

        return errorView
    }

    companion object {
        @JvmStatic
        fun newInstance() : Fragment? {
            return ErrorFragment()
        }
    }
}