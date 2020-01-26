package edu.psu.team3.app.awayteam;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MapFragment extends Fragment implements OnInfoWindowClickListener {
	private FourSquareTask fqT = null;
	private FourSquareTask foodTask = null;
	private FourSquareTask travelTask = null;
	private FourSquareTask shopTask = null;
	private FourSquareTask eventTask = null;

	public enum CategoryID {
		TEAM, FOOD, TRAVEL, SHOP, EVENT
	}

	private LatLng currentLocation;
	private MapView mapView;
	private GoogleMap map;

	// UI Buttons
	ToggleButton teamButton;
	ToggleButton foodButton;
	ToggleButton hotelButton;
	ToggleButton travelButton;
	ToggleButton shopButton;
	ToggleButton eventsButton;

	// collections of markers formatted [categoryID,Marker,URL]
	List<Object[]> markerList = new ArrayList<Object[]>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		try {
			View rootView = inflater.inflate(R.layout.fragment_map, container,
					false);
			mapView = (MapView) rootView.findViewById(R.id.mapview);
			mapView.onCreate(savedInstanceState);

			// Gets to GoogleMap from the MapView and does initialization stuff
			map = mapView.getMap();
			map.getUiSettings().setMyLocationButtonEnabled(true);
			map.setMyLocationEnabled(true);

			MapsInitializer.initialize(this.getActivity());

			// Updates the location and zoom of the MapView
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
					new LatLng(0, 0), 10);
			map.animateCamera(cameraUpdate);

			return rootView;
		} catch (Exception e) {
			Log.e("MAP", "error creating map view: " + e.toString());
			e.printStackTrace();
			return null;
		}

	}

	public void onStart() {
		super.onStart();
		// Get a handle to the Map Fragment
		try {
			map = mapView.getMap();

			// get current location
			UserSession s = UserSession.getInstance(getActivity());
			LocationManager mgr = (LocationManager) getActivity()
					.getSystemService(Context.LOCATION_SERVICE);
			Location lastKnownLocation = mgr
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			try{
			if (lastKnownLocation != null) {
				// I have more current location - use that
				currentLocation = new LatLng(lastKnownLocation.getLatitude(),
						lastKnownLocation.getLongitude());
				// TODO: catch this all
				s.activeTeam.getUser(s.getUsername()).lat = currentLocation.latitude;
				s.activeTeam.getUser(s.getUsername()).lon = currentLocation.longitude;
			} else {
				// use stored user location - may be 0,0
				currentLocation = new LatLng(s.activeTeam.getUser(s
						.getUsername()).lat, s.activeTeam.getUser(s
						.getUsername()).lon);
			}

			// initialize map to user location
			map.setMyLocationEnabled(true);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,
					10));
			map.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
			map.setOnInfoWindowClickListener(this);}catch(Exception ex){
				currentLocation = null;
				Log.e("MAP","unable to collect user location yet. "+ex.toString());
				ex.printStackTrace();
			}

			// setup map layer buttons
			teamButton = (ToggleButton) getView().findViewById(
					R.id.mapTeamLayer);
			teamButton
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								// show map items
								plotTeamLocations();
							} else {
								// remove map items
								removeMarkerCategory(CategoryID.TEAM);
							}

						}
					});
			map.setOnCameraChangeListener(new OnCameraChangeListener() {

				@Override
				public void onCameraChange(CameraPosition arg0) {
					// see if team needs to be drawn
					if (teamButton.isChecked()) {
						plotTeamLocations();
					}
					map.setOnCameraChangeListener(null);
				}
			});

			// teamButton.setChecked(false); // Since a background task is
			// updating
			// team info, it may not be available.
			// don't try to get it
			foodButton = (ToggleButton) getView().findViewById(
					R.id.mapFoodLayer);
			foodButton
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								// show map items
								if (foodTask == null) {
									foodTask = new FourSquareTask();
									foodTask.execute(CategoryID.FOOD, "food");
									buttonView.setEnabled(false);
								} else {
									buttonView.setChecked(false);
								}
							} else {
								// remove map items
								removeMarkerCategory(CategoryID.FOOD);
							}

						}
					});
			if (foodButton.isChecked()) {
				if (foodTask == null) {
					foodTask = new FourSquareTask();
					foodTask.execute(CategoryID.FOOD, "food");
					foodButton.setEnabled(false);
				} else {
					foodButton.setChecked(false);
				}
			}
			travelButton = (ToggleButton) getView().findViewById(
					R.id.mapTravelLayer);
			travelButton
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								// show map items
								if (travelTask == null) {
									travelTask = new FourSquareTask();
									travelTask.execute(CategoryID.TRAVEL,
											"travTrans");
									buttonView.setEnabled(false);
								} else {
									buttonView.setChecked(false);
								}
							} else {
								// remove map items
								removeMarkerCategory(CategoryID.TRAVEL);
							}

						}
					});
			if (travelButton.isChecked()) {
				if (travelTask == null) {

					travelTask = new FourSquareTask();
					travelTask.execute(CategoryID.TRAVEL, "travTrans");
					travelButton.setEnabled(false);
				} else {
					travelButton.setChecked(false);
				}
			}
			shopButton = (ToggleButton) getView().findViewById(
					R.id.mapShopLayer);
			shopButton
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								// show map items
								if (shopTask == null) {
									shopTask = new FourSquareTask();
									shopTask.execute(CategoryID.SHOP,
											"shopServ");
									buttonView.setEnabled(false);
								} else {
									buttonView.setChecked(false);
								}
							} else {
								// remove map items
								removeMarkerCategory(CategoryID.SHOP);
							}

						}
					});
			if (shopButton.isChecked()) {
				if (shopTask == null) {
					shopTask = new FourSquareTask();
					shopTask.execute(CategoryID.SHOP, "shopServ");
					shopButton.setEnabled(false);
				} else {
					shopButton.setChecked(false);
				}
			}
			eventsButton = (ToggleButton) getView().findViewById(
					R.id.mapEventsLayer);
			eventsButton
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked) {
								// show map items
								if (eventTask == null) {
									eventTask = new FourSquareTask();
									eventTask.execute(CategoryID.EVENT,
											"artsEnt");
									buttonView.setEnabled(false);
								} else {
									buttonView.setChecked(false);
								}
							} else {
								// remove map items
								removeMarkerCategory(CategoryID.EVENT);
							}

						}
					});
			if (eventsButton.isChecked()) {
				if (eventTask == null) {
					eventTask = new FourSquareTask();
					eventTask.execute(CategoryID.EVENT, "artsEnt");
					eventsButton.setEnabled(false);
				} else {
					eventsButton.setChecked(false);
				}
			}

		} catch (Exception e) {
			Log.e("MAP", "Error initializing map fragment: " + e.toString());
			e.printStackTrace();
		}

	}

	// removes all markers of this category from the list and the map display
	// uses recursion to prevent overrun resulting from deleting resources being
	// traversed
	private void removeMarkerCategory(CategoryID category) {
		for (Object[] place : markerList) {
			if (place[0].equals(category)) {
				((Marker) place[1]).remove();
				markerList.remove(place);
				removeMarkerCategory(category);
				return;
			}
		}
	}

	// show users with location info on map
	private void plotTeamLocations() {
		LatLngBounds.Builder builder = new LatLngBounds.Builder();

		for (TeamMember member : UserSession.getInstance(getActivity()).activeTeam.teamMembers) {
			if (member.lat != 0 && member.lon != 0) {
				MarkerOptions markerOp = new MarkerOptions();
				markerOp.title(member.firstName + " " + member.lastName);
				markerOp.position(new LatLng(member.lat, member.lon));
				markerOp.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_action_person));

				Marker marker = map.addMarker(markerOp);
				builder.include(marker.getPosition());
				markerList.add(new Object[] { CategoryID.TEAM, marker,
						member.userName });

			}
		}

		CameraUpdate camUpdate = CameraUpdateFactory.newLatLngBounds(
				builder.build(), 150);
		map.animateCamera(camUpdate);

	}

	// Lifecycle calls for the mapview
	@Override
	public void onResume() {
		mapView.onResume();
		super.onResume();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}

	// Makes a query using the foursquare api
	// input: category ID used to manage groups by other tasks, category text
	// (optional),query text (optional)
	// action: populates marker list with new markers, displays markers on map
	public class FourSquareTask extends AsyncTask<Object, Void, JSONObject> {
		CategoryID categoryID;

		@Override
		protected JSONObject doInBackground(Object... params) {
			categoryID = (CategoryID) params[0];
			String category = "";
			String query = "";
			if (params.length > 1) {
				category = (String) params[1];
			}
			if (params.length > 2) {
				query = (String) params[2];
			}

			LocationManager mgr = (LocationManager) getActivity()
					.getSystemService(Context.LOCATION_SERVICE);
			Location lastKnownLocation = mgr
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (lastKnownLocation == null) {
				// no location found - report to user
				return null;
			}
			;

			currentLocation = new LatLng(lastKnownLocation.getLatitude(),
					lastKnownLocation.getLongitude());

			JSONObject result;
			String ll = currentLocation.latitude + ", "
					+ currentLocation.longitude;
			UserSession s = UserSession.getInstance(getActivity());
			String user = s.getUsername();

			result = CommUtil.GetSpots(getActivity(), user, "ll", ll, category,
					10, 20, query);
			return result;
		}

		@Override
		protected void onPostExecute(final JSONObject result) {
			JSONArray resultArray = null;
			LatLngBounds.Builder builder = new LatLngBounds.Builder();

			try {
				resultArray = result.getJSONArray("response");
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(getActivity(),
						"Unable to return results. Ensure Location is ON",
						Toast.LENGTH_SHORT).show();
				return;
			}
			for (int i = 0; i < resultArray.length(); i++) {

				String name = null;
				try {
					name = resultArray.getJSONObject(i).getString("name");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				String placeUrl = null;
				try {
					placeUrl = resultArray.getJSONObject(i).getString("url");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				String phone = null;
				try {
					phone = resultArray.getJSONObject(i).getString("phone");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				String placeCat = null;
				try {
					placeCat = resultArray.getJSONObject(i).getString(
							"category");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				String menu = null;
				try {
					menu = resultArray.getJSONObject(i).getString("menu");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				JSONObject loc = null;
				String address = null;
				try {
					loc = resultArray.getJSONObject(i)
							.getJSONObject("location");
					try {
						address = loc.getString("address");
					} catch (JSONException e) {
						e.printStackTrace();
					}
					Double lat = null;
					try {
						lat = Double.parseDouble(loc.getString("lat"));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					Double lng = null;
					try {
						lng = Double.parseDouble(loc.getString("lng"));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					LatLng place = new LatLng(lat, lng);

					MarkerOptions markerOp = new MarkerOptions();
					markerOp.title(name);
					String snippet = "";
					if (placeCat != "null" && placeCat != null) {
						snippet = snippet.concat(placeCat);
					}
					if (phone != "null" && phone != null) {
						snippet = snippet.concat("  |  Phone: " + phone);
					}
					markerOp.position(place);

					switch (categoryID) {
					case FOOD:
						markerOp.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
						foodTask = null;
						foodButton.setEnabled(true);
						break;
					case TRAVEL:
						markerOp.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
						travelTask = null;
						travelButton.setEnabled(true);
						break;
					case SHOP:
						markerOp.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
						shopTask = null;
						shopButton.setEnabled(true);
						break;
					case EVENT:
						markerOp.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
						eventTask = null;
						eventsButton.setEnabled(true);
					default:
						markerOp.icon(BitmapDescriptorFactory.defaultMarker());
						fqT = null;
						break;

					}

					String uri = null;
					if (menu != null && menu != "null") {
						uri = menu;
						snippet = snippet.concat("  (get menu)");
					} else if (placeUrl != null && placeUrl != "null") {
						uri = placeUrl;
					}
					markerOp.snippet(snippet);
					// add point to map
					Marker marker = map.addMarker(markerOp);
					builder.include(marker.getPosition());
					markerList.add(new Object[] { categoryID, marker, uri });

				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			CameraUpdate camUpdate = CameraUpdateFactory.newLatLngBounds(
					builder.build(), 150);
			map.animateCamera(camUpdate);

		}

		@Override
		protected void onCancelled() {
			fqT = null;
		}
	}

	// Handle clicks on Markers!
	@Override
	public void onInfoWindowClick(Marker marker) {
		Object[] selected = null;
		for (Object[] place : markerList) {
			if (marker.equals(place[1])) {
				selected = place;
				break;
			}
		}
		if (selected != null && selected[0] != CategoryID.TEAM) {
			// foursquare item picked, create an intent
			if (selected[2] != null) {
				Intent fsIntent = new Intent();
				fsIntent.setData(Uri.parse((String) selected[2]));
				fsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(fsIntent);
			} else {
				Toast.makeText(getActivity(),
						"No Additional Info for this location",
						Toast.LENGTH_SHORT).show();
			}
		}
		if (selected != null && selected[0] == CategoryID.TEAM) {
			// open team member detail (like a boss)
			DialogFragment newFragment = new MemberDetailDialog();
			Bundle args = new Bundle();
			args.putString("userName", (String) selected[2]);
			newFragment.setArguments(args);
			newFragment.show(getFragmentManager(), null);

		}

	}

}
