#sudo docker build -t ericsson/eps .
#sudo docker run -t -i ericsson/eps /bin/bash

FROM n3ziniuka5/ubuntu-oracle-jdk:14.10-JDK7

ADD provisionEPS.sh /tmp/provisionEPS.sh

RUN chmod +x /tmp/provisionEPS.sh

COPY eps*.tar.gz /tmp/

RUN /tmp/provisionEPS.sh