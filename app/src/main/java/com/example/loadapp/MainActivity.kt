package com.example.loadapp

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    private var downloadID: Long = 0
    private var selectedGitHubRepository: String? = null
    private var selectedGitHubFileName: String? = null
    lateinit var loadingButton: LoadingButton

    private val notificationManager: NotificationManager by lazy {
        ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        setupViews()
        setupChannel()
    }

    private fun setupViews() {
        loadingButton = findViewById(R.id.loading_button)
        loadingButton.setLoadingButtonState(LoadingButtonState.Idle)
        loadingButton.setOnClickListener {
            if (selectedGitHubRepository == null) {
                loadingButton.setLoadingButtonState(LoadingButtonState.Completed)
                showToast(getString(R.string.noRepotSelectedText))
                return@setOnClickListener
            }
            download()
        }
    }

    private fun setupChannel() {
        createChannel(
            getString(R.string.githubRepo_notification_channel_id),
            getString(R.string.githubRepo_notification_channel_name)
        )
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val action = intent.action

            if (downloadID != id) return

            if (!action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) return

            showNotification(intent)
        }
    }

    private fun showNotification(intent: Intent) {
        val query = DownloadManager.Query()
        query.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor: Cursor = manager.query(query)
        if (!cursor.moveToFirst()) return
        if (cursor.count <= 0) return

        val i = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val status = cursor.getInt(i)
        if (status == DownloadManager.STATUS_SUCCESSFUL) {
            loadingButton.setLoadingButtonState(LoadingButtonState.Completed)
            sendNotification(
                selectedGitHubFileName.toString(),
                getString(R.string.success)
            )
            return
        }

        loadingButton.setLoadingButtonState(LoadingButtonState.Completed)

        sendNotification(
            selectedGitHubFileName.toString(),
            getString(R.string.failed)
        )
    }

    private fun download() {
        loadingButton.setLoadingButtonState(LoadingButtonState.Loading)
        createDirectory()
        val request =
            DownloadManager.Request(Uri.parse(selectedGitHubRepository))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "/repos/repository.zip"
                )

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)
    }

    private fun createDirectory() {
        val file = File(getExternalFilesDir(null), "/repos")
        if (!file.exists()) {
            file.mkdirs()
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view !is RadioButton) return
        when (view.getId()) {
            R.id.glide_button -> setupGlide()
            R.id.load_app_button -> setupLoadApp()
            R.id.retrofit_button -> setupRetrofit()
        }
    }

    private fun setupGlide() {
        selectedGitHubRepository = "https://github.com/bumptech/glide"
        selectedGitHubFileName = getString(R.string.glide_text)
    }

    private fun setupLoadApp() {
        selectedGitHubRepository =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
        selectedGitHubFileName = getString(R.string.load_app_text)
    }

    private fun setupRetrofit() {
        selectedGitHubRepository = "https://github.com/square/retrofit"
        selectedGitHubFileName = getString(R.string.retrofit_text)
    }

    private fun showToast(text: String) {
        val toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.download_completed)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun sendNotification(
        messageBody: String,
        status: String
    ) {
        NotificationSender.sendLoadCompletedNotification(
            messageBody = messageBody,
            status = status,
            context = this
        )
    }


}