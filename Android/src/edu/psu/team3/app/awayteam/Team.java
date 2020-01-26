package edu.psu.team3.app.awayteam;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.format.DateUtils;
import android.util.Log;

public class Team {

	public String name = "Team Name!";
	public String description = "";
	public String location = "";
	public boolean managed = false;
	public boolean userManager = false;

	public List<TeamMember> teamMembers = new ArrayList<TeamMember>();
	public List<TeamEvent> teamEvents = new ArrayList<TeamEvent>();
	public List<TeamTask> teamTasks = new ArrayList<TeamTask>();
	public List<TeamExpense> teamExpenses = new ArrayList<TeamExpense>();

	// Create a team from a JSON Object
	// Assumes that the team is a new instance with only default values
	public void importTeam(JSONObject teamObject) throws Exception {
		// collect team info
		try {
			name = teamObject.getString("teamName");
			description = teamObject.getString("teamDescription");
			location = teamObject.getString("teamLocationName");
			managed = teamObject.getBoolean("teamManaged");
		} catch (Exception e) {
			throw new Exception("Error building team");
		}
		// Load team members
		try {
			JSONArray membersArray = teamObject.getJSONArray("members");
			for (int i = 0; i < membersArray.length(); i++) {
				JSONObject memberObject = membersArray.getJSONObject(i);
				String user = memberObject.getString("loginId");
				String first = memberObject.getString("firstName");
				String last = memberObject.getString("lastName");
				String email = memberObject.getString("email");
				String phone = memberObject.getString("phone");
				double lat = 0;
				double lon = 0;
				try {
					lat = Double.parseDouble(memberObject
							.getString("locLatitude"));
					lon = Double.parseDouble(memberObject
							.getString("locLongitude"));
				} catch (Exception ex) {

				}
				boolean manager = memberObject.getBoolean("manager");
				teamMembers.add(new TeamMember(user, first, last, email, phone,
						lat, lon, manager));
			}
			Collections.sort(teamMembers, TeamMember.FirstNameComparator);
		} catch (Exception e) {
			Log.e("TEAM", e.toString());
			throw new Exception("Error building member list");
		}
		// Load Tasks
		try {
			if (!teamObject.isNull("tasks")) {
				JSONArray tasksArray = teamObject.getJSONArray("tasks");
				for (int i = 0; i < tasksArray.length(); i++) {
					JSONObject taskObject = tasksArray.getJSONObject(i);
					int id = taskObject.getInt("taskId");
					String title = taskObject.getString("taskTitle");
					String description = taskObject
							.getString("taskDescription");
					boolean complete = taskObject.getBoolean("taskCompleted");
					teamTasks
							.add(new TeamTask(id, title, description, complete));
				}
			}
		} catch (Exception e) {
			Log.e("TEAM", e.toString());
			throw new Exception("Error building task list");
		}
		// Load Events
		try {
			if (!teamObject.isNull("events")) {
				JSONArray eventsArray = teamObject.getJSONArray("events");
				for (int i = 0; i < eventsArray.length(); i++) {
					JSONObject eventObject = eventsArray.getJSONObject(i);
					int id = eventObject.getInt("teamEventId");
					String title = eventObject.getString("teamEventName");
					String description = eventObject
							.getString("teamEventDescription");
					String location = eventObject
							.getString("teamEventLocationString");
					DateFormat formatter = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					Date start = formatter.parse(eventObject
							.getString("teamEventStartTime"));
					Date end = formatter.parse(eventObject
							.getString("teamEventEndTime"));
					teamEvents.add(new TeamEvent(id, title, description,
							location, start, end));
				}
			}
		} catch (Exception e) {
			Log.e("TEAM", e.toString());
			throw new Exception("Error building events list");
		}
		// determine if the user is a manager
		if (managed) {
			List<TeamMember> managers = getManagers();
			userManager = searchUser(UserSession.getInstance(null)
					.getUsername(), managers);
		}
	}

	// Create an expense list from a JSON Object
	// Will clear out any old expenses first
	public void importExpenses(JSONArray expenseArray) throws Exception {
		teamExpenses = new ArrayList<TeamExpense>();
		for (int i = 0; i < expenseArray.length(); i++) {
			try {
				JSONObject expense = expenseArray.getJSONObject(i);
				int expenseID = expense.getInt("expenseId");
				String description = expense.getString("description");
				double amount = expense.getDouble("amount");
				boolean receipt = expense.getBoolean("receipt");
				TeamExpense.Category type = TeamExpense.Category.values()[expense
						.getInt("expType") - 1];
				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				Date expDate = formatter.parse(expense.getString("expDate"));
				teamExpenses.add(new TeamExpense(expenseID, expDate, amount,
						type, description, receipt));
			} catch (Exception e) {
				Log.e("EXPENSE", e.toString());
				throw new Exception("Error building expense report");
			}
		}

	}

	// return a list of managers
	public List<TeamMember> getManagers() {
		List<TeamMember> managers = new ArrayList<TeamMember>();
		for (TeamMember member : teamMembers) {
			if (member.manager) {
				managers.add(member);
			}
		}
		return managers;
	}

	// return true if the specified member is in the list
	public boolean searchUser(String username, List<TeamMember> memberList) {
		for (TeamMember member : memberList) {
			if (member.userName.equals(username)) {
				return true;
			}
		}
		return false;
	}

	// return the team member identified by the username or null if not found
	public TeamMember getUser(String username) {
		for (TeamMember member : teamMembers) {
			if (member.userName.equals(username)) {
				return member;
			}
		}
		return null;
	}

	// remove completed tasks
	public void removeCompletedTasks() {
		for (TeamTask task : teamTasks) {
			if (task.complete) {
				teamTasks.remove(task);
			}
		}
	}

	// return an expense identified by expenseID
	public TeamExpense getExpense(int expenseID) {
		for (TeamExpense expense : teamExpenses) {
			if (expense.id == expenseID) {
				return expense;
			}
		}
		return null;
	}

	// return an event identified by eventID
	public TeamEvent getEvent(int eventID) {
		for (TeamEvent event : teamEvents) {
			if (event.id == eventID) {
				return event;
			}
		}
		return null;
	}

	// return task identified by taskID
	public TeamTask getTask(int taskID) {
		for (TeamTask task : teamTasks) {
			if (task.id == taskID) {
				return task;
			}
		}
		return null;
	}

}
