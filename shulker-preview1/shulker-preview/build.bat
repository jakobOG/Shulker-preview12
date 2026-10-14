@echo off
chcp 65001 >nul
title Shulker Preview - Build
echo ============================================
echo  Shulker Preview - Build (Minecraft 1.21.10)
echo ============================================
echo.
java -version >nul 2>&1
if errorlevel 1 (
  echo FEHLER: Java wurde nicht gefunden. Bitte Java 21 (JDK) installieren.
  pause
  exit /b 1
)
call gradlew.bat clean build
if errorlevel 1 (
  echo.
  echo BUILD FEHLGESCHLAGEN. Bitte die Fehlermeldung kopieren und an Claude schicken.
  pause
  exit /b 1
)
echo.
echo FERTIG! Die JAR liegt in: build\libs\
echo Nimm die Datei OHNE "-sources" im Namen und kopiere sie in deinen mods-Ordner.
echo.
pause
