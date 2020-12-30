package com.awesomeproject;

import android.telecom.Call;
import android.util.Log;

import androidx.annotation.NonNull;

import com.algorand.algosdk.account.Account;
import com.algorand.algosdk.crypto.Address;
import com.algorand.algosdk.crypto.Ed25519PublicKey;
import com.algorand.algosdk.crypto.MultisigAddress;
import com.algorand.algosdk.transaction.SignedTransaction;
import com.algorand.algosdk.transaction.Transaction;
import com.algorand.algosdk.v2.client.common.AlgodClient;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AlgoModule extends ReactContextBaseJavaModule{
    public static final String TAG=AlgoModule.class.getSimpleName();
    AlgoRepository algoRepository;
    Account account;
    MultisigAddress multisigAddress;
    AlgodClient algodClient;
    List<String> transactionList=new ArrayList<>();
    private List<SignedTransaction> signedTransaction;
    private Transaction[] groupedTransactions;


    public AlgoModule(ReactApplicationContext reactApplicationContext){
        super(reactApplicationContext);
        Security.removeProvider("BC");
        Security.insertProviderAt(new BouncyCastleProvider(), 0);
        try {
         algoRepository=new AlgoRepository();
         algodClient=   algoRepository.getAlgodClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @NonNull
    @Override
    public String getName() {
        return "AlgoModule";
    }




    @ReactMethod
    public void createNewAccount(Callback callback){
       account= algoRepository.createAccountWithoutMnemonic();
        WritableMap writableMap= Arguments.createMap();
        writableMap.putString("mnemonic",account.toMnemonic());
        writableMap.putString("publicAddress",account.getAddress().toString());
    callback.invoke(writableMap);
    }

    @ReactMethod
    public void recoverAccount(String seedWords,Callback callback){
        try {
             account=algoRepository.createAccountWithMnemonic(seedWords);
            WritableMap writableMap= Arguments.createMap();
            writableMap.putString("mnemonic",account.toMnemonic());
            writableMap.putString("publicAddress",account.getAddress().toString());
            callback.invoke(null,writableMap);
        } catch (GeneralSecurityException e) {
            callback.invoke(e.getMessage(),null);
            e.printStackTrace();
        }

    }


    @ReactMethod
    public void getAccountBalance(String address,Callback callback){
        try {
            Address address1=algoRepository.createAnAddress(address);
            double balance=algoRepository.getWalletBalance(address1);
            callback.invoke(null,balance);
        } catch (NoSuchAlgorithmException e) {
            callback.invoke(e.getMessage(),null);
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void sendFunds(String receiverAddress,String note,int amount,Callback callback){
        if(account==null){
            callback.invoke("Account was null, make sure createNewAccount or recoverAccount has been called at least once ",null);
            return;
        }
       new Thread(new Runnable() {
           @Override
           public void run() {
               try {
                   String id=  algoRepository.sendFunds(account,receiverAddress,note,amount);
                   callback.invoke(null,id);
               } catch (Exception e) {
                   callback.invoke(e.getMessage(),null);
                   e.printStackTrace();
               }
           }
       }).start();

    }


    @ReactMethod
    public void  createMultiSignatureAddress(int version, int threshold, ReadableArray readableArray,Callback callback){
        List<Ed25519PublicKey> ed25519PublicKeys=new ArrayList<>();
        Log.d("nimiDebug",readableArray.size()+"Readable array size");
        for(int i=0;i<readableArray.size();i++){
            try {
                ed25519PublicKeys.add(new Ed25519PublicKey(algoRepository.getClearTextPublicKey( new Address(readableArray.getString(i)))));
                Log.d("nimiDebug",readableArray.getString(i));
            } catch (Exception e) {
                callback.invoke(e.getMessage(),null);
                e.printStackTrace();
            }
        }
            try {
                 multisigAddress=algoRepository.createMultiSigAddress(version,threshold,ed25519PublicKeys);
                callback.invoke(null,multisigAddress.toAddress().toString());
            } catch (NoSuchAlgorithmException e) {
                callback.invoke(e.getMessage(),null);
                e.printStackTrace();

        }


    }
    @ReactMethod
    public void createMultisigTransaction(String receiverAdddress,String note,int valueToSend,Callback callback){
        if(account==null){
            callback.invoke("Account was null",null);
            return;
        }
        if(multisigAddress==null){
            callback.invoke("Multisignature Address was null",null);
            return;
        }
        try {
            Transaction transaction=algoRepository.createTransaction(account,receiverAdddress,note,valueToSend,multisigAddress.toString());
            SignedTransaction signedTransaction=algoRepository.createAMultiSigTransaction(account,transaction,multisigAddress);
            Gson gson=new Gson();
            String jsonString=gson.toJson(signedTransaction);
            callback.invoke(null,jsonString);
        } catch (Exception e) {
            callback.invoke(e.getMessage(),null);
            e.printStackTrace();
        }
    }

    @ReactMethod
    public  void approveMultiSigTransaction(String signedTransactionString,Callback callback){
            
        if(account==null){
            callback.invoke("Account was null",null);
            return;
        }
        if(multisigAddress==null){
            callback.invoke("Multisignature Address was null",null);
            return;
        }
        Gson gson=new Gson();
        SignedTransaction signedTransaction=gson.fromJson(signedTransactionString,SignedTransaction.class);
        try {
            SignedTransaction signedTransaction1=algoRepository.approveMultisigTransaction(account,signedTransaction,multisigAddress);
             gson=new Gson();
            String jsonString=gson.toJson(signedTransaction1);
            callback.invoke(null,jsonString);
        } catch (NoSuchAlgorithmException e) {
            callback.invoke(e.getMessage(),null);
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            callback.invoke(e.getMessage(),null);
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void submitTransactionToNetwork(String signedTransactionString,Callback callback){
        Gson gson=new Gson();
        SignedTransaction signedTransaction=gson.fromJson(signedTransactionString,SignedTransaction.class);

        new  Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String id= algoRepository.submitTransactionToNetwork(signedTransaction);
                    callback.invoke(null,id);
                } catch (Exception e) {
                    callback.invoke(e.getMessage(),null);
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @ReactMethod
    public void createClientFromPurestake(String net,Callback callback){
        try {
            algoRepository.createClientFromPurestakeNode(net);
            callback.invoke(null,"Client created successfully");
        } catch (Exception e) {
            callback.invoke(e.getMessage(),null);
            e.printStackTrace();
        }
    }
    @ReactMethod
    public  void createClientFromHackathonInstance(Callback callback){
        try {
            algoRepository.createClientFromHackathonInstance();
            callback.invoke(null,"Client created successfully");
        } catch (Exception e) {
            callback.invoke(e.getMessage(),null);
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void createClientFromSandbox(Callback callback){
        algoRepository.createAlgodClientFromSandBox();
    }

    @ReactMethod
    public  void createASA(double assetTotal, boolean defaultFrozen, String unitName,
                           String assetName, String url, String assetMetadataHash, String managerAddress, String reserveAddress,
                           String freezeAddress, String clawbackAddress, int decimals,Callback callback){
        try {
            Address manager=new Address(managerAddress);
            Address freeze=new Address(freezeAddress);
            Address reserve=new Address(reserveAddress);
            Address clawback=new Address(clawbackAddress);
            Integer dec=Integer.valueOf(decimals);
            BigInteger assTotal=BigInteger.valueOf((int) assetTotal);
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            Double assetId= null;
                            try {
                                assetId = algoRepository.createASA(account,assTotal,defaultFrozen,unitName,assetName,url,assetMetadataHash,manager
                                        ,reserve,freeze,clawback,dec);
                                callback.invoke(null,assetId);
                            } catch (Exception e) {
                                callback.invoke(e.getMessage(),null);
                                e.printStackTrace();
                            }

                        }
                    }
            ).start();

        } catch (Exception e) {
            callback.invoke(e.getMessage(),null);
            e.printStackTrace();
        }
    }

    @ReactMethod
    public  void changeAccountManager(String senderAddress,
                                      String newManagerAddress,Double assetId,
                                     String reserveAddress,   String freezeAddress, String clawbackAddress,
                                      int decimals,Callback callback) {

        try {
            Address freeze=new Address(freezeAddress);
            Address reserve=new Address(reserveAddress);
            Address clawback=new Address(clawbackAddress);
            Integer dec=Integer.valueOf(decimals);
            Address manager=new Address(newManagerAddress);
            Address sender=new Address(senderAddress);
            new  Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            String id= null;
                            try {
                                id = algoRepository.changeAccountManager(sender,account,manager,assetId.longValue(),reserve,freeze
                                        ,clawback,dec,algoRepository.getAlgodClient());
                                callback.invoke(null,id);
                            } catch (Exception e) {
                                callback.invoke(e.getMessage(),null);
                                e.printStackTrace();
                            }

                        }
                    }
            ).start();

        } catch (NoSuchAlgorithmException e) {
            callback.invoke(e.getMessage(),null);
            e.printStackTrace();
        } catch (Exception e) {
            callback.invoke(e.getMessage(),null);
            e.printStackTrace();
        }
    }

    @ReactMethod
    public   void optInToReceiveAsa(Double assetId,Callback callback)  {

        new  Thread(new Runnable() {
            @Override
            public void run() {
                String id= null;
                try {
                    id = algoRepository.optInToReceiveAsa(assetId.longValue(),algoRepository.getAlgodClient(),account);
                    callback.invoke(null,id);
                } catch (Exception e) {
                    callback.invoke(e.getMessage(),null);
                    e.printStackTrace();
                }

            }
        }).start();




    }

    @ReactMethod
    public   void transferAsa(Double assetId,String receiverAddress,Double assetAmount,Callback callback ) {
        try {
            Address receiver=new Address(receiverAddress);
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            String id= null;
                            try {
                                id = algoRepository.transferAsa(assetId.longValue(),algoRepository.getAlgodClient(),account,
                                        receiver,BigDecimal.valueOf(assetAmount).toBigInteger());
                                callback.invoke(null,id);
                            } catch (Exception e) {
                                callback.invoke(e.getMessage(),null);
                                e.printStackTrace();
                            }

                        }
                    }
            ).start();

        } catch (NoSuchAlgorithmException e) {
            callback.invoke(e.getMessage(),null);
            e.printStackTrace();
        } catch (Exception e) {
            callback.invoke(e.getMessage(),null);
            e.printStackTrace();
        }
    }

    @ReactMethod
    public  void freezeAsa(Double assetId, Boolean freezeState,String addressToFreezeAddress,Callback callback) {
        new  Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Address addressToFreeze=new Address(addressToFreezeAddress);
                    String id=  algoRepository.freezeAsa(assetId.longValue(),freezeState,account,account,addressToFreeze,algoRepository.getAlgodClient());
                    callback.invoke(null,id);
                } catch (NoSuchAlgorithmException e) {
                    callback.invoke(e.getMessage(),null);
                    e.printStackTrace();
                } catch (Exception e) {
                    callback.invoke(e.getMessage(),null);
                    e.printStackTrace();
                }
            }
        }).start();


    }

    @ReactMethod
    public   void revokeAsa(Double assetId,
                            String addressToRevokeAddress,Double amountToRevoke,String receiverAddress,Callback callback){
        new  Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Address addressToRevoke=new Address(addressToRevokeAddress);
                            Address receiver=new Address(receiverAddress);
                            String  id= algoRepository.revokeAsa(assetId.longValue(),account,account,addressToRevoke,algodClient,amountToRevoke.longValue(),receiver);
                            callback.invoke(null,id);
                        } catch (Exception e) {
                            callback.invoke(e.getMessage(),null);
                            e.printStackTrace();
                        }
                    }
                }
        ).start();


    }

    @ReactMethod
    public   void destroyASA(Double assetId,Callback callback) {


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String id=algoRepository.destroyASA(assetId.longValue(),account,algoRepository.getAlgodClient());
                    callback.invoke(null,id);
                } catch (Exception e) {
                    callback.invoke(e.getMessage(),null);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @ReactMethod
    public void createTransaction(String receiverAdddress,String note,int valueToSend,String senderAddress,Callback callback){
        try {
            Transaction transaction=algoRepository.createTransaction(account,receiverAdddress,note,valueToSend,senderAddress);
            Gson gson=new Gson();
            String transactionString=gson.toJson(transaction,Transaction.class);
            callback.invoke(null,transactionString);
        } catch (Exception e) {
            callback.invoke(e.getMessage(),null);
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void addTransaction(String transaction){
        transactionList.add(transaction);
    }
    @ReactMethod
    public void createTransactionForGrouping(String receiverAdddress,String note,int valueToSend,String senderAddress,Callback callback){
        try {
            Transaction transaction=algoRepository.createTransaction(account,receiverAdddress,note,valueToSend,senderAddress);
            Gson gson=new Gson();
            String transactionString=gson.toJson(transaction,Transaction.class);
            addTransaction(transactionString);
            callback.invoke(null,transactionString);
        } catch (Exception e) {
            callback.invoke(e.getMessage(),null);
            e.printStackTrace();
        }
    }


    @ReactMethod
    public void groupTransactions(Callback callback){
        if(transactionList.size()<2){
            callback.invoke("Make sure to call addTransaction passing in each transaction you want to group",null);
        }

        List<Transaction> transactions=new ArrayList<>();
        Gson gson=new Gson();
        for(String transaction: transactionList){
         transactions.add(gson.fromJson(transaction,Transaction.class));

        }
        try {
            Transaction[] transactionArray=new Transaction[transactions.size()];
            transactions.toArray(transactionArray);
            Log.d("nimiDebug",transactionArray[0].amount+" "+transactionArray[1].amount);
            groupedTransactions = algoRepository.groupTransactions(transactionArray);
            callback.invoke(null, "Successfully grouped trasactions");
        } catch (IOException e) {
            callback.invoke(e.getMessage(),null);
            e.printStackTrace();
        }

    }
    @ReactMethod
    public void signGroupedTransactions(Callback callback){
        if(account==null){
            callback.invoke("Account was null",null);
            return;
        }
        if(groupedTransactions==null){
            callback.invoke("You havent grouped any transaction yet",null);
            return;
        }

        signedTransaction= new ArrayList<>();
        Gson gson=new Gson();
        for(Transaction transaction1:groupedTransactions){
//             transaction1=gson.fromJson(transaction,Transaction.class);
            try {
                signedTransaction.add(account.signTransaction(transaction1));

            } catch (NoSuchAlgorithmException e) {
                callback.invoke(e.getMessage(),null);
                e.printStackTrace();
            }
        }
        callback.invoke(null,"Successfully signed group transaction");
    }
    @ReactMethod
    public void assembleSignedTransaction(Callback callback){
        if(account==null){
            callback.invoke("Account was null",null);
            return;
        }
        if(groupedTransactions==null){
            callback.invoke("You havent grouped any transaction yet",null);
            return;
        }

        if(signedTransaction==null){
            callback.invoke("You havent signeed any group transaction yet",null);
            return;
        }
        new  Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SignedTransaction[] signedTransactionsArray=new SignedTransaction[signedTransaction.size()];
                            signedTransaction.toArray(signedTransactionsArray);
                            String id= algoRepository.assembleTransactionGroup(signedTransactionsArray,algoRepository.getAlgodClient());
                            callback.invoke(null,id);
                        } catch (Exception e) {
                            callback.invoke(e.getMessage(),null);
                            e.printStackTrace();
                        }
                    }
                }
        ).start();

    }

}
