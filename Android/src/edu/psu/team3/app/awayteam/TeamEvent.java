package edu.psu.team3.app.awayteam;

import java.util.Comparator;
import java.util.Date;

public class TeamEvent {
	public int id = -1;
	public String title = "title";
	public String description = "";
	public String location = "";
	public Date startTime = null;
	public Date endTime = null;

	public TeamEvent(int nID, String nTitle, String nDesc, String locName, Date start,
			Date end) {
		id = nID;
		title = nTitle;
		description = nDesc;
		location = locName;
		startTime = start;
		endTime = end;
	}

	public TeamEvent() {

	}

	public static Comparator<TeamEvent> StartComparator = new Comparator<TeamEvent>() {

		@Override
		public int compare(TeamEvent a, TeamEvent b) {
			return a.startTime.compareTo(b.startTime);
		}

	};
}
