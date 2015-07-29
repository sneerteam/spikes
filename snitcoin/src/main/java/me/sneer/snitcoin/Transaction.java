package me.sneer.snitcoin;

public class Transaction {
	public final Direction direction;
	public final String hash;
	public final String amount;
	public final String progress;
	
	public Transaction(Direction direction, String hash, String amount, String progress) {
		this.direction = direction;
		this.hash = hash;
		this.amount = amount;
		this.progress = progress;
	}
}
