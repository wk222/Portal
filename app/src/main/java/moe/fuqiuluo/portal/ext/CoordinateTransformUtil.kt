package moe.fuqiuluo.portal.ext

import kotlin.math.*

/**
 * 百度坐标（BD09）、国测局坐标（火星坐标，GCJ02）、和WGS84坐标系之间的转换工具
 *
 * 参考 https://github.com/wandergis/coordtransform 实现的Kotlin版本
 */
object CoordinateTransformUtil {
    private val X_PI = 3.14159265358979324 * 3000.0 / 180.0
    private const val PI = 3.1415926535897932384626
    private const val A = 6378245.0 // 长半轴
    private const val EE = 0.00669342162296594323 // 扁率

    /**
     * 百度坐标系(BD-09)转WGS84坐标
     * @param lng 百度坐标经度
     * @param lat 百度坐标纬度
     * @return WGS84坐标 [经度, 纬度]
     */
    fun bd09toWgs84(lng: Double, lat: Double): DoubleArray {
        val gcj = bd09toGcj02(lng, lat)
        return gcj02toWgs84(gcj[0], gcj[1])
    }

    /**
     * WGS84坐标转百度坐标系(BD-09)
     * @param lng WGS84坐标系的经度
     * @param lat WGS84坐标系的纬度
     * @return 百度坐标 [经度, 纬度]
     */
    fun wgs84toBd09(lng: Double, lat: Double): DoubleArray {
        val gcj = wgs84toGcj02(lng, lat)
        return gcj02toBd09(gcj[0], gcj[1])
    }

    /**
     * 火星坐标系(GCJ-02)转百度坐标系(BD-09)
     * 谷歌、高德——>百度
     * @param lng 火星坐标经度
     * @param lat 火星坐标纬度
     * @return 百度坐标 [经度, 纬度]
     */
    fun gcj02toBd09(lng: Double, lat: Double): DoubleArray {
        val z = sqrt(lng * lng + lat * lat) + 0.00002 * sin(lat * X_PI)
        val theta = atan2(lat, lng) + 0.000003 * cos(lng * X_PI)
        val bdLng = z * cos(theta) + 0.0065
        val bdLat = z * sin(theta) + 0.006
        return doubleArrayOf(bdLng, bdLat)
    }

    /**
     * 百度坐标系(BD-09)转火星坐标系(GCJ-02)
     * 百度——>谷歌、高德
     * @param bdLng 百度坐标经度
     * @param bdLat 百度坐标纬度
     * @return 火星坐标 [经度, 纬度]
     */
    fun bd09toGcj02(bdLng: Double, bdLat: Double): DoubleArray {
        val x = bdLng - 0.0065
        val y = bdLat - 0.006
        val z = sqrt(x * x + y * y) - 0.00002 * sin(y * X_PI)
        val theta = atan2(y, x) - 0.000003 * cos(x * X_PI)
        val ggLng = z * cos(theta)
        val ggLat = z * sin(theta)
        return doubleArrayOf(ggLng, ggLat)
    }

    /**
     * WGS84转GCJ02(火星坐标系)
     * @param lng WGS84坐标系的经度
     * @param lat WGS84坐标系的纬度
     * @return 火星坐标 [经度, 纬度]
     */
    fun wgs84toGcj02(lng: Double, lat: Double): DoubleArray {
        if (outOfChina(lng, lat)) {
            return doubleArrayOf(lng, lat)
        }
        var dLat = transformLat(lng - 105.0, lat - 35.0)
        var dLng = transformLng(lng - 105.0, lat - 35.0)
        val radLat = lat / 180.0 * PI
        var magic = sin(radLat)
        magic = 1 - EE * magic * magic
        val sqrtMagic = sqrt(magic)
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI)
        dLng = (dLng * 180.0) / (A / sqrtMagic * cos(radLat) * PI)
        val mgLat = lat + dLat
        val mgLng = lng + dLng
        return doubleArrayOf(mgLng, mgLat)
    }

    /**
     * GCJ02(火星坐标系)转WGS84
     * @param lng 火星坐标系的经度
     * @param lat 火星坐标系纬度
     * @return WGS84坐标 [经度, 纬度]
     */
    fun gcj02toWgs84(lng: Double, lat: Double): DoubleArray {
        if (outOfChina(lng, lat)) {
            return doubleArrayOf(lng, lat)
        }
        var dLat = transformLat(lng - 105.0, lat - 35.0)
        var dLng = transformLng(lng - 105.0, lat - 35.0)
        val radLat = lat / 180.0 * PI
        var magic = sin(radLat)
        magic = 1 - EE * magic * magic
        val sqrtMagic = sqrt(magic)
        dLat = (dLat * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI)
        dLng = (dLng * 180.0) / (A / sqrtMagic * cos(radLat) * PI)
        val mgLat = lat + dLat
        val mgLng = lng + dLng
        return doubleArrayOf(lng * 2 - mgLng, lat * 2 - mgLat)
    }

    private fun transformLat(lng: Double, lat: Double): Double {
        var ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * sqrt(abs(lng))
        ret += (20.0 * sin(6.0 * lng * PI) + 20.0 * sin(2.0 * lng * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(lat * PI) + 40.0 * sin(lat / 3.0 * PI)) * 2.0 / 3.0
        ret += (160.0 * sin(lat / 12.0 * PI) + 320 * sin(lat * PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    private fun transformLng(lng: Double, lat: Double): Double {
        var ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * sqrt(abs(lng))
        ret += (20.0 * sin(6.0 * lng * PI) + 20.0 * sin(2.0 * lng * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(lng * PI) + 40.0 * sin(lng / 3.0 * PI)) * 2.0 / 3.0
        ret += (150.0 * sin(lng / 12.0 * PI) + 300.0 * sin(lng / 30.0 * PI)) * 2.0 / 3.0
        return ret
    }

    /**
     * 判断是否在国内，不在国内不做偏移
     */
    fun outOfChina(lng: Double, lat: Double): Boolean {
        return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271
    }
}
