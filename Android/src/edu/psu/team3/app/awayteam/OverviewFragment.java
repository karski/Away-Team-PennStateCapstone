package edu.psu.team3.app.awayteam;

import java.util.List;

import edu.psu.team3.app.awayteam.TaskFragment.DeleteTask;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.ExtractedText;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class OverviewFragment extends Fragment {
	private PendingTask mPendingTask = null;
	//private ActionTask mAction = null;

	TextView mTeamNameView;
	TextView mTeamLocView;
	TextView mDescriptionView;
	View mPendingDivView;
	TextView mPendingLabelView;
	TableLayout mPendingTableView;
	View mManagerDivView;
	TextView mManagerLabelView;
	TableLayout mManagerTableView;

	List<Object[]> pendingMembers;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_overview, container,
				false);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		// assign views
		mTeamNameView = (TextView) getView().findViewById(R.id.team_name_text);
		mTeamLocView = (TextView) getView().findViewById(
				R.id.team_location_text);
		mDescriptionView = (TextView) getView().findViewById(
				R.id.team_description_text);
		mPendingDivView = (View) getView().findViewById(R.id.pending_div);
		mPendingLabelView = (TextView) getView().findViewById(
				R.id.pending_label);
		mPendingTableView = (TableLayout) getView().findViewById(
				R.id.pending_table);
		mManagerDivView = (View) getView().findViewById(R.id.manager_div);
		mManagerLabelView = (TextView) getView().findViewById(
				R.id.manager_label);
		mManagerTableView = (TableLayout) getView().findViewById(
				R.id.manager_table);

		// load data into views
		UserSession s = UserSession.getInstance(getActivity());
		mTeamNameView.setText(s.activeTeam.name);
		mTeamLocView.setText(s.activeTeam.location);
		mDescriptionView.setText(s.activeTeam.description);
		mTeamLocView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					String queryLocation = UserSession
							.getInstance(getActivity()).activeTeam.location;
					queryLocation = queryLocation.replace(' ', '+'); // format
																		// as
																		// query
					Intent geoIntent = new Intent(
							android.content.Intent.ACTION_VIEW, Uri
									.parse("geo:0,0?q=" + queryLocation)); // Prepare
																			// intent
					geoIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(geoIntent); // Initiate lookup
				} catch (Exception e) {
					Toast.makeText(getActivity(),
							"Cannot open Map application", Toast.LENGTH_SHORT)
							.show();
				}
			}
		});

		if (s.activeTeam.managed) {
			mManagerDivView.setVisibility(View.VISIBLE);
			mManagerLabelView.setVisibility(View.VISIBLE);
			mManagerTableView.setVisibility(View.VISIBLE);
			// build managers table
			mManagerTableView.removeAllViews();
			List<TeamMember> managers = s.activeTeam.getManagers();
			for (final TeamMember manager : managers) {
				TableRow row = (TableRow) LayoutInflater.from(getActivity())
						.inflate(R.layout.member_entry, null);
				TextView name = (TextView) row.findViewById(R.id.member_name);
				name.setText(manager.firstName + " " + manager.lastName);
				row.findViewById(R.id.role_image).setVisibility(View.VISIBLE);
				row.setClickable(true);
				row.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// take action to display selected user's detail
						DialogFragment newFragment = new MemberDetailDialog();
						Bundle args = new Bundle();
						args.putString("userName", manager.userName);
						newFragment.setArguments(args);
						newFragment.show(getFragmentManager(), null);
					}
				});
				mManagerTableView.addView(row);
			}

			if (s.activeTeam.userManager) {
				// check for pending members
				mPendingTableView.removeAllViews();
				mPendingDivView.setVisibility(View.VISIBLE);
				mPendingLabelView.setVisibility(View.VISIBLE);
				mPendingTableView.setVisibility(View.VISIBLE);

				// dispatch background task to collect pending members
				if (mPendingTask == null) {
					mPendingTask = new PendingTask();
					mPendingTask.execute();
				}
			}
		}
	}

	// Clean up before exiting
	public void onDestroy() {
		super.onDestroy();
		if (mPendingTask != null) {
			mPendingTask.cancel(true);
		}
	}

	// background task to collect pending members
	public class PendingTask extends AsyncTask<Object, Void, List<Object[]>> {
		String username;

		@Override
		protected List<Object[]> doInBackground(Object... params) {
			UserSession s = UserSession.getInstance(getActivity());
			List<Object[]> result = null;

			result = CommUtil.GetPendingMembers(getActivity(), s.getUsername(),
					s.currentTeamID);
			return result;
		}

		@Override
		protected void onPostExecute(final List<Object[]> result) {
			mPendingTask = null;

			if (result == null) {
				// let the manager know that no one is trying to join
				TableRow row = (TableRow) LayoutInflater.from(getActivity())
						.inflate(R.layout.text_row, null);
				mPendingTableView.addView(row);
			} else {
				// build table with members returned
				pendingMembers = result;
				for (Object[] pending : result) {
					// build table row
					TableRow row = (TableRow) LayoutInflater
							.from(getActivity()).inflate(
									R.layout.pending_entry, null);
					((TextView) row.findViewById(R.id.pendingName))
							.setText((String) pending[1] + " "
									+ (String) pending[2]);
					((TextView) row.findViewById(R.id.pendingEmail))
							.setText((String) pending[3]);
					((Button) row.findViewById(R.id.pendingAcceptButton))
							.setContentDescription((String) pending[0]);
					((Button) row.findViewById(R.id.pendingAcceptButton))
							.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Log.v("pending",
											"accepting member: "
													+ v.getContentDescription());
									if (((DisplayActivity) getActivity()).mAction == null) {
										// mAction = new ActionTask();
										((DisplayActivity) getActivity())
												.initActionTask();
										((DisplayActivity) getActivity()).mAction.execute(
												v.getContentDescription(),
												"approve");
									}
								}
							});
					((Button) row.findViewById(R.id.pendingRejectButton))
							.setContentDescription((String) pending[0]);
					((Button) row.findViewById(R.id.pendingRejectButton))
							.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Log.v("pending",
											"rejecting member: "
													+ v.getContentDescription());
									if (((DisplayActivity) getActivity()).mAction == null) {
										// mAction = new ActionTask();
										((DisplayActivity) getActivity())
												.initActionTask();
										((DisplayActivity) getActivity()).mAction.execute(
												v.getContentDescription(),
												"remove");
									}
								}
							});

					mPendingTableView.addView(row);
				}
			}

		}

		@Override
		protected void onCancelled() {
			mPendingTask = null;
		}
	}

	// background task to take action on a pending user
	// requires parameters: target username, action
	// "approve"/"remove"/"promote"/"demote"
	// public class ActionTask extends AsyncTask<Object, Void, Integer> {
	// @Override
	// protected Integer doInBackground(Object... params) {
	// UserSession s = UserSession.getInstance(getActivity());
	// String targetUserName = (String) params[0];
	// String action = (String) params[1];
	// Integer result = 0;
	// result = CommUtil.ManagerAction(getActivity(), s.getUsername(),
	// s.currentTeamID, targetUserName, action);
	//
	// return result;
	// }
	//
	// @Override
	// protected void onPostExecute(final Integer result) {
	// mAction = null;
	// if (result == 1) {// success!
	// ((DisplayActivity) getActivity()).refreshTeam(UserSession
	// .getInstance(getActivity()).currentTeamID);
	// } else {// some error occured
	// Toast.makeText(getActivity().getBaseContext(),
	// "Unable to Complete Action", Toast.LENGTH_SHORT).show();
	// }
	//
	// }
	//
	// @Override
	// protected void onCancelled() {
	// mAction = null;
	// }
	// }
}
