package com.ericsson.component.aia.services.eps.builtin.components.mesa.locator;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.mesa.event.Event;
import com.ericsson.component.aia.services.eps.mesa.locator.*;

/**
 * The Class ProxyLocator manages the event locator
 *
 * @see SmartLocator.
 * @see LegacyLocator
 */
public final class ProxyLocator implements SmartLocator, LegacyLocator {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private UnifiedLocator locator;

    @Override
    public void init() {
        final ServiceLoader<UnifiedLocator> loader = ServiceLoader.load(UnifiedLocator.class);

        final Iterator<UnifiedLocator> it = loader.iterator();
        if (!it.hasNext()) {
            throw new IllegalStateException("No unified locator detected thru ServiceLoader API");
        }
        locator = it.next();
        if (it.hasNext()) {
            log.warn("Duplicate unified locator detected thru ServiceLoader API, gonna use first detected '" + locator.getName() + "'");
        }
        log.info("Loaded {} locator via service loader", locator.getName());
        locator.init();
    }

    @Override
    public void shutdown() {
        locator.shutdown();
    }

    @Override
    public String getName() {
        return "proxy-locator";
    }

    @Override
    public boolean canLocate(final Event event, final int ropCount) {
        final long start = System.currentTimeMillis();
        final boolean result = locator.canLocate(event, ropCount);
        final long duration = System.currentTimeMillis() - start;
        if (log.isTraceEnabled()) {
            log.trace("canLocate() for event {} for ROP count {} took {} millis with result {}", event, ropCount, duration, result);
        }
        return result;
    }

    @Override
    public boolean canLocate(final String eventTypeName, final long resourceId, final long ropId) {
        final long start = System.currentTimeMillis();
        final boolean result = locator.canLocate(eventTypeName, resourceId, ropId);
        final long duration = System.currentTimeMillis() - start;
        if (log.isTraceEnabled()) {
            log.trace("canLocate() for event type {} for resource ID {} for ROP {} took {} millis with result {}", eventTypeName, resourceId,
                    ropId, duration, result);
        }
        return result;
    }

    @Override
    public Event locate(final Event event, final int ropCount) {
        final long start = System.currentTimeMillis();
        if (!canLocate(event, ropCount)) {
            throw new IllegalArgumentException("Locator " + locator.getName() + " does not support location for event " + event + " for ROP count "
                    + ropCount);
        }
        final Event result = locator.locate(event, ropCount);
        if (result == null) {
            throw new IllegalArgumentException("Unable to locate event for " + event + " and ROP count " + ropCount);
        }
        final long duration = System.currentTimeMillis() - start;
        if (log.isTraceEnabled()) {
            log.trace("locate() for event {} for ROP count {} took {} millis with result {}", event, ropCount, duration, result);
        }
        return result;
    }

    @Override
    public Event locate(final String eventTypeName, final long resourceId, final long ropId) {
        final long start = System.currentTimeMillis();
        if (!canLocate(eventTypeName, resourceId, ropId)) {
            throw new IllegalArgumentException("Locator " + locator.getName() + " does not support location for event type " + eventTypeName
                    + " for ROP ID " + ropId + " and for resource ID " + resourceId);
        }
        final Event result = locator.locate(eventTypeName, resourceId, ropId);
        if (result == null) {
            throw new IllegalArgumentException("Unable to locate event for event type " + eventTypeName + " and ROP ID " + ropId
                    + " and for resource ID " + resourceId);
        }
        final long duration = System.currentTimeMillis() - start;
        if (log.isTraceEnabled()) {
            log.trace("locate() for event type {} for ROP ID {} and for resource ID {} took {} millis with result ", eventTypeName, ropId,
                    resourceId, duration, result);
        }
        return result;
    }
}
