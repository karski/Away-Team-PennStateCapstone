package edu.psu.team3.app.awayteam;

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

public class EditPasswordDialog extends DialogFragment {
	private PassChangeTask mPassTask = null;

	private String mOldPass;
	private String mPassword1;
	private String mPassword2;

	private EditText mOldPassView;
	private EditText mPass1View;
	private EditText mPass2View;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// inflate custom view
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle("Change Password: "
				+ UserSession.getInstance(getActivity()).getUsername());
		builder.setIcon(getResources().getDrawable(R.drawable.ic_lock));
		builder.setView(inflater.inflate(R.layout.dialog_change_password, null))
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
								EditPasswordDialog.this.getDialog().cancel();
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
			mOldPassView = (EditText) d.findViewById(R.id.password_old);
			mPass1View = (EditText) d.findViewById(R.id.password1_change);
			mPass2View = (EditText) d.findViewById(R.id.password2_change);
			// assign listeners to custom buttons
			Button positiveButton = (Button) d
					.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					attemptChangePass();
				}
			});
		}
	}

	// check for errors and if none, change the password
	private void attemptChangePass() {
		if (mPassTask != null) {
			return;
		}

		boolean cancel = false;
		View focusView = null;

		mOldPass = mOldPassView.getText().toString();
		mPassword1 = mPass1View.getText().toString();
		mPassword2 = mPass2View.getText().toString();

		// Check valid password
		if (TextUtils.isEmpty(mPassword1)) {
			mPass1View.setError(getString(R.string.error_field_required));
			focusView = mPass1View;
			cancel = true;
		} else if (mPassword1.length() < 4) {
			mPass1View.setError(getString(R.string.error_invalid_password));
			focusView = mPass1View;
			cancel = true;
		}
		// check that password entries match
		if (TextUtils.isEmpty(mPassword2)) {
			mPass2View.setError(getString(R.string.error_field_required));
			focusView = mPass2View;
			cancel = true;
		} else if (!mPassword1.equals(mPassword2)) {
			mPass2View.setError("Passwords must match");
			focusView = mPass2View;
			cancel = true;
		}

		// Check old password is correct
		if (TextUtils.isEmpty(mOldPass)) {
			mOldPassView.setError(getString(R.string.error_field_required));
			focusView = mOldPassView;
			cancel = true;
		} else if (!mOldPass.equals(UserSession.getInstance(getActivity())
				.getPassword())) {
			mOldPassView.setError("Incorrect Password");
			focusView = mOldPassView;
			cancel = true;
		}

		if (cancel) {
			focusView.requestFocus();
			return;
		} else {
			if (mPassTask == null) {
				try {
					mPassTask = new PassChangeTask();
					mPassTask.execute();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	// change password based on the value of mPassword1
	public class PassChangeTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {
			UserSession s = UserSession.getInstance(getActivity());
			// dispatch the login method
			Integer result = 0;
			result = CommUtil.ChangePassword(s.getUsername(), mPassword1,
					getActivity());

			Log.v("Background", "returned from commutil.  result = " + result);

			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mPassTask = null;

			switch (result) {
			case 1: // success!
				Toast.makeText(getActivity(), "Password Changed",
						Toast.LENGTH_SHORT).show();
				getDialog().dismiss();
				break;
			default: // some other error occured
				Toast.makeText(getActivity(), "Unable to Change Password",
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onCancelled() {
			mPassTask = null;
		}
	}

}
