# accepts events of type LTE_CELL_EVENT_CNT_1MIN;
# input variables are var_error_percentage_limit,  var_sub_limit

matched = False


errorRateBadForEvent0 = False
if (lce1min_0.numberOfErrors*100)/lce1min_0.numberOfSuccesses > var_error_percentage_limit:
    errorRateBadForEvent0 = True
    
errorRateBadForEvent1 = False
if (lce1min_1.numberOfErrors*100)/lce1min_1.numberOfSuccesses > var_error_percentage_limit:
    errorRateBadForEvent1 = True
    
errorRateBadForEvent2 = False
if (lce1min_2.numberOfErrors*100)/lce1min_2.numberOfSuccesses > var_error_percentage_limit:
    errorRateBadForEvent2 = True
    
errorRateBadForEvent3 = False
if (lce1min_3.numberOfErrors*100)/lce1min_3.numberOfSuccesses > var_error_percentage_limit:
    errorRateBadForEvent3 = True

sustainedHighErrorRate = False 
if errorRateBadForEvent0 and errorRateBadForEvent1 and errorRateBadForEvent2 and errorRateBadForEvent3:
    sustainedHighErrorRate = True
    
if sustainedHighErrorRate and lce1min_0.impactedSubscribers > var_sub_limit:
    alert.perceivedSeverity = 5
    alert.additionalText = "High error rate within a cell containing large number of subscribers"
    matched = True
    
if sustainedHighErrorRate and lce1min_0.impactedSubscribers < var_sub_limit:
    alert.perceivedSeverity = 5
    alert.additionalText = "High error rate within a cell containing negligible number of subscribers"
    matched = True
    