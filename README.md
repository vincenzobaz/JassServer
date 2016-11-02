# JassServer

## Server component for the android application developed for the Software Engineering class at EPFL.
The server main purpose is to send notification messages to Firebase Cloud Messaging (FCM) servers that will forward them to devices.
As FCM needs the device registration ID of the phone, the app will register the current user with out server, that will store the key,value pair (userId, registrationId) in a redis instance.
The server also monitors the FirebaseDatabase associated to our application to send additional notifications.
