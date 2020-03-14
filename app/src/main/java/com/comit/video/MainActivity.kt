package com.comit.video

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.comit.video.anim.GLFrameAnimActivity
import com.comit.video.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFrameAnim.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        if (v == null) {
            return
        }

        when (v.id) {
            R.id.btn_frame_anim -> {
                val intent = Intent(this, GLFrameAnimActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
