package bg.o.sim.taggerizmus;

import com.google.android.gms.maps.model.LatLng;


/**
 * Stores the modifiable info of a GoogleMap Marker.
 * Necessitated by restrictions on background tasks modifying a GoogleMap object, which in term is necessary for proper Marker instantiation.
 */
public class MarkerDetail {

    private final long id;

    private String address;
    private String country;

    private LatLng latLng;

    public MarkerDetail(long id, String address, String country, LatLng latLng) {
        this.id = id;
        this.address = address;
        this.country = country;
        this.latLng = latLng;
    }

    public long getId() {
        return id;
    }

    public String getAddress() {return address;}
    public String getCountry() {return country;}
    public LatLng getLatLng() {return latLng;}


    public void setAddress(String address) {
        if (address != null && address.length() > 3)
            this.address = address;
    }
    public void setCountry(String country) {
        if (country != null && country.length() >= 2)
            this.country = country;
    }
    public void setLatLng(LatLng latLng) {
        if (latLng != null)
            this.latLng = latLng;
    }
}
