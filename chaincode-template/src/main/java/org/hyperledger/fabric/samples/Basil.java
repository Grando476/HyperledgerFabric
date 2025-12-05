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
    private String qr; // The unique ID (QR code)

    @Property()
    private String extraInfo; // Info like temperature, humidity, etc.

    @Property()
    private String owner; // The MSP ID of the owning organization

    // Default constructor required for deserialization
    public Basil() {

    }

    public Basil(@JsonProperty("qr") final String qr, 
                 @JsonProperty("extraInfo") final String extraInfo,
                 @JsonProperty("owner") final String owner) {
        this.qr = qr;
        this.extraInfo = extraInfo;
        this.owner = owner;
    }
    
    public String getQr() {
        return qr;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public String getOwner() {
        return owner;
    }

    public void setQr(String qr) { 
        this.qr = qr; 
    }
    public void setExtraInfo(String extraInfo) { 
        this.extraInfo = extraInfo; 
    }
    public void setOwner(String owner) { 
        this.owner = owner; 
    }

    @Override
    public String toString() {
        return "Basil{" +
                "qr='" + qr + '\'' +
                ", extraInfo='" + extraInfo + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }
}
