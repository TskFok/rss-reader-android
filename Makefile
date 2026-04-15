SHELL := /bin/bash

APP_ID ?= com.example.rssreader
MAIN_ACTIVITY ?= .app.MainActivity
GRADLEW := ./gradlew

ifeq ($(ANDROID_SERIAL),)
ADB := adb
else
ADB := adb -s $(ANDROID_SERIAL)
endif

.PHONY: help doctor debug release install uninstall run logs test lint clean

help:
	@echo "Android RSS Reader - Make targets"
	@echo ""
	@echo "  make doctor     # Check Java/Gradle/ADB/SDK environment"
	@echo "  make debug      # Build debug APK"
	@echo "  make release    # Build release APK"
	@echo "  make install    # Install debug build on connected device"
	@echo "  make uninstall  # Uninstall app from device"
	@echo "  make run        # Launch app on connected device"
	@echo "  make logs       # Tail app logcat"
	@echo "  make test       # Run unit tests"
	@echo "  make lint       # Run lint"
	@echo "  make clean      # Clean Gradle outputs"
	@echo ""
	@echo "Optional:"
	@echo "  ANDROID_SERIAL=<device_id> make install"

doctor:
	@echo "JAVA_HOME=$$JAVA_HOME"
	@java -version
	@$(GRADLEW) -v
	@$(ADB) version
	@echo "sdk.dir from local.properties:"
	@grep '^sdk.dir=' local.properties || true

debug:
	@$(GRADLEW) assembleDebug

release:
	@$(GRADLEW) assembleRelease
	@APK_PATH=$$(ls -t app/build/outputs/apk/release/app-release.apk 2>/dev/null | head -n 1); \
	if [ -z "$$APK_PATH" ]; then \
		APK_PATH=$$(ls -t app/build/outputs/apk/release/*.apk | grep -v 'unsigned' | head -n 1); \
	fi; \
	if [ -z "$$APK_PATH" ]; then \
		APK_PATH=$$(ls -t app/build/outputs/apk/release/*.apk | head -n 1); \
	fi; \
	if [ -z "$$APK_PATH" ]; then \
		echo "No release APK found."; \
		exit 1; \
	fi; \
	APK_NAME=$$(basename "$$APK_PATH"); \
	cp "$$APK_PATH" "./$$APK_NAME"; \
	echo "Copied $$APK_NAME to $$(pwd)"

install:
	@$(GRADLEW) installDebug

uninstall:
	@$(ADB) uninstall $(APP_ID) || true

run:
	@$(ADB) shell am start -n $(APP_ID)/$(MAIN_ACTIVITY)

logs:
	@$(ADB) logcat -v time | grep --line-buffered "$(APP_ID)\|AndroidRuntime\|FATAL EXCEPTION"

test:
	@$(GRADLEW) testDebugUnitTest

lint:
	@$(GRADLEW) lintDebug

clean:
	@$(GRADLEW) clean
