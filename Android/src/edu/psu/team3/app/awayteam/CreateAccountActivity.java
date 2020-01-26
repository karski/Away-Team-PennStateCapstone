package edu.psu.team3.app.awayteam;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CreateAccountActivity extends Activity {

	private UserAccountTask mAuthTask = null;
	private IDCheckTask mIDCheck = null;

	// Form fields
	private String mUsername;
	private String mPassword1;
	private String mPassword2;
	private String mFirstName;
	private String mLastName;
	private String mPhone;
	private String mEmail;
	private String mEPhone;

	// UI references
	private EditText mUsernameView;
	private EditText mPassword1View;
	private EditText mPassword2View;
	private EditText mNameView;
	private EditText mPhoneView;
	private EditText mEmailView;
	private EditText mEPhoneView;
	private View mCreateFormView;
	private View mCreateStatusView;
	private TextView mStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_account);

		// Assign UI Elements
		mUsernameView = (EditText) findViewById(R.id.new_username_input);
		mPassword1View = (EditText) findViewById(R.id.password1_input);
		mPassword2View = (EditText) findViewById(R.id.password2_input);
		mNameView = (EditText) findViewById(R.id.name_input);
		mPhoneView = (EditText) findViewById(R.id.phone_input);
		mEmailView = (EditText) findViewById(R.id.email_input);
		mEPhoneView = (EditText) findViewById(R.id.emer_phone_input);
		mCreateFormView = findViewById(R.id.create_form);
		mCreateStatusView = findViewById(R.id.create_status);
		mStatusMessageView = (TextView) findViewById(R.id.create_status_message);

		// Assign back button
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Assign buttons
		findViewById(R.id.account_submit_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptCreate();
					}
				});
		findViewById(R.id.username_check_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						mUsername = mUsernameView.getText().toString();

						if (mUsername.isEmpty()) {
							mUsernameView
									.setError(getString(R.string.error_field_required));
						} else if (mIDCheck == null) {
							mIDCheck = new IDCheckTask();

							try {
								mIDCheck.execute(mUsername);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				});
	}

	public void attemptCreate() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUsernameView.setError(null);
		mPassword1View.setError(null);
		mPassword2View.setError(null);
		mEmailView.setError(null);
		mPhoneView.setError(null);
		mEPhoneView.setError(null);

		// Store values at the time of the account creation attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword1 = mPassword1View.getText().toString();
		mPassword2 = mPassword2View.getText().toString();
		mEmail = mEmailView.getText().toString();
		mPhone = mPhoneView.getText().toString();
		mEPhone = mEPhoneView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// check valid phones
		if (!TextUtils.isEmpty(mEPhone) && mEPhone.length() < 7) {
			mEPhoneView.setError("Check Format");
			focusView = mEPhoneView;
			cancel = true;
		}
		if (TextUtils.isEmpty(mPhone)) {
			mPhoneView.setError(getString(R.string.error_field_required));
			focusView = mPhoneView;
			cancel = true;
		} else if (mPhone.length() < 7) {
			mPhoneView.setError("Check Format");
			focusView = mPhoneView;
			cancel = true;
		}

		// check valid email
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError("Check Format");
			focusView = mEmailView;
			cancel = true;
		}

		// check valid name and split
		if (TextUtils.isEmpty(mNameView.getText())) {
			mNameView.setError(getString(R.string.error_field_required));
			focusView = mNameView;
			cancel = true;
		} else if (mNameView.getText().toString().contains(" ")) {
			// name has more than one part, split it
			String[] nameParts = mNameView.getText().toString().split(" ");
			if (nameParts[0].contains(",")) {
				// format is Last, First X
				mLastName = nameParts[0].replace(",", "");
				mFirstName = nameParts[1];
			} else {
				// First name first
				mFirstName = nameParts[0];
				if (nameParts.length == 2) {
					mLastName = nameParts[1];
				} else if (nameParts.length > 2) {
					mLastName = nameParts[2];
				}
			}

		} else {
			mFirstName = mNameView.getText().toString();
			mLastName = "";
		}

		// Check valid password
		if (TextUtils.isEmpty(mPassword1)) {
			mPassword1View.setError(getString(R.string.error_field_required));
			focusView = mPassword1View;
			cancel = true;
		} else if (mPassword1.length() < 4) {
			mPassword1View.setError(getString(R.string.error_invalid_password));
			focusView = mPassword1View;
			cancel = true;
		}
		// check that password entries match
		if (TextUtils.isEmpty(mPassword2)) {
			mPassword2View.setError(getString(R.string.error_field_required));
			focusView = mPassword2View;
			cancel = true;
		} else if (!mPassword1.equals(mPassword2)) {
			mPassword2View.setError("Passwords must match");
			focusView = mPassword2View;
			cancel = true;
		}

		// Check for a valid username
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user account creation attempt.
			mStatusMessageView.setText("Creating New Account");
			showProgress(true);
			mAuthTask = new UserAccountTask();

			try {
				mAuthTask.execute();

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mCreateStatusView.setVisibility(View.VISIBLE);
			mCreateStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mCreateStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mCreateFormView.setVisibility(View.VISIBLE);
			mCreateFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mCreateFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mCreateStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mCreateFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public String getLastName() {
		return mLastName;
	}

	public String getFirstName() {
		return mFirstName;
	}

	/**
	 * An asynchronous registration task used to create a new account for the
	 * user.
	 */
	public class UserAccountTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {

			Integer result = CommUtil.CreateNewUser(getBaseContext(), mUsername, mPassword1, mFirstName, mLastName, mEmail, mPhone, mEPhone);
			Log.v("Background", "returned from commutil.  result = " + result);
			return result;

		}

		@Override
		protected void onPostExecute(final Integer result) {
			mAuthTask = null;
			showProgress(false);

			switch (result) {
			case 1:// create user success
				Toast.makeText(getBaseContext(), "New Account Created!",
						Toast.LENGTH_SHORT).show();
				Intent displayIntent = new Intent(getBaseContext(),
						DisplayActivity.class);
				displayIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(displayIntent);
				finish();
			case -999: // username already in use
				mUsernameView.setError("Username Taken");
				mUsernameView.requestFocus();
				break;
			case -998: // email already taken
				mEmailView.setError("Email already registered");
				mEmailView.requestFocus();
				break;
			default: // some unknown error - probably could not contact server
				Toast.makeText(getBaseContext(), "Account Creation Failed",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}

	// AsyncTask to check if the user already exists
	public class IDCheckTask extends AsyncTask<String, Void, Integer> {
		// dispatch the check to the background
		@Override
		protected Integer doInBackground(String... username) {
			Log.v("Background", "executing in background.  Input = "
					+ username[0]);
			Integer result = CommUtil.LoginIDExist(getBaseContext(),
					username[0]);
			Log.v("Background", "returned from commutil.  result = " + result);
			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mIDCheck = null;
			
			switch (result) {
			case 0:
				Toast.makeText(getBaseContext(),
						"Error contacting name server", Toast.LENGTH_SHORT)
						.show();
				break;
			case 1:
				Drawable icon = getResources().getDrawable(
						R.drawable.green_check);
				icon.setBounds(new Rect(0, 0, 75, 75));
				mUsernameView.setError("Username Available!", icon);
				mUsernameView.requestFocus();
				break;
			default:
				mUsernameView.setError("Username Taken");
				mUsernameView.requestFocus();
			}

		}

		@Override
		protected void onCancelled() {
			mIDCheck = null;
		}
	}

}
