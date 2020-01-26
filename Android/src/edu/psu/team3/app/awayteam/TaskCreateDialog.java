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
import android.widget.EditText;
import android.widget.Toast;

public class TaskCreateDialog extends DialogFragment {
	private CreateTask mCreateTask = null;

	String title;
	String description;

	EditText titleView;
	EditText descView;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// inflate custom view
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle("Create New Task");
		builder.setIcon(getResources().getDrawable(R.drawable.ic_action_new));
		builder.setView(inflater.inflate(R.layout.dialog_task_edit, null))
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
								TaskCreateDialog.this.getDialog().cancel();
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
			// override create button action to prevent closing immediately
			Button positiveButton = (Button) d
					.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					attemptCreateTask();
				}
			});
			// init UI elements
			titleView = (EditText) d.findViewById(R.id.taskedit_title);
			descView = (EditText) d.findViewById(R.id.taskedit_description);

		}
	}

	private void attemptCreateTask() {
		boolean cancel = false;
		View focusView = null;
		title = titleView.getText().toString();
		description = descView.getText().toString();
		if (description == null) {
			description = "";
		}

		if (title.isEmpty()) {
			cancel = true;
			titleView.setError("Task title is required");
			titleView.requestFocus();
		}

		if (!cancel && mCreateTask == null) {
			mCreateTask = new CreateTask();
			mCreateTask.execute();
		}
	}

	public class CreateTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {
			UserSession s = UserSession.getInstance(getActivity());
			Integer result = 0;
			result = CommUtil.CreateTask(getActivity(), s.getUsername(),
					s.currentTeamID, title, description);

			Log.v("Background", "returned from commutil.  result = " + result);

			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mCreateTask = null;
			if (result == 1) {// success!
				Toast.makeText(getActivity().getBaseContext(),
						"New Task Created", Toast.LENGTH_SHORT).show();
				// callback the team id
				((DisplayActivity) getActivity()).refreshTeam(UserSession
						.getInstance(getActivity()).currentTeamID);
				getDialog().dismiss();
			} else {// some error occured
				Toast.makeText(getActivity().getBaseContext(),
						"Unable to Create Task", Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected void onCancelled() {
			mCreateTask = null;
		}
	}
}
