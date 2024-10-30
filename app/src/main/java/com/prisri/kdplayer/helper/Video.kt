package com.prisri.kdplayer.helper
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.prisri.kdplayer.MainActivity
import java.io.File
import java.util.UUID

data class Video(
    val id: String,
    var title: String,
    val duration: Long = 0,
    val folderName: String,
    val size: String,
    val res: String,
    var path: String,
    var artUri: Uri
)

data class Folder(
    val id: String,
    val folderName: String,
    val folderSize: Int
)

@SuppressLint("InlinedApi", "Recycle", "Range")
fun getAllVideos(context: Context): ArrayList<Video> {
    val tempList = ArrayList<Video>()

    // Retrieve sorting preference
    val sortEditor = context.getSharedPreferences("Sorting", AppCompatActivity.MODE_PRIVATE)
    MainActivity.sortValue = sortEditor.getInt("sortValue", 0)

    // Clear existing folder list
    MainActivity.folderList.clear()

    // Query media store for videos
    val projection = arrayOf(
        MediaStore.Video.Media.TITLE,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Video.Media.DATA,
        MediaStore.Video.Media.RESOLUTION,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.BUCKET_ID
    )
    val cursor = context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        MainActivity.sortList[MainActivity.sortValue]
    )

    // Calculate video count for each folder
    val videoCountMap = mutableMapOf<String, Int>()
    if (cursor != null) {
        if (cursor.moveToFirst()) {
            do {
                val folderC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)) ?: "Internal Storage"
                videoCountMap[folderC] = (videoCountMap[folderC] ?: 0) + 1
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    // Create Folder objects with calculated video counts
    val folderList = videoCountMap.map { (folderName, videoCount) ->
        Folder(
            id = UUID.randomUUID().toString(),
            folderName = folderName,
            folderSize = videoCount
        )
    }

    // Update MainActivity.folderList with the new folder list
    MainActivity.folderList.addAll(folderList)

    // Iterate through cursor again to retrieve video information
    val cursor2 = context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        MainActivity.sortList[MainActivity.sortValue]
    )

    if (cursor2 != null) {
        if (cursor2.moveToFirst()) {
            do {
                // Retrieve video information
                val titleC = cursor2.getString(cursor2.getColumnIndex(MediaStore.Video.Media.TITLE)) ?: "Unknown"
                val idC = cursor2.getString(cursor2.getColumnIndex(MediaStore.Video.Media._ID)) ?: "Unknown"
                val folderC = cursor2.getString(cursor2.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)) ?: "Internal Storage"
                val sizeC = cursor2.getString(cursor2.getColumnIndex(MediaStore.Video.Media.SIZE)) ?: "0"
                val pathC = cursor2.getString(cursor2.getColumnIndex(MediaStore.Video.Media.DATA)) ?: "Unknown"
                val resolution = cursor2.getString(cursor2.getColumnIndex(MediaStore.Video.Media.RESOLUTION)) ?: "Unknown"
                val durationC = cursor2.getString(cursor2.getColumnIndex(MediaStore.Video.Media.DURATION))?.toLong() ?: 0L

                try {
                    val file = File(pathC)
                    val artUriC = Uri.fromFile(file)
                    val video = Video(
                        title = titleC,
                        id = idC,
                        folderName = folderC,
                        duration = durationC,
                        size = sizeC,
                        path = pathC,
                        res = resolution,
                        artUri = artUriC
                    )
                    if (file.exists()) tempList.add(video)
                } catch (e: Exception) {
                    // Handle exceptions here
                }
            } while (cursor2.moveToNext())
        }
        cursor2.close()
    }

    return tempList
}

fun checkForInternet(context: Context): Boolean {

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    } else {
        @Suppress("DEPRECATION") val networkInfo =
            connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        return networkInfo.isConnected
    }
}
