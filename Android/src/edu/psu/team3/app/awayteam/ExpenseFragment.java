package edu.psu.team3.app.awayteam;

import java.util.Collections;
import java.util.List;

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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ExpenseFragment extends Fragment {
	private DeleteExpenseTask mDeleteTask = null;

	TextView expenseTotalView;
	TextView expenseLabel;
	ImageButton addExpenseButton;
	ListView expenseListView;
	ExpenseListAdapter adapter;

	boolean delete = false;
	ActionMode mMode = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_expense, container,
				false);
		// identify UI elements
		expenseTotalView = (TextView) rootView.findViewById(R.id.expenseText);
		expenseLabel = (TextView) rootView.findViewById(R.id.expenseLabel);
		addExpenseButton = (ImageButton) rootView
				.findViewById(R.id.add_expense_button);
		expenseListView = (ListView) rootView
				.findViewById(R.id.expenseListView);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		// ensure list of events is sorted
		UserSession s = UserSession.getInstance(getActivity());
		Collections.sort(s.activeTeam.teamExpenses, TeamExpense.DateComparator);
		calcExpense(s.activeTeam.teamExpenses);
		// fill list
		adapter = new ExpenseListAdapter(getActivity(),
				s.activeTeam.teamExpenses);
		// Attach the adapter to a ListView
		expenseListView.setAdapter(adapter);

		expenseListView
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
							mDeleteTask = new DeleteExpenseTask();
							mDeleteTask.execute();
							mode.finish();
							break;
						case R.id.action_selected_edit:
							// create dialog and pass id
							DialogFragment newFragment = new ExpenseEditDialog();
							Bundle bundle = new Bundle();
							bundle.putInt("expenseID", adapter.getSelection()
									.get(0).id);
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
							break;
						default:
							selectMenu.setGroupVisible(R.id.menu_group_edit,
									false);
							break;
						}

						// calculate for subtotal
						double subtotal = 0;
						for (TeamExpense expense : adapter.getSelection()) {
							subtotal += expense.amount;
						}
						String formattedAmount = String.format("%1$,.2f",
								subtotal);
						mode.setTitle("Subtotal: $" + formattedAmount);
					}
				});

		// Assign actions to buttons
		addExpenseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new ExpenseCreateDialog();
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

	// update the expense total field in the window with the total of all items
	// in the list
	private void calcExpense(List<TeamExpense> expenses) {
		double total = 0;
		for (TeamExpense expense : expenses) {
			total += expense.amount;
		}
		String formattedAmount = String.format("%1$,.2f", total);
		expenseTotalView.setText("$" + formattedAmount);
	}

	// background task to delete the selected expenses
	public class DeleteExpenseTask extends AsyncTask<Object, Void, Integer> {
		@Override
		protected Integer doInBackground(Object... params) {
			UserSession s = UserSession.getInstance(getActivity());
			Integer result = 0;
			for (TeamExpense expense : adapter.getSelection()) {
				result = CommUtil.DeleteExpense(getActivity(), s.getUsername(),
						s.currentTeamID, expense.id);
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
								"Expense Deleted", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(
								getActivity().getBaseContext(),
								adapter.getSelection().size()
										+ " Expenses Deleted",
								Toast.LENGTH_SHORT).show();
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
						"Unable to Delete Expense", Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected void onCancelled() {
			mDeleteTask = null;
		}
	}

}
