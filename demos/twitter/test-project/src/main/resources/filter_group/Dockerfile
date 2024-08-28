#sudo docker build -t ericsson/eps-twitter-group-filter .
#sudo docker run -t -i -w "/opt/ericsson/eps/bin" ericsson/eps-twitter-group-filter /bin/bash

FROM ericsson/eps

COPY twitter_filter* /var/ericsson/eps/flows/
COPY twitter_group* /var/ericsson/eps/flows/

COPY *.jar /opt/ericsson/eps/ext-lib/
COPY *.sh /opt/ericsson/eps/bin/
RUN chmod +x /opt/ericsson/eps/bin/tweet.sh