import sys, subprocess

cmd = "docker pull armdocker.rnd.ericsson.se/aia/base/java"
print "===> Running %s" % cmd
process = subprocess.Popen(cmd.split(), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
out, err = process.communicate()
if err:
    print "Error while running command: %s" % err
else:
    print "Response: %s" % out
