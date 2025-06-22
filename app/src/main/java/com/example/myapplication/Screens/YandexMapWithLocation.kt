package com.example.myapplication.Screens

import android.Manifest
import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.ViewModels.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.yandex.mapkit.Animation
import com.yandex.mapkit.GeoObject
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.map.TextStyle
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun YandexMapWithLocation(
    navController: NavController,
    viewModel: MapViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val userLocationState = viewModel.userLocation
    val locationAccuracyState = viewModel.locationAccuracy
    val searchResultsState = viewModel.searchResults

    val userLocation = userLocationState.value
    val searchResults = searchResultsState.value

    val items = listOf("Карта", "Главная", "Профиль")
    var selectedItem by remember { mutableStateOf(0) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSearchPoint by remember { mutableStateOf<Point?>(null) }

    var routePolylines by remember { mutableStateOf<List<Polyline>>(emptyList()) }

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        viewModel.initializeMapKit(context)
    }

    if (!locationPermissions.allPermissionsGranted) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val textToShow = if (locationPermissions.shouldShowRationale) {
                "Для отображения вашего местоположения необходимо предоставить разрешение"
            } else {
                "Для работы карты требуется доступ к местоположению"
            }

            Text(text = textToShow)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { locationPermissions.launchMultiplePermissionRequest() }) {
                Text("Запросить разрешение")
            }
        }
        return
    }

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted) {
            viewModel.startLocationTracking()
        }
    }

    val mapView = remember {
        MapView(context)
    }

    val moveToUserLocation: () -> Unit = {
        userLocation?.let { location ->
            val map = mapView.mapWindow.map
            map.move(
                CameraPosition(location, 15.0f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
        }
    }

    LaunchedEffect(userLocation, routePolylines, selectedSearchPoint) {

        val map = mapView.mapWindow.map
        val mapObjects = map.mapObjects
        mapObjects.clear()

        val userLocationPoint = userLocation ?: Point(54.18716, 45.17950)
        


        try {
            val icon = ImageProvider.fromResource(context, R.drawable.location)
            val placemark = mapObjects.addPlacemark()
            placemark.geometry = userLocationPoint
            placemark.setIcon(icon)
            placemark.setTextStyle(TextStyle().apply { size = 0f })
            placemark.setIconStyle(IconStyle().apply {
                anchor = PointF(0.5f, 0.5f)
                rotationType = RotationType.NO_ROTATION
                zIndex = 150f
                scale = 0.05f
            })

        } catch (e: Exception) {

            try {
                val placemark = mapObjects.addPlacemark()
                placemark.geometry = userLocationPoint
                placemark.setIconStyle(IconStyle().apply {
                    anchor = PointF(0.5f, 0.5f)
                    rotationType = RotationType.NO_ROTATION
                    zIndex = 150f
                    scale = 0.05f
                })

            } catch (e2: Exception) {

            }
        }


        selectedSearchPoint?.let { point ->
            try {
                val icon = ImageProvider.fromResource(context, R.drawable.location)
                mapObjects.addPlacemark().apply {
                    geometry = point
                    setIcon(icon)
                    setTextStyle(TextStyle().apply { size = 0f })
                    setIconStyle(IconStyle().apply {
                        anchor = PointF(0.5f, 0.5f)
                        rotationType = RotationType.NO_ROTATION
                        zIndex = 150f
                        scale = 0.05f
                    })
                }
            } catch (e: Exception) {
                Log.e("MapError", "Ошибка загрузки иконки выбранной точки", e)
            }
        }

        routePolylines.forEach { polyline ->
            mapObjects.addPolyline(polyline).apply {
                setStrokeColor(AndroidColor.BLUE)
                setStrokeWidth(5f)
                zIndex = 200f
            }
        }

        val target = when {
            selectedSearchPoint != null -> selectedSearchPoint!!
            else -> userLocationPoint
        }

        map.move(
            CameraPosition(target, 15.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )
        
        Log.d("MapDebug", "LaunchedEffect завершен. Камера перемещена к: ${target.latitude}, ${target.longitude}")
    }

    val distanceMeters = remember(userLocation, selectedSearchPoint) {
        val from = userLocation ?: Point(54.18716, 45.17950)
        val to = selectedSearchPoint
        if (to != null) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                from.latitude, from.longitude,
                to.latitude, to.longitude,
                results
            )
            results[0]
        } else null
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(56.dp)
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {},
                        label = {
                            Text(item, style = MaterialTheme.typography.titleMedium)
                        },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            when (index) {
                                1 -> navController.navigate("main")
                                2 -> navController.navigate("profile")
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )


            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                var debounceJob by remember { mutableStateOf<Job?>(null) }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        debounceJob?.cancel()
                        debounceJob = CoroutineScope(Dispatchers.Main).launch {
                            delay(500)
                            if (searchQuery.isNotBlank()) {
                                viewModel.searchByQuery(searchQuery)
                            }
                        }
                    },
                    label = { Text("Поиск") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                )

                ShowSearchResults(
                    searchResults = searchResults,
                    userLocation = userLocation,
                    selectedPoint = selectedSearchPoint,
                    onSelect = { point ->
                        selectedSearchPoint = point
                        searchQuery = ""
                        viewModel.clearSearchResults()

                        val fromPoint = userLocation ?: Point(54.18706, 45.17913)
                        viewModel.drawDrivingRoute(fromPoint, point) { polylines ->
                            routePolylines = polylines
                        }
                    }
                )
            }

            if (userLocation != null) {

                FloatingActionButton(
                    onClick = { viewModel.searchNearbyShops(userLocation) },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Магазины рядом")
                }


                FloatingActionButton(
                    onClick = moveToUserLocation,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Мое местоположение")
                }
            }
        }
    }
}

