package edu.psu.team3.app.awayteam;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class ViewGroupUtils {

	public static ViewGroup getParent(View view) {
		return (ViewGroup) view.getParent();
	}

	public static void removeView(View view) {
		try {
			ViewGroup parent = getParent(view);
			if (parent != null) {
				parent.removeView(view);
			}
		} catch (Exception e) {
			Log.e("ViewUtils", e.toString());
			e.printStackTrace();
		}
	}

	public static void replaceView(View currentView, View newView) {
		try {
			ViewGroup parent = getParent(currentView);
			if (parent == null) {
				return;
			}
			final int index = parent.indexOfChild(currentView);
			removeView(currentView);
			removeView(newView);
			parent.addView(newView, index);
		} catch (Exception e) {
			Log.e("ViewUtils", e.toString());
			e.printStackTrace();
		}
	}
}
