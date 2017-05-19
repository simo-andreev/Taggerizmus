package bg.o.sim.taggerizmus;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private DbAdapter dbAdapter;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        dbAdapter = DbAdapter.getInstance(getApplicationContext());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        final Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        dbAdapter.loadMarkers(map);

        /* On map touch - add and store marker */
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                List<Address> addresses = new ArrayList<Address>();
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // The '1' represent max location result to returned
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dbAdapter.addMarker(map.addMarker(new MarkerOptions().position(latLng)), addresses);
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                if (marker.getTag() == null || ! (marker.getTag() instanceof MarkerDetail)) return false;

                Intent i = new Intent(MapActivity.this, DetailsActivity.class);

                i.putExtra(getString(R.string.EXTRA_MARKER), ((MarkerDetail) marker.getTag()).getId());

                startActivity(i);
                return true;
            }
        });


        //TODO - try to center on current location if Location services allowed and present
        //Create a marker;
        LatLng home = new LatLng(0, 0); // null island is best island!!! ~KJU 2017
        //Add it to the map;
        Marker homeMarker = map.addMarker(new MarkerOptions().position(home));
        homeMarker.setTitle(homeMarker.getId());
        //Center view to it;
        map.moveCamera(CameraUpdateFactory.newLatLng(home));
    }
}
