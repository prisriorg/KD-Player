package com.prisri.kdplayer

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.prisri.kdplayer.databinding.ActivityMainBinding
import com.prisri.kdplayer.fragments.FoldersFragment
import com.prisri.kdplayer.fragments.MoreFragment
import com.prisri.kdplayer.fragments.VideosFragment
import com.prisri.kdplayer.helper.Folder
import com.prisri.kdplayer.helper.Video
import com.prisri.kdplayer.helper.getAllVideos


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var runnable: Runnable? = null
    private lateinit var currentFragment: Fragment
    companion object {
        lateinit var videoList: ArrayList<Video>
        lateinit var folderList: ArrayList<Folder>
        lateinit var searchList: ArrayList<Video>
        var search: Boolean = false
        var themeIndex: Int = 0
        var sortValue: Int = 0
        val themesList = arrayOf(
            R.style.coolPinkNav, R.style.coolBlueNav, R.style.coolPurpleNav, R.style.coolGreenNav,
            R.style.coolRedNav, R.style.coolBlackNav
        )
        var dataChanged: Boolean = false
        var adapterChanged: Boolean = false
        val sortList = arrayOf(
            MediaStore.Video.Media.DATE_ADDED + " DESC",
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.TITLE + " DESC",
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.SIZE + " DESC"
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val editor = getSharedPreferences("Themes", MODE_PRIVATE)
        themeIndex = editor.getInt("themeIndex", 0)

        setTheme(themesList[themeIndex])
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayHomeAsUpEnabled(false)
            setIcon(R.drawable.logo_banner)
            title = " "
        }
        if (checkMediaAccessPermission()) {
            folderList = ArrayList()
            videoList = getAllVideos(this)
            setFragment(VideosFragment())
        }else {
            folderList = ArrayList()
            videoList = ArrayList()
        }
        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.allVideo -> setFragment(VideosFragment())
                R.id.allFolder -> setFragment(FoldersFragment())
                R.id.more -> setFragment(MoreFragment())
            }
            return@setOnItemSelectedListener true
        }
    }
    fun checkMediaAccessPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            (ContextCompat.checkSelfPermission(
                this,
                permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this,
                        permission.READ_MEDIA_VIDEO
                    ) == PackageManager.PERMISSION_GRANTED)
        ) {
            // Full access on Android 13 (API level 33) or higher
            true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            ContextCompat.checkSelfPermission(
                this,
                permission.READ_MEDIA_VISUAL_USER_SELECTED
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Partial access on Android 14 (API level 34) or higher
            true
        } else if (ContextCompat.checkSelfPermission(
                this,
                permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Full access up to Android 12 (API level 32)
            true
        } else {
            // Access denied
            false
        }
    }
    private fun setFragment(fragment: Fragment){
        currentFragment = fragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.layout_content, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val gradientList = arrayOf(R.drawable.pink_gradient, R.drawable.blue_gradient, R.drawable.purple_gradient, R.drawable.green_gradient
            , R.drawable.red_gradient, R.drawable.black_gradient)

//        findViewById<LinearLayout>(R.id.gradientLayout).setBackgroundResource(gradientList[themeIndex])

        return super.onOptionsItemSelected(item)
    }

    private fun saveTheme(index: Int){
        val editor = getSharedPreferences("Themes", MODE_PRIVATE).edit()
        editor.putInt("themeIndex", index)
        editor.apply()

        //for restarting app
        finish()
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        runnable = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        (currentFragment as VideosFragment).adapter.onResult(requestCode, resultCode)
    }
    fun requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    permission.READ_EXTERNAL_STORAGE,
                    permission.READ_MEDIA_IMAGES,
                    permission.READ_MEDIA_VIDEO,
                    permission.READ_MEDIA_VISUAL_USER_SELECTED
                ),
                100
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    permission.READ_EXTERNAL_STORAGE,
                    permission.READ_MEDIA_IMAGES,
                    permission.READ_MEDIA_VIDEO
                ),
                100
            )
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(permission.READ_EXTERNAL_STORAGE),
                100
            )
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                folderList = ArrayList()
                videoList = getAllVideos(this)
                setFragment(VideosFragment())
            }else if (!checkMediaAccessPermission()) {
                Snackbar.make(binding.root, "Storage Permission Needed!!", 5000)
                    .setAction("OK") {
                        requestPermissionsIfNeeded();
                    }
                    .show()
            }
        }
    }

}