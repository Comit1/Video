package com.comit.video.anim

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import android.view.Surface
import com.comit.video.draw.Texture2dDraw
import com.comit.video.draw.TextureOesDraw
import com.comit.video.gles.GlUtil
import com.comit.video.glview.Renderer
import java.nio.ByteBuffer


/*
 * Created by Comit on 2020/3/14
 *
 * @description 文件描述
 */
class AnimRenderer(val context: Context) : Renderer {

    private var texture2dDraw: Texture2dDraw? = null

    private var textureOesDraw: TextureOesDraw? = null

    private var canvasWidth = 0
    private var canvasHeight = 0

    private var textureId = -1
    private var textureTarget = GLES20.GL_TEXTURE_2D
    private var offsetX = 0
    private var offsetY = 0
    private var bWidth = 0
    private var bHeight = 0

    private var blendEnable = false

    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null

    private var pboIds: IntArray? = null
    private var pboIndex = 0

    override fun onSurfaceCreated() {
        Log.d("Comit", "onSurfaceCreated")
        GLES20.glClearColor(1f, 1f, 1f, 1f)

        texture2dDraw = Texture2dDraw(context)
        textureOesDraw = TextureOesDraw(context)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        Log.d("Comit", "w=$width, h=$height")
        GLES20.glViewport(0, 0, width, height)

        canvasWidth = width
        canvasHeight = height
    }

    override fun onDrawFrame() {
        Log.d("Comit", "onDrawFrame")
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        if (textureId != -1) {
            GLES20.glViewport(offsetX, offsetY, bWidth, bHeight)

            val baseDraw = if (textureTarget == GLES20.GL_TEXTURE_2D)
                            texture2dDraw else textureOesDraw

            blendEnable(true)
            baseDraw?.draw(textureId)
            blendEnable(false)
        }
    }

    override fun onSurfaceDestroyed() {
        Log.d("Comit", "onSurfaceDestroyed")
        texture2dDraw?.release()
        texture2dDraw = null

        textureOesDraw?.release()
        textureOesDraw = null

        surface?.release()
        surface = null

        surfaceTexture?.release()
        surfaceTexture = null

        if (textureId != -1) {
            GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
            textureId = -1
        }

        releasePbo()
    }

    fun changeBitmap(bitmap: Bitmap) {
        bWidth = bitmap.width
        bHeight = bitmap.height
        offsetX = (canvasWidth - bWidth) / 2
        offsetY = (canvasHeight - bHeight) / 2

//        updateTexture(bitmap)
        updateTextureForSurface(bitmap)
//        updateTextureForPbo(bitmap)
    }

    private fun updateTexture(bitmap: Bitmap) {
        textureTarget = GLES20.GL_TEXTURE_2D
        if (textureId == -1) {
            textureId = GlUtil.createTexture(GLES20.GL_TEXTURE_2D)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLUtils.texImage2D(
                GLES20.GL_TEXTURE_2D,
                0, bitmap, 0
            )
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLUtils.texSubImage2D(
                GLES20.GL_TEXTURE_2D,
                0, 0, 0, bitmap
            )
        }
    }

    private fun updateTextureForSurface(bitmap: Bitmap) {
        textureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
        if (textureId == -1) {
            textureId = GlUtil.createTexture(textureTarget)
            surfaceTexture = SurfaceTexture(textureId).apply {
                setDefaultBufferSize(bitmap.width, bitmap.height)
                surface = Surface(this)
                setOnFrameAvailableListener {
                    Log.d("Comit", "onFrameAvailable")
                    it.updateTexImage()
                }
            }
        }

        surface?.let {
            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            val canvas = it.lockCanvas(null)
            canvas.drawBitmap(bitmap, null, rect, null)
            it.unlockCanvasAndPost(canvas)
        }
    }

    private fun updateTextureForPbo(bitmap: Bitmap) {
        textureTarget = GLES20.GL_TEXTURE_2D
        val pboSize = bitmap.width * bitmap.height * 4
        if (textureId == -1) {
            createPbo(pboSize)
            textureId = GlUtil.createTexture(GLES20.GL_TEXTURE_2D)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glTexImage2D(textureTarget, 0, GLES20.GL_RGBA,
                bitmap.width, bitmap.height, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, null)
        }

        val pboIds = this.pboIds!!

        GLES30.glBindBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER, pboIds[pboIndex])

        val byteBuffer: ByteBuffer = GLES30.glMapBufferRange(
            GLES30.GL_PIXEL_UNPACK_BUFFER,
            0, pboSize, GLES30.GL_MAP_WRITE_BIT
        ) as ByteBuffer

        bitmap.copyPixelsToBuffer(byteBuffer)

        GLES30.glUnmapBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER)

        val nextPboIndex = (pboIndex + 1) % 2
        GLES30.glBindBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER, pboIds[nextPboIndex])

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        GLES20.glTexSubImage2D(textureTarget, 0, 0, 0,
            bitmap.width, bitmap.height, GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE, null)

        GLES30.glBindBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER, 0)
        pboIndex = nextPboIndex
    }

    private fun createPbo(pboSize: Int) {

        if (pboIds != null) {
            return
        }

        val pboIds = IntArray(2)

        GLES30.glGenBuffers(2, pboIds, 0)

        GLES30.glBindBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER, pboIds[0])
        GLES30.glBufferData(GLES30.GL_PIXEL_UNPACK_BUFFER, pboSize,
            null, GLES30.GL_STATIC_COPY)

        GLES30.glBindBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER, pboIds[1])
        GLES30.glBufferData(GLES30.GL_PIXEL_UNPACK_BUFFER, pboSize,
            null, GLES30.GL_STATIC_COPY)

        GLES30.glBindBuffer(GLES30.GL_PIXEL_UNPACK_BUFFER, 0)

        this.pboIds = pboIds
    }

    private fun releasePbo() {
        pboIds?.let {
            GLES30.glDeleteBuffers(2, it, 0)
        }
        pboIds = null
    }

    private fun blendEnable(enable: Boolean) {
        if (enable == blendEnable) {
            return
        }
        blendEnable = enable
        if (enable) {
            GLES20.glDepthMask(false)
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendEquation(GLES20.GL_FUNC_ADD)
            GLES20.glBlendFuncSeparate(
                GLES20.GL_ONE,
                GLES20.GL_ONE_MINUS_SRC_ALPHA,
                GLES20.GL_ONE,
                GLES20.GL_ONE
            )
        } else {
            GLES20.glDisable(GLES20.GL_BLEND)
            GLES20.glDepthMask(true)
        }
    }
}