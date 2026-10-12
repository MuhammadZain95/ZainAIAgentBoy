# Zain AI Agent Boy — Purple Glass Edition

Starter Android application using Kotlin and the Gemini generateContent REST API.

## Features in this starter
- Purple Glass-inspired interface
- Gemini AI chat
- AI-assisted app project planning mode
- GitHub Actions debug APK build and release

## Build
GitHub Actions builds app/build/outputs/apk/debug/app-debug.apk.
The workflow uploads the `app-debug-apk` artifact and publishes a latest release.

## Important limitations
The builder currently plans projects; it does not run Termux commands or create
arbitrary projects on the device. Those features require a separate authenticated
Termux worker and a reviewed command/file protocol.

The Gemini API key is entered at runtime and is not embedded in source code.
For a public production app, route API calls through a secured backend rather
than distributing a reusable API key inside a mobile client.
