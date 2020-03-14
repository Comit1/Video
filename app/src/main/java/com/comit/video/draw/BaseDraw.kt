package com.comit.video.draw

import android.content.Context
import android.opengl.GLES20
import androidx.annotation.NonNull
import androidx.annotation.RawRes
import com.comit.video.gles.Drawable2d
import com.comit.video.gles.GlUtil
import java.lang.ref.WeakReference

/*
 * Created by Comit on 2020/3/6
 */
abstract class BaseDraw(@NonNull context: Context) {

    private val context: WeakReference<Context> = WeakReference(context)

    private var program = 0
    private var positionLoc = -1
    private var textureCoordLoc = -1
    private var mvpMatrixLoc = -1
    private var texMatrixLoc = -1
    private var textureLoc = -1


    companion object {

        @JvmStatic
        val drawable2d = Drawable2d()
    }

    protected fun createProgram(@RawRes vertexShaderId: Int, @RawRes fragmentShaderId: Int) {

        val context = context.get() ?: return

        program = GlUtil.createProgram(context, vertexShaderId, fragmentShaderId)

        positionLoc = GLES20.glGetAttribLocation(program, "aPosition")
        textureCoordLoc = GLES20.glGetAttribLocation(program, "aTextureCoord")
        mvpMatrixLoc = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        texMatrixLoc = GLES20.glGetUniformLocation(program, "uTexMatrix")

        onGetUniformLocation(program)
    }

    fun draw(textureId: Int, mvpMatrix: FloatArray? = null,
             texMatrix: FloatArray? = null) {

        if (program == 0) {
            return
        }

        onUseProgram()
        onSetUniformData()
        onBindTexture(textureId)
        onDraw(mvpMatrix, texMatrix)
    }

    protected open fun onUseProgram() {
        GLES20.glUseProgram(program)
    }

    /**
     * 绘制
     */
    protected open fun onDraw(mvpMatrix: FloatArray?, texMatrix: FloatArray?,
                        drawable2d: Drawable2d = BaseDraw.drawable2d) {

        if (mvpMatrixLoc != -1) {
            val matrix = mvpMatrix ?: GlUtil.IDENTITY_MATRIX
            GLES20.glUniformMatrix4fv(mvpMatrixLoc, 1, false, matrix, 0)
        }
        if (texMatrixLoc != -1) {
            val matrix = texMatrix ?: GlUtil.IDENTITY_MATRIX
            GLES20.glUniformMatrix4fv(texMatrixLoc, 1, false, matrix, 0)
        }
        if (positionLoc != -1) {
            GLES20.glEnableVertexAttribArray(positionLoc)
            GLES20.glVertexAttribPointer(
                positionLoc, drawable2d.coordsPerVertex,
                GLES20.GL_FLOAT, false,
                drawable2d.vertexStride,
                drawable2d.vertexArray
            )
        }
        if (textureCoordLoc != -1) {
            GLES20.glEnableVertexAttribArray(textureCoordLoc)
            GLES20.glVertexAttribPointer(
                textureCoordLoc, 2,
                GLES20.GL_FLOAT, false,
                drawable2d.texCoordStride,
                drawable2d.texCoordArray
            )
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0,
            drawable2d.vertexCount)

        if (positionLoc != -1) {
            GLES20.glDisableVertexAttribArray(positionLoc)
        }
        if (textureCoordLoc != -1) {
            GLES20.glDisableVertexAttribArray(textureCoordLoc)
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glUseProgram(0)
    }

    /**
     * 获取Uniform变量位置
     */
    protected open fun onGetUniformLocation(program: Int) {
        textureLoc = GLES20.glGetUniformLocation(program, "sTexture")
    }

    /**
     * 设置Uniform变量数据
     */
    protected open fun onSetUniformData() {}

    /**
     * 绑定纹理
     */
    protected open fun onBindTexture(textureId: Int) {
        if (textureLoc != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glUniform1i(textureLoc, 0)
        }
    }

    open fun release() {
        if (program != 0) {
            GLES20.glDeleteProgram(program)
            program = 0
        }
    }

}