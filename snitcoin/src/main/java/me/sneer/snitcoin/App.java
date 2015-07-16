package me.sneer.snitcoin;

import java.io.File;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.Wallet.BalanceType;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class App {
	
	private NetworkParameters params;
	private WalletAppKit kit;
	private String filePrefix;

	public App(NetworkParameters params, final String filePrefix) {
		this.params = params;
		this.filePrefix = filePrefix;
		
		kit = new WalletAppKit(params, new File("."), filePrefix);
        kit.setAutoSave(true);
        kit.startAsync();
        kit.awaitRunning();
        kit.wallet().addEventListener(new AbstractWalletEventListener() {
        	@Override
            public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
            	System.out.println("----------------------------------------------------------------------");
            	System.out.println("-----> " + filePrefix + " " + wallet.currentReceiveAddress());
            	System.out.println("----------------------------------------------------------------------");
            	System.out.println("-----> coins resceived: " + tx.getHashAsString());
                System.out.println("received: " + tx.getValue(wallet));
                System.out.println("prev balance: " + prevBalance.getValue());
                System.out.println("new balance: " + newBalance.getValue());
                System.out.println("----------------------------------------------------------------------");
            }

            @Override
            public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
            	System.out.println("----------------------------------------------------------------------");
            	System.out.println("-----> " + filePrefix + " " + wallet.currentReceiveAddress());
            	System.out.println("----------------------------------------------------------------------");
                System.out.println("-----> coins sent: " + tx.getHashAsString());
                System.out.println("coins sent: " + tx.getValue(wallet));
                System.out.println("prev balance: " + prevBalance.getValue());
                System.out.println("new balance: " + newBalance.getValue());
                System.out.println("----------------------------------------------------------------------");
            }

            @Override
            public void onWalletChanged(Wallet wallet) {
            	System.out.println("----------------------------------------------------------------------");
            	System.out.println("-----> " + wallet.currentReceiveAddress());
            	System.out.println("----------------------------------------------------------------------");
            	System.out.println("Wallet chaged! Current Balance: " + wallet.getBalance().getValue());
                System.out.println("----------------------------------------------------------------------");
            	
            }
        	
		});
        
        System.out.println("Balance: " + kit.wallet().getBalance().getValue());
        System.out.println("Send money to: " + kit.wallet().freshReceiveAddress().toString());
	}
	
	public void send(double amount, String address) throws AddressFormatException{
		Address to = new Address(params, address);
		Coin value = Coin.parseCoin(String.valueOf(amount));
        try {
            Wallet.SendResult result = kit.wallet().sendCoins(kit.peerGroup(), to, value);
            
            System.out.println("----------------------------------------------------------------------");
        	System.out.println("-----> " + filePrefix + " " + kit.wallet().currentReceiveAddress());
        	System.out.println("----------------------------------------------------------------------");
            System.out.println("coins sent. transaction hash: " + result.tx.getHashAsString());
            System.out.println("----------------------------------------------------------------------");
        } catch (InsufficientMoneyException e) {
        	System.err.println("----------------------------------------------------------------------");
        	System.err.println("-----> " + filePrefix + " " + kit.wallet().currentReceiveAddress());
        	System.err.println("----------------------------------------------------------------------");
            System.err.println("Not enough coins in your wallet. Missing " + e.missing.getValue() + " satoshis are missing (including fees)");
            System.err.println("----------------------------------------------------------------------");
            
            
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
	}
	
	public String getAddress(){
		return kit.wallet().freshReceiveAddress().toString();
	}
	
	public static void main(String[] args) {
		NetworkParameters params = TestNet3Params.get();
		App neideApp = new App(params, "neide");
		App tangoApp = new App(params, "tango");
		
//		try {
//			neideApp.send(0.001, tangoApp.getAddress());
//			tangoApp.send(0.0001, neideApp.getAddress());
//		} catch (AddressFormatException e) {
//			e.printStackTrace();
//		}
		
		
	}
}
