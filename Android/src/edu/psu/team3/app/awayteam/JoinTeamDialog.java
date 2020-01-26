package edu.psu.team3.app.awayteam;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

public class JoinTeamDialog extends DialogFragment {
	private GetListTask mGetListTask = null;
	private JoinTask mJoinTask = null;

	private List<Object[]> searchList = new ArrayList<Object[]>();

	private ListView mTeamListView;
	private SearchView mSearchView;
	private View mProgressView;

	// list for holding values from server
	// format is team ID,teamName,teamLocation,managed
	private List<Object[]> allTeamsList = new ArrayList<Object[]>();

	private static Object[] mSelection = null; // selection info made by user

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// inflate custom view
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setTitle("Join A Team");
		builder.setIcon(getResources().getDrawable(
				R.drawable.ic_action_add_person));
		builder.setView(inflater.inflate(R.layout.dialog_join_team, null))
		// Add action button
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								JoinTeamDialog.this.getDialog().cancel();
							}
						});

		return builder.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		// resize to prevent keyboard from covering dialog buttons
		getDialog().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		mTeamListView = (ListView) getDialog().findViewById(R.id.teamListView);
		mSearchView = (SearchView) getDialog()
				.findViewById(R.id.teamSearchView);
		mProgressView = getDialog().findViewById(R.id.joinTeam_progress);
		// Collect info on all teams
		try {
			mGetListTask = new GetListTask();
			mGetListTask.execute();

		} catch (Exception e) {
			e.printStackTrace();
		}

		refreshList(allTeamsList);

		mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String query) {
				search(query);
				return true;
			}

		});

		mSearchView.setOnCloseListener(new OnCloseListener() {

			@Override
			public boolean onClose() {
				refreshList(allTeamsList);
				return false;
			}

		});

		// Setup listener for list selections
		mTeamListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View targetView,
					int position, long rowID) {
				mSelection = (Object[]) adapter.getItemAtPosition(position);
				// make background call - this will determine if the
				// dialog closes
				if (mJoinTask == null && mSelection != null) {
					try {
						mJoinTask = new JoinTask();
						mJoinTask.execute();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}

		});
	}

	// Search using list
	private void search(String query) {
		searchList = new ArrayList<Object[]>();
		for (Object[] row : allTeamsList) {
			if (((String) row[1]).toLowerCase().contains(query.toLowerCase())
					|| ((String) row[2]).toLowerCase().contains(
							query.toLowerCase())) {
				searchList.add(row);
			}
		}
		// update listView with search results
		refreshList(searchList);
	}

	// Updates the listview with provided list of teams
	private void refreshList(List<Object[]> teamList) {
		// set up listView adapter and populate
		TeamListAdapter adapter = new TeamListAdapter(getActivity(), 0,
				teamList);
		// Attach the adapter to a ListView
		mTeamListView.setAdapter(adapter);
	}

	public class GetListTask extends AsyncTask<Object, Void, List<Object[]>> {

		@Override
		protected List<Object[]> doInBackground(Object... params) {
			// dispatch the login method
			List<Object[]> result = null;
			result = CommUtil.GetAllTeamsList(getActivity());

			return result;
		}

		@Override
		protected void onPostExecute(final List<Object[]> result) {
			mGetListTask = null;
			mProgressView.setVisibility(View.GONE);

			if (result != null) {
				allTeamsList = result;
				refreshList(allTeamsList);

			} else {// some error occured
				Toast.makeText(getActivity().getBaseContext(),
						"Unable to collect Team List", Toast.LENGTH_SHORT)
						.show();
			}
		}

		@Override
		protected void onCancelled() {
			mGetListTask = null;
		}
	}

	public class JoinTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {
			// dispatch the login method
			int result = 0;
			result = CommUtil.JoinTeam(getActivity(), (int) mSelection[0],
					UserSession.getInstance(getActivity()).getUsername());

			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mJoinTask = null;
			mProgressView.setVisibility(View.GONE);

			if (result == 1) {
				// Successfully joined team or was added to pending members
				// Take action based on whether the team is managed
				if ((boolean) mSelection[3]) {
					Toast.makeText(
							getActivity(),
							"Team information will become visible when a Team Manager approves membership",
							Toast.LENGTH_LONG).show();
					// update view to the same current team
					((DisplayActivity) getActivity()).refreshTeam(UserSession
							.getInstance(getActivity()).currentTeamID);

				} else {
					// Public team selected - pass back team id so it can be
					// loaded
					((DisplayActivity) getActivity())
							.refreshTeam((int) JoinTeamDialog.mSelection[0]);
				}
				getDialog().dismiss();

			} else if (result == -2) {
				// user is already a member - navigate them there!
				Toast.makeText(getActivity().getBaseContext(),
						"You are already on this team!", Toast.LENGTH_SHORT)
						.show();
				((DisplayActivity) getActivity())
						.refreshTeam((int) JoinTeamDialog.mSelection[0]);
				getDialog().dismiss();
			}else if(result==-3){
				Toast.makeText(getActivity().getBaseContext(),
						"Membership Pending for this team.", Toast.LENGTH_SHORT).show();
				Toast.makeText(getActivity().getBaseContext(),
						"A Team Manager must approve your membership", Toast.LENGTH_SHORT).show();
			} else {// some error occured
				Toast.makeText(getActivity().getBaseContext(),
						"Unable to Join Team", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onCancelled() {
			mJoinTask = null;
		}
	}

}
