package com.nu.art.cyborg.maps.google;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public interface OnMapClicks {

	void mOnMapClickListener(int controllerId, LatLng latLng);

	void mOnMapLongClickListener(int controllerId, LatLng latLng);

	void mOnMarkerClickListener(int controllerId, Marker marker);

	void mOnInfoWindowClickListener(int controllerId, Marker marker);
}
