#!/bin/bash
# ********************************************************************
# Ericsson Radio Systems AB                                     SCRIPT
# ********************************************************************
#
# (c) Ericsson Radio Systems AB 2014 - All rights reserved.
#
# The copyright to the computer program(s) herein is the property
# of Ericsson Radio Systems AB, Sweden. The programs may be used
# and/or copied only with the written permission from Ericsson Radio
# Systems AB or in accordance with the terms and conditions stipulated
# in the agreement/contract under which the program(s) have been
# supplied.
#
# ********************************************************************
# Name    : eps.sh
# Date    : 30/09/2014
# Revision: A3
# Purpose : Main wrapper script for starting EPS JVM instances
#
# Usage   : eps.sh -i|--eps-id <eps_instance_id> -d|--deploy-directory [deploy_folder]
# 			-l|--output-log-directory [output_log_directory] -m|--instrumentation-method [instrumentation_method]
#			-L|--instrumentation-location [instrumentation_location]
#			-p|--jmx-port [remote_jmx_port] -H|--max-heap [max_heap_size_in_gb]
#			-I|--initial-heap [initial_heap_size_in_gb] -t|--jvm-tuning [additional_jvm_tuning]
#			-g|--garbage-collector [garbage_collector] -c|--classpath [application_classpath]
#			-z|--zoo-config-path [zoo_config_path] -S|--system-properties [system_properties]
#			-e|--env-file [environment_file]
#
# ********************************************************************

# ********************************************************************
#
#       Configuration Section
#
# ********************************************************************

_dir_=`/usr/bin/dirname $0`
EPS_HOME=`cd $_dir_/.. 2>/dev/null && pwd || $ECHO $_dir_`
DEFAULT_EPS_INSTANCE_ID="default_eps_instance"
DEFAULT_REMOTE_JMX_PORT=""
DEFAULT_MAX_HEAP_SIZE_GB=6G
DEFAULT_INITIAL_HEAP_SIZE_GB=6G
DEFAULT_LOG_DIR="/var/ericsson/log/eps/"
OUTPUT_LOG_NAME="out.log"
ERROR_LOG_NAME="err.log"
DEFAULT_DEPLOY_FOLDER="/var/ericsson/eps/flows/"
DEFAULT_CLASSPATH="$EPS_HOME/ext-lib/*:$EPS_HOME/lib/*:"
DEFAULT_MAIN_CLASS_NAME="com.ericsson.component.aia.services.eps.core.main.EpsApplication"
HOSTNAME=$(hostname)
DEFAULT_ZOOCONFIG_PATH="/etc/opt/ericsson/zookeeper/conf/"
DEFAULT_LOGBACK_FILE="$EPS_HOME/conf/logback.xml"
DEFAULT_JAVA="/usr/bin/java"

# ********************************************************************
#
#   Functions
#
# ********************************************************************


### Function: usage_msg ###
#
#   Print out the usage message
#
# Arguments:
#       none
# Return Values:
#       none
usage_msg()
{
clear

echo " Usage: eps.sh
-i|--eps-id <eps_instance_id>
-d|--deploy-directory [deploy_folder]
-e|--env-file [environment_file]
-l|--output-log-directory [output_log_directory]
-m|--instrumentation-method [instrumentation_method]
-L|--instrumentation-location [instrumentation_location]
-p|--jmx-port [remote_jmx_port]
-H|--max-heap [max_heap_size_in_gb]
-I|--initial-heap [initial_heap_size_in_gb]
-t|--jvm-tuning [additional_jvm_tuning]
-g|--garbage-collector [garbage_collector]
-c|--classpath [application_classpath]
-z|--zoo-config-path [zoo_config_path]
-b|--logback-file-location [logback_file_location]
-S|--system-properties [system_properties]
-X|--bias-locking [bias_lock]

-h|--help :Display usage message"

echo "To add multiple additional classpaths surround in double quotes and seperate with *: e.g. -c \"/opt/ericsson/lib/*:/opt/eps/lib/\""
echo "The default classpath is: ${DEFAULT_CLASSPATH}"
echo "deploy-directory is the directory which contains the flow file, e.g. /opt/ericsson/eps-flows/"
echo "eps-id is the service name of the eps instance you are starting, e.g. eps_1"
echo "system-properties must include \"-D\" as part of the parameter, e.g. -Dabc=123"
echo "Multiple system-properties can be added with multiple entries, e.g. --system-properties \"-Dabc=123\" --system-properties \"-Dxyz=987\""

exit 0
}


