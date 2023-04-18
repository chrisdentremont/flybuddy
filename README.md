# Overview
FlyBuddy is a mobile application that allows the user to search for flights and track them.

Users can search for flights using different criteria (airline name, flight status, flight number). Users can view search results and track flights that are shown as expandable cards on a home tab.

*An account and friends system is not currently implemented but is planned for.*

# Build Instructions
- Download the latest version of [Android Studio](https://developer.android.com/studio?gclid=CjwKCAjw__ihBhADEiwAXEazJjScC5F4-7C_XONpGgRhqJZy40a7jAwSwfU8WdA_QWSun-VyPjltoBoC3IAQAvD_BwE&gclsrc=aw.ds).
- Add a new project and select 'Project from Source Control'. Paste the repo link in the URL field and clone the repo to Android Studio.
- At the top-right corner, click the button labeled 'Sync Project with Gradle Files'. This installs any dependencies required for the application to run.
- Click the green hammer labeled 'Make Project' at the top to build the project and ensure there are no build errors.
- Add a virtual device in the Device Manager. Install the Android SDK version 33. This is the version that the application was developed on; other versions can be used but may not be functional.
- Click the green play button labeled 'Run'. A device emulator should appear and run the application.
