package com.blackjack.cardcounter.pro;

import android.widget.ImageView;

public class MyImageHolder {
	ImageView cardImage;
	String name;
	float endx, endy;

	public MyImageHolder(ImageView cardImage, String name, float player_endx,
			float player_endy) {
		super();
		this.cardImage = cardImage;
		this.name = name;
		this.endx = player_endx;
		this.endy = player_endy;
	}

	public ImageView getCardImage() {
		return cardImage;
	}

	public void setCardImage(ImageView cardImage) {
		this.cardImage = cardImage;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getEndx() {
		return endx;
	}

	public void setEndx(float endx) {
		this.endx = endx;
	}

	public float getEndy() {
		return endy;
	}

	public void setEndy(float endy) {
		this.endy = endy;
	}
}