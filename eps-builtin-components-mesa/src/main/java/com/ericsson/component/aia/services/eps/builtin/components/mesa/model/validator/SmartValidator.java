package com.ericsson.component.aia.services.eps.builtin.components.mesa.model.validator;

import java.util.List;

/**
 * The Class SmartValidator implements ${@link Validator}.
 */
public final class SmartValidator implements Validator {

    @Override
    public List<Message> validate(final Object model) {
        return null;
    }

    // private List<Message> validate(Policy policy) {
    // for(RuleGroup group : policy.getGroups()) {
    // Subscription subscription = group.getSubscription();
    // Set<EventRef> set = subscription.get();
    // for(EventRef ref : set) {
    // // verify here that we can check that events we need will
    // // actually reach us, depending either on static or
    // // dynamic configuration...
    // }
    // }
    // return null;
    // }
    //
    // private List<Message> validate(PolicyConf conf) {
    // return null;
    // }
}
