package com.vaultgallery.app

import android.net.Uri

data class PhotoItem(
    val id: Long,
    val uri: Uri,
    val isHidden: Boolean = false
)
