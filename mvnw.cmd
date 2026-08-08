@echo off
setlocal
set MAVEN_VERSION=3.9.16
set MAVEN_ROOT=%USERPROFILE%\.m2\wrapper\dists
set MAVEN_HOME=%MAVEN_ROOT%\apache-maven-%MAVEN_VERSION%
set MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip
set MAVEN_SHA512=ed41650d42485cfc243fad22158caf9cbb5dc408ce7a09ddb94dd42a019de929ca43065bfa450612cf12bf78b5cafa3884b96c090de326ff590448c933454af3

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo Downloading Apache Maven %MAVEN_VERSION%...
  if not exist "%MAVEN_ROOT%" mkdir "%MAVEN_ROOT%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $zip=Join-Path $env:TEMP 'apache-maven-%MAVEN_VERSION%-bin.zip'; Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile $zip; $hash=(Get-FileHash -Algorithm SHA512 $zip).Hash.ToLowerInvariant(); if ($hash -ne '%MAVEN_SHA512%') { Remove-Item $zip -Force; throw 'Apache Maven checksum validation failed.' }; Expand-Archive -Path $zip -DestinationPath '%MAVEN_ROOT%' -Force; Remove-Item $zip -Force"
  if errorlevel 1 exit /b 1
)

call "%MAVEN_HOME%\bin\mvn.cmd" %*
endlocal
