package uz.androdev.milliytanlov

import android.net.http.SslCertificate
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import uz.androdev.milliytanlov.base.BaseWebViewFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

class WebViewFragment: BaseWebViewFragment() {
    override suspend fun getPageUrl(): String {
        return "https://milliytanlov.uz/"
    }

    override suspend fun initialDelay() {}

    override fun getWebsiteCertificates(): List<SslCertificate> {
        return emptyList()
    }

}