package devnibbles.android.facialrecognition

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import devnibbles.android.facialrecognition.classify.common.*
import devnibbles.android.facialrecognition.detect.mlkit.FaceDetector
import devnibbles.android.facialrecognition.detect.mlkit.FaceGraphic
import devnibbles.android.facialrecognition.detect.mlkit.FrameMetadata
import devnibbles.android.facialrecognition.detect.mlkit.MLCameraSource
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer


class MLKitActivity : AbstractActivity() {

    companion object {
        private const val TAG = "MLKitActivity"
    }

    private var mCameraSource: MLCameraSource? = null
    private lateinit var mViewModel: ImageViewModel
    private var faceText: TextView? = null


    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        mViewModel = ViewModelProviders.of(this).get(ImageViewModel::class.java)

//        mViewModel = ViewModelProviders.of(this).get(CloudAutoMLViewModel::class.java)
//        mViewModel.subscribeClassifications()
//            .observe(this, Observer<Resource<FaceClassification, Throwable>> { resource ->
//                when (resource) {
//                    is LoadingResource -> {
//                        System.out.println("Classifying...")
//                    }
//                    is SuccessResource -> {
//                        System.out.println("SuccessResource : " + resource.data)
//                        val faceId = resource.data.faceId
//                        val name = resource.data.name
//                        val score = resource.data.confidence
//                        (mGraphicOverlay.find(faceId) as? FaceGraphic)?.setName("$name ($score)")
//                    }
//                    is ErrorResource -> {
//                        System.out.println("ErrorResource : " + resource.data)
//                        resource.errorData?.printStackTrace()
//                    }
//                }
//            })
    }

    /**
     * Creates and starts the camera.
     */
    override fun createCameraSource() {
        mCameraSource = MLCameraSource(this, mGraphicOverlay)
        mCameraSource!!.setMachineLearningFrameProcessor(FaceDetector(object : FaceDetector.DetectorCallback {
            override fun onSuccess(
                frameData: ByteBuffer,
                results: List<FirebaseVisionFace>,
                frameMetadata: FrameMetadata,
                firebaseFrameMetadata: FirebaseVisionImageMetadata
            ) {
                if (results.isEmpty()) {
                    // No faces in frame, so clear frame of any previous faces.
                    mGraphicOverlay.clear()
                } else {
                    // We have faces
                    mCameraPreview.stop()
                    faceText?.text = "Face Detected"
                    results.forEach { face ->
                        val existingFace = mGraphicOverlay.find(face.trackingId) as FaceGraphic?
                        if (existingFace == null) {
                            // A new face has been detected.
                            Log.d("TAG", "New Face detected")
                            val faceGraphic = FaceGraphic(face.trackingId, mGraphicOverlay)
                            mGraphicOverlay.add(faceGraphic)

                            //probable try to save this assuming they don't return a the image back to you
                            val byteArray = frameData.convertToByteArray(frameMetadata)

//                            showDialogWithValues(frameData, frameMetadata)

                            // Lets try and find out who this face belongs to
                            mViewModel.postImageForResponse(byteArray, face.boundingBox, face.trackingId).observe(this@MLKitActivity, Observer {
                                if (it) {
                                    showDialogWithValues(frameData, frameMetadata)
                                    //faceText?.text = "New Visitor Detected"
                                    Toast.makeText(this@MLKitActivity, "New Visitor Detected", Toast.LENGTH_SHORT).show()
                                } else {
                                    mCameraPreview.start(mCameraSource)
//                                    faceText?.text = "Visitor is already registered"
//                                    faceText?.text = getString(R.string.face_detections_currently_running)
                                    Toast.makeText(this@MLKitActivity, "visitor already registered", Toast.LENGTH_SHORT).show()
                                }
                            })

                        } else {
                            // We have an existing face, update its position in the frame.
                            existingFace.updateFace(face)
                        }
                    }

                    mGraphicOverlay.postInvalidate()

                }
            }

            override fun onFailure(exception: Exception) {
                exception.printStackTrace()
            }

        }))
    }

    /**
     * Starts or restarts the camera source, if it exists.
     */
    override fun startCameraSource() {
        checkGooglePlayServices()

        if (mCameraSource != null) {
            try {
                mCameraPreview.start(mCameraSource!!, mGraphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                mCameraSource!!.release()
                mCameraSource = null
            }
        }
    }

    /**
     * Releases the resources associated with the camera source.
     */
    override fun releaseCameraSource() {
        if (mCameraSource != null) {
            mCameraSource!!.release()
            mCameraSource = null
        }
    }

    private fun showDialogWithValues(byteBuffer: ByteBuffer, frameMetadata: FrameMetadata) {
        val byteArray = byteBuffer.convertToByteArray(frameMetadata)

        val intent = Intent(this, PreviewActivity::class.java).apply {
            putExtra("BYTE_ARRAY", byteArray)
            putExtra("FACE_ID", "")
        }

        startActivity(intent)
    }

    private fun convertBitmapToBase64(bitmap: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val encoded = Base64.encodeToString(byteArray, Base64.DEFAULT)
        Log.d("TAG", encoded)
    }


}
