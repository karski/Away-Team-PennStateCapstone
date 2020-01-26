package edu.psu.team3.app.awayteam;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TeamListAdapter extends ArrayAdapter<Object[]> {
	Context mContext;
	List<Object[]> teamList;

	public TeamListAdapter(Context context, int resource, List<Object[]> objects) {
		super(context, R.layout.team_list_item, objects);
		mContext = context;
		teamList = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowV = inflater.inflate(R.layout.team_list_item, parent, false);
		if (teamList.get(position) != null) {
			// find views in the row
			TextView nameV = (TextView) rowV.findViewById(R.id.teamEntryName);
			TextView locV = (TextView) rowV
					.findViewById(R.id.teamEntryLocation);
			ImageView manageV = (ImageView) rowV
					.findViewById(R.id.teamEntryManaged);

			nameV.setText(teamList.get(position)[1].toString());
			locV.setText(teamList.get(position)[2].toString());
			if ((boolean) teamList.get(position)[3]) {
				manageV.setVisibility(View.VISIBLE);
			}
		}
		return rowV;

	}

}
