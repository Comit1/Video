package com.comit.video.anim

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import com.comit.video.R

/*
 * Created by Comit on 2020/3/14
 *
 * @description 文件描述
 */
class GLFrameAnimation(val context: Context) : Runnable {

    private val frameIdArray = intArrayOf(
        R.drawable.home4_adidas_img1, R.drawable.home4_adidas_img2,
        R.drawable.home4_adidas_img3, R.drawable.home4_adidas_img4,
        R.drawable.home4_adidas_img5, R.drawable.home4_adidas_img6,
        R.drawable.home4_adidas_img7, R.drawable.home4_adidas_img8,
        R.drawable.home4_adidas_img9, R.drawable.home4_adidas_img10,
        R.drawable.home4_adidas_img11, R.drawable.home4_adidas_img12,
        R.drawable.home4_adidas_img13, R.drawable.home4_adidas_img14
    )

    private val handler = Handler()

    private val interval = 25L

    private var start = false
    private var index = 0

    private var listener: Listener? = null

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun start() {
        if (!start) {
            index = 0
            onFrame(index)
            handler.postDelayed(this, interval)
        }
    }

    override fun run() {
        index = (index + 1) % frameIdArray.size
        var delay = interval
//        if (index >= frameIdArray.size) {
//            // frame end
//            delay = 1000
//            index = -1
//        } else {
//            onFrame(index)
//        }
        onFrame(index);

        handler.postDelayed(this, delay)
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
        start = false
    }

    private fun onFrame(index: Int) {
        val bitmap = BitmapFactory.decodeResource(context.resources, frameIdArray[index])
        listener?.onFrame(bitmap)
    }

    interface Listener {
        fun onFrame(bitmap: Bitmap)
    }
}