package me.sneer.snitcoin.examples;

import java.io.File;
import java.util.List;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.Wallet.BalanceType;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * The following example shows you how to create a SendRequest to send coins from a wallet to a given address.
 */
public class SendRequest {

    public static void main(String[] args) throws Exception {

        // We use the WalletAppKit that handles all the boilerplate for us. Have a look at the Kit.java example for more details.
        NetworkParameters params = TestNet3Params.get();
        WalletAppKit kit = new WalletAppKit(params, new File("."), "sendrequest-example");
        kit.setAutoSave(true);
        kit.startAsync();
        kit.awaitRunning();

        System.out.println("Balance: " + kit.wallet().getBalance().getValue());
        System.out.println("Send money to: " + kit.wallet().currentReceiveAddress().toString());
        
        WalletListener wListener = new WalletListener();
        kit.wallet().addEventListener(wListener);

        // How much coins do we want to send?
        // The Coin class represents a monetary Bitcoin value.
        // We use the parseCoin function to simply get a Coin instance from a simple String.
        Coin value = Coin.parseCoin("0.01");

        // To which address you want to send the coins?
        // The Address class represents a Bitcoin address.
        Address to = new Address(params, "mkW3GjhFrsyenoMGtD1TnrVJUTxHWkiWZJ");

        // There are different ways to create and publish a SendRequest. This is probably the easiest one.
        // Have a look at the code of the SendRequest class to see what's happening and what other options you have: https://bitcoinj.github.io/javadoc/0.11/com/google/bitcoin/core/Wallet.SendRequest.html
        // 
        // Please note that this might raise a InsufficientMoneyException if your wallet has not enough coins to spend.
        // When using the testnet you can use a faucet (like the http://faucet.xeno-genesis.com/) to get testnet coins.
        // In this example we catch the InsufficientMoneyException and register a BalanceFuture callback that runs once the wallet has enough balance.
        try {
            Wallet.SendResult result = kit.wallet().sendCoins(kit.peerGroup(), to, value);
            System.out.println("coins sent. transaction hash: " + result.tx.getHashAsString());
            // you can use a block explorer like https://www.biteasy.com/ to inspect the transaction with the printed transaction hash. 
        } catch (InsufficientMoneyException e) {
            System.out.println("Not enough coins in your wallet. Missing " + e.missing.getValue() + " satoshis are missing (including fees)");
            System.out.println("Send money to: " + kit.wallet().currentReceiveAddress().toString());

            // Bitcoinj allows you to define a BalanceFuture to execute a callback once your wallet has a certain balance.
            // Here we wait until the we have enough balance and display a notice.
            // Bitcoinj is using the ListenableFutures of the Guava library. Have a look here for more information: https://code.google.com/p/guava-libraries/wiki/ListenableFutureExplained
            ListenableFuture<Coin> balanceFuture = kit.wallet().getBalanceFuture(value, BalanceType.AVAILABLE);
            FutureCallback<Coin> callback = new FutureCallback<Coin>() {
                public void onSuccess(Coin balance) {
                    System.out.println("coins arrived and the wallet now has enough balance");
                }

                public void onFailure(Throwable t) {
                    System.out.println("something went wrong");
                }
            };
            Futures.addCallback(balanceFuture, callback);
        }

        // shutting down 
        //kit.stopAsync();
        //kit.awaitTerminated();
    }
    
    
    
    static class WalletListener extends AbstractWalletEventListener {

        @Override
        public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
            System.out.println("-----> coins resceived: " + tx.getHashAsString());
            System.out.println("received: " + tx.getValue(wallet));
        }

        @Override
        public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
            System.out.println("-----> confidence changed: " + tx.getHashAsString());
            TransactionConfidence confidence = tx.getConfidence();
            System.out.println("new block depth: " + confidence.getDepthInBlocks());
        }

        @Override
        public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
            System.out.println("coins sent: " + tx.getValue(wallet));
        }

        @Override
        public void onReorganize(Wallet wallet) {
        	System.out.println("on reorganize: " + wallet.getBalance().getValue());
        }

        @Override
        public void onWalletChanged(Wallet wallet) {
        	System.out.println("on wallet chaged: " + wallet.getBalance().getValue());
        }

        @Override
        public void onKeysAdded(List<ECKey> keys) {
            System.out.println("new key added");
        }

        public void onScriptsChanged(Wallet wallet, List<Script> scripts, boolean isAddingScripts) {
            System.out.println("new script added");
        }
    }
}