# ********************************************************************
#
#   Main body of program
#
# ********************************************************************
#

jvm_prop=""
main_class_name=${DEFAULT_MAIN_CLASS_NAME}
jvm_prop+=" -server"

# set this to true to enable CDI environment and detection of CDI beans (including event handlers)
jvm_prop+=" -Dcom.ericsson.component.aia.services.eps.module.cdi.enabled=false"

memory_prop=""
bias_lock=""


# Execute getopt
ARGS=$(getopt -o i:c:p:d:g:t:l:H:m:L:S:I:z:b:e:h -l "eps-id:,classpath:,max-heap:,initial-heap:,instrumentation-method:,instrumentation-location:,system-properties:,garbage-collector:,bias-locking:,output-log-directory:,jvm-tuning:,deploy-directory:,jmx-port:,zoo-config-path:,logback-file-location:,env-file:,help" -n "getopt.sh" -- "$@");

# Getopt result cannot be empty
if [ $# -eq 0 ];
then
  usage_msg
  exit 1
fi

eval set -- "$ARGS";

# Parse input parameters
while [ $# -gt 0 ]; do
  case "$1" in

    -i|--eps-id)
      shift;
      if [ -n "$1" ]; then
      	eps_instance_id="$1"
        shift;
      else
      	eps_instance_id=${DEFAULT_EPS_INSTANCE_ID}
      fi
      ;;

    -c|--classpath)
      shift;
      if [ -n "$1" ]; then
      _user_classpath_="$1"
      classpath="${DEFAULT_CLASSPATH}${_user_classpath_}:"
        shift;
      else
     	classpath="${DEFAULT_CLASSPATH}"
      fi
      ;;

    -d|--deploy-directory)
      shift;
      if [ -n "$1" ]; then
        _user_deploy_directory_="$1"
      	jvm_prop+=" -Dcom.ericsson.component.aia.services.eps.module.deployment.folder.path=${_user_deploy_directory_}"
        shift;
      else
      	jvm_prop+=" -Dcom.ericsson.component.aia.services.eps.module.deployment.folder.path=/var/ericsson/eps/flows"
      fi
      ;;

    -p|--jmx-port)
      shift;
      if [ -n "$1" ]; then
      	remote_jmx_port="$1"
        echo "--jmx-port used: $remote_jmx_port";
        shift;
      fi
      ;;

   -H|--max-heap)
      shift;
      if [ -n "$1" ]; then
      	max_heap_size_gb="$1"
        shift;
      else
      	max_heap_size_gb=${DEFAULT_MAX_HEAP_SIZE_GB}
      fi
      ;;

   -I|--initial-heap)
      shift;
      if [ -n "$1" ]; then
      	initial_heap_size_gb="$1"
        shift;
      else
      	initial_heap_size_gb=${DEFAULT_INITIAL_HEAP_SIZE_GB}
      fi
      ;;

   -t|--jvm-tuning)
      shift;
      if [ -n "$1" ]; then
      	additional_java_tuning="$1"
      	# we allow every instance to tune JVM separately
	   	memory_prop+=" $additional_java_tuning"
        shift;
      else
      	memory_prop+=" "
      fi
      ;;

   -g|--garbage-collector)
      shift;
      if [ -n "$1" ]; then
      	GC+=" $1"
        shift;
      else
      	GC=" -XX:+UseParallelOldGC" # parallel GC for both young and old generations
      	GC+=" -XX:MaxGCPauseMillis=2"
      	GC+=" -XX:+ScavengeBeforeFullGC"
      fi
      ;;

    -X|--bias-locking)
          shift;
          if [ -n "$1" ]; then
          	bias_lock+="$1"
          	jvm_prop+=" $bias_lock"
            shift;
          fi
          ;;

    -h|--help)
        usage_msg
        shift
        ;;

   -l|--output-log-directory)
      shift;
      if [ -n "$1" ]; then
      	output_log_path="$1"
        shift;
      fi
      ;;

   -z|--zoo-config-path)
      shift;
      if [ -n "$1" ]; then
      	zoo_config_path="$1"
        shift;
      fi
      ;;

   -b|--logback-file-location)
      shift;
      if [ -n "$1" ]; then
      	logback_config_file="$1"
        shift;
      fi
      ;;

   -m|--instrumentation-method)
      shift;
      if [ -n "$1" ]; then
      	instr_method="$1";
      	jvm_prop+=" -Dcom.ericsson.component.aia.services.eps.core.statistics.reporting.method=$instr_method"
        shift;
      fi
      ;;

   -L|--instrumentation-location)
      shift;
      if [ -n "$1" ]; then
      	instr_location="$1";
      	jvm_prop+=" -Dcom.ericsson.component.aia.services.eps.core.statistics.reporting.csv.location=$instr_location"
        shift;
      fi
      ;;

   -S|--system-properties)
      shift;
      if [ -n "$1" ]; then
      	sys_properties="$1";
        jvm_prop+=" $sys_properties"
        shift;
      fi
      ;;

	-e|--env-file)
	shift;
      if [ -n "$1" ]; then
      	ENV_FILE="$1";
        shift;
      fi
      ;;

    --)
      shift;
      break;
      ;;
  esac
