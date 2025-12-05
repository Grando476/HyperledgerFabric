/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples;

import java.util.ArrayList;
import java.util.List;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import com.owlike.genson.Genson;

@Contract(
        name = "basil",
        info = @Info(
                title = "Basil Contract",
                description = "Smart Contract for tracing basil plants",
                version = "1.0",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "student@example.com",
                        name = "Student Name",
                        url = "https://example.com")))
@Default
public final class BasilContract implements ContractInterface {

    private final Genson genson = new Genson();

    private enum BasilErrors {
        BASIL_NOT_FOUND,
        BASIL_ALREADY_EXISTS
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Basil CreateTracking(final Context ctx, final String qr, final String extraInfo) {
        ChaincodeStub stub = ctx.getStub();
        String basilState = stub.getStringState(qr);
        if (basilState != null && !basilState.isEmpty()) {
            String errorMessage = String.format("Basil with QR %s already exists", qr);
            throw new ChaincodeException(errorMessage, BasilErrors.BASIL_ALREADY_EXISTS.toString());
        }

        String owner = ctx.getClientIdentity().getMSPID();

        // 1. Create Basil with null leg initially
        Basil basil = new Basil(qr, extraInfo, owner, null);
        
        // 2. FIXED: Use getEpochSecond() instead of getSeconds()
        long timestamp = ctx.getStub().getTxTimestamp().getEpochSecond(); 
        
        // 3. Create Genesis Leg
        BasilLeg genesisLeg = new BasilLeg(timestamp, "Origin");
        basil.setCurrentLeg(genesisLeg);

        String newBasilState = genson.serialize(basil);
        stub.putStringState(qr, newBasilState);
        return basil;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Basil GetActualTracking(final Context ctx, final String qr) {
        ChaincodeStub stub = ctx.getStub();
        String basilState = stub.getStringState(qr);
        if (basilState == null || basilState.isEmpty()) {
            String errorMessage = String.format("Basil with QR %s does not exist", qr);
            throw new ChaincodeException(errorMessage, BasilErrors.BASIL_NOT_FOUND.toString());
        }
        return genson.deserialize(basilState, Basil.class);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Basil UpdateTracking(final Context ctx, final String qr, final String newExtraInfo, 
                                final String gpsPosition, final long timestamp) {
        ChaincodeStub stub = ctx.getStub();
        String basilState = stub.getStringState(qr);

        if (basilState == null || basilState.isEmpty()) {
             throw new ChaincodeException("Basil not found", BasilErrors.BASIL_NOT_FOUND.toString());
        }

        Basil basil = genson.deserialize(basilState, Basil.class);

        // Permission Check
        String callerMsp = ctx.getClientIdentity().getMSPID();
        if (!basil.getOwner().equals(callerMsp)) {
            String errorMessage = String.format("Permission Denied: Caller %s is not the owner of %s", callerMsp, qr);
            throw new ChaincodeException(errorMessage);
        }

        // Update logic
        basil.setExtraInfo(newExtraInfo);
        
        // Create new leg
        BasilLeg newLeg = new BasilLeg(timestamp, gpsPosition);
        basil.setCurrentLeg(newLeg);

        String newBasilJSON = genson.serialize(basil);
        stub.putStringState(qr, newBasilJSON);

        return basil;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Basil TransferTracking(final Context ctx, final String qr, final String newOwnerMsp) {
        ChaincodeStub stub = ctx.getStub();
        String basilState = stub.getStringState(qr);

        if (basilState == null || basilState.isEmpty()) {
            throw new ChaincodeException("Basil not found", BasilErrors.BASIL_NOT_FOUND.toString());
        }

        Basil basil = genson.deserialize(basilState, Basil.class);

        String callerMsp = ctx.getClientIdentity().getMSPID();
        if (!basil.getOwner().equals(callerMsp)) {
             String errorMessage = String.format("Permission Denied: Caller %s is not the owner of %s", callerMsp, qr);
            throw new ChaincodeException(errorMessage);
        }

        // FIXED: Added the 4th argument (basil.getCurrentLeg()) to preserve the current location
        Basil transferredBasil = new Basil(qr, basil.getExtraInfo(), newOwnerMsp, basil.getCurrentLeg());

        String newJSON = genson.serialize(transferredBasil);
        stub.putStringState(qr, newJSON);

        return transferredBasil;
    }
    
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteTracking(final Context ctx, final String qr) {
        ChaincodeStub stub = ctx.getStub();
        String basilState = stub.getStringState(qr);

        if (basilState == null || basilState.isEmpty()) {
            throw new ChaincodeException("Basil not found", BasilErrors.BASIL_NOT_FOUND.toString());
        }

        Basil basil = genson.deserialize(basilState, Basil.class);

        String callerMsp = ctx.getClientIdentity().getMSPID();
        if (!basil.getOwner().equals(callerMsp)) {
            String errorMessage = String.format("Permission Denied: Caller %s is not the owner of %s", callerMsp, qr);
            throw new ChaincodeException(errorMessage);
        }

        stub.delState(qr);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetHistory(final Context ctx, final String qr) {
        ChaincodeStub stub = ctx.getStub();
        QueryResultsIterator<KeyModification> history = stub.getHistoryForKey(qr);
        List<BasilLeg> historyList = new ArrayList<>();

        for (KeyModification modification : history) {
            String basilJSON = modification.getStringValue();
            // Handle deletes (value is null/empty)
            // Handle deletes (value is null/empty)
            if (basilJSON == null || basilJSON.isEmpty()) {
                historyList.add(new BasilLeg(modification.getTimestamp().getEpochSecond(), "DELETED")); // <--- FIXED
                continue;
            }

            Basil basilState = genson.deserialize(basilJSON, Basil.class);
            long timestamp = modification.getTimestamp().getEpochSecond();
            
            // Extract GPS from the nested BasilLeg
            String historicalGps = "Unknown";
            if (basilState.getCurrentLeg() != null) {
                historicalGps = basilState.getCurrentLeg().getGpsPosition();
            }

            historyList.add(new BasilLeg(timestamp, historicalGps));;
        }
        return genson.serialize(historyList);
    }
}