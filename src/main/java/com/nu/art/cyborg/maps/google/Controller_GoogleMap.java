/*
 * The google-maps module, is an implementation of Google Maps SDK
 * for Android, allowing you to use the power of Cyborg, with Google Maps.
 *
 * Copyright (C) 2017  Adam van der Kruk aka TacB0sS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

	public static class MapMarker {

		public LatLng position;

		private float color;

		private String title;

		private Marker marker;

		public MapMarker(LatLng position, float color, String title) {
			this.position = position;
			this.color = color;
			this.title = title;
		}
	}

	private MapFragment mapFragment;

	private GoogleMap mGoogleMap;

	private Marker myMarker;

	private ArrayList<MapMarker> markers = new ArrayList<>();

	//	private PolylineOptions rectLine;

	private Polyline polyline;

	/**
	 * Range is between 2 to 21
	 */
	private int cameraZoom = 16;

	private int mapLayoutId;

	public Controller_GoogleMap() {
		super(R.layout.controller__google_maps_fragment);
	}

	@Override
	protected void onCreate() {
		super.onCreate();
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setMapFragmentId();
	}

	private void setMapFragmentId() {
		Random random = new Random();
		while (getActivity().findViewById(mapLayoutId = Math.abs(random.nextInt())) != null)
			;

		FrameLayout fl = new FrameLayout(getActivity());

		((ViewGroup) getRootView()).addView(fl);
		fl.setId(mapLayoutId);
		getActivity().findViewById(fl.getId());
	}

	private void removeAllMarkers() {

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

	public void removeMarker(MapMarker marker) {
		markers.remove(marker);
		updateRoute();
	}

	private void updateRoute() {
		PolylineOptions polyline = new PolylineOptions().width(3).color(Color.RED);
		for (MapMarker marker : markers) {
			polyline.add(marker.position);
		}

		updateRoutesOnMap(polyline);
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
		dispatchEvent("Map is ready.", OnMapReadyListener.class, new Processor<OnMapReadyListener>() {
			@Override
			public void process(OnMapReadyListener listener) {
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
	}

	public Marker addMarker(MapMarker mapMarker) {
		MarkerOptions markerOptions = new MarkerOptions().position(mapMarker.position)
																										 .title(mapMarker.title)
																										 .icon(BitmapDescriptorFactory.defaultMarker(mapMarker.color));
		markers.add(mapMarker);

		return mapMarker.marker = mGoogleMap.addMarker(markerOptions);
	}

	private void updateRoutesOnMap(PolylineOptions rectLine) {
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
