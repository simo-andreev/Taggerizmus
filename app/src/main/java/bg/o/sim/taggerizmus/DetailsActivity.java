package bg.o.sim.taggerizmus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


/**
 * Displays a location-marker's details and provides an interface for modifying said details.
 */
public class DetailsActivity extends AppCompatActivity {

    private DbAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
    }
}
