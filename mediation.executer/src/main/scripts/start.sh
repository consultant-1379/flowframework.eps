#!/bin/sh

if [[ -z $MEDIATION_HOME ]]; then
  MEDIATION_HOME="/opt/ericsson/mediation/APPLICATION_NAME/"
fi

mediation_jar="${MEDIATION_HOME}/lib/mediation-template-*UBER*.jar"

echo "Starting APPLICATION_NAME ..."
echo "------------------------------------------------------------------------"
#JAVA_CMD="java $memory_prop ${jvm_prop} ${jmx_prop} ${debug_prop} ${LOG_ARGS} ${CONFIG_DIR_ARG} ${vm_param} ${OTHER_PARAMS} $@ -jar ${mediation_jar} -Dflow=$1"
JAVA_CMD="java $@ -jar ${mediation_jar}"

echo ${JAVA_CMD}
echo "------------------------------------------------------------------------"

exec ${JAVA_CMD}
