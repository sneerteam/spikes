package me.sneer.snitcoin;

import java.util.Scanner;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Wallet.DustySendRequested;

public class Main {

	public static void main(String[] args) {
		
		Snitcoin snitcoin = new Snitcoin();
		snitcoin.setListener(new Listener() {
			public void onChange(Status status) {
				
				System.out.println("--------------------------------------------");
				System.out.println("Message: " + status.message);
				System.out.println("Balance: " + status.balance);
				System.out.println("Receive Address: " + status.receiveAddress);
				
				System.out.println("Transactions: ");
				for(Transaction transaction : status.transactions){
					System.out.println("\tDirection: " + transaction.direction);
					System.out.println("\tAmount: " + transaction.amount);
					System.out.println("\tAddress: " + transaction.address);
					System.out.println("\tStatus: " + transaction.status);
					System.out.println("");
				}
				System.out.println("--------------------------------------------");
			}
		});

		snitcoin.run();
		
		Scanner scan = new Scanner (System.in);
		while(true){
			System.out.println ("Amount to Send: ");
			String amount = scan.nextLine();
			
			System.out.println ("Address to Send: ");
			String address = scan.nextLine();
			
			try {
				snitcoin.send(amount, address);
			} catch (DustySendRequested e) {
				e.printStackTrace();
			} catch (AddressFormatException e) {
				e.printStackTrace();
			} catch (InsufficientMoneyException e) {
				e.printStackTrace();
			}
			
		}
	}
}
