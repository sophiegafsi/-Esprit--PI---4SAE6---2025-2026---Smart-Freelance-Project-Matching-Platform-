@echo off
setlocal
set ROOT=%~dp0

echo ==========================================
echo   Starting Smart Freelance Microservices
echo ==========================================
echo.

start "Eureka Server (8761)" cmd /k "cd /d ""%ROOT%eureka-server"" && call mvnw.cmd -Dmaven.test.skip=true spring-boot:run"
timeout /t 8 >nul

start "API Gateway (8088)" cmd /k "cd /d ""%ROOT%api-gateway"" && call mvnw.cmd -Dmaven.test.skip=true spring-boot:run"
timeout /t 8 >nul

start "Evaluation Service (8085)" cmd /k "cd /d ""%ROOT%evaluation-service\evaluation-service"" && set GOOGLE_GENAI_API_KEY=local-test-key && call mvnw.cmd -Dmaven.test.skip=true spring-boot:run"
timeout /t 5 >nul

start "Recompense Service (8094)" cmd /k "cd /d ""%ROOT%recompense\recompense"" && call mvnw.cmd -Dmaven.test.skip=true spring-boot:run"
timeout /t 5 >nul

start "Angular Frontend (4200)" cmd /k "cd /d ""%ROOT%frontend"" && call npm run dev"

echo.
echo All run windows have been launched.
echo Eureka Dashboard: http://localhost:8761
echo API Gateway:      http://localhost:8088
echo Angular Front:    http://localhost:4200
echo.

endlocal
