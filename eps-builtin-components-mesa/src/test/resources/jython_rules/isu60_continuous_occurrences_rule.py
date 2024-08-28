import com.ericsson.component.aia.services.eps.monitoring.core.event.incident.Level as Level

# accepts events of type INTERNAL_SYSTEM_UTILIZATION_60MIN;
# input variables are var_cc_dl 

matched = False

# function definition
def matches(event):
    if event.CONSUMED_CREDITS_DL > var_cc_dl and not v.isOnPublicHoliday(event) and not v.isDuringWorkHours(event):
        return True
    return False

# if both current event and last 4 events satisfy expression, 
# then flag current event as matched
if matches(isu60):
    i.message = "Consumed DL Credits is Consistently Exceeding threshold"
    i.level = Level.WARN
    matched = True  