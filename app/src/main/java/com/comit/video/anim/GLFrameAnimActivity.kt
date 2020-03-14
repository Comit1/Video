package com.comit.video.anim

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.comit.video.databinding.ActivityGlFrameAnimBinding
import com.comit.video.glview.GLSurfaceView

/*
 * Created by Comit on 2020/3/14
 *
 * @description 文件描述
 */
class GLFrameAnimActivity : AppCompatActivity() {

    private lateinit var glView: GLSurfaceView
    private val renderer = AnimRenderer(this)

    private val frameAnimation = GLFrameAnimation(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityGlFrameAnimBinding.inflate(layoutInflater)
        setContentView(binding.root)

        glView = binding.glView
        glView.setRenderer(renderer)
        glView.requestRender()

        frameAnimation.setListener(object : GLFrameAnimation.Listener {
            override fun onFrame(bitmap: Bitmap) {
                glView.queueSafeEvent {
                    renderer.changeBitmap(bitmap)
                    glView.requestRender()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        frameAnimation.start()
    }

    override fun onPause() {
        super.onPause()
        frameAnimation.stop()
    }
}