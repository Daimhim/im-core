package org.daimhim.im.core.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.daimhim.im.core.android.databinding.ActivityMainBinding

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityMainBinding.inflate(layoutInflater).root)
    }
}