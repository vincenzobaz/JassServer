# JassServer

## Server component for the android application developed for the Software Engineering class at EPFL.
The server main purpose is to send notification messages to Firebase Cloud Messaging (FCM) servers that will forward them to devices.
As FCM needs the device registration ID of the phone, the app will register the current user with out server, that will store the key,value pair (userId, registrationId) in a redis instance.
The server also monitors the FirebaseDatabase associated to our application to send additional notifications.

## Deployment

`docker-compose build` will prepare the docker images that can be run with `docker-compose up` adding the flag `-d` to run as a daemon.

The `prod.env` file must contain:

 - `FCM_KEY` : Firebase Cloud Messaging key
 - `FCM_URL`: The URL of the Firebase Cloud Messaging servers. At the time of project creation it is `https://fcm.googleapis.com/fcm/send`
 - `REDIS_HOST`: The redis hostname, which based on our docker compose services, is simply `redis`
 - `DELETE_EXPIRED` : `true` if you want to schedule the destruction of matches at their expiration date, else `false`
 - `FIREBASE_DB` : The url of the Firabse Realtime Database.
 - `FIREBASE_KEY` : Path to the json key file necessary to authenticate with Firebase and have access to the Database
```

