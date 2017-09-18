package com.nu.art.cyborg.maps.google;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.location.Location;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cyborg.googlemapsmodule.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.nu.art.core.generics.Processor;
import com.nu.art.cyborg.core.CyborgController;
import com.nu.art.cyborg.modules.LocationModule.OnLocationUpdatedListener;

import java.util.ArrayList;
import java.util.Random;

public class Controller_GoogleMap
		extends CyborgController
		implements OnLocationUpdatedListener, OnMapReadyCallback, OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener, OnInfoWindowClickListener {

	private MapFragment mapFragment;

	private GoogleMap mGoogleMap;

	private Marker myMarker;

	private ArrayList<Marker> allNonStickyMarker = new ArrayList<>();

	private PolylineOptions rectLine;

	private Polyline polyline;

	/**
	 * Range is between 2 to 21
	 */
	private int cameraZoom = 16;

	private final String USER_POSITION_INFO = "You're here";

	private final String TARGET_LOCATION_INFO = "Remove marker";

	private final float DEFAULT_TARGET_MARKER_COLOR = BitmapDescriptorFactory.HUE_GREEN;

	private final float DEFAULT_USER_MARKER = BitmapDescriptorFactory.HUE_AZURE;

	private int mapLayoutId;

	public Controller_GoogleMap() {
		super(R.layout.controller__google_maps_fragment);
	}

	@Override
	protected void onCreate() {
		super.onCreate();

		rectLine = new PolylineOptions().width(3).color(Color.RED);
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setMapFragmentId();
	}

	private void setMapFragmentId() {
		if (mGoogleMap != null) {
			removeAllMarkers();
		}

		Random random = new Random();
		while (getActivity().findViewById(mapLayoutId = Math.abs(random.nextInt())) != null)
			;

		FrameLayout fl = new FrameLayout(getActivity());

		((ViewGroup) getRootView()).addView(fl);
		fl.setId(mapLayoutId);
		getActivity().findViewById(fl.getId());
	}

	@Override
	protected void onResume() {
		super.onResume();
		addMapFragmentToViewHierarchy();
	}

	private void addMapFragmentToViewHierarchy() {
		if (mapFragment != null)
			return;

		mapFragment = MapFragment.newInstance();
		final FragmentManager fm = getActivity().getFragmentManager();

		if (getActivity().findViewById(mapLayoutId) == null)
			return;

		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(mapLayoutId, mapFragment, "map-fragment-" + mapLayoutId);
		ft.commit();
		mapFragment.getMapAsync(Controller_GoogleMap.this);
	}

	/**
	 * Removes all Markers from the map and list and then updates the map view
	 */
	public void removeAllMarkers() {
		while (!allNonStickyMarker.isEmpty()) {
			allNonStickyMarker.remove(0).remove();
		}
		updateRoute();
	}

	/**
	 * Removes the given <b>list Marker</b> from both the map and list and then updates the map view
	 */
	public void removeMarker(Marker marker) {
		if (allNonStickyMarker.remove(marker)) {
			marker.remove();
		}

		updateRoute();
	}

	private void updateRoute() {
		rectLine = new PolylineOptions().width(5).color(Color.RED);
		for (int i = 0; i < allNonStickyMarker.size(); i++) {
			Marker marker = allNonStickyMarker.get(i);
			rectLine.add(marker.getPosition());
			addMarker(marker.getPosition(), DEFAULT_TARGET_MARKER_COLOR, TARGET_LOCATION_INFO);
		}

		updateRoutesOnMap();
	}

	@Override
	public void onLocationUpdated(Location location) {
		LatLng myPosition = new LatLng(location.getLatitude(), location.getLongitude());
		setCameraLocation(myPosition);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mGoogleMap = googleMap;
		googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mGoogleMap.setOnMapClickListener(this);
		mGoogleMap.setOnMapLongClickListener(this);
		mGoogleMap.setOnMarkerClickListener(this);
		mGoogleMap.setOnInfoWindowClickListener(this);
		updateRoute();
		dispatchEvent("Map event MAP_READY was called.", MyOnMapReadyCallback.class, new Processor<MyOnMapReadyCallback>() {
			@Override
			public void process(MyOnMapReadyCallback listener) {
				listener.onMapReady(getRootView().getId());
			}
		});
	}

	public void setCameraZoom(int cameraZoom) {
		this.cameraZoom = cameraZoom;
	}

	public void setCameraLocation(LatLng position) {
		if (position == null)
			return;

		CameraUpdate newLocation = CameraUpdateFactory.newLatLngZoom(position, cameraZoom);
		mGoogleMap.moveCamera(newLocation);
		addStickyMarker(position);
	}

	public void addStickyMarker(LatLng position) {
		if (myMarker != null)
			myMarker.remove();

		myMarker = addMarker(position, DEFAULT_USER_MARKER, USER_POSITION_INFO);
	}

	public void addTargetMarker(LatLng position) {
		Marker marker = addMarker(position, DEFAULT_TARGET_MARKER_COLOR, TARGET_LOCATION_INFO);
		allNonStickyMarker.add(marker);
		addMarkerToRoute(marker);
	}

	private Marker addMarker(LatLng position, float color, String title) {
		MarkerOptions markerOptions = new MarkerOptions().position(position).title(title).icon(BitmapDescriptorFactory.defaultMarker(color));
		return mGoogleMap.addMarker(markerOptions);
	}

	private void addMarkerToRoute(Marker marker) {
		rectLine.add(marker.getPosition());

		updateRoutesOnMap();
	}

	private void updateRoutesOnMap() {
		if (polyline != null)
			polyline.remove();
		polyline = mGoogleMap.addPolyline(rectLine);
	}

	@Override
	public void onMapClick(final LatLng latLng) {
		dispatchEvent("Map event MAP_CLICK was called.", OnMapClicks.class, new Processor<OnMapClicks>() {
			@Override
			public void process(OnMapClicks listener) {
				listener.onMapClick(getRootView().getId(), latLng);
			}
		});
	}

	@Override
	public void onMapLongClick(final LatLng latLng) {
		dispatchEvent("Map event MAP_LONG_CLICK was called.", OnMapClicks.class, new Processor<OnMapClicks>() {
			@Override
			public void process(OnMapClicks listener) {
				listener.onMapLongClick(getRootView().getId(), latLng);
			}
		});
	}

	@Override
	public boolean onMarkerClick(final Marker marker) {
		dispatchEvent("Map event MAP_MARKER_CLICK was called.", OnMapClicks.class, new Processor<OnMapClicks>() {
			@Override
			public void process(OnMapClicks listener) {
				listener.onMarkerClick(getRootView().getId(), marker);
			}
		});
		return true;
	}

	@Override
	public void onInfoWindowClick(final Marker marker) {
		dispatchEvent("Map event MAP_INFO_WINDOW_CLICK was called.", OnMapClicks.class, new Processor<OnMapClicks>() {
			@Override
			public void process(OnMapClicks listener) {
				listener.onInfoWindowClick(getRootView().getId(), marker);
			}
		});
	}
}
