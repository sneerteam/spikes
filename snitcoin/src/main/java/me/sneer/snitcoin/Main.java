package me.sneer.snitcoin;

import java.util.Scanner;

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
					System.out.println("\tAmount: " + transaction.amount);
					System.out.println("\tTransaction hash: " + transaction.hash);
					System.out.println("\tProgress: " + transaction.progress);
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
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
}
