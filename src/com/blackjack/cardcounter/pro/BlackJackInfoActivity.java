package com.blackjack.cardcounter.pro;

import com.gen.blackjack.cardcounter.pro.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class BlackJackInfoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_black_jack_info);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setTitle("Back");
		getActionBar().setDisplayUseLogoEnabled(false);
		((TextView) findViewById(R.id.link1)).setSelected(true);
		((TextView) findViewById(R.id.link2)).setSelected(true);
		((TextView) findViewById(R.id.link3)).setSelected(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.black_jack_info, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case android.R.id.home: {
			onBackPressed();
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}
}
