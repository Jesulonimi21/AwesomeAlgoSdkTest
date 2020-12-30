package com.awesomeproject;
import android.util.Log;

import com.algorand.algosdk.crypto.Digest;
import com.algorand.algosdk.crypto.Ed25519PublicKey;
import com.algorand.algosdk.crypto.MultisigAddress;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.TxGroup;
import com.algorand.algosdk.util.Encoder;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.algorand.algosdk.v2.client.common.Response;
import com.algorand.algosdk.v2.client.model.*;
import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.transaction.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
public class AlgoRepository {
     String HACKATHON_API_ADDRESS="http://hackathon.algodev.network";
     String HACKATHON_API_TOKEN="ef920e2e7e002953f4b29a8af720efe8e4ecc75ff102b165e0472834b25832c1";
     Integer HACKATHON_API_PORT=9100;
     String PURESTAKE_ALGOD_API_TESTNET_ADDRESS="https://testnet-algorand.api.purestake.io/ps2";
     String PURESTAKE_ALGOD_API_MAINNET_ADDRESS="https://mainnet-algorand.api.purestake.io/ps2";
     String PURESTAKE_API_KEY="ADRySlL0NK5trzqZGAE3q1xxIqlQdSfk1nbHxTNe";
     Integer PURESTAKE_API_PORT=443;
     final String TESTNET="TESTNET";
     final String MAINNET="MAINNET";

     String SANDBOX_ALGOD_ADDRESS="127.0.0.1";
     final Integer SANDBOX_ALGOD_PORT = 4001;
      final String SANDBOX_ALGOD_API_TOKEN ="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

     AlgodClient algodClient;

     public AlgoRepository() throws Exception {
         algodClient=createClientFromHackathonInstance();
     }

