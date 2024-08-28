import math

# accepts events of type INTERNAL_SYSTEM_UTILIZATION_60MIN
# input variable is var_diff

matched = False 

diff = math.fabs(e0.CONSUMED_CREDITS_DL - e1.CONSUMED_CREDITS_DL)
if diff >= var_diff:
    i.cause = "DL"
    i.level = Level.WARN
    matched = True
    
