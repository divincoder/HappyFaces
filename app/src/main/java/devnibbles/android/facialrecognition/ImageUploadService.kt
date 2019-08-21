package devnibbles.android.facialrecognition

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ImageUploadService {

    @POST("/API/MobileDetect")
    fun postImageToServerAsync(
        @Body body: ImageModel
    ): Call<ImageResponse>


    @POST("/API/CustomerContact")
    fun postCustomerInformation(
        @Body body: CustomerRequestModel
    ): Call<CustomerResponseModel>

}