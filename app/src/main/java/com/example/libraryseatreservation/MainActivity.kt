package com.example.libraryseatreservation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val firstFloorBtn = findViewById<Button>(R.id.firstFloorBtn)
        val secondFloorBtn = findViewById<Button>(R.id.secondFloorBtn)
        val thirdFloorBtn = findViewById<Button>(R.id.thirdFloorBtn)

        firstFloorBtn.setOnClickListener { openFloorActivity("FirstFloor") }
        secondFloorBtn.setOnClickListener { openFloorActivity("SecondFloor") }
        thirdFloorBtn.setOnClickListener { openFloorActivity("ThirdFloor") }

        // 🔹 Get the currently logged-in user from Firebase Authentication
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    FirebaseDatabase.getInstance().getReference("Users")
                        .child(userId)
                        .child("token")
                        .setValue(token)
                }
            }
        }
    }

    private fun openFloorActivity(floorName: String) {
        Log.d("MainActivity", "First floor button clicked")

        val intent = Intent(this, FloorActivity::class.java)
        intent.putExtra("floor", floorName)

        startActivity(intent)
    }
}
