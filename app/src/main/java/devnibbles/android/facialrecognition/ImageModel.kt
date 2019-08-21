package devnibbles.android.facialrecognition


data class ImageModel(
    val image: String,
    val face_location: String,
    val frame_id: Int,
    val qview_id: String,
    val time_of_visit: String,
    val view_type: String
)

data class ImageResponse(

    val new_visit: String,
    val face_id: String
)

data class CustomerRequestModel (
    val face_id: String?,
    val customer_name: String,
    val phone_number: String
)

data class CustomerResponseModel (
    val status: String,
    val face_id: String
)