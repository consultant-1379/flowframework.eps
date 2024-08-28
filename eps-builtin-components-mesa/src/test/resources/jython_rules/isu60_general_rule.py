import com.ericsson.component.aia.services.eps.monitoring.core.rule.thr.ThresholdRule as Rule
import com.ericsson.component.aia.opdashboard.incident.explore.jython.SimplePolicy as Policy
import com.ericsson.component.aia.opdashboard.incident.explore.jython.policy.Kind as Kind

import com.ericsson.component.aia.services.eps.builtin.components.mesa.test.INTERNAL_SYSTEM_UTILIZATION_60MIN as INTERNAL_SYSTEM_UTILIZATION_60MIN


class UplinkCreditsThresholdRule(Rule):
    def getName(self):
        return "INTERNAL_SYSTEM_UTILIZATION_60MIN CONSUMED_CREDITS_UL_PERCENT_AVG thresold rule"
        
    def eval(self):
        if e.CONSUMED_CREDITS_UL_PERCENT_AVG >= 60:
            i0.level = Kind.WARN
            i0.cause = "unknown"
            i0.subCause = "unknown"
            return True
        return False
    
class DownlinkCreditsThresholdRule(Rule):
    def getName(self):
        return "INTERNAL_SYSTEM_UTILIZATION_60MIN CONSUMED_CREDITS_UL_PERCENT_AVG thresold rule"
        
    def evaluate(self):
        if e.CONSUMED_CREDITS_DL_PERCENT_AVG >= 60:
            i1.level = Kind.WARN
            i1.cause = "unknown"
            i1.subCause = "unknown"
            return True
        return False

policy = Policy("eniq.events.wcdma", "wcdma_ran_policy_1")
policy.append(UplinkCreditsThresholdRule())
policy.append(DownlinkCreditsThresholdRule())