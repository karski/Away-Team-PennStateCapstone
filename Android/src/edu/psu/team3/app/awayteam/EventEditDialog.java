package edu.psu.team3.app.awayteam;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class EventEditDialog extends DialogFragment {
	private EditEventTask mEditTask = null;

	private Date startTime;
	private Date endTime;
	private String title = null;
	private String location = null;
	private String description = null;

	boolean endTimeSet = false;

	Button startDateView;
	Button startTimeView;
	Button endDateView;
	Button endTimeView;
	EditText titleView;
	EditText locationView;
	EditText descView;

	TeamEvent event = null;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		Bundle args = getArguments();
		event = UserSession.getInstance(getActivity()).activeTeam.getEvent(args
				.getInt("id"));
		// inflate custom view
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle("Edit Event");
		builder.setIcon(getResources().getDrawable(R.drawable.ic_action_event));
		builder.setView(inflater.inflate(R.layout.dialog_event_edit, null))
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
								EventEditDialog.this.getDialog().cancel();
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
					attemptEditEvent();
				}
			});
			// init times
			startTime = event.startTime;
			endTime = event.endTime;
			// init UI elements
			// set up each button with a picker dialog and handler logic
			startDateView = (Button) d.findViewById(R.id.eventedit_start_date);
			startDateView.setText(DateFormat.getDateInstance(DateFormat.MEDIUM)
					.format(startTime));
			startDateView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(startTime);
					DatePickerDialog dateDialog = new DatePickerDialog(
							getActivity(),
							new DatePickerDialog.OnDateSetListener() {

								@Override
								public void onDateSet(DatePicker view,
										int year, int monthOfYear,
										int dayOfMonth) {
									Calendar cal = Calendar.getInstance();
									cal.setTime(startTime);
									cal.set(year, monthOfYear, dayOfMonth);
									startTime = cal.getTime();
									if (!endTimeSet) {
										// if the user hasn't set their own end
										// time, match the dates
										cal.setTime(endTime);
										cal.set(year, monthOfYear, dayOfMonth);
										endTime = cal.getTime();
										endDateView.setText(DateFormat
												.getDateInstance(
														DateFormat.MEDIUM)
												.format(endTime));

									}
									startDateView.setText(DateFormat
											.getDateInstance(DateFormat.MEDIUM)
											.format(startTime));
								}
							}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
							cal.get(Calendar.DAY_OF_MONTH));
					dateDialog.show();
				}
			});

			startTimeView = (Button) d.findViewById(R.id.eventedit_start_time);
			startTimeView.setText(DateFormat.getTimeInstance(DateFormat.SHORT)
					.format(startTime));
			startTimeView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(startTime);
					TimePickerDialog timeDialog = new TimePickerDialog(
							getActivity(),
							new TimePickerDialog.OnTimeSetListener() {

								@Override
								public void onTimeSet(TimePicker view,
										int hourOfDay, int minute) {
									Calendar cal = Calendar.getInstance();
									cal.setTime(startTime);
									cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
									cal.set(Calendar.MINUTE, minute);
									startTime = cal.getTime();
									if (!endTimeSet) {
										// if the user hasn't set their own end
										// time, add an hour to the given time
										cal.add(Calendar.HOUR, 1);
										endTime = cal.getTime();
										endTimeView.setText(DateFormat
												.getTimeInstance(
														DateFormat.SHORT)
												.format(endTime));

									}
									startTimeView.setText(DateFormat
											.getTimeInstance(DateFormat.SHORT)
											.format(startTime));
								}
							}, cal.get(Calendar.HOUR_OF_DAY), cal
									.get(Calendar.MINUTE),
							android.text.format.DateFormat
									.is24HourFormat(getActivity()));
					timeDialog.show();
				}
			});

			endDateView = (Button) d.findViewById(R.id.eventedit_end_date);
			endDateView.setText(DateFormat.getDateInstance(DateFormat.MEDIUM)
					.format(endTime));
			endDateView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(endTime);
					DatePickerDialog dateDialog = new DatePickerDialog(
							getActivity(),
							new DatePickerDialog.OnDateSetListener() {

								@Override
								public void onDateSet(DatePicker view,
										int year, int monthOfYear,
										int dayOfMonth) {
									endTimeSet = true;
									Calendar cal = Calendar.getInstance();
									cal.setTime(endTime);
									cal.set(year, monthOfYear, dayOfMonth);
									endTime = cal.getTime();
									endDateView.setText(DateFormat
											.getDateInstance(DateFormat.MEDIUM)
											.format(endTime));
								}
							}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
							cal.get(Calendar.DAY_OF_MONTH));
					dateDialog.show();
				}
			});

			endTimeView = (Button) d.findViewById(R.id.eventedit_end_time);
			endTimeView.setText(DateFormat.getTimeInstance(DateFormat.SHORT)
					.format(endTime));
			endTimeView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(endTime);
					TimePickerDialog timeDialog = new TimePickerDialog(
							getActivity(),
							new TimePickerDialog.OnTimeSetListener() {

								@Override
								public void onTimeSet(TimePicker view,
										int hourOfDay, int minute) {
									endTimeSet = true;
									Calendar cal = Calendar.getInstance();
									cal.setTime(endTime);
									cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
									cal.set(Calendar.MINUTE, minute);
									endTime = cal.getTime();
									endTime = cal.getTime();
									endTimeView.setText(DateFormat
											.getTimeInstance(DateFormat.SHORT)
											.format(endTime));
								}
							}, cal.get(Calendar.HOUR_OF_DAY), cal
									.get(Calendar.MINUTE),
							android.text.format.DateFormat
									.is24HourFormat(getActivity()));
					timeDialog.show();
				}
			});

			titleView = (EditText) d.findViewById(R.id.eventedit_title);
			titleView.setText(event.title);
			locationView = (EditText) d.findViewById(R.id.eventedit_location);
			locationView.setText(event.location);
			descView = (EditText) d.findViewById(R.id.eventedit_description);
			descView.setText(event.description);
		}
	}

	private void attemptEditEvent() {
		boolean cancel = false;
		View focusView = null;

		description = descView.getText().toString();
		title = titleView.getText().toString();
		location = locationView.getText().toString();
		if (description.isEmpty()) {
			description = "";
		}
		if (title.isEmpty()) {
			cancel = true;
			titleView.setError("Event must have a title");
			focusView = titleView;
		}
		if (startTime.compareTo(endTime) >= 0) {
			cancel = true;
			Toast.makeText(getActivity(), "Start time must be before end time",
					Toast.LENGTH_SHORT).show();
		}

		if (cancel) {
			if (focusView != null) {
				focusView.requestFocus();
			}
		} else if (mEditTask == null) {
			mEditTask = new EditEventTask();
			mEditTask.execute();
		}

	}

	public class EditEventTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {
			UserSession s = UserSession.getInstance(getActivity());
			Integer result = 0;
			result = CommUtil.EditEvent(getActivity(), s.getUsername(),
					s.currentTeamID, event.id, startTime, endTime, title,
					location, description);

			Log.v("Background", "returned from commutil.  result = " + result);

			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mEditTask = null;
			if (result == 1) {// success!
				Toast.makeText(getActivity().getBaseContext(), "Event Updated",
						Toast.LENGTH_SHORT).show();
				// callback the team id
				((DisplayActivity) getActivity()).refreshTeam(UserSession
						.getInstance(getActivity()).currentTeamID);
				getDialog().dismiss();
			} else {// some error occured
				Toast.makeText(getActivity().getBaseContext(),
						"Unable to Update Event", Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected void onCancelled() {
			mEditTask = null;
		}
	}
}
