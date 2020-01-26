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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;

public class CreateTeamDialog extends DialogFragment {
	private CreateTeamTask mCreateTask = null;

	private String mTeamName = null;
	private String mDescription = null;
	private String mLocationName = null;
	private boolean mManaged = false;

	private EditText mTeamNameView;
	private EditText mLocNameView;
	private EditText mDescriptionView;
	private CheckBox mManagedView;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// inflate custom view
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle("Create New Team");
		builder.setIcon(getResources().getDrawable(
				R.drawable.ic_action_add_group));
		builder.setView(inflater.inflate(R.layout.dialog_create_team, null))
				// Add action buttons
				.setPositiveButton("Create",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								CreateTeamDialog.this.getDialog().cancel();
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
			Button positiveButton = (Button) d
					.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mTeamNameView = (EditText) getDialog().findViewById(
							R.id.teamNameInput);
					mLocNameView = (EditText) getDialog().findViewById(
							R.id.teamLocationInput);
					mDescriptionView = (EditText) getDialog().findViewById(
							R.id.teamDescriptionInput);
					mManagedView = (CheckBox) getDialog().findViewById(
							R.id.managedCheckBox);

					attemptCreateTeam();
				}
			});
			mManagedView = (CheckBox) getDialog().findViewById(
					R.id.managedCheckBox);
			mManagedView
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								getDialog().findViewById(
										R.id.createTeamPrivateExplain)
										.setVisibility(View.VISIBLE);
							} else {
								getDialog().findViewById(
										R.id.createTeamPrivateExplain)
										.setVisibility(View.GONE);
							}
						}

					});
		}
	}

	// check input values and dispatch asynch task
	private void attemptCreateTeam() {
		boolean cancel = false;
		mTeamName = mTeamNameView.getText().toString();
		if (mTeamName.isEmpty()) {
			mTeamNameView.setError("Team Name Required");
			mTeamNameView.requestFocus();
			cancel = true;
		}
		mDescription = mDescriptionView.getText().toString();
		mLocationName = mLocNameView.getText().toString();
		mManaged = mManagedView.isChecked();

		if (!cancel && mCreateTask == null) {
			try {
				mCreateTask = new CreateTeamTask();
				mCreateTask.execute();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public class CreateTeamTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {
			// dispatch the login method
			Integer result = 0;
			result = CommUtil.CreateTeam(getActivity().getBaseContext(),
					mTeamName, mLocationName, mDescription, 
					mManaged);

			Log.v("Background", "returned from commutil.  result = " + result);

			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mCreateTask = null;
			if (result > 0) {// success!
				Toast.makeText(getActivity().getBaseContext(),
						"New Team Created", Toast.LENGTH_SHORT).show();
				//callback the team id
				((DisplayActivity) getActivity()).refreshTeam(result);
				getDialog().dismiss();
			} else if (result == -1) {// team name taken
				mTeamNameView.setError("Team Name Taken");
				mTeamNameView.requestFocus();
			} else {// some other error occured
				Toast.makeText(getActivity().getBaseContext(),
						"Unable to Create Team", Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected void onCancelled() {
			mCreateTask = null;
		}
	}

}
