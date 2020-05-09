package com.ocm.smartreceptionsdkdemo

import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import java.io.IOException
import java.io.InputStreamReader
import java.io.LineNumberReader

object MacHelper {
    private fun getAPNType(context: Context): Int {
        var netType = 0
        val connMgr = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo ?: return netType
        val nType = networkInfo.type
        if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = 1// wifi
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            val nSubType = networkInfo.subtype
            val mTelephony = context
                    .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS && !mTelephony.isNetworkRoaming) {
                netType = 2// 3G
            } else {
                netType = 3// 2G
            }
        } else {
            netType = 4
        }
        return netType
    }

    fun getLocalMac(context: Context): String? {
        var mac: String? = null
        var str: String? = ""
        val str1 = "cat /sys/class/net/wlan0/address"
        try {
            var pp: Process? = null
            try {
                var cmdStr = str1
                pp = Runtime.getRuntime().exec(cmdStr)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val ir = InputStreamReader(pp!!.inputStream)
            val input = LineNumberReader(ir)
            while (null != str) {
                str = input.readLine()
                if (str != null) {
                    mac = str.trim { it <= ' ' }// 去空格
                    break
                }
            }
        } catch (ex: IOException) {
            // 赋予默认值
            ex.printStackTrace()
        }

        return mac
    }
}
