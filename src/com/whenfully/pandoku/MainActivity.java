package com.whenfully.pandoku;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;

import com.whenfully.pandoku.db.PandokuDatabase;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getName();

	private ImageButton resumeGameButton;

	private PandokuDatabase db;

	private View menuLayout;
	private PopupWindow menuPopup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onCreate(" + savedInstanceState + ")");

		Util.setFullscreenMode(this);

		super.onCreate(savedInstanceState);

		BackupUtil.restoreOrBackupDatabase(this);

		setContentView(R.layout.main);
		MyBroadcastReceiver.register(this);

		db = new PandokuDatabase(this);

		resumeGameButton = (ImageButton) findViewById(R.id.resumeButton);
		resumeGameButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onResumeGameButton();
			}
		});

		ImageButton newGameButton = (ImageButton) findViewById(R.id.newgameButton);
		newGameButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSelectNewGameButton();
			}
		});

	}

	@Override
	protected void onResume() {
		if (Constants.LOG_V)
			Log.v(TAG, "onResume()");

		super.onResume();

		final boolean hasGamesInProgress = db.hasGamesInProgress();
		resumeGameButton.setEnabled(hasGamesInProgress);
	}

	@Override
	protected void onDestroy() {
		if (Constants.LOG_V)
			Log.v(TAG, "onDestroy()");

		super.onDestroy();

		if (db != null) {
			db.close();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("menu");

		if (Constants.LOG_V)
			Log.v(TAG, "onCreateOptionsMenu(" + menu + ")");

		if (null == menuPopup) {

			menuLayout = getLayoutInflater().inflate(R.layout.menu, null);

			menuPopup = new PopupWindow(menuLayout, LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);

			menuPopup.setOutsideTouchable(true);
			menuPopup.setFocusable(true);
			menuPopup.setBackgroundDrawable(new BitmapDrawable());
			menuPopup.setAnimationStyle(R.style.MenuAnimation);

			menuPopup.setTouchInterceptor(new OnTouchListener() {

				public boolean onTouch(View v, MotionEvent event) {
					if (Constants.LOG_V)
						Log.v(TAG, "onTouch(" + v + ", " + event + ")");

					if (event.getY() < 0) {
						if (menuPopup.isShowing())
							menuPopup.dismiss();
					}
					return false;
				}
			});

			menuLayout.setFocusableInTouchMode(true);

			menuLayout.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (Constants.LOG_V)
						Log.v(TAG, "onKey(" + v + ", " + event + ")");
					if ((keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BACK)
							&& (menuPopup.isShowing())) {
						menuPopup.dismiss();
						return true;
					}
					return false;
				}
			});

			((Button) menuLayout.findViewById(R.id.menu_update))
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							onUpdateButton();
						}
					});

			((Button) menuLayout.findViewById(R.id.menu_help))
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							onHelpButton();
						}
					});

			((Button) menuLayout.findViewById(R.id.menu_about))
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							onAboutButton();
						}
					});

			((Button) menuLayout.findViewById(R.id.menu_exit))
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							onExitButton();
						}
					});

		}

		return super.onCreateOptionsMenu(menu);// false;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (menuPopup.isShowing())
			menuPopup.dismiss();
		else
			menuPopup.showAtLocation(findViewById(R.id.layoutRoot),
					Gravity.BOTTOM, 0, 0);

		return false;
	}

	void onResumeGameButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onResumeGameButton()");

		Intent intent = new Intent(this, ResumeGameActivity.class);
		startActivity(intent);
	}

	void onSelectNewGameButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onSelectNewGameButton()");

		Intent intent = new Intent(this, NewGameActivity.class);
		startActivity(intent);
	}

	void onUpdateButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onUpdateButton()");

		Intent intent = new Intent(this, MyUpdate.class);
		startActivity(intent);

		menuPopup.dismiss();
	}

	void onHelpButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onHelpButton()");

		Intent intent = new Intent(this, HelpActivity.class);
		startActivity(intent);

		menuPopup.dismiss();
	}

	void onAboutButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onAboutButton()");

		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);

		menuPopup.dismiss();
	}

	void onExitButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onExitButton()");

		Util.exit(this);

	}
}
