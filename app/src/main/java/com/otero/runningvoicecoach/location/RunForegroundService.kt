package com.otero.runningvoicecoach.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.otero.runningvoicecoach.MainActivity
import com.otero.runningvoicecoach.R
import com.otero.runningvoicecoach.domain.pace.PaceCalculator

class RunForegroundService : Service() {
    private lateinit var locationTracker: LocationTracker
    private val handler = Handler(Looper.getMainLooper())
    private var startedAtMillis: Long = 0L

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateNotification()
            handler.postDelayed(this, NOTIFICATION_UPDATE_INTERVAL_MILLIS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationTracker = LocationTracker(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopRun()
            else -> startRun()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacks(updateRunnable)
        locationTracker.stop()
        super.onDestroy()
    }

    private fun startRun() {
        if (!hasLocationPermission()) {
            stopSelf()
            return
        }

        if (startedAtMillis == 0L) {
            startedAtMillis = System.currentTimeMillis()
        }

        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        locationTracker.start()
        handler.removeCallbacks(updateRunnable)
        handler.post(updateRunnable)
    }

    private fun stopRun() {
        handler.removeCallbacks(updateRunnable)
        locationTracker.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val state = locationTracker.state.value
        val elapsedSeconds = if (startedAtMillis > 0L) {
            (System.currentTimeMillis() - startedAtMillis) / MILLIS_PER_SECOND
        } else {
            0L
        }

        val contentIntent = PendingIntent.getActivity(
            this,
            REQUEST_OPEN_APP,
            Intent(this, MainActivity::class.java),
            pendingIntentFlags()
        )
        val stopIntent = PendingIntent.getService(
            this,
            REQUEST_STOP,
            Intent(this, RunForegroundService::class.java).setAction(ACTION_STOP),
            pendingIntentFlags()
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Carrera activa")
            .setContentText(
                "${formatDuration(elapsedSeconds)} · ${formatDistance(state.totalDistanceMeters)} · ${
                    PaceCalculator.formatPace(state.currentPaceSecondsPerKm)
                }"
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .addAction(0, "Finalizar", stopIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Carrera activa",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Seguimiento de carrera en curso"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun pendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    companion object {
        private const val CHANNEL_ID = "active_run"
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_OPEN_APP = 2001
        private const val REQUEST_STOP = 2002
        private const val NOTIFICATION_UPDATE_INTERVAL_MILLIS = 1_000L
        private const val MILLIS_PER_SECOND = 1_000L

        const val ACTION_STOP = "com.otero.runningvoicecoach.action.STOP_RUN"

        fun start(context: Context) {
            val intent = Intent(context, RunForegroundService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, RunForegroundService::class.java).setAction(ACTION_STOP)
            context.startService(intent)
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}

private fun formatDistance(distanceMeters: Double): String {
    return "%.2f km".format(distanceMeters / 1000.0)
}
