package edu.psu.team3.app.awayteam;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

@SuppressLint("DefaultLocale")
public final class UserSession {
	// strings for saving and recalling shared prefs
	private static final String PREFS = "sessionpref";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String REMEMBER = "remember";
	private static final String TEAMID = "teamid";

	private static volatile UserSession INSTANCE = new UserSession();

	private String username = null; // user's username entered for login
	private String password = null; // user's password entered for login
	private String loginID = null; // user identification received from server
	private String loginSecret = null; // secret for hash from server
	private boolean rememberLogin = false; // true if we should remember the
											// user credentials and
											// automatically login again when
											// the app is restarted
	private static Context mContext = null;// holds context for accessing prefs
											// and other functions

	public List<Object[]> teamList = new ArrayList<Object[]>();// list of
																// available
																// teams in
																// format
																// [id,name]
	public Team activeTeam = null;// current team being viewed
	public int currentTeamID = -1; // current selection in the team list (-1
									// indicates no team selected)

	// empty constructor to ensure only one copy of the class exists
	// - all variables will be filled in after successful authentication
	private UserSession() {

	}

	public static UserSession getInstance(Context context) {
		if (mContext == null && context != null) {
			mContext = context;
		}
		return INSTANCE;
	}

	// After the user successfully logs in, provide the following information to
	// the user session so that it can be used to track and authenticate the
	// user
	// INPUTS: userLoginName: the login name the user typed into the form
	// userPassword: the password the user entered to login
	// userLoginID: identification provided by the server
	// userSecret: authentication secret provided by the server
	// rememberMe: TRUE if the user has selected the option to remain logged in
	public void setUp(String userLoginName, String userPassword,
			String userLoginID, String userSecret, boolean rememberMe) {
		username = userLoginName;
		password = userPassword;
		loginID = userLoginID;
		loginSecret = userSecret;
		rememberLogin = rememberMe;

		if (rememberMe && mContext != null) {
			Log.v("Session", "Saving preferences");
			SharedPreferences.Editor prefs = mContext.getSharedPreferences(
					PREFS, Context.MODE_PRIVATE).edit();
			prefs.putString(USERNAME, username);
			prefs.putString(PASSWORD, password);
			prefs.putBoolean(REMEMBER, rememberLogin);
			prefs.putInt(TEAMID, currentTeamID);
			prefs.commit();
		}
	}

	// Shorter version of setUp for use when the user has saved their
	// credentials with the app
	// only the authentication user identifier and session secret from the
	// server need to be passed again to update for this session
	// all other data is already stored in the shared prefs
	public void setUp(String userLoginID, String userSecret) {
		loginID = userLoginID;
		loginSecret = userSecret;
	}

	// Collect an array of team names
	public List<String> getTeamListNames() {
		ArrayList<String> result = new ArrayList<String>();
		for (Object[] row : teamList) {
			result.add(row[1].toString());
		}

		return result;
	}

	// find the location of a team based on teamID
	public int getTeamListPosition(int teamID) {
		for (int i = 0; i < teamList.size(); i++) {
			if ((int) teamList.get(i)[0] == teamID) {
				return i;
			}
		}
		return 0;

	}

	// change the password
	public void changePassword(String newPass) {
		password = newPass;
		if (rememberLogin && mContext != null) {
			SharedPreferences.Editor prefs = mContext.getSharedPreferences(
					PREFS, Context.MODE_PRIVATE).edit();
			prefs.putString(USERNAME, username);
			prefs.putString(PASSWORD, password);
			prefs.putBoolean(REMEMBER, rememberLogin);
			prefs.commit();
		}
	}

	//

	// check if the user is remembered by the app
	public boolean remembered() {
		// check if the user session has been initialized yet - if not, this is
		// before login
		if (mContext != null) {
			SharedPreferences prefs = mContext.getSharedPreferences(PREFS,
					Context.MODE_PRIVATE);
			rememberLogin = prefs.getBoolean(REMEMBER, false);
			if (rememberLogin) {
				username = prefs.getString(USERNAME, null);
				password = prefs.getString(PASSWORD, null);
				currentTeamID = prefs.getInt(TEAMID, -1);
			}
		}
		Log.v("Session", "Remembered user session? " + rememberLogin);
		return rememberLogin;
	}

	// get the username for the purpose of populating login
	public String getUsername() {
		return username;
	}

	// get the password for the purpose of populating login
	public String getPassword() {
		return password;
	}

	// update the value of the current team and save for next startup
	public void updateCurrentTeam(int id) {
		currentTeamID = id;
		SharedPreferences.Editor prefs = mContext.getSharedPreferences(PREFS,
				Context.MODE_PRIVATE).edit();
		prefs.putInt(TEAMID, id);
		prefs.commit();
	}

	// End the session and delete all stored data on the user
	// call this when the user selects "logout"
	public void terminateSession() {
		if (mContext != null) {
			SharedPreferences.Editor prefs = mContext.getSharedPreferences(
					PREFS, Context.MODE_PRIVATE).edit();
			prefs.clear();
			prefs.commit();
		}
		activeTeam = null;
	}

	// Create a new team object based on JSON Object (formatted according to
	// API)
	public void loadTeam(JSONObject message) {
		//create an empty team, clearing old data
		activeTeam = new Team();
		//collect basic team information
		try {
			activeTeam.importTeam(message);
		} catch (Exception e) {
			e.printStackTrace();
			activeTeam = null;
		}
	}

	// Create a hash to authenticate a session
	public List<NameValuePair> createHash() {
		byte[] hash = null;
		List<NameValuePair> secret = new ArrayList<NameValuePair>();
		long mstime = System.currentTimeMillis();
		long seconds = mstime / 1000;
		String time = String.valueOf(seconds);
		String token = time + username.toLowerCase() + loginID;// combine the
																// parts of the
		// token

		// hash function
		try {
			SecretKeySpec secretKey = new SecretKeySpec(loginSecret.getBytes(),
					"HmacSHA256");
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(secretKey);
			hash = mac.doFinal(token.getBytes());
			secret.add(new BasicNameValuePair("AWT_AUTH", loginID));
			secret.add(new BasicNameValuePair("AWT_AUTH_CHALLENGE",
					hexify(hash)));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return secret;
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	private static String hexify(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}

		return new String(hexChars).toLowerCase();
	}

}
