package com.ericsson.component.aia.services.eps.builtin.components.mesa.binder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.MesaException;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.Util;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.View.ViewType;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.matrix.MatrixView;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.sequence.SequenceView;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.view.singleton.SingletonView;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * Provides automated binding of event objects to a variable name in Jython. The binding can be done in one of two ways
 *
 * 1) The bindings are done based on the class name of the event, a properties file provides class name to variable pairs 2) The bindings are done
 * based on incremental char steps starting with the letter e.
 *
 * For additional clarity in the case of a sequence or matrix the post-fix _x, where x is a number is applied. The numbering is such that 0 will
 * represent the latest event and 1 the next event in chronological order example below. e_0 e_1 e_2 e_3 event 3am, event 2am, event 1am, event 12am
 *
 */
public final class SimpleEventBinder implements EventBinder {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final boolean simpleNaming;
    private final Map<String, String> map; // key is simple class name, value is binding name

    /**
     * Instantiates a new simple event binder.
     *
     * @param fileName
     *            the file name
     */
    public SimpleEventBinder(final String fileName) {
        super();
        if (fileName == null) {
            simpleNaming = true;
        } else {
            simpleNaming = false;
        }
        map = new HashMap<String, String>();
        if (!simpleNaming) {
            loadMap(fileName);
        }
    }

    @Override
    public void bind(final View view, final PythonInterpreter interpreter) {
        interpreter.set("v", view);
        interpreter.set("view", view);
        if (log.isTraceEnabled()) {
            log.trace("Registered view under variables 'v' and 'view'");
        }
        switch (view.getViewType()) {
            case SINGLETON:
                processSingletonView((SingletonView) view, interpreter);
                break;
            case SEQUENCE:
                processSequenceView((SequenceView) view, interpreter);
                break;
            case MATRIX:
                processMatrixView((MatrixView) view, interpreter);
                break;
            // case CUBE:
            // // processCubeView((CubeView) view, interpreter);
            // throw new NotYetImplementedException(
            // "Complex event binding as yet to be dicided");
            // case BAG:
            // // processBagView((BagView) view, interpreter);
            // throw new NotYetImplementedException(
            // "Complex event binding as yet to be dicided");
            default:
                throw new IllegalArgumentException("Unknown view type '" + view.getViewType() + "'");
        }
    }

    private void processSingletonView(final SingletonView view, final PythonInterpreter interpreter) {
        String name = "e";
        if (!simpleNaming) {
            name = getSafe(view.getFirst());
        }
        interpreter.set(name, view.getFirst());

        if (log.isTraceEnabled()) {
            log.trace("Registered event under variable '{}' for singleton view", name);
        }
    }

    private void processSequenceView(final SequenceView view, final PythonInterpreter interpreter) {
        processSequenceView("e", view, interpreter, ViewType.SEQUENCE);
    }

    private void processMatrixView(final MatrixView view, final PythonInterpreter interpreter) {
        char character = 'e';
        for (final SequenceView sequenceView : view.getSequenceViews()) {
            processSequenceView(String.valueOf(character++), sequenceView, interpreter, ViewType.MATRIX);
        }
    }

    // private void processCubeView(final CubeView view,
    // final PythonInterpreter interpreter) {
    // throw new NotYetImplementedException(
    // "Complex event binding as yet to be dicided");
    // }
    //
    // private void processBagView(final BagView view,
    // final PythonInterpreter interpreter) {
    // throw new NotYetImplementedException(
    // "Complex event binding as yet to be dicided");
    // }

    private void processSequenceView(final String smpPrefix, final SequenceView view, final PythonInterpreter interpreter, final ViewType source) {
        String namePrefix = smpPrefix;
        if (!simpleNaming) {
            namePrefix = getSafe(view.getFirst());
        }
        for (int i = 0; i < view.size(); i++) {
            final String name = namePrefix + "_" + i;
            interpreter.set(name, view.get(i));
            if (log.isTraceEnabled()) {
                log.trace("Registered event under variable '{}' for {} view", name, source.toString());
            }
        }
    }

    private String getSafe(final Event event) {
        final String simpleClassName = event.getClass().getSimpleName();
        final String value = map.get(simpleClassName);
        if ((value == null) || value.isEmpty()) {
            throw new IllegalArgumentException("There is no binding associated with simple class name '" + simpleClassName
                    + "'. These are all known bindings: " + map.toString());
        }
        return value;
    }

    private void loadMap(final String fileName) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(fileName));
            final Properties props = new Properties();
            props.load(inputStream);
            for (final Object simpleClassName : props.keySet()) {
                final String binding = props.getProperty((String) simpleClassName);
                map.put((String) simpleClassName, binding);
            }
            log.info("Loaded following variable bindings {}", map);
        } catch (final Exception e) {
            throw new MesaException("Exception while loading variable bindings from root of classpath", e);
        } finally {
            Util.close(inputStream);
        }
    }
}
