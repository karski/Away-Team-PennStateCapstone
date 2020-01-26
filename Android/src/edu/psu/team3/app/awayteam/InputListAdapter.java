package edu.psu.team3.app.awayteam;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class InputListAdapter extends ArrayAdapter<Object[]> {

	Context mContext;
	List<Object[]> inputList;

	public InputListAdapter(Context context, int resource,
			List<Object[]> objects) {
		super(context, R.layout.input_entry, objects);
		mContext = context;
		inputList = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.input_entry, parent, false);
		}
		if (inputList.get(position) != null) {
			// find views in the row
			TextView text = (TextView) convertView.findViewById(R.id.inputText);
			ImageView image = (ImageView) convertView
					.findViewById(R.id.inputImage);

			text.setText(inputList.get(position)[0].toString());
			image.setImageResource((int) inputList.get(position)[1]);

		}
		return convertView;

	}

}
