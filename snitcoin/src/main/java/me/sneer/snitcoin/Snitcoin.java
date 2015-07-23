package me.sneer.snitcoin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.Wallet.DustySendRequested;
import org.bitcoinj.core.WalletEventListener;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;

public class Snitcoin implements Runnable{
	
	private NetworkParameters params;
	private WalletAppKit kit;
	private String filePrefix;
	private Listener listener;
	private List<me.sneer.snitcoin.Transaction> transactions;
	
	public Snitcoin() {
		this.params = TestNet3Params.get();
		this.filePrefix = "testnet3";
		transactions = new ArrayList();
	}
	
	public void send(String amount, String address) throws AddressFormatException, InsufficientMoneyException, DustySendRequested{
		Address to = new Address(params, address);
		Coin value = Coin.parseCoin(amount);
		kit.wallet().sendCoins(kit.peerGroup(), to, value);
	}
	
	public void run() {
		kit = new WalletAppKit(params, new File("."), this.filePrefix);
		kit.setAutoSave(true);
        kit.startAsync();
        kit.awaitRunning();
        
        kit.wallet().addEventListener(new WalletEventListenerImpl());
        listener.onChange(new Status(kit.wallet().getBalance().toString(), transactions, kit.wallet().currentReceiveAddress().toString(), "Started! "));
	}
	
	public void setListener(Listener listener) {
		this.listener = listener;
	}

	private class WalletEventListenerImpl extends AbstractWalletEventListener implements WalletEventListener{
		public void onKeysAdded(List<ECKey> keys) {
			listener.onChange(new Status(null, transactions, null, "Keys Added: " + keys));
		}

		public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
			transactions.add(new me.sneer.snitcoin.Transaction(Direction.RECEIVE, null, tx.getValue(wallet).toString(), null));
			listener.onChange(new Status(wallet.getBalance().toString(), transactions, wallet.freshReceiveAddress().toString(),"Coins Received: " + tx.getHashAsString()));
		}

		public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
			transactions.add(new me.sneer.snitcoin.Transaction(Direction.SEND, null, tx.getValue(wallet).toString(), null));
			listener.onChange(new Status(wallet.getBalance().toString(), transactions, wallet.freshReceiveAddress().toString(),"Coins Sent: " + tx.getHashAsString()));
		}

		public void onReorganize(Wallet wallet) {
			listener.onChange(new Status(wallet.getBalance().toString(), transactions, null, "Reorganize!"));
		}

		public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
			listener.onChange(new Status(wallet.getBalance().toString(), transactions, null, "Transaction Confidence Changed: " + tx.getHashAsString()));
		}

		public void onWalletChanged(Wallet wallet) {
			listener.onChange(new Status(wallet.getBalance().toString(), transactions, null, "Wallet Changed!"));
		}

		public void onScriptsChanged(Wallet wallet, List<Script> scripts, boolean isAddingScripts) {
			listener.onChange(new Status(wallet.getBalance().toString(), transactions, null, "Scripts Changed!"));
		}
		
	}
}
