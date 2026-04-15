# RSS Reader Android

Android RSS Reader app based on the backend OpenAPI.

## Features

- Configurable server base URL
- Username/password login
- Feishu quick login callback flow
- Feed list and article list
- AI summary history
- Material You style UI

## Requirements

- JDK 17
- Android SDK (platform 35)
- Android build-tools 35.x
- adb (optional, for install/run)

## Build

```bash
make debug
make release
```

Release APK (after `make release`) will be copied to project root.

## Useful Commands

```bash
make doctor
make install
make run
make logs
make test
```

## API

OpenAPI definition is in [`openapi.yaml`](./openapi.yaml).
