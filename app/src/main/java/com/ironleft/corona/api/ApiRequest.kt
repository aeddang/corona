package com.ironleft.corona.api

import io.reactivex.Single
import retrofit2.http.*


interface ApiRequest{

    @GET(ApiConst.API_GOOGLE)
    fun getNews(
        @Query(ApiConst.FIELD_SMODE) smode: String?,
        @Query(ApiConst.FIELD_PAGE) page: Int,
        @Query(ApiConst.FIELD_PER_PAGE) per_page: Int
    ): Single<Lists<DataNews>>

    @GET(ApiConst.API_GOOGLE)
    fun getAllDatas(
        @Query(ApiConst.FIELD_SMODE) smode: String?
    ): Single<List<DataCountry>>

    @GET(ApiConst.API_GOOGLE)
    fun getDatas(
        @Query(ApiConst.FIELD_SMODE) smode: String?,
        @Query(ApiConst.FIELD_COUNTRY) country: String?,
        @Query(ApiConst.FIELD_PROVINCE) province: String?
    ): Single<Lists<DataNews>>

    @GET(ApiConst.API_GOOGLE)
    fun getGraphDatas(
        @Query(ApiConst.FIELD_SMODE) smode: String?,
        @Query(ApiConst.FIELD_COUNTRY) country: String?,
        @Query(ApiConst.FIELD_PROVINCE) province: String?
    ): Single<List<DataGraph>>
}

