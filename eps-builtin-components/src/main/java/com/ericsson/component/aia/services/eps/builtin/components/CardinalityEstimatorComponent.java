/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.services.eps.builtin.components;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.component.aia.itpf.common.Controllable;
import com.ericsson.component.aia.itpf.common.event.ControlEvent;
import com.ericsson.component.aia.itpf.common.event.handler.AbstractEventHandler;
import com.ericsson.component.aia.itpf.common.event.handler.EventInputHandler;

/**
 * Builtin component used for cardinality estimation based on the HyperLogLog algorithm http://algo.inria.fr/flajolet/Publications/FlFuGaMe07.pdf.
 *
 */
public class CardinalityEstimatorComponent extends AbstractEventHandler implements EventInputHandler, Controllable {

    public static final int CONTROL_EVENT_GET_CARDINALITY = 100001;
    public static final int CONTROL_EVENT_GET_BUCKETS = 100002;

    private HyperLogLog hll;

    @Override
    public void onEvent(final Object inputEvent) {
        if (inputEvent instanceof Long) {
            final Long inputEv = (Long) inputEvent;
            hll.update(inputEv);
        } else if (inputEvent instanceof List) {
            final List<Integer> buckets = (List<Integer>) inputEvent;
            for (int j = 0; j < buckets.size(); j++) {
                final ArrayList<Integer> arr = (ArrayList<Integer>) buckets.toArray()[j];
                final int[] arrBuckets = new int[arr.size()];
                for (int i = 0; i < arrBuckets.length; i++) {
                    arrBuckets[i] = arr.get(i);
                }
                hll.merge(arrBuckets);
            }
        } else {
            log.warn("Event can be only of type Long or List.");
        }
    }

    @Override
    public void react(final ControlEvent controlEvent) {
        if (controlEvent.getType() == CONTROL_EVENT_GET_CARDINALITY) {
            final long cardinality = hll.cardinality();
            log.debug("Cardinality: {}", cardinality);
            hll.clear();
            sendToAllSubscribers(cardinality);
        } else if (controlEvent.getType() == CONTROL_EVENT_GET_BUCKETS) {
            final ArrayList<Integer> buckets = new ArrayList<Integer>(hll.getBuckets().length);
            for (final int bucket : hll.getBuckets()) {
                buckets.add(bucket);
            }
            sendToAllSubscribers(buckets);
        }
    }

    @Override
    protected void doInit() {
        this.hll = new HyperLogLog(8);
    }

    /**
     * Encapsulates the HyperLogLog algorithm
     */
    public class HyperLogLog {
        private final int log2m;
        private final double alphaMM;
        private final int[] bucketM;

        /**
         *
         * @param log2m
         *            the log2m value to use
         */
        public HyperLogLog(final int log2m) {
            if ((log2m < 0) || (log2m > 30)) {
                throw new IllegalArgumentException("log2m argument is " + log2m + " and is outside the range [0, 30]");
            }

            this.log2m = log2m;
            final int mVal = 1 << this.log2m;
            this.bucketM = new int[mVal];

            initM();

            switch (log2m) {
                case 4:
                    alphaMM = 0.673 * mVal * mVal;
                    break;
                case 5:
                    alphaMM = 0.697 * mVal * mVal;
                    break;
                case 6:
                    alphaMM = 0.709 * mVal * mVal;
                    break;
                default:
                    alphaMM = (0.7213 / (1 + (1.079 / mVal))) * mVal * mVal;
            }
        }

        private void initM() {
            for (int i = 0; i < this.bucketM.length; i++) {
                this.bucketM[i] = Integer.MIN_VALUE;
            }
        }

        /**
         *
         * @return the buckets
         */
        public int[] getBuckets() {
            return this.bucketM;
        }

        /**
         * clears the buckets
         *
         * @return true
         */
        public boolean clear() {
            initM();
            return true;
        }

        /**
         * hashes the input value and updates the bucket
         *
         * @param value
         *            the value to update
         * @return true
         */
        public boolean update(final Long value) {
            final int hashedValue = hashLong(value);
            final int jIdx = hashedValue >>> (Integer.SIZE - this.log2m);
            final int pVal = Long.numberOfLeadingZeros((hashedValue << this.log2m) | (1 << (this.log2m - 1))) + 1;

            if (this.bucketM[jIdx] < pVal) {
                this.bucketM[jIdx] = pVal;
            }
            return true;
        }

        /**
         *
         * @return the cardinality
         */
        public long cardinality() {
            double Sum = 0;
            final int count = this.bucketM.length;
            double zeros = 0.0;
            for (int j = 0; j < count; j++) {
                final int val = this.bucketM[j];
                Sum += 1.0 / (1 << val);
                if (val == 0) {
                    zeros++;
                }
            }

            final double estimate = alphaMM * (1 / Sum);

            if (estimate <= ((5.0 / 2.0) * count)) {
                // Small Range Estimate
                return Math.round(count * Math.log(count / zeros));
            } else {
                return Math.round(estimate);
            }
        }

        /**
         * Merges the input HyperLogLog with this.
         *
         * Compares the buckets of both HyperLogLog instances, and keeps the bucket with the largest value
         *
         * @param hll
         *            the HyperLogLog to merge
         * @return the merged HyperLogLog
         */
        public HyperLogLog merge(final HyperLogLog hll) {
            final int[] bucketM2 = hll.getBuckets();
            if (this.bucketM.length != bucketM2.length) {
                throw new IllegalArgumentException("The number of buckets doesn't match!");
            }

            final int count = this.bucketM.length;

            for (int j = 0; j < count; j++) {
                if (this.bucketM[j] < bucketM2[j]) {
                    this.bucketM[j] = bucketM2[j];
                }
            }
            return this;
        }

        private HyperLogLog merge(final int[] bucketM2) {
            if (this.bucketM.length != bucketM2.length) {
                throw new IllegalArgumentException("The number of buckets doesn't match!");
            }

            final int count = this.bucketM.length;

            for (int j = 0; j < count; j++) {
                if (this.bucketM[j] < bucketM2[j]) {
                    this.bucketM[j] = bucketM2[j];
                }
            }
            return this;
        }

        /**
         * Returns a hash code.
         *
         * @param data
         *            the value to hash
         * @return a hash code value for the data
         */
        public int hashLong(final long data) {
            final int mVal = 0x5bd1e995;
            final int rVal = 24;

            int hashVal = 0;

            int kVal = (int) data * mVal;
            kVal ^= kVal >>> rVal;
            hashVal ^= kVal * mVal;

            kVal = (int) (data >> 32) * mVal;
            kVal ^= kVal >>> rVal;
            hashVal *= mVal;
            hashVal ^= kVal * mVal;

            hashVal ^= hashVal >>> 13;
            hashVal *= mVal;
            hashVal ^= hashVal >>> 15;

            return hashVal;
        }

    }

}