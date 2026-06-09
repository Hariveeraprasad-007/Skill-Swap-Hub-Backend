@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF)
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0__%"=="" (SET __MVNW_ARG0__=%~dpnx0) ELSE (SET __MVNW_ARG0__=%__MVNW_ARG0__% %~dpnx0)
@SET __MVNW_PSMODULEP_SAVE=%PSModulePath%
@SET PSModulePath=
@FOR /F "usebackq tokens=1* delims==" %%A IN (`powershell -noprofile "& {$scriptDir='%~dp0'; $env:__MVNW_ARG0__=''; $wrapperJar=$scriptDir+'.mvn\wrapper\maven-wrapper.jar'; $wrapperUrl='https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar'; if (-not (Test-Path $wrapperJar)) { $nl=[Environment]::NewLine; Write-Output \"Downloading from: $wrapperUrl\"; (New-Object System.Net.WebClient).DownloadFile($wrapperUrl, $wrapperJar) }; Write-Output \"__MVNW_CMD__=java -jar \`\"$wrapperJar\`\" %*\"}"`) DO @IF "%%A"=="__MVNW_CMD__" SET __MVNW_CMD__=%%B
@SET PSModulePath=%__MVNW_PSMODULEP_SAVE%
@SET __MVNW_PSMODULEP_SAVE=
%__MVNW_CMD__%
@SET __MVNW_CMD__=
@SET __MVNW_ARG0__=
