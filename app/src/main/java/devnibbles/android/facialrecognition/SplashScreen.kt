package devnibbles.android.facialrecognition

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({
            runOnUiThread {
                startActivity(Intent(this@SplashScreen, MLKitActivity::class.java))
                finish()
            }
        }, 3000)

    }
}
