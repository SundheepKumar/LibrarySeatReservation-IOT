package com.example.libraryseatreservation

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class FloorActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var gridLayout: GridLayout
    private lateinit var bookSeatButton: Button
    private lateinit var timerTextView: TextView
    private var countDownTimer: CountDownTimer? = null
    private val userId = "user123" // Replace with actual authenticated user ID
    private var selectedSeatId: String? = null
    private val reservationDuration = 30 * 1000L // 30 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_floor)

        val floorName = intent.getStringExtra("floor") ?: "FirstFloor"
        database = FirebaseDatabase.getInstance().getReference("Seats").child(floorName)
        gridLayout = findViewById(R.id.gridLayout)
        bookSeatButton = findViewById(R.id.bookSeatButton)
        timerTextView = findViewById(R.id.timerTextView)

        loadSeats()
        listenForSeatUpdates()

        bookSeatButton.setOnClickListener {
            selectedSeatId?.let {
                reserveSeat(it)
                bookSeatButton.visibility = View.GONE
            }
        }
    }

    private fun loadSeats() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                gridLayout.removeAllViews()
                for (seat in snapshot.children) {
                    val seatId = seat.key ?: continue
                    val status = seat.child("status").value.toString()
                    val reservedBy = seat.child("reservedBy").value?.toString()

                    val seatView = ImageView(this@FloorActivity)
                    seatView.setImageResource(
                        when (status) {
                            "available" -> R.drawable.greenseat
                            "reserved" -> R.drawable.orangeseat
                            "occupied" -> R.drawable.readseat
                            else -> R.drawable.greyseat
                        }
                    )

                    seatView.setOnClickListener {
                        when {
                            status == "available" -> {
                                selectedSeatId = seatId
                                bookSeatButton.visibility = View.VISIBLE
                            }
                            status == "reserved" && reservedBy == userId -> {
                                confirmUnreserveSeat(seatId)
                            }
                        }
                    }

                    val params = GridLayout.LayoutParams()
                    params.width = 150
                    params.height = 150
                    params.setMargins(16, 16, 16, 16)
                    seatView.layoutParams = params
                    gridLayout.addView(seatView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@FloorActivity, "Failed to load seats", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun listenForSeatUpdates() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val seatId = snapshot.key ?: return
                val status = snapshot.child("status").value.toString()
                val reservedBy = snapshot.child("reservedBy").value?.toString()
                val occupied = snapshot.child("occupied").value as? Boolean ?: false

                if (status == "reserved" && occupied) {
                    countDownTimer?.cancel() // Stop timer when occupied
                    if (reservedBy == userId) {
                        showOccupancyAlert(seatId)
                    } else {
                        triggerBuzzer()
                    }
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showOccupancyAlert(seatId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seat Occupied")
        builder.setMessage("Is it you sitting in the reserved seat?")
        builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
            database.child(seatId).child("status").setValue("occupied")
        }
        builder.setNegativeButton("No") { _: DialogInterface, _: Int ->
            triggerBuzzer()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun triggerBuzzer() {
        val buzzerRef = FirebaseDatabase.getInstance().getReference("Buzzer")
        buzzerRef.setValue("on") // NodeMCU will read this value
    }

    private fun reserveSeat(seatId: String) {
        val reservationTime = System.currentTimeMillis() + reservationDuration
        database.child(seatId).setValue(
            mapOf(
                "status" to "reserved",
                "reservedBy" to userId,
                "reservedUntil" to reservationTime,
                "occupied" to false
            )
        )
        startTimer(seatId, reservationDuration)
        Toast.makeText(this, "Seat reserved!", Toast.LENGTH_SHORT).show()
    }

    private fun startTimer(seatId: String, timeMillis: Long) {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(timeMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                timerTextView.text = "Time: ${secondsLeft}s"
            }

            override fun onFinish() {
                database.child(seatId).get().addOnSuccessListener { snapshot ->
                    val occupied = snapshot.child("occupied").value as? Boolean ?: false
                    if (!occupied) {
                        database.child(seatId).setValue(
                            mapOf(
                                "status" to "available",
                                "reservedBy" to null,
                                "reservedUntil" to null,
                                "occupied" to false
                            )
                        )
                        timerTextView.text = "Time: 00:00"
                    }
                }
            }
        }
        countDownTimer?.start()
    }

    private fun confirmUnreserveSeat(seatId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Unreserve Seat")
        builder.setMessage("Are you sure you want to unreserve this seat?")
        builder.setPositiveButton("Yes") { _, _ ->
            unreserveSeat(seatId)
        }
        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun unreserveSeat(seatId: String) {
        countDownTimer?.cancel() // Stop the timer when unreserving

        database.child(seatId).setValue(
            mapOf(
                "status" to "available",
                "reservedBy" to null,
                "reservedUntil" to null,
                "occupied" to false
            )
        )
        timerTextView.text = "Time: 00:00"
        Toast.makeText(this, "Seat unreserved!", Toast.LENGTH_SHORT).show()
    }
}
