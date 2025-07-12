package com.imaginamos.farmatodo.networking.api

import com.imaginamos.farmatodo.model.algolia.ItemAlgolia
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface ApiAlgoliaProxy {
    @Headers("content-type: application/json")
    @GET("/1/indexes/{productIndex}/{objectID}")
    fun getItemByObjectID(
        @Path("productIndex") productIndex: String,
        @Path("objectID") objectID: String,
        @Header("x-nearby-stores") nearbyStores: String
    ): Call<ItemAlgolia>
}