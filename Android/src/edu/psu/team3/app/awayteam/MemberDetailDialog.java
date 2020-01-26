package edu.psu.team3.app.awayteam;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MemberDetailDialog extends DialogFragment {
	// private ActionTask mAction = null;

	// UI elements
	TextView nameView;
	TextView managerView;
	TextView phoneView;
	TextView emailView;
	ImageButton smsButton;
	ImageButton callButton;
	ImageButton emailButton;
	Button managerRemoveButton;
	Button managerPromoteButton;
	TextView managerToolsView;

	TeamMember member;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// inflate custom view
		LayoutInflater inflater = getActivity().getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.member_detail, null))
		// Add action buttons
				.setNegativeButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						MemberDetailDialog.this.getDialog().cancel();
					}
				});

		return builder.create();
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog d = getDialog();
		// get target team member
		String userName = getArguments().getString("userName");
		member = UserSession.getInstance(getActivity()).activeTeam
				.getUser(userName);
		if (member == null) {
			Toast.makeText(getActivity(), "Team Member Not Found!",
					Toast.LENGTH_SHORT).show();
			d.dismiss();
		}
		// assign UI elements
		nameView = (TextView) d.findViewById(R.id.member_name_text);
		managerView = (TextView) d.findViewById(R.id.member_manager_text);
		phoneView = (TextView) d.findViewById(R.id.phone_text);
		emailView = (TextView) d.findViewById(R.id.email_text);
		smsButton = (ImageButton) d.findViewById(R.id.contact_text_button);
		callButton = (ImageButton) d.findViewById(R.id.contact_call_button);
		emailButton = (ImageButton) d.findViewById(R.id.contact_email_button);
		managerToolsView = (TextView) d
				.findViewById(R.id.member_manager_tools_label);
		managerPromoteButton = (Button) d
				.findViewById(R.id.manager_tool_promote);
		managerRemoveButton = (Button) d.findViewById(R.id.manager_tool_remove);
		// fill data
		nameView.setText(member.firstName + " " + member.lastName);
		if (member.manager) {
			managerView.setVisibility(View.VISIBLE);
		}
		// format phone number if able
		String formattedPhone = "";
		switch (member.phone.length()) {
		case (10):
			formattedPhone = "(" + member.phone.substring(0, 3) + ")"
					+ member.phone.substring(3, 6) + "-"
					+ member.phone.substring(6);
			break;
		case (7):
			formattedPhone = member.phone.substring(0, 3) + "-"
					+ member.phone.substring(3);
			break;
		default:
			formattedPhone = member.phone;
		}
		phoneView.setText(formattedPhone);
		emailView.setText(member.email);
		// setup buttons
		smsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String number = "smsto:" + member.phone;
				// create intent
				Intent smsIntent = new Intent();
				smsIntent.setData(Uri.parse(number));
				smsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// launch intent
				try {
					startActivity(smsIntent);
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(getActivity(),
							"There is no messaging client installed.",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		callButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String number = "tel:" + member.phone;
				// create intent
				Intent dialIntent = new Intent(Intent.ACTION_DIAL);
				dialIntent.setData(Uri.parse(number));
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
		});
		emailButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String email = "mailto:" + member.email;
				// create intent
				Intent emailIntent = new Intent();
				emailIntent.setData(Uri.parse(email));
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
			}
		});
		// show the manager tools if the user is a manager
		if (UserSession.getInstance(getActivity()).activeTeam.userManager
				&& !UserSession.getInstance(getActivity()).getUsername()
						.equals(member.userName)) {
			managerToolsView.setVisibility(View.VISIBLE);
			managerRemoveButton.setVisibility(View.VISIBLE);

			managerRemoveButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// remove member
					if (((DisplayActivity) getActivity()).mAction == null) {
						// mAction = new ActionTask();
						((DisplayActivity) getActivity()).initActionTask();
						((DisplayActivity) getActivity()).mAction.execute(
								member.userName, "remove", getDialog());
					}

				}
			});
			// if the member being viewed is not already a manager, allow
			// promotion
			if (!member.manager) {
				managerPromoteButton.setVisibility(View.VISIBLE);
				managerPromoteButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// assign member as a manager
						if (((DisplayActivity) getActivity()).mAction == null) {
							// mAction = new ActionTask();
							((DisplayActivity) getActivity()).initActionTask();
							((DisplayActivity) getActivity()).mAction.execute(
									member.userName, "promote", getDialog());
						}
					}
				});
			}

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
	// return result;
	// }
	//
	// @Override
	// protected void onPostExecute(final Integer result) {
	// mAction = null;
	// if (result == 1) {// success!
	// ((DisplayActivity) getActivity()).refreshTeam(UserSession
	// .getInstance(getActivity()).currentTeamID);
	// getDialog().dismiss();
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
