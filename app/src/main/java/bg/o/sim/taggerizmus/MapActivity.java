package bg.o.sim.taggerizmus;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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

        dbAdapter.loadMarkers(map);

        /* On map touch - add and store marker */
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                dbAdapter.addMarker(map.addMarker(new MarkerOptions().position(latLng)));
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {

                Intent i = new Intent(MapActivity.this, DetailsActivity.class);
                i.putExtra(getString(R.string.EXTRA_MARKER), marker.getId());
                startActivity(i);
                return true;
            }
        });
        //Create a marker;
        LatLng home = new LatLng(0, 0); // null island is best island!!! ~KJU 2017


        //Add it to the map;
        Marker homeMarker = map.addMarker(new MarkerOptions().position(home));
        homeMarker.setTitle(homeMarker.getId());


        //Center view to it;
        map.moveCamera(CameraUpdateFactory.newLatLng(home));
    }
}
