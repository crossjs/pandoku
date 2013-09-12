package com.whenfully.pandoku;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Html;
//import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.whenfully.pandoku.commands.PandokuContext;
import com.whenfully.pandoku.commands.EliminateValuesCommand;
import com.whenfully.pandoku.commands.SetValuesCommand;
import com.whenfully.pandoku.db.PandokuDatabase;
import com.whenfully.pandoku.db.GameStatistics;
import com.whenfully.pandoku.db.PuzzleId;
import com.whenfully.pandoku.history.Command;
import com.whenfully.pandoku.history.History;
import com.whenfully.pandoku.im.InputMethod;
import com.whenfully.pandoku.im.InputMethodTarget;
import com.whenfully.pandoku.model.PandokuPuzzle;
import com.whenfully.pandoku.model.Level;
import com.whenfully.pandoku.model.Position;
import com.whenfully.pandoku.model.PuzzleType;
import com.whenfully.pandoku.model.ValueSet;
import com.whenfully.pandoku.source.PuzzleHolder;
import com.whenfully.pandoku.source.PuzzleSource;
import com.whenfully.pandoku.source.PuzzleSourceIds;
import com.whenfully.pandoku.source.PuzzleSourceResolver;

public class PandokuActivity extends Activity implements OnTouchListener,
		OnKeyListener, OnGestureListener, TickListener {
	private static final String TAG = PandokuActivity.class.getName();

	public static ArrayList<Activity> activityList = new ArrayList<Activity>();

	private static final int DIALOG_CONFIRM_RESET_PUZZLE = 0;
	private static final int DIALOG_CONFIRM_RESET_ALL_PUZZLES = 1;

	private static final String APP_STATE_PUZZLE_SOURCE_ID = "puzzleSourceId";
	private static final String APP_STATE_PUZZLE_NUMBER = "puzzleNumber";
	private static final String APP_STATE_GAME_STATE = "gameState";
	private static final String APP_STATE_HIGHLIGHTED_DIGIT = "highlightedDigit";
	private static final String APP_STATE_HISTORY = "history";

	private static final int REQUEST_CODE_SETTINGS = 0;

	private static final int GAME_STATE_NEW_ACTIVITY_STARTED = 0;
	private static final int GAME_STATE_ACTIVITY_STATE_RESTORED = 1;
	private static final int GAME_STATE_READY = 2;
	private static final int GAME_STATE_PLAYING = 3;
	private static final int GAME_STATE_SOLVED = 4;

	private int gameState;

	private PandokuDatabase db;

	private Vibrator vibrator;

	private PuzzleSource source;
	private int puzzleNumber;
	private PandokuPuzzle puzzle;
	private TickTimer timer = new TickTimer(this);

	private History<PandokuContext> history = new History<PandokuContext>(
			new PandokuContext() {
				public TickTimer getTimer() {
					return timer;
				}

				public PandokuPuzzle getPuzzle() {
					return puzzle;
				}
			});

	private ViewGroup layoutRoot;
	private TextView puzzleNameView;
	private TextView puzzleLevelView;
	private TextView puzzleSourceView;
	private PandokuPuzzleView andokuView;
	private TextView timerView;
	private ViewGroup keypad;
	private ViewGroup buttons;
	private KeypadToggleButton[] keypadToggleButtons;
	private ImageButton undoButton;
	private ImageButton redoButton;
	private TextView congratsView;
	private View fingertip;
	private Button startButton;
	private FingertipView fingertipView;

	private Toast toast;

	private final int[] fingertipViewScreenLocation = new int[2];
	private final int[] viewScreenLocation = new int[2];

	private final InputMethodTarget inputMethodTarget = new InputMethodTarget() {
		public int getPuzzleSize() {
			return puzzle.getSize();
		}

		public Position getMarkedPosition() {
			return andokuView.getMarkedPosition();
		}

		public void setMarkedPosition(Position position) {
			andokuView.markPosition(position);
			cancelToast();
		}

		public boolean isClue(Position position) {
			return puzzle.isClue(position.row, position.col);
		}

		public ValueSet getCellValues(Position position) {
			return puzzle.getValues(position.row, position.col);
		}

		public void setCellValues(Position position, ValueSet values) {
			PandokuActivity.this.setCellValues(position, values);
		}

		public int getNumberOfDigitButtons() {
			return keypadToggleButtons.length;
		}

		public void checkButton(int digit, boolean checked) {
			keypadToggleButtons[digit].setChecked(checked);
		}

		public void highlightDigit(Integer digit) {
			andokuView.highlightDigit(digit);
		}
	};

	private InputMethodPolicy inputMethodPolicy;
	private InputMethod inputMethod;

	static final int FLING_MIN_DISTANCE = 120, FLING_MIN_VELOCITY = 150;
	private GestureDetector gestureDetector;

	private View menuLayout;
	private PopupWindow menuPopup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onCreate(" + savedInstanceState + ")");

		Util.setFullscreenMode(this);

		super.onCreate(savedInstanceState);
		activityList.add(this);

		setContentView(R.layout.pandoku);
		MyBroadcastReceiver.register(this);

		db = new PandokuDatabase(this);

		gestureDetector = new GestureDetector(this);

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		layoutRoot = (ViewGroup) findViewById(R.id.layoutRoot);

		puzzleNameView = (TextView) findViewById(R.id.labelPuzzleName);
		puzzleLevelView = (TextView) findViewById(R.id.labelPuzzleLevel);
		puzzleSourceView = (TextView) findViewById(R.id.labelPuzzleSource);

		andokuView = (PandokuPuzzleView) findViewById(R.id.viewPuzzle);
		andokuView.setOnKeyListener(this);

		timerView = (TextView) findViewById(R.id.labelTimer);

		buttons = (ViewGroup) findViewById(R.id.buttons);

		keypad = (ViewGroup) findViewById(R.id.keypad);

		keypadToggleButtons = new KeypadToggleButton[9];
		keypadToggleButtons[0] = (KeypadToggleButton) findViewById(R.id.input_1);
		keypadToggleButtons[1] = (KeypadToggleButton) findViewById(R.id.input_2);
		keypadToggleButtons[2] = (KeypadToggleButton) findViewById(R.id.input_3);
		keypadToggleButtons[3] = (KeypadToggleButton) findViewById(R.id.input_4);
		keypadToggleButtons[4] = (KeypadToggleButton) findViewById(R.id.input_5);
		keypadToggleButtons[5] = (KeypadToggleButton) findViewById(R.id.input_6);
		keypadToggleButtons[6] = (KeypadToggleButton) findViewById(R.id.input_7);
		keypadToggleButtons[7] = (KeypadToggleButton) findViewById(R.id.input_8);
		keypadToggleButtons[8] = (KeypadToggleButton) findViewById(R.id.input_9);

		for (int i = 0; i < 9; i++) {
			final int digit = i;
			keypadToggleButtons[i].setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					onKeypad(digit);
				}
			});
		}

		((ImageButton) findViewById(R.id.input_clear))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						onClear();
					}
				});

		((ImageButton) findViewById(R.id.input_invert))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						onInvert();
					}
				});

		((ImageButton) findViewById(R.id.input_check))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						onCheckPuzzle();
					}
				});

		((ImageButton) findViewById(R.id.input_pause))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						onPauseResumeGame();
					}
				});

		undoButton = (ImageButton) findViewById(R.id.input_undo);
		undoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onUndo();
			}
		});

		redoButton = (ImageButton) findViewById(R.id.input_redo);
		redoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onRedo();
			}
		});

		congratsView = (TextView) findViewById(R.id.labelCongrats);

		fingertip = findViewById(R.id.labelFingertip);

		startButton = (Button) findViewById(R.id.buttonStart);
		startButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onStartButton();
			}
		});

		fingertipView = new FingertipView(this);
		fingertipView.setOnTouchListener(this);
		addContentView(fingertipView, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		createThemeFromPreferences();

		createPuzzle(savedInstanceState);

		createInputMethod();

		((TextView) startButton).setTypeface(Typeface.createFromAsset(
				getAssets(), "fonts/pandoku.ttf"));

		if (isRestoreSavedInstanceState(savedInstanceState)) {
			inputMethod.onRestoreInstanceState(savedInstanceState);

			if (savedInstanceState.containsKey(APP_STATE_HIGHLIGHTED_DIGIT))
				andokuView.highlightDigit(savedInstanceState
						.getInt(APP_STATE_HIGHLIGHTED_DIGIT));

			history.restoreInstanceState(savedInstanceState
					.getBundle(APP_STATE_HISTORY));
			undoButton.setEnabled(history.canUndo());
			redoButton.setEnabled(history.canRedo());
		}
	}

	private void createThemeFromPreferences() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);

		ColorTheme.Builder builder = new ColorTheme.Builder(getResources());

		builder.areaColorPolicy = AreaColorPolicy.valueOf(settings.getString(
				Settings.KEY_COLORED_REGIONS,
				AreaColorPolicy.STANDARD_X_HYPER_SQUIGGLY.name()));
		builder.highlightDigitsPolicy = HighlightDigitsPolicy.valueOf(settings
				.getString(Settings.KEY_HIGHLIGHT_DIGITS,
						HighlightDigitsPolicy.ONLY_SINGLE_VALUES.name()));

		ColorThemePolicy colorThemePolicy = ColorThemePolicy.valueOf(settings
				.getString(Settings.KEY_COLOR_THEME,
						ColorThemePolicy.CLASSIC.name()));
		colorThemePolicy.customize(builder);

		Theme theme = builder.build();

		setTheme(theme);
	}

	private void setTheme(Theme theme) {
		layoutRoot.setBackgroundDrawable(theme.getBackground());
		puzzleNameView.setTextColor(theme.getNameTextColor());
		puzzleLevelView.setTextColor(theme.getLevelTextColor());
		puzzleSourceView.setTextColor(theme.getSourceTextColor());
		timerView.setTextColor(theme.getTimerTextColor());
		andokuView.setTheme(theme);
	}

	private void createInputMethod() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);

		InputMethodPolicy inputMethodPolicy = InputMethodPolicy
				.valueOf(settings.getString(Settings.KEY_INPUT_METHOD,
						InputMethodPolicy.CELL_THEN_VALUES.name()));
		if (inputMethodPolicy != this.inputMethodPolicy) {
			this.inputMethodPolicy = inputMethodPolicy;
			this.inputMethod = inputMethodPolicy
					.createInputMethod(inputMethodTarget);
			this.inputMethod.reset();
		}
	}

	@Override
	protected void onPause() {
		if (Constants.LOG_V)
			Log.v(TAG, "onPause(" + timer + ")");

		super.onPause();

		if (gameState == GAME_STATE_PLAYING) {
			timer.stop();
			autoSavePuzzle();
		}

		setKeepScreenOn(false);
	}

	@Override
	protected void onResume() {
		if (Constants.LOG_V)
			Log.v(TAG, "onResume(" + timer + ")");

		super.onResume();

		if (gameState == GAME_STATE_PLAYING) {
			setKeepScreenOn(true);

			timer.start();
		}
	}

	@Override
	protected void onDestroy() {
		if (Constants.LOG_V)
			Log.v(TAG, "onDestroy()");

		super.onDestroy();

		if (source != null) {
			source.close();
		}

		if (db != null) {
			db.close();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onSaveInstanceState(" + timer + ")");

		super.onSaveInstanceState(outState);

		if (source != null) {
			outState.putString(APP_STATE_PUZZLE_SOURCE_ID, source.getSourceId());
			outState.putInt(APP_STATE_PUZZLE_NUMBER, puzzleNumber);
			outState.putInt(APP_STATE_GAME_STATE, gameState);

			Integer highlightedDigit = andokuView.getHighlightedDigit();
			if (highlightedDigit != null)
				outState.putInt(APP_STATE_HIGHLIGHTED_DIGIT, highlightedDigit);

			outState.putBundle(APP_STATE_HISTORY, history.saveInstanceState());

			inputMethod.onSaveInstanceState(outState);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (Constants.LOG_V)
			Log.v(TAG, "onActivityResult(" + requestCode + ", " + resultCode
					+ ", " + data + ")");

		switch (requestCode) {
		case REQUEST_CODE_SETTINGS:
			onReturnedFromSettings();
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("menu");

		if (Constants.LOG_V)
			Log.v(TAG, "onCreateOptionsMenu(" + menu + ")");

		if (null == menuPopup) {

			menuLayout = getLayoutInflater().inflate(R.layout.menu_pandoku,
					null);

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
						closeMenu();
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
					if (keyCode == KeyEvent.KEYCODE_MENU
							|| keyCode == KeyEvent.KEYCODE_BACK) {
						closeMenu();
						return true;
					}
					return false;
				}
			});

			((Button) menuLayout.findViewById(R.id.menu_settings))
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							onSettings();
						}
					});

			((Button) menuLayout.findViewById(R.id.menu_reset_puzzle))
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							onResetPuzzle(false);
						}
					});

			((Button) menuLayout.findViewById(R.id.menu_reset_all))
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							onResetAllPuzzles(false);
						}
					});

			((Button) menuLayout.findViewById(R.id.menu_eliminate))
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							onEliminateValues();
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
		else {

			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(this);
			View me = menuLayout.findViewById(R.id.menu_eliminate);
			me.setVisibility((gameState == GAME_STATE_PLAYING && settings
					.getBoolean(Settings.KEY_ENABLE_ELIMINATE_VALUES, false)) ? View.VISIBLE
					: View.GONE);
			me.setEnabled(puzzle.canEliminateValues());

			menuLayout.findViewById(R.id.menu_reset_puzzle).setVisibility(
					gameState == GAME_STATE_PLAYING ? View.VISIBLE : View.GONE);

			menuLayout.findViewById(R.id.menu_reset_all).setVisibility(
					gameState == GAME_STATE_READY ? View.VISIBLE : View.GONE);

			menuPopup.showAtLocation(findViewById(R.id.layoutRoot),
					Gravity.BOTTOM, 0, 0);
		}

		return false;
	}

	void closeMenu() {
		if (menuPopup != null && menuPopup.isShowing())
			menuPopup.dismiss();
	}

	void onKeypad(int digit) {
		if (gameState != GAME_STATE_PLAYING)
			return;

		inputMethod.onKeypad(digit);

		cancelToast();
	}

	void onClear() {
		if (gameState != GAME_STATE_PLAYING)
			return;

		inputMethod.onClear();

		cancelToast();
	}

	void onInvert() {
		if (gameState != GAME_STATE_PLAYING)
			return;

		inputMethod.onInvert();

		cancelToast();
	}

	void onUndo() {
		if (history.undo())
			onCommandExecuted();
	}

	void onRedo() {
		if (history.redo())
			onCommandExecuted();
	}

	private void setCellValues(Position position, ValueSet values) {
		execute(new SetValuesCommand(position, values));
	}

	private void execute(Command<PandokuContext> command) {
		if (history.execute(command))
			onCommandExecuted();
	}

	private void onCommandExecuted() {
		undoButton.setEnabled(history.canUndo());
		redoButton.setEnabled(history.canRedo());

		if (puzzle.isCompletelyFilled()) {
			if (puzzle.isSolved()) {
				timer.stop();
				autoSavePuzzle();

				enterGameState(GAME_STATE_SOLVED);
				return;
			} else {
				showInfo(R.string.info_invalid_solution);
			}
		}

		updateKeypadHighlighing();

		andokuView.invalidate();

		inputMethod.onValuesChanged();
	}

	public boolean onKey(View view, int keyCode, KeyEvent event) {
		if (gameState != GAME_STATE_PLAYING)
			return false;

		if (event.getAction() != KeyEvent.ACTION_DOWN)
			return false;

		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
			inputMethod.onMoveMark(-1, 0);
			return true;

		case KeyEvent.KEYCODE_DPAD_DOWN:
			inputMethod.onMoveMark(1, 0);
			return true;

		case KeyEvent.KEYCODE_DPAD_LEFT:
			inputMethod.onMoveMark(0, -1);
			return true;

		case KeyEvent.KEYCODE_DPAD_RIGHT:
			inputMethod.onMoveMark(0, 1);
			return true;

		case KeyEvent.KEYCODE_1:
		case KeyEvent.KEYCODE_2:
		case KeyEvent.KEYCODE_3:
		case KeyEvent.KEYCODE_4:
		case KeyEvent.KEYCODE_5:
		case KeyEvent.KEYCODE_6:
		case KeyEvent.KEYCODE_7:
		case KeyEvent.KEYCODE_8:
		case KeyEvent.KEYCODE_9:
			onKeypad(keyCode - KeyEvent.KEYCODE_1);
			return true;

		default:
			return false;
		}
	}

	public boolean onTouch(View view, MotionEvent event) {
		if (gameState != GAME_STATE_PLAYING)
			return false;

		fingertipView.getLocationOnScreen(fingertipViewScreenLocation);
		andokuView.getLocationOnScreen(viewScreenLocation);

		// translate event x/y from fingertipView to andokuView coordinates
		float x = event.getX() + fingertipViewScreenLocation[0]
				- viewScreenLocation[0];
		float y = event.getY() + fingertipViewScreenLocation[1]
				- viewScreenLocation[1];
		Position position = andokuView.getPositionAt(x, y, 0.5f,
				Util.dipInt(this, 10));

		int action = event.getAction();

		if (position == null && action == MotionEvent.ACTION_DOWN)
			return false;

		boolean editable = position != null
				&& !puzzle.isClue(position.row, position.col);

		if (action == MotionEvent.ACTION_DOWN
				|| action == MotionEvent.ACTION_MOVE) {
			if (position == null) {
				fingertipView.hide();
			} else {
				PointF center = andokuView.getPositionCenterPoint(position);
				// translate center from andokuView coordinates to fingertipView
				// coordinates
				center.x += viewScreenLocation[0]
						- fingertipViewScreenLocation[0];
				center.y += viewScreenLocation[1]
						- fingertipViewScreenLocation[1];

				fingertipView.show(center, editable);
			}

			inputMethod.onSweep();
		} else if (action == MotionEvent.ACTION_UP) {
			fingertipView.hide();

			inputMethod.onTap(position, editable);
		} else { // MotionEvent.ACTION_CANCEL
			fingertipView.hide();

			inputMethod.onSweep();
		}

		return true;
	}

	// callback from tick timer
	public void onTick(long time) {
		timerView.setText(DateUtil.formatTime(time));
	}

	private void onGotoPuzzleRelative(int offset) {
		int number = puzzleNumber + offset;
		int numPuzzles = source.numberOfPuzzles();
		if (number < 0)
			number = numPuzzles - 1;
		if (number >= numPuzzles)
			number = 0;

		gotoPuzzle(number);
	}

	private void gotoPuzzle(int number) {
		setPuzzle(number);

		inputMethod.reset();

		enterGameState(GAME_STATE_READY);
	}

	void onCheckPuzzle() {
		if (Constants.LOG_V)
			Log.v(TAG, "onCheckPuzzle()");

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean checkAgainstSolution = settings.getBoolean(
				Settings.KEY_CHECK_AGAINST_SOLUTION, true);

		if (checkAgainstSolution) {
			if (puzzle.hasSolution()) {
				checkPuzzle(true);
			} else {
				new ComputeSolutionAndCheckPuzzleTask().execute();
			}
		} else {
			checkPuzzle(false);
		}

		// menuPopup.dismiss();
	}

	private void checkPuzzle(boolean checkAgainstSolution) {
		boolean errors = puzzle.checkForErrors(checkAgainstSolution);

		if (errors) {
			showWarning(R.string.warn_puzzle_errors);
		} else {
			showInfo(String.format(getText(R.string.info_puzzle_ok_n)
					.toString(), puzzle.getMissingValuesCount()));

		}

		andokuView.invalidate();
	}

	void onStartButton() {
		if (Constants.LOG_V)
			Log.v(TAG, "onStartButton()");

		if (gameState == GAME_STATE_SOLVED) {
			onGoAhead();
		} else if (gameState == GAME_STATE_READY && puzzle.isSolved()) {
			onResetPuzzle(false);
		} else {
			enterGameState(GAME_STATE_PLAYING);
		}
	}

	void onGoAhead() {
		if (Constants.LOG_V)
			Log.v(TAG, "onGoAhead()");
		// 继续下一关
		congratsView.setVisibility(View.GONE);
		fingertip.setVisibility(View.VISIBLE);
		onGotoPuzzleRelative(1);
		// enterGameState(GAME_STATE_READY);
	}

	void onSettings() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivityForResult(intent, REQUEST_CODE_SETTINGS);

		closeMenu();
	}

	// callback from reset puzzle dialog
	void onResetPuzzle(boolean confirmed) {
		if (!confirmed && !puzzle.isModified())
			confirmed = true;

		if (!confirmed) {
			showDialog(DIALOG_CONFIRM_RESET_PUZZLE);
		} else {
			timer.stop();
			deleteAutoSavedPuzzle();

			gotoPuzzle(puzzleNumber);
		}

		closeMenu();
	}

	// callback from reset all puzzles dialog
	void onResetAllPuzzles(boolean confirmed) {
		if (!confirmed) {
			showDialog(DIALOG_CONFIRM_RESET_ALL_PUZZLES);
		} else {
			resetAllPuzzles();
		}

		closeMenu();
	}

	private void resetAllPuzzles() {
		String sourceId = source.getSourceId();

		if (Constants.LOG_V)
			Log.v(TAG, "deleting all puzzles for " + sourceId);

		db.deleteAll(sourceId);

		gotoPuzzle(0);
	}

	void onEliminateValues() {
		execute(new EliminateValuesCommand());

		closeMenu();
	}

	void onExitButton() {
		Util.exit(this);
	}

	private void onReturnedFromSettings() {
		createThemeFromPreferences();

		createInputMethod();

		setTimerVisibility(gameState);

		andokuView.invalidate();
	}

	private void createPuzzle(Bundle savedInstanceState) {
		if (isRestoreSavedInstanceState(savedInstanceState))
			createPuzzleFromSavedInstanceState(savedInstanceState);
		else
			createPuzzleFromIntent();
	}

	private boolean isRestoreSavedInstanceState(Bundle savedInstanceState) {
		return savedInstanceState != null
				&& savedInstanceState.getString(APP_STATE_PUZZLE_SOURCE_ID) != null;
	}

	private void createPuzzleFromSavedInstanceState(Bundle savedInstanceState) {
		String puzzleSourceId = savedInstanceState
				.getString(APP_STATE_PUZZLE_SOURCE_ID);
		int number = savedInstanceState.getInt(APP_STATE_PUZZLE_NUMBER);

		if (Constants.LOG_V)
			Log.v(TAG, "createPuzzleFromSavedInstanceState(): "
					+ puzzleSourceId + ":" + number);

		initializePuzzle(puzzleSourceId, number);

		gameState = GAME_STATE_ACTIVITY_STATE_RESTORED;
		enterGameState(savedInstanceState.getInt(APP_STATE_GAME_STATE));
	}

	private void createPuzzleFromIntent() {
		final Intent intent = getIntent();
		String puzzleSourceId = intent
				.getStringExtra(Constants.EXTRA_PUZZLE_SOURCE_ID);
		if (puzzleSourceId == null)
			puzzleSourceId = PuzzleSourceIds.forAssetFolder("standard_n_1");
		int number = intent.getIntExtra(Constants.EXTRA_PUZZLE_NUMBER, 0);

		if (Constants.LOG_V)
			Log.v(TAG, "createPuzzleFromIntent(): " + puzzleSourceId + ":"
					+ number);

		initializePuzzle(puzzleSourceId, number);

		gameState = GAME_STATE_NEW_ACTIVITY_STARTED;
		boolean start = getIntent().getBooleanExtra(
				Constants.EXTRA_START_PUZZLE, false);
		enterGameState(start ? GAME_STATE_PLAYING : GAME_STATE_READY);
	}

	private void initializePuzzle(String puzzleSourceId, int number) {
		source = PuzzleSourceResolver.resolveSource(this, puzzleSourceId);

		setPuzzle(number);
	}

	private void setPuzzle(int number) {
		puzzleNumber = number;

		puzzle = createPandokuPuzzle(number);
		history.clear();
		undoButton.setEnabled(false);
		redoButton.setEnabled(false);

		andokuView.setPuzzle(puzzle);

		puzzleNameView.setText(getPuzzleName());
		puzzleLevelView.setText(getPuzzleLevel());
		puzzleSourceView.setText(getPuzzleSource());

		if (!restoreAutoSavedPuzzle()) {
			Log.w(TAG, "unable to restore auto-saved puzzle");
			timer.reset();
		}
	}

	private PandokuPuzzle createPandokuPuzzle(int number) {
		PuzzleHolder holder = source.load(number);
		return new PandokuPuzzle(holder.getName(), holder.getPuzzle(),
				holder.getLevel());
	}

	private String getPuzzleName() {
		String name = puzzle.getName();
		if (name != null && name.length() > 0)
			return name;

		PuzzleType puzzleType = puzzle.getPuzzleType();
		return Util.getPuzzleName(getResources(), puzzleType);
	}

	private String getPuzzleLevel() {
		final Level level = puzzle.getLevel();
		if (level == Level.UNKNOWN)
			return "";

		final Resources resources = getResources();
		String[] levels = resources.getStringArray(R.array.levels);
		return "（" + levels[level.ordinal()] + "）";
	}

	private String getPuzzleSource() {
		final String suffix = "#" + (puzzleNumber + 1) + "/"
				+ source.numberOfPuzzles();
		return suffix;

	}

	private void onPauseResumeGame() {
		if (gameState == GAME_STATE_PLAYING) {
			timer.stop();
			autoSavePuzzle();

			enterGameState(GAME_STATE_READY);
		} else if (gameState == GAME_STATE_READY) {
			enterGameState(GAME_STATE_PLAYING);
		} else
			throw new IllegalStateException("pause/resume");
	}

	private void enterGameState(int newGameState) {
		if (Constants.LOG_V)
			Log.v(TAG, "enterGameState(" + newGameState + ")");

		setKeepScreenOn(newGameState == GAME_STATE_PLAYING);

		switch (newGameState) {
		case GAME_STATE_READY:
			if (puzzle.isSolved())
				startButton.setText(R.string.button_reset_game);
			else if (timer.getTime() > 0)
				startButton.setText(R.string.button_resume);
			else
				startButton.setText(R.string.button_start_game);
			break;

		case GAME_STATE_PLAYING:
			if (!puzzle.isRestored())
				autoSavePuzzle(); // save for correct 'date-created' timestamp
			timer.start();
			break;

		case GAME_STATE_SOLVED:
			startButton.setText(R.string.button_go_ahead);
			updateCongrats();
			break;

		default:
			throw new IllegalStateException();
		}

//		boolean showNameAndLevel = newGameState != GAME_STATE_PLAYING
//				|| hasSufficientVerticalSpace();
//		puzzleNameView.setVisibility(showNameAndLevel ? View.VISIBLE
//				: View.GONE);
//		puzzleLevelView.setVisibility(showNameAndLevel ? View.VISIBLE
//				: View.GONE);

		final boolean showKeypad = newGameState == GAME_STATE_PLAYING;
		keypad.setVisibility(showKeypad ? View.VISIBLE : View.GONE);
		buttons.setVisibility(showKeypad ? View.GONE : View.VISIBLE);
		if (showKeypad) {
			updateKeypadHighlighing();
		} else {
			congratsView
					.setVisibility(newGameState == GAME_STATE_SOLVED ? View.VISIBLE
							: View.GONE);

			fingertip
					.setVisibility(newGameState == GAME_STATE_READY ? View.VISIBLE
							: View.GONE);

			startButton.setVisibility(newGameState == GAME_STATE_READY
					|| newGameState == GAME_STATE_SOLVED ? View.VISIBLE
					: View.GONE);
		}

		andokuView.setPaused(newGameState == GAME_STATE_READY
				&& !puzzle.isSolved() && timer.getTime() > 0);
		andokuView.setPreview(newGameState == GAME_STATE_READY);

		setTimerVisibility(newGameState);

		if (gameState != newGameState)
			andokuView.invalidate();

		this.gameState = newGameState;
	}

	// 320x240 device (e.g. HTC Tattoo) does not have enough vertical space to
	// display title and name)
