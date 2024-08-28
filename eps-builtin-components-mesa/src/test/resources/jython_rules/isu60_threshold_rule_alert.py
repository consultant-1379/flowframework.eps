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
    alert.resourceId = isu60.resourceId
    alert.additionalText = "Too much consumed credits in uplink and downlink"
    alert.perceivedSeverity = 5
    matched = True
elif badDl or badUl:
    alert.resourceId = isu60.resourceId
    alert.additionalText = "Too much consumed credits in uplink or downlink"
    alert.perceivedSeverity = 1
    matched = True

# set to true if you wish to propagate incident
matched = badDl or badUl
    