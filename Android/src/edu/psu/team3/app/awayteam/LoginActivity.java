package edu.psu.team3.app.awayteam;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;
	private PasswordResetTask mResetTask = null;

	// Values for email and password and remember check at the time of the login
	// attempt.
	private String mUsername;
	private String mPassword;
	private boolean mRemember;

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private CheckBox mRememberView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Set up the login form.
		mUsernameView = (EditText) findViewById(R.id.username_entry);
		mUsernameView.setText(mUsername);

		mPasswordView = (EditText) findViewById(R.id.password_entry);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});
		mRememberView = (CheckBox) findViewById(R.id.remember_me_checkbox);
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		// Initialize Buttons
		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});

		findViewById(R.id.create_account_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						startActivity(new Intent(LoginActivity.this,
								CreateAccountActivity.class));
					}
				});
	}

	@Override
	public void onStart() {
		super.onStart();
		// check for previous saved login
		try {
			UserSession s = UserSession.getInstance(getApplicationContext());
			if (s.remembered()) {
				mPassword = s.getPassword();
				mUsername = s.getUsername();
				mRemember = true;
				// attempt login
				mLoginStatusMessageView
						.setText(R.string.login_progress_signing_in);
				showProgress(true);
				if (mAuthTask == null) {
					mAuthTask = new UserLoginTask();
					// Create Login Request
					try {
						mAuthTask.execute();

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			Log.e("LOGIN", "Not ready to log in yet: " + e.toString());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mAuthTask != null) {
			mAuthTask.cancel(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_forgot_password) {
			mUsername = mUsernameView.getText().toString();
			if (mUsername.isEmpty()) {
				Toast.makeText(LoginActivity.this,
						"Enter your Username in the Login screen",
						Toast.LENGTH_LONG).show();
			} else if (mResetTask == null) {
				mResetTask = new PasswordResetTask();
				mResetTask.execute();
			}

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid username
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}

		mRemember = mRememberView.isChecked();

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			if (mAuthTask == null) {
				mAuthTask = new UserLoginTask();
				// Create Login Request
				try {
					mAuthTask.execute();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	// @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
		// int shortAnimTime = getResources().getInteger(
		// android.R.integer.config_shortAnimTime);
		//
		// mLoginStatusView.setVisibility(View.VISIBLE);
		// mLoginStatusView.animate().setDuration(shortAnimTime)
		// .alpha(show ? 1 : 0)
		// .setListener(new AnimatorListenerAdapter() {
		// @Override
		// public void onAnimationEnd(Animator animation) {
		// mLoginStatusView.setVisibility(show ? View.VISIBLE
		// : View.GONE);
		// }
		// });
		//
		// mLoginFormView.setVisibility(View.VISIBLE);
		// mLoginFormView.animate().setDuration(shortAnimTime)
		// .alpha(show ? 0 : 1)
		// .setListener(new AnimatorListenerAdapter() {
		// @Override
		// public void onAnimationEnd(Animator animation) {
		// mLoginFormView.setVisibility(show ? View.GONE
		// : View.VISIBLE);
		// }
		// });
		// } else {
		// The ViewPropertyAnimator APIs are not available, so simply show
		// and hide the relevant UI components.
		// UPDATE: simply show the logo - forget animations and such
		mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
		mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		// }

	}

	/**
	 * An asynchronous login/registration task used to authenticate the user.
	 */
	public class UserLoginTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {

			// dispatch the login method
			Integer result = 0;
			result = CommUtil.LoginUser(getBaseContext(), mUsername, mPassword,
					mRemember);
			Log.v("Background", "returned from commutil.  result = " + result);

			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mAuthTask = null;
			showProgress(false);

			switch (result) {
			case 1: // success!
				Intent displayIntent = new Intent(getBaseContext(),
						DisplayActivity.class);
				displayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(displayIntent);
				finish();
				break;
			case -1: // username incorrect
				mUsernameView.setError("Username Not Found");
				mUsernameView.requestFocus();
				Toast.makeText(getBaseContext(),
						"Do you want to create a new account?",
						Toast.LENGTH_SHORT).show();
				break;
			case -2: // password incorrect
				mPasswordView.setError(getResources().getString(
						R.string.error_incorrect_password));
				mPasswordView.requestFocus();
				Toast.makeText(getBaseContext(),
						"Try resetting password using Menu Option",
						Toast.LENGTH_SHORT).show();
				break;
			default: // some other error occured
				Toast.makeText(getBaseContext(), "Unable to Login",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			try {
				showProgress(false);
			} catch (Exception e) {
				Log.e("LOGIN",
						"Error on login caught - it appears the display is not available: "
								+ e.toString());
			}
		}
	}

	public class PasswordResetTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {

			// dispatch the login method
			Integer result = 0;
			result = CommUtil.ResetPassword(getBaseContext(), mUsername);
			Log.v("Background", "returned from commutil.  result = " + result);

			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mAuthTask = null;

			switch (result) {
			case 1: // success!
				Toast.makeText(
						LoginActivity.this,
						"A Temporary Password has been sent to your Email (Check your SPAM folder)",
						Toast.LENGTH_LONG).show();
				break;
			case -1: // wrong username supplied
				Toast.makeText(
						LoginActivity.this,
						"Entered Username does not exist. Enter correct Username or try creating a new account.",
						Toast.LENGTH_LONG).show();
			default: // some other error occured
				Toast.makeText(getBaseContext(),
						"Unable to Reset Password. Check entered Username.",
						Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
		}
	}
}
