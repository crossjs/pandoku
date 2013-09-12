package com.whenfully.pandoku;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MyUpdate extends Activity {
	private static final String TAG = MyUpdate.class.getName();

	private String UPDATE_XML_URL = "http://pandoku.googlecode.com/svn/update.xml";
	private String UPDATE_APK_URL = "";
	private int UPDATE_APK_VER = 0;
	private String UPDATE_APK_NAM = "";
	private String UPDATE_APK_DES = "";
	private int SIZE_TOTAL = 0;
	private int SIZE_SOFAR = 0;

	private boolean IS_QUIT = false;

	private String UPDATE_APK_PATH;
	private File UPDATE_APK;

	private InputStream is;
	private URLConnection conn;

	private ProgressBar progressBar;
	private TextView textView;

	private Button updateButton;

	private int state = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (Constants.LOG_V)
			Log.v(TAG, "onCreate(" + savedInstanceState + ")");

		Util.setFullscreenMode(this);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.my_update);

		Typeface typeface = Typeface.createFromAsset(getAssets(),
				"fonts/pandoku.ttf");

		((TextView) findViewById(R.id.pageTitle)).setTypeface(typeface);
		((TextView) findViewById(R.id.pageButton)).setTypeface(typeface);

		findViewById(R.id.pageButton).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		textView = (TextView) findViewById(R.id.textView);

		updateButton = (Button) findViewById(R.id.updateButton);

		updateButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doUpdate();
			}
		});

	}

	private void doUpdate() {
		switch (state) {
		case 0:
			new Thread() {
				public void run() {
					if (checkNewVersion()) {
						sendMsg(9);
					} else {
						sendMsg(8);
					}
				}
			}.start();
			break;
		case 1:
			finish();
			break;
		case 2:
			downloadApk();
			break;
		case 3:
			finish();
			break;
		case 4:
			openFile();
			break;
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
				case 0:
					progressBar.setVisibility(View.VISIBLE);
					progressBar.setIndeterminate(true);
					textView.setTextSize(11f);
					textView.setText("正在检测更新……");
					updateButton.setVisibility(View.VISIBLE);
					updateButton.setText(R.string.button_cancel_check_update);
					state = 1;
					break;
				case 9:
					progressBar.setVisibility(View.GONE);
					textView.setTextSize(17f);
					textView.setText(Html.fromHtml("检测到新版本：<br><b>"
							+ UPDATE_APK_NAM + "</b><br>"
							+ String.format(UPDATE_APK_DES, "<br>")));
					updateButton.setVisibility(View.VISIBLE);
					updateButton.setText(R.string.button_download_update);
					state = 2;
					break;
				case 8:
					progressBar.setVisibility(View.GONE);
					textView.setTextSize(17f);
					textView.setText("没有检测到新版本");
					updateButton.setVisibility(View.GONE);
					break;
				case 1:
					progressBar.setVisibility(View.VISIBLE);
					progressBar.setIndeterminate(false);
					textView.setTextSize(11f);
					textView.setText("正在下载更新……");
					updateButton.setVisibility(View.VISIBLE);
					updateButton.setText(R.string.button_cancel_download_update);
					state = 3;
					break;
				case 2:
					progressBar.setMax(SIZE_TOTAL);
					break;
				case 3:
					progressBar.setProgress(SIZE_SOFAR);
					break;
				case 4:
					progressBar.setVisibility(View.GONE);
					textView.setTextSize(17f);
					textView.setText("文件下载完毕");
					updateButton.setVisibility(View.VISIBLE);
					updateButton.setText(R.string.button_install_update);
					state = 4;
					break;
				case 5:
					progressBar.setVisibility(View.VISIBLE);
					progressBar.setIndeterminate(true);
					textView.setTextSize(11f);
					textView.setText("开始安装更新……");
					updateButton.setVisibility(View.GONE);
					break;
				case -1:
					progressBar.setVisibility(View.GONE);
					String error = msg.getData().getString("error");
					textView.setTextSize(17f);
					textView.setText(error);
					updateButton.setVisibility(View.VISIBLE);
					updateButton.setText(R.string.button_check_update);
					state = 0;
					break;
				}
			}
			super.handleMessage(msg);
		}
	};

	private void sendMsg(int flag) {
		if (Constants.LOG_V)
			Log.v(TAG, "sendMsg(" + flag + ")");
		Message msg = new Message();
		msg.what = flag;
		handler.sendMessage(msg);
	}

	private boolean checkNewVersion() {
		if (Constants.LOG_V)
			Log.v(TAG, "checkNewVersion()");
		sendMsg(0);
		try {
			URL url = new URL(UPDATE_XML_URL);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(false);
			SAXParser parser = factory.newSAXParser();
			is = url.openStream();
			parser.parse(is, new DefaultHandler() {
				private String cur = "";
				private boolean inApk = false;

				@Override
				public void startDocument() throws SAXException {
				}

				@Override
				public void startElement(String uri, String localName,
						String qName, Attributes attributes)
						throws SAXException {
					if (localName.equals("apk")) {
						inApk = true;
					}
					cur = localName;
				}

				@Override
				public void characters(char[] ch, int start, int length)
						throws SAXException {
					if (inApk) {
						String str = new String(ch, start, length).trim();
						if (str == null || str.equals(""))
							return;
						if (cur.equals("url")) {
							UPDATE_APK_URL = str;
						} else if (cur.equals("versioncode")) {
							UPDATE_APK_VER = Integer.parseInt(str);
						} else if (cur.equals("versionname")) {
							UPDATE_APK_NAM = str;
						} else if (cur.equals("description")) {
							UPDATE_APK_DES = str;
						}
					}
				}

				@Override
				public void endElement(String uri, String localName,
						String qName) throws SAXException {
					if (localName.equals("apk")) {
						inApk = false;
					}
				}

				@Override
				public void endDocument() throws SAXException {
					super.endDocument();
				}
			});
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (UPDATE_APK_VER > getPackageManager().getPackageInfo(
					getPackageName(), 0).versionCode)
				return true;
			else
				return false;
		} catch (NameNotFoundException e) {
			// e.printStackTrace();
			return true;
		}
	}

	private boolean createFolder() {
		if (!Util.isSDCardExist()) {
			if (Constants.LOG_V)
				Log.w(TAG, "SD卡未装载，不能进行数据库备份与恢复");
			return false;
		}

		File sdcard = Environment.getExternalStorageDirectory();
		File andokuBaseDir = new File(sdcard, Constants.PANDOKU_BASE_DIR_NAME);
		if (!andokuBaseDir.isDirectory() && !andokuBaseDir.mkdirs()) {
			if (Constants.LOG_V)
				Log.e(TAG, "无法在SD卡根目录下创建“" + Constants.PANDOKU_BASE_DIR_NAME
						+ "”文件夹");
			return false;
		}
		File andokuBackupDir = new File(andokuBaseDir,
				Constants.PANDOKU_UPDATE_DIR_NAME);
		if (!andokuBackupDir.isDirectory() && !andokuBackupDir.mkdirs()) {
			if (Constants.LOG_V)
				Log.e(TAG, "无法在“" + Constants.PANDOKU_BASE_DIR_NAME
						+ "”文件夹下创建“" + Constants.PANDOKU_UPDATE_DIR_NAME
						+ "”文件夹");
			return false;
		}
		return true;
	}

	/* 处理下载URL文件自定义函数 */
	private void downloadApk() {
		if (Constants.LOG_V)
			Log.v(TAG, "downloadApk()");
		sendMsg(1);
		if (!createFolder()) {
			textView.setText("未能将更新所需信息写入SD卡，请检查");
			updateButton.setVisibility(View.GONE);
		} else {

			try {
				new Thread(new Runnable() {
					public void run() {
						try {
							URL myURL = new URL(UPDATE_APK_URL);
							conn = myURL.openConnection();
							conn.connect();
							is = conn.getInputStream();
							if (is == null) {
								throw new RuntimeException("stream is null");
							}

							UPDATE_APK_PATH = Util.getSDCardPath() + "/"
									+ Constants.PANDOKU_BASE_DIR_NAME + "/"
									+ Constants.PANDOKU_UPDATE_DIR_NAME + "/"
									+ "pandoku.apk";

							UPDATE_APK = new File(UPDATE_APK_PATH);

							FileOutputStream fos = new FileOutputStream(
									UPDATE_APK);
							byte buf[] = new byte[256];

							SIZE_TOTAL = conn.getContentLength();
							sendMsg(2);

							do {
								int numread = is.read(buf);
								if (numread <= 0) {
									sendMsg(4);
									break;
								}
								SIZE_SOFAR += numread;
								sendMsg(3);
								fos.write(buf, 0, numread);
							} while (!IS_QUIT);

							// if (!IS_QUIT) {
							// openFile();
							// }
							try {
								is.close();
							} catch (Exception ex) {
							}
						} catch (Exception ex) {
						}
					}
				}).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void openFile() {
		if (Constants.LOG_V)
			Log.v(TAG, "openFile()");

		sendMsg(5);
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		intent.setDataAndType(Uri.fromFile(UPDATE_APK),
				getMIMEType(UPDATE_APK.getName()));
		startActivity(intent);
	}

	private String getMIMEType(String fName) {
		String type = "";

		String end = fName
				.substring(fName.lastIndexOf(".") + 1, fName.length())
				.toLowerCase();

		if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			type = "audio";
		} else if (end.equals("3gp") || end.equals("mp4")) {
			type = "video";
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			type = "image";
		} else if (end.equals("apk")) {
			type = "application/vnd.android.package-archive";
		} else {
			type = "*";
		}
		if (end.equals("apk")) {
		} else {
			type += "/*";
		}
		return type;
	}

	@Override
	protected void onDestroy() {
		if (Constants.LOG_V)
			Log.v(TAG, "onDestroy()");

		super.onDestroy();

		IS_QUIT = true;

		if (is != null) {
			try {
				is.close();
			} catch (Exception ex) {
			}
		}
	}

	@Override
	public void finish() {
		if (Constants.LOG_V)
			Log.v(TAG, "finish()");
		super.finish();

		Thread.currentThread().interrupt();
	}
}