@Composable
fun ShowSearchResults(
    searchResults: List<GeoObject>,
    userLocation: Point?,
    selectedPoint: Point?,
    onSelect: (Point) -> Unit
) {
    if (searchResults.isNotEmpty()) {
        val sortedResults = remember(searchResults, userLocation) {
            searchResults
                .mapNotNull { geoObject ->
                    val point = geoObject.geometry.firstOrNull()?.point
                    val name = geoObject.name ?: geoObject.descriptionText ?: "Без названия"


                    val address = when {
                        geoObject.descriptionText != null && geoObject.descriptionText != name -> geoObject.descriptionText
                        else -> null
                    }

                    if (point != null) {
                        val distance = if (userLocation != null) {
                            val results = FloatArray(1)
                            android.location.Location.distanceBetween(
                                userLocation.latitude, userLocation.longitude,
                                point.latitude, point.longitude,
                                results
                            )
                            results[0]
                        } else {

                            val defaultPoint = Point(54.18716, 45.17950)
                            val results = FloatArray(1)
                            android.location.Location.distanceBetween(
                                defaultPoint.latitude, defaultPoint.longitude,
                                point.latitude, point.longitude,
                                results
                            )
                            results[0]
                        }

                        listOf(geoObject, point, distance, address)
                    } else null
                }
                .sortedBy { it[2] as? Float ?: Float.MAX_VALUE }
        }

        val nearestPoint = sortedResults.firstOrNull()?.get(1) as? Point

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
        ) {
            items(sortedResults.size) { index ->
                val result = sortedResults[index]
                val geoObject = result[0] as GeoObject
                val point = result[1] as Point
                val distance = result[2] as? Float
                val address = result[3] as? String
                
                val name = geoObject.name ?: geoObject.descriptionText ?: "Без названия"
                val isSelected = selectedPoint == point
                val isNearest = nearestPoint == point

                ListItem(
                    headlineContent = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(name)
                                if (isNearest) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Ближайший",
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            address?.let { addr ->
                                Text(
                                    text = addr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                            }
                            distance?.let {
                                Text(
                                    text = "Расстояние: ${"%.0f".format(it)} м",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .background(if (isSelected) Color.LightGray else Color.Transparent)
                        .clickable { onSelect(point) }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}
