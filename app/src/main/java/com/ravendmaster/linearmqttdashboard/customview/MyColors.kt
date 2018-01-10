package com.ravendmaster.linearmqttdashboard.customview

object MyColors {

    val colors = intArrayOf(-0xe54364, -7617718, -0xcb6725, -0x64a64a, -0xcbb6a2, -0xe95f7b, -0xd851a0, -0xd67f47, -0x71bb53, -0xd3c1b0, -0xe3bf1, -0x1981de, -0x18b3c4, -0x130f0f, -0x6a5a5a, -0xc63ee, -0x2cac00, -0x3fc6d5, -0x423c39, -0x807373)
    //           0          1            2            3           4
    //           5
    //           10
    //           15

    //public static final int RGB_LED_RED = -2937298;
    //public static final int RGB_LED_YELLOW = -5317;
    //public static final int RGB_LED_GREEN = -7617718;

    //public static final int RGB_GRAY = 0xAAAAAA;

    val dark: Int
        get() = colors[4]

    val asBlack: Int
        get() = colors[9]

    val blue: Int
        get() = colors[7]

    val gray: Int
        get() = colors[19]
    val ltGray: Int
        get() = colors[14]
    val veryLtGray: Int
        get() = colors[18]

    val white: Int
        get() = colors[13]

    val red: Int
        get() = colors[12]
    val yellow: Int
        get() = colors[10]
    val green: Int
        get() = colors[1]

    val black: Int
        get() = -0x1000000

    fun getColorByIndex(index: Int): Int? {
        return if (index >= colors.size) null else colors[index]
    }
}
