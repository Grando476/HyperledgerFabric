/*
 * SPDX-License-Identifier: Apache-2.0
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.identity.Identities;
import org.hyperledger.fabric.client.identity.Signers;
import org.hyperledger.fabric.client.identity.X509Identity;

import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;

public final class App {

    // IMPORTANT: Check that this path matches your structure! 
    // Usually it is: ../../fabric-samples/test-network
    private static final Path PATH_TO_TEST_NETWORK = Paths.get("..", "..", "fabric-samples", "test-network");

    private static final String CHANNEL_NAME = "mychannel";
    private static final String CHAINCODE_NAME = "basil"; // Matches the name in BasilContract.java

    private static final String PEER_ENDPOINT = "localhost:7051";
    private static final String OVERRIDE_AUTH = "peer0.org1.example.com";

    public static void main(final String[] args) throws Exception {

        // --- 1. SETUP CONNECTION credentials (TLS) ---
        ChannelCredentials credentials = TlsChannelCredentials.newBuilder()
                .trustManager(PATH_TO_TEST_NETWORK.resolve(Paths.get(
                        "organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt"))
                        .toFile())
                .build();

        ManagedChannel channel = Grpc.newChannelBuilder(PEER_ENDPOINT, credentials)
                .overrideAuthority(OVERRIDE_AUTH)
                .build();

        // --- 2. SETUP IDENTITIES (Org1 and Org2) ---
        
        // Identity for Org1 (Pittaluga)
        Gateway.Builder builderOrg1 = Gateway.newInstance()
                .identity(new X509Identity("Org1MSP",
                        Identities.readX509Certificate(Files.newBufferedReader(PATH_TO_TEST_NETWORK.resolve(Paths.get(
                                "organizations/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/signcerts/cert.pem"))))))
                .signer(Signers.newPrivateKeySigner(Identities.readPrivateKey(Files.newBufferedReader(Files.list(
                        PATH_TO_TEST_NETWORK.resolve(Paths.get(
                                "organizations/peerOrganizations/org1.example.com/users/User1@org1.example.com/msp/keystore")))
                        .findFirst().orElseThrow()))))
                .connection(channel)
                .commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));

        // Identity for Org2 (Supermarket)
        Gateway.Builder builderOrg2 = Gateway.newInstance()
                .identity(new X509Identity("Org2MSP",
                        Identities.readX509Certificate(Files.newBufferedReader(PATH_TO_TEST_NETWORK.resolve(Paths.get(
                                "organizations/peerOrganizations/org2.example.com/users/User1@org2.example.com/msp/signcerts/cert.pem"))))))
                .signer(Signers.newPrivateKeySigner(Identities.readPrivateKey(Files.newBufferedReader(Files.list(
                        PATH_TO_TEST_NETWORK.resolve(Paths.get(
                                "organizations/peerOrganizations/org2.example.com/users/User1@org2.example.com/msp/keystore")))
                        .findFirst().orElseThrow()))))
                .connection(channel)
                .commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));


        // --- 3. START APPLICATION LOOP ---
        try (Gateway gatewayOrg1 = builderOrg1.connect();
             Gateway gatewayOrg2 = builderOrg2.connect()) {

            // Get Contract references for both organizations
            Contract contractOrg1 = gatewayOrg1.getNetwork(CHANNEL_NAME).getContract(CHAINCODE_NAME);
            Contract contractOrg2 = gatewayOrg2.getNetwork(CHANNEL_NAME).getContract(CHAINCODE_NAME);

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n--- BASIL TRACKING SYSTEM ---");
                System.out.println("Who are you?");
                System.out.println("0: Pittaluga & fratelli (Org1)");
                System.out.println("1: Supermarket (Org2)");
                System.out.print("Select Organization: ");
                String orgChoice = scanner.nextLine();

                Contract activeContract;
                String activeOrgName;

                if (orgChoice.equals("0")) {
                    activeContract = contractOrg1;
                    activeOrgName = "Org1 (Pittaluga)";
                } else if (orgChoice.equals("1")) {
                    activeContract = contractOrg2;
                    activeOrgName = "Org2 (Supermarket)";
                } else {
                    System.out.println("Invalid organization.");
                    continue;
                }

                System.out.println("\nActing as: " + activeOrgName);
                System.out.println("Select Operation:");
                System.out.println("1: Create Tracking (Create Plant)");
                System.out.println("2: Get Actual Tracking (Read Info)");
                System.out.println("3: Update Tracking (Add Leg/Info)");
                System.out.println("4: Transfer Ownership");
                System.out.println("5: Get History");
				System.out.println("6: Delete Tracking");
                System.out.println("exit: Quit App");
                System.out.print("Choice: ");
                String txChoice = scanner.nextLine();

                if (txChoice.equals("exit")) break;

                try {
                    byte[] result;
                    String qr, info;
                    
                    switch (txChoice) {
                        case "1": // Create
                            System.out.print("Enter QR Code: ");
                            qr = scanner.nextLine();
                            System.out.print("Enter Extra Info (e.g. 'Genovese'): ");
                            info = scanner.nextLine();
                            
                            System.out.println("Submitting transaction...");
                            result = activeContract.submitTransaction("CreateTracking", qr, info);
                            System.out.println("SUCCESS! Result: " + new String(result, StandardCharsets.UTF_8));
                            break;

                        case "2": // Get
                            System.out.print("Enter QR Code: ");
                            qr = scanner.nextLine();
                            
                            System.out.println("Reading ledger...");
                            result = activeContract.evaluateTransaction("GetActualTracking", qr);
                            System.out.println("DATA: " + new String(result, StandardCharsets.UTF_8));
                            break;

                        case "3": // Update
                            System.out.print("Enter QR Code: ");
                            qr = scanner.nextLine();
                            System.out.print("Enter New Info: ");
                            info = scanner.nextLine();
                            System.out.print("Enter GPS Position: ");
                            String gps = scanner.nextLine();
                            String timestamp = String.valueOf(System.currentTimeMillis());

                            System.out.println("Updating...");
                            // Note: We pass arguments as Strings. Longs must be converted to String.
                            result = activeContract.submitTransaction("UpdateTracking", qr, info, gps, timestamp);
                            System.out.println("UPDATED: " + new String(result, StandardCharsets.UTF_8));
                            break;

                        case "4": // Transfer
                            System.out.print("Enter QR Code: ");
                            qr = scanner.nextLine();
                            System.out.println("Enter New Owner MSP (e.g. 'Org1MSP' or 'Org2MSP'): ");
                            String newOwner = scanner.nextLine();

                            System.out.println("Transferring...");
                            result = activeContract.submitTransaction("TransferTracking", qr, newOwner);
                            System.out.println("TRANSFERRED: " + new String(result, StandardCharsets.UTF_8));
                            break;

                        case "5": // History
                            System.out.print("Enter QR Code: ");
                            qr = scanner.nextLine();
                            
                            System.out.println("Fetching history...");
                            result = activeContract.evaluateTransaction("GetHistory", qr);
                            System.out.println("HISTORY: " + new String(result, StandardCharsets.UTF_8));
                            break;
						case "6": // Delete
							System.out.print("Enter QR Code to DELETE: ");
							qr = scanner.nextLine();

							System.out.println("Deleting...");
							// submitTransaction returns a byte array, but void methods return empty bytes
							activeContract.submitTransaction("DeleteTracking", qr);
							System.out.println("DELETED SUCCESSFULLY!");
							break;	
                        default:
                            System.out.println("Invalid option.");
                    }
                } catch (Exception e) {
                    System.err.println("ERROR: " + e.getMessage());
                }
            }
        }
    }
}