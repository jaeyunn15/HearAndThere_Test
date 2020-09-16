package com.example.hearandthere_test.ui.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hearandthere_test.R
import com.example.hearandthere_test.ui.map.MapActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        btn_goTest.setOnClickListener {
            val nextIntent = Intent(this, MapActivity::class.java)
            startActivity(nextIntent)
        }

        btn_SendFeedback.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/SeqEpNvQf6qHGAKR7"))
            startActivity(intent);
        }
    }
}