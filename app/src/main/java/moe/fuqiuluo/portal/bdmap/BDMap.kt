package moe.fuqiuluo.portal.bdmap

import android.util.Log
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.search.sug.SuggestionResult
import moe.fuqiuluo.portal.ext.CoordinateTransformUtil

fun SuggestionResult.toPoi(
    currentLocation: Pair<Double, Double>? = null
) = this.allSuggestions
    .filter { it.key != null && it.pt != null }
    .map {
    val bd09Lat = it.pt.latitude
    val bd09Lon = it.pt.longitude
    val wgs84 = CoordinateTransformUtil.bd09toWgs84(bd09Lon, bd09Lat)
    val lon = wgs84[0]
    val lat = wgs84[1]
    if (currentLocation != null) {
        Log.d("toPoi", "currentLocation: $currentLocation, lat: $lat, lon: $lon")
        Poi(
            name = it.key,
            address = it.city + " " + it.district,
            longitude = lon,
            latitude = lat,
            tag = it.tag,
        ).also {
            val distance = it.distanceTo(currentLocation.first, currentLocation.second).toInt()
            if (distance < 1000) {
                it.address = "${distance}m ${it.address}"
            } else {
                it.address = "${(distance / 1000.0).toString().take(4)}km ${it.address}"
            }
        }
    } else {
        Poi(
            name = it.key,
            address = it.city + " " + it.district,
            longitude = lon,
            latitude = lat,
            tag = it.tag,
        )
    }
}

fun BaiduMap.setMapConfig(mode: MyLocationConfiguration.LocationMode, resourceId: Int?) {
    setMyLocationConfiguration(MyLocationConfiguration(mode, true,  resourceId?.let { BitmapDescriptorFactory.fromResource(it) }))
}

fun BaiduMap.locateMe() {
    setMyLocationConfiguration(MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, null))
    setMyLocationConfiguration(MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null))
}