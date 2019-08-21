package devnibbles.android.facialrecognition


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_collect_info_fragment_dialog.view.*
import android.graphics.BitmapFactory



/**
 * A simple [Fragment] subclass.
 *
 */
class CollectInfoFragmentDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_collect_info_fragment_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bitmapdata: ByteArray? = arguments?.getByteArray("BYTE_ARRAY")

        if (bitmapdata != null) {
            val bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.size)
            view.preview_image.setImageBitmap(bitmap)
        }

    }

}
