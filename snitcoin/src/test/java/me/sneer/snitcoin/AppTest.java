package me.sneer.snitcoin;

import java.io.File;
import java.io.IOException;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.params.TestNet3Params;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AppTest extends TestCase {
	private TestNet3Params neideParams;
	private ECKey neideKey;
	private Address neideAddress;
	private File neideWalletFile;
	private Wallet neideWallet;

	public AppTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		return new TestSuite(AppTest.class);
	}
	
	public void setUp() throws Exception{
		neideParams = TestNet3Params.get();
		neideKey = new ECKey();
		neideAddress = new Address(neideParams, Utils.sha256hash160(neideKey.getPubKey()));
		
		neideWalletFile = new File("neide.wallet", ".");
		Wallet wallet = new Wallet(neideParams);
		wallet.importKey(neideKey);
		wallet.saveToFile(neideWalletFile);
		
		neideWallet = Wallet.loadFromFile(neideWalletFile);
		
	}
	
	
	public void testPersistWallet() throws IOException {
		
	}
	
}
