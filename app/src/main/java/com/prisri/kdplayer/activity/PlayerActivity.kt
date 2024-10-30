package com.prisri.kdplayer.activity

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.prisri.kdplayer.MainActivity
import com.prisri.kdplayer.databinding.ActivityPlayerBinding
import com.prisri.kdplayer.player.MyPlayer

class PlayerActivity : AppCompatActivity(){
    private lateinit var binding: ActivityPlayerBinding
    var myPlayer: MyPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        val editor = getSharedPreferences("Themes", MODE_PRIVATE)
        MainActivity.themeIndex = editor.getInt("themeIndex", 0)
        setTheme(MainActivity.themesList[MainActivity.themeIndex])
        binding = ActivityPlayerBinding.inflate(layoutInflater)

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //hide notch and fill full screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        setContentView(binding.root)
        supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        //hide notch and fill full screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        if (intent.getParcelableExtra<Uri>("class") != null) {
            val videoUri = intent.getParcelableExtra<Uri>("class")
            myPlayer = MyPlayer(this);
            myPlayer!!.play(videoUri.toString());
//            myPlayer!!.setTitle(title);
//            myPlayer!!.setVideoResolution(wh);
//            myPlayer!!.setVideoName(name);
//            myPlayer!!.setVideoSize(size);
//            myPlayer!!.setVideoDate(date);

        }
    }
    override fun onPause() {
        super.onPause()
        if (myPlayer != null) {
            myPlayer!!.onPause()
        }
    }
    override fun onResume() {
        super.onResume()
        if (myPlayer != null) {
            myPlayer!!.onResume()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (myPlayer != null) {
            myPlayer!!.onPause()
        }
    }

    override fun onBackPressed() {
        if (myPlayer != null) {
            myPlayer!!.onPause()
            myPlayer!!.onBackPressed()

        }
        super.onBackPressed()
    }

    fun getSize(size: Long): String? {
        val kilo: Long = 1024
        val mega = kilo * kilo
        val giga = mega * kilo
        val tera = giga * kilo
        var s = ""
        val kb = size.toDouble() / kilo
        val mb = kb / kilo
        val gb = mb / kilo
        val tb = gb / kilo
        if (size < kilo) {
            s = "$size Bytes"
        } else if (size >= kilo && size < mega) {
            s = String.format("%.2f", kb) + " KB"
        } else if (size >= mega && size < giga) {
            s = String.format("%.2f", mb) + " MB"
        } else if (size >= giga && size < tera) {
            s = String.format("%.2f", gb) + " GB"
        } else if (size >= tera) {
            s = String.format("%.2f", tb) + " TB"
        }
        return s
    }

}
