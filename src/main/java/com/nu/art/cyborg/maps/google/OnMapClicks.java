package com.nu.art.cyborg.maps.google;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public interface OnMapClicks {

	void onMapClick(int controllerId, LatLng latLng);

	void onMapLongClick(int controllerId, LatLng latLng);

	void onMarkerClick(int controllerId, Marker marker);

	void onInfoWindowClick(int controllerId, Marker marker);
}
