# Maps app for Smartwatch EMS

This project is a smartphone app for using Maps to travel. It sends a bluetooth signal to low energy bluetooth devices whenever a turn is coming up and as soon as the turn is finished.

## Installation

Make sure you follow the steps under the [Installation](https://docs.mapbox.com/android/beta/navigation/guides/install/) guide. Once you have your **public and secret access tokens ready**, do the following:

Go to MapApp/app/src/main/res/values (app/res/values in Android view) and there you should add a file called: `mapbox_access_token.xml`. Add your public token as a string. 
In `mapbox_access_token.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools">
    <string name="mapbox_access_token">CHANGE THIS TO THE PUBLIC MAPBOX TOKEN</string>
</resources>
```

Make the text file `gradle.properties` under the Gradle User Home (`~/.gradle` for Unix(-like) or `C\Users\<username>\.gradle` for Win), in which add the following line and change the mapbox token to be your secret acess token.

```agsl
MAPBOX_DOWNLOADS_TOKEN = REPLACE THIS LEFT OPERAND WITH YOUR MAPBOX SECRET TOKEN
```

    Note: the secret token is shown once just only after you make a secret token. If you accidently reload the page before copying the secret token, you'll need to create another secret token.
Run the project and enjoy!

## Tips for building the project

If you clone the repository to a drive/partition different from the one in which SDKs are installed, such as `/mnt/g` (google drive), which could occur exeptions while the build. To avoid this, it is reccomended to clone and build this project under the same drive/partitions. 

When building, at least check the value of `complieSDK` in the `build.gradle` (Module level). Your target Android version should be between `minSdkVersion` and `compileSDK`
