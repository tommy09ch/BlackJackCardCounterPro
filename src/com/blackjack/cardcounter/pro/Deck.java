package com.blackjack.cardcounter.pro;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import android.util.Log;

public class Deck implements Serializable {
	Card[] deckOfCard;
	Stack<Card> deckStack = new Stack<Card>();
	int numOfDeck = 0;
	int cutoff = 0;
	int running_count = 0;
	int true_count = 0;
	int min = 26, max = 78;
	int remDeck;

	public Deck(int deckNumb) {
		numOfDeck = deckNumb;
		deckOfCard = new Card[numOfDeck * 52];
		initializeDeck();
		shuffleDeck();
		// displayDecks();
	}

	public Card dealtCard() {
		Card dcard = deckStack.pop();
		if (dcard.getRank() >= 10 || dcard.getRank() == 1) {
			running_count -= 1;
		} else if (dcard.getRank() >= 2 && dcard.getRank() <= 6) {
			running_count += 1;
		}
		remDeck = (int) Math.round((deckStack.size() - cutoff) / 52.0);

		if (remDeck == 0) {
			true_count = running_count;
		} else {
			true_count = running_count / remDeck;
		}
		return dcard;
	}

	public Card peek() {
		return deckStack.peek();
	}

	public int getRunning_count() {
		return running_count;
	}

	public int getTrue_count() {
		return true_count;
	}
	
	public int getRemDeck() {
		return remDeck;
	}

	public int sizeOfDeckStack() {
		return deckStack.size();
	}

	public void shuffleDeck() {
		Random rand = new Random(System.nanoTime());
		int swapind = 0;

		for (int iter = 0; iter < 5; iter++) {
			for (int card = 0; card < (numOfDeck * 52); card++) {
				swapind = Math.abs(rand.nextInt() % 312);
				Card tmpCard = deckOfCard[card];
				deckOfCard[card] = deckOfCard[swapind];
				deckOfCard[swapind] = tmpCard;
			}
		}
		List<Card> list = Arrays.asList(deckOfCard);
		deckStack.clear();
		deckStack.addAll(list);

		cutoff = rand.nextInt((max - min) + 1) + min;
		running_count = 0;
		true_count = 0;
	}

	public int getCutOff() {
		return cutoff;
	}

	private void initializeDeck() {
		String suit_str = "";
		String name_str = "";
		int cardval = 0;
		int index = 0;

		for (int deck = 0; deck < numOfDeck; deck++) {
			for (int suit = 0; suit < 4; suit++) {
				for (int value = 1; value < 14; value++) {

					suit_str = (suit == 0) ? "Spades" : (suit == 1) ? "Clubs"
							: (suit == 2) ? "Diamonds" : "Hearts";
					name_str = (value == 1) ? "Ace" : (value == 11) ? "Jack"
							: (value == 12) ? "Queen" : (value == 13) ? "King"
									: Integer.toString(value);
					cardval = (value > 10) ? 10 : value;
					deckOfCard[index++] = new Card(name_str, suit_str, cardval,
							value);
				}
			}
		}
	}

	public void displayDecks() {
		for (int card = 0; card < (numOfDeck * 52); card++) {
			Log.e("DEBUG",
					deckOfCard[card].getName() + " of "
							+ deckOfCard[card].getSuit() + ", "
							+ deckOfCard[card].getValue());
		}

		// Node tmp = deck_head.getNext();
		// while (tmp.next != null) {
		// Log.e("DEBUG", tmp.getData().getName() + " of " +
		// tmp.getData().getSuit() + ", " + tmp.getData().getValue());
		// tmp = tmp.getNext();
		// }
	}
}
