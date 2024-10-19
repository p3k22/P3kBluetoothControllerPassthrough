package com.p3kIndustries.p3kps4bt2pc.ui

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import com.p3kIndustries.p3kps4bt2pc.MainActivity
import com.p3kIndustries.p3kps4bt2pc.R

class MainUiReferences()
{
    lateinit var logsTextView: TextView
    lateinit var statusTextView: TextView
    lateinit var logScrollView: ScrollView
    lateinit var ipEditText: EditText
    lateinit var portEditText: EditText
    lateinit var connectButton: Button
    lateinit var autoDetectButton: Button
    lateinit var statusDot: View

    fun initialize(mainActivity: MainActivity)
    {
        logsTextView = mainActivity.findViewById(R.id.logTextView)
        statusTextView = mainActivity.findViewById(R.id.statusTextView)
        statusDot = mainActivity.findViewById(R.id.statusDot)
        logScrollView = mainActivity.findViewById(R.id.logScrollView)
        ipEditText = mainActivity.findViewById(R.id.ipEditText)
        portEditText = mainActivity.findViewById(R.id.portEditText)
        connectButton = mainActivity.findViewById(R.id.connectButton)
        autoDetectButton = mainActivity.findViewById(R.id.autoDetectButton)
    }
}
