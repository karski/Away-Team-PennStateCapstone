package edu.psu.team3.app.awayteam;

import java.util.Comparator;

public class TeamMember {

	public String userName = "";
	public String firstName = "";
	public String lastName = "";
	public String email = "";
	public String phone = "";
	public double lat, lon = 0;
	public boolean manager = false;

	public TeamMember(String mUserName,String mFirstName, String mLastName, String mEmail,
			String mPhone, double mLat, double mLon, boolean mManager) {
		userName = mUserName;
		firstName = mFirstName;
		lastName = mLastName;
		email = mEmail;
		phone = mPhone;
		lat = mLat;
		lon = mLon;
		manager = mManager;
	}

	public TeamMember() {

	}

	public static Comparator<TeamMember> FirstNameComparator = new Comparator<TeamMember>() {

		@Override
		public int compare(TeamMember a, TeamMember b) {
			String aName = a.firstName+" "+a.lastName;
			String bName = b.firstName+" "+b.lastName;
			return aName.compareTo(bName);
		}

	};
	public static Comparator<TeamMember> LastNameComparator = new Comparator<TeamMember>() {

		@Override
		public int compare(TeamMember a, TeamMember b) {
			String aName = a.lastName+" "+a.firstName;
			String bName = b.lastName+" "+b.firstName;
			return aName.compareTo(bName);
		}

	};
}