    public  byte[] getClearTextPublicKey(Address address) {
        byte[] b = new byte[0];
        try {
            b = address.toVerifyKey().getEncoded();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (b.length != 44) {
            throw new RuntimeException("Generated public key and X.509 prefix is the wrong size");
        } else {
            byte[] raw = new byte[32];
            System.arraycopy(b, 12, raw, 0, 32);
            return raw;
        }
    }
//"soup someone render seven flip woman olive great random color scene physical put tilt say route coin clutch repair goddess rack cousin decide abandon cream"
//    LL2ZGXSHW7FJGOOVSV76RRZ6IGU5ZF4DPCHQ23G7ZLIWCB4WEMIATDBTLY

//    algod account address: FMBQKMGDE7LYNDHCSUPJBXNMMT3HC2TXMIFAJKGBYJQDZN4R3M554N4QTY
//    algod account MNEMONIC: box wear empty voyage scout cheap arrive father wagon correct thought sand planet comfort also patient vast patient tide rather young cinnamon plastic abandon model

//Multisog address
//WN3CZQ3ANKKHA5YZEVGWQHAEPKDMAKAX5EMSLH3LZX3ASY4OFIIXW53UAA

    public  AlgodClient createClientFromHackathonInstance() throws Exception {
        algodClient = (AlgodClient) new AlgodClient(HACKATHON_API_ADDRESS,
                HACKATHON_API_PORT, HACKATHON_API_TOKEN);

            String[] headers = {"X-API-Key"};
            String[] values = {HACKATHON_API_TOKEN};
            NodeStatusResponse status = algodClient.GetStatus().execute().body();
            System.out.println("algod last round: " + status.lastRound);

        return algodClient;
    }

    public  AlgodClient createClientFromPurestakeNode(String net) throws Exception {
        String[] headers = {"X-API-Key"};
        String[] values = {PURESTAKE_API_KEY};
        if(net.equals(TESTNET)){
            algodClient=new AlgodClient("https://testnet-algorand.api.purestake.io/ps2", PURESTAKE_API_PORT,PURESTAKE_API_KEY);


                NodeStatusResponse status = algodClient.GetStatus().execute(headers,values).body();
                System.out.println("algod last round: " + status.lastRound);

            return  algodClient;
        }else if(net==MAINNET){
            algodClient=new AlgodClient(PURESTAKE_ALGOD_API_MAINNET_ADDRESS, PURESTAKE_API_PORT, PURESTAKE_API_KEY);

                NodeStatusResponse status = algodClient.GetStatus().execute(headers,values).body();
                System.out.println("algod last round: " + status.lastRound);

            return  algodClient;
        }   else
            throw new Exception(net +" is not currently supported by this sdk");
    }


     AlgodClient createAlgodClientFromSandBox(){
        algodClient = (AlgodClient) new AlgodClient(SANDBOX_ALGOD_API_TOKEN,
                SANDBOX_ALGOD_PORT, SANDBOX_ALGOD_API_TOKEN );
        return  algodClient;
    }

    public   Account createAccountWithoutMnemonic( ){
        Account myAccount1= null;

        try {
            myAccount1 = new Account();
            System.out.println(" algod account address: " + myAccount1.getAddress());
            System.out.println(" algod account MNEMONIC: " + myAccount1.toMnemonic());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.out.println(" Eror while creating new account "+e);
        }
        return myAccount1;
    }

    public   Account createAccountWithMnemonic(String mnemonic) throws GeneralSecurityException {
        Account myAccount1= null;

            myAccount1 = new Account(mnemonic);
            System.out.println(" algod account address: " + myAccount1.getAddress());
            System.out.println(" algod account MNEMONIC: " + myAccount1.toMnemonic());

        return  myAccount1;
    }

    public   double getWalletBalance(Address address) {
        String[] headers = {"X-API-Key"};
        String[] values = {PURESTAKE_API_KEY};
        com.algorand.algosdk.v2.client.model.Account accountInfo = null;
        try {
            accountInfo = algodClient.AccountInformation(address).execute().body();
            System.out.println("Account Balance: "+ accountInfo.amount+" microAlgos");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return  accountInfo.amount.doubleValue();
    }

    public  Address createAnAddress(String base58PKey) throws NoSuchAlgorithmException {
        return  new Address(base58PKey);
    }

    public  String sendFunds(Account senderAccount,String receiverAdddress,String note,int valueToSend) throws Exception {
        TransactionParametersResponse transactionParametersResponse;
        Transaction transaction;
        String senderAddress=senderAccount.getAddress().toString();
        PendingTransactionResponse pendingTransactionResponse;

            transactionParametersResponse = algodClient.TransactionParams().execute().body();
            transaction= Transaction.PaymentTransactionBuilder().sender(senderAddress)
                    .amount(valueToSend).receiver(new Address(receiverAdddress)).note(note.getBytes()).suggestedParams(transactionParametersResponse).build();
            SignedTransaction signedTransaction=senderAccount.signTransaction(transaction);
            byte[] encodedTransaction= Encoder.encodeToMsgPack(signedTransaction);

            String id=algodClient.RawTransaction().rawtxn(encodedTransaction).execute().body().txId;

            waitForConfirmation(id);
            pendingTransactionResponse=algodClient.PendingTransactionInformation(id).execute().body();
            System.out.println("Transaction information (with notes): " + pendingTransactionResponse.toString());
            return id;

    }


    public  void waitForConfirmation(String txID) throws Exception {
        Long lastRound = algodClient.GetStatus().execute().body().lastRound;
        while (true) {
            try {
                // Check the pending tranactions
                Response<PendingTransactionResponse> pendingInfo = algodClient.PendingTransactionInformation(txID).execute();
                if (pendingInfo.body().confirmedRound != null && pendingInfo.body().confirmedRound > 0) {
                    // Got the completed Transaction
                    System.out.println("Transaction " + txID + " confirmed in round " + pendingInfo.body().confirmedRound);
                    break;
                }
                lastRound++;
                algodClient.WaitForBlock(lastRound).execute();
            } catch (Exception e) {
                throw (e);
            }
        }
    }

    public  MultisigAddress createMultiSigAddress(int version,int threshold,List<Ed25519PublicKey> ed25519PublicKeys) throws NoSuchAlgorithmException {
        List<Ed25519PublicKey> publicKeys = new ArrayList<>();
        Log.d("nimiDebug",ed25519PublicKeys.size()+"Public key size");
        for(Ed25519PublicKey ed25519PublicKey:ed25519PublicKeys){
            publicKeys.add(ed25519PublicKey);
        }
        MultisigAddress msig = new MultisigAddress(version, threshold, publicKeys);

            System.out.println(msig.toAddress());

        return  msig;
    }

    public  Transaction createTransaction(Account senderAccount,String receiverAdddress,String note,int valueToSend,String senderAddress) throws Exception {
        TransactionParametersResponse transactionParametersResponse;
        Transaction transaction=null;
//        String senderAddress=senderAccount.getAddress().toString();
        PendingTransactionResponse pendingTransactionResponse;

            transactionParametersResponse = algodClient.TransactionParams().execute().body();
            transaction= Transaction.PaymentTransactionBuilder().sender(senderAddress)
                    .amount(valueToSend).receiver(new Address(receiverAdddress)).note(note.getBytes()).suggestedParams(transactionParametersResponse).build();

        return transaction;
    }

//    public    SignedTransaction createSignedTransaction(Transaction transaction){
//
//    }
//    public   void submitSignedTransaction(){
//
//    }

    public  SignedTransaction createAMultiSigTransaction(Account account, Transaction transaction,MultisigAddress msig) throws NoSuchAlgorithmException {
        SignedTransaction signedTransaction=null;
        PendingTransactionResponse pendingTransactionResponse;

            signedTransaction = account.signMultisigTransaction(msig, transaction);
//            byte[] encodedTransaction= Encoder.encodeToMsgPack(signedTransaction);
//            String id=algodClient.RawTransaction().rawtxn(encodedTransaction).execute().body().txId;
//            waitForConfirmation(id);
//            pendingTransactionResponse=algodClient.PendingTransactionInformation(id).execute().body();
//            System.out.println("Transaction information (with notes): " + pendingTransactionResponse.toString());

        return  signedTransaction;
    }

    public  SignedTransaction approveMultisigTransaction(Account account, SignedTransaction transaction,MultisigAddress msig) throws NoSuchAlgorithmException, JsonProcessingException {
        SignedTransaction signedTransaction=null;
        PendingTransactionResponse pendingTransactionResponse;

            signedTransaction = account.appendMultisigTransaction(msig, transaction);
            byte[] encodedTxBytes = Encoder.encodeToMsgPack(signedTransaction);
//            String id=  algodClient.RawTransaction().rawtxn(encodedTxBytes).execute().body().txId;
//            waitForConfirmation(id);
//            pendingTransactionResponse = algodClient.PendingTransactionInformation(id).execute().body();
//            System.out.println("Transaction information (with notes): " + pendingTransactionResponse.toString());
//            submitTransactionToNetwork(signedTransaction);

        return signedTransaction;
    }
    public  Double createASA(Account  senderAccount, BigInteger assetTotal, boolean defaultFrozen, String unitName,
                                 String assetName, String url, String assetMetadataHash, Address manager, Address reserve,
                                 Address freeze, Address clawback, Integer decimals) throws Exception {
        TransactionParametersResponse   params = algodClient.TransactionParams().execute().body();
        Transaction tx = Transaction.AssetCreateTransactionBuilder().sender(senderAccount.getAddress()).assetTotal(assetTotal)
                .assetDecimals(decimals).assetUnitName(unitName).assetName(assetName).url(url)
                .metadataHashUTF8(assetMetadataHash).manager(manager).reserve(reserve).freeze(freeze)
                .defaultFrozen(defaultFrozen).clawback(clawback).suggestedParams(params).build();
        SignedTransaction signedTx = senderAccount.signTransaction(tx);
        String id = submitTransactionToNetwork(signedTx);
        System.out.println("Transaction ID: " + id);
        waitForConfirmation(id);
        // Read the transaction
        PendingTransactionResponse pTrx = algodClient.PendingTransactionInformation(id).execute().body();
        // Now that the transaction is confirmed we can get the assetID
        Long assetID = pTrx.assetIndex;
        System.out.println("AssetID = " + assetID);
        printCreatedAsset(senderAccount, assetID,algodClient);
        printAssetHolding(senderAccount, assetID,algodClient);

        return Double.valueOf(assetID);

    }
    public  void printCreatedAsset(Account account, Long assetID,AlgodClient client) throws Exception {
        if (client == null)
            client = createClientFromHackathonInstance();
        String accountInfo = client.AccountInformation(account.getAddress()).execute().toString();
        JSONObject jsonObj = new JSONObject(accountInfo.toString());
        JSONArray jsonArray = (JSONArray) jsonObj.get("created-assets");
        if (jsonArray.length() > 0)
            try {
                for (int i =0;i<jsonArray.length();i++){
                    JSONObject ca = (JSONObject) jsonArray.get(i);
                    Integer myassetIDInt = (Integer) ca.get("index");
                    if (assetID.longValue() == myassetIDInt.longValue()) {
                        System.out.println("Created Asset Info: " + ca.toString(2)); // pretty print
                        break;
                    }
                }
            } catch (Exception e) {
                throw (e);
            }
    }
    // utility function to print asset holding
    public  void printAssetHolding(Account account, Long assetID,AlgodClient client) throws Exception {
        if (client == null)
            client = createClientFromHackathonInstance();
        String accountInfo = client.AccountInformation(account.getAddress()).execute().toString();

        JSONObject jsonObj = new JSONObject(accountInfo.toString());
        JSONArray jsonArray = (JSONArray) jsonObj.get("assets");
        if (jsonArray.length() > 0)
            try {
                for (int i =0;i<jsonArray.length();i++){
                    JSONObject ca = (JSONObject) jsonArray.get(i);
                    Integer myassetIDInt = (Integer) ca.get("asset-id");
                    if (assetID.longValue() == myassetIDInt.longValue()) {
                        System.out.println("Asset Holding Info: " + ca.toString(2)); // pretty print
                        break;
                    }
                }
            } catch (Exception e) {
                throw (e);
            }
    }


    public  String changeAccountManager(Address sender, Account presentManager,Address newManager,Long assetId
            ,Address reserve, Address freeze,Address clawback,Integer decimals,AlgodClient client) throws Exception {
        TransactionParametersResponse  params = client.TransactionParams().execute().body();
        params.fee = (long) 1000;
        Transaction tx = Transaction.AssetConfigureTransactionBuilder().sender(sender).assetIndex(assetId)
                .manager(newManager).reserve(reserve).freeze(freeze).clawback(clawback).suggestedParams(params)
                .build();
        SignedTransaction signedTx = presentManager.signTransaction(tx);
        String id = submitTransactionToNetwork(signedTx);
        System.out.println("Transaction ID: " + id);
        waitForConfirmation(signedTx.transactionID);
        // We can now list the account information for acct3
        // and see that it can accept the new asset
        System.out.println("Account 3 = " + presentManager.getAddress().toString());
        printAssetHolding(presentManager, assetId,client);
        return id;
    }


    public   String optInToReceiveAsa(Long assetId,AlgodClient client,Account sender) throws Exception {
        TransactionParametersResponse  params = client.TransactionParams().execute().body();
        params.fee = (long) 1000;
        Transaction    tx = Transaction.AssetAcceptTransactionBuilder().acceptingAccount(sender.getAddress()).assetIndex(assetId)
                .suggestedParams(params).build();
        SignedTransaction  signedTx = sender.signTransaction(tx);
        String id = submitTransactionToNetwork(signedTx);
        System.out.println("Transaction ID: " + id);
        waitForConfirmation(signedTx.transactionID);
        System.out.println("Account 3 = " + sender.getAddress().toString());
        printAssetHolding(sender, assetId,client);

        return  id;
    }

    public String transferAsa(Long assetId, AlgodClient client, Account sender, Address receiver, BigInteger assetAmount ) throws Exception {
        TransactionParametersResponse  params = client.TransactionParams().execute().body();
        params.fee = (long) 1000;
        Transaction   tx = Transaction.AssetTransferTransactionBuilder().sender(sender.getAddress()).assetReceiver(receiver)
                .assetAmount(assetAmount).assetIndex(assetId).suggestedParams(params).build();
        SignedTransaction signedTx = sender.signTransaction(tx);
        String id = submitTransactionToNetwork(signedTx);
        System.out.println("Transaction ID: " + id);
        waitForConfirmation(signedTx.transactionID);
        System.out.println("Account 3  = " + sender.getAddress().toString());
        printAssetHolding(sender, assetId,client);
        System.out.println("Account 1  = " + sender.getAddress().toString());
        printAssetHolding(sender, assetId,client);
        return id;
    }


    public String freezeAsa(Long assetId, Boolean freezeState, Account sender, Account manager, Address addressToFreeze, AlgodClient client) throws Exception {
        TransactionParametersResponse  params = client.TransactionParams().execute().body();
        params.fee = (long) 1000;
        Transaction  tx = Transaction.AssetFreezeTransactionBuilder().sender(sender.getAddress()).freezeTarget(addressToFreeze)
                .freezeState(freezeState).assetIndex(assetId).suggestedParams(params).build();
        SignedTransaction  signedTx = manager.signTransaction(tx);
        String id = submitTransactionToNetwork(signedTx);
        System.out.println("Transaction ID: " + id);
        waitForConfirmation(signedTx.transactionID);
        System.out.println("Account 3 = " + addressToFreeze);
//        printAssetHolding(addressToFreeze, assetId,client);
        return id;
    }


    public   String revokeAsa(Long assetId,Account sender,Account clawback,
                                  Address addressToRevoke,AlgodClient client,Long amountToRevoke,Address receiver) throws Exception {
        TransactionParametersResponse   params = client.TransactionParams().execute().body();
        params.fee = (long) 1000;
        BigInteger  assetAmount = BigInteger.valueOf(amountToRevoke);
        Transaction  tx = Transaction.AssetClawbackTransactionBuilder().sender(sender.getAddress())
                .assetClawbackFrom(addressToRevoke).assetReceiver(receiver).assetAmount(assetAmount)
                .assetIndex(assetId).suggestedParams(params).build();
        SignedTransaction  signedTx = clawback.signTransaction(tx);
        String id = submitTransactionToNetwork(signedTx);
        System.out.println("Transaction ID: " + id);
        waitForConfirmation(signedTx.transactionID);
//        System.out.println("Account 3 = " + addressToFreeze);
        return  id;
    }

    public String destroyASA(Long assetId, Account manager, AlgodClient client) throws Exception {
        TransactionParametersResponse   params = client.TransactionParams().execute().body();
        params.fee = (long) 1000;
        Transaction   tx = Transaction.AssetDestroyTransactionBuilder().sender(manager.getAddress()).assetIndex(assetId)
                .suggestedParams(params).build();
        SignedTransaction  signedTx = manager.signTransaction(tx);
        String id = submitTransactionToNetwork(signedTx);
//        System.out.println("Transaction ID: " + id);
        waitForConfirmation(signedTx.transactionID);

        System.out.println("Nothing should print after this, Account 1 asset is sucessfully deleted");
        printAssetHolding(manager, assetId,client);
        printCreatedAsset(manager, assetId,client);
        return id;
    }
    public    String  submitTransactionToNetwork(SignedTransaction signedTransaction) throws Exception {
        PendingTransactionResponse pendingTransactionResponse=null;
        byte[] encodedTxBytes = new byte[0];
        encodedTxBytes = Encoder.encodeToMsgPack(signedTransaction);
        String id=  algodClient.RawTransaction().rawtxn(encodedTxBytes).execute().body().txId;

        waitForConfirmation(id);
        pendingTransactionResponse = algodClient.PendingTransactionInformation(id).execute().body();
        System.out.println("Transaction information (with notes): " + pendingTransactionResponse.toString());
        return id;
    }


    public  Transaction[] groupTransactions(Transaction[] transactions) throws IOException {
        Digest gid = TxGroup.computeGroupID(transactions);
        for(int i=0;i< transactions.length;i++){
            transactions[i].assignGroupID(gid);
        }
        return transactions;
    }

    public   SignedTransaction signTransaction(Account account,Transaction transaction){
        SignedTransaction signedTransaction=null;
        try {
            signedTransaction=   account.signTransaction(transaction);
            return signedTransaction;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return signedTransaction;
    }
    public String assembleTransactionGroup(SignedTransaction[] transactions, AlgodClient algodClient) throws Exception {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream( );
        for(int i=0;i< transactions.length;i++){
            byte[] encodedTxBytes1 = Encoder.encodeToMsgPack(transactions[i]);
            byteOutputStream.write(encodedTxBytes1);
        }

        byte groupTransactionBytes[] = byteOutputStream.toByteArray();

        String id =algodClient .RawTransaction().rawtxn(groupTransactionBytes).execute().body().txId;
        System.out.println("Successfully sent tx with ID: " + id);
        waitForConfirmation(id);
        return id;
    }

public AlgodClient getAlgodClient(){
         return  this.algodClient;
}

}
