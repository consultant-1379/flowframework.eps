import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;

class InvalidEventHandler:
    """ Test EPS Script """

    def __init__ (self):
        print "This is test EPS script"
        
    def init(self, ctx):
        self.__ctx = ctx

    def setInstanceId(self, instId):
        self.__instanceId = instId

    def getInstanceId(self):
        return self.__instanceId
        

eventHandler = EventHandler()
eventHandler.setInstanceId("InvalidJythonTripleMessagesHandler")