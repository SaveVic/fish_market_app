package ppl.asn.fishmarket

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import ppl.asn.fishmarket.data.PreferencesData

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashscreenActivity : AppCompatActivity() {

    private val loadingTime = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        Handler().postDelayed({
            val token = PreferencesData.getToken(baseContext)
            if(token.isEmpty()){
                val intent = Intent(this@SplashscreenActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else{
                val intent = Intent(this@SplashscreenActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, loadingTime)
    }

    override fun onBackPressed() {}
}
