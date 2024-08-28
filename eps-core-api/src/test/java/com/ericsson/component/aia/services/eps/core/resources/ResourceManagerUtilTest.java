/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.resources;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ericsson.component.aia.services.eps.core.util.ResourceManagerUtil;

public class ResourceManagerUtilTest {

    private static final String NULL_URI = null;
    private static final String EMPTY_URI = "";
    private static final String EOL = System.getProperty("line.separator");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.core.util.ResourceManagerUtil#normalizeURI(java.lang.String)} .
     */
    @Test
    public void normalizeURI_NullURI_ThrowIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("URI must not be null or empty");
        ResourceManagerUtil.normalizeURI(NULL_URI);
    }

    @Test
    public void normalizeURI_EmptyURI_ThrowIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("URI must not be null or empty");
        ResourceManagerUtil.normalizeURI(EMPTY_URI);
    }

    @Test
    public void normalizeURI_ClassResourceURIEndWithClassSuffix_ReturnNormalizedURI() throws Exception {
        final String uri = "class:com.ericsson.test.Sample.class";
        assertEquals("com.ericsson.test.Sample", ResourceManagerUtil.normalizeURI(uri));
    }

    @Test
    public void normalizeURI_ClassResourceURINotEndWithClassSuffix_ReturnNormalizedURI() throws Exception {
        final String uri = "class:com.ericsson.test.Sample";
        assertEquals("com.ericsson.test.Sample", ResourceManagerUtil.normalizeURI(uri));
    }

    @Test
    public void normalizeURI_ClassResourceURIInSlash_ReturnNormalizedURI() throws Exception {
        final String uri = "class:com/ericsson/test/Sample.class";
        assertEquals("com/ericsson/test/Sample", ResourceManagerUtil.normalizeURI(uri));
    }

    @Test
    public void normalizeURI_ClassResourceURIWithBlankAfterPrefix_ReturnNormalizedURI() throws Exception {
        final String uri = "class:  com.ericsson.test.Sample.class";
        assertEquals("com.ericsson.test.Sample", ResourceManagerUtil.normalizeURI(uri));
    }

    @Test
    public void normalizeURI_ClasspathResourceURI_ReturnNormalizedURI() throws Exception {
        final String uri = "classpath:/usr/java/sample.txt";
        assertEquals("/usr/java/sample.txt", ResourceManagerUtil.normalizeURI(uri));
    }

    @Test
    public void normalizeURI_FileResourceURI_ReturnNormalizedURI() throws Exception {
        final String uri = "file:/usr/sample.txt";
        assertEquals("/usr/sample.txt", ResourceManagerUtil.normalizeURI(uri));
    }

    @Test
    public void normalizeURI_UnsupportedResourceURI_ThrowIllegalArgumentException() throws Exception {
        final String uri = "tcp://127.0.0.1:8888";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unsupported URI [tcp://127.0.0.1:8888] for normalization!");
        ResourceManagerUtil.normalizeURI(uri);
    }

    @Test
    public void normalizeURI_IncorrectResourceURI_ThrowIllegalArgumentException() throws Exception {
        final String uri = "/usr/java/lib";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unsupported URI [/usr/java/lib] for normalization!");
        ResourceManagerUtil.normalizeURI(uri);
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.core.util.ResourceManagerUtil#isClassResourceURI(java.lang.String)} .
     */
    @Test
    public void isClassResourceURI_NullURI_ReturnFalse() throws Exception {
        assertFalse(ResourceManagerUtil.isClassResourceURI(NULL_URI));
    }

    @Test
    public void isClassResourceURI_EmptyURI_ReturnFalse() throws Exception {
        assertFalse(ResourceManagerUtil.isClassResourceURI(EMPTY_URI));
    }

    @Test
    public void isClassResourceURI_URIStartWithClassPrefix_ReturnTrue() throws Exception {
        final String uri = "class:com.ericsson.test.Sample";
        assertTrue(ResourceManagerUtil.isClassResourceURI(uri));
    }

    @Test
    public void isClassResourceURI_URIStartWithClassPrefixAndBlank_ReturnTrue() throws Exception {
        final String uri = "class:  com.ericsson.test.Sample";
        assertTrue(ResourceManagerUtil.isClassResourceURI(uri));
    }

    @Test
    public void isClassResourceURI_URIStartWithBlank_ReturnFalse() throws Exception {
        final String uri = "  class:com.ericsson.test.Sample";
        assertFalse(ResourceManagerUtil.isClassResourceURI(uri));
    }

    @Test
    public void isClassResourceURI_URINotStartWithClassPrefix_ReturnFalse() throws Exception {
        final String uri = "file:/usr/sample.txt";
        assertFalse(ResourceManagerUtil.isClassResourceURI(uri));
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.core.util.ResourceManagerUtil#isClasspathResourceURI(java.lang.String)} .
     */
    @Test
    public void isClasspathResourceURI_NullURI_ReturnFalse() throws Exception {
        assertFalse(ResourceManagerUtil.isClasspathResourceURI(NULL_URI));
    }

    @Test
    public void isClasspathResourceURI_EmptyURI_ReturnFalse() throws Exception {
        assertFalse(ResourceManagerUtil.isClasspathResourceURI(EMPTY_URI));
    }

    @Test
    public void isClasspathResourceURI_URIStartWithClasspathPrefix_ReturnTrue() throws Exception {
        final String uri = "classpath:/usr/java/sample.txt";
        assertTrue(ResourceManagerUtil.isClasspathResourceURI(uri));
    }

    @Test
    public void isClasspathResourceURI_URIStartWithClasspathPrefixAndBlank_ReturnTrue() throws Exception {
        final String uri = "classpath:  /usr/java/sample.txt";
        assertTrue(ResourceManagerUtil.isClasspathResourceURI(uri));
    }

    @Test
    public void isClasspathResourceURI_URIStartWithBlank_ReturnFalse() throws Exception {
        final String uri = "  classpath:/usr/java/sample.txt";
        assertFalse(ResourceManagerUtil.isClasspathResourceURI(uri));
    }

    @Test
    public void isClasspathResourceURI_URINotStartWithClasspathPrefix_ReturnFalse() throws Exception {
        final String uri = "class:com.ericsson.test.Sample";
        assertFalse(ResourceManagerUtil.isClasspathResourceURI(uri));
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.core.util.ResourceManagerUtil#isFileResourceURI(java.lang.String)} .
     */
    @Test
    public void isFileResourceURI_NullURI_ReturnFalse() throws Exception {
        assertFalse(ResourceManagerUtil.isFileResourceURI(NULL_URI));
    }

    @Test
    public void isFileResourceURI_EmptyURI_ReturnFalse() throws Exception {
        assertFalse(ResourceManagerUtil.isFileResourceURI(EMPTY_URI));
    }

    @Test
    public void isFileResourceURI_URIStartWithFilePrefix_ReturnTrue() throws Exception {
        final String uri = "file:/usr/sample.txt";
        assertTrue(ResourceManagerUtil.isFileResourceURI(uri));
    }

    @Test
    public void isFileResourceURI_URIStartWithFilePrefixAndBlank_ReturnTrue() throws Exception {
        final String uri = "file:  /usr/sample.txt";
        assertTrue(ResourceManagerUtil.isFileResourceURI(uri));
    }

    @Test
    public void isFileResourceURI_URIStartWithBlank_ReturnFalse() throws Exception {
        final String uri = "  file:/usr/sample.txt";
        assertFalse(ResourceManagerUtil.isFileResourceURI(uri));
    }

    @Test
    public void isFileResourceURI_URINotStartWithFilePrefix_ReturnFalse() throws Exception {
        final String uri = "class:com.ericsson.test.Sample";
        assertFalse(ResourceManagerUtil.isFileResourceURI(uri));
    }

    /**
     * Test method for {@link com.ericsson.component.aia.services.eps.core.util.ResourceManagerUtil#loadResourceFromURI(java.lang.String)} .
     */
    @Test
    public void loadResourceFromURI_NullURI_ThrowIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unable to load resource from null or empty URI");
        ResourceManagerUtil.loadResourceAsTextFromURI(NULL_URI);
    }

    @Test
    public void loadResourceFromURI_EmptyURI_ThrowIllegalArgumentException() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unable to load resource from null or empty URI");
        ResourceManagerUtil.loadResourceAsTextFromURI(EMPTY_URI);
    }

    @Test
    /*
     * Issue: This is actually not an unit test. Because the method loadResourceFromURI() has dependency to an external class Resources in this case
     */
    public void loadResourceFromURI_SingleLineClasspathResourceURI_ReturnContent() throws Exception {
        final String uri = "classpath:/test_file_single_line.txt";
        final String content = ResourceManagerUtil.loadResourceAsTextFromURI(uri);
        assertEquals("This is a classpath resource for unit test.", content);
    }

    @Test
    /*
     * Issue: This is actually not an unit test. Because the method loadResourceFromURI() has dependency to an external class Resources in this case
     */
    public void loadResourceFromURI_FileResourceURI_ReturnFileContent() throws Exception {
        final File temp = File.createTempFile("test_sample", ".txt");
        temp.deleteOnExit();
        final FileWriter fileWriter = new FileWriter(temp);
        final BufferedWriter bufWriter = new BufferedWriter(fileWriter);
        bufWriter.write("This is a file resource for unit test.");
        bufWriter.newLine();
        bufWriter.write("Target: ResourceManagerUtilTest.java");
        bufWriter.close();
        fileWriter.close();
        final String uri = "file:" + temp.getAbsolutePath();
        final String content = ResourceManagerUtil.loadResourceAsTextFromURI(uri);
        assertEquals("This is a file resource for unit test." + EOL + "Target: ResourceManagerUtilTest.java", content);
    }

    @Test
    public void loadResourceFromURI_ClassResourceURIInDot_ThrowIllegalArgumentException() throws Exception {
        final String uri = "class:com.ericsson.test.Sample.class";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unable to load class [com/ericsson/test/Sample] as text");
        ResourceManagerUtil.loadResourceAsTextFromURI(uri);
    }

    @Test
    public void loadResourceFromURI_ClassResourceURIInSlash_ThrowIllegalArgumentException() throws Exception {
        final String uri = "class:com/ericsson/test/Sample.class";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unable to load class [com/ericsson/test/Sample] as text");
        ResourceManagerUtil.loadResourceAsTextFromURI(uri);
    }

    @Test
    public void loadResourceFromURI_UnknownURI_ThrowIllegalArgumentException() throws Exception {
        final String uri = "C:/Users/User/Documents/sample.xml";
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Unknown uri type [C:/Users/User/Documents/sample.xml]. Unable to load it!");
        ResourceManagerUtil.loadResourceAsTextFromURI(uri);
    }
}
