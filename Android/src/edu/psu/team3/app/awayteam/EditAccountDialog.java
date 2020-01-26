package edu.psu.team3.app.awayteam;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditAccountDialog extends DialogFragment {
	private ModifyAccountTask mModTask = null;
	private GetUserTask mGetTask = null;

	private String mFirstName;
	private String mLastName;
	private String mPhone;
	private String mEmail;
	private String mEPhone;

	private EditText mNameView;
	private EditText mPhoneView;
	private EditText mEmailView;
	private EditText mEPhoneView;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// inflate custom view
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle("Edit Account: "
				+ UserSession.getInstance(getActivity()).getUsername());
		builder.setIcon(getResources().getDrawable(R.drawable.ic_action_person));
		builder.setView(inflater.inflate(R.layout.dialog_account_edit, null))
				// Add action buttons
				.setPositiveButton("Apply",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								EditAccountDialog.this.getDialog().cancel();
							}
						});

		return builder.create();
	}

	@Override
	public void onStart() {
		super.onStart(); // super.onStart() is where dialog.show() is actually
							// called on the underlying dialog, so we have to do
							// it after this point
		AlertDialog d = (AlertDialog) getDialog();
		if (d != null) {
			// resize to prevent keyboard from covering dialog buttons
			d.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			// fill fields with current data
			mNameView = (EditText) d.findViewById(R.id.edit_name_input);
			mEmailView = (EditText) d.findViewById(R.id.edit_email_input);
			mPhoneView = (EditText) d.findViewById(R.id.edit_phone_input);
			mEPhoneView = (EditText) d.findViewById(R.id.edit_emer_phone_input);
			if (mGetTask == null) {
				try {
					mGetTask = new GetUserTask();
					mGetTask.execute();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// assign listeners to custom buttons
			Button positiveButton = (Button) d
					.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					attemptModifyAccount();
				}
			});

			d.findViewById(R.id.edit_change_password_button)
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							DialogFragment passwordFragment = new EditPasswordDialog();
							passwordFragment.show(getActivity()
									.getFragmentManager(), null);
						}
					});
		}
	}

	// check all the text field values and then dispatch modify task
	private void attemptModifyAccount() {
		if (mModTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPhoneView.setError(null);
		mEPhoneView.setError(null);

		// Store values at the time of the account creation attempt.
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

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// perform modify account task
			try {
				mModTask = new ModifyAccountTask();
				mModTask.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	// Async task to modify user account
	public class ModifyAccountTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {

			Integer result = CommUtil.ModifyUser(getActivity(), UserSession
					.getInstance(getActivity()).getUsername(), mFirstName,
					mLastName, mEmail, mPhone, mEPhone);
			Log.v("Background", "returned from commutil.  result = " + result);
			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mModTask = null;

			switch (result) {
			case 1:// create user success
				Toast.makeText(getDialog().getContext(), "Account Updated!",
						Toast.LENGTH_SHORT).show();
				getDialog().dismiss();
				break;
			case -998: // email already taken
				mEmailView.setError("Email already registered");
				mEmailView.requestFocus();
				break;
			default: // some unknown error - probably could not contact server
				Toast.makeText(getDialog().getContext(),
						"Account Modification Failed", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}

		@Override
		protected void onCancelled() {
			mModTask = null;
		}
	}

	// Async task to get current user account info
	public class GetUserTask extends AsyncTask<Object, Void, List<String>> {

		@Override
		protected List<String> doInBackground(Object... params) {

			List<String> result = CommUtil.GetUser(getActivity(), UserSession
					.getInstance(getActivity()).getUsername());
			Log.v("Background", "returned from commutil.  result = " + result);
			return result;
		}

		@Override
		protected void onPostExecute(final List<String> result) {
			mGetTask = null;

			if (result != null) {
				mNameView.setText(result.get(0) + " " + result.get(1));
				mEmailView.setText(result.get(2));
				mPhoneView.setText(result.get(3));
				mEPhoneView.setText(result.get(4));
			}
		}

		@Override
		protected void onCancelled() {
			mGetTask = null;
		}
	}

}
