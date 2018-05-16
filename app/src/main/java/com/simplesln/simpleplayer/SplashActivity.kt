package com.simplesln.simpleplayer

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private val REQ_READ_STORAGE = 2;
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        if(askStorageReadPermission()){
            goToMainActivity()
        }
    }

    private fun goToMainActivity(){
        handler.postDelayed(Runnable {
            startActivity(Intent(applicationContext,MainActivity::class.java))
            finish()
        },200)
    }

    private fun askStorageReadPermission() : Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQ_READ_STORAGE);
            return false;
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQ_READ_STORAGE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goToMainActivity()
            }
        }
    }
}