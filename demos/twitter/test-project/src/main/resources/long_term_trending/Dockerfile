#sudo docker build -t ericsson/eps-twitter-lt .
#sudo docker run -d ericsson/eps-twitter-lt

FROM ericsson/eps

COPY twitter_long_term_trending* /var/ericsson/eps/flows/
COPY *.jar /opt/ericsson/eps/ext-lib/

ENTRYPOINT ["/opt/ericsson/eps/bin/startEPSDemo.sh"]