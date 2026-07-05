package com.example.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class WeatherRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    private val listMapType = Types.newParameterizedType(List::class.java, mapType)
    private val listMapAdapter = moshi.adapter<List<Map<String, Any?>>>(listMapType)
    private val mapAdapter = moshi.adapter<Map<String, Any?>>(mapType)

    // Kocaeli Meteorology Stations
    val stations = listOf(
        Station("19116", "DERİNCE MERKEZ"),
        Station("18802", "DERİNCE KÖYLERİ"),
        Station("18414", "KÖRFEZ"),
        Station("17067", "GÖLCÜK"),
        Station("17066", "İZMİT MERKEZ"),
        Station("18413", "KARTEPE"),
        Station("18409", "BAŞİSKELE"),
        Station("17068", "KOCAELİ CENGİZ TOPEL HAVALİMANI"),
        Station("18104", "KANDIRA"),
        Station("17630", "KARTEPE KAYAK MERKEZİ"),
        Station("17639", "GEBZE"),
        Station("18411", "DİLOVASI"),
        Station("18410", "DARICA"),
        Station("17449", "KANDIRA KEFKEN ADASI DOĞU MENDİREK FENERİ"),
        Station("18412", "KARAMÜRSEL"),
        Station("18799", "İZMİT AKMEŞE BELDESİ"),
        Station("18801", "BAŞİSKELE YUVACIK ORMAN SAHASI"),
        Station("18798", "KANDIRA AKÇAOVA BELDESİ"),
        Station("19117", "GÖLCÜK AYVAZPINARI KÖYÜ"),
        Station("18800", "İZMİT ORMAN SAHASI"),
        Station("19115", "ÇAYIROVA"),
        Station("20526", "KARTEPE KAYAK MERKEZİ (MANUEL)")
    )

    private val hadiseHaritasi = mapOf(
        "A" to Pair("☀️", "Açık"),
        "AB" to Pair("🌤️", "Az Bulutlu"),
        "PB" to Pair("⛅", "Parçalı Bulutlu"),
        "CB" to Pair("☁️", "Çok Bulutlu"),
        "D" to Pair("💨", "Dumanlı"),
        "PU" to Pair("🌫️", "Puslu"),
        "S" to Pair("🌫️", "Sisli"),
        "HY" to Pair("🌧️", "Hafif Yağmurlu"),
        "Y" to Pair("🌧️", "Yağmurlu"),
        "KY" to Pair("🌧️", "Kuvvetli Yağmurlu"),
        "KKY" to Pair("🌨️", "Karla Karışık Yağmurlu"),
        "HKY" to Pair("🌨️", "Hafif Karla Karışık Yağmurlu"),
        "K" to Pair("❄️", "Kar Yağışlı"),
        "HK" to Pair("❄️", "Hafif Kar Yağışlı"),
        "KK" to Pair("❄️", "Yoğun Kar Yağışlı"),
        "GYS" to Pair("⛈️", "Gökgürültülü Sağanak Yağışlı"),
        "MS" to Pair("🌦️", "Mevzi Sağanak Yağışlı"),
        "DY" to Pair("🌨️", "Dolu"),
        "SCK" to Pair("🔥", "Sıcak"),
        "SOG" to Pair("❄️", "Soğuk"),
        "R" to Pair("💨", "Rüzgarlı"),
        "KF" to Pair("🌪️", "Toz Fırtınası"),
        "SG" to Pair("🌧️", "Sağanak Yağışlı"),
        "GS" to Pair("⛈️", "Gökgürültülü Sağanak"),
        "GSY" to Pair("⛈️", "Gökgürültülü Sağanak Yağmur"),
        "HSY" to Pair("🌧️", "Hafif Sağanak Yağışlı"),
        "HHY" to Pair("🌧️", "Yağışlı")
    )

    private val alaroHadiseHaritasi = mapOf(
        "A" to "Açık ☀️",
        "AB" to "Az Bulutlu 🌤️",
        "PB" to "Parçalı Bulutlu ⛅",
        "CB" to "Çok Bulutlu ☁️",
        "HY" to "Hafif Yağmurlu 🌦️",
        "Y" to "Yağmurlu 🌧️",
        "KY" to "Kuvvetli Yağmurlu ⛈️",
        "KKY" to "Karla Karışık Yağmur 🌨️",
        "K" to "Kar Yağışlı ❄️",
        "SCK" to "Sıcak 🌡️",
        "SGK" to "Soğuk 🥶",
        "SIS" to "Sisli 🌫️",
        "PUS" to "Puslu 🌁",
        "GSY" to "Gökgürültülü Sağanak Yağmur ⛈️",
        "HHY" to "Yağışlı 🌧️",
        "KGY" to "Kuvvetli Gökgürültülü Yağmurlu ⛈️"
    )

    private fun safeDouble(value: Any?): Double? {
        if (value == null) return null
        val d = when (value) {
            is Number -> value.toDouble()
            is String -> {
                val trimmed = value.trim()
                if (trimmed.lowercase() == "null" || trimmed.isEmpty()) null else trimmed.toDoubleOrNull()
            }
            else -> null
        }
        return if (d == null || d <= -9000.0) null else d
    }

    private fun formatVeriZamani(v: String?): String? {
        if (v == null) return null
        return try {
            if (v.contains("T")) {
                val cleanStr = v.substring(0, 19)
                val formatterInput = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                val localTime = LocalDateTime.parse(cleanStr, formatterInput)
                val adjustedTime = localTime.plusHours(3)
                val formatterOutput = DateTimeFormatter.ofPattern("HH:mm")
                adjustedTime.format(formatterOutput)
            } else {
                v
            }
        } catch (e: Exception) {
            v
        }
    }

    private fun getWindDirectionText(deg: Double?): String? {
        if (deg == null) return null
        val d = deg
        return when {
            d >= 337.5 || d < 22.5 -> "↓ Kuzey (Yıldız)"
            d >= 22.5 && d < 67.5 -> "↙ Kuzeydoğu (Poyraz)"
            d >= 67.5 && d < 112.5 -> "← Doğu (Gündoğusu)"
            d >= 112.5 && d < 157.5 -> "↖ Güneydoğu (Keşişleme)"
            d >= 157.5 && d < 202.5 -> "↑ Güney (Kıble)"
            d >= 202.5 && d < 247.5 -> "↗ Güneybatı (Lodos)"
            d >= 247.5 && d < 292.5 -> "➡️ Batı (Günbatısı)"
            d >= 292.5 && d < 337.5 -> "↘ Kuzeybatı (Karayel)"
            else -> deg.toString()
        }
    }

    private fun calculateFeltTemperature(
        t: Double,
        rh: Double,
        windSpeedKmH: Double,
        hadiseKodu: String?,
        hour: Int
    ): Double {
        return try {
            val windMS = windSpeedKmH / 3.6
            val e = (rh / 100.0) * 6.105 * Math.exp((17.27 * t) / (237.7 + t))
            val isol = if (hour in 7..18) {
                when (hadiseKodu) {
                    "A" -> 800
                    "AB", "G" -> 600
                    "PB" -> 400
                    "CB", "Y", "HY", "KY", "GYS", "GS", "KKY", "K", "DY" -> 100
                    else -> 200
                }
            } else {
                0
            }
            val qNet = 0.15 * isol
            val felt = t + (0.34 * e) - (0.70 * windMS) + (0.70 * qNet / (windMS + 10.0)) - 4.25
            Math.round(felt * 10.0) / 10.0
        } catch (e: Exception) {
            t
        }
    }

    suspend fun fetchCurrentObservations(): List<Observation> = withContext(Dispatchers.IO) {
        val headers = mapOf(
            "Origin" to "https://mgm.gov.tr",
            "Referer" to "https://mgm.gov.tr/",
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Accept" to "application/json"
        )

        stations.map { station ->
            async {
                val url = "https://servis.mgm.gov.tr/web/sondurumlar?istno=${station.kod}"
                var retries = 0
                val maxRetries = 5
                var result: Observation? = null
                
                while (retries < maxRetries && result == null) {
                    try {
                        val request = Request.Builder()
                            .url(url)
                            .apply {
                                headers.forEach { (key, value) -> addHeader(key, value) }
                            }
                            .build()
    
                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val bodyString = response.body?.string()
                                if (!bodyString.isNullOrEmpty()) {
                                    val list = listMapAdapter.fromJson(bodyString)
                                    if (!list.isNullOrEmpty()) {
                                        val map = list[0]
                                        val rawTime = map["veriZamani"] as? String
                                        val formattedTime = formatVeriZamani(rawTime)
                                        val hadiseKodu = map["hadiseKodu"] as? String
                                        val hadisePair = hadiseHaritasi[hadiseKodu] ?: Pair("❓", hadiseKodu ?: "Bilinmiyor")
    
                                        val t = safeDouble(map["sicaklik"])
                                        val rh = safeDouble(map["nem"])
                                        val ws = safeDouble(map["ruzgarHiz"]) ?: 0.0
    
                                        val feltTemp = if (t != null && rh != null) {
                                            var hour = 12
                                            try {
                                                if (rawTime != null && rawTime.contains("T")) {
                                                    val cleanStr = rawTime.substring(0, 19)
                                                    val dtUtc = LocalDateTime.parse(cleanStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                                                    hour = dtUtc.plusHours(3).hour
                                                }
                                            } catch (e: Exception) {
                                                // Fallback
                                            }
                                            calculateFeltTemperature(t, rh, ws, hadiseKodu, hour)
                                        } else {
                                            t
                                        }
    
                                        result = Observation(
                                            stationCode = station.kod,
                                            stationName = station.ad,
                                            veriZamani = rawTime,
                                            formattedVeriZamani = formattedTime,
                                            hadiseKodu = hadiseKodu,
                                            hadiseEmoji = hadisePair.first,
                                            hadiseMetin = hadisePair.second,
                                            sicaklik = t,
                                            hissedilenSicaklik = feltTemp,
                                            nem = rh,
                                            ruzgarHiz = safeDouble(map["ruzgarHiz"]),
                                            ruzgarYon = getWindDirectionText(safeDouble(map["ruzgarYon"])),
                                            ruzgarHamle = safeDouble(map["ruzgarHamle"]),
                                            aktuelBasinc = safeDouble(map["aktuelBasinc"]),
                                            denizyeIrgBasinc = safeDouble(map["denizyeIrgBasinc"]),
                                            gorus = safeDouble(map["gorus"]),
                                            yagis00Now = safeDouble(map["yagis00Now"]),
                                            yagis10Dk = safeDouble(map["yagis10Dk"]),
                                            yagis1Saat = safeDouble(map["yagis1Saat"]),
                                            yagis12Saat = safeDouble(map["yagis12Saat"]),
                                            yagis24Saat = safeDouble(map["yagis24Saat"]),
                                            karKalinlik = safeDouble(map["karKalinlik"]),
                                            denizSicaklik = safeDouble(map["denizSicaklik"]),
                                            merkezId = safeDouble(map["merkezId"])
                                        )
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (result == null) {
                        retries++
                        if (retries < maxRetries) kotlinx.coroutines.delay(800)
                    }
                }
                result
            }
        }.awaitAll().filterNotNull().filter { it.sicaklik != null }
    }

    suspend fun fetchAlaroForecasts(progressCallback: (Float) -> Unit): List<AlaroForecast> = withContext(Dispatchers.IO) {
        val headers = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Accept" to "application/json",
            "Origin" to "https://www.mgm.gov.tr"
        )

        val enlem = "40.757270059827206_40.75642485666443"
        val boylam = "29.799942970275882_29.835777282714847"
        val step = 15
        val totalHours = 72

        val istanbulZone = ZoneId.of("Europe/Istanbul")
        val nowInIstanbul = ZonedDateTime.now(istanbulZone)
        val startDateTime = nowInIstanbul.truncatedTo(ChronoUnit.HOURS)

        val semaphore = Semaphore(10)
        var completedCount = 0

        val jobs = (0 until totalHours).map { i ->
            async {
                semaphore.withPermit {
                    val targetDateTime = startDateTime.plusMinutes(15L * i)
                    val formattedParamDate = targetDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    val encodedDate = URLEncoder.encode(formattedParamDate, "UTF-8")
                    val url = "https://modelservis.mgm.gov.tr/web/modeltahmin/lokasyon/light3?enlem=$enlem&boylam=$boylam&tarih=$encodedDate&step=$step"

                    var forecastResult: AlaroForecast? = null
                    try {
                        val request = Request.Builder()
                            .url(url)
                            .apply {
                                headers.forEach { (key, value) -> addHeader(key, value) }
                            }
                            .build()

                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val bodyString = response.body?.string()
                                if (!bodyString.isNullOrEmpty()) {
                                    val list = listMapAdapter.fromJson(bodyString)
                                    if (!list.isNullOrEmpty()) {
                                        val map = list[0]
                                        val temp = safeDouble(map["sicaklik"]) ?: 0.0
                                        val felt = safeDouble(map["hissedilenSicaklik"]) ?: temp
                                        val windSpeed = safeDouble(map["ruzgarHizi"]) ?: 0.0
                                        val windDir = safeDouble(map["ruzgarYonu"]) ?: 0.0
                                        val precip = safeDouble(map["saatlikToplamYagis"])
                                            ?: safeDouble(map["yagis"])
                                            ?: safeDouble(map["toplamYagis"])
                                            ?: safeDouble(map["saatlikYagis"])
                                            ?: 0.0
                                        val humidity = safeDouble(map["nem"]) ?: 50.0
                                        val cloud = safeDouble(map["bulutKapaliligi"]) ?: 0.0
                                        val pressure = safeDouble(map["basinc"]) ?: 0.0
                                        val snow = safeDouble(map["karYuksekligi"]) ?: 0.0
                                        val rawHadise = map["hadise"] as? String ?: ""
                                        val hadiseText = alaroHadiseHaritasi[rawHadise] ?: rawHadise

                                        val windArrows = listOf("⬇️", "↙️", "⬅️", "↖️", "⬆️", "↗️", "➡️", "↘️")
                                        val windArrowIdx = (Math.round(windDir / 45.0) % 8).toInt()
                                        val windArrow = windArrows.getOrElse(windArrowIdx) { "➡️" }

                                        forecastResult = AlaroForecast(
                                            dateTime = targetDateTime.toLocalDateTime(),
                                            formattedTime = targetDateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                            formattedDate = targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                            sicaklik = temp,
                                            hissedilenSicaklik = felt,
                                            ruzgarHizi = windSpeed,
                                            ruzgarYonu = windDir,
                                            ruzgarOk = windArrow,
                                            saatlikToplamYagis = precip,
                                            nem = humidity,
                                            bulutKapaliligi = cloud,
                                            basinc = pressure,
                                            karYuksekligi = snow,
                                            hadise = rawHadise,
                                            hadiseMetin = hadiseText
                                        )
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        synchronized(this@WeatherRepository) {
                            completedCount++
                            progressCallback(completedCount.toFloat() / totalHours.toFloat())
                        }
                    }
                    forecastResult
                }
            }
        }

        jobs.awaitAll().filterNotNull().sortedBy { it.dateTime }
    }

    suspend fun fetchAlaro3DayForecasts(progressCallback: (Float) -> Unit): List<AlaroForecast> = withContext(Dispatchers.IO) {
        val headers = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Accept" to "application/json",
            "Origin" to "https://www.mgm.gov.tr"
        )

        val enlem = "40.7575190231096_40.7554254596625"
        val boylam = "29.79602930252441_29.830257961875766"
        val step = 60
        val totalHours = 72

        val istanbulZone = ZoneId.of("Europe/Istanbul")
        val nowInIstanbul = ZonedDateTime.now(istanbulZone)
        val startDateTime = nowInIstanbul.truncatedTo(ChronoUnit.HOURS)

        val semaphore = Semaphore(10)
        var completedCount = 0

        val jobs = (0 until totalHours).map { i ->
            async {
                semaphore.withPermit {
                    val targetDateTime = startDateTime.plusHours(i.toLong())
                    val formattedParamDate = targetDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00"))
                    val encodedDate = URLEncoder.encode(formattedParamDate, "UTF-8")
                    val url = "https://modelservis.mgm.gov.tr/web/modeltahmin/lokasyon/light3?enlem=$enlem&boylam=$boylam&tarih=$encodedDate&step=$step"

                    var forecastResult: AlaroForecast? = null
                    try {
                        val request = Request.Builder()
                            .url(url)
                            .apply {
                                headers.forEach { (key, value) -> addHeader(key, value) }
                            }
                            .build()

                        client.newCall(request).execute().use { response ->
                            if (response.isSuccessful) {
                                val bodyString = response.body?.string()
                                if (!bodyString.isNullOrEmpty()) {
                                    val list = listMapAdapter.fromJson(bodyString)
                                    if (!list.isNullOrEmpty()) {
                                        val map = list[0]
                                        val temp = safeDouble(map["sicaklik"]) ?: 0.0
                                        val felt = safeDouble(map["hissedilenSicaklik"]) ?: temp
                                        val windSpeed = safeDouble(map["ruzgarHizi"]) ?: 0.0
                                        val windDir = safeDouble(map["ruzgarYonu"]) ?: 0.0
                                        val precip = safeDouble(map["saatlikToplamYagis"])
                                            ?: safeDouble(map["yagis"])
                                            ?: safeDouble(map["toplamYagis"])
                                            ?: safeDouble(map["saatlikYagis"])
                                            ?: 0.0
                                        val humidity = safeDouble(map["nem"]) ?: 50.0
                                        val cloud = safeDouble(map["bulutKapaliligi"]) ?: 0.0
                                        val pressure = safeDouble(map["basinc"]) ?: 0.0
                                        val snow = safeDouble(map["karYuksekligi"]) ?: 0.0
                                        val rawHadise = map["hadise"] as? String ?: ""
                                        val hadiseText = alaroHadiseHaritasi[rawHadise] ?: rawHadise

                                        val windArrows = listOf("⬇️", "↙️", "⬅️", "↖️", "⬆️", "↗️", "➡️", "↘️")
                                        val windArrowIdx = (Math.round(windDir / 45.0) % 8).toInt()
                                        val windArrow = windArrows.getOrElse(windArrowIdx) { "➡️" }

                                        forecastResult = AlaroForecast(
                                            dateTime = targetDateTime.toLocalDateTime(),
                                            formattedTime = targetDateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                            formattedDate = targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                            sicaklik = temp,
                                            hissedilenSicaklik = felt,
                                            ruzgarHizi = windSpeed,
                                            ruzgarYonu = windDir,
                                            ruzgarOk = windArrow,
                                            saatlikToplamYagis = precip,
                                            nem = humidity,
                                            bulutKapaliligi = cloud,
                                            basinc = pressure,
                                            karYuksekligi = snow,
                                            hadise = rawHadise,
                                            hadiseMetin = hadiseText
                                        )
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        synchronized(this@WeatherRepository) {
                            completedCount++
                            progressCallback(completedCount.toFloat() / totalHours.toFloat())
                        }
                    }
                    forecastResult
                }
            }
        }

        jobs.awaitAll().filterNotNull().sortedBy { it.dateTime }
    }

    private val ukmoWmoMap = mapOf(
        0 to "Açık ☀️", 1 to "Çoğunlukla Açık 🌤️", 2 to "Parçalı Bulutlu ⛅", 3 to "Kapalı ☁️",
        45 to "Sisli 🌫️", 48 to "Kırağı Sisli 🌫️❄️", 51 to "Hafif Çiseleme 🌧️", 53 to "Orta Çiseleme 🌧️",
        55 to "Yoğun Çiseleme 🌧️", 56 to "Hafif Dondurucu Çiseleme 🥶🌧️", 57 to "Yoğun Dondurucu Çiseleme 🥶🌧️",
        61 to "Hafif Yağmurlu 🌦️", 63 to "Yağmurlu 🌧️", 65 to "Kuvvetli Yağmurlu ⛈️",
        66 to "Hafif Dondurucu Yağmur 🥶🌧️", 67 to "Kuvvetli Dondurucu Yağmur 🥶⛈️",
        71 to "Hafif Kar Yağışlı 🌨️", 73 to "Kar Yağışlı ❄️", 75 to "Yoğun Kar Yağışlı ☃️",
        77 to "Kar Taneleri ❄️", 80 to "Hafif Sağanak Yağışlı 🌦️", 81 to "Sağanak Yağışlı 🌧️🌀",
        82 to "Şiddetli Sağanak Yağışlı ⛈️🌊", 85 to "Hafif Kar Sağanaklı 🌨️❄️", 86 to "Yoğun Kar Sağanaklı ☃️❄️",
        95 to "Gökgürültülü Sağanak ⛈️", 96 to "Dolu Yağışlı Gökgürültülü Sağanak ⛈️🌨️",
        99 to "Şiddetli Dolu Yağışlı Gökgürültülü Sağanak ⛈️☠️"
    )

    private fun getRuzgarOku(derece: Double?): String {
        if (derece == null) return "💨"
        val yonler = listOf("⬇️", "↙️", "⬅️", "↖️", "⬆️", "↗️", "➡️", "↘️")
        val indeks = (Math.round(derece / 45.0) % 8).toInt()
        return yonler.getOrElse(indeks) { "💨" }
    }

    private fun getDoubleListValue(list: List<*>?, index: Int): Double {
        if (list == null || index >= list.size) return 0.0
        return safeDouble(list[index]) ?: 0.0
    }

    private fun hissedilenSicaklikHesapla(tc: Double, rh: Double, vKmh: Double): Double {
        val tf = (tc * 9.0 / 5.0) + 32.0
        val vMph = vKmh / 1.609344

        return if (tc <= 10.0 && vKmh >= 4.8) {
            val wcF = 35.74 + (0.6215 * tf) - (35.75 * Math.pow(vMph, 0.16)) + (0.4275 * tf * Math.pow(vMph, 0.16))
            Math.round(((wcF - 32.0) * 5.0 / 9.0) * 10.0) / 10.0
        } else if (tc >= 26.7) {
            var hiF = 0.5 * (tf + 61.0 + ((tf - 68.0) * 1.2) + (rh * 0.094))
            if (hiF >= 80.0) {
                hiF = (-42.379 + (2.04901523 * tf) + (10.14333127 * rh) -
                        (0.22475541 * tf * rh) - (0.00683783 * tf * tf) -
                        (0.05481717 * rh * rh) + (0.00122874 * tf * tf * rh) +
                        (0.00085282 * tf * rh * rh) - (0.00000199 * tf * tf * rh * rh))

                if (rh < 13.0 && tf >= 80.0 && tf <= 112.0) {
                    val adjustment = ((13.0 - rh) / 4.0) * Math.sqrt((17.0 - Math.abs(tf - 95.0)) / 17.0)
                    hiF -= adjustment
                } else if (rh > 85.0 && tf >= 80.0 && tf <= 87.0) {
                    val adjustment = ((rh - 85.0) / 10.0) * ((87.0 - tf) / 5.0)
                    hiF += adjustment
                }
            }
            Math.round(((hiF - 32.0) * 5.0 / 9.0) * 10.0) / 10.0
        } else {
            val e = (rh / 100.0) * 6.105 * Math.exp((17.27 * tc) / (237.7 + tc))
            val vMs = vKmh / 3.6
            val atC = tc + (0.33 * e) - (0.70 * vMs) - 4.00
            Math.round(atC * 10.0) / 10.0
        }
    }

    private fun durumTahminEt(bulut: Double, yagis: Double, kar: Double): String {
        return when {
            kar > 0.5 -> "Kar Yağışlı ❄️"
            yagis > 5.0 -> "Kuvvetli Yağmurlu ⛈️"
            yagis > 0.5 -> "Yağmurlu 🌧️"
            yagis > 0.0 -> "Hafif Yağmurlu 🌦️"
            bulut > 80.0 -> "Çok Bulutlu ☁️"
            bulut > 40.0 -> "Parçalı Bulutlu ⛅"
            bulut > 15.0 -> "Az Bulutlu 🌤️"
            else -> "Açık ☀️"
        }
    }

    suspend fun fetchUkmoForecasts(): List<UkmoForecast> = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast" +
                "?latitude=40.754" +
                "&longitude=29.834" +
                "&hourly=temperature_2m,relative_humidity_2m,precipitation,weather_code," +
                "wind_speed_10m,wind_direction_10m,wind_gusts_10m,cloud_cover," +
                "cloud_cover_low,cloud_cover_mid,cloud_cover_high,cloud_cover_2m," +
                "wet_bulb_temperature_2m,cape,convective_inhibition," +
                "snowfall,visibility,apparent_temperature,dew_point_2m," +
                "shortwave_radiation,direct_radiation,diffuse_radiation," +
                "direct_normal_irradiance,global_tilted_irradiance,terrestrial_radiation" +
                "&models=ukmo_seamless" +
                "&timezone=Europe/Istanbul" +
                "&tilt=45" +
                "&azimuth=0"

        try {
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrEmpty()) {
                        val rootMap = mapAdapter.fromJson(bodyString)
                        val hourlyMap = rootMap?.get("hourly") as? Map<String, Any?>
                        val times = hourlyMap?.get("time") as? List<*>
                        if (!times.isNullOrEmpty()) {
                            val temp2mList = hourlyMap["temperature_2m"] as? List<*>
                            val relHum2mList = hourlyMap["relative_humidity_2m"] as? List<*>
                            val precipList = hourlyMap["precipitation"] as? List<*>
                            val weatherCodeList = hourlyMap["weather_code"] as? List<*>
                            val windSpeed10mList = hourlyMap["wind_speed_10m"] as? List<*>
                            val windDirection10mList = hourlyMap["wind_direction_10m"] as? List<*>
                            val windGusts10mList = hourlyMap["wind_gusts_10m"] as? List<*>
                            val cloudCoverList = hourlyMap["cloud_cover"] as? List<*>
                            val cloudCoverLowList = hourlyMap["cloud_cover_low"] as? List<*>
                            val cloudCoverMidList = hourlyMap["cloud_cover_mid"] as? List<*>
                            val cloudCoverHighList = hourlyMap["cloud_cover_high"] as? List<*>
                            val cloudCover2mList = hourlyMap["cloud_cover_2m"] as? List<*>
                            val wetBulbTempList = hourlyMap["wet_bulb_temperature_2m"] as? List<*>
                            val capeList = hourlyMap["cape"] as? List<*>
                            val cinList = hourlyMap["convective_inhibition"] as? List<*>
                            val snowfallList = hourlyMap["snowfall"] as? List<*>
                            val visibilityList = hourlyMap["visibility"] as? List<*>
                            val apparentTempList = hourlyMap["apparent_temperature"] as? List<*>
                            val dewPoint2mList = hourlyMap["dew_point_2m"] as? List<*>
                            val shortwaveList = hourlyMap["shortwave_radiation"] as? List<*>
                            val directList = hourlyMap["direct_radiation"] as? List<*>
                            val diffuseList = hourlyMap["diffuse_radiation"] as? List<*>
                            val dniList = hourlyMap["direct_normal_irradiance"] as? List<*>
                            val gtiList = hourlyMap["global_tilted_irradiance"] as? List<*>
                            val terrestrialList = hourlyMap["terrestrial_radiation"] as? List<*>

                            val list = mutableListOf<UkmoForecast>()
                            val formatterInput = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

                            for (i in times.indices) {
                                val timeStr = times[i] as? String ?: continue
                                val localTime = try {
                                    LocalDateTime.parse(timeStr, formatterInput)
                                } catch (e: Exception) {
                                    continue
                                }

                                val temp = getDoubleListValue(temp2mList, i)
                                val rh = getDoubleListValue(relHum2mList, i)
                                val precip = getDoubleListValue(precipList, i)
                                val wCode = getDoubleListValue(weatherCodeList, i).toInt()
                                val windSpeed = getDoubleListValue(windSpeed10mList, i)
                                val windDir = getDoubleListValue(windDirection10mList, i)
                                val windGust = getDoubleListValue(windGusts10mList, i)
                                val cloudCover = getDoubleListValue(cloudCoverList, i)
                                val cloudCoverLow = getDoubleListValue(cloudCoverLowList, i)
                                val cloudCoverMid = getDoubleListValue(cloudCoverMidList, i)
                                val cloudCoverHigh = getDoubleListValue(cloudCoverHighList, i)
                                val cloudCover2m = getDoubleListValue(cloudCover2mList, i)
                                val wetBulb = getDoubleListValue(wetBulbTempList, i)
                                val cape = getDoubleListValue(capeList, i)
                                val cin = getDoubleListValue(cinList, i)
                                val snowfall = getDoubleListValue(snowfallList, i)
                                val visibility = getDoubleListValue(visibilityList, i)
                                val apparent = getDoubleListValue(apparentTempList, i)
                                val dewPoint = getDoubleListValue(dewPoint2mList, i)
                                val shortwave = getDoubleListValue(shortwaveList, i)
                                val direct = getDoubleListValue(directList, i)
                                val diffuse = getDoubleListValue(diffuseList, i)
                                val dni = getDoubleListValue(dniList, i)
                                val gti = getDoubleListValue(gtiList, i)
                                val terrestrial = getDoubleListValue(terrestrialList, i)

                                val condition = ukmoWmoMap[wCode] ?: "Bilinmiyor"
                                val windArrow = getRuzgarOku(windDir)

                                list.add(
                                    UkmoForecast(
                                        dateTime = localTime,
                                        formattedTime = localTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                        formattedDate = localTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                        sicaklik = temp,
                                        hissedilenSicaklik = apparent,
                                        nem = rh,
                                        yagis = precip,
                                        weatherCode = wCode,
                                        durum = condition,
                                        ruzgarHizi = windSpeed,
                                        ruzgarYonu = windDir,
                                        ruzgarOk = windArrow,
                                        ruzgarHamlesi = windGust,
                                        toplamBulut = cloudCover,
                                        alcakBulut = cloudCoverLow,
                                        ortaBulut = cloudCoverMid,
                                        yuksekBulut = cloudCoverHigh,
                                        sis2m = cloudCover2m,
                                        islakHazneSicakligi = wetBulb,
                                        cape = cape,
                                        cin = cin,
                                        karYagisi = snowfall,
                                        gorusUzakligi = visibility / 1000.0,
                                        ciyNoktasi = dewPoint,
                                        ghi = shortwave,
                                        directRadiation = direct,
                                        diffuseRadiation = diffuse,
                                        dni = dni,
                                        gti = gti,
                                        terrestrialRadiation = terrestrial
                                    )
                                )
                            }
                            return@withContext list
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext emptyList<UkmoForecast>()
    }

    suspend fun fetchImgwAlaroForecasts(): List<ImgwAlaroForecast> = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val url = "https://forecastapi.imgw.pl/get?lat=40.76&lon=29.81&z=0&m=alaro&lasttime=0&p=mobile&t=cf91c88878cda27ceria76da208a06aeb9&_nocache=$timestamp"
        
        val headers = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Accept" to "application/json",
            "Cache-Control" to "no-cache, no-store, must-revalidate",
            "Pragma" to "no-cache",
            "Expires" to "0"
        )

        try {
            val request = Request.Builder()
                .url(url)
                .apply {
                    headers.forEach { (key, value) -> addHeader(key, value) }
                }
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrEmpty()) {
                        val rootMap = mapAdapter.fromJson(bodyString)
                        val forecastsList = rootMap?.get("Data") as? List<*>
                        if (!forecastsList.isNullOrEmpty()) {
                            val list = mutableListOf<ImgwAlaroForecast>()
                            val tzIst = ZoneId.of("Europe/Istanbul")

                            for (item in forecastsList) {
                                val map = item as? Map<*, *> ?: continue
                                val dateStr = map["Date"] as? String ?: continue

                                val localDateTime = try {
                                    val cleanDate = if (dateStr.contains("T")) dateStr.substring(0, 19) else dateStr
                                    val parsedUtc = LocalDateTime.parse(cleanDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                                    val utcZonedDateTime = parsedUtc.atZone(ZoneId.of("UTC"))
                                    val istanbulZonedDateTime = utcZonedDateTime.withZoneSameInstant(tzIst)
                                    istanbulZonedDateTime.toLocalDateTime()
                                } catch (e: Exception) {
                                    continue
                                }

                                val tempK = safeDouble(map["Temperature"]) ?: 273.15
                                val tempC = Math.round((tempK - 273.15) * 10.0) / 10.0

                                val ciyK = safeDouble(map["Dewpoint_Temperature"]) ?: tempK
                                val ciyC = Math.round((ciyK - 273.15) * 10.0) / 10.0

                                val windSpeedMs = safeDouble(map["Wind_Speed"]) ?: 0.0
                                val windSpeedKmh = Math.round((windSpeedMs * 3.6) * 10.0) / 10.0

                                val windGustMs = safeDouble(map["Wind_Gust"]) ?: 0.0
                                val windGustKmh = Math.round((windGustMs * 3.6) * 10.0) / 10.0

                                val windDir = safeDouble(map["Wind_Dir"]) ?: 0.0
                                val windArrow = getRuzgarOku(windDir)

                                val nem = safeDouble(map["Humidity"]) ?: 50.0
                                val yagis = safeDouble(map["Precipitation"]) ?: 0.0
                                val bulut = safeDouble(map["Cloud"]) ?: 0.0
                                val kar = safeDouble(map["Snow"]) ?: 0.0

                                val basincPa = safeDouble(map["PressureMSL"]) ?: safeDouble(map["Pressure"]) ?: 101300.0
                                val basincHpa = Math.round((basincPa / 100.0) * 10.0) / 10.0

                                val apparentC = hissedilenSicaklikHesapla(tempC, nem, windSpeedKmh)
                                val conditionText = durumTahminEt(bulut, yagis, kar)

                                list.add(
                                    ImgwAlaroForecast(
                                        dateTime = localDateTime,
                                        formattedTime = localDateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                        formattedDate = localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                        sicaklik = tempC,
                                        hissedilenSicaklik = apparentC,
                                        ciyNoktasi = ciyC,
                                        ruzgarHizi = windSpeedKmh,
                                        ruzgarHamlesi = windGustKmh,
                                        ruzgarYonu = windDir,
                                        ruzgarOk = windArrow,
                                        yagis = yagis,
                                        nem = nem,
                                        bulut = bulut,
                                        basinc = basincHpa,
                                        kar = kar,
                                        hadiseMetin = conditionText
                                    )
                                )
                            }
                            return@withContext list
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext emptyList<ImgwAlaroForecast>()
    }

    suspend fun fetchRadarAnalysis(): RadarAnalysisResult? = withContext(Dispatchers.IO) {
        val url = "https://www.mgm.gov.tr/FTPDATA/uzal/radar/brs/brsppi15.jpg"
        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .addHeader("Referer", "https://mgm.gov.tr/")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val inputStream = response.body?.byteStream()
                    if (inputStream != null) {
                        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                        if (bitmap != null) {
                            return@withContext analyzeRadarBitmap(bitmap)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    private fun analyzeRadarBitmap(bitmap: android.graphics.Bitmap): RadarAnalysisResult {
        val h = bitmap.height
        val w = bitmap.width
        val intensityMap = Array(h) { IntArray(w) }
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val pixel = pixels[y * w + x]
                val R = (pixel shr 16) and 0xFF
                val G = (pixel shr 8) and 0xFF
                val B = pixel and 0xFF

                val isGray = Math.abs(R - G) < 20 && Math.abs(G - B) < 20
                val isSea = R in 151..189 && G in 181..219 && B in 201..239
                val isLand = R in 221..255 && G in 221..255 && B in 191..229
                val isBg = isGray || isSea || isLand

                if (!isBg) {
                    if (B > 200 && G > 180 && R < 120) {
                        intensityMap[y][x] = 1
                    } else if (B > 160 && R < 130 && G < 180) {
                        intensityMap[y][x] = 2
                    } else if (G > 150 && R < 130 && B < 130) {
                        intensityMap[y][x] = 3
                    } else if (R > 180 && G > 140 && B < 100) {
                        intensityMap[y][x] = 4
                    } else if (R > 180 && G < 100 && B < 100) {
                        intensityMap[y][x] = 5
                    }
                }
            }
        }

        // Morphological Gradient
        val gradientMap = Array(h) { IntArray(w) }
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                var maxVal = 0
                var minVal = 5
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val v = intensityMap[y + dy][x + dx]
                        if (v > maxVal) maxVal = v
                        if (v < minVal) minVal = v
                    }
                }
                gradientMap[y][x] = maxVal - minVal
            }
        }

        // BFS Connected Component Labeling
        val labeledCells = Array(h) { IntArray(w) }
        var nextLabel = 1
        val visited = Array(h) { BooleanArray(w) }

        for (y in 0 until h) {
            for (x in 0 until w) {
                if (intensityMap[y][x] > 0 && !visited[y][x]) {
                    val queue = java.util.ArrayDeque<Pair<Int, Int>>()
                    queue.add(Pair(x, y))
                    visited[y][x] = true

                    while (queue.isNotEmpty()) {
                        val curr = queue.poll() ?: break
                        val cx_ = curr.first
                        val cy_ = curr.second
                        labeledCells[cy_][cx_] = nextLabel

                        for (dy in -1..1) {
                            for (dx in -1..1) {
                                if (dx == 0 && dy == 0) continue
                                val nx = cx_ + dx
                                val ny = cy_ + dy
                                if (nx in 0 until w && ny in 0 until h) {
                                    if (intensityMap[ny][nx] > 0 && !visited[ny][nx]) {
                                        visited[ny][nx] = true
                                        queue.add(Pair(nx, ny))
                                    }
                                }
                            }
                        }
                    }
                    nextLabel++
                }
            }
        }

        // Radar projection
        val latRadar = 40.538333
        val lonRadar = 29.903333
        val latTarget = 40.7640
        val lonTarget = 29.8117
        val cx = 360.0
        val cy = 360.0
        val pxPerKm = 360.0 / 400.0

        val latMid = Math.toRadians((latRadar + latTarget) / 2.0)
        val mPerDegLat = 111132.954 - 559.822 * Math.cos(2.0 * latMid)
        val mPerDegLon = 111412.84 * Math.cos(latMid) - 93.5 * Math.cos(3.0 * latMid)
        val dyKm = (latTarget - latRadar) * (mPerDegLat / 1000.0)
        val dxKm = (lonTarget - lonRadar) * (mPerDegLon / 1000.0)

        val targetX = ((cx + dxKm * pxPerKm) - 3.0).toInt()
        val targetY = ((cy - dyKm * pxPerKm) - 5.0).toInt()

        // Coverage calculations
        val searchRadiusPx = (100 * pxPerKm).toInt()
        val yMin = Math.max(0, targetY - searchRadiusPx)
        val yMax = Math.min(h - 1, targetY + searchRadiusPx)
        val xMin = Math.max(0, targetX - searchRadiusPx)
        val xMax = Math.min(w - 1, targetX + searchRadiusPx)

        var nonZeroCount = 0
        var totalCells = 0
        for (y in yMin..yMax) {
            for (x in xMin..xMax) {
                totalCells++
                if (intensityMap[y][x] > 0) {
                    nonZeroCount++
                }
            }
        }
        val coverage = if (totalCells > 0) nonZeroCount.toDouble() / totalCells else 0.0

        val weatherForecast = when {
            coverage == 0.0 -> "Tamamen Açık, Bulutsuz"
            coverage < 0.05 -> "Az Bulutlu, Yüksek Görüş Mesafesi"
            coverage < 0.15 -> "Parçalı Bulutlu, Kararsız Atmosfer"
            coverage < 0.30 -> "Çok Bulutlu, Aktif Kütle Geçişleri"
            else -> "Kapalı Stratiform, Kesintisiz Yağış"
        }

        // Regionprops computation
        val areas = IntArray(nextLabel)
        val sumX = DoubleArray(nextLabel)
        val sumY = DoubleArray(nextLabel)
        val maxIntensity = IntArray(nextLabel)
        val pixelsByLabel = Array(nextLabel) { mutableListOf<Pair<Int, Int>>() }

        for (y in 0 until h) {
            for (x in 0 until w) {
                val label = labeledCells[y][x]
                if (label > 0) {
                    areas[label]++
                    sumX[label] += x.toDouble()
                    sumY[label] += y.toDouble()
                    if (intensityMap[y][x] > maxIntensity[label]) {
                        maxIntensity[label] = intensityMap[y][x]
                    }
                    pixelsByLabel[label].add(Pair(x, y))
                }
            }
        }

        val threats = mutableListOf<RadarThreat>()
        var coreIntensity = 0

        for (l in 1 until nextLabel) {
            val area = areas[l]
            if (area < 6) continue

            val c_x = sumX[l] / area
            val c_y = sumY[l] / area

            val distToTargetKm = Math.sqrt((c_x - targetX) * (c_x - targetX) + (c_y - targetY) * (c_y - targetY)) / pxPerKm
            if (distToTargetKm > 300.0) continue

            val max_int = maxIntensity[l]
            if (targetY in 0 until h && targetX in 0 until w && intensityMap[targetY][targetX] > 0 && distToTargetKm < (15.0 / pxPerKm)) {
                if (max_int > coreIntensity) {
                    coreIntensity = max_int
                }
            }

            // Moments
            var mu20 = 0.0
            var mu02 = 0.0
            var mu11 = 0.0
            for (p in pixelsByLabel[l]) {
                val dx_ = p.first - c_x
                val dy_ = p.second - c_y
                mu20 += dx_ * dx_
                mu02 += dy_ * dy_
                mu11 += dx_ * dy_
            }
            mu20 /= area
            mu02 /= area
            mu11 /= area

            val common = Math.sqrt((mu20 - mu02) * (mu20 - mu02) + 4.0 * mu11 * mu11)
            val a_sq = (mu20 + mu02 + common) / 2.0
            val b_sq = Math.max(0.0, (mu20 + mu02 - common) / 2.0)

            val majorAxisLength = 4.0 * Math.sqrt(a_sq)
            val minorAxisLength = 4.0 * Math.sqrt(b_sq)
            val eccentricity = if (a_sq > 0) Math.sqrt(1.0 - (b_sq / a_sq)) else 0.0
            val orientation = 0.5 * Math.atan2(2.0 * mu11, mu20 - mu02)

            // Perimeter approximation
            var perimeter = 0.0
            for (p in pixelsByLabel[l]) {
                val px = p.first
                val py = p.second
                var isEdge = false
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val nx = px + dx
                        val ny = py + dy
                        if (nx !in 0 until w || ny !in 0 until h || labeledCells[ny][nx] != l) {
                            isEdge = true
                            break
                        }
                    }
                    if (isEdge) break
                }
                if (isEdge) {
                    perimeter += 1.0
                }
            }
            if (perimeter == 0.0) perimeter = 1.0
            val circularity = (4.0 * Math.PI * area) / (perimeter * perimeter)

            val stormNature = when {
                circularity < 0.25 && max_int >= 4 -> "⚡ Şiddetli Türbülans / Süperhücre"
                eccentricity > 0.85 -> "🌧️ Squall Line (Hat Fırtınası)"
                else -> "💧 Standart Sağanak"
            }

            // Weighted centroid
            var weightedSumX = 0.0
            var weightedSumY = 0.0
            var totalWeight = 0.0
            for (p in pixelsByLabel[l]) {
                val weight = intensityMap[p.second][p.first].toDouble()
                weightedSumX += p.first * weight
                weightedSumY += p.second * weight
                totalWeight += weight
            }
            val w_x = if (totalWeight > 0.0) weightedSumX / totalWeight else c_x
            val w_y = if (totalWeight > 0.0) weightedSumY / totalWeight else c_y

            val shear_dx = w_x - c_x
            val shear_dy = w_y - c_y
            val shear_mag = Math.sqrt(shear_dx * shear_dx + shear_dy * shear_dy) + 0.0001

            var base_dir_x = Math.sin(orientation)
            var base_dir_y = Math.cos(orientation)
            if (base_dir_x < 0.0) {
                base_dir_x = -base_dir_x
                base_dir_y = -base_dir_y
            }

            var dir_x = (base_dir_x * 0.7) + ((shear_dx / shear_mag) * 0.3)
            var dir_y = (base_dir_y * 0.7) + ((shear_dy / shear_mag) * 0.3)

            val vec_mag = Math.sqrt(dir_x * dir_x + dir_y * dir_y) + 0.0001
            dir_x /= vec_mag
            dir_y /= vec_mag

            val coriolisAngle = Math.atan2(dir_y, dir_x) - Math.toRadians(7.0)
            val dir_x_cor = Math.cos(coriolisAngle)
            val dir_y_cor = Math.sin(coriolisAngle)

            val v_x = targetX - c_x
            val v_y = targetY - c_y
            val crossDistKm = Math.abs(v_x * dir_y_cor - v_y * dir_x_cor) / pxPerKm
            val dotProduct = (v_x * dir_x_cor) + (v_y * dir_y_cor)

            val cloudWidthKm = (minorAxisLength / pxPerKm) + 12.0
            val baseProb = (100.0 - (crossDistKm / cloudWidthKm) * 50.0).toInt()

            if (dotProduct < 0.0) continue
            if (crossDistKm > cloudWidthKm) continue

            val isOverWater = (c_x < targetX) && (Math.abs(c_y - targetY) < (15.0 / pxPerKm))

            var cellGrad = 0
            for (p in pixelsByLabel[l]) {
                val gVal = gradientMap[p.second][p.first]
                if (gVal > cellGrad) cellGrad = gVal
            }

            var bayesianProb = baseProb.toDouble()
            if (isOverWater) bayesianProb += 12.0
            if (max_int >= 4) bayesianProb += 10.0
            if (cellGrad >= 3) bayesianProb += 8.0
            if (circularity < 0.3) bayesianProb -= 15.0

            val finalProb = Math.min(98.0, Math.max(25.0, bayesianProb)).toInt()

            val stormSpeedKmh = 35.0 + (max_int * 5.0) + (shear_mag * 2.0)
            val leadX = c_x + (dir_x_cor * (majorAxisLength / 2.0))
            val leadY = c_y + (dir_y_cor * (majorAxisLength / 2.0))
            val distToLeadKm = Math.sqrt((targetX - leadX) * (targetX - leadX) + (targetY - leadY) * (targetY - leadY)) / pxPerKm

            val etaMin = ((distToLeadKm / stormSpeedKmh) * 60.0).toInt()
            val cloudLengthKm = majorAxisLength / pxPerKm
            val durationMin = ((cloudLengthKm / stormSpeedKmh) * 60.0).toInt()

            val statusStr = when {
                etaMin <= 5 -> "GİRİŞ YAPIYOR"
                etaMin <= 20 -> "AKTİF YAKLAŞIM"
                else -> "Yörüngeye Girdi"
            }

            val solidity = area.toDouble() / (majorAxisLength * minorAxisLength * Math.PI / 4.0).coerceAtLeast(1.0)
            val decayRate = if (solidity > 0.65) 0.998 else 0.990

            if (finalProb > 20 && etaMin >= 0) {
                threats.add(
                    RadarThreat(
                        cx = c_x, cy = c_y, wx = w_x, wy = w_y,
                        dirX = dir_x_cor, dirY = dir_y_cor,
                        maxInt = max_int, prob = finalProb, status = statusStr,
                        eta = etaMin, duration = durationMin, nature = stormNature,
                        decay = decayRate, widthKm = cloudWidthKm,
                        speedPxMin = (stormSpeedKmh * pxPerKm) / 60.0
                    )
                )
            }
        }

        threats.sortByDescending { it.prob }

        val durumRenkleri = mapOf(
            0 to "Temiz",
            1 to "Çok Hafif",
            2 to "Hafif Yağış",
            3 to "Orta Sağanak",
            4 to "Kuvvetli Yağış",
            5 to "Şiddetli Fırtına"
        )

        // 6-hour simulation
        val timeSteps = (0..360 step 10).toList()
        val predictedDbz = DoubleArray(timeSteps.size)

        for (stepIdx in timeSteps.indices) {
            val tMin = timeSteps[stepIdx]
            var maxValAtStep = 0.0
            if (tMin == 0 && coreIntensity > 0) {
                maxValAtStep = coreIntensity.toDouble()
            }

            for (t in threats) {
                if (t.prob < 45) continue

                val stX = t.cx + t.dirX * t.speedPxMin * tMin
                val stY = t.cy + t.dirY * t.speedPxMin * tMin

                val distPx = Math.sqrt((targetX - stX) * (targetX - stX) + (targetY - stY) * (targetY - stY))
                val distKm = distPx / pxPerKm

                val expandedWidth = t.widthKm + (0.05 * tMin)

                if (distKm < expandedWidth) {
                    val currentDecay = Math.pow(t.decay, tMin.toDouble())
                    val spatialFactor = Math.exp(-(distKm * distKm) / (2.0 * expandedWidth * expandedWidth))
                    val contribution = t.maxInt * spatialFactor * currentDecay
                    if (contribution > maxValAtStep) {
                        maxValAtStep = contribution
                    }
                }
            }
            predictedDbz[stepIdx] = maxValAtStep
        }

        // Timeline report
        val timeline6h = mutableListOf<RadarTimelineStep>()
        for (i in 0 until 6) {
            val startT = i * 60
            val endT = (i + 1) * 60
            var peakLevel = 0.0
            for (stepIdx in timeSteps.indices) {
                val tMin = timeSteps[stepIdx]
                if (tMin in startT..endT) {
                    if (predictedDbz[stepIdx] > peakLevel) {
                        peakLevel = predictedDbz[stepIdx]
                    }
                }
            }
            val roundedPeak = Math.round(peakLevel).toInt()
            val desc = if (roundedPeak > 0) {
                durumRenkleri[roundedPeak] ?: "Temiz"
            } else {
                if (coverage > 0.3) "Çok Bulutlu"
                else if (coverage > 0.1) "Parçalı Bulutlu"
                else "Açık / Temiz"
            }
            timeline6h.add(RadarTimelineStep(time = "T+${i + 1} Saat", desc = desc, level = roundedPeak))
        }

        // Level to dBZ and Rain Rates conversion
        val dbzProjection = predictedDbz.map { v ->
            if (v <= 0.0) 0.0 else 10.0 + (v * 10.0)
        }
        val rainRates = dbzProjection.map { dbz ->
            if (dbz < 15.0) 0.0 else Math.pow(Math.pow(10.0, dbz / 10.0) / 200.0, 0.625)
        }

        return RadarAnalysisResult(
            weatherForecast = weatherForecast,
            coreIntensity = coreIntensity,
            threats = threats,
            timeline = timeline6h,
            predictedDbz = dbzProjection,
            rainRates = rainRates,
            radarBitmap = bitmap
        )
    }
}