done

# Source any passed env file
if [ ! -z ${ENV_FILE} ] && [ -s ${ENV_FILE} ]; then
	. ${ENV_FILE}
fi

# Set default values if nothing is passed in as an arguement or left blank

if [ -z "$eps_instance_id" ]; then
	eps_instance_id=${DEFAULT_EPS_INSTANCE_ID}
fi

if [ -z "$classpath" ]; then
	classpath="${DEFAULT_CLASSPATH}"
fi

if [ -z "$initial_heap_size_gb" ]; then
	initial_heap_size_gb=${DEFAULT_INITIAL_HEAP_SIZE_GB}
fi

if [ -z "$max_heap_size_gb" ]; then
	max_heap_size_gb=${DEFAULT_MAX_HEAP_SIZE_GB}
fi

if [ -z "${_user_deploy_directory_}" ]; then
	jvm_prop+=" -Dcom.ericsson.component.aia.services.eps.module.deployment.folder.path=${DEFAULT_DEPLOY_FOLDER}"
fi

if [ -z "${bias_lock}" ]; then
	jvm_prop+=" -XX:+UseBiasedLocking"
fi

if [ -z "${GC}" ]; then
	GC=" -XX:+UseParallelOldGC" # parallel GC for both young and old generations
	GC+=" -XX:MaxGCPauseMillis=2"
	GC+=" -XX:+ScavengeBeforeFullGC"
fi

if [ -z "${output_log_path}" ]; then

	if [ ! -d ${DEFAULT_LOG_DIR} ] ; then
  		mkdir -p ${DEFAULT_LOG_DIR}
	fi
    output_log=${DEFAULT_LOG_DIR}/${eps_instance_id}-${OUTPUT_LOG_NAME}
    error_log_path=${DEFAULT_LOG_DIR}/${eps_instance_id}-${ERROR_LOG_NAME}
else
   	if [ ! -d ${output_log_path} ] ; then
		mkdir -p ${output_log_path}
	fi
	output_log=${output_log_path}/${eps_instance_id}-${OUTPUT_LOG_NAME}
	error_log_path=${output_log_path}/${eps_instance_id}-${ERROR_LOG_NAME}
fi

if [ -z "$zoo_config_path" ]; then
	zoo_config_path="${DEFAULT_ZOOCONFIG_PATH}"
fi

if [ -z "$logback_config_file" ]; then
	logback_config_file="${DEFAULT_LOGBACK_FILE}"
fi


local_host=127.0.0.1

jvm_prop+=" -DEPS_INSTANCE_ID=${eps_instance_id}"
jvm_prop+=" -Dlogback.configurationFile=${logback_config_file}"
jvm_prop+=" -Dcom.ericsson.component.aia.itpf.sdk.external.configuration.folder.path=$zoo_config_path"

