package devnibbles.android.facialrecognition

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class ImageViewModel(application: Application) : AndroidViewModel(application) {

    companion object{
        var customerFaceId: String? = ""
    }



    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle() //wen i commented it worked
        return rotatedImg
    }

    private fun encodeTobase64(image: Bitmap): String {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val b = baos.toByteArray()

        return Base64.encodeToString(b, Base64.DEFAULT)
    }


    fun postImageForResponse(imageBytes: ByteArray, boundingBox: Rect, faceId: Int): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        val faceLocation = "${boundingBox.top}, ${boundingBox.right}, ${boundingBox.bottom}, ${boundingBox.left}"

        val rotatedBitmap = rotateImage(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size), 270F)
//System.currentTimeMillis(); Calendar.getInstance().timeInMillis
        val body = ImageModel(
            encodeTobase64(rotatedBitmap),
//            Base64.encodeToString(rotatedBitmap.convertToByteArray(), Base64.DEFAULT),
            faceLocation, faceId, "qvf5ma0j001", "${System.currentTimeMillis()}", "in"
        )

        val imageCallback: Callback<ImageResponse> = object : Callback<ImageResponse> {

            override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                Log.d("TAG", "failed is ${t.cause.toString()}")
                result.postValue(false)
            }

            override fun onResponse(call: Call<ImageResponse>, response: Response<ImageResponse>) {
                if (response.isSuccessful) {
                    Log.d("TAG", "Success is ${response.body()?.new_visit}")

                    val imageRes = response.body() as ImageResponse
                    customerFaceId = imageRes.face_id
                    result.postValue(imageRes.new_visit == "true")
                }
            }

        }

        //make the post call
        getRESTService().postImageToServerAsync(body).enqueue(imageCallback)

        return result
    }

    fun postCustomerData(customerName: String, customerPhoneNo: String): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        // var body: CustomerRequestModel? = null

        //if (customerFaceId != null) {
        val body = CustomerRequestModel(customerFaceId, customerName, customerPhoneNo)

        Log.d("ok", customerFaceId)
        //}

        val customerCallback: Callback<CustomerResponseModel> = object : Callback<CustomerResponseModel> {
            override fun onFailure(call: Call<CustomerResponseModel>, t: Throwable) {
                Log.d("TAG", "customer upload failed ${t.cause.toString()}")
                result.postValue(false)
            }

            override fun onResponse(call: Call<CustomerResponseModel>, response: Response<CustomerResponseModel>) {
                if (response.isSuccessful) {
                    Log.d("TAG", "Customer uploaded ${response.body()?.status}")
                    //val res = response.body() as CustomerResponseModel
                    result.postValue(response.body()?.status == "success")
                }
            }

        }

        //make the post call
        //if (body != null)
        getRESTService().postCustomerInformation(body).enqueue(customerCallback)
        Log.d("ok", customerFaceId)

        return result
    }

    private fun getRESTService(): ImageUploadService {
        val gsonFactory = GsonConverterFactory
            .create()

        val networkClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addNetworkInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addNetworkInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                    val newRequest = chain.request().newBuilder()
                        .addHeader("Content-Type", "application/json")
//                        .addHeader("User-Agent", "Android-App")
                        .build()

                    return chain.proceed(newRequest)
                }

            }).build()

        return Retrofit.Builder()
            .baseUrl("https://qview.ai/")
            .addConverterFactory(gsonFactory)
            .client(networkClient)
            .build()
            .create(ImageUploadService::class.java)
    }
}