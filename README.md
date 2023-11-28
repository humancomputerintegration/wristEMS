# Maps app for Smartwatch EMS

This project is a smartwatch app for using Maps to travel. It sends a bluetooth signal to low energy bluetooth devices whenever a turn is coming up and as soon as the turn is finished.

## Installation

Make sure you follow the steps under the [Installation](https://docs.mapbox.com/android/beta/navigation/guides/install/) guide. Once you have your **public and secret access tokens ready**, do the following:

Go to res/values and there you should fin Mapbox_access_token.xml. Change this to be your public token. 

In the gradle.properties, change the mapbox token to be your secret acess token.

Run the project and enjoy!