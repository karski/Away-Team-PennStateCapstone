package edu.psu.team3.app.awayteam;

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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class TaskFragment extends Fragment {
	private DeleteTask mDeleteTask = null;

	Button mSortButton;
	ImageButton mAddButton;
	ListView mTaskListView;
	TaskListAdapter adapter;
	boolean sort = false;
	boolean delete = false;
	ActionMode mMode = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_tasks, container,
				false);

		// register UI
		mSortButton = (Button) rootView.findViewById(R.id.sort_toggle_button);
		mAddButton = (ImageButton) rootView.findViewById(R.id.add_task_button);
		mTaskListView = (ListView) rootView.findViewById(R.id.taskListView);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		// Fill in listview
		adapter = new TaskListAdapter(getActivity(),
				UserSession.getInstance(getActivity()).activeTeam.teamTasks);

		mSortButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!sort) {
					adapter.sort(TeamTask.CompletedComparator);
					mSortButton.setText(" Sort Oldest First");
				} else {
					adapter.sort(TeamTask.IdComparator);
					mSortButton.setText(" Sort Incomplete First");
				}
				sort = !sort;
			}
		});
		mAddButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new TaskCreateDialog();
				newFragment.show(getFragmentManager(), null);
			}
		});
		// Attach the adapter to a ListView
		mTaskListView.setAdapter(adapter);
		// handle multiple selections
		mTaskListView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			Menu selectMenu;

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
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
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
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
					DialogFragment newFragment = new TaskEditDialog();
					Bundle bundle = new Bundle();
					bundle.putInt("taskID", adapter.getSelection().get(0).id);
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
					selectMenu.setGroupVisible(R.id.menu_group_edit, true);
					mode.setTitle("1 Task Selected");
					break;
				default:
					selectMenu.setGroupVisible(R.id.menu_group_edit, false);
					mode.setTitle(checkedCount + " Tasks Selected");
					break;
				}

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

	// background task to delete the selected tasks
	public class DeleteTask extends AsyncTask<Object, Void, Integer> {
		@Override
		protected Integer doInBackground(Object... params) {
			UserSession s = UserSession.getInstance(getActivity());
			Integer result = 0;
			for (TeamTask task : adapter.getSelection()) {
				result = CommUtil.UpdateTask(getActivity(), s.getUsername(),
						s.currentTeamID, task.id, task.complete, true);
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
								"Task Deleted", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(
								getActivity().getBaseContext(),
								adapter.getSelection().size()
										+ " Tasks Deleted", Toast.LENGTH_SHORT)
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
						"Unable to Delete Task", Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected void onCancelled() {
			mDeleteTask = null;
		}
	}
}
