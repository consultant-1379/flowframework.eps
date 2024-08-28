package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.builder;

import java.io.File;
import java.net.URI;
import java.util.List;

import javax.xml.bind.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.common.MesaException;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.context.Context;
import com.ericsson.component.aia.services.eps.builtin.components.mesa.model.validator.Message;

/**
 * The Class AbstractBuilder
 *
 * @see Builder.
 */
public abstract class AbstractBuilder implements Builder {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Context context;

    /**
     * Instantiates a new abstract builder.
     *
     * @param context
     *            the context
     */
    public AbstractBuilder(final Context context) {
        super();
        this.context = context;
    }

    /**
     * Gets the context.
     *
     * @return the context
     */
    protected final Context getContext() {
        return context;
    }

    /**
     * Validate.
     *
     * @param model
     *            the model
     */
    protected final void validate(final Object model) {
        if (context.validator() != null) {
            final List<Message> messages = context.validator().validate(model);
            if (!messages.isEmpty()) {
                // TODO refactor and decide how to propagate messages collected
                // during model validation etc.
                log.trace("Validated {} messages", messages.size());
            }
        }
    }

    /**
     * Builds the jaxb model.
     *
     * @param model
     *            the model
     * @param clazz
     *            the clazz
     * @return the object
     */
    protected final Object buildJaxbModel(final URI model, final Class<?> clazz) {
        try {
            final File file = new File(model);
            if (!file.exists()) {
                throw new IllegalArgumentException("File " + file.getAbsolutePath() + " does not exist");
            }
            final JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(file);
        } catch (final JAXBException e) {
            throw new MesaException(e);
        }
    }
}
