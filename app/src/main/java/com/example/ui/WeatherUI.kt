package com.example.ui
import androidx.compose.ui.graphics.graphicsLayer

import com.example.R
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kotlin.math.roundToInt
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.AlaroForecast
import com.example.data.Observation
import com.example.data.UkmoForecast
import com.example.data.ImgwAlaroForecast
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.Map
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAppContent(viewModel: WeatherViewModel) {

    val context = LocalContext.current

    val observationsState by viewModel.observationsState.collectAsState()
    val alaroState by viewModel.alaroState.collectAsState()
    val alaroProgress by viewModel.alaroProgress.collectAsState()
    val alaro3DayState by viewModel.alaro3DayState.collectAsState()
    val alaro3DayProgress by viewModel.alaro3DayProgress.collectAsState()
    val ukmoState by viewModel.ukmoState.collectAsState()
    val imgwAlaroState by viewModel.imgwAlaroState.collectAsState()
    val radarAnalysisState by viewModel.radarAnalysisState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    // Request Notification Permission on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Bildirim izni verilmedi. Güncelleme bildirimleri gösterilemeyebilir.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        viewModel.loadAllData(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Meteoroloji Takip",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Kocaeli Meteoroloji Bilgi Sistemi",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadAllData(context) },
                        enabled = !isRefreshing,
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Verileri Yenile",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Cloud else Icons.Outlined.Cloud,
                            contentDescription = "Son Durumlar"
                        )
                    },
                    label = { Text("Gözlem", fontSize = 9.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_son_durumlar")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.ShowChart else Icons.Outlined.ShowChart,
                            contentDescription = "MGM ALARO"
                        )
                    },
                    label = { Text("ALARO", fontSize = 9.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_mgm_alaro")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.Speed else Icons.Outlined.Speed,
                            contentDescription = "UKMO"
                        )
                    },
                    label = { Text("UKMO", fontSize = 9.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_ukmo_seamless")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.DeviceThermostat else Icons.Outlined.DeviceThermostat,
                            contentDescription = "Polonya ALARO"
                        )
                    },
                    label = { Text("PL-ALARO", fontSize = 9.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_poland_alaro")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 4) Icons.Filled.Map else Icons.Outlined.Map,
                            contentDescription = "AI Radar"
                        )
                    },
                    label = { Text("Radar", fontSize = 9.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_ai_radar")
                )
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 5) Icons.Filled.Info else Icons.Outlined.Info,
                            contentDescription = "Hakkında"
                        )
                    },
                    label = { Text("Hakkında", fontSize = 9.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_hakkinda")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {


            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> SonDurumlarScreen(state = observationsState)
                    1 -> MgmAlaroMergedScreen(
                        state15 = alaroState,
                        progress15 = alaroProgress,
                        state3Day = alaro3DayState,
                        progress3Day = alaro3DayProgress
                    )
                    2 -> UkmoScreen(state = ukmoState)
                    3 -> PolonyaAlaroScreen(state = imgwAlaroState)
                    4 -> AiRadarScreen(stateFlow = viewModel.radarAnalysisState, onRefresh = { viewModel.loadAllData(context) })
                    5 -> HakkindaScreen(viewModel = viewModel) { viewModel.loadAllData(context) }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// SCREEN 1: SON DURUMLAR (CURRENT OBSERVATIONS)
// -------------------------------------------------------------------------------------
@Composable
fun SonDurumlarScreen(state: UIState<List<Observation>>) {
    when (state) {
        is UIState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Kocaeli istasyon verileri taranıyor...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
        }
        is UIState.Success -> {
            val observations = state.data
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                item {
                    Text(
                        text = "Kocaeli Bölgesel Güncel Gözlemler",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(observations) { observation ->
                    ObservationCard(observation)
                }
            }
        }
        is UIState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("⚠️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        }
        else -> {}
    }
}

data class DisplayGridItem(
    val emoji: String,
    val label: String,
    val value: String,
    val valueColor: Color,
    val iconRes: Int? = null
)

@Composable
fun ObservationCard(obs: Observation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            Column(modifier = Modifier.padding(16.dp)) {
                // Station title bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(18.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = obs.stationName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = obs.hadiseEmoji,
                        fontSize = 28.sp,

                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Observation parameters grid (2-columns)
                val items = mutableListOf<DisplayGridItem>()

                if (obs.sicaklik != null) {
                    items.add(DisplayGridItem("🌡️", "Sıcaklık", "${obs.sicaklik}°C", Color(0xFFFF453A)))
                }
                if (obs.hissedilenSicaklik != null) {
                    items.add(DisplayGridItem("🌡️", "Hissedilen", "${obs.hissedilenSicaklik}°C", Color(0xFFFF9F0A)))
                }
                if (obs.hadiseMetin.isNotEmpty() && obs.hadiseMetin != "Bilinmiyor" && obs.hadiseKodu != null) {
                    items.add(DisplayGridItem(obs.hadiseEmoji, "Güncel Hadise", obs.hadiseMetin, MaterialTheme.colorScheme.onSurface, null))
                }
                if (obs.nem != null) {
                    items.add(DisplayGridItem("💧", "Nem Oranı", "%${obs.nem.toInt()}", Color(0xFF34C759)))
                }
                if (obs.ruzgarHiz != null) {
                    items.add(DisplayGridItem("🌬️", "Rüzgar Hızı", "${obs.ruzgarHiz.roundToInt()} km/sa", Color(0xFF007AFF)))
                }
                if (obs.ruzgarYon != null && obs.ruzgarYon != "Bilinmiyor") {
                    val arrowOnly = when {
                        obs.ruzgarYon.startsWith("➡️") -> "➡️"
                        obs.ruzgarYon.isNotEmpty() -> obs.ruzgarYon.first().toString()
                        else -> ""
                    }
                    items.add(DisplayGridItem("🧭", "Rüzgar Yönü", arrowOnly, MaterialTheme.colorScheme.onSurface))
                }
                if (obs.aktuelBasinc != null) {
                    items.add(DisplayGridItem("⚖️", "Güncel Basınç", "${obs.aktuelBasinc.toInt()} hPa", Color(0xFFBF5AF2)))
                }
                if (obs.yagis00Now != null) {
                    items.add(DisplayGridItem("🌧️", "Günlük Toplam Yağış", "${obs.yagis00Now} mm", Color(0xFF0A84FF)))
                }
                if (obs.karKalinlik != null && obs.karKalinlik > 0) {
                    items.add(DisplayGridItem("❄️", "Kar Kalınlığı", "${obs.karKalinlik} cm", MaterialTheme.colorScheme.onSurface))
                } else if (obs.denizSicaklik != null) {
                    items.add(DisplayGridItem("🌊", "Deniz Suyu Sıcaklığı", "${obs.denizSicaklik} °C", Color(0xFF34C759)))
                } else if (obs.gorus != null) {
                    items.add(DisplayGridItem("👁️", "Görüş Mesafesi", "${(obs.gorus / 1000).toInt()} km", MaterialTheme.colorScheme.onSurface))
                }
                if (obs.formattedVeriZamani != null) {
                    items.add(DisplayGridItem("🕒", "Son Güncelleme", obs.formattedVeriZamani, MaterialTheme.colorScheme.onSurfaceVariant))
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val chunkedItems = items.chunked(2)
                    chunkedItems.forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowItems.forEach { item ->
                                GridItem(
                                    modifier = Modifier.weight(1f),
                                    emoji = item.emoji,
                                    label = item.label,
                                    value = item.value,
                                    valueColor = item.valueColor,
                                    iconRes = item.iconRes
                                )
                            }
                            if (rowItems.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridItem(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    value: String,
    valueColor: Color,
    iconRes: Int? = null
) {
    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            if (iconRes != null) {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Text(emoji, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label.uppercase(java.util.Locale("tr", "TR")),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// SCREEN 2: MGM ALARO MODELİ (FORECAST MODEL)
// -------------------------------------------------------------------------------------
@Composable
fun MgmAlaroScreen(state: UIState<List<AlaroForecast>>, progress: Float) {
    when (state) {
        is UIState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "MGM ALARO Modeli yükleniyor...",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "İşlenen Saat Adımı: ${(progress * 72).toInt()} / 72",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
        is UIState.Success -> {
            val forecasts = state.data
            var selectedIndex by remember { mutableStateOf<Int?>(null) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = "MGM ALARO",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(12.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kocaeli, Derince 72 Saatlik Tahmin Modeli",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Interactive Details Card
                item {
                    val currentForecast = selectedIndex?.let { forecasts.getOrNull(it) } ?: forecasts.firstOrNull { it.dateTime.isAfter(java.time.LocalDateTime.now().minusHours(1)) } ?: forecasts.firstOrNull()
                    if (currentForecast != null) {
                        ForecastDetailCard(currentForecast, selectedIndex != null)
                    }
                }

                // Subplot 1: Temperature & Felt & Wind Direction
                item {
                    ChartContainer(
                        title = "Sıcaklık ve Model Hissedilen Sıcaklığı",
                        subtitle = "Grafiğe basarak saatlik detayları inceleyebilirsiniz"
                    ) {
                        WeatherAlaroLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.sicaklik, it.hissedilenSicaklik) },
                            colors = listOf(Color(0xFFFF453A), Color(0xFFFF9F0A)),
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 2: Precipitation
                item {
                    ChartContainer(
                        title = "Yağış Miktarı",
                        subtitle = "Saatlik toplam yağış miktarı (mm)"
                    ) {
                        WeatherAlaroBarChart(
                            forecasts = forecasts,
                            getValue = { it.saatlikToplamYagis },
                            barColor = Color(0xFF007AFF), onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 3: Relative Humidity
                item {
                    ChartContainer(
                        title = "Bağıl Nem",
                        subtitle = "Havadaki nem oranı yüzdesi (%)"
                    ) {
                        WeatherAlaroLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.nem) },
                            colors = listOf(Color(0xFF34C759)),
                            filled = true,
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 4: Cloudiness
                item {
                    ChartContainer(
                        title = "Bulut Kapalılığı",
                        subtitle = "Gökyüzü bulutluluk oranı yüzdesi (%)"
                    ) {
                        WeatherAlaroLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.bulutKapaliligi) },
                            colors = listOf(MaterialTheme.colorScheme.onSurfaceVariant),
                            filled = true,
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 5: Atmospheric Pressure
                item {
                    ChartContainer(
                        title = "Atmosfer Basıncı",
                        subtitle = "Aktüel hava basıncı (hPa)"
                    ) {
                        WeatherAlaroLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.basinc) },
                            colors = listOf(Color(0xFFBF5AF2)),
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 6: Snow Depth
                item {
                    val hasSnow = forecasts.any { it.karYuksekligi > 0 }
                    ChartContainer(
                        title = "Kar Yüksekliği",
                        subtitle = if (hasSnow) "Beklenen kar kalınlığı (cm)" else "Önümüzdeki 72 saatte kar yağışı beklenmiyor"
                    ) {
                        if (hasSnow) {
                            WeatherAlaroBarChart(
                                forecasts = forecasts,
                                getValue = { it.karYuksekligi },
                                barColor = MaterialTheme.colorScheme.onSurface, onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("❄️ Kar Birikimi Yok", color = Color(0x331D1B20), fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
        is UIState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("⚠️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        }
        else -> {}
    }
}

@Composable
fun MgmAlaro3DayScreen(state: UIState<List<AlaroForecast>>, progress: Float) {
    when (state) {
        is UIState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ALARO 3 Günlük Model yükleniyor...",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "İşlenen Saat Adımı: ${(progress * 72).toInt()} / 72",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
        is UIState.Success -> {
            val forecasts = state.data
            var selectedIndex by remember { mutableStateOf<Int?>(null) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = "ALARO 3 Günlük",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(12.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kocaeli, Derince 3 Günlük Tahmin Modeli (1 Sa Adım)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Interactive Details Card
                item {
                    val currentForecast = selectedIndex?.let { forecasts.getOrNull(it) } ?: forecasts.firstOrNull { it.dateTime.isAfter(java.time.LocalDateTime.now().minusHours(1)) } ?: forecasts.firstOrNull()
                    if (currentForecast != null) {
                        ForecastDetailCard(currentForecast, selectedIndex != null)
                    }
                }

                // Subplot 1: Temperature & Felt & Wind Direction
                item {
                    ChartContainer(
                        title = "Sıcaklık ve Model Hissedilen Sıcaklığı",
                        subtitle = "Grafiğe basarak saatlik detayları inceleyebilirsiniz"
                    ) {
                        WeatherAlaroLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.sicaklik, it.hissedilenSicaklik) },
                            colors = listOf(Color(0xFFFF453A), Color(0xFFFF9F0A)),
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 2: Precipitation
                item {
                    ChartContainer(
                        title = "Yağış Miktarı",
                        subtitle = "Saatlik toplam yağış miktarı (mm)"
                    ) {
                        WeatherAlaroBarChart(
                            forecasts = forecasts,
                            getValue = { it.saatlikToplamYagis },
                            barColor = Color(0xFF007AFF), onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 3: Relative Humidity
                item {
                    ChartContainer(
                        title = "Bağıl Nem",
                        subtitle = "Havadaki nem oranı yüzdesi (%)"
                    ) {
                        WeatherAlaroLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.nem) },
                            colors = listOf(Color(0xFF34C759)),
                            filled = true,
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 4: Cloudiness
                item {
                    ChartContainer(
                        title = "Bulut Kapalılığı",
                        subtitle = "Gökyüzü bulutluluk oranı yüzdesi (%)"
                    ) {
                        WeatherAlaroLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.bulutKapaliligi) },
                            colors = listOf(MaterialTheme.colorScheme.onSurfaceVariant),
                            filled = true,
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 5: Atmospheric Pressure
                item {
                    ChartContainer(
                        title = "Atmosfer Basıncı",
                        subtitle = "Aktüel hava basıncı (hPa)"
                    ) {
                        WeatherAlaroLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.basinc) },
                            colors = listOf(Color(0xFFBF5AF2)),
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 6: Snow Depth
                item {
                    val hasSnow = forecasts.any { it.karYuksekligi > 0 }
                    ChartContainer(
                        title = "Kar Yüksekliği",
                        subtitle = if (hasSnow) "Beklenen kar kalınlığı (cm)" else "Önümüzdeki 72 saatte kar yağışı beklenmiyor"
                    ) {
                        if (hasSnow) {
                            WeatherAlaroBarChart(
                                forecasts = forecasts,
                                getValue = { it.karYuksekligi },
                                barColor = MaterialTheme.colorScheme.onSurface, onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("❄️ Kar Birikimi Yok", color = Color(0x331D1B20), fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
        is UIState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("⚠️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        }
        else -> {}
    }
}

@Composable
fun ForecastDetailCard(forecast: AlaroForecast, isCustom: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isCustom) "Seçilen Saat Bilgileri" else "Mevcut Saat Dilimi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${forecast.formattedDate} - ${forecast.formattedTime}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${forecast.sicaklik}°C",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hissedilen: ${forecast.hissedilenSicaklik}°C",
                            fontSize = 13.sp,
                            color = Color(0xFFFF9F0A),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = forecast.hadiseMetin,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${forecast.ruzgarOk} Rüzgar",
                        fontSize = 13.sp,
                        color = Color(0xFF007AFF),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${forecast.ruzgarHizi} km/sa (${forecast.ruzgarYonu}°)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoBadge(emoji = "💧", label = "Nem", value = "%${forecast.nem.toInt()}", color = Color(0xFF34C759))
                InfoBadge(emoji = "🌧️", label = "Yağış", value = "${forecast.saatlikToplamYagis} mm", color = Color(0xFF007AFF))
                InfoBadge(emoji = "☁️", label = "Bulut", value = "%${forecast.bulutKapaliligi.toInt()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                InfoBadge(emoji = "⚖️", label = "Basınç", value = "${forecast.basinc.toInt()} hPa", color = Color(0xFFBF5AF2))
            }
        }
    }
}

@Composable
fun InfoBadge(emoji: String, label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(label, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ChartContainer(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(14.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp, top = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

// -------------------------------------------------------------------------------------
// NATIVE CUSTOM LINE CHART (WITH HORIZONTAL SCROLL)
// -------------------------------------------------------------------------------------
@Composable
fun WeatherAlaroLineChart(
    forecasts: List<AlaroForecast>,
    getValues: (AlaroForecast) -> List<Double>,
    colors: List<Color>,
    filled: Boolean = false,
    onPointSelected: (Int) -> Unit, selectedIndex: Int? = null

) {
    val precomputedValues = androidx.compose.runtime.remember(forecasts) { forecasts.map { getValues(it) } }



    val context = androidx.compose.ui.platform.LocalContext.current
    val customTypeface = android.graphics.Typeface.SANS_SERIF

    val scrollState = rememberScrollState()
    val density = androidx.compose.ui.platform.LocalDensity.current

    val stepWidth = 45.dp
    val chartHeight = 220.dp
    val totalWidth = stepWidth * forecasts.size

    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.parseColor("#AAAAAA")
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = customTypeface ?: android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    val arrowPaint = remember {
        Paint().apply {
            color = android.graphics.Color.parseColor("#007AFF")
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Canvas(
            modifier = Modifier
                .width(totalWidth)
                .height(chartHeight)
                .pointerInput(forecasts) {
                    detectTapGestures { offset ->
                        val colWidthPx = with(density) { stepWidth.toPx() }
                        val index = (offset.x / colWidthPx).toInt()
                        if (index in forecasts.indices) {
                            onPointSelected(index)
                        }
                    }
                }
        ) {
            val stepWidthPx = stepWidth.toPx()
            val canvasHeight = size.height
            val bottomPadding = 45.dp.toPx()
            val topPadding = 45.dp.toPx()
            val chartAreaHeight = canvasHeight - bottomPadding - topPadding

            // 1. Draw "BERAT" giant background watermark
            drawWatermark()

            // 2. Find min and max for scaling
            var globalMin = Double.MAX_VALUE
            var globalMax = Double.MIN_VALUE

            precomputedValues.forEach { vals ->
                vals.forEach { v ->
                    if (v < globalMin) globalMin = v
                    if (v > globalMax) globalMax = v
                }
            }

            // Fallback for flat charts
            if (globalMin == globalMax) {
                globalMin -= 5.0
                globalMax += 5.0
            }

            val range = globalMax - globalMin

            // 3. Draw Grid Lines
            for (i in forecasts.indices) {
                val x = i * stepWidthPx + (stepWidthPx / 2)
                drawLine(
                    color = Color(0x0C000000),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight - bottomPadding),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Horizontal Grid Lines
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = topPadding + (chartAreaHeight * i / gridLines)
                drawLine(
                    color = Color(0x0A000000),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 4. Draw Lines & Areas
            colors.forEachIndexed { lineIdx, lineColor ->
                val path = Path()
                val points = forecasts.mapIndexed { index, forecast ->
                    val x = index * stepWidthPx + (stepWidthPx / 2)
                    val yVal = precomputedValues[index].getOrNull(lineIdx) ?: 0.0
                    val y = topPadding + (chartAreaHeight - ((yVal - globalMin) / range * chartAreaHeight)).toFloat()
                    Offset(x, y)
                }

                if (points.isNotEmpty()) {
                    path.moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val cpX = (prev.x + curr.x) / 2f
                        path.cubicTo(cpX, prev.y, cpX, curr.y, curr.x, curr.y)
                    }
                    path.lineTo(points.last().x, points.last().y)

                    // Draw Line Path
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // If filled area is requested (e.g. Relative Humidity/Cloudiness)
                    if (filled) {
                        val filledPath = Path().apply {
                            addPath(path)
                            lineTo(points.last().x, canvasHeight - bottomPadding)
                            lineTo(points.first().x, canvasHeight - bottomPadding)
                            close()
                        }
                        drawPath(
                            path = filledPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(lineColor.copy(alpha = 0.12f), Color.Transparent),
                                startY = topPadding,
                                endY = canvasHeight - bottomPadding
                            )
                        )
                    }

                    // Draw circles & values
                    points.forEachIndexed { i, pt ->
                        // Draw Point Circle
                        drawCircle(
                            color = lineColor,
                            radius = 3.5.dp.toPx(),
                            center = pt
                        )

                        // Draw value label
                        val forecast = forecasts[i]
                        val yVal = precomputedValues[i].getOrNull(lineIdx) ?: 0.0
                        val valStr = if (yVal == yVal.toInt().toDouble()) "${yVal.toInt()}" else String.format("%.1f", yVal)

                        // For temperatures, show degree symbol. For percentages, show %.
                        val labelText = if (colors.size > 1) "$valStr°" else "$valStr%"

                        drawContext.canvas.nativeCanvas.drawText(
                            labelText,
                            pt.x,
                            pt.y - 12.dp.toPx(),
                            textPaint.apply {
                                color = if (lineIdx == 0) android.graphics.Color.parseColor("#E3E3E3") else android.graphics.Color.parseColor("#FF9F0A")
                            }
                        )

                        // Wind Indicators (Only in Subplot 1 / multi-line temperature chart)
                        if (colors.size > 1 && lineIdx == 0) {
                            // Draw wind direction icon (arrow)
                            drawContext.canvas.nativeCanvas.drawText(
                                forecast.ruzgarOk,
                                pt.x,
                                canvasHeight - 20.dp.toPx(),
                                arrowPaint
                            )
                        }
                    }
                }
            }

            // 5. Draw Time Axis Labels
            forecasts.forEachIndexed { index, forecast ->
                val x = index * stepWidthPx + (stepWidthPx / 2)
                if (selectedIndex == index) {
                    drawRect(
                        color = Color(0x33007AFF),
                        topLeft = androidx.compose.ui.geometry.Offset(x - (stepWidthPx / 2), 0f),
                        size = androidx.compose.ui.geometry.Size(stepWidthPx, canvasHeight)
                    )
                }
                

drawContext.canvas.nativeCanvas.drawText(
                    forecast.formattedTime,
                    x,
                    canvasHeight - 4.dp.toPx(),
                    textPaint.apply { color = android.graphics.Color.parseColor("#AAAAAA") }
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// NATIVE CUSTOM BAR CHART (WITH HORIZONTAL SCROLL)
// -------------------------------------------------------------------------------------
@Composable
fun WeatherAlaroBarChart(
    forecasts: List<AlaroForecast>,
    getValue: (AlaroForecast) -> Double,
    barColor: Color,
    onPointSelected: ((Int) -> Unit)? = null, selectedIndex: Int? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val customTypeface = android.graphics.Typeface.SANS_SERIF


    val scrollState = rememberScrollState()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val stepWidth = 45.dp
    val chartHeight = 140.dp
    val totalWidth = stepWidth * forecasts.size

    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.parseColor("#AAAAAA")
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = customTypeface ?: android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Canvas(
            modifier = Modifier
                .width(totalWidth)
                .height(chartHeight)
                .pointerInput(forecasts) {
                    detectTapGestures { offset ->
                        if (onPointSelected != null) {
                            val colWidthPx = with(density) { stepWidth.toPx() }
                            val index = (offset.x / colWidthPx).toInt()
                            if (index in forecasts.indices) {
                                onPointSelected(index)
                            }
                        }
                    }
                }
        ) {
            val stepWidthPx = stepWidth.toPx()
            val canvasHeight = size.height
            val bottomPadding = 35.dp.toPx()
            val topPadding = 25.dp.toPx()
            val chartAreaHeight = canvasHeight - bottomPadding - topPadding

            // 1. Draw Watermark
            drawWatermark()

            var maxVal = forecasts.maxOfOrNull { getValue(it) } ?: 1.0
            if (maxVal == 0.0) maxVal = 1.0

            // Grid Lines
            for (i in forecasts.indices) {
                val x = i * stepWidthPx + (stepWidthPx / 2)
                drawLine(
                    color = Color(0x0C000000),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight - bottomPadding),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw Bars
            forecasts.forEachIndexed { i, forecast ->
                val xCenter = i * stepWidthPx + (stepWidthPx / 2)
                if (selectedIndex == i) {
                    drawRect(
                        color = Color(0x33007AFF),
                        topLeft = androidx.compose.ui.geometry.Offset(xCenter - (stepWidthPx / 2), 0f),
                        size = androidx.compose.ui.geometry.Size(stepWidthPx, canvasHeight)
                    )
                }
                val yVal = getValue(forecast)

                if (yVal > 0) {
                    val barHeight = (yVal / maxVal * chartAreaHeight).toFloat()
                    val barWidth = 14.dp.toPx()
                    val left = xCenter - (barWidth / 2)
                    val top = canvasHeight - bottomPadding - barHeight

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
                    )

                    // Draw values above bars
                    val displayVal = if (yVal == yVal.toInt().toDouble()) "${yVal.toInt()}" else String.format("%.1f", yVal)
                    drawContext.canvas.nativeCanvas.drawText(
                        displayVal,
                        xCenter,
                        top - 6.dp.toPx(),
                        textPaint.apply { color = android.graphics.Color.parseColor("#E3E3E3") }
                    )
                }



                // Draw Time Axis Labels
                

drawContext.canvas.nativeCanvas.drawText(
                    forecast.formattedTime,
                    xCenter,
                    canvasHeight - 4.dp.toPx(),
                    textPaint.apply { color = android.graphics.Color.parseColor("#AAAAAA") }
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// CHART WATERMARK UTILITY
// -------------------------------------------------------------------------------------
private fun DrawScope.drawWatermark() {
    val watermarkPaint = Paint().apply {
        color = android.graphics.Color.argb(8, 29, 25, 43) // 0.03f opacity grey-violet
        textSize = 150f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
    }

    drawContext.canvas.nativeCanvas.save()
    drawContext.canvas.nativeCanvas.rotate(-20f, size.width / 2, size.height / 2)
    drawContext.canvas.nativeCanvas.drawText(
        "BERAT",
        size.width / 2,
        size.height / 2 + 50f,
        watermarkPaint
    )
    drawContext.canvas.nativeCanvas.restore()
}

// -------------------------------------------------------------------------------------
// SCREEN 3: HAKKINDA (ABOUT SCREEN)
// -------------------------------------------------------------------------------------
@Composable
fun HakkindaScreen(viewModel: WeatherViewModel, onManualRefresh: () -> Unit) {

    val context = LocalContext.current
    val currentTheme by viewModel.themeState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // App visual splash asset logo
            Card(
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .size(160.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(28.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_weather_cover_1783169531486),
                    contentDescription = "Hava Durumu Kapak Resmi",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Kocaeli MGM",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Meteoroloji Gözlem ve Tahmin Sistemi",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Uygulama Hakkında",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Bu uygulama, Kocaeli güncel meteoroloji gözlemleri ile MGM ALARO, UKMO, PL-ALARO tahmin modellerini ve AI Radar verilerini sunmaktadır.",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Görünüm Teması",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val themes = listOf(AppTheme.LIGHT, AppTheme.DARK, AppTheme.SYSTEM)

                        themes.forEach { theme ->
                            val isSelected = currentTheme == theme
                            val label = when (theme) {
                                AppTheme.LIGHT -> "Açık"
                                AppTheme.DARK -> "Koyu"
                                AppTheme.SYSTEM -> "Sistem"
                            }
                            val icon = when (theme) {
                                AppTheme.LIGHT -> Icons.Default.WbSunny
                                AppTheme.DARK -> Icons.Default.NightsStay
                                AppTheme.SYSTEM -> Icons.Default.Settings
                            }
                            val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .background(containerColor, RoundedCornerShape(22.dp))
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(22.dp)
                                    )
                                    .clickable {
                                        viewModel.setTheme(context, theme)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = contentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = contentColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Geliştirici",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Berat TORUN tarafından yapılmıştır",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onManualRefresh,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("manual_refresh_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("VERİLERİ GÜNCELLE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// MGM ALARO MERGED SCREEN (WITH SUB-TABS)
// -------------------------------------------------------------------------------------
@Composable
fun MgmAlaroMergedScreen(
    state15: UIState<List<AlaroForecast>>,
    progress15: Float,
    state3Day: UIState<List<AlaroForecast>>,
    progress3Day: Float
) {
    var subTab by remember { mutableStateOf(0) }
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = subTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { subTab = 0 },
                text = { Text("ALARO (15 Dk)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = subTab == 1,
                onClick = { subTab = 1 },
                text = { Text("ALARO (3 Gün)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            if (subTab == 0) {
                MgmAlaroScreen(state = state15, progress = progress15)
            } else {
                MgmAlaro3DayScreen(state = state3Day, progress = progress3Day)
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// SCREEN 4: UKMO 10 km MODEL SCREEN
// -------------------------------------------------------------------------------------
@Composable
fun UkmoScreen(state: UIState<List<UkmoForecast>>) {
    when (state) {
        is UIState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("UKMO 10 km Model tahminleri yükleniyor...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
        }
        is UIState.Success -> {
            val forecasts = state.data
            var selectedIndex by remember { mutableStateOf<Int?>(null) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = "UKMO 10 km Model",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(12.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Derince için UK Met Office Kesintisiz Tahmin Modeli",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Interactive Details Card
                item {
                    val currentForecast = selectedIndex?.let { forecasts.getOrNull(it) } ?: forecasts.firstOrNull { it.dateTime.isAfter(java.time.LocalDateTime.now().minusHours(1)) } ?: forecasts.firstOrNull()
                    if (currentForecast != null) {
                        UkmoDetailCard(currentForecast, selectedIndex != null)
                    }
                }

                // Subplot 1: Temperature & Felt & Wind Direction
                item {
                    ChartContainer(
                        title = "Sıcaklık ve Hissedilen Sıcaklık",
                        subtitle = "Grafiğe basarak saatlik detayları inceleyebilirsiniz"
                    ) {
                        UkmoLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.sicaklik, it.hissedilenSicaklik) },
                            colors = listOf(Color(0xFFFF453A), Color(0xFFFF9F0A)),
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 2: Precipitation
                item {
                    ChartContainer(
                        title = "Yağış Miktarı",
                        subtitle = "Saatlik toplam yağış miktarı (mm)"
                    ) {
                        UkmoBarChart(
                            forecasts = forecasts,
                            getValue = { it.yagis },
                            barColor = Color(0xFF007AFF), onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 3: Relative Humidity and Fog
                item {
                    ChartContainer(
                        title = "Bağıl Nem ve Sis",
                        subtitle = "Havadaki nem oranı (%) ve Sis oranı (%)"
                    ) {
                        UkmoLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.nem, it.sis2m) },
                            colors = listOf(Color(0xFF34C759), Color(0xFF8E8E93)),
                            filled = true,
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 4: Cloudiness Layers
                item {
                    ChartContainer(
                        title = "Bulut Katmanları",
                        subtitle = "Toplam (% siyah), Alçak (% mavi), Orta (% turuncu), Yüksek (% yeşil) bulutluluk"
                    ) {
                        UkmoLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.toplamBulut, it.alcakBulut, it.ortaBulut, it.yuksekBulut) },
                            colors = listOf(MaterialTheme.colorScheme.onSurface, Color(0xFF007AFF), Color(0xFFFF9F0A), Color(0xFF34C759)),
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 5: CAPE / CIN (Atmospheric Instability)
                item {
                    ChartContainer(
                        title = "CAPE ve CIN Değerleri",
                        subtitle = "Konvektif Kararsızlık (CAPE, turuncu) ve Konvektif Engelleme (CIN, kırmızı)"
                    ) {
                        UkmoLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.cape, it.cin) },
                            colors = listOf(Color(0xFFFF9F0A), Color(0xFFFF3B30)),
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 6: Solar Radiation
                item {
                    ChartContainer(
                        title = "Güneş Radyasyonu ve Işınım",
                        subtitle = "GTI (Eğik, kırmızı), DNI (Doğrusal, pembe), GHI (Kısa Dalga, sarı)"
                    ) {
                        UkmoLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.gti, it.dni, it.ghi) },
                            colors = listOf(Color(0xFFFF5E3A), Color(0xFFFF2D55), Color(0xFFFF9500)),
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }
            }
        }
        is UIState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("⚠️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        }
        else -> {}
    }
}

@Composable
fun UkmoDetailCard(forecast: UkmoForecast, isCustom: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isCustom) "Seçilen Saat Bilgileri (UKMO)" else "Mevcut Saat Dilimi (UKMO)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${forecast.formattedDate} - ${forecast.formattedTime}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${forecast.sicaklik}°C",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hissedilen: ${forecast.hissedilenSicaklik}°C",
                            fontSize = 13.sp,
                            color = Color(0xFFFF9F0A),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = forecast.durum,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${forecast.ruzgarOk} Rüzgar",
                        fontSize = 13.sp,
                        color = Color(0xFF007AFF),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${forecast.ruzgarHizi} km/sa | Gust: ${forecast.ruzgarHamlesi} km/sa",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Text("Bulut ve Nem Parametreleri", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoBadge(emoji = "☁️", label = "Top. Bulut", value = "%${forecast.toplamBulut.toInt()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                InfoBadge(emoji = "🌫️", label = "Alçak Blt", value = "%${forecast.alcakBulut.toInt()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                InfoBadge(emoji = "⛅", label = "Orta Blt", value = "%${forecast.ortaBulut.toInt()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                InfoBadge(emoji = "🌤️", label = "Yüksek Blt", value = "%${forecast.yuksekBulut.toInt()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoBadge(emoji = "💧", label = "Bağıl Nem", value = "%${forecast.nem.toInt()}", color = Color(0xFF34C759))
                InfoBadge(emoji = "🌁", label = "Sis 2m", value = "%${forecast.sis2m.toInt()}", color = Color(0xFF8E8E93))
                InfoBadge(emoji = "🌡️", label = "Çiy Noktası", value = "${forecast.ciyNoktasi}°C", color = Color(0xFF00C7BE))
                InfoBadge(emoji = "🧪", label = "Islak Hzn", value = "${forecast.islakHazneSicakligi}°C", color = Color(0xFFAF52DE))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Text("Konvektif Stabilite ve Dinamikler", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoBadge(emoji = "⚡", label = "CAPE", value = "${forecast.cape.toInt()} J/kg", color = Color(0xFFFFCC00))
                InfoBadge(emoji = "🛡️", label = "CIN", value = "${forecast.cin.toInt()} J/kg", color = Color(0xFFFF3B30))
                InfoBadge(emoji = "👓", label = "Görüş", value = "${String.format("%.1f", forecast.gorusUzakligi)} km", color = Color(0xFF5856D6))
                InfoBadge(emoji = "🌧️", label = "Yağış", value = "${forecast.yagis} mm", color = Color(0xFF007AFF))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Text("Güneş Radyasyonu ve Işınım (GTI & DNI)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoBadge(emoji = "☀️", label = "GHI", value = "${forecast.ghi.toInt()} W/m²", color = Color(0xFFFF9500))
                InfoBadge(emoji = "🎯", label = "DNI", value = "${forecast.dni.toInt()} W/m²", color = Color(0xFFFF2D55))
                InfoBadge(emoji = "📐", label = "GTI (Eğik)", value = "${forecast.gti.toInt()} W/m²", color = Color(0xFFFF5E3A))
                InfoBadge(emoji = "🌍", label = "Yer Rady.", value = "${forecast.terrestrialRadiation.toInt()} W/m²", color = Color(0xFF8E8E93))
            }
        }
    }
}

@Composable
fun UkmoLineChart(
    forecasts: List<UkmoForecast>,
    getValues: (UkmoForecast) -> List<Double>,
    colors: List<Color>,
    filled: Boolean = false,
    onPointSelected: (Int) -> Unit, selectedIndex: Int? = null

) {
    val precomputedValues = androidx.compose.runtime.remember(forecasts) { forecasts.map { getValues(it) } }



    val context = androidx.compose.ui.platform.LocalContext.current
    val customTypeface = android.graphics.Typeface.SANS_SERIF

    val scrollState = rememberScrollState()
    val density = androidx.compose.ui.platform.LocalDensity.current

    val stepWidth = 45.dp
    val chartHeight = 220.dp
    val totalWidth = stepWidth * forecasts.size

    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.parseColor("#AAAAAA")
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = customTypeface ?: android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    val arrowPaint = remember {
        Paint().apply {
            color = android.graphics.Color.parseColor("#007AFF")
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Canvas(
            modifier = Modifier
                .width(totalWidth)
                .height(chartHeight)
                .pointerInput(forecasts) {
                    detectTapGestures { offset ->
                        val colWidthPx = with(density) { stepWidth.toPx() }
                        val index = (offset.x / colWidthPx).toInt()
                        if (index in forecasts.indices) {
                            onPointSelected(index)
                        }
                    }
                }
        ) {
            val stepWidthPx = stepWidth.toPx()
            val canvasHeight = size.height
            val bottomPadding = 45.dp.toPx()
            val topPadding = 45.dp.toPx()
            val chartAreaHeight = canvasHeight - bottomPadding - topPadding

            drawWatermark()

            var globalMin = Double.MAX_VALUE
            var globalMax = Double.MIN_VALUE

            precomputedValues.forEach { vals ->
                vals.forEach { v ->
                    if (v < globalMin) globalMin = v
                    if (v > globalMax) globalMax = v
                }
            }

            if (globalMin == globalMax) {
                globalMin -= 5.0
                globalMax += 5.0
            }

            val range = globalMax - globalMin

            for (i in forecasts.indices) {
                val x = i * stepWidthPx + (stepWidthPx / 2)
                drawLine(
                    color = Color(0x0C000000),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight - bottomPadding),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val gridLines = 4
            for (i in 0..gridLines) {
                val y = topPadding + (chartAreaHeight * i / gridLines)
                drawLine(
                    color = Color(0x0A000000),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            colors.forEachIndexed { lineIdx, lineColor ->
                val path = Path()
                val points = forecasts.mapIndexed { index, forecast ->
                    val x = index * stepWidthPx + (stepWidthPx / 2)
                    val yVal = precomputedValues[index].getOrNull(lineIdx) ?: 0.0
                    val y = topPadding + (chartAreaHeight - ((yVal - globalMin) / range * chartAreaHeight)).toFloat()
                    Offset(x, y)
                }

                if (points.isNotEmpty()) {
                    path.moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val cpX = (prev.x + curr.x) / 2f
                        path.cubicTo(cpX, prev.y, cpX, curr.y, curr.x, curr.y)
                    }
                    path.lineTo(points.last().x, points.last().y)

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    if (filled) {
                        val filledPath = Path().apply {
                            addPath(path)
                            lineTo(points.last().x, canvasHeight - bottomPadding)
                            lineTo(points.first().x, canvasHeight - bottomPadding)
                            close()
                        }
                        drawPath(
                            path = filledPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(lineColor.copy(alpha = 0.12f), Color.Transparent),
                                startY = topPadding,
                                endY = canvasHeight - bottomPadding
                            )
                        )
                    }

                    points.forEachIndexed { i, pt ->
                        drawCircle(
                            color = lineColor,
                            radius = 3.5.dp.toPx(),
                            center = pt
                        )

                        val forecast = forecasts[i]
                        val yVal = precomputedValues[i].getOrNull(lineIdx) ?: 0.0
                        val valStr = if (yVal == yVal.toInt().toDouble()) "${yVal.toInt()}" else String.format("%.1f", yVal)
                        val labelText = if (colors.size > 1) "$valStr°" else "$valStr"

                        drawContext.canvas.nativeCanvas.drawText(
                            labelText,
                            pt.x,
                            pt.y - 12.dp.toPx(),
                            textPaint.apply {
                                color = if (lineIdx == 0) android.graphics.Color.parseColor("#E3E3E3") else android.graphics.Color.parseColor("#FF9F0A")
                            }
                        )

                        if (colors.size > 1 && lineIdx == 0) {
                            drawContext.canvas.nativeCanvas.drawText(
                                forecast.ruzgarOk,
                                pt.x,
                                canvasHeight - 20.dp.toPx(),
                                arrowPaint
                            )
                        }
                    }
                }
            }

            forecasts.forEachIndexed { index, forecast ->
                val x = index * stepWidthPx + (stepWidthPx / 2)
                if (selectedIndex == index) {
                    drawRect(
                        color = Color(0x33007AFF),
                        topLeft = androidx.compose.ui.geometry.Offset(x - (stepWidthPx / 2), 0f),
                        size = androidx.compose.ui.geometry.Size(stepWidthPx, canvasHeight)
                    )
                }
                

drawContext.canvas.nativeCanvas.drawText(
                    forecast.formattedTime,
                    x,
                    canvasHeight - 4.dp.toPx(),
                    textPaint.apply { color = android.graphics.Color.parseColor("#AAAAAA") }
                )
            }
        }
    }
}

@Composable
fun UkmoBarChart(
    forecasts: List<UkmoForecast>,
    getValue: (UkmoForecast) -> Double,
    barColor: Color,
    onPointSelected: ((Int) -> Unit)? = null, selectedIndex: Int? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val customTypeface = android.graphics.Typeface.SANS_SERIF


    val scrollState = rememberScrollState()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val stepWidth = 45.dp
    val chartHeight = 140.dp
    val totalWidth = stepWidth * forecasts.size

    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.parseColor("#AAAAAA")
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = customTypeface ?: android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Canvas(
            modifier = Modifier
                .width(totalWidth)
                .height(chartHeight)
                .pointerInput(forecasts) {
                    detectTapGestures { offset ->
                        if (onPointSelected != null) {
                            val colWidthPx = with(density) { stepWidth.toPx() }
                            val index = (offset.x / colWidthPx).toInt()
                            if (index in forecasts.indices) {
                                onPointSelected(index)
                            }
                        }
                    }
                }
        ) {
            val stepWidthPx = stepWidth.toPx()
            val canvasHeight = size.height
            val bottomPadding = 35.dp.toPx()
            val topPadding = 25.dp.toPx()
            val chartAreaHeight = canvasHeight - bottomPadding - topPadding

            drawWatermark()

            var maxVal = forecasts.maxOfOrNull { getValue(it) } ?: 1.0
            if (maxVal == 0.0) maxVal = 1.0

            for (i in forecasts.indices) {
                val x = i * stepWidthPx + (stepWidthPx / 2)
                drawLine(
                    color = Color(0x0C000000),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight - bottomPadding),
                    strokeWidth = 1.dp.toPx()
                )
            }

            forecasts.forEachIndexed { i, forecast ->
                val xCenter = i * stepWidthPx + (stepWidthPx / 2)
                if (selectedIndex == i) {
                    drawRect(
                        color = Color(0x33007AFF),
                        topLeft = androidx.compose.ui.geometry.Offset(xCenter - (stepWidthPx / 2), 0f),
                        size = androidx.compose.ui.geometry.Size(stepWidthPx, canvasHeight)
                    )
                }
                val yVal = getValue(forecast)

                if (yVal > 0) {
                    val barHeight = (yVal / maxVal * chartAreaHeight).toFloat()
                    val barWidth = 14.dp.toPx()
                    val left = xCenter - (barWidth / 2)
                    val top = canvasHeight - bottomPadding - barHeight

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
                    )

                    val displayVal = if (yVal == yVal.toInt().toDouble()) "${yVal.toInt()}" else String.format("%.1f", yVal)
                    drawContext.canvas.nativeCanvas.drawText(
                        displayVal,
                        xCenter,
                        top - 6.dp.toPx(),
                        textPaint.apply { color = android.graphics.Color.parseColor("#E3E3E3") }
                    )
                }

                

drawContext.canvas.nativeCanvas.drawText(
                    forecast.formattedTime,
                    xCenter,
                    canvasHeight - 4.dp.toPx(),
                    textPaint.apply { color = android.graphics.Color.parseColor("#AAAAAA") }
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// SCREEN 5: POLONYA ALARO MODEL SCREEN
// -------------------------------------------------------------------------------------
@Composable
fun PolonyaAlaroScreen(state: UIState<List<ImgwAlaroForecast>>) {

    when (state) {
        is UIState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Polonya ALARO Model tahminleri yükleniyor...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            }
        }
        is UIState.Success -> {
            val forecasts = state.data
            var selectedIndex by remember { mutableStateOf<Int?>(null) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = "Polonya ALARO Modeli",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(12.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Derince için IMGW ALARO Sayısal Tahmin Çıktıları",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Interactive Details Card
                item {
                    val currentForecast = selectedIndex?.let { forecasts.getOrNull(it) } ?: forecasts.firstOrNull { it.dateTime.isAfter(java.time.LocalDateTime.now().minusHours(1)) } ?: forecasts.firstOrNull()
                    if (currentForecast != null) {
                        PolonyaDetailCard(currentForecast, selectedIndex != null)
                    }
                }

                // Subplot 1: Temperature & Felt & Wind Direction
                item {
                    ChartContainer(
                        title = "Sıcaklık ve Hissedilen Sıcaklık",
                        subtitle = "Grafiğe basarak saatlik detayları inceleyebilirsiniz"
                    ) {
                        PolonyaLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.sicaklik, it.hissedilenSicaklik) },
                            colors = listOf(Color(0xFFFF453A), Color(0xFFFF9F0A)),
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 2: Precipitation
                item {
                    ChartContainer(
                        title = "Yağış Miktarı",
                        subtitle = "Saatlik toplam yağış miktarı (mm)"
                    ) {
                        PolonyaBarChart(
                            forecasts = forecasts,
                            getValue = { it.yagis },
                            barColor = Color(0xFF007AFF), onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 3: Relative Humidity
                item {
                    ChartContainer(
                        title = "Bağıl Nem",
                        subtitle = "Havadaki nem oranı yüzdesi (%)"
                    ) {
                        PolonyaLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.nem) },
                            colors = listOf(Color(0xFF34C759)),
                            filled = true,
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 4: Cloudiness
                item {
                    ChartContainer(
                        title = "Bulut Kapalılığı",
                        subtitle = "Gökyüzü bulutluluk oranı yüzdesi (%)"
                    ) {
                        PolonyaLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.bulut) },
                            colors = listOf(MaterialTheme.colorScheme.onSurfaceVariant),
                            filled = true,
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 5: Atmospheric Pressure
                item {
                    ChartContainer(
                        title = "Atmosfer Basıncı",
                        subtitle = "Aktüel hava basıncı (hPa)"
                    ) {
                        PolonyaLineChart(
                            forecasts = forecasts,
                            getValues = { listOf(it.basinc) },
                            colors = listOf(Color(0xFFBF5AF2)),
                            onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                        )
                    }
                }

                // Subplot 6: Snow Depth
                item {
                    val hasSnow = forecasts.any { it.kar > 0 }
                    ChartContainer(
                        title = "Kar Yüksekliği",
                        subtitle = if (hasSnow) "Beklenen kar kalınlığı (cm)" else "Önümüzdeki tahmin periyodunda kar beklenmiyor"
                    ) {
                        if (hasSnow) {
                            PolonyaBarChart(
                                forecasts = forecasts,
                                getValue = { it.kar },
                                barColor = MaterialTheme.colorScheme.onSurface, onPointSelected = { index -> selectedIndex = index }, selectedIndex = selectedIndex
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("❄️ Kar Birikimi Yok", color = Color(0x331D1B20), fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
        is UIState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("⚠️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            }
        }
        else -> {}
    }
}

@Composable
fun PolonyaDetailCard(forecast: ImgwAlaroForecast, isCustom: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isCustom) "Seçilen Saat Bilgileri (IMGW)" else "Mevcut Saat Dilimi (IMGW)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${forecast.formattedDate} - ${forecast.formattedTime}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${forecast.sicaklik}°C",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hissedilen: ${forecast.hissedilenSicaklik}°C",
                            fontSize = 13.sp,
                            color = Color(0xFFFF9F0A),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Text(
                        text = forecast.hadiseMetin,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${forecast.ruzgarOk} Rüzgar",
                        fontSize = 13.sp,
                        color = Color(0xFF007AFF),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${forecast.ruzgarHizi} km/sa | Gust: ${forecast.ruzgarHamlesi} km/sa",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoBadge(emoji = "💧", label = "Nem", value = "%${forecast.nem.toInt()}", color = Color(0xFF34C759))
                InfoBadge(emoji = "🌧️", label = "Yağış", value = "${forecast.yagis} mm", color = Color(0xFF007AFF))
                InfoBadge(emoji = "☁️", label = "Bulut", value = "%${forecast.bulut.toInt()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                InfoBadge(emoji = "⚖️", label = "Basınç", value = "${forecast.basinc.toInt()} hPa", color = Color(0xFFBF5AF2))
            }
        }
    }
}

@Composable
fun PolonyaLineChart(
    forecasts: List<ImgwAlaroForecast>,
    getValues: (ImgwAlaroForecast) -> List<Double>,
    colors: List<Color>,
    filled: Boolean = false,
    onPointSelected: (Int) -> Unit, selectedIndex: Int? = null

) {
    val precomputedValues = androidx.compose.runtime.remember(forecasts) { forecasts.map { getValues(it) } }



    val context = androidx.compose.ui.platform.LocalContext.current
    val customTypeface = android.graphics.Typeface.SANS_SERIF

    val scrollState = rememberScrollState()
    val density = androidx.compose.ui.platform.LocalDensity.current

    val stepWidth = 45.dp
    val chartHeight = 220.dp
    val totalWidth = stepWidth * forecasts.size

    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.parseColor("#AAAAAA")
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = customTypeface ?: android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    val arrowPaint = remember {
        Paint().apply {
            color = android.graphics.Color.parseColor("#007AFF")
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Canvas(
            modifier = Modifier
                .width(totalWidth)
                .height(chartHeight)
                .pointerInput(forecasts) {
                    detectTapGestures { offset ->
                        val colWidthPx = with(density) { stepWidth.toPx() }
                        val index = (offset.x / colWidthPx).toInt()
                        if (index in forecasts.indices) {
                            onPointSelected(index)
                        }
                    }
                }
        ) {
            val stepWidthPx = stepWidth.toPx()
            val canvasHeight = size.height
            val bottomPadding = 45.dp.toPx()
            val topPadding = 45.dp.toPx()
            val chartAreaHeight = canvasHeight - bottomPadding - topPadding

            drawWatermark()

            var globalMin = Double.MAX_VALUE
            var globalMax = Double.MIN_VALUE

            precomputedValues.forEach { vals ->
                vals.forEach { v ->
                    if (v < globalMin) globalMin = v
                    if (v > globalMax) globalMax = v
                }
            }

            if (globalMin == globalMax) {
                globalMin -= 5.0
                globalMax += 5.0
            }

            val range = globalMax - globalMin

            for (i in forecasts.indices) {
                val x = i * stepWidthPx + (stepWidthPx / 2)
                drawLine(
                    color = Color(0x0C000000),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight - bottomPadding),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val gridLines = 4
            for (i in 0..gridLines) {
                val y = topPadding + (chartAreaHeight * i / gridLines)
                drawLine(
                    color = Color(0x0A000000),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            colors.forEachIndexed { lineIdx, lineColor ->
                val path = Path()
                val points = forecasts.mapIndexed { index, forecast ->
                    val x = index * stepWidthPx + (stepWidthPx / 2)
                    val yVal = precomputedValues[index].getOrNull(lineIdx) ?: 0.0
                    val y = topPadding + (chartAreaHeight - ((yVal - globalMin) / range * chartAreaHeight)).toFloat()
                    Offset(x, y)
                }

                if (points.isNotEmpty()) {
                    path.moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val cpX = (prev.x + curr.x) / 2f
                        path.cubicTo(cpX, prev.y, cpX, curr.y, curr.x, curr.y)
                    }
                    path.lineTo(points.last().x, points.last().y)

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    if (filled) {
                        val filledPath = Path().apply {
                            addPath(path)
                            lineTo(points.last().x, canvasHeight - bottomPadding)
                            lineTo(points.first().x, canvasHeight - bottomPadding)
                            close()
                        }
                        drawPath(
                            path = filledPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(lineColor.copy(alpha = 0.12f), Color.Transparent),
                                startY = topPadding,
                                endY = canvasHeight - bottomPadding
                            )
                        )
                    }

                    points.forEachIndexed { i, pt ->
                        drawCircle(
                            color = lineColor,
                            radius = 3.5.dp.toPx(),
                            center = pt
                        )

                        val forecast = forecasts[i]
                        val yVal = precomputedValues[i].getOrNull(lineIdx) ?: 0.0
                        val valStr = if (yVal == yVal.toInt().toDouble()) "${yVal.toInt()}" else String.format("%.1f", yVal)
                        val labelText = if (colors.size > 1) "$valStr°" else "$valStr"

                        drawContext.canvas.nativeCanvas.drawText(
                            labelText,
                            pt.x,
                            pt.y - 12.dp.toPx(),
                            textPaint.apply {
                                color = if (lineIdx == 0) android.graphics.Color.parseColor("#E3E3E3") else android.graphics.Color.parseColor("#FF9F0A")
                            }
                        )

                        if (colors.size > 1 && lineIdx == 0) {
                            drawContext.canvas.nativeCanvas.drawText(
                                forecast.ruzgarOk,
                                pt.x,
                                canvasHeight - 20.dp.toPx(),
                                arrowPaint
                            )
                        }
                    }
                }
            }

            forecasts.forEachIndexed { index, forecast ->
                val x = index * stepWidthPx + (stepWidthPx / 2)
                if (selectedIndex == index) {
                    drawRect(
                        color = Color(0x33007AFF),
                        topLeft = androidx.compose.ui.geometry.Offset(x - (stepWidthPx / 2), 0f),
                        size = androidx.compose.ui.geometry.Size(stepWidthPx, canvasHeight)
                    )
                }
                

drawContext.canvas.nativeCanvas.drawText(
                    forecast.formattedTime,
                    x,
                    canvasHeight - 4.dp.toPx(),
                    textPaint.apply { color = android.graphics.Color.parseColor("#AAAAAA") }
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// AI RADAR ANALYSIS SCREEN
// -------------------------------------------------------------------------------------
@Composable
fun AiRadarScreen(
    stateFlow: kotlinx.coroutines.flow.StateFlow<UIState<com.example.data.RadarAnalysisResult>>,
    onRefresh: () -> Unit
) {

    val state by stateFlow.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val s = state) {
            is UIState.Idle, is UIState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Yapay Zeka Radar Analizi Yapılıyor",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "MGM Bursa PPI Radar verisi canlı olarak indiriliyor, görüntü işleme ve adveksiyon modelleri koşturuluyor...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
            is UIState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Hata",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Analiz Yüklenemedi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = s.message,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onRefresh,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Yeniden Dene")
                    }
                }
            }
            is UIState.Success -> {
                val data = s.data
                val threats = data.threats
                val timeline = data.timeline

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Yapay Zeka Radar Takip ve Nowcast",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Mevcut Durum: ${data.weatherForecast}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Threat Alerts Banner
                    if (threats.isNotEmpty()) {
                        val activeThreat = threats.firstOrNull { it.prob > 40 }
                        if (activeThreat != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (activeThreat.prob > 70) MaterialTheme.colorScheme.errorContainer
                                    else Color(0xFFFFF3CD)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    val alertColor = if (activeThreat.prob > 70) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF856404)
                                    val alertBg = if (activeThreat.prob > 70) MaterialTheme.colorScheme.error else Color(0xFFFFC107)

                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(alertBg, RoundedCornerShape(24.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("⚡", fontSize = 24.sp, color = Color.White)
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "YAKLAŞAN HÜCRE ALARMI!",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = alertColor
                                        )
                                        Text(
                                            text = "Derince bölgesine %${activeThreat.prob} fırtına hücresi olasılığı tespit edildi. ETA: ${activeThreat.eta} dakika.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = alertColor.copy(alpha = 0.9f),
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Radar Map View
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Bursa PPI Canlı Radar Görüntüsü ve AI Katmanları",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

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

                            val targetX = ((cx + dxKm * pxPerKm) - 3.0).toFloat()
                            val targetY = ((cy - dyKm * pxPerKm) - 5.0).toFloat()

                            RadarMapView(
                                bitmap = data.radarBitmap,
                                threats = threats,
                                targetX = targetX,
                                targetY = targetY,
                                pxPerKm = pxPerKm.toFloat(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF3333)))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Derince (Hedef)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).background(Color.Magenta))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Kinematik Rüzgar Vektörü", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF9900)))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("50 km Koridor Sınırı", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF007AFF)))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Bursa Radar İstasyonu", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    // Nowcast Chart
                    NowcastProjectionChart(
                        dbzList = data.predictedDbz,
                        rainRates = data.rainRates
                    )

                    // Active Threats List
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Aktif Fırtına Hücre Tehdit Analizleri",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (threats.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Aktif tehdit unsuru fırtına hücresi tespit edilmedi.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                threats.forEach { t ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = t.nature,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (t.prob > 70) MaterialTheme.colorScheme.errorContainer
                                                        else MaterialTheme.colorScheme.primaryContainer,
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "Risk: %${t.prob}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (t.prob > 70) MaterialTheme.colorScheme.onErrorContainer
                                                    else MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Varış Süresi (ETA)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    text = if (t.eta <= 2) "ŞİMDİ / GİRİŞTE" else "${t.eta} dakika",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (t.eta <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Column {
                                                Text("Yağış Süresi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    text = "${t.duration} dakika",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Column {
                                                Text("Hücre Durumu", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    text = t.status,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Timeline Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Saatlik Hücre Yoğunluk Tahmini",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            timeline.forEach { step ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = step.time,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    val badgeColor = when (step.level) {
                                        0 -> Color(0xFF4CAF50)
                                        1 -> Color(0xFF8BC34A)
                                        2 -> Color(0xFF00BCD4)
                                        3 -> Color(0xFFFF9800)
                                        4 -> Color(0xFFFF5722)
                                        5 -> Color(0xFFE91E63)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .border(1.dp, badgeColor, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = step.desc,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = badgeColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = onRefresh,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("RADAR ANALİZİNİ GÜNCELLE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RadarMapView(
    bitmap: android.graphics.Bitmap?,
    threats: List<com.example.data.RadarThreat>,
    targetX: Float,
    targetY: Float,
    pxPerKm: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val scaleX = canvasW / 720f
            val scaleY = canvasH / 720f

            if (bitmap != null) {
                drawImage(
                    image = bitmap.asImageBitmap(),
                    dstSize = IntSize(canvasW.toInt(), canvasH.toInt())
                )
            } else {
                drawRect(
                    color = Color.DarkGray.copy(alpha = 0.4f)
                )
            }

            val radarCx = 360f * scaleX
            val radarCy = 360f * scaleY
            drawCircle(
                color = Color(0xFF007AFF),
                radius = 6.dp.toPx(),
                center = Offset(radarCx, radarCy)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(radarCx, radarCy)
            )

            drawCircle(
                color = Color.Cyan.copy(alpha = 0.25f),
                radius = 370f * pxPerKm * scaleX,
                center = Offset(radarCx, radarCy),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )

            val tX = targetX * scaleX
            val tY = targetY * scaleY

            drawCircle(
                color = Color(0xFFFF9900).copy(alpha = 0.6f),
                radius = 50f * pxPerKm * scaleX,
                center = Offset(tX, tY),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            )

            val crossSize = 12.dp.toPx()
            drawLine(
                color = Color(0xFFFF3333),
                start = Offset(tX - crossSize, tY),
                end = Offset(tX + crossSize, tY),
                strokeWidth = 3.5.dp.toPx()
            )
            drawLine(
                color = Color(0xFFFF3333),
                start = Offset(tX, tY - crossSize),
                end = Offset(tX, tY + crossSize),
                strokeWidth = 3.5.dp.toPx()
            )

            threats.forEach { t ->
                val tcX = t.cx.toFloat() * scaleX
                val tcY = t.cy.toFloat() * scaleY
                val twX = t.wx.toFloat() * scaleX
                val twY = t.wy.toFloat() * scaleY

                val intensityColor = when (t.maxInt) {
                    1 -> Color(0xFF00FFFF)
                    2 -> Color(0xFF0088FF)
                    3 -> Color(0xFF00FF00)
                    4 -> Color(0xFFFFA500)
                    5 -> Color(0xFFFF0000)
                    else -> Color.White
                }

                drawCircle(
                    color = intensityColor,
                    radius = 8.dp.toPx(),
                    center = Offset(tcX, tcY)
                )
                drawCircle(
                    color = Color.Black,
                    radius = 8.dp.toPx(),
                    center = Offset(tcX, tcY),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                val wCrossSize = 4.dp.toPx()
                drawLine(
                    color = Color.White,
                    start = Offset(twX - wCrossSize, twY - wCrossSize),
                    end = Offset(twX + wCrossSize, twY + wCrossSize),
                    strokeWidth = 1.5.dp.toPx()
                )
                drawLine(
                    color = Color.White,
                    start = Offset(twX + wCrossSize, twY - wCrossSize),
                    end = Offset(twX - wCrossSize, twY + wCrossSize),
                    strokeWidth = 1.5.dp.toPx()
                )

                drawLine(
                    color = Color.White.copy(alpha = 0.8f),
                    start = Offset(tcX, tcY),
                    end = Offset(twX, twY),
                    strokeWidth = 2.dp.toPx()
                )

                val coneLen = 65f * scaleX
                val endX = tcX + t.dirX.toFloat() * coneLen
                val endY = tcY + t.dirY.toFloat() * coneLen

                drawLine(
                    color = Color.Magenta,
                    start = Offset(tcX, tcY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.5.dp.toPx()
                )

                val angle = Math.atan2(t.dirY, t.dirX)
                val arrowSize = 8.dp.toPx()
                val arrowAngle1 = angle + Math.PI - Math.toRadians(30.0)
                val arrowAngle2 = angle + Math.PI + Math.toRadians(30.0)

                drawLine(
                    color = Color.Magenta,
                    start = Offset(endX, endY),
                    end = Offset((endX + Math.cos(arrowAngle1) * arrowSize).toFloat(), (endY + Math.sin(arrowAngle1) * arrowSize).toFloat()),
                    strokeWidth = 2.5.dp.toPx()
                )
                drawLine(
                    color = Color.Magenta,
                    start = Offset(endX, endY),
                    end = Offset((endX + Math.cos(arrowAngle2) * arrowSize).toFloat(), (endY + Math.sin(arrowAngle2) * arrowSize).toFloat()),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        }
    }
}

@Composable
fun NowcastProjectionChart(
    dbzList: List<Double>,
    rainRates: List<Double>,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val customTypeface = android.graphics.Typeface.SANS_SERIF

    val isDark = isSystemInDarkTheme()
    val axisColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
    val gridColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = "AI 6 Saatlik Nowcast Yağış Projeksiyonu",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color(0xFF00FFFF).copy(alpha = 0.3f))
                        .border(1.5.dp, Color(0xFF00FFFF))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Yansıtıcılık (dBZ)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(width = 20.dp, height = 4.dp)) {
                    drawLine(
                        color = Color(0xFFFF9900),
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("Yağış Şiddeti (mm/sa)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                val paddingLeft = 40.dp.toPx()
                val paddingRight = 40.dp.toPx()
                val paddingTop = 15.dp.toPx()
                val paddingBottom = 25.dp.toPx()

                val chartW = w - paddingLeft - paddingRight
                val chartH = h - paddingTop - paddingBottom

                val steps = 5
                for (i in 0..steps) {
                    val ratio = i.toFloat() / steps
                    val y = paddingTop + chartH * (1f - ratio)

                    drawLine(
                        color = gridColor,
                        start = Offset(paddingLeft, y),
                        end = Offset(paddingLeft + chartW, y),
                        strokeWidth = 1f
                    )

                    val dbzVal = (ratio * 65f).toInt()
                    drawContext.canvas.nativeCanvas.drawText(
                        "$dbzVal",
                        paddingLeft - 8.dp.toPx(),
                        y + 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                            textSize = 9.sp.toPx()
                            textAlign = android.graphics.Paint.Align.RIGHT
                            isAntiAlias = true
                        }
                    )

                    val rainVal = String.format("%.1f", ratio * 15f)
                    drawContext.canvas.nativeCanvas.drawText(
                        rainVal,
                        paddingLeft + chartW + 8.dp.toPx(),
                        y + 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                            textSize = 9.sp.toPx()
                            textAlign = android.graphics.Paint.Align.LEFT
                            isAntiAlias = true
                        }
                    )
                }

                val stepSize = dbzList.size
                for (i in 0 until stepSize step 6) {
                    val ratio = i.toFloat() / (stepSize - 1)
                    val x = paddingLeft + ratio * chartW

                    drawLine(
                        color = gridColor,
                        start = Offset(x, paddingTop),
                        end = Offset(x, paddingTop + chartH),
                        strokeWidth = 1f
                    )

                    val label = if (i == 0) "Şimdi" else "T+${i / 6} Sa"
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        x,
                        paddingTop + chartH + 16.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                            textSize = 9.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                    )
                }

                if (dbzList.isNotEmpty()) {
                    val path = Path()
                    path.moveTo(paddingLeft, paddingTop + chartH)

                    dbzList.forEachIndexed { idx, dbz ->
                        val rX = idx.toFloat() / (dbzList.size - 1)
                        val x = paddingLeft + rX * chartW
                        val rY = (dbz / 65.0).coerceIn(0.0, 1.0).toFloat()
                        val y = paddingTop + chartH * (1f - rY)
                        path.lineTo(x, y)
                    }
                    path.lineTo(paddingLeft + chartW, paddingTop + chartH)
                    path.close()

                    drawPath(
                        path = path,
                        color = Color(0xFF00FFFF).copy(alpha = 0.15f)
                    )

                    val linePath = Path()
                    dbzList.forEachIndexed { idx, dbz ->
                        val rX = idx.toFloat() / (dbzList.size - 1)
                        val x = paddingLeft + rX * chartW
                        val rY = (dbz / 65.0).coerceIn(0.0, 1.0).toFloat()
                        val y = paddingTop + chartH * (1f - rY)
                        if (idx == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
                    }
                    drawPath(
                        path = linePath,
                        color = Color(0xFF00FFFF),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }

                if (rainRates.isNotEmpty()) {
                    val rainPath = Path()
                    rainRates.forEachIndexed { idx, rr ->
                        val rX = idx.toFloat() / (rainRates.size - 1)
                        val x = paddingLeft + rX * chartW
                        val rY = (rr / 15.0).coerceIn(0.0, 1.0).toFloat()
                        val y = paddingTop + chartH * (1f - rY)
                        if (idx == 0) rainPath.moveTo(x, y) else rainPath.lineTo(x, y)
                    }
                    drawPath(
                        path = rainPath,
                        color = Color(0xFFFF9900),
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PolonyaBarChart(
    forecasts: List<ImgwAlaroForecast>,
    getValue: (ImgwAlaroForecast) -> Double,
    barColor: Color,
    onPointSelected: ((Int) -> Unit)? = null, selectedIndex: Int? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val customTypeface = android.graphics.Typeface.SANS_SERIF

    val scrollState = rememberScrollState()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val stepWidth = 45.dp
    val chartHeight = 140.dp
    val totalWidth = stepWidth * forecasts.size

    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.parseColor("#AAAAAA")
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = customTypeface ?: android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Canvas(
            modifier = Modifier
                .width(totalWidth)
                .height(chartHeight)
                .pointerInput(forecasts) {
                    detectTapGestures { offset ->
                        if (onPointSelected != null) {
                            val colWidthPx = with(density) { stepWidth.toPx() }
                            val index = (offset.x / colWidthPx).toInt()
                            if (index in forecasts.indices) {
                                onPointSelected(index)
                            }
                        }
                    }
                }
        ) {
            val stepWidthPx = stepWidth.toPx()
            val canvasHeight = size.height
            val bottomPadding = 35.dp.toPx()
            val topPadding = 25.dp.toPx()
            val chartAreaHeight = canvasHeight - bottomPadding - topPadding

            drawWatermark()

            var maxVal = forecasts.maxOfOrNull { getValue(it) } ?: 1.0
            if (maxVal == 0.0) maxVal = 1.0

            for (i in forecasts.indices) {
                val x = i * stepWidthPx + (stepWidthPx / 2)
                drawLine(
                    color = Color(0x0C000000),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight - bottomPadding),
                    strokeWidth = 1.dp.toPx()
                )
            }

            forecasts.forEachIndexed { i, forecast ->
                val xCenter = i * stepWidthPx + (stepWidthPx / 2)
                if (selectedIndex == i) {
                    drawRect(
                        color = Color(0x33007AFF),
                        topLeft = androidx.compose.ui.geometry.Offset(xCenter - (stepWidthPx / 2), 0f),
                        size = androidx.compose.ui.geometry.Size(stepWidthPx, canvasHeight)
                    )
                }
                val yVal = getValue(forecast)

                if (yVal > 0) {
                    val barHeight = (yVal / maxVal * chartAreaHeight).toFloat()
                    val barWidth = 14.dp.toPx()
                    val left = xCenter - (barWidth / 2)
                    val top = canvasHeight - bottomPadding - barHeight

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
                    )

                    val displayVal = if (yVal == yVal.toInt().toDouble()) "${yVal.toInt()}" else String.format("%.1f", yVal)
                    drawContext.canvas.nativeCanvas.drawText(
                        displayVal,
                        xCenter,
                        top - 6.dp.toPx(),
                        textPaint.apply { color = android.graphics.Color.parseColor("#E3E3E3") }
                    )
                }

                

drawContext.canvas.nativeCanvas.drawText(
                    forecast.formattedTime,
                    xCenter,
                    canvasHeight - 4.dp.toPx(),
                    textPaint.apply { color = android.graphics.Color.parseColor("#AAAAAA") }
                )
            }
        }
    }
}
