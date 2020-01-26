package edu.psu.team3.app.awayteam;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class MembersFragment extends Fragment {
	// private ActionTask mAction = null;

	// UI references
	private Spinner mGroupSpinner;
	private Spinner mManagerSpinner;
	private ListView mMemberListView;
	private Button mContactManagersButton;
	private Button mContactMembersButton;

	MemberListAdapter adapter;
	ActionMode mMode = null;
	boolean background = false;// state holder for removing or promoting members
								// in background

	// list for spinners
	List<String> modeList = new ArrayList<String>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_members, container,
				false);

		// register UI
		mGroupSpinner = (Spinner) rootView
				.findViewById(R.id.group_contact_mode_spinner);
		mManagerSpinner = (Spinner) rootView
				.findViewById(R.id.manager_contact_mode_spinner);
		mMemberListView = (ListView) rootView.findViewById(R.id.memberListView);
		mContactManagersButton = (Button) rootView
				.findViewById(R.id.manager_contact_button);
		mContactMembersButton = (Button) rootView
				.findViewById(R.id.group_contact_button);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		UserSession s = UserSession.getInstance(getActivity());
		// assign actions to contact buttons and hide manager display if
		// required
		if (s.activeTeam.managed) {
			mContactManagersButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int mode = mManagerSpinner.getSelectedItemPosition();
					List<TeamMember> contacts = UserSession
							.getInstance(getActivity()).activeTeam
							.getManagers();
					contact(mode, contacts);
				}

			});
		} else {
			// not managed, hide unused UI elements
			mContactManagersButton.setVisibility(View.GONE);
			mManagerSpinner.setVisibility(View.GONE);
		}
		mContactMembersButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int mode = mGroupSpinner.getSelectedItemPosition();
				List<TeamMember> contacts = UserSession
						.getInstance(getActivity()).activeTeam.teamMembers;
				contact(mode, contacts);
			}

		});

		// build list of members
		adapter = new MemberListAdapter(getActivity(), 0,
				s.activeTeam.teamMembers);
		// Attach the adapter to a ListView
		mMemberListView.setAdapter(adapter);

		// Setup listener for list selections
		mMemberListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View targetView,
					int position, long rowID) {
				String userName = ((TeamMember) adapter
						.getItemAtPosition(position)).userName;
				// take action to display selected user's detail
				DialogFragment newFragment = new MemberDetailDialog();
				Bundle args = new Bundle();
				args.putString("userName", userName);
				newFragment.setArguments(args);
				newFragment.show(getFragmentManager(), null);

			}

		});

		// setup multi-selection mode
		mMemberListView
				.setMultiChoiceModeListener(new MultiChoiceModeListener() {

					Menu selectMenu;
					boolean manager;

					@Override
					public boolean onPrepareActionMode(ActionMode mode,
							Menu menu) {
						return false;
					}

					@Override
					public void onDestroyActionMode(ActionMode mode) {
						if (!background) {
							adapter.clearSelection();
						}
						// otherwise, hold on to the selection so the background
						// task can use it
						mMode = null;
					}

					@Override
					public boolean onCreateActionMode(ActionMode mode, Menu menu) {
						MenuInflater inflater = getActivity().getMenuInflater();
						inflater.inflate(R.menu.multi_select_members, menu);
						if (UserSession.getInstance(getActivity()).activeTeam.userManager) {
							menu.setGroupVisible(
									R.id.menu_group_manager_header, true);
							menu.setGroupVisible(
									R.id.menu_group_manager_not_manager, true);
							menu.setGroupVisible(
									R.id.menu_group_manager_not_self, true);
							manager = true;
						} else {
							menu.setGroupVisible(
									R.id.menu_group_manager_header, false);
							menu.setGroupVisible(
									R.id.menu_group_manager_not_manager, false);
							menu.setGroupVisible(
									R.id.menu_group_manager_not_self, false);
							manager = false;
						}
						Log.v("MENU", "Manager = " + manager);
						selectMenu = menu;
						background = false;
						mMode = mode;
						return true;
					}

					@Override
					public boolean onActionItemClicked(ActionMode mode,
							MenuItem item) {
						switch (item.getItemId()) {
						case R.id.action_selected_call:
							contact(2, adapter.getSelection());
							adapter.clearSelection();
							mode.finish();
							break;
						case R.id.action_selected_sms:
							contact(1, adapter.getSelection());
							adapter.clearSelection();
							mode.finish();
							break;
						case R.id.action_selected_email:
							contact(0, adapter.getSelection());
							adapter.clearSelection();
							mode.finish();
							break;
						case R.id.action_selected_promote:
							if (((DisplayActivity) getActivity()).mAction == null) {
								background = true;
								((DisplayActivity) getActivity())
										.initActionTask();
								// mAction = new ActionTask();
								((DisplayActivity) getActivity()).mAction
										.execute(
												adapter.getSelection().get(0).userName,
												"promote");
							}
							mode.finish();
							break;
						case R.id.action_selected_remove:
							if (((DisplayActivity) getActivity()).mAction == null) {
								background = true;
								// mAction = new ActionTask();
								((DisplayActivity) getActivity())
										.initActionTask();
								((DisplayActivity) getActivity()).mAction
										.execute(
												adapter.getSelection().get(0).userName,
												"remove");
							}
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
							selectMenu.setGroupVisible(R.id.menu_group_call,
									true);
							mode.setTitle("1 Person Selected");
							break;
						default:
							selectMenu.setGroupVisible(R.id.menu_group_call,
									false);
							mode.setTitle(checkedCount + " People Selected");
							break;
						}

						// ensure manager menu options reflect current selection
						if (manager) {
							if (adapter.selectionContainsManager()
									|| adapter.getSelection().size() > 1) {
								selectMenu.setGroupEnabled(
										R.id.menu_group_manager_not_manager,
										false);
							} else {
								selectMenu.setGroupEnabled(
										R.id.menu_group_manager_not_manager,
										true);
							}
							if (adapter.selectionContainsSelf()
									|| adapter.getSelection().size() > 1) {
								selectMenu
										.setGroupEnabled(
												R.id.menu_group_manager_not_self,
												false);
							} else {
								selectMenu.setGroupEnabled(
										R.id.menu_group_manager_not_self, true);
							}

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

	// dispatches contact command to system handler
	private void contact(int mode, List<TeamMember> contacts) {
		// figure out the mode of contact
		String contactMode = getResources().getStringArray(
				R.array.contact_options)[mode];

		switch (contactMode) {
		case "Email":
			// collect array of email addresses
			StringBuilder emails = new StringBuilder("mailto:");
			for (TeamMember member : contacts) {
				emails.append(member.email + ";");
			}
			// create intent
			Intent emailIntent = new Intent();
			emailIntent.setData(Uri.parse(emails.toString()));
			emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			emailIntent.putExtra(Intent.EXTRA_SUBJECT,
					UserSession.getInstance(getActivity()).activeTeam.name);
			// launch intent
			try {
				startActivity(emailIntent);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(getActivity(),
						"There is no email client installed.",
						Toast.LENGTH_SHORT).show();
			}
			break;
		case "SMS":
			StringBuilder numbers = new StringBuilder("smsto:");
			for (TeamMember member : contacts) {
				numbers.append(member.phone + ";");
			}
			// create intent
			Intent smsIntent = new Intent();
			smsIntent.setData(Uri.parse(numbers.toString()));
			smsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// launch intent
			try {
				startActivity(smsIntent);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(getActivity(),
						"There is no messaging client installed.",
						Toast.LENGTH_SHORT).show();
			}
			break;
		case "Call":
			final StringBuilder phone = new StringBuilder("tel:");
			// can only call one number, so ensure that only one is selected
			if (contacts.size() > 1) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setTitle("Select a Number to Call");
				final ArrayAdapter<String> adapter = new ArrayAdapter<>(
						getActivity(), android.R.layout.select_dialog_item);
				for (TeamMember member : contacts) {
					adapter.add(member.phone);
				}
				builder.setNegativeButton("cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});

				builder.setAdapter(adapter,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int selected) {
								phone.append(adapter.getItem(selected));
								Intent dialIntent = new Intent(
										Intent.ACTION_DIAL);
								dialIntent.setData(Uri.parse(phone.toString()));
								dialIntent
										.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								// launch intent
								try {
									startActivity(dialIntent);
								} catch (android.content.ActivityNotFoundException ex) {
									Toast.makeText(
											getActivity(),
											"There is no phone client installed.",
											Toast.LENGTH_SHORT).show();
								}
							}
						});
				builder.show();

			} else {
				phone.append(contacts.get(0).phone);
				// create intent
				Intent dialIntent = new Intent(Intent.ACTION_DIAL);
				dialIntent.setData(Uri.parse(phone.toString()));
				dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// launch intent
				try {
					startActivity(dialIntent);
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(getActivity(),
							"There is no phone client installed.",
							Toast.LENGTH_SHORT).show();
				}
			}

			break;
		default:
			Toast.makeText(getActivity(), "Unable to find contact mode",
					Toast.LENGTH_SHORT).show();
			return;
		}

	}

	// background task to take action on a pending user
	// will complete action for all selected members
	// requires parameter:action "approve"/"remove"/"promote"/"demote"
	// public class ActionTask extends AsyncTask<Object, Void, Integer> {
	// @Override
	// protected Integer doInBackground(Object... params) {
	// UserSession s = UserSession.getInstance(getActivity());
	// String action = (String) params[0];
	// Integer result = 0;
	// for (TeamMember member : adapter.getSelection())
	// result = CommUtil.ManagerAction(getActivity(), s.getUsername(),
	// s.currentTeamID, member.userName, action);
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
