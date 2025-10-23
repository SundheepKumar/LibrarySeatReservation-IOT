# IoT-Based Library Seat Monitoring App (Kotlin + Firebase)

NOTE: THIS PROJECT (HARDWARE + Mobile App Integrated) IS A PUBLISHED PATENT.

## Overview
This Android application is part of an IoT-based library seat monitoring system.  
It connects to the Firebase Realtime Database where ESP8266-based IoT nodes update seat occupancy information.  
The app reads the live seat data and displays whether each seat is available, reserved, or occupied.  
It also supports short 5-minute reservations which automatically expire if not occupied.

---

## Features
- Fetch seat status in real time from Firebase Realtime Database
- Display seat availability (Available / Occupied / Reserved)
- Automatically refresh seat states when data changes
- Handle 5-minute reservation timeout logic
- Lightweight Kotlin-based code with simple Firebase integration
- No authentication required

---

## Components Used
- Kotlin-based Android app
- Firebase Realtime Database
- ESP8266 NodeMCU devices with Ultrasonic sensors (for data input)
- Basic internet connection (Wi-Fi)

---

## Firebase Structure Example
"seats": {
"A1": {
"status": "available",
"reserved_until": ""
},
"A2": {
"status": "occupied",
"reserved_until": ""
},
"A3": {
"status": "reserved",
"reserved_until": "2025-10-23T14:30:00Z"
}
}

How to Run

1. Clone the repository:
git clone https://github.com/SundheepKumar/LibrarySeatReservation-IOT.git
2. Open the project in Android Studio.
3. Add your google-services.json file.
4. Check your Firebase Realtime Database URL in google-services.json.
5. Build and run the app on an emulator or Android device.

How the System Works:
Each ESP8266 device sends seat status to Firebase.
The Android app continuously listens to Firebase updates.
Changes appear instantly on the app without manual refresh.
Reservation timeouts can be managed either by IoT device or app logic.

Future Enhancements
Add user authentication for controlled booking
Push notifications for reservation reminders
Admin dashboard for seat usage statistics
Offline caching and better error handling

FIRMWARE CODE WILL BE UPLOADED SOON. (ESP8266)

