FROM armdocker.rnd.ericsson.se/aia/base/java

ARG MEDIATION_HOME=/opt/ericsson/mediation/APPLICATION_NAME/

RUN mkdir -p $MEDIATION_HOME/bin && \
    mkdir $MEDIATION_HOME/etc && \
    mkdir $MEDIATION_HOME/lib

COPY /opt/ericsson/mediation/APPLICATION_NAME/bin/* /opt/ericsson/mediation/APPLICATION_NAME/bin/
COPY /opt/ericsson/mediation/APPLICATION_NAME/etc/* /opt/ericsson/mediation/APPLICATION_NAME/etc/
COPY /opt/ericsson/mediation/APPLICATION_NAME/lib/* /opt/ericsson/mediation/APPLICATION_NAME/lib/

EXPOSE 8080 10101 11101 9092 9093 9094 2181

ENTRYPOINT ["/opt/ericsson/mediation/APPLICATION_NAME/bin/start.sh"]