//	private boolean hasSufficientVerticalSpace() {
//		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//		float aspect = ((float) displayMetrics.heightPixels)
//				/ displayMetrics.widthPixels;
//		return aspect >= 480f / 320;
//	}

	private void setTimerVisibility(int forGameState) {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);

		boolean showTimerWhilePlaying = settings.getBoolean(
				Settings.KEY_SHOW_TIMER, true);
		boolean showTimer = showTimerWhilePlaying
				|| forGameState != GAME_STATE_PLAYING;
		timerView.setVisibility(showTimer ? View.VISIBLE : View.INVISIBLE);
	}

	private void setKeepScreenOn(boolean keepScreenOn) {
		andokuView.setKeepScreenOn(keepScreenOn);
	}

	private void updateCongrats() {
		String congrats = getResources().getString(R.string.message_congrats);
		String details = getStatistics();
		String message = congrats + "<br/><br/>" + details;
		congratsView.setText(Html.fromHtml(message));
	}

	private String getStatistics() {
		GameStatistics stats = db.getStatistics(source.getSourceId());
		return getResources().getString(R.string.message_statistics_details,
				stats.numGamesSolved,
				DateUtil.formatTime(stats.getAverageTime()),
				DateUtil.formatTime(stats.minTime));
	}

	private void updateKeypadHighlighing() {
		final int size = puzzle.getSize();
		int[] counter = new int[size];

		for (int row = 0; row < size; row++) {
			for (int col = 0; col < size; col++) {
				ValueSet values = puzzle.getValues(row, col);
				if (values.size() == 1) {
					int value = values.nextValue(0);
					counter[value]++;
				}
			}
		}

		for (int digit = 0; digit < size; digit++) {
			final boolean digitCompleted = counter[digit] == size;
			keypadToggleButtons[digit].setHighlighted(digitCompleted);
		}
	}

	private void autoSavePuzzle() {
		PuzzleId puzzleId = getCurrentPuzzleId();

		if (Constants.LOG_V)
			Log.v(TAG, "auto-saving puzzle " + puzzleId);

		db.saveGame(puzzleId, puzzle, timer);
	}

	private void deleteAutoSavedPuzzle() {
		PuzzleId puzzleId = getCurrentPuzzleId();

		if (Constants.LOG_V)
			Log.v(TAG, "deleting auto-save game " + puzzleId);

		db.delete(puzzleId);
	}

	private boolean restoreAutoSavedPuzzle() {
		PuzzleId puzzleId = getCurrentPuzzleId();

		if (Constants.LOG_V)
			Log.v(TAG, "restoring auto-save game " + puzzleId);

		return db.loadGame(puzzleId, puzzle, timer);
	}

	private PuzzleId getCurrentPuzzleId() {
		return new PuzzleId(source.getSourceId(), puzzleNumber);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_CONFIRM_RESET_PUZZLE:
			return createConfirmResetPuzzleDialog();
		case DIALOG_CONFIRM_RESET_ALL_PUZZLES:
			return createConfirmResetAllPuzzlesDialog();
		default:
			return null;
		}
	}

	private CustomAlertDialog createConfirmResetPuzzleDialog() {
		final CustomAlertDialog cad = new CustomAlertDialog(this);
		cad.show();

		return cad.setMessage(R.string.message_reset_puzzle)
				.setConfirmButton(new OnClickListener() {

					@Override
					public void onClick(View v) {
						onResetPuzzle(true);
						cad.dismiss();
					}
				}).setCancelButton(new OnClickListener() {

					@Override
					public void onClick(View v) {
						cad.dismiss();
					}
				});
	}

	private CustomAlertDialog createConfirmResetAllPuzzlesDialog() {
		final CustomAlertDialog cad = new CustomAlertDialog(this);
		cad.show();

		return cad.setMessage(R.string.message_reset_all_puzzles)
				.setConfirmButton(new OnClickListener() {

					@Override
					public void onClick(View v) {
						onResetAllPuzzles(true);
						cad.dismiss();
					}
				}).setCancelButton(new OnClickListener() {

					@Override
					public void onClick(View v) {
						cad.dismiss();
					}
				});
	}

	private void showInfo(int resId) {
		showInfo(getText(resId));
	}

	private void showInfo(CharSequence message) {
		showToast(message, false);
	}

	private void showWarning(int resId) {
		showWarning(getText(resId));
	}

	private void showWarning(CharSequence message) {
		vibrator.vibrate(new long[] { 0, 80, 80, 120 }, -1);
		showToast(message, true);
	}

	private void showToast(CharSequence message, boolean warning) {
		cancelToast();

		fingertipView.getLocationOnScreen(fingertipViewScreenLocation);
		timerView.getLocationOnScreen(viewScreenLocation);
		int y = viewScreenLocation[1] - fingertipViewScreenLocation[1]
				- Util.dipInt(this, 17);

		toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, y);
		toast.show();
	}

	private void cancelToast() {
		if (toast != null) {
			toast.cancel();
			toast = null;
		}
	}

	private final class ComputeSolutionAndCheckPuzzleTask extends
			AsyncTask<Void, Integer, Boolean> {
		private boolean timerRunning;
		private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			timerRunning = timer.isRunning();
			timer.stop();

			String message = getResources().getString(
					R.string.message_computing_solution);
			progressDialog = ProgressDialog.show(PandokuActivity.this, "",
					message, true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			return puzzle.computeSolution();
		}

		@Override
		protected void onPostExecute(Boolean solved) {
			progressDialog.dismiss();

			if (timerRunning)
				timer.start();

			if (solved)
				checkPuzzle(true);
			else
				showWarning(R.string.warn_invalid_puzzle);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 游戏中禁用手势
		return gameState == GAME_STATE_PLAYING ? false : gestureDetector
				.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		if (Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE) {
				// 向左
				onGotoPuzzleRelative(1);
				return true;
			} else if (e1.getX() - e2.getX() < -FLING_MIN_DISTANCE) {
				// 向右
				onGotoPuzzleRelative(-1);
				return true;
			}
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
}
