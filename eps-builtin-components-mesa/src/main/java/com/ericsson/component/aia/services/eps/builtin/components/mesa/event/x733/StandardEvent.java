package com.ericsson.component.aia.services.eps.builtin.components.mesa.event.x733;

import java.sql.Timestamp;
import java.util.Date;

import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * Marker for global standard aligned {@link Event}s.
 */
public interface StandardEvent extends Event {

    /**
     * Gets the managed object.
     *
     * @return the managed object
     */
    @X733Field
    String getManagedObject();

    /**
     * Gets the additional text.
     *
     * @return the additional text
     */
    @X733Field
    String getAdditionalText();

    /**
     * Gets the monitored attributes.
     *
     * @return the monitored attributes
     */
    @X733Field
    String getMonitoredAttributes();

    /**
     * Gets the specific problems.
     *
     * @return the specific problems
     */
    @X733Field
    String getSpecificProblems();

    /**
     * Gets the threshold information.
     *
     * @return the threshold information
     */
    @X733Field
    String getThresholdInformation();

    /**
     * Gets the backed up status.
     *
     * @return the backed up status
     */
    @X733Field
    String getBackedUpStatus();

    /**
     * Gets the backup object.
     *
     * @return the backup object
     */
    @X733Field
    String getBackupObject();

    /**
     * Gets the trend indication.
     *
     * @return the trend indication
     */
    @X733Field
    String getTrendIndication();

    /**
     * Gets the correlated notifications.
     *
     * @return the correlated notifications
     */
    @X733Field
    String getCorrelatedNotifications();

    /**
     * Gets the state change definition.
     *
     * @return the state change definition
     */
    @X733Field
    String getStateChangeDefinition();

    /**
     * Gets the proposed repair actions.
     *
     * @return the proposed repair actions
     */
    @X733Field
    String getProposedRepairActions();

    /**
     * Gets the perceived severity.
     *
     * @return the perceived severity
     */
    @X733Field
    Integer getPerceivedSeverity();

    /**
     * Gets the probable cause.
     *
     * @return the probable cause
     */
    @X733Field
    ProbableCause getProbableCause();

    /**
     * Gets the current date.
     *
     * @return the current date
     */
    @X733Field
    Date getCurrentDate();

    /**
     * Gets the event type.
     *
     * @return the event type
     */
    @X733Field
    Integer getEventType();

    /**
     * Gets the event date.
     *
     * @return the event date
     */
    @X733Field
    Timestamp getEventDate();

    /**
     * Gets the invoke identifier.
     *
     * @return the invoke identifier
     */
    @X733Field
    String getInvokeIdentifier();

    /**
     * Gets the mode id.
     *
     * @return the mode id
     */
    @X733Field
    String getModeId();

    /**
     * Sets the mode id.
     *
     * @param modeId
     *            the new mode id
     */
    @X733Field
    void setModeId(String modeId);

    /**
     * Gets the notification identifier.
     *
     * @return the notification identifier
     */
    @X733Field
    Integer getNotificationIdentifier();

    /**
     * Sets the resource id.
     *
     * @param resourceId
     *            the new resource id
     */
    void setResourceId(long resourceId);

    /**
     * Sets the perceived severity.
     *
     * @param severity
     *            the new perceived severity
     */
    void setPerceivedSeverity(Integer severity);

    /**
     * Sets the managed object.
     *
     * @param managedObject
     *            the new managed object
     */
    void setManagedObject(String managedObject);

    /**
     * Sets the additional text.
     *
     * @param additionalText
     *            the new additional text
     */
    void setAdditionalText(String additionalText);

    /**
     * Sets the monitored attributes.
     *
     * @param monitoredAttributes
     *            the new monitored attributes
     */
    void setMonitoredAttributes(String monitoredAttributes);

    /**
     * Sets the specific problems.
     *
     * @param specificProblems
     *            the new specific problems
     */
    void setSpecificProblems(String specificProblems);

    /**
     * Sets the threshold information.
     *
     * @param thresholdInformation
     *            the new threshold information
     */
    void setThresholdInformation(String thresholdInformation);

    /**
     * Sets the backed up status.
     *
     * @param backedUpStatus
     *            the new backed up status
     */
    void setBackedUpStatus(String backedUpStatus);

    /**
     * Sets the backup object.
     *
     * @param backupObject
     *            the new backup object
     */
    void setBackupObject(String backupObject);

    /**
     * Sets the trend indicator.
     *
     * @param trendIndicator
     *            the new trend indicator
     */
    void setTrendIndicator(String trendIndicator);

    /**
     * Sets the correlated notifications.
     *
     * @param correlatedNotifications
     *            the new correlated notifications
     */
    void setCorrelatedNotifications(String correlatedNotifications);

    /**
     * Sets the state change definition.
     *
     * @param stateChangeDefinition
     *            the new state change definition
     */
    void setStateChangeDefinition(String stateChangeDefinition);

    /**
     * Sets the proposed repair actions.
     *
     * @param proposedRepairActions
     *            the new proposed repair actions
     */
    void setProposedRepairActions(String proposedRepairActions);

    /**
     * Sets the probable cause.
     *
     * @param probableCause
     *            the new probable cause
     */
    void setProbableCause(ProbableCause probableCause);

    /**
     * Sets the current date.
     *
     * @param currentDate
     *            the new current date
     */
    void setCurrentDate(Date currentDate);

    /**
     * Sets the event type.
     *
     * @param eventType
     *            the new event type
     */
    void setEventType(Integer eventType);

    /**
     * Sets the event date.
     *
     * @param eventDate
     *            the new event date
     */
    void setEventDate(Timestamp eventDate);

    /**
     * Sets the invoke identifier.
     *
     * @param invokeIdentifier
     *            the new invoke identifier
     */
    void setInvokeIdentifier(String invokeIdentifier);

    /**
     * Sets the notification identifier.
     *
     * @param notificationIdentifier
     *            the new notification identifier
     */
    void setNotificationIdentifier(Integer notificationIdentifier);
}
