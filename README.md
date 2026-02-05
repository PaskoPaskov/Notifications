# Notifications

Desktop Java application for sending automated notifications.

## Description
Notifications is a Java desktop application that runs in the system tray and
periodically sends messages based on user configuration.

The project is created for learning purposes and is actively evolving.

## Features
- System tray integration
- Scheduled background tasks
- User configuration stored in properties file
- Reconfigure option without reinstalling the app
- Windows EXE build via Maven + jpackage

## Technologies
- Java 21
- Maven
- ScheduledExecutorService
- System Tray (AWT)
- jpackage (Windows EXE)

## Build
```bash
mvn clean package
