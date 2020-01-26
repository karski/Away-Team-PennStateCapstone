package edu.psu.team3.app.awayteam;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.android.gms.internal.m;

import edu.psu.team3.app.awayteam.TaskListAdapter.CheckTask;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ExpenseListAdapter extends ArrayAdapter<TeamExpense> {
	ImageButton tempButtonHolder;

	List<TeamExpense> expenseList;
	List<TeamExpense> selectedList = new ArrayList<TeamExpense>();
	private Context mContext;

	public ExpenseListAdapter(Context context, List<TeamExpense> objects) {
		super(context, R.layout.expense_entry, objects);
		expenseList = objects;
		mContext = context;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowV = inflater.inflate(R.layout.expense_entry, parent, false);
		TextView dateView = (TextView) rowV.findViewById(R.id.expenseDate);
		dateView.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(
				expenseList.get(position).date));
		TextView catView = (TextView) rowV.findViewById(R.id.expenseCategory);
		catView.setText(expenseList.get(position).category.toString());
		TextView descView = (TextView) rowV
				.findViewById(R.id.expenseDescription);
		descView.setText(expenseList.get(position).description);
		TextView amountView = (TextView) rowV.findViewById(R.id.expenseAmount);
		String formattedAmount = String.format("%1$,.2f",
				expenseList.get(position).amount);
		amountView.setText("$" + formattedAmount);

		if (expenseList.get(position).hasReceipt) {
			ImageButton receiptButton = (ImageButton) rowV
					.findViewById(R.id.receiptButton);
			receiptButton.setVisibility(View.VISIBLE);
			receiptButton.setContentDescription(Integer.toString(expenseList
					.get(position).id));
			receiptButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Download receipt and open it
					// for now, simply download the image in the background
					UserSession s = UserSession.getInstance(getContext()
							.getApplicationContext());
					String url = "https://api.awayteam.redshrt.com/expense/getreceipt?loginId="
							+ s.getUsername()
							+ "&teamId="
							+ s.currentTeamID
							+ "&expenseId="
							+ v.getContentDescription().toString();

					if (NetworkTasks.NetworkAvailable(mContext)) {

						try {
							// use android's download manager! what!?
							DownloadManager.Request request = new DownloadManager.Request(
									Uri.parse(url));
							request.setDescription("Receipt image for AwayTeam app");
							request.setTitle("Receipt Download");
							SimpleDateFormat format = new SimpleDateFormat(
									"yyyyMMddHHmmss");
							request.setDestinationInExternalPublicDir(
									Environment.DIRECTORY_PICTURES, "Receipt_"
											+ format.format(new Date())
											+ ".jpg");
							request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
							DownloadManager manager = (DownloadManager) mContext
									.getSystemService(Context.DOWNLOAD_SERVICE);
							manager.enqueue(request);
							Toast.makeText(mContext, "Downloading Receipt...",
									Toast.LENGTH_SHORT).show();
						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(mContext, "Error downloading image",
									Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(
								mContext,
								"Unable to download Receipt. Check Network Settings",
								Toast.LENGTH_SHORT).show();
					}
				}
			});
		}

		return rowV;
	}

	public void addSelection(int position) {
		selectedList.add(expenseList.get(position));
	}

	public void removeSelection(int postion) {
		selectedList.remove(expenseList.get(postion));
	}

	public void clearSelection() {
		selectedList = new ArrayList<TeamExpense>();
	}

	public List<TeamExpense> getSelection() {
		return selectedList;
	}

}
