package com.mbd.cmscommon.data.local

import androidx.room.TypeConverter
import com.mbd.cmscommon.domain.model.FeeHead
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun feeHeadsToJson(heads: List<FeeHead>): String {
        val array = JSONArray()
        heads.forEach { head ->
            val obj = JSONObject()
            obj.put("label", head.label)
            obj.put("amount", head.amount)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun jsonToFeeHeads(json: String): List<FeeHead> {
        if (json.isBlank()) return emptyList()
        val array = JSONArray(json)
        return (0 until array.length()).map { index ->
            val obj = array.getJSONObject(index)
            FeeHead(label = obj.getString("label"), amount = obj.getDouble("amount"))
        }
    }
}
