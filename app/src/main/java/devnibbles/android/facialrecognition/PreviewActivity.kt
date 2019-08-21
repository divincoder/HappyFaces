package devnibbles.android.facialrecognition

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_preview.*
import java.util.*


class PreviewActivity : AppCompatActivity() {

    var endOfField = false
    var speech = ""
    var name = ""
    var phone = ""

    lateinit var textToSpeech: TextToSpeech
    var params: HashMap<String, String> = HashMap()
    private lateinit var mViewModel: ImageViewModel

    private val utteranceProgressListener: UtteranceProgressListener = object : UtteranceProgressListener() {

        override fun onDone(utteranceId: String?) {

            //    val requestCode = Integer.parseInt(utteranceId!!)

//            if (listener != null) {
//                listener.onSpeechCompleted(requestCode)
//            }

            //remember to call shutdown, when you are done with textToSpeech
            textToSpeech.shutdown()
        }

        override fun onError(utteranceId: String?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onStart(utteranceId: String?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        mViewModel = ViewModelProviders.of(this@PreviewActivity).get(ImageViewModel::class.java)

        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {

                textToSpeech.language = Locale.ENGLISH
                textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener)

                speech = "Hello!, Please Enter your Full Name Below"
                // params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = utteranceProgressListener.

                textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null)
            }
        }

        val byteArray = intent.getByteArrayExtra("BYTE_ARRAY")

        if (byteArray != null) {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            val rotateBitmap = rotateImage(bitmap, 270F)

//            preview_image.setImageBitmap(bitmap)
            preview_image.setImageBitmap(rotateBitmap)
        }

        submit_button.setOnClickListener {
            Toast.makeText(this, "button clicked", Toast.LENGTH_SHORT).show()
            name = name_EDT.text.toString()
            phone = phone_EDT.text.toString()
            if (endOfField) {
                Toast.makeText(this, "button clicked after $name and $phone", Toast.LENGTH_SHORT).show()

                mViewModel.postCustomerData(name, phone)
                    .observe(this@PreviewActivity, Observer {
                        if (it) {
                            Log.d("ok", "data saved successfully")
                            Toast.makeText(this, "Visitor Data Saved Successfully", Toast.LENGTH_SHORT).show()
                            onBackPressed()
                        } else {
                            Log.d("ok", "data saving failed")
                            Toast.makeText(this, "Visitor Data Not Saved", Toast.LENGTH_SHORT).show()
                        }
                    })

            } else {
                showNextField()
            }

        }

    }

    private fun showNextField() {
        input_flippers.showNext()
        speech = "Please, Enter your Phone Number Below"
        textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null)

        submit_button.text = getString(R.string.submit)
        endOfField = true

        previous_button.visibility = View.VISIBLE
        previous_button.setOnClickListener {
            input_flippers.showPrevious()
            endOfField = false
            previous_button.visibility = View.INVISIBLE
            speech = "Hello!, Please Enter your Full Name Below"
            submit_button.text = getString(R.string.next)
            textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    public override fun onPause() {
        textToSpeech.stop()
        textToSpeech.shutdown()
        super.onPause()
    }

    //Rotate Image
    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }
}
