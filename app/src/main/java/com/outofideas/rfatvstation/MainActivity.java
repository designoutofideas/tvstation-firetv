name: Build RFA TVStation APK

on:
  push:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: rfa-tvstation-apk

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v3
        with:
          gradle-version: '8.4'

      - name: Build unsigned APK
        run: gradle assembleRelease

      - name: Decode keystore
        run: |
          echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 --decode > keystore.jks

      - name: Sign APK
        uses: r0adkll/sign-android-release@v1
        id: sign_app
        with:
          releaseDirectory: rfa-tvstation-apk/app/build/outputs/apk/release
          signingKeyBase64: ${{ secrets.KEYSTORE_BASE64 }}
          alias: ${{ secrets.KEY_ALIAS }}
          keyStorePassword: ${{ secrets.KEYSTORE_PASSWORD }}
          keyPassword: ${{ secrets.KEY_PASSWORD }}

      - name: Upload APK artifact
        uses: actions/upload-artifact@v4
        with:
          name: RFA-TVStation-release
          path: ${{ steps.sign_app.outputs.signedReleaseFile }}
          retention-days: 30
