package org.kman.test.modernizedoauth2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by kman on 2/4/17.
 */

public class OAuthResultActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String code = null;
		final Intent intent = getIntent();
		if (intent != null) {
			final Uri uri = intent.getData();
			if (uri != null) {
				code = uri.getQueryParameter("code");
			}
		}

		final String msg = getString(R.string.auth_result, code);
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

		final Intent mainIntent = new Intent(this, MainActivity.class);
		mainIntent.setAction(Intent.ACTION_MAIN);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		mainIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

		startActivity(mainIntent);
	}
}
