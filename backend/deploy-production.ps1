#!/usr/bin/env powershell
<#
.SYNOPSIS
    ATM Monitoring System - Production Deployment Script for Windows Server
.DESCRIPTION
    This script encrypts sensitive data and deploys the ATM Monitoring application to production
.NOTES
    Author: ATM Monitoring Team
    Date: 2025-07-21
#>

param(
    [string]$JasyptPassword = "AtmSecure2024!Deploy",
    [string]$ServerPort = "9000",
    [switch]$SkipBuild = $false,
    [switch]$Help
)

if ($Help) {
    Write-Host "Usage: .\deploy-production.ps1 [-JasyptPassword <password>] [-ServerPort <port>] [-SkipBuild] [-Help]"
    Write-Host ""
    Write-Host "Parameters:"
    Write-Host "  -JasyptPassword   Encryption password for Jasypt (default: AtmSecure2024!Deploy)"
    Write-Host "  -ServerPort       Port for the application (default: 9000)"
    Write-Host "  -SkipBuild        Skip Maven build process"
    Write-Host "  -Help             Show this help message"
    exit 0
}

# Colors for output
$Red = "Red"
$Green = "Green"
$Yellow = "Yellow"
$Blue = "Cyan"

function Write-Status {
    param([string]$Message, [string]$Color = "White")
    Write-Host "[$((Get-Date).ToString('HH:mm:ss'))] $Message" -ForegroundColor $Color
}

function Write-Error-Exit {
    param([string]$Message)
    Write-Status "ERROR: $Message" $Red
    exit 1
}

function Test-Prerequisites {
    Write-Status "Checking prerequisites..." $Blue
    
    # Check Java
    try {
        $javaVersion = java -version 2>&1 | Select-String "version"
        Write-Status "Java found: $javaVersion" $Green
    } catch {
        Write-Error-Exit "Java not found. Please install Java 17 or higher."
    }
    
    # Check Maven
    try {
        $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven"
        Write-Status "Maven found: $mavenVersion" $Green
    } catch {
        Write-Error-Exit "Maven not found. Please install Apache Maven."
    }
    
    Write-Status "Prerequisites check completed successfully!" $Green
}

function Encrypt-Password {
    param([string]$Password, [string]$EncryptionKey)
    
    Write-Status "Encrypting password..." $Yellow
    
    $result = mvn exec:java -Dexec.mainClass="com.zemenbank.atmMonitoring.utils.EncryptionUtil" -Dexec.args="`"$EncryptionKey`" `"$Password`"" -q 2>&1
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Exit "Failed to encrypt password. Maven execution failed."
    }
    
    # Extract encrypted value from output
    $encryptedLine = $result | Select-String "Encrypted value:"
    if ($encryptedLine) {
        $encrypted = ($encryptedLine -split "Encrypted value: ")[1]
        return $encrypted.Trim()
    } else {
        Write-Error-Exit "Could not extract encrypted value from Maven output."
    }
}

function Update-ProductionProperties {
    param([string]$JasyptKey)
    
    Write-Status "Updating production properties with encrypted values..." $Blue
    
    # Define the passwords to encrypt
    $passwords = @{
        "local.datasource.password" = "ATMNTRING_PWD"
        "fcubs.datasource.password" = "Pa5s_4AtM1nBW168D3"
        "spring.mail.password" = "@Overkill2026"
        "mso.password" = "c0d15b291761182489a0c9d7d490ab4b"
        "application.security.jwt.secret-key" = "28472B4B6250655368566B5970337336763979244226452948404D635166546A"
    }
    
    $propsFile = "src\main\resources\application-prod.properties"
    $content = Get-Content $propsFile -Raw
    
    foreach ($prop in $passwords.GetEnumerator()) {
        Write-Status "Encrypting $($prop.Key)..." $Yellow
        $encrypted = Encrypt-Password $prop.Value $JasyptKey
        $encValue = "ENC($encrypted)"
        
        # Replace the property value
        $pattern = "^$($prop.Key)=.*$"
        $replacement = "$($prop.Key)=$encValue"
        $content = $content -replace $pattern, $replacement
    }
    
    # Write back to file
    $content | Out-File -FilePath $propsFile -Encoding UTF8 -NoNewline
    Write-Status "Production properties updated successfully!" $Green
}

