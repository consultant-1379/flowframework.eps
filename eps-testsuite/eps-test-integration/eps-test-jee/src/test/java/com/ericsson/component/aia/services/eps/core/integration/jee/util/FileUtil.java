/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT © Ericsson AB 2013-2015 - All Rights Reserved
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.integration.jee.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author esarlag
 * 
 */
public class FileUtil {

    public static void deleteCsvFiles() {
        final File folder = new File("target");
        for (final File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".csv")) {
                file.delete();
            }
        }
    }

    public static List<File> getCsvFiles() {
        final List<File> csvFileList = new ArrayList<File>();
        final File folder = new File("target");
        for (final File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".csv")) {
                csvFileList.add(file);
            }
        }
        return csvFileList;
    }

}
