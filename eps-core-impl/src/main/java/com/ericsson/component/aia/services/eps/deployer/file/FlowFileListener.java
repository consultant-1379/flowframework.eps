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
package com.ericsson.component.aia.services.eps.deployer.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.resources.file.FileResourceEvent;
import com.ericsson.oss.itpf.sdk.resources.file.FileResourceEventType;
import com.ericsson.oss.itpf.sdk.resources.file.listener.FileResourceListener;

/**
 * Listener responsible to watch the flow directory, for every new file added try to parse it as module.
 *
 * @author epiemir
 */
public class FlowFileListener implements FileResourceListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final FileSystemListenerImpl fileSystemListener;
    private final ClassLoader epsClassLoader;

    /**
     * Instantiates a new flow file listener.
     *
     * @param fsImpl
     *            the {@link FileSystemListenerImpl}
     */
    public FlowFileListener(final FileSystemListenerImpl fsImpl) {
        this.fileSystemListener = fsImpl;
        epsClassLoader = Thread.currentThread().getContextClassLoader();
    }

    @Override
    public String getURI() {
        return fileSystemListener.getURI();
    }

    @Override
    public FileResourceEventType[] getEventTypes() {
        return new FileResourceEventType[] { FileResourceEventType.FILE_CREATED };
    }

    @Override
    public void onEvent(final FileResourceEvent fileResourceEvent) {
        logger.debug("Received event {}", fileResourceEvent);
        // set class loader saved from initialization phase due to java service loader scope
        final ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(epsClassLoader);
        final String modifiedResourceURI = fileResourceEvent.getURI();
        fileSystemListener.parseModuleFromFilePath(modifiedResourceURI);
        Thread.currentThread().setContextClassLoader(currentThreadClassLoader);
    }

}
