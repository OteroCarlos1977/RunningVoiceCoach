package com.otero.runningvoicecoach.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.otero.runningvoicecoach.domain.pace.PaceCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationTracker(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
) {
    private val _state = MutableStateFlow(RunLocationState())
    val state: StateFlow<RunLocationState> = _state.asStateFlow()

    private var lastAcceptedLocation: Location? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.locations.forEach(::handleLocation)
        }
    }

    @SuppressLint("MissingPermission")
    fun start() {
        if (!hasLocationPermission()) {
            _state.value = _state.value.copy(
                isTracking = false,
                lastError = "Permiso de ubicacion no concedido"
            )
            return
        }

        _state.value = _state.value.copy(isTracking = true, lastError = null)
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            context.mainLooper
        )
    }

    fun stop() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _state.value = _state.value.copy(isTracking = false)
    }

    fun reset() {
        lastAcceptedLocation = null
        _state.value = RunLocationState()
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun handleLocation(location: Location) {
        if (!isAccurateEnough(location)) {
            return
        }

        val speedMetersPerSecond = location.speed.takeIf { location.hasSpeed() && it >= MIN_TRACKED_SPEED_METERS_PER_SECOND }
        val previousLocation = lastAcceptedLocation
        val distanceDelta = if (previousLocation != null && isPlausibleMovement(previousLocation, location)) {
            previousLocation.distanceTo(location).toDouble()
        } else {
            0.0
        }

        lastAcceptedLocation = location

        _state.value = _state.value.copy(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracyMeters = location.accuracy,
            altitudeMeters = location.altitude.takeIf { location.hasAltitude() },
            speedMetersPerSecond = speedMetersPerSecond,
            totalDistanceMeters = _state.value.totalDistanceMeters + distanceDelta,
            currentPaceSecondsPerKm = speedMetersPerSecond?.let {
                PaceCalculator.calculatePaceSecondsPerKm(it.toDouble())
            },
            timestampMillis = location.time,
            isTracking = true,
            lastError = null
        )
    }

    private fun isAccurateEnough(location: Location): Boolean {
        return location.hasAccuracy() && location.accuracy <= MAX_ACCEPTED_ACCURACY_METERS
    }

    private fun isPlausibleMovement(previous: Location, current: Location): Boolean {
        val elapsedSeconds = (current.time - previous.time) / MILLIS_PER_SECOND
        if (elapsedSeconds <= 0.0) {
            return false
        }

        val distanceMeters = previous.distanceTo(current)
        val calculatedSpeed = distanceMeters / elapsedSeconds
        val reportedSpeed = current.speed.takeIf { current.hasSpeed() } ?: return false

        return distanceMeters >= MIN_ACCEPTED_MOVEMENT_METERS &&
            reportedSpeed >= MIN_TRACKED_SPEED_METERS_PER_SECOND &&
            calculatedSpeed <= MAX_PLAUSIBLE_RUNNING_SPEED_METERS_PER_SECOND
    }

    private companion object {
        const val MAX_ACCEPTED_ACCURACY_METERS = 25f
        const val MAX_PLAUSIBLE_RUNNING_SPEED_METERS_PER_SECOND = 12.0
        const val MIN_ACCEPTED_MOVEMENT_METERS = 0.75f
        const val MIN_TRACKED_SPEED_METERS_PER_SECOND = 0.8f
        const val MILLIS_PER_SECOND = 1000.0

        val locationRequest: LocationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1_000L
        )
            .setMinUpdateIntervalMillis(500L)
            .setMinUpdateDistanceMeters(1f)
            .build()
    }
}
