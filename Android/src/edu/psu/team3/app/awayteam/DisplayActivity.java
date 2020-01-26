package edu.psu.team3.app.awayteam;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class DisplayActivity extends Activity implements ActionBar.TabListener {
	private GetTeamTask mGetTeam = null;
	private RefreshSpinnerTask mRefreshList = null;
	private LeaveTeamTask mLeaveTeam = null;
	public UpdateLocationTask mLocation = null;
	public ActionTask mAction = null;
	public UploadReceiptTask mReceiptTask = null;

	ActionBar actionBar;
	SectionsPagerAdapter mSectionsPagerAdapter;
	Spinner spinnerView;
	ViewPager mViewPager;
	private Menu optionsMenu;
	Location lastKnownLocation;
	List<TeamMember> memberPromoteList = new ArrayList<TeamMember>();

	// For testing
	public DialogFragment currentDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//TODO: catch this
		// set the activity that will host fragments
		setContentView(R.layout.activity_display);

		// Set up the action bar. For tabs
		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		initActionBar();

	}

	public void initActionBar() {
		// Add dropdown to action bar
		actionBar = getActionBar();
		actionBar.show();
		int titleId = Resources.getSystem().getIdentifier("action_bar_title",
				"id", "android");
		View titleView = findViewById(titleId);

		// attach listener for handling spinner selection change
		spinnerView = (Spinner) getLayoutInflater().inflate(
				R.layout.team_spinner_layout, null);

		// swap out title for spinner
		ViewGroupUtils.replaceView(titleView, spinnerView);
		spinnerView
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// Collect team ID from teamList and save to current
						// TeamID for reference
						UserSession s = UserSession
								.getInstance(getBaseContext());
						int teamID = (int) s.teamList.get(position)[0];
						s.updateCurrentTeam(teamID);

						// load a new team based on selection
						if (mGetTeam == null) {
							try {
								mGetTeam = new GetTeamTask();
								mGetTeam.execute(teamID);
								setRefreshActionButtonState(true);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});

		// populate spinner if there are teams available
		try {
			if (UserSession.getInstance(this).teamList != null) {
				refreshTeamSpinner();
			}

			// Request the team information
			if (UserSession.getInstance(this).activeTeam == null) {
				refreshTeam(UserSession.getInstance(this).currentTeamID);
			}
		} catch (Exception e) {
			Log.e("INIT",
					"Caught an error trying to start the actionbar Spinner: "
							+ e.toString());
		}
	}

	// Catch exit conditions and cancel background tasks in an orderly fashion
	// to prevent background tasks crashing in unpredictable ways
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mGetTeam != null) {
			mGetTeam.cancel(true);
		}
		if (mRefreshList != null) {
			mRefreshList.cancel(true);
		}
		if (mLeaveTeam != null) {
			mLeaveTeam.cancel(true);
		}
		if (mLocation != null) {
			mLocation.cancel(true);
		}
		if (mAction != null) {
			mAction.cancel(true);
		}
		if (mReceiptTask != null) {
			mReceiptTask.cancel(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.optionsMenu = menu;
		getMenuInflater().inflate(R.menu.display, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Configure menus to reflect user role

		menu.clear();
		getMenuInflater().inflate(R.menu.display, menu);
		if (UserSession.getInstance(getBaseContext()) != null
				&& UserSession.getInstance(getBaseContext()).activeTeam != null) {
			if (UserSession.getInstance(getBaseContext()).activeTeam.userManager) {
				menu.clear();
				getMenuInflater().inflate(R.menu.display_manager, menu);
			}
		}
		this.optionsMenu = menu;
		UserSession s = UserSession.getInstance(this);
		if (s.activeTeam == null && s.currentTeamID >= 0) {
			setRefreshActionButtonState(true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			refreshTeam(UserSession.getInstance(getBaseContext()).currentTeamID);
			return true;
		}
		if (id == R.id.action_leave) {
			if (mLeaveTeam == null) {
				mLeaveTeam = new LeaveTeamTask();
				mLeaveTeam.execute(this);
			}
		}
		if (id == R.id.action_edit_team) {
			DialogFragment newFragment = new EditTeamDialog();
			newFragment.show(getFragmentManager(), null);
			return true;
		}
		if (id == R.id.action_add_manager) {
			UserSession s = UserSession.getInstance(this);
			List<String> promoteNames = new ArrayList<String>();
			for (TeamMember member : s.activeTeam.teamMembers) {
				if (!member.manager) {
					memberPromoteList.add(member);
					promoteNames.add(member.firstName + " " + member.lastName);
				}
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Select a Member make Manager");
			final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.select_dialog_item, promoteNames);

			builder.setNegativeButton("cancel",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int selected) {
					if (mAction == null) {
						mAction = new ActionTask();
						mAction.execute(
								memberPromoteList.get(selected).userName,
								"promote");
					}
				}
			});
			builder.show();

		}
		if (id == R.id.action_create_team) {
			DialogFragment newFragment = new CreateTeamDialog();
			newFragment.show(getFragmentManager(), null);
			currentDialog = newFragment;
			return true;
		}
		if (id == R.id.action_join_team) {
			DialogFragment newFragment = new JoinTeamDialog();
			newFragment.show(getFragmentManager(), null);
			return true;
		}
		if (id == R.id.action_modify_account) {
			DialogFragment newFragment = new EditAccountDialog();
			newFragment.show(getFragmentManager(), null);
			return true;
		}
		if (id == R.id.action_logout) {
			UserSession.getInstance(getBaseContext()).terminateSession();
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			// check for active team before displaying anything besides
			// placeholder
			if (UserSession.getInstance(getBaseContext()).activeTeam != null) {
				String[] titles = getResources().getStringArray(
						R.array.tab_titles);
				switch (titles[position]) {
				case "Overview":
					return new OverviewFragment();
				case "Member List":
					return new MembersFragment();
				case "Team Calendar":
					return new CalendarFragment();
				case "Team Tasks":
					return new TaskFragment();
				case "Map":
					return new MapFragment();
				case "Expenses":
					return new ExpenseFragment();
				}
			}
			return new PlaceholderFragment();
		}

		@Override
		public int getCount() {
			// count the number of titles in the string file
			return getResources().getStringArray(R.array.tab_titles).length;
		}

		@Override
		public int getItemPosition(Object item) {
			return POSITION_NONE;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return getResources().getStringArray(R.array.tab_titles)[position];
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_display,
					container, false);
			// button listeners:
			rootView.findViewById(R.id.create_button).setOnClickListener(
					new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							DialogFragment newFragment = new CreateTeamDialog();
							newFragment.show(getFragmentManager(), null);
						}
					});
			rootView.findViewById(R.id.join_button).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							DialogFragment newFragment = new JoinTeamDialog();
							newFragment.show(getFragmentManager(), null);
						}
					});
			return rootView;
		}
	}

	// catches activity returns and passes them along to the correct fragment
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	// Collects the new team ID from a dialog
	public void refreshTeam(int teamID) {
		Log.v("DISPLAY", "received team selection: " + teamID);
		// spin the refresh button to indicate progress
		setRefreshActionButtonState(true);

		// run background task to update spinner
		// TeamID will already be saved as the UserSession team ID, but just
		// double check
		UserSession.getInstance(this).updateCurrentTeam(teamID);
		if (mRefreshList == null) {
			try {
				mRefreshList = new RefreshSpinnerTask();
				mRefreshList.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// run background task to get team info if team exists
		if (teamID != -1 && mGetTeam == null) {
			try {
				mGetTeam = new GetTeamTask();
				mGetTeam.execute(teamID);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (teamID == -1) {
			// temporarily show placeholder fragment
			mSectionsPagerAdapter.notifyDataSetChanged();
			mViewPager.invalidate();
			setRefreshActionButtonState(false);
		}

		// This seems like a good place to get the user location
		// get current location
		if (mLocation == null) {
			LocationManager mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			lastKnownLocation = mgr
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (lastKnownLocation != null) {
				mLocation = new UpdateLocationTask();
				mLocation.execute();
			}
		}
	}

	private void refreshTeamSpinner() {
		// reload the spinner data
		// ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		// R.layout.team_spinner_item, UserSession.getInstance(
		// getBaseContext()).getTeamListNames());
		// adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		TeamMemberListAdapter adapter = new TeamMemberListAdapter(this,
				R.layout.team_spinner_item,
				UserSession.getInstance(this).teamList);
		spinnerView.setAdapter(adapter);
		// set the spinner to the correct location
		try {
			UserSession s = UserSession.getInstance(this);
			if (s.currentTeamID > 0) {
				spinnerView
						.setSelection(s.getTeamListPosition(s.currentTeamID));
			}
		} catch (Exception e) {
			Log.e("Spinner", "Error while refreshing spinner: " + e.toString());
		}
	}

	public void setRefreshActionButtonState(final boolean refreshing) {
		if (optionsMenu != null) {
			final MenuItem refreshItem = optionsMenu
					.findItem(R.id.action_refresh);
			if (refreshItem != null) {
				if (refreshing) {
					refreshItem
							.setActionView(R.layout.actionbar_indeterminate_progress);
				} else {
					refreshItem.setActionView(null);
				}
			}
		}
	}

	public void initReceiptTask() {
		mReceiptTask = new UploadReceiptTask();
	}

	// uploads receipt image - requires 2 parameters: expense ID #, receipt URI
	public class UploadReceiptTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {
			Integer result = 0;
			try {
				UserSession s = UserSession.getInstance(getBaseContext());
				int id = (int) params[0];
				Uri receiptURI = (Uri) params[1];
				Log.v("UPLOAD",
						"ReceiptURI="
								+ ((receiptURI == null) ? "null" : receiptURI
										.toString()));

				if (id > 0 && receiptURI != null) {
					// try to upload the receipt if rest of receipt is uploaded
					// to
					// attach to
					result = CommUtil.UploadReceipt(getBaseContext(),
							s.getUsername(), s.currentTeamID, id, receiptURI);
				}

				Log.v("Receipt", "returned from commutil.  result = " + result);
			} catch (Exception e) {
				Log.e("RECEIPT", "Failed to upload receipt: " + e.toString());
			}
			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mReceiptTask = null;
			if (result == 0) {
				Toast.makeText(getBaseContext(), "Unable to Upload Receipt",
						Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected void onCancelled() {
			Log.v("Upload", "Receipt upload was canceled. Status = "
					+ mReceiptTask.getStatus().toString());
			if (!mReceiptTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
				Toast.makeText(getBaseContext(), "Unable to Upload Receipt",
						Toast.LENGTH_SHORT).show();
			}
			mReceiptTask = null;
		}
	}

	// Collect a team from the server
	// requires a team ID passed as only parameter
	public class GetTeamTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {
			UserSession s = UserSession.getInstance(getBaseContext());
			// dispatch the collection method
			Integer result = 0;
			result = CommUtil.GetTeam(getBaseContext(), (int) params[0],
					s.getUsername());
			Integer secondary = CommUtil.GetExpenses(getBaseContext(),
					s.getUsername(), (int) params[0]);
			Log.v("Background", "returned from commutil.  result = " + result);

			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mGetTeam = null;

			switch (result) {
			case 1: // success!
				mSectionsPagerAdapter.notifyDataSetChanged();
				mViewPager.invalidate();
				break;
			case -1:
				Toast.makeText(getBaseContext(), "Denied Access to Team",
						Toast.LENGTH_SHORT).show();
				mSectionsPagerAdapter.notifyDataSetChanged();
				mViewPager.invalidate();
				break;
			default: // some other error occured

				Toast.makeText(getBaseContext(),
						"Unable to get Team information", Toast.LENGTH_SHORT)
						.show();
			}
			setRefreshActionButtonState(false);

		}

		@Override
		protected void onCancelled() {
			mGetTeam = null;
			setRefreshActionButtonState(false);
		}

	}

	// Update Team Spinner
	public class RefreshSpinnerTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {
			UserSession s = UserSession.getInstance(getBaseContext());
			Integer result = 0;
			result = CommUtil.GetMemberTeamsList(getBaseContext(),
					s.getUsername());

			Log.v("Background", "returned from commutil.  result = " + result);

			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mRefreshList = null;

			switch (result) {
			case 1: // success!
				// refresh the list
				refreshTeamSpinner();
				break;
			case -1: // no teams available, refresh spinner to clear old data
				refreshTeamSpinner();
				break;
			default: // some other error occured

				Toast.makeText(getBaseContext(),
						"Unable to get list of your teams", Toast.LENGTH_SHORT)
						.show();
			}
		}

		@Override
		protected void onCancelled() {
			mRefreshList = null;
		}

	}

	// Dispatch request to leave the team
	// INPUT: first parameter will be the activity context,
	// second will be boolean value about whether this is a
	// confirmed request
	public class LeaveTeamTask extends AsyncTask<Object, Void, Integer> {

		Context activity = null;

		@Override
		protected Integer doInBackground(Object... params) {
			UserSession s = UserSession.getInstance(getBaseContext());
			boolean confirmed = false;
			activity = (Context) params[0];
			if (params.length > 1) {
				confirmed = (boolean) params[1];
			}
			// dispatch the collection method
			Integer result = 0;
			result = CommUtil.LeaveTeam(getBaseContext(), s.getUsername(),
					s.currentTeamID, confirmed);
			Log.v("Background", "returned from commutil.  result = " + result);

			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			switch (result) {
			case 1: // success!
				UserSession.getInstance(getBaseContext()).activeTeam = null;
				refreshTeam(-1);
				break;
			case -1:
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle("Are You Sure?");
				builder.setMessage(
						"If you leave this team, it will be deleted.")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										mLeaveTeam = null;
										mLeaveTeam = new LeaveTeamTask();
										mLeaveTeam.execute(activity, true);
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// User cancelled the dialog
									}
								});
				// Create the AlertDialog object and return it
				builder.create().show();
				break;
			default: // some other error occured

				Toast.makeText(getBaseContext(), "Unable to complete Request",
						Toast.LENGTH_SHORT).show();
			}
			mLeaveTeam = null;
		}

		@Override
		protected void onCancelled() {
			mLeaveTeam = null;
		}
	}

	// Update user's location
	public class UpdateLocationTask extends AsyncTask<Object, Void, Integer> {

		@Override
		protected Integer doInBackground(Object... params) {
			UserSession s = UserSession.getInstance(getBaseContext());
			Integer result = 0;
			result = CommUtil.UpdateUserLocation(getBaseContext(),
					s.getUsername(), lastKnownLocation.getLatitude(),
					lastKnownLocation.getLongitude());

			Log.v("Background", "returned from commutil.  result = " + result);

			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mLocation = null;
			// we're done here!
		}

		@Override
		protected void onCancelled() {
			mLocation = null;
		}

	}

	public void initActionTask() {
		mAction = new ActionTask();
	}

	// background task to take action on a pending user
	// requires parameters: target username, action
	// "approve"/"remove"/"promote"/"demote"
	// optional input: dialog so that it may be dismissed
	public class ActionTask extends AsyncTask<Object, Void, Integer> {
		Dialog d = null;

		@Override
		protected Integer doInBackground(Object... params) {
			UserSession s = UserSession.getInstance(getBaseContext());
			String targetUserName = (String) params[0];
			String action = (String) params[1];
			if (params.length > 2) {
				d = (Dialog) params[2];
			}
			Integer result = 0;
			result = CommUtil.ManagerAction(getBaseContext(), s.getUsername(),
					s.currentTeamID, targetUserName, action);
			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mAction = null;
			if (result == 1) {// success!
				try {
					if (d != null) {
						d.dismiss();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				refreshTeam(UserSession.getInstance(getBaseContext()).currentTeamID);
			} else {// some error occured
				Toast.makeText(getBaseContext(), "Unable to Complete Action",
						Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected void onCancelled() {
			mAction = null;
		}
	}

}
