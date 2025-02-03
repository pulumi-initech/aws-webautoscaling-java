@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  app startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and APP_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\app.jar;%APP_HOME%\lib\aws-6.66.3.jar;%APP_HOME%\lib\tls-5.0.0.jar;%APP_HOME%\lib\pulumi-1.0.0.jar;%APP_HOME%\lib\guava-33.3.1-jre.jar;%APP_HOME%\lib\junit-jupiter-params-5.11.4.jar;%APP_HOME%\lib\junit-jupiter-engine-5.11.4.jar;%APP_HOME%\lib\junit-jupiter-api-5.11.4.jar;%APP_HOME%\lib\junit-platform-engine-1.11.4.jar;%APP_HOME%\lib\junit-platform-commons-1.11.4.jar;%APP_HOME%\lib\junit-jupiter-5.11.4.jar;%APP_HOME%\lib\assertj-core-3.27.3.jar;%APP_HOME%\lib\failureaccess-1.0.2.jar;%APP_HOME%\lib\listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar;%APP_HOME%\lib\grpc-protobuf-1.57.2.jar;%APP_HOME%\lib\protobuf-java-util-3.25.5.jar;%APP_HOME%\lib\grpc-stub-1.57.2.jar;%APP_HOME%\lib\grpc-protobuf-lite-1.57.2.jar;%APP_HOME%\lib\grpc-netty-shaded-1.57.2.jar;%APP_HOME%\lib\grpc-core-1.57.2.jar;%APP_HOME%\lib\grpc-context-1.57.2.jar;%APP_HOME%\lib\grpc-api-1.57.2.jar;%APP_HOME%\lib\jsr305-3.0.2.jar;%APP_HOME%\lib\checker-qual-3.43.0.jar;%APP_HOME%\lib\error_prone_annotations-2.28.0.jar;%APP_HOME%\lib\j2objc-annotations-3.0.0.jar;%APP_HOME%\lib\future-converter-java8-guava-1.2.0.jar;%APP_HOME%\lib\gson-2.10.jar;%APP_HOME%\lib\protobuf-java-3.25.5.jar;%APP_HOME%\lib\byte-buddy-1.15.11.jar;%APP_HOME%\lib\opentest4j-1.3.0.jar;%APP_HOME%\lib\proto-google-common-protos-2.17.0.jar;%APP_HOME%\lib\future-converter-java8-common-1.2.0.jar;%APP_HOME%\lib\future-converter-guava-common-1.2.0.jar;%APP_HOME%\lib\future-converter-common-1.2.0.jar;%APP_HOME%\lib\perfmark-api-0.26.0.jar;%APP_HOME%\lib\annotations-4.1.1.4.jar;%APP_HOME%\lib\animal-sniffer-annotations-1.23.jar


@rem Execute app
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %APP_OPTS%  -classpath "%CLASSPATH%" myproject.WebserverInfra %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable APP_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%APP_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
