package com.ironleft.corona.api

import com.google.gson.annotations.SerializedName



data class Response<T> (
    @SerializedName("status") val status: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("msg") val message: String,
    @SerializedName("data") val data: T
)


data class Lists<T>(
    @SerializedName("total_rows") val total_rows: String?,
    @SerializedName("list") val list: ArrayList<T>)

data class DataNews(
    @SerializedName("title") val title: String?,
    @SerializedName("post_datetime") val post_datetime: String?,
    @SerializedName("img") val img: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("post_username") val post_username: String?,
    @SerializedName("post_hit") val post_hit: String?)

data class DataCountry(
    @SerializedName("country") val country: String?,
    @SerializedName("confirmed") val confirmed: String?,
    @SerializedName("deaths") val deaths: String?,
    @SerializedName("recovered") val recovered: String?,
    @SerializedName("gap_confirmed") val gap_confirmed: String?,
    @SerializedName("gap_deaths") val gap_deaths: String?,
    @SerializedName("gap_recovered") val gap_recovered: String?,
    @SerializedName("total") val total: String?,
    @SerializedName("udate") val udate: String?,
    @SerializedName("last_update") val last_update: String?,
    @SerializedName("lati") val lati: String?,
    @SerializedName("longi") val longi: String?)

data class DataGraph(
    @SerializedName("country") val country: String?,
    @SerializedName("ymd") val ymd: String?,
    @SerializedName("confirmed") val confirmed: String?,
    @SerializedName("deaths") val deaths: String?,
    @SerializedName("recovered") val recovered: String?)
