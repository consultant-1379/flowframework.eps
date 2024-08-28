/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.core.coordination.utils;

import java.util.HashMap;
import java.util.Map;

public class TestingSingleton {
    private long numberOfCallsToGetStatus = 0;
    private static final int timeoutSeconds = 7;
    private Map<String,Object> data = new HashMap<>();
    private static final TestingSingleton SINGLETON = new TestingSingleton();
    private TestingSingleton(){
        
    }
    public static TestingSingleton getInstance(){
        return SINGLETON;
    }
    
    public void setData(final Map<String, Object> data){
        this.data = data;
    }
    
    public Map<String, Object> getData(){
        return data;
    }
    public boolean verifyDataWithTimeOut(final Map<String,Object> expectedData){
        for(int iterations = 0;iterations < timeoutSeconds;iterations++){
            if(data!=null && data.equals(expectedData)){
                return true;
            }else if(data==null && expectedData==null){
                return true;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        return false;
    }
    public long getNumberOfCallsToGetStatus(){
        return numberOfCallsToGetStatus;
    }
    public void setNumberOfCallsToGetStatus(final long numberOfCallsToGetStatus){
        this.numberOfCallsToGetStatus = numberOfCallsToGetStatus;
    }
    public void incrementNumberOfCallsToGetStatus(){
        numberOfCallsToGetStatus++;
    }
}
