package edu.psu.team3.app.awayteam;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class EditTeamDialog extends DialogFragment {
	private EditTeamTask mEditTask = null;

	private String mTeamName = null;
	private String mDescription = null;
	private String mLocationName = null;

	private EditText mTeamNameView;
	private EditText mLocNameView;
	private EditText mDescriptionView;
	private CheckBox mManagedView;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// inflate custom view
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle("Edit Team");
		builder.setIcon(getResources().getDrawable(R.drawable.ic_action_group));
		builder.setView(inflater.inflate(R.layout.dialog_create_team, null))
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
								EditTeamDialog.this.getDialog().cancel();
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
		UserSession s = UserSession.getInstance(getActivity());
		if (d != null) {
			// resize to prevent keyboard from covering dialog buttons
			d.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			// assign UI elements
			mTeamNameView = (EditText) d.findViewById(R.id.teamNameInput);
			mLocNameView = (EditText) d.findViewById(R.id.teamLocationInput);
			mDescriptionView = (EditText) d
					.findViewById(R.id.teamDescriptionInput);
			mManagedView = (CheckBox) d.findViewById(R.id.managedCheckBox);
			// assign button and listener
			Button positiveButton = (Button) d
					.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					attemptEditTeam();
				}
			});
			// hide management checkbox
			mManagedView = (CheckBox) getDialog().findViewById(
					R.id.managedCheckBox);
			mManagedView.setVisibility(View.GONE);
			// fill in values
			mTeamNameView.setText(s.activeTeam.name);
			mLocNameView.setText(s.activeTeam.location);
			mDescriptionView.setText(s.activeTeam.description);
		}
	}

	// check input values and dispatch asynch task
	private void attemptEditTeam() {
		boolean cancel = false;
		mTeamName = mTeamNameView.getText().toString();
		if (mTeamName.isEmpty()) {
			mTeamNameView.setError("Team Name Required");
			mTeamNameView.requestFocus();
			cancel = true;
		}
		mDescription = mDescriptionView.getText().toString();
		mLocationName = mLocNameView.getText().toString();

		if (!cancel && mEditTask == null) {
			try {
				mEditTask = new EditTeamTask();
				mEditTask.execute();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public class EditTeamTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {
			UserSession s = UserSession.getInstance(getActivity());

			Integer result = 0;
			result = CommUtil.ModifyTeam(getActivity().getBaseContext(),
					s.getUsername(), s.currentTeamID, mTeamName, mLocationName,
					mDescription);

			Log.v("Background", "returned from commutil.  result = " + result);

			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mEditTask = null;
			if (result == 1) {// success!
				Toast.makeText(getActivity().getBaseContext(),
						"Team info Modified", Toast.LENGTH_SHORT).show();
				// callback the team id
				((DisplayActivity) getActivity()).refreshTeam(UserSession
						.getInstance(getActivity()).currentTeamID);
				getDialog().dismiss();
			} else if (result == -1) {// permission revoked
				mTeamNameView.setError("Not allowed to Edit this Team");
				mTeamNameView.requestFocus();
			} else {// some other error occured
				Toast.makeText(getActivity().getBaseContext(),
						"Unable to Edit Team", Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected void onCancelled() {
			mEditTask = null;
		}
	}

}
