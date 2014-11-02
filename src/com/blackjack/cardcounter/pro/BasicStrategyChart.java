package com.blackjack.cardcounter.pro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.example.blackjackcardcountingtrainerpro.R;

import android.content.Context;

public class BasicStrategyChart {
	// s = stand, h = hit, d = double, x = surrender/hit, c = double/stand
	Character[][] hardTotalChart = new Character[10][10];
	Character[][] softTotalChart = new Character[7][10];
	Character[][] splitChart = new Character[10][10];
	Character move = 'p';
	Context context;

	public BasicStrategyChart(Context con) {
		context = con;
		setupCharts();
	}

	public Character getCorrectMoves(Hand hand, int dealerFaceUp) {

		// Split
		if (hand.getHand().get(0).getValue() == hand.getHand().get(1)
				.getValue()) {
			if (hand.getHand().size() == 2) {
				move = splitChart[hand.getHand().get(0).getValue() - 1][dealerFaceUp - 1];
				if (move == 'n') {
					move = getHardTotalChart(hand, dealerFaceUp);
				}
			} else {
				move = getHardTotalChart(hand, dealerFaceUp);
			}
		} else if (hand.getAceFlagger()) {
			int index = 0;

			for (int i = 0; i < hand.getHand().size(); i++) {
				index += hand.getHand().get(i).getValue();
			}
			index -= 1;

			if (index == 9 || index == 10)
				move = 's';
			else if (index > 10) {
				move = getHardTotalChart(hand, dealerFaceUp);
			} else
				move = softTotalChart[index - 2][dealerFaceUp - 1];
		} else {
			move = getHardTotalChart(hand, dealerFaceUp);
		}
		return move;
	}

	public Character getHardTotalChart(Hand hand, int dealerFaceUp) {
		if (hand.getBestScore() >= 18)
			move = 's';
		else if (hand.getBestScore() <= 7)
			move = 'h';
		else
			move = hardTotalChart[hand.getBestScore() - 8][dealerFaceUp - 1];
		return move;
	}

	private void setupCharts() {
		BufferedReader reader = null;

		try {
			InputStream is = context.getResources().openRawResource(
					R.raw.hardtotalchart);
			reader = new BufferedReader(new InputStreamReader(is));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int r = hardTotalChart.length - 1; r >= 0; r--) {
			for (int c = 0; c < hardTotalChart[r].length; c++) {
				try {
					hardTotalChart[r][c] = reader.readLine().charAt(0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		try {
			InputStream is = context.getResources().openRawResource(
					R.raw.softtotalchart);
			reader = new BufferedReader(new InputStreamReader(is));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int r = softTotalChart.length - 1; r >= 0; r--) {
			for (int c = 0; c < softTotalChart[r].length; c++) {
				try {
					softTotalChart[r][c] = reader.readLine().charAt(0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		try {
			InputStream is = context.getResources().openRawResource(
					R.raw.splitchart);
			reader = new BufferedReader(new InputStreamReader(is));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int r = splitChart.length - 1; r >= 0; r--) {
			for (int c = 0; c < splitChart[r].length; c++) {
				try {
					splitChart[r][c] = reader.readLine().charAt(0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
