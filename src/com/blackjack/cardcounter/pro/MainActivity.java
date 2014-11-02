package com.blackjack.cardcounter.pro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.blackjack.cardcounter.pro.SimpleGestureFilter.SimpleGestureListener;
import com.example.blackjackcardcountingtrainerpro.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("serial")
public class MainActivity extends Activity implements SimpleGestureListener,
		AnimationListener, Serializable {
	TextView score1TV, score2TV, score3TV, countTV, popbetTV_bet,
			popsetTV_errorRatio, popsetTV_correctMoves,
			popsetTV_incorrectMoves, popsetTV_runCount, popsetTV_trueCount,
			popsetTV_handsPlayed, popsetTV_remDeck, popsetTV_bank,
			popsetTV_winnings, popsetTV_viewChart, popsetTV_viewBJInfo,
			popsetTV_instruct, popsetTV_startNewShoe, popsetTV_resetGame,
			popsumTV_handsplayed, popsumTV_correctplays,
			popsumTV_incorrectplays, popsumTV_winnings, popsumTV_bank;;
	Button hit, stand, doubl, split, surrend;
	Button popbetButt_chip5, popbetButt_chip25, popbetButt_chip100,
			popbetButt_chip500, popbetButt_chip1000, setting;
	Switch popsetSwitch_repBet, popsetSwitch_hint, popsetSwitch_score,
			popsetSwitch_count;
	CheckBox popinstCB_hide;

	float CARD_ANIM_START_X, CARD_ANIM_START_Y, player_endx, player_endy,
			dealer_endx, dealer_endy;

	private Hand player, dealer, player_splithand;
	private Deck deck;
	private BasicStrategyChart bsc;

	private int player_bank = 1000, bet = 5, flipCardId = 0,
			numbhandplayed = 0, correctPlay = 0, incorrectPlay = 0;
	private boolean splitFlag = false, splitChecker = false, repeatBet = false,
			settingEnableTips = true, hitEnable, surrendEnable, splitEnable,
			doublEnable, standEnable, shouldHit = false, shouldStand = false,
			shouldSplit = false, shouldSurrender = false, shouldDouble = false,
			newgame = false, showInstruction = true;

	private SimpleGestureFilter detector;
	private LinkedList<MyImageHolder> imageLinkedList = new LinkedList<MyImageHolder>();
	private List<MyImageHolder> discarded = new ArrayList<MyImageHolder>();
	private RelativeLayout rl;
	private ImageView flipCard;
	private Point size = new Point();
	private TranslateAnimation translateAnim;
	private MyImageHolder splitImgCard1, splitImgCard2;
	private AlertDialog popup_settingAlert, popup_placeBetAlert,
			popup_viewChartAlert, popup_openingMenuAlert, popup_summaryAlert,
			popup_instructionAlert;
	private Character correctMove;
	private String curPlayerHand;
	String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	File savedSetting = new File(sdPath + "/BlackJackCCTP/saved/setting.data");
	File savedObject = new File(sdPath + "/BlackJackCCTP/saved/myobject.data");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		rl = (RelativeLayout) findViewById(R.id.mainLayout);

		getWindowManager().getDefaultDisplay().getSize(size);
		System.out.println(size.x + "," + size.y);

		CARD_ANIM_START_X = size.x;
		CARD_ANIM_START_Y = 0;
		player_endx = (float) (size.x / 4);
		player_endy = (float) (size.y - (size.y / 2.5));
		dealer_endx = (float) (size.x / 4);
		dealer_endy = 0;

		detector = new SimpleGestureFilter(this, this);
		bsc = new BasicStrategyChart(this);

		setupSettingAlert();
		setupPlaceBetAlert();
		setupViewChartAlert();
		setupOpeningMenuAlert();
		setupSummaryAlert();
		setupInstructionAlert();
		initializeViews();

		File savedObject = new File(sdPath
				+ "/BlackJackCCTP/saved/setting.data");
		try {
			FileInputStream f_in = new FileInputStream(savedObject);
			ObjectInputStream obj_in = new ObjectInputStream(f_in);
			showInstruction = obj_in.readBoolean();
			if (showInstruction)
				popinstCB_hide.setChecked(false);
			else
				popinstCB_hide.setChecked(true);
			f_in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				prepInfoAndSettings();
				popup_settingAlert.show();
				int width = (size.x > 1000) ? 1000 : size.x;
				int height = (int) ((size.y > 1500) ? 1200 : size.y / 1.2);
				popup_settingAlert.getWindow().setLayout(width, height);
			}

		});

		openingMenu();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent me) {
		switch (me.getActionMasked()) {
		case MotionEvent.ACTION_POINTER_UP:
			if (me.getPointerCount() == 2) {
				onTwoFingerTap();
			}
			break;
		}

		if (setting != null) {
			int[] pos = new int[2];
			setting.getLocationOnScreen(pos);

			if (me.getY() <= (pos[1] + setting.getHeight())
					&& me.getX() > pos[0]) {
				if (!popup_settingAlert.isShowing()) {
					setting.performClick();
				}
			} else
				this.detector.onTouchEvent(me);
		}

		return super.dispatchTouchEvent(me);
	}

	private void openingMenu() {
		popup_openingMenuAlert.show();
		int width = (size.x > 1000) ? 1000 : size.x;
		popup_openingMenuAlert.getWindow().setLayout(width,
				LayoutParams.WRAP_CONTENT);
	}

	private void playRound() {
		resetForNewHand();
		player.resetHand();
		dealer.resetHand();
		if (!repeatBet)
			placeBet();
		else {
			bet = (bet > player_bank) ? 5 : bet;
			player.setBetholder(bet);
			beginDeal();
		}
	}

	private void playerMoves(final Hand curHand) {
		if ((curHand.getBestScore() == 21) && !splitFlag) {
			hit.setEnabled(false);
			stand.setEnabled(false);
			doubl.setEnabled(false);
			split.setEnabled(false);
			surrend.setEnabled(false);
			new CountDownTimer(1750, 1000) {
				public void onFinish() {
					dealerMoves(false);
				}

				public void onTick(long millisUntilFinished) {
				}
			}.start();
		}

		hit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doubl.setEnabled(false);
				split.setEnabled(false);
				surrend.setEnabled(false);

				ImageView tmpCardImg = getCardImage(deck.peek(), false);
				runDealAnim(tmpCardImg, player_endx, player_endy, "player");
				player_endx += 40;
				player_endy += 15;

				curHand.recieveDealtedCard(deck.dealtCard());
				updateInfo(curHand.getName());

				if ((curHand.busted() || curHand.getBestScore() == 21)
						&& !splitFlag) {
					hit.setEnabled(false);
					stand.setEnabled(false);
					doubl.setEnabled(false);
					split.setEnabled(false);
					surrend.setEnabled(false);
					new CountDownTimer(1750, 1000) {
						public void onFinish() {
							dealerMoves(false);
						}

						public void onTick(long millisUntilFinished) {
						}
					}.start();
				} else if ((curHand.busted() || curHand.getBestScore() == 21)
						&& splitFlag) {
					new CountDownTimer(1500, 1000) {

						@Override
						public void onTick(long millisUntilFinished) {
						}

						@Override
						public void onFinish() {
							doubl.setEnabled(true);
							splitFlag = false;
							player_endx = (float) (size.x / 2) + 40;
							player_endy = (float) (size.y - (size.y / 2.5)) + 15;

							ImageView tmpCardImg = getCardImage(deck.peek(),
									false);
							runDealAnim(tmpCardImg, player_endx, player_endy,
									"player");
							player_endx += 40;
							player_endy += 15;

							player_splithand.recieveDealtedCard(deck
									.dealtCard());
							updateInfo("split");
							playerMoves(player_splithand);
						}
					}.start();
				}
			}

		});

		stand.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!splitFlag) {
					hit.setEnabled(false);
					stand.setEnabled(false);
					doubl.setEnabled(false);
					split.setEnabled(false);
					surrend.setEnabled(false);
					dealerMoves(false);
				} else {
					splitFlag = false;
					doubl.setEnabled(true);
					player_endx = (float) (size.x / 2) + 40;
					player_endy = (float) (size.y - (size.y / 2.5)) + 15;

					ImageView tmpCardImg = getCardImage(deck.peek(), false);
					runDealAnim(tmpCardImg, player_endx, player_endy, "player");
					player_endx += 40;
					player_endy += 15;

					player_splithand.recieveDealtedCard(deck.dealtCard());
					updateInfo("split");
					playerMoves(player_splithand);
				}

			}

		});

		doubl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				player_endx += 100;
				player_endy -= 15;
				ImageView tmpCardImg = getCardImage(deck.peek(), false);
				runDealAnim(tmpCardImg, player_endx, player_endy, "player");
				curHand.setBetholder(curHand.getBetholder() * 2);
				curHand.recieveDealtedCard(deck.dealtCard());

				updateInfo(curHand.getName());

				if (!splitFlag) {
					hit.setEnabled(false);
					stand.setEnabled(false);
					doubl.setEnabled(false);
					split.setEnabled(false);
					surrend.setEnabled(false);
					new CountDownTimer(2000, 1000) {
						public void onFinish() {
							dealerMoves(false);
						}

						public void onTick(long millisUntilFinished) {
						}
					}.start();
				} else {
					new CountDownTimer(1500, 1000) {

						public void onTick(long millisUntilFinished) {
						}

						public void onFinish() {
							splitFlag = false;

							player_endx = (float) (size.x / 2) + 40;
							player_endy = (float) (size.y - (size.y / 2.5)) + 15;

							ImageView tmpCardImg = getCardImage(deck.peek(),
									false);
							runDealAnim(tmpCardImg, player_endx, player_endy,
									"player");
							player_endx += 40;
							player_endy += 15;

							player_splithand.recieveDealtedCard(deck
									.dealtCard());
							updateInfo("split");
							playerMoves(player_splithand);
						}
					}.start();
				}
			}

		});

		surrend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				curHand.setBetholder(curHand.getBetholder() / 2);
				hit.setEnabled(false);
				stand.setEnabled(false);
				doubl.setEnabled(false);
				split.setEnabled(false);
				surrend.setEnabled(false);
				dealerMoves(true);
			}

		});

		split.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				player_endx = 40;
				player_endy = (float) (size.y - (size.y / 2.5)) + 15;

				splitFlag = true;
				splitChecker = true;
				surrend.setEnabled(false);
				split.setEnabled(false);

				Animation transMove1 = new TranslateAnimation(splitImgCard1
						.getEndx(), 0, splitImgCard1.getEndy(), splitImgCard1
						.getEndy());
				transMove1.setDuration(750);
				transMove1.setFillEnabled(true);
				transMove1.setFillAfter(true);
				Animation transMove2 = new TranslateAnimation(splitImgCard2
						.getEndx(), (float) (size.x / 2), splitImgCard1
						.getEndy(), splitImgCard1.getEndy());
				transMove2.setDuration(750);
				transMove2.setFillEnabled(true);
				transMove2.setFillAfter(true);

				discarded.get(0).setEndx(0);
				discarded.get(2).setEndx((float) (size.x / 2));
				discarded.get(2).setEndy(splitImgCard1.getEndy());

				splitImgCard1.getCardImage().startAnimation(transMove1);
				splitImgCard2.getCardImage().startAnimation(transMove2);

				ImageView tmpCardImg = getCardImage(deck.peek(), false);
				runDealAnim(tmpCardImg, player_endx, player_endy, "player");
				player_endx += 40;
				player_endy += 15;

				player_splithand = new Hand("split");
				player_splithand.setBetholder(bet);
				Card splitcard1 = curHand.getHand().get(0);
				Card splitcard2 = curHand.getHand().get(1);
				curHand.resetHand();
				curHand.setBetholder(bet);

				curHand.recieveDealtedCard(splitcard1);
				curHand.recieveDealtedCard(deck.dealtCard());
				updateInfo("player");

				player_splithand.recieveDealtedCard(splitcard2);
			}

		});
	}

	private void dealerMoves(final boolean surrender) {
		int dealersize = 0;

		RelativeLayout.LayoutParams par = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		par.topMargin = (int) discarded.get(3).endy;
		par.rightMargin = (int) discarded.get(3).endx;
		flipCard.setLayoutParams(par);
		flipCard.bringToFront();
		flipCard.setImageResource(flipCardId);

		imageLinkedList.clear();

		score3TV.setText(dealer.getHardSoft());

		if (!dealer.blackJack()) {
			while (dealer.getBestScore() < 17) {
				dealersize++;
				imageLinkedList.add(new MyImageHolder(getCardImage(deck.peek(),
						false), "dealer", dealer_endx, dealer_endy));
				dealer_endx += 40;
				dealer.recieveDealtedCard(deck.dealtCard());
			}
			if (!imageLinkedList.isEmpty()) {
				MyImageHolder myh = imageLinkedList.pop();
				runDealAnim(myh.getCardImage(), myh.getEndx(), myh.getEndy(),
						myh.getName());
			}
		}
		score3TV.setText(dealer.getHardSoft());

		new CountDownTimer((dealersize * 1000), 1000) {
			public void onFinish() {
				if (surrender) {
					Toast.makeText(MainActivity.this, "Surrender, You Lose!",
							Toast.LENGTH_LONG).show();
					dealerWins(false, player.getBetholder());
				} else {
					if (splitChecker) {
						decideWinner(player, true);
					} else {
						decideWinner(player, false);
					}
				}
			}

			public void onTick(long millisUntilFinished) {

			}
		}.start();
	}

	private void decideWinner(Hand plhand, boolean anoth) {
		String winner = "";
		if (dealer.blackJack() && plhand.blackJack()) {
			push(anoth);
			winner = "Push!";
		} else if (plhand.blackJack()) {
			playerWins(anoth, plhand.getBetholder()
					+ (plhand.getBetholder() / 2));
			winner = "$ BlackJack $ You Win!";
		} else if (dealer.blackJack()) {
			dealerWins(anoth, plhand.getBetholder());
			winner = "You Lose!";
		} else if (plhand.busted()) {
			dealerWins(anoth, plhand.getBetholder());
			winner = "Busted, You Lose!";
		} else if (dealer.busted() && !plhand.busted()) {
			playerWins(anoth, plhand.getBetholder());
			winner = "You Win!";
		} else if (plhand.getBestScore() > dealer.getBestScore()) {
			playerWins(anoth, plhand.getBetholder());
			winner = "You Win!";
		} else if (dealer.getBestScore() > plhand.getBestScore()) {
			dealerWins(anoth, plhand.getBetholder());
			winner = "You Lose!";
		} else {
			push(anoth);
			winner = "Push!";
		}

		Toast.makeText(MainActivity.this, winner, Toast.LENGTH_LONG).show();
	}

	private void playerWins(boolean anoth, final int handbet) {
		numbhandplayed++;
		player_bank += handbet;

		if (anoth) {
			decideWinner(player_splithand, false);
		} else {
			new CountDownTimer(5000, 1000) {

				public void onTick(long millisUntilFinished) {
					if (millisUntilFinished / 1000 == 1)
						runDiscardCardsAnim();
				}

				public void onFinish() {
					if (deck.sizeOfDeckStack() < deck.getCutOff()) {
						endOfGame();
					} else {

						playRound();
					}
				}
			}.start();
		}
	}

	private void dealerWins(boolean anoth, final int handbet) {
		numbhandplayed++;
		player_bank -= handbet;
		if (anoth) {
			decideWinner(player_splithand, false);
		} else {
			new CountDownTimer(5000, 1000) {

				public void onTick(long millisUntilFinished) {
					if (millisUntilFinished / 1000 == 1)
						runDiscardCardsAnim();
				}

				public void onFinish() {
					if (deck.sizeOfDeckStack() < deck.getCutOff()) {
						endOfGame();
					} else {
						playRound();
					}
				}
			}.start();
		}
	}

	private void push(boolean anoth) {
		numbhandplayed++;
		if (anoth) {
			decideWinner(player_splithand, false);
		} else {
			new CountDownTimer(5000, 1000) {

				public void onTick(long millisUntilFinished) {
					if (millisUntilFinished / 1000 == 1)
						runDiscardCardsAnim();
				}

				public void onFinish() {
					if (deck.sizeOfDeckStack() < deck.getCutOff()) {
						endOfGame();
					} else {
						playRound();
					}
				}
			}.start();
		}
	}

	private void beginDeal() {
		saveGameState();
		countTV.setText("Count: " + deck.getRunning_count() + ", True Count: "
				+ deck.getTrue_count());

		imageLinkedList.clear();
		discarded.clear();

		for (int i = 0; i < 2; i++) {
			imageLinkedList.add(new MyImageHolder(getCardImage(deck.peek(),
					false), "player", player_endx, player_endy));
			player_endx += 40;
			player_endy += 15;
			player.recieveDealtedCard(deck.dealtCard());

			if (i == 1) {
				imageLinkedList.add(new MyImageHolder(flipCard, "dealer",
						dealer_endx, dealer_endy));
				getCardImage(deck.peek(), true);
				dealer_endx += 40;
			} else {
				imageLinkedList.add(new MyImageHolder(getCardImage(deck.peek(),
						false), "dealer", dealer_endx, dealer_endy));
				dealer_endx += 145;
			}
			dealer.recieveDealtedCard(deck.dealtCard());
		}

		if (player.getHand().get(0).getValue() == player.getHand().get(1)
				.getValue()) {
			split.setEnabled(true);
			splitImgCard1 = imageLinkedList.get(0);
			splitImgCard2 = imageLinkedList.get(2);
		}

		updateInfo("player");
		MyImageHolder myh = imageLinkedList.pop();
		runDealAnim(myh.getCardImage(), myh.getEndx(), myh.getEndy(),
				myh.getName());

		new CountDownTimer(5500, 1000) {

			public void onTick(long millisUntilFinished) {
			}

			public void onFinish() {

				if (player.blackJack() || dealer.blackJack()) {
					hit.setEnabled(false);
					stand.setEnabled(false);
					doubl.setEnabled(false);
					split.setEnabled(false);
					surrend.setEnabled(false);
					dealerMoves(false);
				} else {
					playerMoves(player);
				}
			}
		}.start();
	}

	private void endOfGame() {
		popsumTV_handsplayed.setText("Hands Played: " + numbhandplayed);
		popsumTV_correctplays.setText("Correct Moves: " + correctPlay);
		popsumTV_incorrectplays.setText("Wrong Moves: " + incorrectPlay);
		popsumTV_winnings.setText("Winnings/Losses: $" + (player_bank - 1000));
		popsumTV_bank.setText("Bank: $" + player_bank);

		popup_summaryAlert.show();
		int width = (size.x > 1000) ? 750 : size.x;
		popup_summaryAlert.getWindow().setLayout(width,
				LayoutParams.WRAP_CONTENT);
	}

	private void saveGameState() {

		if (savedObject.exists()) {
			try {
				FileOutputStream f_out = new FileOutputStream(savedObject);
				ObjectOutputStream obj_out = new ObjectOutputStream(f_out);

				obj_out.writeObject(deck);
				obj_out.writeInt(player_bank);
				obj_out.writeInt(correctPlay);
				obj_out.writeInt(incorrectPlay);
				obj_out.writeBoolean(popsetSwitch_repBet.isChecked());
				obj_out.writeBoolean(popsetSwitch_hint.isChecked());
				obj_out.writeBoolean(popsetSwitch_score.isChecked());
				obj_out.writeBoolean(popsetSwitch_count.isChecked());
				obj_out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {

			File dir = new File(sdPath + "/BlackJackCCTP/saved");
			dir.mkdirs();
			savedObject = new File(dir, "myobject.data");

			try {
				FileOutputStream f_out = new FileOutputStream(savedObject);
				ObjectOutputStream obj_out = new ObjectOutputStream(f_out);

				obj_out.writeObject(deck);
				obj_out.writeInt(player_bank);
				obj_out.writeInt(correctPlay);
				obj_out.writeInt(incorrectPlay);
				obj_out.writeBoolean(popsetSwitch_repBet.isChecked());
				obj_out.writeBoolean(popsetSwitch_hint.isChecked());
				obj_out.writeBoolean(popsetSwitch_score.isChecked());
				obj_out.writeBoolean(popsetSwitch_count.isChecked());
				obj_out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void restoreGameState() {
		try {
			FileInputStream f_in = new FileInputStream(savedObject);
			ObjectInputStream obj_in = new ObjectInputStream(f_in);

			Object obj = obj_in.readObject();
			if (obj instanceof Deck) {
				deck = (Deck) obj;
			}
			player_bank = obj_in.readInt();
			correctPlay = obj_in.readInt();
			incorrectPlay = obj_in.readInt();
			if (obj_in.readBoolean())
				popsetSwitch_repBet.setChecked(true);
			else
				popsetSwitch_repBet.setChecked(false);
			if (obj_in.readBoolean()) {
				popsetSwitch_hint.setChecked(true);
			} else {
				popsetSwitch_hint.setChecked(false);
			}
			if (obj_in.readBoolean()) {
				popsetSwitch_score.setChecked(true);
			} else {
				popsetSwitch_score.setChecked(false);
			}
			if (obj_in.readBoolean()) {
				popsetSwitch_count.setChecked(true);
			} else {
				popsetSwitch_count.setChecked(false);
			}
			f_in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setupSettingAlert() {
		LayoutInflater inflater = this.getLayoutInflater();
		View popup_setting = inflater.inflate(R.layout.popup_setting, null);

		popsetTV_errorRatio = (TextView) popup_setting
				.findViewById(R.id.popsetTV_errorRatio);
		popsetTV_correctMoves = (TextView) popup_setting
				.findViewById(R.id.popsetTV_correctMoves);
		popsetTV_incorrectMoves = (TextView) popup_setting
				.findViewById(R.id.popsetTV_incorrectMoves);
		popsetTV_runCount = (TextView) popup_setting
				.findViewById(R.id.popsetTV_runCount);
		popsetTV_trueCount = (TextView) popup_setting
				.findViewById(R.id.popsetTV_trueCount);
		popsetTV_handsPlayed = (TextView) popup_setting
				.findViewById(R.id.popsetTV_handsPlayed);
		popsetTV_remDeck = (TextView) popup_setting
				.findViewById(R.id.popsetTV_remDeck);
		popsetTV_bank = (TextView) popup_setting
				.findViewById(R.id.popsetTV_bank);
		popsetTV_winnings = (TextView) popup_setting
				.findViewById(R.id.popsetTV_winnings);
		popsetTV_viewChart = (TextView) popup_setting
				.findViewById(R.id.popsetTV_viewChart);
		popsetTV_viewBJInfo = (TextView) popup_setting
				.findViewById(R.id.popsetTV_viewBJInfo);
		popsetTV_startNewShoe = (TextView) popup_setting
				.findViewById(R.id.popsetTV_startNewShoe);
		popsetTV_resetGame = (TextView) popup_setting
				.findViewById(R.id.popsetTV_resetGame);
		popsetTV_instruct = (TextView) popup_setting
				.findViewById(R.id.popsetTV_instruct);

		popsetSwitch_repBet = (Switch) popup_setting
				.findViewById(R.id.popsetSwitch_repBet);
		popsetSwitch_hint = (Switch) popup_setting
				.findViewById(R.id.popsetSwitch_hint);
		popsetSwitch_score = (Switch) popup_setting
				.findViewById(R.id.popsetSwitch_score);
		popsetSwitch_count = (Switch) popup_setting
				.findViewById(R.id.popsetSwitch_count);
		popsetSwitch_hint.setChecked(true);

		popsetTV_viewChart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				popup_viewChartAlert.show();
				int width = (size.x > 1000) ? 1000 : size.x;
				int height = (int) ((size.y > 1500) ? 1200 : size.y / 1.2);
				popup_viewChartAlert.getWindow().setLayout(width, height);
			}

		});

		popsetTV_viewBJInfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this,
						BlackJackInfoActivity.class);
				startActivity(i);
			}

		});

		popsetTV_instruct.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				popup_instructionAlert.show();
				int width = (size.x > 1000) ? 1000 : size.x;
				popup_instructionAlert.getWindow().setLayout(width,
						LayoutParams.WRAP_CONTENT);
			}

		});

		popsetTV_startNewShoe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				popup_settingAlert.dismiss();
				runDiscardCardsAnim();
				Toast.makeText(MainActivity.this, "Shuffling...",
						Toast.LENGTH_LONG).show();

				new CountDownTimer(5000, 1000) {

					public void onTick(long millisUntilFinished) {
					}

					public void onFinish() {
						Toast.makeText(MainActivity.this, "Starting New Shoe",
								Toast.LENGTH_SHORT).show();
						numbhandplayed = 0;
						correctPlay = 0;
						incorrectPlay = 0;

						deck.shuffleDeck();
						playRound();
					}
				}.start();
			}

		});

		popsetTV_resetGame.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				popup_settingAlert.dismiss();
				runDiscardCardsAnim();
				Toast.makeText(MainActivity.this, "Shuffling...",
						Toast.LENGTH_LONG).show();
				new CountDownTimer(5000, 1000) {

					public void onTick(long millisUntilFinished) {
					}

					public void onFinish() {
						Toast.makeText(MainActivity.this, "Starting New Game",
								Toast.LENGTH_SHORT).show();

						player_bank = 1000;
						bet = 5;
						numbhandplayed = 0;
						correctPlay = 0;
						incorrectPlay = 0;

						deck = new Deck(6);
						player = new Hand("player");
						dealer = new Hand("dealer");
						playRound();
					}
				}.start();
			}
		});

		popsetSwitch_repBet
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							repeatBet = true;
						} else {
							repeatBet = false;
						}
					}

				});

		popsetSwitch_score
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							settingEnableHandScore();
						} else {
							settingDisableHandScore();
						}
					}

				});

		popsetSwitch_count
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							settingEnableCount();
						} else {
							settingDisableCount();
						}
					}

				});

		popsetSwitch_hint
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							settingEnableTips = true;
						} else {
							settingEnableTips = false;
						}
					}

				});

		popup_settingAlert = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

		builder.setTitle("Info and Settings");
		builder.setView(popup_setting);
		builder.setPositiveButton("Okay!",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});

		popup_settingAlert = builder.create();
	}

	private void setupPlaceBetAlert() {
		LayoutInflater inflater = this.getLayoutInflater();
		View popup_placebet = inflater.inflate(R.layout.popup_placebet, null);
		popbetTV_bet = (TextView) popup_placebet
				.findViewById(R.id.popbetTV_bet);
		popbetButt_chip5 = (Button) popup_placebet
				.findViewById(R.id.popbetButt_chip5);
		popbetButt_chip25 = (Button) popup_placebet
				.findViewById(R.id.popbetButt_chip25);
		popbetButt_chip100 = (Button) popup_placebet
				.findViewById(R.id.popbetButt_chip100);
		popbetButt_chip500 = (Button) popup_placebet
				.findViewById(R.id.popbetButt_chip500);
		popbetButt_chip1000 = (Button) popup_placebet
				.findViewById(R.id.popbetButt_chip1000);
		popbetTV_bet.setText(Integer.toString(bet));

		popbetButt_chip5.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (bet + 5 > player_bank) {
					Toast.makeText(MainActivity.this, "Not Enough Fund.",
							Toast.LENGTH_LONG).show();
				} else {
					bet += 5;
					popbetTV_bet.setText(Integer.toString(bet));
				}
			}

		});
		popbetButt_chip25.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (bet + 25 > player_bank) {
					Toast.makeText(MainActivity.this, "Not Enough Fund.",
							Toast.LENGTH_LONG).show();
				} else {
					bet += 25;
					popbetTV_bet.setText(Integer.toString(bet));
				}
			}

		});
		popbetButt_chip100.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (bet + 100 > player_bank) {
					Toast.makeText(MainActivity.this, "Not Enough Fund.",
							Toast.LENGTH_LONG).show();
				} else {
					bet += 100;
					popbetTV_bet.setText(Integer.toString(bet));
				}
			}

		});
		popbetButt_chip500.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (bet + 500 > player_bank) {
					Toast.makeText(MainActivity.this, "Not Enough Fund.",
							Toast.LENGTH_LONG).show();
				} else {
					bet += 500;
					popbetTV_bet.setText(Integer.toString(bet));
				}
			}

		});
		popbetButt_chip1000.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (bet + 1000 > player_bank) {
					Toast.makeText(MainActivity.this, "Not Enough Fund.",
							Toast.LENGTH_LONG).show();
				} else {
					bet += 1000;
					popbetTV_bet.setText(Integer.toString(bet));
				}
			}

		});

		popup_placeBetAlert = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

		builder.setView(popup_placebet);
		builder.setCancelable(false);
		builder.setNeutralButton("Clear",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		builder.setPositiveButton("Place Bet",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						player.setBetholder(bet);
						beginDeal();
					}
				});
		popup_placeBetAlert = builder.create();
	}

	private void setupViewChartAlert() {
		LayoutInflater inflater = this.getLayoutInflater();
		View popup_chart = inflater.inflate(R.layout.popup_viewchart, null);

		popup_viewChartAlert = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

		builder.setTitle("Basic Strategy Charts");
		builder.setView(popup_chart);
		builder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						popup_settingAlert.dismiss();
					}
				});
		builder.setNegativeButton("Back",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				});

		popup_viewChartAlert = builder.create();
	}

	private void setupOpeningMenuAlert() {
		LayoutInflater inflater = this.getLayoutInflater();
		View popup_opmenu = inflater.inflate(R.layout.popup_openingmenu, null);
		TextView resumeGame = (TextView) popup_opmenu
				.findViewById(R.id.resumeGame);

		String sdPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		File savedGame = new File(sdPath + "/BlackJackCCTP/saved/myobject.data");

		if (!savedGame.exists()) {
			resumeGame.setEnabled(false);
		} else {
			resumeGame.setEnabled(true);
			resumeGame.setBackgroundResource(R.drawable.btn_action);
		}

		((TextView) popup_opmenu.findViewById(R.id.learnBJ))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent i = new Intent(MainActivity.this,
								BlackJackInfoActivity.class);
						startActivity(i);
					}

				});

		((TextView) popup_opmenu.findViewById(R.id.startGame))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						popup_openingMenuAlert.dismiss();
						if (showInstruction) {
							newgame = true;
							displayStartInstruction();
						} else {
							deck = new Deck(6);
							player = new Hand("player");
							dealer = new Hand("dealer");
							playRound();
						}
					}

				});

		((TextView) popup_opmenu.findViewById(R.id.resumeGame))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						popup_openingMenuAlert.dismiss();
						restoreGameState();
						player = new Hand("player");
						dealer = new Hand("dealer");
						playRound();
					}

				});

		popup_openingMenuAlert = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

		builder.setView(popup_opmenu);
		builder.setCancelable(false);
		popup_openingMenuAlert = builder.create();
	}

	private void setupSummaryAlert() {
		LayoutInflater inflater = this.getLayoutInflater();
		View popup_summary = inflater.inflate(R.layout.popup_summary, null);
		popsumTV_handsplayed = (TextView) popup_summary
				.findViewById(R.id.popsumTV_handsplayed);
		popsumTV_correctplays = (TextView) popup_summary
				.findViewById(R.id.popsumTV_correctplays);
		popsumTV_incorrectplays = (TextView) popup_summary
				.findViewById(R.id.popsumTV_incorrectplays);
		popsumTV_winnings = (TextView) popup_summary
				.findViewById(R.id.popsumTV_winnings);
		popsumTV_bank = (TextView) popup_summary
				.findViewById(R.id.popsumTV_bank);

		popup_summaryAlert = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("End of Shoe! Summary");
		builder.setPositiveButton("Begin New Shoe",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(MainActivity.this, "Shuffling...",
								Toast.LENGTH_LONG).show();

						new CountDownTimer(5000, 1000) {

							public void onTick(long millisUntilFinished) {
							}

							public void onFinish() {
								Toast.makeText(MainActivity.this,
										"Starting New Shoe", Toast.LENGTH_SHORT)
										.show();
								numbhandplayed = 0;
								correctPlay = 0;
								incorrectPlay = 0;

								deck.shuffleDeck();
								playRound();
							}
						}.start();
					}

				});
		builder.setView(popup_summary);
		builder.setCancelable(false);
		popup_summaryAlert = builder.create();
	}

	private void setupInstructionAlert() {
		if (!savedSetting.exists()) {
			File dir = new File(sdPath + "/BlackJackCCTP/saved");
			dir.mkdirs();
			savedSetting = new File(dir, "setting.data");
			try {
				FileOutputStream f_out = new FileOutputStream(savedSetting);
				ObjectOutputStream obj_out = new ObjectOutputStream(f_out);

				obj_out.writeBoolean(showInstruction);
				obj_out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		LayoutInflater inflater = this.getLayoutInflater();
		View popup_instruction = inflater.inflate(R.layout.popup_instruction,
				null);

		popinstCB_hide = (CheckBox) popup_instruction
				.findViewById(R.id.popinst_hide);
		popinstCB_hide
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {

						if (isChecked) {
							showInstruction = false;
						} else {
							showInstruction = true;
						}

						try {
							FileOutputStream f_out = new FileOutputStream(
									savedSetting);
							ObjectOutputStream obj_out = new ObjectOutputStream(
									f_out);

							obj_out.writeBoolean(showInstruction);
							obj_out.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				});

		popup_instructionAlert = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Instructions");
		builder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						popup_settingAlert.dismiss();
					}

				});
		builder.setNegativeButton("Back",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}

				});
		builder.setView(popup_instruction);
		popup_instructionAlert = builder.create();
	}

	private void displayStartInstruction() {
		LayoutInflater inflater = this.getLayoutInflater();
		View popup_instruction_temp = inflater.inflate(
				R.layout.popup_instruction, null);

		((CheckBox) popup_instruction_temp.findViewById(R.id.popinst_hide))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {

						if (isChecked) {
							showInstruction = false;
						} else {
							showInstruction = true;
						}

						try {
							FileOutputStream f_out = new FileOutputStream(
									savedSetting);
							ObjectOutputStream obj_out = new ObjectOutputStream(
									f_out);

							obj_out.writeBoolean(showInstruction);
							obj_out.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				});

		AlertDialog popup_instructionAlertTemp = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setCancelable(false);
		builder.setTitle("Instructions");
		builder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						deck = new Deck(6);
						player = new Hand("player");
						dealer = new Hand("dealer");
						playRound();

					}

				});
		builder.setView(popup_instruction_temp);
		popup_instructionAlertTemp = builder.create();
		popup_instructionAlertTemp.show();
		int width = (size.x > 1000) ? 1000 : size.x;
		popup_instructionAlertTemp.getWindow().setLayout(width,
				LayoutParams.WRAP_CONTENT);
	}

	private void placeBet() {
		bet = (bet > player_bank) ? 5 : bet;
		popup_placeBetAlert.setTitle("Bank: $" + player_bank);
		popup_placeBetAlert.show();
		int width = (size.x > 1000) ? 1000 : size.x;
		popup_placeBetAlert.getWindow().setLayout(width,
				LayoutParams.WRAP_CONTENT);

		Button neutralButton = popup_placeBetAlert
				.getButton(DialogInterface.BUTTON_NEUTRAL);
		neutralButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View onClick) {
				bet = 0;
				popbetTV_bet.setText(Integer.toString(bet));
			}
		});
	}

	protected void prepInfoAndSettings() {
		if (correctPlay - incorrectPlay > 0)
			popsetTV_errorRatio.setTextColor(Color.GREEN);
		else if (correctPlay - incorrectPlay < 0)
			popsetTV_errorRatio.setTextColor(Color.RED);
		popsetTV_errorRatio.setText(Integer.toString(correctPlay
				- incorrectPlay));
		popsetTV_correctMoves.setText(Integer.toString(correctPlay));
		popsetTV_incorrectMoves.setText(Integer.toString(incorrectPlay));
		popsetTV_runCount.setText(Integer.toString(deck.getRunning_count()));
		if (deck.getTrue_count() > 0)
			popsetTV_trueCount.setTextColor(Color.GREEN);
		else if (deck.getTrue_count() < 0)
			popsetTV_trueCount.setTextColor(Color.RED);
		popsetTV_trueCount.setText(Integer.toString(deck.getTrue_count()));
		popsetTV_handsPlayed.setText(Integer.toString(numbhandplayed));
		popsetTV_remDeck.setText(Integer.toString(deck.getRemDeck()));
		popsetTV_bank.setText("$" + Integer.toString(player_bank));
		if (player_bank - 1000 >= 0)
			popsetTV_winnings.setTextColor(Color.GREEN);
		else
			popsetTV_winnings.setTextColor(Color.RED);
		popsetTV_winnings.setText("$" + Integer.toString(player_bank - 1000));
	}

	protected void updateInfo(String name) {
		curPlayerHand = name;
		if (name.equalsIgnoreCase("player")) {
			score1TV.setText(player.getHardSoft());
			correctMove = bsc.getCorrectMoves(player, dealer.getHand().get(0)
					.getValue());
		} else {
			correctMove = bsc.getCorrectMoves(player_splithand, dealer
					.getHand().get(0).getValue());
			score2TV.setText(player_splithand.getHardSoft());
		}

		shouldHit = (correctMove == 'h') ? true : false;
		shouldStand = (correctMove == 's') ? true : false;
		shouldDouble = (correctMove == 'd' || correctMove == 'c') ? true
				: false;
		shouldSurrender = (correctMove == 'x') ? true : false;
		shouldSplit = (correctMove == 'y') ? true : false;
	}

	private void settingDisableHandScore() {
		score1TV.setVisibility(View.INVISIBLE);
		score2TV.setVisibility(View.INVISIBLE);
		score3TV.setVisibility(View.INVISIBLE);
	}

	private void settingDisableCount() {
		countTV.setVisibility(View.INVISIBLE);
	}

	private void settingEnableHandScore() {
		score1TV.setVisibility(View.VISIBLE);
		score2TV.setVisibility(View.VISIBLE);
		score3TV.setVisibility(View.VISIBLE);
	}

	private void settingEnableCount() {
		countTV.setVisibility(View.VISIBLE);
	}

	private void initializeViews() {
		score1TV = (TextView) this.findViewById(R.id.score1);
		score2TV = (TextView) this.findViewById(R.id.score2);
		score3TV = (TextView) this.findViewById(R.id.score3);
		countTV = (TextView) this.findViewById(R.id.count);

		flipCard = (ImageView) this.findViewById(R.id.flipcard);

		hit = (Button) this.findViewById(R.id.hit);
		stand = (Button) this.findViewById(R.id.stand);
		doubl = (Button) this.findViewById(R.id.doubl);
		split = (Button) this.findViewById(R.id.split);
		surrend = (Button) this.findViewById(R.id.surrender);
		setting = (Button) findViewById(R.id.setting);
		setting.bringToFront();

		settingDisableHandScore();
		settingDisableCount();
	}

	private void resetForNewHand() {
		splitFlag = false;
		splitChecker = false;
		player_endx = (float) (size.x / 4);
		player_endy = (float) (size.y - (size.y / 2.5));
		dealer_endx = (float) (size.x / 4);
		dealer_endy = 0;
		flipCard.setImageResource(R.drawable.back_red);
		flipCard.setVisibility(View.INVISIBLE);

		score1TV.setText("");
		score2TV.setText("");
		score3TV.setText("");
		countTV.setText("");

		hit.setEnabled(true);
		stand.setEnabled(true);
		doubl.setEnabled(true);
		surrend.setEnabled(true);
		split.setEnabled(false);

		hitEnable = false;
		surrendEnable = false;
		splitEnable = false;
		doublEnable = false;
		standEnable = false;
	}

	public void runDiscardCardsAnim() {
		for (int i = 0; i < discarded.size(); i++) {
			MyImageHolder card = discarded.get(i);

			Animation discard = new TranslateAnimation(card.getEndx(), -500,
					card.getEndy(), -700);
			discard.setDuration(1000);
			discard.setFillAfter(true);
			card.getCardImage().startAnimation(discard);
			card.getCardImage().setVisibility(View.GONE);
		}
	}

	public void runDealAnim(ImageView card, float endx, float endy, String name) {
		discarded.add(new MyImageHolder(card, name, endx, endy));
		card.setVisibility(View.VISIBLE);
		card.bringToFront();

		translateAnim = new TranslateAnimation(CARD_ANIM_START_X, endx,
				CARD_ANIM_START_Y, endy);
		translateAnim.setAnimationListener(this);
		int duration = (name.equalsIgnoreCase("player")) ? 1500 : 1000;
		translateAnim.setDuration(duration);
		translateAnim.setFillEnabled(true);
		translateAnim.setFillAfter(true);

		card.startAnimation(translateAnim);
	}

	private void alertIncorrectPlay(final String wrongPlay) {
		String messgBuilder = "";

		String corMov = (shouldHit) ? "HIT" : (shouldStand) ? "STAND"
				: (shouldDouble) ? "DOUBLE DOWN" : (shouldSplit) ? "SPLIT"
						: "SURRENDER";

		Hand hand = (curPlayerHand.equalsIgnoreCase("player")) ? player
				: player_splithand;
		String handtype = hand.getHandType();
		if (handtype.contains("hard")) {
			messgBuilder = "When you have a Hard Total of "
					+ hand.getBestScore() + ", and the dealer is showing a(n) "
					+ dealer.getDealerFaceUpCard() + "; You should " + corMov
					+ ", not " + wrongPlay;
		} else if (handtype.contains("soft")) {
			messgBuilder = "When you have a Soft Total of "
					+ hand.getHardSoft() + ", and the dealer is showing a(n) "
					+ dealer.getDealerFaceUpCard() + "; You should " + corMov
					+ ", not " + wrongPlay;
		} else {
			messgBuilder = "When you have a Pair of "
					+ hand.getDealerFaceUpCard()
					+ "'s, and the dealer is showing a(n) "
					+ dealer.getDealerFaceUpCard() + "; You should " + corMov
					+ ", not " + wrongPlay;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("Care for a Tip");
		builder.setCancelable(false);
		builder.setMessage(messgBuilder);
		builder.setNeutralButton("View Chart",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						popup_viewChartAlert.show();
						int width = (size.x > 1000) ? 1000 : size.x;
						int height = (int) ((size.y > 1500) ? 1200
								: size.y / 1.2);
						popup_viewChartAlert.getWindow().setLayout(width,
								height);

						if (wrongPlay.equalsIgnoreCase("surrender"))
							surrend.performClick();
						else if (wrongPlay.equalsIgnoreCase("stand"))
							stand.performClick();
						else if (wrongPlay.equalsIgnoreCase("hit"))
							hit.performClick();
						else if (wrongPlay.equals("split"))
							split.performClick();
						else
							doubl.performClick();
					}

				});
		builder.setPositiveButton("Okay",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (wrongPlay.equalsIgnoreCase("surrender"))
							surrend.performClick();
						else if (wrongPlay.equalsIgnoreCase("stand"))
							stand.performClick();
						else if (wrongPlay.equalsIgnoreCase("hit"))
							hit.performClick();
						else if (wrongPlay.equals("split"))
							split.performClick();
						else
							doubl.performClick();
					}
				});
		builder.show();
	}

	@Override
	public void onSwipe(int direction) {
		switch (direction) {

		case SimpleGestureFilter.SURRENDER:
			if (surrend.isEnabled()) {
				if (shouldSurrender) {
					surrend.performClick();
					correctPlay += 1;
				} else if (settingEnableTips) {
					incorrectPlay += 1;
					alertIncorrectPlay("surrender");
				} else {
					incorrectPlay += 1;
					surrend.performClick();
				}
			} else if (settingEnableTips) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						MainActivity.this);
				builder.setTitle("Can not perform this action");
				builder.setMessage("Surrender is only allowed on the first move of a hand.");
				builder.setPositiveButton("Okay",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});
				builder.show();
			}
			break;
		case SimpleGestureFilter.STAND:
			if (stand.isEnabled()) {
				if (shouldStand) {
					correctPlay += 1;
					stand.performClick();
				} else if (settingEnableTips) {
					incorrectPlay += 1;
					alertIncorrectPlay("stand");
				} else {
					incorrectPlay += 1;
					stand.performClick();
				}

			}
			break;
		}
	}

	@Override
	public void onSingleTap() {
		if (hit.isEnabled()) {
			if (shouldHit) {
				correctPlay += 1;
				hit.performClick();
			} else if (settingEnableTips) {
				incorrectPlay += 1;
				alertIncorrectPlay("hit");
			} else {
				incorrectPlay += 1;
				hit.performClick();
			}
		}
	}

	@Override
	public void onDoubleTap() {
		if (doubl.isEnabled()) {
			if (shouldDouble) {
				correctPlay += 1;
				doubl.performClick();
			} else if (settingEnableTips) {
				incorrectPlay += 1;
				alertIncorrectPlay("double down");
			} else {
				incorrectPlay += 1;
				doubl.performClick();
			}

		} else if (settingEnableTips) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					MainActivity.this);
			builder.setTitle("Can not perform this action");
			builder.setMessage("Double Down is only allowed on the first move of a hand.");
			builder.setPositiveButton("Okay",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			builder.show();
		}
	}

	public void onTwoFingerTap() {
		if (split.isEnabled()) {
			if (shouldSplit) {
				correctPlay += 1;
				split.performClick();
			} else if (settingEnableTips) {
				incorrectPlay += 1;
				alertIncorrectPlay("split");
			} else {
				incorrectPlay += 1;
				split.performClick();
			}

		} else if (settingEnableTips) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					MainActivity.this);
			builder.setTitle("Can not perform this action");
			builder.setMessage("Splitting is only allowed for same pairs.\n"
					+ "There is no multiple splitting.");
			builder.setPositiveButton("Okay",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			builder.show();
		}
	}

	@Override
	public void onAnimationStart(Animation animation) {
		if (hit.isEnabled()) {
			hit.setEnabled(false);
			hitEnable = true;
		}
		if (stand.isEnabled()) {
			stand.setEnabled(false);
			standEnable = true;
		}
		if (doubl.isEnabled()) {
			doubl.setEnabled(false);
			doublEnable = true;
		}
		if (split.isEnabled()) {
			split.setEnabled(false);
			splitEnable = true;
		}
		if (surrend.isEnabled()) {
			surrend.setEnabled(false);
			surrendEnable = true;
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (animation == translateAnim) {
			if (!imageLinkedList.isEmpty()) {
				MyImageHolder myh = imageLinkedList.pop();
				runDealAnim(myh.getCardImage(), myh.getEndx(), myh.getEndy(),
						myh.getName());
			} else {
				if (hitEnable) {
					hit.setEnabled(true);
					hitEnable = false;
				}
				if (standEnable) {
					stand.setEnabled(true);
					standEnable = false;
				}
				if (doublEnable) {
					doubl.setEnabled(true);
					doublEnable = false;
				}
				if (splitEnable) {
					split.setEnabled(true);
					splitEnable = false;
				}
				if (surrendEnable) {
					surrend.setEnabled(true);
					surrendEnable = false;
				}

				if (shouldDouble && !doubl.isEnabled() && correctMove == 'd') {
					shouldDouble = false;
					shouldHit = true;
				} else if (shouldDouble && !doubl.isEnabled()
						&& correctMove == 'c') {
					shouldDouble = false;
					shouldStand = true;
				} else if (shouldSurrender && !surrend.isEnabled()) {
					shouldSurrender = false;
					shouldHit = true;
				} else if ((shouldSplit && !split.isEnabled())) {
					Character move = 'p';
					if (curPlayerHand.equalsIgnoreCase("player")) {
						move = bsc.getHardTotalChart(player, dealer.getHand()
								.get(0).getValue());
					} else {
						move = bsc.getHardTotalChart(player_splithand, dealer
								.getHand().get(0).getValue());
					}
					shouldHit = (move == 'h') ? true : false;
					shouldStand = (move == 's') ? true : false;
					shouldDouble = (move == 'd' || move == 'c') ? true : false;
					shouldSurrender = (move == 'x') ? true : false;

					if (shouldDouble && !doubl.isEnabled() && move == 'd') {
						shouldDouble = false;
						shouldHit = true;
					} else if (shouldSurrender && !surrend.isEnabled()) {
						shouldSurrender = false;
						shouldHit = true;
					}
				}
			}

		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		Toast.makeText(getApplicationContext(), "Animation Repeat",
				Toast.LENGTH_SHORT).show();
	}

	public ImageView getCardImage(Card card, boolean getflipid) {
		ImageView selectedImage = new ImageView(this);
		int rId = 0;

		switch (card.getRank()) {
		case 1:
			rId = ((card.getSuit().equalsIgnoreCase("spades")) ? R.drawable.spade1
					: (card.getSuit().equalsIgnoreCase("clubs")) ? R.drawable.club1
							: (card.getSuit().equalsIgnoreCase("diamonds")) ? R.drawable.diamond1
									: R.drawable.heart1);
			break;
		case 2:
			rId = ((card.getSuit().equalsIgnoreCase("spades")) ? R.drawable.spade2
					: (card.getSuit().equalsIgnoreCase("clubs")) ? R.drawable.club2
							: (card.getSuit().equalsIgnoreCase("diamonds")) ? R.drawable.diamond2
									: R.drawable.heart2);
			break;
		case 3:
			rId = ((card.getSuit().equalsIgnoreCase("spades")) ? R.drawable.spade3
					: (card.getSuit().equalsIgnoreCase("clubs")) ? R.drawable.club3
							: (card.getSuit().equalsIgnoreCase("diamonds")) ? R.drawable.diamond3
									: R.drawable.heart3);
			break;
		case 4:
			rId = ((card.getSuit().equalsIgnoreCase("spades")) ? R.drawable.spade4
					: (card.getSuit().equalsIgnoreCase("clubs")) ? R.drawable.club4
							: (card.getSuit().equalsIgnoreCase("diamonds")) ? R.drawable.diamond4
									: R.drawable.heart4);
			break;
		case 5:
			rId = ((card.getSuit().equalsIgnoreCase("spades")) ? R.drawable.spade5
					: (card.getSuit().equalsIgnoreCase("clubs")) ? R.drawable.club5
							: (card.getSuit().equalsIgnoreCase("diamonds")) ? R.drawable.diamond5
									: R.drawable.heart5);
			break;
		case 6:
			rId = ((card.getSuit().equalsIgnoreCase("spades")) ? R.drawable.spade6
					: (card.getSuit().equalsIgnoreCase("clubs")) ? R.drawable.club6
							: (card.getSuit().equalsIgnoreCase("diamonds")) ? R.drawable.diamond6
									: R.drawable.heart6);
			break;
		case 7:
			rId = ((card.getSuit().equalsIgnoreCase("spades")) ? R.drawable.spade7
					: (card.getSuit().equalsIgnoreCase("clubs")) ? R.drawable.club7
							: (card.getSuit().equalsIgnoreCase("diamonds")) ? R.drawable.diamond7
									: R.drawable.heart7);
			break;

		case 8:
			rId = ((card.getSuit().equalsIgnoreCase("spades")) ? R.drawable.spade8
					: (card.getSuit().equalsIgnoreCase("clubs")) ? R.drawable.club8
							: (card.getSuit().equalsIgnoreCase("diamonds")) ? R.drawable.diamond8
									: R.drawable.heart8);
			break;
		case 9:
			rId = ((card.getSuit().equalsIgnoreCase("spades")) ? R.drawable.spade9
					: (card.getSuit().equalsIgnoreCase("clubs")) ? R.drawable.club9
							: (card.getSuit().equalsIgnoreCase("diamonds")) ? R.drawable.diamond9
									: R.drawable.heart9);
			break;
		case 10:
			rId = ((card.getSuit().equalsIgnoreCase("spades")) ? R.drawable.spade10
					: (card.getSuit().equalsIgnoreCase("clubs")) ? R.drawable.club10
							: (card.getSuit().equalsIgnoreCase("diamonds")) ? R.drawable.diamond10
									: R.drawable.heart10);
			break;
		case 11:
			rId = ((card.getSuit().equalsIgnoreCase("spades")) ? R.drawable.spade11
					: (card.getSuit().equalsIgnoreCase("clubs")) ? R.drawable.club11
							: (card.getSuit().equalsIgnoreCase("diamonds")) ? R.drawable.diamond11
									: R.drawable.heart11);
			break;
		case 12:
			rId = ((card.getSuit().equalsIgnoreCase("spades")) ? R.drawable.spade12
					: (card.getSuit().equalsIgnoreCase("clubs")) ? R.drawable.club12
							: (card.getSuit().equalsIgnoreCase("diamonds")) ? R.drawable.diamond12
									: R.drawable.heart12);
			break;
		case 13:
			rId = ((card.getSuit().equalsIgnoreCase("spades")) ? R.drawable.spade13
					: (card.getSuit().equalsIgnoreCase("clubs")) ? R.drawable.club13
							: (card.getSuit().equalsIgnoreCase("diamonds")) ? R.drawable.diamond13
									: R.drawable.heart13);
			break;
		}

		if (getflipid) {
			flipCardId = rId;
			selectedImage.setImageResource(R.drawable.back_red);
			selectedImage.setVisibility(View.INVISIBLE);
			rl.addView(selectedImage);
			return selectedImage;
		} else {
			selectedImage.setImageResource(rId);
			selectedImage.setVisibility(View.INVISIBLE);
			rl.addView(selectedImage);
			return selectedImage;
		}
	}

	class MyImageHolder {
		ImageView cardImage;
		String name;
		float endx, endy;

		public MyImageHolder(ImageView cardImage, String name,
				float player_endx, float player_endy) {
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

	@SuppressWarnings("serial")
	class MySaveObject implements Serializable {
		// Deck savedDeck;
		int savedBank, savedCorrect, savedIncorrect;
		boolean repeatState, hintState, scoreState, countState;

		public MySaveObject(int savedBank, int savedCorrect,
				int savedIncorrect, boolean repeatState, boolean hintState,
				boolean scoreState, boolean countState) {
			super();
			// this.savedDeck = savedDeck;
			this.savedBank = savedBank;
			this.savedCorrect = savedCorrect;
			this.savedIncorrect = savedIncorrect;
			this.repeatState = repeatState;
			this.hintState = hintState;
			this.scoreState = scoreState;
			this.countState = countState;
		}
	}
}

class SimpleGestureFilter extends SimpleOnGestureListener {

	public final static int SURRENDER = 1; // Surrender
	public final static int STAND = 2;

	public final static int MODE_TRANSPARENT = 0;
	public final static int MODE_SOLID = 1;
	public final static int MODE_DYNAMIC = 2;

	private final static int ACTION_FAKE = -13; // just an unlikely number
	private int swipe_Min_Distance = 50;

	private int mode = MODE_DYNAMIC;
	private boolean running = true;
	private boolean tapIndicator = false;

	private Activity context;
	private GestureDetector detector;
	private SimpleGestureListener listener;

	public SimpleGestureFilter(Activity context, SimpleGestureListener sgl) {

		this.context = context;
		this.detector = new GestureDetector(context, this);
		this.listener = sgl;
	}

	public void onTouchEvent(MotionEvent event) {

		if (!this.running)
			return;

		boolean result = this.detector.onTouchEvent(event);

		if (this.mode == MODE_SOLID)
			event.setAction(MotionEvent.ACTION_CANCEL);
		else if (this.mode == MODE_DYNAMIC) {

			if (event.getAction() == ACTION_FAKE)
				event.setAction(MotionEvent.ACTION_UP);
			else if (result)
				event.setAction(MotionEvent.ACTION_CANCEL);
			else if (this.tapIndicator) {
				event.setAction(MotionEvent.ACTION_DOWN);
				this.tapIndicator = false;
			}
		}
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		final float xDistance = Math.abs(e1.getX() - e2.getX());
		final float yDistance = Math.abs(e1.getY() - e2.getY());

		boolean result = false;

		if (xDistance > yDistance && xDistance > this.swipe_Min_Distance) {
			this.listener.onSwipe(STAND);
			result = true;
		} else if (yDistance > xDistance && yDistance > this.swipe_Min_Distance) {
			if (e1.getY() > e2.getY()) // bottom to up
				this.listener.onSwipe(SURRENDER);
			result = true;
		}

		return result;
	}

	@Override
	public boolean onDoubleTap(MotionEvent ev) {
		this.listener.onDoubleTap();
		return true;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent ev) {
		this.listener.onSingleTap();
		return true;
	}

	static interface SimpleGestureListener {
		void onSwipe(int direction);

		void onSingleTap();

		void onDoubleTap();
	}
}