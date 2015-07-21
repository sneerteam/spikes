package me.sneer.snitcoin;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.BlockChainListener;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.GetDataMessage;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.PeerEventListener;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.Wallet.DustySendRequested;
import org.bitcoinj.core.WalletEventListener;
import org.bitcoinj.core.AbstractBlockChain.NewBlockType;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.utils.MonetaryFormat;

public class Snitcoin implements Runnable{
	
	private NetworkParameters params;
	private WalletAppKit kit;
	private String filePrefix;
	private Listener listener;
	
	public Snitcoin() {
		this.params = TestNet3Params.get();
		this.filePrefix = "testnet3";
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
        
        listener.onChange(new Status(kit.wallet().getBalance().toString(), null, kit.wallet().currentReceiveAddress().toString(), "Started! "));
	}
	
	public void addListener(Listener listener) {
		this.listener = listener;
	}

	private class WalletEventListenerImpl extends AbstractWalletEventListener implements WalletEventListener{
		public void onKeysAdded(List<ECKey> keys) {
			listener.onChange(new Status(null, null, null, "Keys Added: " + keys));
		}

		public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
			listener.onChange(new Status(wallet.getBalance().toString(), null, wallet.freshReceiveAddress().toString(),"Coins Received: " + tx.getHashAsString()));
		}

		public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
			listener.onChange(new Status(wallet.getBalance().toString(), null, wallet.freshReceiveAddress().toString(),"Coins Sent: " + tx.getHashAsString()));
		}

		public void onReorganize(Wallet wallet) {
			listener.onChange(new Status(wallet.getBalance().toString(), null, null, "Reorganize!"));
		}

		public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
			listener.onChange(new Status(wallet.getBalance().toString(), null, null, "Transaction Confidence Changed: " + tx.getHashAsString()));
		}

		public void onWalletChanged(Wallet wallet) {
			listener.onChange(new Status(wallet.getBalance().toString(), null, null, "Wallet Changed!"));
		}

		public void onScriptsChanged(Wallet wallet, List<Script> scripts, boolean isAddingScripts) {
			listener.onChange(new Status(wallet.getBalance().toString(), null, null, "Scripts Changed!"));
		}
		
	}
}
