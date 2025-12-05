/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.samples;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class BasilLeg {

    @Property()
    private long timestamp;

    @Property()
    private String gpsPosition;

    // REMOVED 'Basil basil' reference to fix the Infinite Loop crash

    public BasilLeg() {}

    public BasilLeg(@JsonProperty("timestamp") final long timestamp,
                    @JsonProperty("gpsPosition") final String gpsPosition) {
        this.timestamp = timestamp;
        this.gpsPosition = gpsPosition;
    }

    public long getTimestamp() { return timestamp; }
    public String getGpsPosition() { return gpsPosition; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setGpsPosition(String gpsPosition) { this.gpsPosition = gpsPosition; }
}