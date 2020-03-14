package com.comit.video

import android.opengl.EGLSurface
import com.comit.video.gles.EglCore

/*
 * Created by Comit on 2020/3/6
 *
 * @description 视频处理流程
 */
class VideoHandler {

    private var eglCore: EglCore? = null
    private var eglSurface: EGLSurface? = null

    private fun start() {
        if (eglCore != null) {
            stop()
        }

        eglCore = EglCore(null, EglCore.FLAG_RECORDABLE).apply {
            eglSurface = createOffscreenSurface(1, 1)
            makeCurrent(eglSurface)
        }

    }

    private fun stop() {
        eglCore?.let {
            it.makeNothingCurrent()
            it.releaseSurface(eglSurface)
            it.release()
        }
        eglSurface = null
        eglCore = null
    }

}