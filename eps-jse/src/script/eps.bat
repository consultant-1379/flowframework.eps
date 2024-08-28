@echo off

echo "Usage: eps.bat <EPS_INSTANCE_ID> [DEPLOY_FOLDER] [OUTPUT_LOG] [INSTRUMENTATION_METHOD] [INSTRUMENTATION_LOCATION] [REMOTE_JMX_PORT] [MAX_HEAP_SIZE_GB] [INITIAL_HEAP_SIZE_GB] [ADDITIONAL_JVM_TUNING] [HOST_IP]"

rem If need to configure EPS parameters for a specific EPS instance, it is recommended to create a separate script file which invokes this eps.sh script for each EPS instance.

SET eps_instance_id=%1
SET deploy_folder=%2
SET output_log=%3
SET instr_option=%4
SET instr_output=%5
SET remote_jmx_port=%6
SET max_heap_size_gb=%7
SET initial_heap_size_gb=%8
SET additional_java_tuning=%9

SHIFT

SET GC=%9

SHIFT

SET host=%9

ECHO supplied arguments eps_instance_id=%eps_instance_id%, deploy_folder=%deploy_folder%, output_log=%output_log%, instr_option=%instr_option%, instr_output=%instr_output%, remote_jmx_port=%remote_jmx_port%, max_heap_size_gb=%max_heap_size_gb%, initial_heap_size_gb=%initial_heap_size_gb%, additional_java_tuning=%additional_java_tuning% ,GC=%GC%, host_IP=%host%
ECHO -

REM Trying to auto get the IP is a pain below for ref
REM FOR /f "tokens=1-9" %%a in ('"systeminfo | find /i "OS Name""') DO (set ver=%%e)
REM ECHO checking windows version
REM IF "%ver%"=="7" (set ip_address_string="IPv4 Address") ELSE (set ip_address_string="IP Address")
REM ECHO looking for IP address
REM  /f "usebackq tokens=2 delims=:" %%f in (`ipconfig ^| findstr /c:%ip_address_string%`)DO echo "x%"%%f":192=%"=="x%"%%f""

SET jmx_prop=""

IF "%remote_jmx_port%" NEQ "" (
	IF "%host%" NEQ "" (
	ECHO will expose JMX on address %host%:%remote_jmx_port%
	SET jmx_prop=-Dcom.sun.management.jmxremote.port=%remote_jmx_port% -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=%host%
	)
)

REM If hazelcast stats are required use line below.
REM	SET jmx_hazelcast= -Dhazelcast.jmx=true -Dhazelcast.jmx.detailed=true

ECHO JMX settings: %jmx_prop%
ECHO -

set HOME=%CD%
if "%EPS_HOME%" == "" (
	echo EPS_HOME not set using current dir 
	SET EPS_HOME=%HOME%
)

ECHO EPS_HOME is set to %EPS_HOME%

SET main_class_name=com.ericsson.component.aia.services.eps.core.main.EpsApplication

IF "%deploy_folder%" == "" (
	SET jvm_prop= -server -Dcom.ericsson.component.aia.services.eps.module.deployment.folder.path=\var\ericsson\eps\flows
) ELSE (
	SET jvm_prop= -server -Dcom.ericsson.component.aia.services.eps.module.deployment.folder.path=%deploy_folder%
)

ECHO jvm props: %jvm_prop%
ECHO -

REM set this to true to enable CDI environment and detection of CDI beans (including event handlers)
SET jvm_prop_cdi= -Dcom.ericsson.component.aia.services.eps.module.cdi.enabled=false

IF "%eps_instance_id%" == ""	(SET eps_instance_id=default_eps_instance)

IF "%initial_heap_size_gb%"== "" (SET initial_heap_size_gb=1)

if "%max_heap_size_gb%" == "" (SET max_heap_size_gb=1)

echo EPS unique instance id is %eps_instance_id%

SET jvm_prop_ID= -DEPS_INSTANCE_ID=%eps_instance_id%
SET jvm_prop_log_conf= -Dlogback.configurationFile=%EPS_HOME%\conf\logback.xml


IF "%output_log%" == "" (SET output_log=%EPS_HOME%\log\out.log)



SET error_log_path=%EPS_HOME%\log\err.log

IF "%instr_option%" == "" (SET jvm_prop_stats= -Dcom.ericsson.component.aia.services.eps.core.statistics.reporting.method=%instr_option%)

IF "%instr_output%" == "" (SET jvm_prop_csv= -Dcom.ericsson.component.aia.services.eps.core.statistics.reporting.csv.location=%instr_output%)

SET memory_prop=""

REM Heap sizing


SET memory_prop= -Xms%initial_heap_size_gb%g -Xmx%max_heap_size_gb%g

SET addtional_props=""

IF "%additional_java_tuning%" NEQ "" (echo additional JVM tuning is %additional_java_tuning% & SET addtional_props=%additional_java_tuning%)


IF  "%GC%" == ""(SET GC= -XX:+UseParallelOldGC -XX:MaxGCPauseMillis=2 -XX:+ScavengeBeforeFullGC)


REM Optimization options
SET jvm_prop_optimization= -server -XX:+AggressiveOpts -XX:+UseCompressedOops -XX:+UseFastAccessorMethods -XX:+TieredCompilation -XX:+UseBiasedLocking -XX:+UseStringCache -Djava.net.preferIPv4Stack=true -XX:+DisableExplicitGC -Dsun.rmi.dgc.server.gcInterval=3600000 -Dsun.rmi.dgc.client.gcInterval=3600000

ECHO JVM tuning properties are %jvm_prop% %jvm_prop_cdi% %jvm_prop_ID% %jvm_prop_log_conf% %addtional_props% %jvm_prop_optimization%
ECHO -
ECHO JVM memory tuning is %memory_prop%
ECHO -
ECHO JMX configuration is %jmx_prop%
ECHO -
ECHO GC tuning is %GC%
ECHO -
ECHO Using the following classpath, note java 1.6 or higher required to use wildcard
SET CLASSPATH=%EPS_HOME%\lib\*;%EPS_HOME%\ext-lib\*
ECHO %CLASSPATH%
ECHO -
ECHO Starting EPS process... unique instance id is %eps_instance_id%
ECHO -----------------------------------------
ECHO  java %GC%%memory_prop%%jvm_prop% %jvm_prop_optimization% %jvm_prop_cdi%%jmx_prop% %jvm_prop_ID% %jvm_prop_log_conf%%addtional_props% %main_class_name%
ECHO -----------------------------------------

java %GC%%memory_prop%%jvm_prop% %jvm_prop_optimization% %jvm_prop_cdi%%jmx_prop% %jvm_prop_ID% %jvm_prop_log_conf%%addtional_props% %main_class_name% >> %output_log% 2>>%error_log_path%

