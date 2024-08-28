# accepts events of type INTERNAL_SYSTEM_UTILIZATION_60MIN;
# input variables are var_cc_ul & var_cc_dl 

matched = False

# agg event
badUl60 = False
if isu60_0.CONSUMED_CREDITS_UL > var_cc_ul and isu60_1.CONSUMED_CREDITS_UL > var_cc_ul:
    badUl60 = True

badDl60 = False
if isu60_0.CONSUMED_CREDITS_DL > var_cc_dl and isu60_1.CONSUMED_CREDITS_DL > var_cc_dl:
    badDl60 = True
 
# raw event
badUl = False
if isu_0.CONSUMED_CREDITS_UL > var_cc_ul and isu_1.CONSUMED_CREDITS_UL > var_cc_ul:
    badUl = True

badDl = False
if isu_0.CONSUMED_CREDITS_DL > var_cc_dl and isu_1.CONSUMED_CREDITS_DL > var_cc_dl:
    badDl = True   

if badDl and badUl and badUl60 and badDl60:
    alert.additionalText = "Both raw event agg events have UL & DL problems"
    alert.perceivedSeverity = 5
elif badDl or badUl or badDl60 or badUl60:
    #alert.resourceId = isu60.resourceId
    alert.additionalText = "Bad UL or DL has been detected"
    alert.perceivedSeverity = 1

# set to true if you wish to propagate incident
matched = badDl or badUl or badDl60 or badUl60
    