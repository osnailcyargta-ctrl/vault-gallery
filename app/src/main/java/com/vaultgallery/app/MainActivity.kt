package com.vaultgallery.app

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PhotoAdapter
    private lateinit var prefs: SharedPreferences
    private lateinit var audioManager: AudioManager
    private var allPhotos = mutableListOf<PhotoItem>()

    companion object {
        const val PREFS = "vault_prefs"
        const val KEY_HIDDEN = "hidden_ids"
        const val REQ_PERM = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        recyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (hasPermissions()) loadPhotos()
    }

    private fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkPermissions() {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else Manifest.permission.READ_EXTERNAL_STORAGE

        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, arrayOf(perm), REQ_PERM)
        } else {
            loadPhotos()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_PERM && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadPhotos()
        }
    }

    private fun isVolumeMax(): Boolean {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return cur >= max
    }

    private fun isBrightnessMin(): Boolean {
        val brightness = try {
            android.provider.Settings.System.getInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) { 128 }
        return brightness <= 10
    }

    private fun getHiddenIds(): MutableSet<String> {
        return prefs.getStringSet(KEY_HIDDEN, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
    }

    private fun saveHiddenIds(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_HIDDEN, ids).apply()
    }

    fun onPhotoClick(photo: PhotoItem) {
        val hiddenIds = getHiddenIds()

        if (hiddenIds.contains(photo.id.toString())) {
            // Photo is hidden - only show if volume max AND brightness min
            if (isVolumeMax() && isBrightnessMin()) {
                // Reveal it - unhide
                hiddenIds.remove(photo.id.toString())
                saveHiddenIds(hiddenIds)
                Toast.makeText(this, "Photo revealed!", Toast.LENGTH_SHORT).show()
                loadPhotos()
            }
            return
        }

        if (isVolumeMax()) {
            // Hide this photo
            hiddenIds.add(photo.id.toString())
            saveHiddenIds(hiddenIds)
            Toast.makeText(this, "Photo hidden!", Toast.LENGTH_SHORT).show()
            loadPhotos()
        } else {
            // Normal open
            val intent = Intent(this, PhotoViewActivity::class.java)
            intent.putExtra("photo_uri", photo.uri.toString())
            startActivity(intent)
        }
    }

    fun loadPhotos() {
        val hiddenIds = getHiddenIds()
        val showHidden = isVolumeMax() && isBrightnessMin()

        allPhotos.clear()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, sortOrder
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val isHidden = hiddenIds.contains(id.toString())
                if (!isHidden || showHidden) {
                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    allPhotos.add(PhotoItem(id, uri, isHidden))
                }
            }
        }

        adapter = PhotoAdapter(allPhotos, this)
        recyclerView.adapter = adapter
    }
}
