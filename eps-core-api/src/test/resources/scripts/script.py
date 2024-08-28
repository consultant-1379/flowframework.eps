import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

class EventHandler(com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler):
    """ Test EPS Script """

    def __init__ (self):
        print "This is test EPS script"

    def setInstanceId(self, instId):
        self.__instanceId = instId

    def getInstanceId(self):
        return self.__instanceId

handler = EventHandler()
handler.setInstanceId("jythonHandlerName")