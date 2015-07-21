package me.sneer.snitcoin;

public class Transaction {
	public final Direction direction;
	public final String address;
	public final String amount;
	public final String status;
	
	public Transaction(Direction direction, String address, String amount, String status) {
		this.direction = direction;
		this.address = address;
		this.amount = amount;
		this.status = status;
	}
}
