package com.example.data

import java.time.LocalDateTime

data class Station(
    val kod: String,
    val ad: String
)

data class Observation(
    val stationCode: String,
    val stationName: String,
    val veriZamani: String?,
    val formattedVeriZamani: String?,
    val hadiseKodu: String?,
    val hadiseEmoji: String,
    val hadiseMetin: String,
    val sicaklik: Double?,
    val hissedilenSicaklik: Double?,
    val nem: Double?,
    val ruzgarHiz: Double?,
    val ruzgarYon: String?,
    val ruzgarHamle: Double?,
    val aktuelBasinc: Double?,
    val denizyeIrgBasinc: Double?,
    val gorus: Double?,
    val yagis00Now: Double?,
    val yagis10Dk: Double?,
    val yagis1Saat: Double?,
    val yagis12Saat: Double?,
    val yagis24Saat: Double?,
    val karKalinlik: Double?,
    val denizSicaklik: Double?,
    val merkezId: Double?
)

data class AlaroForecast(
    val dateTime: LocalDateTime,
    val formattedTime: String, // e.g. "14:30"
    val formattedDate: String, // e.g. "04.07.2026"
    val sicaklik: Double,
    val hissedilenSicaklik: Double,
    val ruzgarHizi: Double,
    val ruzgarYonu: Double,
    val ruzgarOk: String,
    val saatlikToplamYagis: Double,
    val nem: Double,
    val bulutKapaliligi: Double,
    val basinc: Double,
    val karYuksekligi: Double,
    val hadise: String,
    val hadiseMetin: String
)

data class UkmoForecast(
    val dateTime: LocalDateTime,
    val formattedTime: String,
    val formattedDate: String,
    val sicaklik: Double,
    val hissedilenSicaklik: Double,
    val nem: Double,
    val yagis: Double,
    val weatherCode: Int,
    val durum: String,
    val ruzgarHizi: Double,
    val ruzgarYonu: Double,
    val ruzgarOk: String,
    val ruzgarHamlesi: Double,
    val toplamBulut: Double,
    val alcakBulut: Double,
    val ortaBulut: Double,
    val yuksekBulut: Double,
    val sis2m: Double,
    val islakHazneSicakligi: Double,
    val cape: Double,
    val cin: Double,
    val karYagisi: Double,
    val gorusUzakligi: Double,
    val ciyNoktasi: Double,
    val ghi: Double,
    val directRadiation: Double,
    val diffuseRadiation: Double,
    val dni: Double,
    val gti: Double,
    val terrestrialRadiation: Double
)

data class ImgwAlaroForecast(
    val dateTime: LocalDateTime,
    val formattedTime: String,
    val formattedDate: String,
    val sicaklik: Double,
    val hissedilenSicaklik: Double,
    val ciyNoktasi: Double,
    val ruzgarHizi: Double,
    val ruzgarHamlesi: Double,
    val ruzgarYonu: Double,
    val ruzgarOk: String,
    val yagis: Double,
    val nem: Double,
    val bulut: Double,
    val basinc: Double,
    val kar: Double,
    val hadiseMetin: String
)

data class RadarThreat(
    val cx: Double,
    val cy: Double,
    val wx: Double,
    val wy: Double,
    val dirX: Double,
    val dirY: Double,
    val maxInt: Int,
    val prob: Int,
    val status: String,
    val eta: Int,
    val duration: Int,
    val nature: String,
    val decay: Double,
    val widthKm: Double,
    val speedPxMin: Double
)

data class RadarTimelineStep(
    val time: String,
    val desc: String,
    val level: Int
)

data class RadarAnalysisResult(
    val weatherForecast: String,
    val coreIntensity: Int,
    val threats: List<RadarThreat>,
    val timeline: List<RadarTimelineStep>,
    val predictedDbz: List<Double>,
    val rainRates: List<Double>,
    val radarBitmap: android.graphics.Bitmap? = null
)


