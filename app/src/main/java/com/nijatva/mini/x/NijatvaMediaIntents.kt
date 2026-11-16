// NijatvaMediaIntents.kt
//
// Drop-in helper for the Nijatva app family. Copy this file into any of the apps
// (Camera, Gallery, Photo Editor, Video Player, Music Player, File Manager, Browser)
// and change the `package` line to that app's package.
//
// Design goal: every call is a SOFT dependency.
//   1. try the matching Nijatva app (explicit package),
//   2. else let the system resolve it (chooser),
//   3. else show a short message.
// Nothing here throws, so a missing sibling app can never crash the caller, and no
// feature is blocked — the user just gets the system chooser instead.
//
// Family packages:
//   Photos        com.nijatva.photos
//   Gallery       com.nijatva.gallery
//   Camera        com.nijatva.camera
//   Photo Editor  com.nijatva.photo.editor
//   Video Player  com.nijatva.videoplayer
//   Music Player  com.nijatva.music.player
//   File Manager  com.nijatva.file
//   Browser       in.nijatva.browser
//
// Also add to each app's AndroidManifest.xml (needed on Android 11+ to see/launch them):
//
//   <queries>
//       <package android:name="com.nijatva.photos" />
//       <package android:name="com.nijatva.gallery" />
//       <package android:name="com.nijatva.camera" />
//       <package android:name="com.nijatva.photo.editor" />
//       <package android:name="com.nijatva.videoplayer" />
//       <package android:name="com.nijatva.music.player" />
//       <package android:name="com.nijatva.file" />
//       <package android:name="in.nijatva.browser" />
//   </queries>

package com.nijatva.mini.x   // <-- change to your app's package

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object NijatvaMediaIntents {

    const val PHOTOS = "com.nijatva.photos"
    const val GALLERY = "com.nijatva.gallery"
    const val CAMERA = "com.nijatva.camera"
    const val PHOTO_EDITOR = "com.nijatva.photo.editor"
    const val VIDEO_PLAYER = "com.nijatva.videoplayer"
    const val MUSIC_PLAYER = "com.nijatva.music.player"
    const val FILE_MANAGER = "com.nijatva.file"
    const val BROWSER = "in.nijatva.browser"

    fun isInstalled(context: Context, pkg: String): Boolean = try {
        context.packageManager.getLaunchIntentForPackage(pkg) != null
    } catch (e: Exception) {
        false
    }

    private fun startOn(context: Context, intent: Intent, pkg: String): Boolean = try {
        context.startActivity(Intent(intent).setPackage(pkg))
        true
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: Exception) {
        false
    }

    private fun startAny(context: Context, intent: Intent, title: String): Boolean = try {
        context.startActivity(Intent.createChooser(intent, title))
        true
    } catch (e: Exception) {
        false
    }

    private fun viewOf(uri: String, mime: String) = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(Uri.parse(uri), mime)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private fun nothingFound(context: Context, what: String): Boolean {
        Toast.makeText(context, "No app found to $what", Toast.LENGTH_SHORT).show()
        return false
    }

    /** Open an image/video in Nijatva Gallery, else any gallery, else any viewer. */
    fun openImage(context: Context, uri: String, mime: String? = "image/*"): Boolean {
        val intent = viewOf(uri, mime ?: "image/*")
        if (startOn(context, intent, GALLERY)) return true
        if (startOn(context, intent, PHOTOS)) return true
        return startAny(context, intent, "Open in") || nothingFound(context, "open this item")
    }

    /** ACTION_EDIT in Nijatva Photo Editor, else any editor. */
    fun editImage(context: Context, uri: String, mime: String? = "image/*"): Boolean {
        val intent = Intent(Intent.ACTION_EDIT).apply {
            setDataAndType(Uri.parse(uri), mime ?: "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        if (startOn(context, intent, PHOTO_EDITOR)) return true
        return startAny(context, intent, "Edit with") || nothingFound(context, "edit this image")
    }

    /** Play a video in Nijatva Video Player, else any player. */
    fun playVideo(context: Context, uri: String, mime: String? = "video/*"): Boolean {
        val intent = viewOf(uri, mime ?: "video/*")
        if (startOn(context, intent, VIDEO_PLAYER)) return true
        return startAny(context, intent, "Play with") || nothingFound(context, "play this video")
    }

    /** Play audio in Nijatva Music Player, else any player. */
    fun playAudio(context: Context, uri: String, mime: String? = "audio/*"): Boolean {
        val intent = viewOf(uri, mime ?: "audio/*")
        if (startOn(context, intent, MUSIC_PLAYER)) return true
        return startAny(context, intent, "Play with") || nothingFound(context, "play this audio")
    }

    /** Open Nijatva Camera (falls back to the system still-image camera). */
    fun openCamera(context: Context): Boolean {
        context.packageManager.getLaunchIntentForPackage(CAMERA)?.let {
            if (startOn(context, it, CAMERA)) return true
        }
        return try {
            context.startActivity(Intent("android.media.action.STILL_IMAGE_CAMERA"))
            true
        } catch (e: Exception) {
            nothingFound(context, "open the camera")
        }
    }

    /** Open Nijatva File Manager. */
    fun openFileManager(context: Context): Boolean {
        context.packageManager.getLaunchIntentForPackage(FILE_MANAGER)?.let {
            if (startOn(context, it, FILE_MANAGER)) return true
        }
        return nothingFound(context, "open the file manager")
    }

    /** Open a URL in Nijatva Browser, else any browser. */
    fun openInBrowser(context: Context, url: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (startOn(context, intent, BROWSER)) return true
        return startAny(context, intent, "Open with") || nothingFound(context, "open this link")
    }

    /** Share media through the system share sheet (never file:// — always content://). */
    fun shareMedia(context: Context, uri: String, mime: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, Uri.parse(uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startAny(context, intent, "Share")
    }
}
