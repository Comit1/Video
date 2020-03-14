package com.comit.video.draw

import android.content.Context
import com.comit.video.R

/*
 * Created by Comit on 2020/3/6
 */
class TextureOesDraw(context: Context) : BaseDraw(context) {

    init {
        createProgram(R.raw.vertex_shader, R.raw.fragment_shader_oes)
    }
}