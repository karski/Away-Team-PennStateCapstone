package edu.psu.team3.app.awayteam;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskListAdapter extends ArrayAdapter<TeamTask> {
	CheckTask mCheckTask = null;

	List<TeamTask> taskList;
	List<TeamTask> selectedList = new ArrayList<TeamTask>();

	private Context mContext;

	public TaskListAdapter(Context context, List<TeamTask> objects) {
		super(context, R.layout.task_entry, objects);
		taskList = objects;
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowV = inflater.inflate(R.layout.task_entry, parent, false);
		CheckBox taskCheckBox = (CheckBox) rowV
				.findViewById(R.id.task_checkbox);
		taskCheckBox.setChecked(taskList.get(position).complete);
		final int positionHolder = position;
		taskCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

				// Dispatch a message to the server to update the task item
				if (mCheckTask == null) {
					mCheckTask = new CheckTask();
					mCheckTask.execute(positionHolder, isChecked);
					taskList.get(positionHolder).complete = isChecked;
				}
			}
		});
		((TextView) rowV.findViewById(R.id.task_title)).setText(taskList
				.get(position).title);
		((TextView) rowV.findViewById(R.id.task_description)).setText(taskList
				.get(position).description);

		return rowV;
	}

	public void addSelection(int position) {
		selectedList.add(taskList.get(position));
	}

	public void removeSelection(int postion) {
		selectedList.remove(taskList.get(postion));
	}

	public void clearSelection() {
		selectedList = new ArrayList<TeamTask>();
	}

	public List<TeamTask> getSelection() {
		return selectedList;
	}

	// background task to update status of checkboxes
	public class CheckTask extends AsyncTask<Object, Void, Integer> {
		@Override
		protected Integer doInBackground(Object... params) {
			int position = (int) params[0];
			boolean checked = (boolean) params[1];
			UserSession s = UserSession.getInstance(getContext()
					.getApplicationContext());
			Integer result = 0;
			result = CommUtil.UpdateTask(getContext().getApplicationContext(),
					s.getUsername(), s.currentTeamID,
					taskList.get(position).id, checked, false);
			Log.v("Background", "returned from commutil.  result = " + result);

			return result;
		}

		@Override
		protected void onPostExecute(final Integer result) {
			mCheckTask = null;
			if (result == 1) {// success!
				Log.v("TASK", "Check action synced to server");
			} else {// some error occured
				Toast.makeText(getContext().getApplicationContext(),
						"Unable to Update Task", Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected void onCancelled() {
			mCheckTask = null;
		}
	}
}
