package com.lib.util

import android.content.Context
import android.content.res.Resources
import java.text.DecimalFormat
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToLong

const val TAG = "UnitConvertUtil"

val Int.toPx: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.toDp: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun String.toSafeBoolean(): Boolean {
    return this == "true" || this == "1"
}

fun String.toSafeInt(): Int {
    if(this == "") return 0
    return this.toInt()
}

fun String.toSafeSting(): String {
    if(this == "") return ""
    return this.toString()
}

fun String.toSafePercent(): String {
    if(this == "") return "0"
    return String.format("%.0f", this.toDouble())
}

fun String.toSafe2Percent(): String {
    if(this == "") return "0"
    return String.format("%.0f", this.toDouble())
}

fun String.toSafeKal(): String {
    if(this == "") return "0"
    return this.toDouble().toKal()
}

fun String.toSafeTime(): String {
    if(this == "") return "0"
    return String.format("%.0f", this.toDouble())
}

fun String.toVersionNumber(): Int {
    val numStr = this.replace(".","")
    return numStr.toInt()
}

fun Int.toSimilarityPart():String{
    return when(this){
        1 -> "LeftArm"
        2 -> "RightArm"
        3 -> "LeftLeg"
        4 -> "RightLeg"
        5 -> "CoreBody"
        else -> "undefiend"
    }

}

fun Double.percentToDouble(): Double {
    return this / 100.0
}

fun Double.toPercent(): String {
    val pct = this * 100.0
    return String.format("%.2f", pct)
}

fun Float.toPercent(): String {
    val pct = this * 100.0f
    return String.format("%.2f", pct)
}

fun Double.toKal(): String {
    return String.format("%.0f", this)
}

fun Long.toCertainDigitsString(len: Int): String {
    val str = this.toString()
    if (str.length >= len) return str
    val digitsString = "00000000$str"
    val s = digitsString.length - len
    return digitsString.substring(s)
}


fun String.secToLong(): Long {
    val d = this.toDoubleOrNull() ?: 0.0
    return round(d * 1000.0).toLong()
}

fun String.secToTimeString(): String {
    return secToLong().millisecToTimeString()
}

fun String.secToMinTimeString(): String {
    return secToLong().secToMinTimeString()
}

fun Long.millisecToTimeString(div: String = ":", isFillDigits: Boolean = false): String {
    if(this < 0) return "00${div}00"
    val i: Long = (this.toDouble() / 1000f).roundToLong()
    val s: Long = i % 60
    val m: Long = floor((i % 3600).toDouble() / 60.0).toLong()
    val h: Long = floor(i.toDouble() / 3600.0).toLong()
    var str = ""
    if (isFillDigits || h > 0) str += (h.toCertainDigitsString(2) + div)
    str += (m.toCertainDigitsString(2) + div)
    str += (s.toCertainDigitsString(2))
    return str
}

fun Long.secToMinTimeString(): String {
    if(this < 0) return "00"
    val i: Long = (this.toDouble() / 1000f).roundToLong()
    val s: Long = i % 60
    val m: Long = floor(i / 60.0).toLong()
    var str = ""
    str += m
    // str += (s.toCertainDigitsString(2))
//    Log.d(TAG, "[$this][$i][$s][$m][$str]")
    return str
}

fun Long.millisecToSec(): Double {
    return this.toDouble() / 1000.0
}

fun String.toKoreanDigit():String{
    val units = arrayOf("만","천","백","십","")
    val nums = arrayOf("영","일","이","삼","사","오","육","칠","팔","구")
    val digit = this.toSafeInt()
    if(digit == 0) return nums[0]
    val digitString = digit.toLong().toCertainDigitsString(5)
    var returnString = ""
    digitString.forEachIndexed { index, c ->
        val d = c.toString().toInt()
        if(d == 0) return@forEachIndexed
        val dstr = if(d==1 && index != 4) "" else nums[d]
        returnString = "$returnString$dstr${units[index]}"
    }
    return returnString
}

fun String.decimalFormat(): String {
    val decimal = this.toLong()
    if (decimal > 999) {
        val df = DecimalFormat("#,###")
        return df.format(decimal)
    }
    return this
}


fun Float.toDp(context:Context): Float{
    return context.resources.displayMetrics.density * this
}

fun Int.toDp(context:Context): Float{
    return context.resources.displayMetrics.density * this.toFloat()
}

// 닉네임 글자수 자르기
fun textlength(temp :String, length :Int) :String{
    var text =""

    if(temp.length> length){
        text  = temp.substring(0,length)+".."
    } else {
        text =temp
    }
    return text
}