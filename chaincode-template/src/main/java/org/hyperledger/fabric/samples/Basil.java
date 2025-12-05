/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.fabric.samples;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Basil {

    @Property()
    private String qr;

    @Property()
    private String extraInfo;

    @Property()
    private String owner;

    // --- NEW: Basil holds the current Leg ---
    @Property()
    private BasilLeg currentLeg;

    public Basil() {}

    public Basil(@JsonProperty("qr") final String qr, 
                 @JsonProperty("extraInfo") final String extraInfo,
                 @JsonProperty("owner") final String owner,
                 @JsonProperty("currentLeg") final BasilLeg currentLeg) {
        this.qr = qr;
        this.extraInfo = extraInfo;
        this.owner = owner;
        this.currentLeg = currentLeg;
    }

    // --- GETTERS ---
    public String getQr() { return qr; }
    public String getExtraInfo() { return extraInfo; }
    public String getOwner() { return owner; }
    
    // This gets saved to the Ledger!
    public BasilLeg getCurrentLeg() { return currentLeg; }

    // --- SETTERS ---
    public void setQr(String qr) { this.qr = qr; }
    public void setExtraInfo(String extraInfo) { this.extraInfo = extraInfo; }
    public void setOwner(String owner) { this.owner = owner; }
    public void setCurrentLeg(BasilLeg currentLeg) { this.currentLeg = currentLeg; }
}