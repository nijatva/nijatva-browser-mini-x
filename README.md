# Mini X

A privacy-first Android browser built on the system WebView — the same engine and the
same features as Nijatva, under a different name and package.

**Containers** · **Tracker and ad blocking** · **Tab groups** · **Signed extension store** ·
**Reader mode** · **Native text-to-speech** · **HLS downloads**

- Package: `com.nijatva.mini.x`
- No Google Play Services, no accounts, no ads, no analytics
- Extensions come from the same signed catalogue as Nijatva

## Build

```bash
./gradlew assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/`. GitHub Actions builds one on every push
to `main` and keeps it as a workflow artifact.
