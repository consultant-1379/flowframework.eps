import com.ericsson.component.aia.services.eps.monitoring.core.event.incident.Level as Level

# accepts events of type INTERNAL_SYSTEM_UTILIZATION_60MIN;
# input variables are var_cc_ul & var_cc_dl 

matched = False

badUl = False
if isu60.CONSUMED_CREDITS_UL > var_cc_ul:
    badUl = True

badDl = False
if isu60.CONSUMED_CREDITS_DL > var_cc_dl:
    badDl = True
    
if badDl and badUl:
    i.message = "DL & UL"
    i.level = Level.ERROR
elif badDl or badUl:
    i.message = "DL" if badDl else "UL"
    i.level = Level.WARN

# set to true if you wish to propagate incident
matched = badDl or badUl
    