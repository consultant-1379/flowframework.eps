package com.ericsson.component.aia.services.eps.deployer.util;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ericsson.component.aia.services.eps.core.util.ResourceManagerUtil;
import com.ericsson.component.aia.services.eps.deployer.util.EpsSchemaValidator;

public class EpsSchemaValidatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test_null_constructor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Flow xml must not be null.");
        new EpsSchemaValidator(null);
    }

    @Test
    public void test_empty_constructor() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Flow xml must not be null.");
        new EpsSchemaValidator("");
    }

    @Test
    public void test_valid_invalidSchema() {
        final File resource = new File("src/test/resources/flow_01.xml");
        final String resourceURI = ResourceManagerUtil.FILE_PREFIX + resource.getAbsolutePath();

        final EpsSchemaValidator schemaValidator = new EpsSchemaValidator(resourceURI);
        assertFalse(schemaValidator.isCompliantWithSchema());
    }

    @Test
    public void test_valid_validSchema() {
        final File resource = new File("src/test/resources/simple_io_flow.xml");
        final String resourceURI = ResourceManagerUtil.FILE_PREFIX + resource.getAbsolutePath();

        final EpsSchemaValidator schemaValidator = new EpsSchemaValidator(resourceURI);
        assertTrue(schemaValidator.isCompliantWithSchema());
    }
}
