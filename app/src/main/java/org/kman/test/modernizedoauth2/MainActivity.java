package org.kman.test.modernizedoauth2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String PREF_KEY_EMAIL = "email";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		mCheckUseSignOut = (CheckBox) findViewById(R.id.oauth_use_sign_out);
		mButtonAuthNew = (Button) findViewById(R.id.oauth_auth_new);
		mEditEmail = (EditText) findViewById(R.id.oauth_email_address);
		mButtonAuthWithEmail = (Button) findViewById(R.id.oauth_auth_with_email);

		mEditEmail.setText(mSharedPrefs.getString(PREF_KEY_EMAIL, null));
		mEditEmail.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				onEditEmailChanged(s);
			}
		});
		mButtonAuthWithEmail.setEnabled(false);

		mButtonAuthNew.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onStartAuth(false);
			}
		});
		mButtonAuthWithEmail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onStartAuth(true);
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		final String email = getEditTextTrimmedOrNull(mEditEmail);
		mSharedPrefs.edit().putString(PREF_KEY_EMAIL, email).apply();
	}

	private void onEditEmailChanged(Editable s) {
		mButtonAuthWithEmail.setEnabled(getEditTextTrimmedOrNull(s) != null);
	}

	private void onStartAuth(boolean withEmail) {
		final String email;
		if (withEmail) {
			email = getEditTextTrimmedOrNull(mEditEmail);
		} else {
			email = null;
		}

		final Uri approvalUri = buildApprovalUri(INTERNAL_URI, email);
		final Uri intentUri;

		if (mCheckUseSignOut.isChecked()) {
			final Uri.Builder signOutUriBuilder = Uri.parse(GOOGLE_SIGN_OUT_URI).buildUpon();
			intentUri = signOutUriBuilder.appendQueryParameter(GOOGLE_SIGN_OUT_CONTINUE, approvalUri.toString())
					.build();
		} else {
			intentUri = approvalUri;
		}

		final Intent intent;
		if (isPackageInstalled(CHROME_PACKAGE_NAME)) {
			CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
			CustomTabsIntent customTabsIntent = builder.setShowTitle(false).build();
			intent = customTabsIntent.intent;
		} else {
			intent = new Intent(Intent.ACTION_VIEW);
		}

		intent.setData(intentUri);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

		try {
			startActivity(intent);
		} catch (Exception x) {
			showToast(x);
		}
	}

	private String getEditTextTrimmedOrNull(EditText edit) {
		final Editable editable = edit.getText();
		return getEditTextTrimmedOrNull(editable);
	}

	private String getEditTextTrimmedOrNull(Editable editable) {
		if (editable != null && editable.length() != 0) {
			final String s = editable.toString().trim();
			if (s.length() != 0) {
				return s;
			}
		}
		return null;
	}

	private void showToast(String s) {
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}

	private void showToast(Exception x) {
		showToast(x.toString());
	}

	private boolean isPackageInstalled(String packageName) {
		final PackageManager pm = getPackageManager();
		try {
			final PackageInfo info = pm.getPackageInfo(packageName, 0);
			return info != null;
		} catch (PackageManager.NameNotFoundException x) {
			// Ignore
		}
		return false;
	}

	// OAuth methods

	private static final String APPROVAL_URI = "https://accounts.google.com/o/oauth2/auth";

	private static final String GMAIL_SCOPE = "https://mail.google.com/";
	private static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
	private static final String PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";

	private static final String CLIENT_ID = "781777166716-fl7jroljr6u38qbq0tt4kpsaclsf4kre.apps.googleusercontent.com";

	private static final String INTERNAL_URI = "org.kman.test.modernizedoauth2.callback:/";

	private static final String GOOGLE_SIGN_OUT_URI = "https://accounts.google.com/SignOutOptions";
	private static final String GOOGLE_SIGN_OUT_CONTINUE = "continue";

	private static final String CHROME_PACKAGE_NAME = "com.android.chrome";

	private static final String WEB_SCOPES = GMAIL_SCOPE + " " + EMAIL_SCOPE + " " + PROFILE_SCOPE;

	private Uri buildApprovalUri(String redirectUri, String email) {
		final Uri.Builder builder = Uri.parse(APPROVAL_URI).buildUpon();
		builder.appendQueryParameter("access_type", "offline");
		builder.appendQueryParameter("scope", WEB_SCOPES);
		builder.appendQueryParameter("state", "getCode");
		builder.appendQueryParameter("approval_prompt", "force");
		builder.appendQueryParameter("response_type", "code");
		builder.appendQueryParameter("client_id", CLIENT_ID);
		builder.appendQueryParameter("redirect_uri", redirectUri);
		if (!TextUtils.isEmpty(email)) {
			builder.appendQueryParameter("login_hint", email);
		}

		return builder.build();
	}

	private SharedPreferences mSharedPrefs;
	private CheckBox mCheckUseSignOut;
	private Button mButtonAuthNew;
	private EditText mEditEmail;
	private Button mButtonAuthWithEmail;
}
