#sudo docker build -t ericsson/eps-twitter-trend .
#sudo docker run -d ericsson/eps-twitter-trend

FROM ericsson/eps

COPY twitter_trending* /var/ericsson/eps/flows/
COPY *.jar /opt/ericsson/eps/ext-lib/

ENTRYPOINT ["/opt/ericsson/eps/bin/startEPSDemo.sh"]