@echo off
echo ==========================================
echo Workflow Executor - Test Suite
echo ==========================================
echo.
call gradlew.bat test
if errorlevel 1 (
  echo.
  echo Tests FAILED. Review the output above.
  pause
  exit /b 1
)
echo.
echo Tests PASSED.
pause
