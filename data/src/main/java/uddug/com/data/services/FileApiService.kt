package uddug.com.data.services

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import uddug.com.data.services.models.response.file.UploadFileDto

interface FileApiService {
    @Multipart
    @POST("core/files")
    suspend fun uploadFiles(
        @Part files: List<MultipartBody.Part>,
        @Query("raw") raw: Boolean = false
    ): List<UploadFileDto>
}
