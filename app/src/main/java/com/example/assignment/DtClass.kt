package com.example.assignment

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

open class DtClass {

    val dateFormat = SimpleDateFormat("dd-mm-yyyy")

    fun millisecondsToDate(milliseconds:Long,dateFormat: SimpleDateFormat):String{
        return dateFormat.format(milliseconds)
    }

    fun dateToMilliseconds(date:String,dateFormat: SimpleDateFormat):Long{
        val mDate = dateFormat.parse(date)
        return mDate.time
    }
}