package com.whenfully.pandoku;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class CustomAlertDialog extends AlertDialog {

	private ImageButton buttonConfirm;
	private ImageButton buttonCancel;
	private TextView textMessage;

	public CustomAlertDialog(Context context) {
		super(context);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_alert_dialog);

		buttonConfirm = (ImageButton) findViewById(R.id.buttonConfirm);
		buttonCancel = (ImageButton) findViewById(R.id.buttonCancel);
		textMessage = (TextView) findViewById(R.id.textMessage);
	}

	public CustomAlertDialog setConfirmButton(View.OnClickListener listener) {
		buttonConfirm.setOnClickListener(listener);
		return this;
	}

	public CustomAlertDialog setCancelButton(View.OnClickListener listener) {
		buttonCancel.setOnClickListener(listener);
		return this;
	}

	public CustomAlertDialog setMessage(int resid) {
		textMessage.setText(resid);
		return this;
	}

	public CustomAlertDialog setMessage(String text) {
		textMessage.setText(text);
		return this;
	}
}
