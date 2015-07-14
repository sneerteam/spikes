package me.sneer.snitcoin;

import java.io.File;
import java.util.List;

import org.bitcoinj.core.AbstractWalletEventListener;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;

public class App {

	public static void main(String[] args) {

		NetworkParameters params = TestNet3Params.get();
		WalletAppKit kit = new WalletAppKit(params, new File("./walletappkit-example"), "walletappkit-example");
		kit.startAsync();
		kit.awaitRunning();

		WalletListener wListener = new WalletListener();
		kit.wallet().addEventListener(wListener);

		System.out.println("send money to: " + kit.wallet().freshReceiveAddress().toString());

	}

	static class WalletListener extends AbstractWalletEventListener {

		@Override
		public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
			System.out.println("-----> coins resceived: " + tx.getHashAsString());
			System.out.println("received: " + tx.getValue(wallet));
			
			System.out.println("-- PrevBalance " + prevBalance);
			System.out.println("-- NewBalance " + newBalance);
		}

		@Override
		public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
			System.out.println("-----> confidence changed: " + tx.getHashAsString());
			TransactionConfidence confidence = tx.getConfidence();
			System.out.println("new block depth: " + confidence.getDepthInBlocks());
		}

		@Override
		public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
			System.out.println("coins sent");
			
			System.out.println("-- PrevBalance " + prevBalance);
			System.out.println("-- NewBalance " + newBalance);
		}

		@Override
		public void onReorganize(Wallet wallet) {
		}

		@Override
		public void onWalletChanged(Wallet wallet) {
			System.out.println("wallet changed: " + wallet.getBalance());
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
