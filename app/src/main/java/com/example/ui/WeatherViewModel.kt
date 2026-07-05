package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AlaroForecast
import com.example.data.ImgwAlaroForecast
import com.example.data.Observation
import com.example.data.UkmoForecast
import com.example.data.WeatherRepository
import com.example.data.RadarAnalysisResult
import com.example.data.RadarThreat
import com.example.data.RadarTimelineStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UIState<out T> {
    object Idle : UIState<Nothing>
    object Loading : UIState<Nothing>
    data class Success<out T>(val data: T) : UIState<T>
    data class Error(val message: String) : UIState<Nothing>
}

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _themeState = MutableStateFlow(AppTheme.SYSTEM)
    val themeState: StateFlow<AppTheme> = _themeState.asStateFlow()

    fun loadTheme(context: Context) {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedTheme = prefs.getString("theme_key", AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        _themeState.value = try {
            AppTheme.valueOf(savedTheme)
        } catch (e: Exception) {
            AppTheme.SYSTEM
        }
    }

    fun setTheme(context: Context, theme: AppTheme) {
        _themeState.value = theme
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("theme_key", theme.name).apply()
    }

    private val _observationsState = MutableStateFlow<UIState<List<Observation>>>(UIState.Idle)
    val observationsState: StateFlow<UIState<List<Observation>>> = _observationsState.asStateFlow()

    private val _alaroState = MutableStateFlow<UIState<List<AlaroForecast>>>(UIState.Idle)
    val alaroState: StateFlow<UIState<List<AlaroForecast>>> = _alaroState.asStateFlow()

    private val _alaroProgress = MutableStateFlow(0f)
    val alaroProgress: StateFlow<Float> = _alaroProgress.asStateFlow()

    private val _alaro3DayState = MutableStateFlow<UIState<List<AlaroForecast>>>(UIState.Idle)
    val alaro3DayState: StateFlow<UIState<List<AlaroForecast>>> = _alaro3DayState.asStateFlow()

    private val _alaro3DayProgress = MutableStateFlow(0f)
    val alaro3DayProgress: StateFlow<Float> = _alaro3DayProgress.asStateFlow()

    private val _ukmoState = MutableStateFlow<UIState<List<UkmoForecast>>>(UIState.Idle)
    val ukmoState: StateFlow<UIState<List<UkmoForecast>>> = _ukmoState.asStateFlow()

    private val _imgwAlaroState = MutableStateFlow<UIState<List<ImgwAlaroForecast>>>(UIState.Idle)
    val imgwAlaroState: StateFlow<UIState<List<ImgwAlaroForecast>>> = _imgwAlaroState.asStateFlow()

    private val _radarAnalysisState = MutableStateFlow<UIState<RadarAnalysisResult>>(UIState.Idle)
    val radarAnalysisState: StateFlow<UIState<RadarAnalysisResult>> = _radarAnalysisState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        // We will trigger loading from the UI once the context is available,
        // or trigger it in an onCreate block, so we can also send notifications.
    }

    fun loadAllData(context: Context) {
        if (_isRefreshing.value) return
        _isRefreshing.value = true

        viewModelScope.launch {
            // Setup Notification Channel
            createNotificationChannel(context)

            // Load Son Durumlar
            _observationsState.value = UIState.Loading
            val obsDeferred = launch {
                try {
                    val list = repository.fetchCurrentObservations()
                    if (list.isNotEmpty()) {
                        _observationsState.value = UIState.Success(list)
                    } else {
                        _observationsState.value = UIState.Error("İstasyon verileri alınamadı.")
                    }
                } catch (e: Exception) {
                    _observationsState.value = UIState.Error(e.message ?: "Beklenmedik bir hata oluştu.")
                }
            }

            // Load ALARO Model
            _alaroState.value = UIState.Loading
            _alaroProgress.value = 0f
            val alaroDeferred = launch {
                try {
                    val list = repository.fetchAlaroForecasts { progress ->
                        _alaroProgress.value = progress
                    }
                    if (list.isNotEmpty()) {
                        _alaroState.value = UIState.Success(list)
                    } else {
                        _alaroState.value = UIState.Error("ALARO Model tahminleri alınamadı.")
                    }
                } catch (e: Exception) {
                    _alaroState.value = UIState.Error(e.message ?: "Model verileri yüklenemedi.")
                }
            }

            // Load ALARO 3-Day Model
            _alaro3DayState.value = UIState.Loading
            _alaro3DayProgress.value = 0f
            val alaro3DayDeferred = launch {
                try {
                    val list = repository.fetchAlaro3DayForecasts { progress ->
                        _alaro3DayProgress.value = progress
                    }
                    if (list.isNotEmpty()) {
                        _alaro3DayState.value = UIState.Success(list)
                    } else {
                        _alaro3DayState.value = UIState.Error("ALARO 3 Günlük Model tahminleri alınamadı.")
                    }
                } catch (e: Exception) {
                    _alaro3DayState.value = UIState.Error(e.message ?: "3 Günlük Model verileri yüklenemedi.")
                }
            }

            // Load UKMO Model
            _ukmoState.value = UIState.Loading
            val ukmoDeferred = launch {
                try {
                    val list = repository.fetchUkmoForecasts()
                    if (list.isNotEmpty()) {
                        _ukmoState.value = UIState.Success(list)
                    } else {
                        _ukmoState.value = UIState.Error("UKMO Model tahminleri alınamadı.")
                    }
                } catch (e: Exception) {
                    _ukmoState.value = UIState.Error(e.message ?: "UKMO Model verileri yüklenemedi.")
                }
            }

            // Load IMGW Alaro Model (Polonya)
            _imgwAlaroState.value = UIState.Loading
            val imgwAlaroDeferred = launch {
                try {
                    val list = repository.fetchImgwAlaroForecasts()
                    if (list.isNotEmpty()) {
                        _imgwAlaroState.value = UIState.Success(list)
                    } else {
                        _imgwAlaroState.value = UIState.Error("Polonya ALARO Model tahminleri alınamadı.")
                    }
                } catch (e: Exception) {
                    _imgwAlaroState.value = UIState.Error(e.message ?: "Polonya ALARO verileri yüklenemedi.")
                }
            }

            // Load AI Radar Analysis
            _radarAnalysisState.value = UIState.Loading
            val radarDeferred = launch {
                try {
                    val result = repository.fetchRadarAnalysis()
                    if (result != null) {
                        _radarAnalysisState.value = UIState.Success(result)
                    } else {
                        _radarAnalysisState.value = UIState.Error("Radar analizi yapılamadı. Görüntü indirilemedi veya geçersiz.")
                    }
                } catch (e: Exception) {
                    _radarAnalysisState.value = UIState.Error(e.message ?: "Radar verileri yüklenemedi.")
                }
            }

            // Wait for all to complete
            obsDeferred.join()
            alaroDeferred.join()
            alaro3DayDeferred.join()
            ukmoDeferred.join()
            imgwAlaroDeferred.join()
            radarDeferred.join()

            _isRefreshing.value = false

            // Trigger Notification indicating successful data update
            val obsOk = _observationsState.value is UIState.Success
            val alaroOk = _alaroState.value is UIState.Success
            val alaro3DayOk = _alaro3DayState.value is UIState.Success

            if (obsOk || alaroOk || alaro3DayOk) {
                var updateMessage = "Hava durumu verileri başarıyla güncellendi."
                if (obsOk && alaroOk && alaro3DayOk) {
                    updateMessage = "Kocaeli Son Durumlar, ALARO ve ALARO 3 Günlük verileri güncellendi."
                } else if (obsOk && alaroOk) {
                    updateMessage = "Kocaeli Son Durumlar ve ALARO Model verileri güncellendi."
                } else if (obsOk) {
                    updateMessage = "Kocaeli Bölgesel Gözlemler başarıyla güncellendi."
                } else if (alaroOk) {
                    updateMessage = "MGM ALARO tahminleri başarıyla güncellendi."
                } else if (alaro3DayOk) {
                    updateMessage = "MGM ALARO 3 Günlük tahminleri başarıyla güncellendi."
                }
                sendUpdateNotification(context, "MGM Kocaeli Güncellendi", updateMessage)
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MGM Weather Updates"
            val descriptionText = "Notifications for MGM Kocaeli meteorological data updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("MGM_WEATHER_UPDATES", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendUpdateNotification(context: Context, title: String, message: String) {
        // Build the notification
        val builder = NotificationCompat.Builder(context, "MGM_WEATHER_UPDATES")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            // Check POST_NOTIFICATIONS permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (!hasPermission) {
                    // Safe skip, we can also display a toast or fallback
                    return
                }
            }
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1002, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
