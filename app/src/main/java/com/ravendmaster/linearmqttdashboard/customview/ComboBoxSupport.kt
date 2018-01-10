package com.ravendmaster.linearmqttdashboard.customview

object ComboBoxSupport {
    fun getLabelByValue(valueString: String, valuesList: String): String {
        val values = valuesList.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (value in values) {
            val valueData = value.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (valueData.size > 0) {
                if (valueData[0] == valueString) {
                    return if (valueData.size > 1) valueData[1] else valueData[0]
                }
            }
        }
        return valueString
    }
}
