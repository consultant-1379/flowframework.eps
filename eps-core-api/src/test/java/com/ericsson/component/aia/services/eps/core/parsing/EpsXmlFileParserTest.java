package com.ericsson.component.aia.services.eps.core.parsing;

import java.io.InputStream;

import org.junit.*;

import com.ericsson.component.aia.services.eps.component.module.EpsModule;
import com.ericsson.component.aia.services.eps.core.parsing.EpsModuleParser;
import com.ericsson.component.aia.services.eps.core.parsing.EpsXmlFileParser;
import com.ericsson.oss.itpf.sdk.resources.Resources;

public class EpsXmlFileParserTest {

    private EpsModuleParser<InputStream> parser;

    @Before
    public void setUp() throws Exception {
        parser = new EpsXmlFileParser();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_null() {
        parser.parseModule(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_non_xml_file() {
        final InputStream inputStream = Resources.getClasspathResource("/not_xml.txt").getInputStream();
        Assert.assertNotNull(inputStream);
        parser.parseModule(inputStream);
    }

    @Test
    public void test_flow_01() {
        final InputStream inputStream = Resources.getClasspathResource("/flow_01.xml").getInputStream();
        Assert.assertNotNull(inputStream);
        final EpsModule module = parser.parseModule(inputStream);
        Assert.assertNotNull(module);
        Assert.assertEquals("com.ericsson.test", module.getNamespace());
        Assert.assertEquals("test_name", module.getName());
        Assert.assertEquals("1.1.0", module.getVersion());
    }

}
