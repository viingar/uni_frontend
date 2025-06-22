package com.example.myapplication.ViewModels

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.*
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.location.*
import com.yandex.mapkit.search.*
import com.yandex.runtime.Error
import com.yandex.mapkit.GeoObject
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType

class MapViewModel : ViewModel() {
    private val _searchResults = mutableStateOf<List<GeoObject>>(emptyList())
    val searchResults: State<List<GeoObject>> = _searchResults

    private var searchManager: SearchManager? = null
    private var searchSession: Session? = null

    private val _userLocation = mutableStateOf<Point?>(null)
    val userLocation: State<Point?> = _userLocation

    private val _userHeading = mutableStateOf<Float?>(null)
    val userHeading: State<Float?> = _userHeading

    private val _locationAccuracy = mutableStateOf(50f)
    val locationAccuracy: State<Float> = _locationAccuracy

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    private var drivingRouter: DrivingRouter? = null
    private var drivingSession: DrivingSession? = null

    fun initializeMapKit(context: Context) {
        try {
            MapKitFactory.initialize(context)

            locationManager = MapKitFactory.getInstance().createLocationManager()
            searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE)
            drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.ONLINE)


        } catch (e: Exception) {

        }
    }

    fun startLocationTracking() {
        if (locationManager == null) {

            return
        }

        locationListener = object : LocationListener {
            override fun onLocationUpdated(location: Location) {

                _userLocation.value = Point(location.position.latitude, location.position.longitude)
                location.accuracy?.let { _locationAccuracy.value = it.toFloat() }
                location.heading?.let { _userHeading.value = it.toFloat() }
            }

            override fun onLocationStatusUpdated(status: LocationStatus) {

            }
        }

        try {
            locationManager?.subscribeForLocationUpdates(
                1.0,
                1,
                100.0,
                true,
                FilteringMode.OFF,
                locationListener!!
            )

        } catch (e: Exception) {

        }
    }

    fun searchNearbyShops(point: Point) {
        if (searchManager == null) {

            return
        }

        _searchResults.value = emptyList()

        searchSession = searchManager?.submit(
            "магазин",
            Geometry.fromPoint(point),
            SearchOptions().apply {
                searchTypes = SearchType.BIZ.value
                resultPageSize = 20
            },
            object : Session.SearchListener {
                override fun onSearchResponse(response: Response) {
                    val results = response.collection.children.mapNotNull { it.obj }
                    _searchResults.value = results

                }

                override fun onSearchError(error: Error) {

                }
            }
        )
    }

    fun searchByQuery(query: String) {
        if (searchManager == null) {

            return
        }

        val centerPoint = _userLocation.value ?: Point(54.1838, 45.1749)
        _searchResults.value = emptyList()

        searchSession = searchManager?.submit(
            query,
            Geometry.fromPoint(centerPoint),
            SearchOptions().apply {
                searchTypes = SearchType.GEO.value or SearchType.BIZ.value
                resultPageSize = 20
            },
            object : Session.SearchListener {
                override fun onSearchResponse(response: Response) {
                    val results = response.collection.children.mapNotNull { it.obj }
                    _searchResults.value = results

                }

                override fun onSearchError(error: Error) {

                }
            }
        )
    }

    fun drawDrivingRoute(from: Point, to: Point, callback: (List<Polyline>) -> Unit) {
        val requestPoints = listOf(
            RequestPoint(from, RequestPointType.WAYPOINT, null, null),
            RequestPoint(to, RequestPointType.WAYPOINT, null, null)
        )

        val drivingOptions = DrivingOptions().apply {
            routesCount = 1
        }

        drivingSession = drivingRouter?.requestRoutes(
            requestPoints,
            drivingOptions,
            VehicleOptions(),
            object : DrivingSession.DrivingRouteListener {
                override fun onDrivingRoutes(routes: List<DrivingRoute>) {

                    val polylines = if (routes.isNotEmpty()) {
                        listOf(routes.first().geometry)
                    } else {
                        emptyList()
                    }
                    callback(polylines)
                }

                override fun onDrivingRoutesError(error: Error) {

                    callback(emptyList())
                }
            }
        )
    }

    fun stopLocationTracking() {
        locationListener?.let { listener ->
            locationManager?.unsubscribe(listener)
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    override fun onCleared() {
        searchSession?.cancel()
        stopLocationTracking()
        super.onCleared()
    }
}
