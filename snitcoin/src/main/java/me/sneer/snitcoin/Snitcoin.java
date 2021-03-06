package me.sneer.snitcoin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.RejectMessage;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionBroadcast;
import org.bitcoinj.core.TransactionBroadcast.ProgressCallback;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.Wallet.DustySendRequested;
import org.bitcoinj.core.WalletEventListener;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;

public class Snitcoin implements Runnable {

	private NetworkParameters params;
	private WalletAppKit kit;
	private String filePrefix;
	private Listener listener;
	private List<me.sneer.snitcoin.Transaction> transactions;
	private Map<Peer, RejectMessage> rejects = Collections.synchronizedMap(new HashMap<Peer, RejectMessage>());

	public Snitcoin() {
		this.params = TestNet3Params.get();
		this.filePrefix = "testnet3";
		transactions = new ArrayList<me.sneer.snitcoin.Transaction>();
	}

	public void send(String amount, String address)
			throws AddressFormatException, InsufficientMoneyException, DustySendRequested {
		Address to = new Address(params, address);
		Coin value = Coin.parseCoin(amount);
		kit.wallet().sendCoins(kit.peerGroup(), to, value);
	}

	public void run() {
		kit = new WalletAppKit(params, new File("."), this.filePrefix);
		kit.setAutoSave(true);
        kit.startAsync();
        kit.awaitRunning();
        kit.peerGroup().setDownloadTxDependencies(true);
        kit.wallet().addEventListener(new WalletEventListenerImpl());
        
        Set<Transaction> ts = kit.wallet().getTransactions(true);
        for (Transaction t : ts) {

        	List<Peer> peers = kit.peerGroup().getConnectedPeers();
        	boolean mined = t.getAppearsInHashes() != null;
        	int numToBroadcastTo = (int) Math.max(1, Math.round(Math.ceil(peers.size() / 2.0)));
        	int numWaitingFor = (int) Math.ceil((peers.size() - numToBroadcastTo) / 2.0);
        	int numSeenPeers = t.getConfidence().numBroadcastPeers() + t.getConfidence().numBroadcastPeers() / 1;
        	
        	final double progress = Math.min(1.0, mined ? 1.0 : numSeenPeers / (double) numWaitingFor);
        	transactions.add(new me.sneer.snitcoin.Transaction(t.getHashAsString(), t.getValue(kit.wallet()).toPlainString(), "" + progress));
		}
		notify2(kit.wallet(), "Started! ");
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	void notify2(Wallet wallet, String message) {
		listener.onChange(new Status(wallet.getBalance().toPlainString() + "/EST:" + wallet.getBalance(Wallet.BalanceType.ESTIMATED).toPlainString(),
				transactions, kit.wallet().currentReceiveAddress().toString(), message));
	}

	private class WalletEventListenerImpl extends AbstractWalletEventListener implements WalletEventListener {

		private void addTransaction(Transaction tx) {
			String hash = tx.getHashAsString();
			String amount = tx.getValue(kit.wallet()).toPlainString();
			transactions.add(new me.sneer.snitcoin.Transaction(hash, amount, "0.0"));
		}

		private void setBroadcastProgressCallback(final Transaction tx, TransactionBroadcast broadcast) {
			broadcast.setProgressCallback(new ProgressCallback() {
				public void onBroadcastProgress(double progress) {
					for (int i = 0; i < transactions.size(); i++) {
						me.sneer.snitcoin.Transaction t = transactions.get(i);
						if (t.hash.equals(tx.getHashAsString())) {
							transactions.set(i, new me.sneer.snitcoin.Transaction(t.hash, t.amount, String.valueOf(progress)));
							notify2(kit.wallet(), "Transaction Progress: " + tx.getHashAsString());
						}
					}
				}
			});
		}

		public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
			addTransaction(tx);
			TransactionBroadcast broadcast = kit.peerGroup().broadcastTransaction(tx);
			setBroadcastProgressCallback(tx, broadcast);
			notify2(wallet, "Coins Received: " + tx.getHashAsString());
		}

		public void onCoinsSent(Wallet wallet, final Transaction tx, Coin prevBalance, Coin newBalance) {
			notify2(wallet, "Coins Sent: " + tx.getHashAsString());
		}

		public void onWalletChanged(Wallet wallet) {
			notify2(wallet, "Wallet Changed!");
		}
	}
}
