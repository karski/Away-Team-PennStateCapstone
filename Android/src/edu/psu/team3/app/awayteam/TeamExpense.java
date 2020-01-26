package edu.psu.team3.app.awayteam;

import java.util.Comparator;
import java.util.Date;

public class TeamExpense {

	public enum Category {
		BREAKFAST(1), LUNCH(2), DINNER(3), SNACK(4), OTHER(5);
		private int value;

		private Category(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	public int id = -1;
	public Date date = null;
	public double amount = 0;
	public Category category = Category.OTHER;
	public String description = "";
	public boolean hasReceipt = false;

	public TeamExpense(int expID, Date expDate, double expAmount,
			Category expCategory, String expDescription, boolean receipt) {
		id = expID;
		date = expDate;
		amount = expAmount;
		category = expCategory;
		description = expDescription;
		hasReceipt = receipt;
	}

	public TeamExpense() {

	}

	public static Comparator<TeamExpense> DateComparator = new Comparator<TeamExpense>() {

		@Override
		public int compare(TeamExpense a, TeamExpense b) {
			return a.date.compareTo(b.date);
		}

	};


}