function Build-Application {
    if ($SkipBuild) {
        Write-Status "Skipping build process as requested..." $Yellow
        return
    }
    
    Write-Status "Building application..." $Blue
    
    # Clean and build
    mvn clean package -DskipTests -Pprod
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Exit "Maven build failed!"
    }
    
    Write-Status "Application built successfully!" $Green
}

function Deploy-Application {
    param([string]$JasyptKey, [string]$Port)
    
    Write-Status "Deploying application..." $Blue
    
    # Set environment variables
    $env:JASYPT_ENCRYPTOR_PASSWORD = $JasyptKey
    $env:SERVER_PORT = $Port
    $env:SPRING_PROFILES_ACTIVE = "prod"
    
    Write-Status "Environment variables set:" $Blue
    Write-Status "  JASYPT_ENCRYPTOR_PASSWORD: [HIDDEN]" $Blue
    Write-Status "  SERVER_PORT: $Port" $Blue
    Write-Status "  SPRING_PROFILES_ACTIVE: prod" $Blue
    
    # Create startup script
    $startupScript = @"
@echo off
echo Starting ATM Monitoring System - Production
echo ==========================================
set JASYPT_ENCRYPTOR_PASSWORD=$JasyptKey
set SERVER_PORT=$Port
set SPRING_PROFILES_ACTIVE=prod

echo Server will start on port: %SERVER_PORT%
echo Profile: %SPRING_PROFILES_ACTIVE%
echo.

java -jar target\ATMNTRING-0.0.1-SNAPSHOT.jar
pause
"@
    
    $startupScript | Out-File -FilePath "start-production.bat" -Encoding ASCII
    
    # Create service installation script
    $serviceScript = @"
@echo off
echo Installing ATM Monitoring as Windows Service
echo ===========================================

set SERVICE_NAME=ATMMonitoring
set JAR_PATH=%CD%\target\ATMNTRING-0.0.1-SNAPSHOT.jar
set LOG_PATH=%CD%\logs

if not exist "%LOG_PATH%" mkdir "%LOG_PATH%"

sc create %SERVICE_NAME% binPath="java -jar %JAR_PATH%" start=auto
sc description %SERVICE_NAME% "ATM Monitoring System Production Service"

echo Service installed successfully!
echo To start: sc start %SERVICE_NAME%
echo To stop: sc stop %SERVICE_NAME%
echo To remove: sc delete %SERVICE_NAME%
pause
"@
    
    $serviceScript | Out-File -FilePath "install-service.bat" -Encoding ASCII
    
    Write-Status "Deployment files created successfully!" $Green
    Write-Status "  - start-production.bat: Manual startup script" $Green
    Write-Status "  - install-service.bat: Windows service installation" $Green
}

function Show-Summary {
    Write-Status "=== DEPLOYMENT SUMMARY ===" $Green
    Write-Status "Application: ATM Monitoring System" $Blue
    Write-Status "Profile: Production" $Blue
    Write-Status "Port: $ServerPort" $Blue
    Write-Status "" $Blue
    Write-Status "Next Steps:" $Yellow
    Write-Status "1. To start manually: .\start-production.bat" $Yellow
    Write-Status "2. To install as service: .\install-service.bat (Run as Administrator)" $Yellow
    Write-Status "3. Application URL: http://localhost:$ServerPort/api" $Yellow
    Write-Status "4. API Documentation: http://localhost:$ServerPort/api/swagger-ui.html" $Yellow
    Write-Status "" $Blue
    Write-Status "Log files will be in the logs/ directory" $Blue
    Write-Status "==========================" $Green
}

# Main execution
try {
    Write-Status "Starting ATM Monitoring Production Deployment" $Green
    Write-Status "=============================================" $Green
    
    Test-Prerequisites
    Update-ProductionProperties $JasyptPassword
    Build-Application
    Deploy-Application $JasyptPassword $ServerPort
    Show-Summary
    
    Write-Status "Deployment completed successfully!" $Green
    
} catch {
    Write-Error-Exit "Deployment failed: $($_.Exception.Message)"
} 