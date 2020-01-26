package edu.psu.team3.app.awayteam;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.*;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

public class CommUtil {

	// Check if username is already being used
	// Returns: 0 = error
	// 1 = available
	// 2 = NOT available
	public static int LoginIDExist(Context context, String username) {
		String url = "https://api.awayteam.redshrt.com/user/LoginIDExist?loginId="
				+ username;

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}
		JSONObject result = null;
		// collect the results
		try {
			result = NetworkTasks.RequestData(false, url, null);
			if (result.getString("response").equals("success")) {
				if (result.getString("message").equals("available")) {
					// success & available
					return 1;
				} else {
					// success, but not available
					return 2;
				}
			} else {
				// returned failure, an error occured
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	// Attempts to create a new user
	// INPUT: pairs = a name=value pairs list containing values for:
	// "loginId","email","password",
	// "firstName","lastName","cellPhone","emergencyPhone"
	// Returns int code based on success:
	// 1 = success! User created
	// 0 = unknown error or connection error
	// -999 = username already used
	// -998 = email already used
	public static int CreateNewUser(Context context, String username,
			String password, String firstName, String lastName, String email,
			String phone, String ePhone) {
		String url = "https://api.awayteam.redshrt.com/user/CreateUser";

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("loginId", username));
		pairs.add(new BasicNameValuePair("password", password));
		pairs.add(new BasicNameValuePair("firstName", firstName));
		pairs.add(new BasicNameValuePair("lastName", lastName));
		pairs.add(new BasicNameValuePair("email", email));
		pairs.add(new BasicNameValuePair("cellPhone", phone));
		if (ePhone == null) {
			pairs.add(new BasicNameValuePair("emergencyPhone", ""));
		} else {
			pairs.add(new BasicNameValuePair("emergencyPhone", ePhone));
		}

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		JSONObject result = null;

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("response").equals("success")) {
				// connection was good

				if (Integer.parseInt(result.getString("message")) > 0) {
					// User created and database ID returned
					// Authenticate user to store secret info from server

					int authSuccess = LoginUser(context, username, password,
							true);
					if (authSuccess == 1) {
						return 1;
					} else {
						return 0;
					}
				}
			} else if (!result.getString("message").isEmpty()) {
				// Return the error from the server - make sure these error
				// codes don't change because the UI will handle them as
				// expected in the original API
				return Integer.parseInt(result.getString("message"));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	// Resets the user's password
	// INPUT: entered username
	// RETURN: int code based on success:
	// 1 = success - user password was reset and an email sent to the user
	// -1 = username not found - make sure the user entered a name
	// 0 = some other error
	public static int ResetPassword(Context context, String username) {
		String url = "https://api.awayteam.redshrt.com/user/ResetPassword";
		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("loginId", username));
		JSONObject result = null;

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("response").equals("success")) {
				// succeeded
				return 1;
			} else if (!result.getString("message").isEmpty()) {
				switch (result.getString("message")) {
				case "user not found":
					return -1;
				default:
					return 0;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;

	}

	// Attempts to modify a user account
	// INPUT:
	// "loginId","email",
	// "firstName","lastName","cellPhone","emergencyPhone"
	// Returns int code based on success:
	// 1 = success! User created
	// 0 = unknown error or connection error
	// -999 = username already used
	// -998 = email already used
	public static int ModifyUser(Context context, String username,
			String firstName, String lastName, String email, String phone,
			String ePhone) {
		String url = "https://api.awayteam.redshrt.com/user/modifyuser";

		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", username));
		pairs.add(new BasicNameValuePair("firstName", firstName));
		pairs.add(new BasicNameValuePair("lastName", lastName));
		pairs.add(new BasicNameValuePair("email", email));
		pairs.add(new BasicNameValuePair("cellPhone", phone));
		if (ePhone == null) {
			pairs.add(new BasicNameValuePair("emergencyPhone", ""));
		} else {
			pairs.add(new BasicNameValuePair("emergencyPhone", ePhone));
		}

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		JSONObject result = null;

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("response").equals("true")) {
				// succeeded
				return 1;
			} else if (!result.getString("message").isEmpty()) {
				// Return the error from the server - make sure these error
				// codes don't change because the UI will handle them as
				// expected in the original API
				return Integer.parseInt(result.getString("message"));

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	// Attempts to authenticate user - for use when user session is already
	// stored
	// INPUT: context, username, password
	// Returns int code based on success:
	// 1 = success! User logged in
	// 0 = unknown error or connection error
	// -1 = username not found
	// -2 = password incorrect
	public static int AuthenticateUser(Context context, String username,
			String password) {
		String url = "https://api.awayteam.redshrt.com/user/AuthenticatePassword";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("loginId", username));
		pairs.add(new BasicNameValuePair("password", password));
		JSONObject result = null;

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("response").equals("success")) {
				// collect secret to store with session data
				String userID = result.get("userIdentifier").toString();
				String userSecret = result.getString("userSecret").toString();
				Log.v("login", "User Identifier from Server: " + userID);
				Log.v("login", "User Secret from Server: " + userSecret);

				// save values to user session
				UserSession.getInstance(context).setUp(userID, userSecret);

				return 1;
			} else if (result.getString("response").equals("failure")) {
				switch (result.getString("message")) {
				case "bad password":
					return -2;
				case "password not submitted":
					return -2;
				case "user not found":
					return -1;
				case "user not submitted":
					return -1;
				default:
					return 0;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	// Attempts to log in user
	// INPUT: context, username, password, and the value of "remember me"
	// Returns int code based on success:
	// 1 = success! User logged in
	// 0 = unknown error or connection error
	// -1 = username not found
	// -2 = password incorrect
	public static int LoginUser(Context context, String username,
			String password, boolean remember) {
		String url = "https://api.awayteam.redshrt.com/user/AuthenticatePassword";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("loginId", username));
		pairs.add(new BasicNameValuePair("password", password));
		JSONObject result = null;

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("response").equals("success")) {
				// collect secret to store with session data
				String userID = result.get("userIdentifier").toString();
				String userSecret = result.getString("userSecret").toString();
				Log.v("login", "User Identifier from Server: " + userID);
				Log.v("login", "User Secret from Server: " + userSecret);

				// save all values to user session
				UserSession.getInstance(context).setUp(username, password,
						userID, userSecret, remember);

				return 1;
			} else if (result.getString("response").equals("failure")) {
				switch (result.getString("message")) {
				case "bad password":
					return -2;
				case "password not submitted":
					return -2;
				case "user not found":
					return -1;
				case "user not submitted":
					return -1;
				default:
					return 0;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	// Changes the password of the selected user
	// INPUTS: username - user readable login ID
	// newPass - new password
	// RETURN: 1 = success, password updated
	// 0 = unknown error or connection failure
	// -1 = username not found
	// -2 = username missing
	public static int ChangePassword(String username, String newPass,
			Context context) {
		String url = "https://api.awayteam.redshrt.com/user/changepassword";
		List<NameValuePair> pairs = null;
		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		pairs = UserSession.getInstance(context).createHash();
		pairs.add(new BasicNameValuePair("loginId", username));
		pairs.add(new BasicNameValuePair("newPassword", newPass));
		JSONObject result = null;

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("response").equals("success")) {
				// Update user session password
				UserSession s = UserSession.getInstance(context);
				s.changePassword(newPass);
				// reauthenticate with new credentials
				int authSuccess = AuthenticateUser(context, s.getUsername(),
						s.getPassword());
				if (authSuccess == 1) {
					return 1;
				} else {
					return 0;
				}
			} else if (result.getString("response").equals("failure")) {
				switch (result.getString("message")) {
				case "user not found":
					return -1;
				case "user not submitted":
					return -2;
				default:
					return 0;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	// Update the user's current location
	// returns the success of the operation 1= success 0=error
	public static int UpdateUserLocation(Context context, String userName,
			double lat, double lon) {
		String url = "https://api.awayteam.redshrt.com/user/UpdateLocation";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		JSONObject result = null;
		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", userName));
		pairs.add(new BasicNameValuePair("lat", Double.toString(lat)));
		pairs.add(new BasicNameValuePair("lng", Double.toString(lon)));

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("response").equals("success")) {
				// success, report the good news!
				return 1;
			} else {
				// everything else is fail
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;

	}

	// Create a new team
	// INPUTS: team name, description, location name, lat and lon, managed
	// RETURN: 1 = success, team created
	// 0 = unknown error or connection failure
	// -1 = team name already used
	public static int CreateTeam(Context context, String teamName,
			String locationName, String description, boolean managed) {
		String url = "https://api.awayteam.redshrt.com/team/createteam";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", UserSession.getInstance(
				context).getUsername()));
		pairs.add(new BasicNameValuePair("teamName", teamName));
		pairs.add(new BasicNameValuePair("teamDescription", description));
		pairs.add(new BasicNameValuePair("teamLocationName", locationName));
		pairs.add(new BasicNameValuePair("teamManaged", Boolean
				.toString(managed)));

		JSONObject result = null;

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("status").equals("success")) {
				return result.getJSONObject("response").getInt("teamId");
			} else if (result.getString("status").equals("failure")) {
				switch (result.getString("response")) {
				case "team name is already used":
					return -1;
				default:
					return 0;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	// Edits a team
	// INPUTS: username, team id for editing, team name, description, location
	// name, lat and lon, managed
	// RETURN: 1 = success, team changed
	// 0 = unknown error or connection failure
	// -1 = user not part of the team
	public static int ModifyTeam(Context context, String userName, int teamID,
			String teamName, String locationName, String description) {
		String url = "https://api.awayteam.redshrt.com/team/modifyteam";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", userName));
		pairs.add(new BasicNameValuePair("teamId", Integer.toString(teamID)));
		pairs.add(new BasicNameValuePair("teamName", teamName));
		pairs.add(new BasicNameValuePair("teamDescription", description));
		pairs.add(new BasicNameValuePair("teamLocationName", locationName));
		pairs.add(new BasicNameValuePair("teamManaged", String.valueOf(true)));

		JSONObject result = null;

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("status").equals("success")) {
				return 1;
			} else if (result.getString("status").equals("failure")) {
				switch (result.getString("response")) {
				case "user not part of team":
					return -1;
				default:
					return 0;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	// Joins a team selected by the user
	// INPUTS: team id, username
	// RETURN: 1 = success! user added to team or added to pending for managed
	// teams
	// 0 = unknown error or connection failure
	// -1 = team does not exist
	// -2 = already a member of the team
	// -3 = tried to join a team where you are pending
	public static int JoinTeam(Context context, int teamID, String userName) {
		String url = "https://api.awayteam.redshrt.com/teammember/jointeam";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", UserSession.getInstance(
				context).getUsername()));
		pairs.add(new BasicNameValuePair("teamId", Integer.toString(teamID)));

		JSONObject result = null;

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("status").equals("success")) {
				return 1;
			} else if (result.getString("status").equals("failure")) {
				switch (result.getString("response")) {
				case "team id does not exist":
					return -1;
				case "team member already exists":
					return -2;
				case "team membership already pending":
					return -3;
				default:
					return 0;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	// Attempts to remove user from the team
	// INPUT:
	// RETURN: 1 = success, user is removed
	// -1 = user is the last member or manager of the team. Needs confirmation
	// 0 = some other error occurred
	public static int LeaveTeam(Context context, String username, int teamID,
			boolean confirmed) {
		String url = "https://api.awayteam.redshrt.com/teammember/leaveteam";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", username));
		pairs.add(new BasicNameValuePair("teamId", Integer.toString(teamID)));
		pairs.add(new BasicNameValuePair("confirmed", String.valueOf(confirmed)));
		JSONObject result = null;

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("status").equals("success")) {
				return 1;
			} else if (result.getString("status").equals("failure")) {
				switch (result.getString("response")) {
				case "team will be deleted":
					return -1;
				default:
					return 0;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	// Collect the list of all teams on the system
	// RETURN: list of all the teams formatted as: id,teamname,location,managed
	public static List<Object[]> GetAllTeamsList(Context context) {
		String url = "https://api.awayteam.redshrt.com/team/getallteams";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return null;
		}

		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();

		JSONObject result = null;

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("status").equals("success")) {
				// collect data and pass array to display list
				List<Object[]> teamList = new ArrayList<Object[]>();
				JSONArray response = result.getJSONArray("response");
				for (int i = 0; i < response.length(); i++) {
					int id = response.getJSONObject(i).getInt("teamId");
					String name = response.getJSONObject(i).getString(
							"teamName");
					String location = response.getJSONObject(i).getString(
							"teamLocationName");
					if (location.equals("null")) {
						location = "";
					}
					boolean managed = response.getJSONObject(i)
							.getString("teamManaged").equals("1"); // this is
																	// kind of a
																	// hack
					teamList.add(new Object[] { id, name, location, managed });
				}
				//sort the list alphabetically by name (the second entry/index 1)
				Collections.sort(teamList, new Comparator<Object[]>() {
					public int compare(Object[] a, Object[] b){
						return ((String)a[1]).compareTo(((String)b[1]));
					}
				});
				return teamList;
			} else if (result.getString("status").equals("failure")) {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// Get all the information about a team
	// This call will update the user session and build an Active Team
	// INPUTS: team name, description, location name, lat and lon, managed
	// RETURN: 1 = success, team created
	// 0 = unknown error or connection failure
	// -1 = error is a permission problem (user is not a member of the
	// team), where a different message should be given
	public static int GetTeam(Context context, int teamID, String userName) {
		String url = "https://api.awayteam.redshrt.com/team/getteam";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("teamId", String.valueOf(teamID)));
		pairs.add(new BasicNameValuePair("loginId", userName));

		JSONObject result = null;

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("status").equals("success")) {
				UserSession.getInstance(context).loadTeam(
						result.getJSONObject("response"));
				return 1;
			} else if (result.getString("status").equals("failure")) {
				if (result.getString("message").equals("Access Denied")) {
					// user should not access this team
					return -1;
				}
				// some other error occurred
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	// Collect the list of teams the user is a member of
	// RETURN: 1 if successful, 0 if error, -1 if there are no registered teams
	// ACTIONS: updates the teamList in the UserSession if successful
	public static int GetMemberTeamsList(Context context, String userName) {
		String url = "https://api.awayteam.redshrt.com/team/getteamlist";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", userName));

		JSONObject result = null;

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("status").equals("success")) {
				// collect data and pass array to UserSession
				List<Object[]> teamList = new ArrayList<Object[]>();
				JSONArray response = result.getJSONArray("response");
				for (int i = 0; i < response.length(); i++) {
					int id = response.getJSONObject(i).getInt("teamId");
					String name = response.getJSONObject(i).getString(
							"teamName");
					boolean pending = response.getJSONObject(i).getBoolean(
							"pendingApproval");

					teamList.add(new Object[] { id, name, pending });

				}
				UserSession.getInstance(context).teamList = teamList;
				return 1;
			} else if (result.getString("status").equals("failure")
					&& result.getString("response").equals(
							"user not part of any team")) {
				UserSession.getInstance(context).teamList = new ArrayList<Object[]>();
				return -1;
			} else if (result.getString("status").equals("failure")) {
				// only error is no teams available
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	// Collect the list of all pending users for given team
	// RETURN: list of all the teams formatted as: id,firstname,lastname,email
	public static List<Object[]> GetPendingMembers(Context context,
			String username, int teamID) {
		String url = "https://api.awayteam.redshrt.com/Manager/PendingUsers";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return null;
		}

		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", username));
		pairs.add(new BasicNameValuePair("teamId", Integer.toString(teamID)));

		JSONObject result = null;

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("response").equals("success")) {
				// collect data and pass array to display list
				List<Object[]> pendingList = new ArrayList<Object[]>();
				JSONArray response = result.getJSONArray("message");
				for (int i = 0; i < response.length(); i++) {
					String user = response.getJSONObject(i)
							.getString("loginId");
					String first = "";
					first = response.getJSONObject(i).getString("firstName");
					String last = "";
					last = response.getJSONObject(i).getString("lastName");
					String email = response.getJSONObject(i).getString("email");
					pendingList.add(new Object[] { user, first, last, email });
				}
				return pendingList;
			} else if (result.getString("response").equals("failure")) {
				return null;
			}
		} catch (Exception e) {
			Log.e("GetPending", e.toString());
		}
		return null;
	}

	// Provides Manager Actions
	// Input: actions include "approve"/"remove"/"promote"/"demote"
	// target user is the user being modified
	// Result: 1 = success
	// 0 = failure
	// -1 = unauthorized
	public static int ManagerAction(Context context, String userName,
			int teamID, String targetUserName, String action) {
		String url = "https://api.awayteam.redshrt.com/Manager/TakeAction";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		JSONObject result = null;
		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", userName));
		pairs.add(new BasicNameValuePair("teamId", Integer.toString(teamID)));
		pairs.add(new BasicNameValuePair("subjectLoginId", targetUserName));
		pairs.add(new BasicNameValuePair("action", action));

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("response").equals("success")) {
				// success, report the good news!
				return 1;
			} else if (result.getString("message").equals(
					"requesting user is not a manager")) {
				// everything else is fail
				return -1;
			} else {
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;

	}

	// Get all the information about a user
	// INPUTS: username
	// RETURN: a list of strings in order:
	// FirstName,LastName,Email,Phone,EmerPhone
	// null = error encountered
	public static List<String> GetUser(Context context, String userName) {
		String url = "https://api.awayteam.redshrt.com/user/getuser"
				+ "?loginId=" + userName;

		if (!NetworkTasks.NetworkAvailable(context)) {
			return null;
		}

		JSONObject result = null;
		List<String> user = new ArrayList<String>();

		try {
			result = NetworkTasks.RequestData(false, url, null);
			if (result.getString("response").equals("success")) {
				JSONObject message = result.getJSONObject("message");
				user.add(message.getString("firstName"));
				user.add(message.getString("lastName"));
				user.add(message.getString("email"));
				user.add(message.getString("cellPhone"));
				user.add(message.getString("emergencyPhone"));
				return user;
			} else if (result.getString("response").equals("failure")) {
				// an error occurred
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// Get Spots
	// input: required info for foursquare query
	// RETURN: JSONObject that contains all info
	public static JSONObject GetSpots(Context context, String userName,
			String searchMethod, String searchValue, String category,
			Integer limit, Integer radius, String query) {
		String url = "https://api.awayteam.redshrt.com/FQ/GetSpots";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return null;
		}

		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", userName));

		if (searchMethod.equalsIgnoreCase("ll")) {
			pairs.add(new BasicNameValuePair("radius", radius.toString()));
		}

		if (limit != null) {
			pairs.add(new BasicNameValuePair("limit", limit.toString()));
		}

		pairs.add(new BasicNameValuePair("category", category));
		pairs.add(new BasicNameValuePair("searchMethod", searchMethod));
		pairs.add(new BasicNameValuePair("searchValue", searchValue));

		pairs.add(new BasicNameValuePair("query", query));

		JSONObject result = null;

		try {
			result = NetworkTasks.RequestData(true, url, pairs);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	// Get expenses for the user
	// returns the success of the operation 1= success 0=error
	public static int GetExpenses(Context context, String userName, int teamID) {
		String url = "https://api.awayteam.redshrt.com/expense/getexpense";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		JSONObject result = null;
		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", userName));
		pairs.add(new BasicNameValuePair("teamId", Integer.toString(teamID)));

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getJSONArray("response").length() > 0) {
				// collect data and pass array to UserSession
				UserSession.getInstance(context).activeTeam
						.importExpenses(result.getJSONArray("response"));
				return 1;
			} else {
				// no error conditions specified
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;

	}

	// Create expenses for the user
	// note: remember category is +1 position of item from spinner input
	// returns the success of the operation 1= success 0=error
	public static int CreateExpense(Context context, String userName,
			int teamID, Date date, double amount, int category,
			String description) {
		String url = "https://api.awayteam.redshrt.com/expense/createexpense";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		JSONObject result = null;
		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", userName));
		pairs.add(new BasicNameValuePair("teamId", Integer.toString(teamID)));
		pairs.add(new BasicNameValuePair("description", description));
		pairs.add(new BasicNameValuePair("amount", Double.toString(amount)));
		pairs.add(new BasicNameValuePair("expType", Integer.toString(category)));
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		pairs.add(new BasicNameValuePair("expDate", formatter.format(date)));

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("response").equals("success")) {
				// success, report the good news!
				return result.getInt("message");
			} else {
				// everything else is fail
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;

	}

	// Edit expenses for the user
	// note: remember category is +1 position of item from spinner input
	// returns the success of the operation 1= success 0=error
	public static int EditExpense(Context context, String userName, int teamID,
			int expenseID, Date date, double amount, int category,
			String description) {
		String url = "https://api.awayteam.redshrt.com/expense/modifyexpense";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		JSONObject result = null;
		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", userName));
		pairs.add(new BasicNameValuePair("teamId", Integer.toString(teamID)));
		pairs.add(new BasicNameValuePair("expenseId", Integer
				.toString(expenseID)));
		pairs.add(new BasicNameValuePair("description", description));
		pairs.add(new BasicNameValuePair("amount", Double.toString(amount)));
		pairs.add(new BasicNameValuePair("expType", Integer.toString(category)));
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		pairs.add(new BasicNameValuePair("expDate", formatter.format(date)));

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("response").equals("success")) {
				// success, report the good news!
				return 1;
			} else {
				// everything else is fail
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;

	}

	// Delete Expense
	public static int DeleteExpense(Context context, String userName,
			int teamID, int expenseID) {
		String url = "https://api.awayteam.redshrt.com/expense/deleteexpense";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		JSONObject result = null;
		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", userName));
		pairs.add(new BasicNameValuePair("teamId", Integer.toString(teamID)));
		pairs.add(new BasicNameValuePair("expenseId", Integer
				.toString(expenseID)));
		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("response").equals("success")) {
				// success, report the good news!
				return 1;
			} else {
				// everything else is fail
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;

	}

	// Upload receipt image file to the given receipt
	// 1=success, 0=fail
	public static int UploadReceipt(Context context, String userName,
			int teamID, int expenseID, Uri image) {
		String url = "https://api.awayteam.redshrt.com/expense/putreceipt";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		JSONObject result = null;
		// get the file into the post and add the other parts as text
		try {
			NetworkTasks.doHTTPS();
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(url);
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

			// getting a file directly from storage doesn't work
			// final File file = new File(image.getPath());
			// FileBody fbody = new FileBody(file);

			InputStream inStream = context.getContentResolver()
					.openInputStream(image);
			// byte[] imageBytes = NetworkTasks.readBytes(inStream);
			// InputStreamBody inputStreamBody = new InputStreamBody(
			// new ByteArrayInputStream(imageBytes), image
			// .getLastPathSegment().toString());

			// TODO: try compression
			//InputStreamBody inputStreamBody = new InputStreamBody(inStream, "receipt.jpg");
			//ConnectivityManager connMgr = (ConnectivityManager) context
			//		.getSystemService(Context.CONNECTIVITY_SERVICE);
			//if (connMgr.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE) {
				// try compression
				Bitmap bmp = BitmapFactory.decodeStream(inStream);
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				bmp.compress(CompressFormat.JPEG, 50, outStream);

				InputStream in = new ByteArrayInputStream(
						outStream.toByteArray());
				InputStreamBody inputStreamBody = new InputStreamBody(in, "receipt.jpg");
			//}

			// InputStreamBody inputStreamBody = new InputStreamBody(inStream,
			// ContentType.DEFAULT_BINARY);

			// builder.addPart("file", fbody);
			builder.addPart("file", inputStreamBody);
			builder.addTextBody("loginId", userName);
			builder.addTextBody("teamId", Integer.toString(teamID));
			builder.addTextBody("expenseId", Integer.toString(expenseID));
			List<NameValuePair> pairs = UserSession.getInstance(context)
					.createHash();
			builder.addTextBody(pairs.get(0).getName(), pairs.get(0).getValue());
			builder.addTextBody(pairs.get(1).getName(), pairs.get(1).getValue());

			Log.v("Comm", url);
			final HttpEntity postEntity = builder.build();
			post.setEntity(postEntity);
			HttpResponse response = client.execute(post);

			// collect reply
			StringBuilder replyBuilder = new StringBuilder();
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200 || statusCode == 401) {
				// FYI 200 is good - auth passed
				// 401 is bad - auth failed
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					replyBuilder.append(line);
				}
				Log.v("ReceiptTask",
						"Returned Message: " + replyBuilder.toString()); // response
				// data
			} else {
				Log.e("NetTask", "Failed to download file");
			}

			result = new JSONObject(replyBuilder.toString());
			if (result.getString("response").equals("success")) {
				// success, report the good news!
				return 1;
			} else {
				// everything else is fail
				return 0;
			}

		} catch (Exception e) {
			Log.e("Receipt", e.toString());
			e.printStackTrace();
			return 0;
		}

	}

	// Create event for the user
	// returns the success of the operation 1= success 0=error
	public static int CreateEvent(Context context, String userName, int teamID,
			Date startTime, Date endTime, String title, String location,
			String description) {
		String url = "https://api.awayteam.redshrt.com/teamevent/createevent";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		JSONObject result = null;
		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", userName));
		pairs.add(new BasicNameValuePair("teamEventTeamId", Integer
				.toString(teamID)));
		pairs.add(new BasicNameValuePair("teamEventName", title));
		pairs.add(new BasicNameValuePair("teamEventLocationString", location));
		pairs.add(new BasicNameValuePair("teamEventDescription", description));
		DateFormat formatter = new SimpleDateFormat("yyyy-M-d HH:mm");
		pairs.add(new BasicNameValuePair("teamEventStartTime", formatter
				.format(startTime)));
		Log.v("EventComm", "start: " + formatter.format(startTime));
		pairs.add(new BasicNameValuePair("teamEventEndTime", formatter
				.format(endTime)));

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("status").equals("success")) {
				// success, report the good news!
				return 1;
			} else {
				// everything else is fail
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;

	}

	// Edit event for the user
	// returns the success of the operation 1= success 0=error
	public static int EditEvent(Context context, String userName, int teamID,
			int eventID, Date startTime, Date endTime, String title,
			String location, String description) {
		String url = "https://api.awayteam.redshrt.com/teamevent/editevent";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		JSONObject result = null;
		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", userName));
		pairs.add(new BasicNameValuePair("teamEventTeamId", Integer
				.toString(teamID)));
		pairs.add(new BasicNameValuePair("teamEventId", Integer
				.toString(eventID)));
		pairs.add(new BasicNameValuePair("teamEventName", title));
		pairs.add(new BasicNameValuePair("teamEventLocationString", location));
		pairs.add(new BasicNameValuePair("teamEventDescription", description));
		DateFormat formatter = new SimpleDateFormat("yyyy-M-d HH:mm");
		pairs.add(new BasicNameValuePair("teamEventStartTime", formatter
				.format(startTime)));
		Log.v("EventComm", "start: " + formatter.format(startTime));
		pairs.add(new BasicNameValuePair("teamEventEndTime", formatter
				.format(endTime)));

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("status").equals("success")) {
				// success, report the good news!
				return 1;
			} else {
				// everything else is fail
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;

	}

	// Delete an event from a team
	// returns the success of the operation 1= success 0=error
	public static int DeleteEvent(Context context, String userName, int teamID,
			int eventID) {
		String url = "https://api.awayteam.redshrt.com/teamevent/deleteevent";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		JSONObject result = null;
		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", userName));
		pairs.add(new BasicNameValuePair("teamEventTeamId", Integer
				.toString(teamID)));
		pairs.add(new BasicNameValuePair("teamEventId", Integer
				.toString(eventID)));

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("status").equals("success")) {
				// success, report the good news!
				return 1;
			} else {
				// everything else is fail
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;

	}

	// Create task for the user
	// returns the success of the operation 1= success 0=error
	public static int CreateTask(Context context, String userName, int teamID,
			String title, String description) {
		String url = "https://api.awayteam.redshrt.com/teamtasks/createtask";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		JSONObject result = null;
		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", userName));
		pairs.add(new BasicNameValuePair("taskTeamId", Integer.toString(teamID)));
		pairs.add(new BasicNameValuePair("taskDescription", description));
		pairs.add(new BasicNameValuePair("taskTitle", title));

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("status").equals("success")) {
				// success, report the good news!
				return 1;
			} else {
				// everything else is fail
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;

	}

	// Updates the state of the task, either checking it or deleting it
	// returns success of case 1= yay! 0 = booo!
	public static int UpdateTask(Context context, String userName, int teamID,
			int taskID, boolean checked, boolean deleted) {
		String url = "https://api.awayteam.redshrt.com/teamtasks/updatetask";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		JSONObject result = null;
		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", userName));
		pairs.add(new BasicNameValuePair("taskTeamId", Integer.toString(teamID)));
		pairs.add(new BasicNameValuePair("taskId", Integer.toString(taskID)));
		pairs.add(new BasicNameValuePair("taskCompleted", String
				.valueOf(checked)));
		pairs.add(new BasicNameValuePair("taskDeletion", String
				.valueOf(deleted)));

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("status").equals("success")) {
				// success, report the good news!
				return 1;
			} else {
				// everything else is fail
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	// Update the text values of a task
	public static int ModifyTask(Context context, String userName, int teamID,
			int taskID, String title, String description) {
		String url = "https://api.awayteam.redshrt.com/teamtasks/edittask";

		if (!NetworkTasks.NetworkAvailable(context)) {
			return 0;
		}

		JSONObject result = null;
		List<NameValuePair> pairs = UserSession.getInstance(context)
				.createHash();
		pairs.add(new BasicNameValuePair("loginId", userName));
		pairs.add(new BasicNameValuePair("taskTeamId", Integer.toString(teamID)));
		pairs.add(new BasicNameValuePair("taskId", Integer.toString(taskID)));
		pairs.add(new BasicNameValuePair("taskDescription", description));
		pairs.add(new BasicNameValuePair("taskTitle", title));

		try {
			result = NetworkTasks.RequestData(true, url, pairs);
			if (result.getString("status").equals("success")) {
				// success, report the good news!
				return 1;
			} else {
				// everything else is fail
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

}
