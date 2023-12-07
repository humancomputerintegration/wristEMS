# Maps app for Smartwatch EMS

This project is a smartwatch app for using Maps to travel. It sends a bluetooth signal to low energy bluetooth devices whenever a turn is coming up and as soon as the turn is finished.

## Installation

Make sure you follow the steps under the [Installation](https://docs.mapbox.com/android/beta/navigation/guides/install/) guide. Once you have your **public and secret access tokens ready**, do the following:

Go to res/values and there you should add a file called: mapbox_access_token.xml. Add your public token as a string. 
In mapbox_access_token.xml:

//"<?xml version="1.0" encoding="utf-8"?>
//<resources xmlns:tools="http://schemas.android.com/tools">
//    <string name="mapbox_access_token">CHANGE THIS TO THE PUBLIC MAPBOX TOKEN</string>
//</resources>"



In the gradle.properties, change the mapbox token to be your secret acess token.

Run the project and enjoy!
