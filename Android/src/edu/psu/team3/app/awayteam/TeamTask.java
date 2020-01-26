package edu.psu.team3.app.awayteam;

import java.util.Comparator;

public class TeamTask {

	public int id = -1;
	public String title = "title";
	public String description = "";
	public boolean complete = false;

	public TeamTask(int nID, String nTitle, String nDescription,
			boolean nComplete) {
		id = nID;
		title = nTitle;
		description = nDescription;
		complete = nComplete;
	}

	public TeamTask() {

	}

	// compare tasks alphabetically based on title
	public static Comparator<TeamTask> AlphaComparator = new Comparator<TeamTask>() {

		@Override
		public int compare(TeamTask a, TeamTask b) {
			return a.title.compareTo(b.title);
		}

	};
	
	// compare tasks based on id
	public static Comparator<TeamTask> IdComparator = new Comparator<TeamTask>() {

		@Override
		public int compare(TeamTask a, TeamTask b) {
			if (a.id == b.id) {
				return 0;
			} else if (a.id>b.id) {
				return 1;
			} else {
				return -1;
			}
		}

	};

	// Compare completed to incomplete tasks, placing completed tasks as
	// "greater" so stacked to the bottom
	public static Comparator<TeamTask> CompletedComparator = new Comparator<TeamTask>() {

		@Override
		public int compare(TeamTask a, TeamTask b) {
			if (a.complete == b.complete) {
				return 0;
			} else if (a.complete) {
				return 1;
			} else {
				return -1;
			}
		}

	};
}
