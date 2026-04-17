package com.badgr.orbreader.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Expected JSON on success:
 *   { "text": "...", "wordCount": 1234, "fileType": "pdf" }
 * Expected JSON on error:
 *   { "error": "message" }
 */
data class ConversionResponse(
    val text: String?,
    val wordCount: Int?,
    val fileType: String?,
    val error: String?
)

interface ConvertApi {

    /**
     * POST /convert
     * Multipart field name: "file"
     */
    @Multipart
    @POST("convert")
    suspend fun convertFile(
        @Part file: MultipartBody.Part
    ): Response<ConversionResponse>
}
