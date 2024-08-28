#!/bin/bash

EPS_VER="3.0.2"

INSTALL_DIR=/opt/ericsson/eps
BIN_DIR=$INSTALL_DIR/bin
LOG_DIR=/var/ericsson/log/
RAM="512M"
EPS_FILE_NAME=eps-$EPS_VER.tar.gz
LOCAL_EPS_LOCATION=/tmp/$EPS_FILE_NAME
FLOW_DIR=/var/ericsson/eps/flows/

sudo mkdir -p $INSTALL_DIR
sudo mkdir -p $LOG_DIR
sudo mkdir -p $FLOW_DIR
sudo chmod 777 $LOG_DIR
sudo chmod 777 $FLOW_DIR


NEXUS_URL="https://arm101-eiffel004.lmera.ericsson.se:8443/nexus/service/local/artifact/maven/redirect?r=releases&g=com.ericsson.oss.services.eps&a=eps-jse&v=$EPS_VER&e=tar.gz&c=bin"
TARGET_FILE="$INSTALL_DIR/$EPS_FILE_NAME"
START_FILE_NAME="startEPSDemo.sh"
START_FILE="$BIN_DIR/$START_FILE_NAME"
START_CMD="$BIN_DIR/eps.sh -i demoEps -I $RAM -d $FLOW_DIR"
KEEP_ALIVE_CMD="while true; do echo EPS >> /dev/null; sleep 10; done"

if [ -e "$LOCAL_EPS_LOCATION" ]
then
	cp $LOCAL_EPS_LOCATION $TARGET_FILE
	echo "Copied file from $LOCAL_EPS_LOCATION to $TARGET_FILE"  
else
	echo "Was not able to find local file. Will try to download it from Nexus"
	sudo wget -q $NEXUS_URL -O $TARGET_FILE
fi

if [ -e "$TARGET_FILE" ]
then
	sudo tar -xzf $TARGET_FILE -C $INSTALL_DIR
	sudo touch $START_FILE
	sudo echo "#!/bin/bash" > $START_FILE
	sudo echo $START_CMD >> $START_FILE
	sudo echo $KEEP_ALIVE_CMD >> $START_FILE
	sudo chmod 777 $START_FILE
	echo "Successfully configured EPS"
else
	echo "Did not find EPS binary to configure"
fi