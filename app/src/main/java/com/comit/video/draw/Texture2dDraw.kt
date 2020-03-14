package com.comit.video.draw

import android.content.Context
import com.comit.video.R

/*
 * Created by Comit on 2020/3/6
 */
class Texture2dDraw(context: Context) : BaseDraw(context) {

    init {
        createProgram(R.raw.vertex_shader, R.raw.fragment_shader)
    }
}