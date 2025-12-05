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

    @Property()
    private Basil basil; // Reference to the Basil state at this leg

    // Default constructor
    public BasilLeg() {
        this.timestamp = 0;
        this.gpsPosition = "";
        this.basil = null;
    }

    public BasilLeg(@JsonProperty("timestamp") final long timestamp,
                    @JsonProperty("gpsPosition") final String gpsPosition,
                    @JsonProperty("basil") final Basil basil) {
        this.timestamp = timestamp;
        this.gpsPosition = gpsPosition;
        this.basil = basil;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getGpsPosition() {
        return gpsPosition;
    }

    public Basil getBasil() {
        return basil;
    }

    public void setTimestamp(long timestamp) { 
        this.timestamp = timestamp; 
    }
    public void setGpsPosition(String gpsPosition) { 
        this.gpsPosition = gpsPosition; 
    }
    public void setBasil(Basil basil) { 
        this.basil = basil; 
    }

    @Override
    public String toString() {
        return "BasilLeg{" +
                "timestamp=" + timestamp +
                ", gpsPosition='" + gpsPosition + '\'' +
                ", basil=" + basil +
                '}';
    }
}