#
# JMX options for remote monitoring
#
jmx_prop=""

if [ -n "$remote_jmx_port" ]; then
    echo "Will Expose JMX on address $host:$remote_jmx_port"
    jmx_prop+=" -Dcom.sun.management.jmxremote=false"
    jmx_prop+=" -Dcom.sun.management.jmxremote.port=$remote_jmx_port"
    jmx_prop+=" -Dcom.sun.management.jmxremote.ssl=false"
    jmx_prop+=" -Dcom.sun.management.jmxremote.authenticate=false"
    jmx_prop+=" -Djava.rmi.server.hostname=$local_host"
    jmx_prop+=" -Dcom.sun.management.jmxremote.host=$local_host"
fi

# in case we want to enable hazelcast statistics
#jmx_prop+=" -Dhazelcast.jmx=true"
#jmx_prop+=" -Dhazelcast.jmx.detailed=true"

#
# Heap sizing
#

memory_prop+=" -Xms${initial_heap_size_gb}"       # set initial Java heap size
memory_prop+=" -Xmx${max_heap_size_gb}"       # set maximum Java heap size

#
# Optimization options
#
jvm_prop+=" -server"
jvm_prop+=" -XX:+AggressiveOpts"                   # Turn on point performance compiler optimizations that are expected to be default in upcoming releases.
jvm_prop+=" -XX:+UseCompressedOops"                # Enables the use of compressed pointers for optimized 64-bit performance with Java heap sizes less than 32gb.
jvm_prop+=" -XX:+UseFastAccessorMethods"           # Use optimized versions of Get<Primitive>Field.
jvm_prop+=" -XX:+TieredCompilation"
jvm_prop+=" -XX:+UseStringCache"
jvm_prop+=" -Djava.net.preferIPv4Stack"
jvm_prop+=" -XX:+DisableExplicitGC"
jvm_prop+=" -Dsun.rmi.dgc.server.gcInterval=3600000 -Dsun.rmi.dgc.client.gcInterval=3600000 "

#
# GC Activity logs VM Options - can be enabled for debugging purposes
# No need to turn this on in production
#
#jvm_prop+=" -XX:+PrintGCDetails" #prints used GC
#jvm_prop+=" -XX:+PrintGCDateStamps"
#jvm_prop+=" -XX:+PrintGCApplicationStoppedTime"
#jvm_prop+=" -XX:+PrintGCApplicationConcurrentTime"
#jvm_prop+=" -XX:+PrintTLAB"
#jvm_prop+=" -XX:+ResizeTLAB"
#jvm_prop+=" -Xloggc:/var/ericsson/log/eps/vmgc-eps-${eps_instance_id}.log"

ulimit -n 300000

if [ ! -z "${JAVA_HOME}" ]; then
	JAVA=${JAVA_HOME}/bin/java
else
	JAVA=$DEFAULT_JAVA
fi

echo "Starting EPS process... unique instance id is $eps_instance_id"
echo "-----------------------------------------"
echo  ${JAVA} -cp "$classpath" $memory_prop $GC $jvm_prop $jmx_prop $main_class_name
echo "-----------------------------------------"

if [ -f $error_log_path ] ; then
                _lines_=`wc -l $error_log_path | awk '{print $1}'`
else
                _lines_=0
fi

${JAVA} -cp "$classpath" $memory_prop $GC $jvm_prop $jmx_prop $main_class_name >> $output_log 2>> $error_log_path &

JPID=$!
sleep 1
ps --pid ${JPID} > /dev/null 2>&1
_rc_=$?
if [ ${_rc_} -eq 0 ]; then
    PIDFILE_LOC=${HOME}/.eps_${HOSTNAME}
    mkdir -p ${PIDFILE_LOC}
    echo $JPID > ${PIDFILE_LOC}/${eps_instance_id}.pid
	echo "${eps_instance_id} started successfully"
	exit 0
else
	echo "${eps_instance_id} failed to start"
	_lc_=`wc -l $error_log_path | awk '{print $1}'`
    _lc_=$(( ${_lc_} - ${_lines_} ))
    tail -${_lc_} $error_log_path
    exit ${_rc_}
fi