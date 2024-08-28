import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventHandlerContext;

class EventHandler(com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler):
    """ Test EPS Script """

    def __init__ (self):
        print "This is test EPS script"
        
    def init(self, ctx):
        self.__ctx = ctx

    def setInstanceId(self, instId):
        self.__instanceId = instId

    def getInstanceId(self):
        return self.__instanceId
    
    def onEvent(self, event):
        for value in [1,2,3]:
            for subscriber in self.__ctx.getEventSubscribers():
                subscriber.sendEvent(event)
        

eventHandler = EventHandler()
eventHandler.setInstanceId("jythonTripleMessagesHandler")