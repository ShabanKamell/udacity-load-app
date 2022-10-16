package com.example.loadapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {
    private var fileName = ""
    private var status = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(findViewById(R.id.toolbar))
        parseData()
        setupViews()
    }

    private fun setupViews() {
        findViewById<Button>(R.id.ok_button).setOnClickListener {
            returnToMainActivity()
        }
        findViewById<TextView>(R.id.file_name).text = fileName
        findViewById<TextView>(R.id.status_text).text = status
    }

    private fun parseData() {
        fileName = intent.getStringExtra(FILE_NAME).toString()
        status = intent.getStringExtra(STATUS).toString()
    }

    private fun returnToMainActivity() {
        val mainActivity = Intent(this, MainActivity::class.java)
        startActivity(mainActivity)
    }

    companion object {
        const val FILE_NAME = "fileName"
        const val STATUS = "status"
    }
}
