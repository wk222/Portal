package moe.fuqiuluo.portal.ext

import com.baidu.location.BDLocation
import com.baidu.mapapi.model.LatLng

/**
 * BD09 LatLng (from Baidu Map SDK) → WGS84
 * 返回 Pair(latitude, longitude)
 */
val LatLng.wgs84: Pair<Double, Double>
    get() {
        val result = CoordinateTransformUtil.bd09toWgs84(longitude, latitude)
        return result[1] to result[0] // lat, lng
    }

/**
 * BD09 BDLocation (from Baidu Location SDK) → WGS84
 * 返回 Pair(latitude, longitude)
 */
val BDLocation.wgs84: Pair<Double, Double>
    get() {
        val result = CoordinateTransformUtil.bd09toWgs84(longitude, latitude)
        return result[1] to result[0] // lat, lng
    }

/**
 * WGS84 Pair(latitude, longitude) → BD09 LatLng (for display on Baidu Map)
 */
val Pair<Double, Double>.bd09: LatLng
    get() {
        val result = CoordinateTransformUtil.wgs84toBd09(second, first) // lng, lat
        return LatLng(result[1], result[0]) // LatLng(lat, lng)
    }

/**
 * 保留向后兼容：gcj02 属性现在等同于 bd09
 * （因为百度地图已切换到 BD09LL 原生坐标系）
 */
val Pair<Double, Double>.gcj02: LatLng
    get() = bd09