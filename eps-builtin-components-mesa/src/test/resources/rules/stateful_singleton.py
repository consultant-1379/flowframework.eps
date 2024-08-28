import com.ericsson.component.aia.services.eps.builtin.components.mesa.state.State as State

# accepts events of type INTERNAL_SYSTEM_UTILIZATION_60MIN;
# input variables are var_cc_ul & var_cc_dl 

matched = False

# returns True if alarm should be raised
def checkIfShouldRaiseAlarm():
    badUl = False
    if isu60.CONSUMED_CREDITS_UL > var_cc_ul:
        badUl = True

    badDl = False
    if isu60.CONSUMED_CREDITS_DL > var_cc_dl:
        badDl = True
    
    if badDl and badUl:
        alarmStart.additionalText = "Too much consumed credits in uplink and downlink"
        alarmStart.perceivedSeverity = 5
        return True
    elif badDl or badUl:
        alarmStart.additionalText = "Too much consumed credits in uplink or downlink"
        alarmStart.perceivedSeverity = 5
        return True
    print "all good"
    return False

def checkIfShouldClearAlarm():
    goodUl = False
    if isu60.CONSUMED_CREDITS_UL < var_cc_ul:
        goodUl = True

    goodDl = False
    if isu60.CONSUMED_CREDITS_DL < var_cc_dl:
        goodDl = True
    
    if goodUl and goodDl:
        alarmEnd.additionalText = "Consumed credits in uplink and downlink have improved"
        alarmEnd.perceivedSeverity = 1
        return True
    return False

if state.hasKey(State.MATCHED_FLAG_KEY):
    matched = checkIfShouldClearAlarm()
else:
    matched = checkIfShouldRaiseAlarm()