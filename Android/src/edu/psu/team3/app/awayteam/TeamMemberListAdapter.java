package edu.psu.team3.app.awayteam;

import java.util.List;

import edu.psu.team3.app.awayteam.R.id;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TeamMemberListAdapter extends ArrayAdapter<Object[]> {
	Context mContext;
	List<Object[]> teamList; // [id,name,pending]

	public TeamMemberListAdapter(Context context, int resource,
			List<Object[]> objects) {
		super(context, resource, objects);
		mContext = context;
		teamList = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TextView rowV = (TextView) inflater.inflate(R.layout.team_spinner_item, parent,
				false);

		rowV.setText((String) teamList.get(position)[1]);

		return rowV;

	}
	
	@Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TextView rowV = (TextView) inflater.inflate(R.layout.team_spinner_dropdown, parent,
				false);

		rowV.setText((String) teamList.get(position)[1]);
		
		if((boolean) teamList.get(position)[2]){
			rowV.setEnabled(false);
			rowV.setText((String) teamList.get(position)[1]+"(pending)");
			rowV.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					Toast.makeText(mContext, "Team Membership Pending Approval", Toast.LENGTH_SHORT).show();
					return true;
				}
			});
			rowV.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Toast.makeText(mContext, "Team Membership Pending Approval", Toast.LENGTH_SHORT).show();					
				}
			});
		}
		
        return rowV;
    }

}
