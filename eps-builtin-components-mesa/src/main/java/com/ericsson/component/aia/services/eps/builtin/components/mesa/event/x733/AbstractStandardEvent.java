package com.ericsson.component.aia.services.eps.builtin.components.mesa.event.x733;

import java.sql.Timestamp;
import java.util.Date;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.AbstractEvent;
import com.ericsson.component.aia.services.eps.mesa.event.Event;

/**
 * Common standard fields.
 */

@SuppressWarnings("PMD.TooManyFields")
public abstract class AbstractStandardEvent extends AbstractEvent implements StandardEvent, Event {

    private static final long serialVersionUID = 4270071921880535037L;

    // names, types and order of these fields matches definitions in
    // interface com.ericsson.eniq.events.notification.northbound.Notification;
    // we assume here that structure of Notification interface is correctly
    // aligned with global & Ericsson standards, considering that that feature
    // has been running in production for quite some time; if there are any
    // errors in Notification interface, they are inherited here

    private Integer notificationIdentifier;
    private Timestamp eventDate;
    private String invokeIdentifier;
    private String modeId;
    private Integer eventType;
    private Date currentDate;
    private ProbableCause probableCause;
    private Integer perceivedSeverity;
    private String backedUpStatus;
    private String backupObject;
    private String trendIndicator;
    private String correlatedNotifications;
    private String stateChangeDefinition;
    private String proposedRepairActions;
    private String monitoredAttributes;
    private String specificProblems;
    private String thresholdInformation;
    private String managedObject;
    private String additionalText;

    @Override
    public final String getManagedObject() {
        return managedObject;
    }

    @Override
    public final String getAdditionalText() {
        return additionalText;
    }

    @Override
    public final String getMonitoredAttributes() {
        return monitoredAttributes;
    }

    @Override
    public final String getSpecificProblems() {
        return specificProblems;
    }

    @Override
    public final String getThresholdInformation() {
        return thresholdInformation;
    }

    @Override
    public final String getBackedUpStatus() {
        return backedUpStatus;
    }

    @Override
    public final String getBackupObject() {
        return backupObject;
    }

    @Override
    public final String getTrendIndication() {
        return trendIndicator;
    }

    @Override
    public final String getCorrelatedNotifications() {
        return correlatedNotifications;
    }

    @Override
    public final String getStateChangeDefinition() {
        return stateChangeDefinition;
    }

    @Override
    public final String getProposedRepairActions() {
        return proposedRepairActions;
    }

    @Override
    public final Integer getPerceivedSeverity() {
        return perceivedSeverity;
    }

    @Override
    public final ProbableCause getProbableCause() {
        return probableCause;
    }

    @Override
    public final Date getCurrentDate() {
        return currentDate;
    }

    @Override
    public final Integer getEventType() {
        return eventType;
    }

    @Override
    public final Timestamp getEventDate() {
        return eventDate;
    }

    @Override
    public final String getInvokeIdentifier() {
        return invokeIdentifier;
    }

    @Override
    public final String getModeId() {
        return modeId;
    }

    @Override
    public final Integer getNotificationIdentifier() {
        return notificationIdentifier;
    }

    @Override
    public final void setManagedObject(final String managedObject) {
        this.managedObject = managedObject;
    }

    @Override
    public final void setAdditionalText(final String additionalText) {
        this.additionalText = additionalText;
    }

    @Override
    public final void setMonitoredAttributes(final String monitoredAttributes) {
        this.monitoredAttributes = monitoredAttributes;
    }

    @Override
    public final void setSpecificProblems(final String specificProblems) {
        this.specificProblems = specificProblems;
    }

    @Override
    public final void setThresholdInformation(final String thresholdInformation) {
        this.thresholdInformation = thresholdInformation;
    }

    @Override
    public final void setBackedUpStatus(final String backedUpStatus) {
        this.backedUpStatus = backedUpStatus;
    }

    @Override
    public final void setBackupObject(final String backupObject) {
        this.backupObject = backupObject;
    }

    @Override
    public final void setTrendIndicator(final String trendIndicator) {
        this.trendIndicator = trendIndicator;
    }

    @Override
    public final void setCorrelatedNotifications(final String correlatedNotifications) {
        this.correlatedNotifications = correlatedNotifications;
    }

    @Override
    public final void setStateChangeDefinition(final String stateChangeDefinition) {
        this.stateChangeDefinition = stateChangeDefinition;
    }

    @Override
    public final void setProposedRepairActions(final String proposedRepairActions) {
        this.proposedRepairActions = proposedRepairActions;
    }

    @Override
    public final void setPerceivedSeverity(final Integer perceivedSeverity) {
        this.perceivedSeverity = perceivedSeverity;
    }

    @Override
    public final void setProbableCause(final ProbableCause probableCause) {
        this.probableCause = probableCause;
    }

    @Override
    public final void setCurrentDate(final Date currentDate) {
        this.currentDate = currentDate;
    }

    @Override
    public final void setEventType(final Integer eventType) {
        this.eventType = eventType;
    }

    @Override
    public final void setEventDate(final Timestamp eventDate) {
        this.eventDate = eventDate;
    }

    @Override
    public final void setInvokeIdentifier(final String invokeIdentifier) {
        this.invokeIdentifier = invokeIdentifier;
    }

    @Override
    public final void setNotificationIdentifier(final Integer notificationIdentifier) {
        this.notificationIdentifier = notificationIdentifier;
    }

    @Override
    public final void setModeId(final String modeId) {
        this.modeId = modeId;
    }

    @Override
    public String toString() {
        return "StandardEvent [managedObject=" + managedObject + ", additionalText=" + additionalText + ", monitoredAttributes="
                + monitoredAttributes + ", specificProblems=" + specificProblems + ", thresholdInformation=" + thresholdInformation
                + ", backedUpStatus=" + backedUpStatus + ", backupObject=" + backupObject + ", trendIndicator=" + trendIndicator
                + ", correlatedNotifications=" + correlatedNotifications + ", stateChangeDefinition=" + stateChangeDefinition
                + ", proposedRepairActions=" + proposedRepairActions + ", perceivedSeverity=" + perceivedSeverity + ", probableCause="
                + probableCause + ", currentDate=" + currentDate + ", eventType=" + eventType + ", eventDate=" + eventDate + ", invokeIdentifier="
                + invokeIdentifier + ", modeId=" + modeId + ", notificationIdentifier=" + notificationIdentifier + "]";
    }
}
