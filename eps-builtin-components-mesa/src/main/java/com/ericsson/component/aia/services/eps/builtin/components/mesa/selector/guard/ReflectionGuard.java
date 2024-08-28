package com.ericsson.component.aia.services.eps.builtin.components.mesa.selector.guard;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Set;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.MesaException;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.conf.ConfId;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * All events within the view have to match the guard specification.
 * <p>
 *
 * TODO FIXME performance, maybe use some open source reflection tools etc?
 *
 * @param <T>
 *            the generic type
 */
public final class ReflectionGuard<T> extends SetGuard<T> {

    private final String fieldName;

    /**
     * Instantiates a new reflection guard.
     *
     * @param confId
     *            the conf id
     * @param tenant
     *            the tenant
     * @param allowed
     *            the allowed
     * @param set
     *            the set
     * @param fieldName
     *            the field name
     */
    public ReflectionGuard(final ConfId confId, final String tenant, final boolean allowed, final Set<T> set, final String fieldName) {
        super(confId, tenant, allowed, set);
        this.fieldName = fieldName;
    }

    /**
     * Instantiates a new reflection guard.
     *
     * @param confId
     *            the conf id
     * @param tenant
     *            the tenant
     * @param allowed
     *            the allowed
     * @param fieldName
     *            the field name
     */
    public ReflectionGuard(final ConfId confId, final String tenant, final boolean allowed, final String fieldName) {
        super(confId, tenant, allowed);
        this.fieldName = fieldName;
    }

    @Override
    protected boolean matches(final View view) {

        final Iterator<Event> itr = view.iterator();
        while (itr.hasNext()) {
            final Event event = itr.next();
            Object value;
            // TODO see getValue, reflection + inheritance is a bit of a pain
            if ("resourceId".equalsIgnoreCase(fieldName)) {
                value = event.getResourceId();
            } else {
                value = getValue(event);
            }

            if (value == null) {
                return false;
            }
            final boolean matches = getSet().contains(value);
            if (matches && allowed) {
                continue;
            }
            if (matches && !allowed) {
                return false;
            }
            if (!matches && allowed) {
                return false;
            }
            if (!matches && !allowed) {
                continue;
            }
        }
        return true;
    }

    private Object getValue(final Event event) {
        try {
            // TODO figure this one out getDeclaredFiled can find private fields
            // but not inherited ones
            // getField can get inheretedFields but not private ones.
            final Field field = event.getClass().getDeclaredField(fieldName); // TODO
            // performance,
            // caching
            // etc,
            // see
            // above
            field.setAccessible(true); // TODO will this work in Java EE
                                       // environment?
            return field.get(event);
        } catch (final Exception e) {
            throw new MesaException("Exception while reflecting field '" + fieldName + "'");
        }
    }
}
