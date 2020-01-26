package edu.psu.team3.app.awayteam;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class CalendarFragment extends Fragment {
	private DeleteTask mDeleteTask = null;

	Button todayButton;
	ImageButton addEventButton;
	ListView eventsListView;
	CalendarListAdapter adapter;
	boolean delete = false; // state tracker for multi-select
	ActionMode mMode = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_calendar, container,
				false);
		// identify UI elements
		todayButton = (Button) rootView.findViewById(R.id.today_button);
		addEventButton = (ImageButton) rootView
				.findViewById(R.id.add_event_button);
		eventsListView = (ListView) rootView.findViewById(R.id.agenda_list);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		// ensure list of events is sorted
		UserSession s = UserSession.getInstance(getActivity());
		Collections.sort(s.activeTeam.teamEvents, TeamEvent.StartComparator);
		// fill list
		adapter = new CalendarListAdapter(getActivity(), 0,
				s.activeTeam.teamEvents);
		// Attach the adapter to a ListView
		eventsListView.setAdapter(adapter);
		// handle multiple selections
		eventsListView
				.setMultiChoiceModeListener(new MultiChoiceModeListener() {

					Menu selectMenu;

					@Override
					public boolean onPrepareActionMode(ActionMode mode,
							Menu menu) {
						return false;
					}

					@Override
					public void onDestroyActionMode(ActionMode mode) {
						if (!delete) {
							adapter.clearSelection();
						}
						// otherwise, hold on to the selection so the background
						// task can use it
						mMode = null;
					}

					@Override
					public boolean onCreateActionMode(ActionMode mode, Menu menu) {
						MenuInflater inflater = getActivity().getMenuInflater();
						inflater.inflate(R.menu.multi_select, menu);
						selectMenu = menu;
						delete = false;
						mMode = mode;
						return true;
					}

					@Override
					public boolean onActionItemClicked(ActionMode mode,
							MenuItem item) {
						switch (item.getItemId()) {
						case R.id.action_selected_delete:
							delete = true;
							((DisplayActivity) getActivity())
									.setRefreshActionButtonState(true);
							mDeleteTask = new DeleteTask();
							mDeleteTask.execute();
							mode.finish();
							break;
						case R.id.action_selected_edit:
							// create dialog and pass id
							DialogFragment newFragment = new EventEditDialog();
							Bundle bundle = new Bundle();
							bundle.putInt("id",
									adapter.getSelection().get(0).id);
							newFragment.setArguments(bundle);
							newFragment.show(getFragmentManager(), null);
							mode.finish();
							break;
						}
						return true;
					}

					@Override
					public void onItemCheckedStateChanged(ActionMode mode,
							int position, long id, boolean checked) {
						// update selected list
						if (checked) {
							adapter.addSelection(position);
						} else {
							adapter.removeSelection(position);
						}

						final int checkedCount = adapter.getSelection().size();
						switch (checkedCount) {
						case 0:
							mode.setSubtitle(null);
							break;
						case 1:
							selectMenu.setGroupVisible(R.id.menu_group_edit,
									true);
							mode.setTitle("1 Event Selected");
							break;
						default:
							selectMenu.setGroupVisible(R.id.menu_group_edit,
									false);
							mode.setTitle(checkedCount + " Events Selected");
							break;
						}

					}
				});

		// position view so today is in sight
		ScrollToToday(false);
		// Assign listener to event clicks
		eventsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View targetView,
					int position, long rowID) {

				// Create dialog to show details
				DialogFragment newFragment = new EventDetailDialog();
				Bundle args = new Bundle();
				args.putInt("position", position);
				newFragment.setArguments(args);
				newFragment.show(getFragmentManager(), null);
			}
		});

		// Assign actions to buttons
		todayButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// find the first entry that is today or later
				ScrollToToday(true);
			}
		});

		addEventButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new EventCreateDialog();
				newFragment.show(getFragmentManager(), null);
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mMode != null) {
			mMode.finish();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mDeleteTask != null) {
			mDeleteTask.cancel(true);
		}
	}

	// scrolls the view to the current day.
	// input "loud" indicates whether the view has the current focus and should
	// annunciate issues and scroll with animation
	private void ScrollToToday(boolean loud) {
		for (int i = 0; i < adapter.getCount(); i++) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			Date today = cal.getTime();
			if (!adapter.getItem(i).startTime.before(today)) {

				if (loud) {
					eventsListView.setSelection(i);
					eventsListView.smoothScrollToPosition(i);// .smoothScrollToPositionFromTop(i,
																// 0, 1000);
				} else {
					eventsListView.setSelectionFromTop(i, 0);
				}

				return;
			}
		}
		if (loud) {
			Toast.makeText(getActivity(), "No Events found after Today",
					Toast.LENGTH_SHORT).show();
		}
	}

	public class DeleteTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {
			UserSession s = UserSession.getInstance(getActivity());
			Integer result = 0;
			for (TeamEvent event : adapter.getSelection()) {
				result = CommUtil.DeleteEvent(getActivity(), s.getUsername(),
						s.currentTeamID, event.id);
				Log.v("Background", "returned from commutil.  result = "
						+ result);
			}
			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mDeleteTask = null;
			delete = false;
			if (result == 1) {// success!
				try {
					if (adapter.getSelection().size() == 1) {
						Toast.makeText(getActivity().getBaseContext(),
								"Event Deleted", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(
								getActivity().getBaseContext(),
								adapter.getSelection().size()
										+ " Events Deleted", Toast.LENGTH_SHORT)
								.show();
					}
					adapter.clearSelection();

					((DisplayActivity) getActivity()).refreshTeam(UserSession
							.getInstance(getActivity()).currentTeamID);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {// some error occured
				((DisplayActivity) getActivity())
						.setRefreshActionButtonState(false);
				Toast.makeText(getActivity().getBaseContext(),
						"Unable to Delete Event", Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected void onCancelled() {
			mDeleteTask = null;
		}
	}
}
