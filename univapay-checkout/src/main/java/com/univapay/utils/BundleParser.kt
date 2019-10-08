package com.univapay.utils

import android.os.Bundle
import java.util.*

/**
 * A helper class for parsing bundle.
 */
object BundleParser {

    fun fromMap(map: Map<String, String>?): Bundle {
        val b = Bundle()
        if (map != null) {
            val k = map.keys
            val keys = k.toTypedArray()
            val v = map.values
            val values = v.toTypedArray()


            b.putStringArray("keys", keys)
            b.putStringArray("values", values)
        }
        return b
    }

    fun toMap(b: Bundle): Map<String, String> {
        val keys = b.getStringArray("keys")
        val values = b.getStringArray("values")
        val map = HashMap<String, String>()

        if (keys != null && values != null) {
            if (keys.size != values.size) {
                throw IllegalStateException("Key & Value map don't match")
            }

            for (i in keys.indices) {
                map[keys[i]] = values[i]
            }
        }
        return map
    }
}
