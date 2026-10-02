@echo off
echo ==========================================
echo Workflow Executor - Local Startup
echo ==========================================
echo.
echo Make sure Docker Desktop is running.
echo.
echo Starting PostgreSQL...
docker compose up -d postgres
if errorlevel 1 (
  echo.
  echo Failed to start PostgreSQL. Make sure Docker Desktop is running.
  pause
  exit /b 1
)
echo.
echo PostgreSQL is running.
echo.
echo Starting Spring Boot application...
call gradlew.bat bootRun
pause
