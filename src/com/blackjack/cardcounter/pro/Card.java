package com.blackjack.cardcounter.pro;

import java.io.Serializable;

public class Card implements Serializable {
	String name;
	String suit;
	int value;
	int rank;

	public Card(String name, String suit, int value, int rank) {
		super();
		this.value = value;
		this.name = name;
		this.suit = suit;
		this.rank = rank;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSuit() {
		return suit;
	}

	public void setSuit(String suit) {
		this.suit = suit;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}
}
