package com.blackjack.cardcounter.pro;

import java.util.ArrayList;

public class Hand {
	String name = "";
	int hard = 0;
	int soft = 0;
	int betholder = 0;
	boolean aceFlagger = false;
	ArrayList<Card> hand;

	public Hand(String name) {
		this.name = name;
		hand = new ArrayList<Card>();
	}

	public int getBetholder() {
		return betholder;
	}

	public void setBetholder(int betholder) {
		this.betholder = betholder;
	}

	public String getName() {
		return name;
	}

	public boolean blackJack() {
		if ((hand.get(0).getRank() == 1 && hand.get(1).getValue() == 10)
				|| (hand.get(0).getValue() == 10 && hand.get(1).getRank() == 1)) {
			return true;
		} else {
			return false;
		}
	}

	public String getHardSoft() {
		if (hard == soft) {
			return Integer.toString(hard);
		} else if (hard > 21) {
			return Integer.toString(soft);
		} else {
			return Integer.toString(soft) + "/" + Integer.toString(hard);
		}
	}

	public boolean busted() {
		if (hard < 22 || soft < 22) {
			return false;
		} else {
			return true;
		}
	}

	public int getBestScore() {
		if (hard == soft) {
			return hard;
		} else if (hard < 22)
			return hard;
		else {
			return soft;
		}
	}

	public boolean getAceFlagger() {
		return aceFlagger;
	}

	public String getHandType() {
		if (hand.get(0).getValue() == hand.get(1).getValue()) {
			return "pair";
		} else if (getAceFlagger()) {
			int index = 0;

			for (int i = 0; i < hand.size(); i++) {
				index += hand.get(i).getValue();
			}
			index -= 1;
			if (index > 10) {
				return "hard";
			} else
				return "soft";
		} else {
			return "hard";
		}
	}

	public String getDealerFaceUpCard() {
		return hand.get(0).getName();
	}

	public void recieveDealtedCard(Card card) {
		hand.add(card);
		if (card.rank == 1 && !aceFlagger) {
			aceFlagger = true;
			hard += 11;
			soft += 1;
		} else {
			hard += card.getValue();
			soft += card.getValue();
		}
	}

	public ArrayList<Card> getHand() {
		return hand;
	}

	public void resetHand() {
		hand.clear();
		hard = 0;
		soft = 0;
		betholder = 0;
		aceFlagger = false;
	}
}
