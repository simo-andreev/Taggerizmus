package bg.o.sim.taggerizmus;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;


/**
 * Displays a location-marker's details and provides an interface for modifying said details.
 */
public class DetailsActivity extends AppCompatActivity {

    private static final byte REQUEST_CODE_TAKE_PHOTO = 1;
    //Camera activity should return the photo in extras, under this key.
    private static final String CAM_RETURN_KEY = "data";


    private DbAdapter dbAdapter;

    private TextView address;
    private TextView country;

    private TextView longitude;
    private TextView latitude;

    private ImageView image;

    private Button photoButton;

    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        dbAdapter = DbAdapter.getInstance(this);

        long id = getIntent().getLongExtra(getString(R.string.EXTRA_MARKER), -1);
        if (id == -1) finish(); //TODO - ! decide on how to notify user... gently... that something went wrong. !

        marker = dbAdapter.getMarker(id);
        if (marker == null) finish(); //TODO - ! decide on how to notify user... gently... that something ELSE went wrong. !

        MarkerDetail details = (MarkerDetail) marker.getTag(); //TODO - this needs a check too :/

        address = (TextView) findViewById(R.id.details_address);
        country = (TextView) findViewById(R.id.details_country);

        latitude = (TextView) findViewById(R.id.details_lon);
        longitude = (TextView) findViewById(R.id.details_lat);

        image = (ImageView) findViewById(R.id.details_image);

        Uri imageUri = Uri.parse(getFilesDir() + "/" + getString(R.string.FILE_NAME_MARKER_IMAGE) + ((MarkerDetail) marker.getTag()).getId());
        image.setImageURI(imageUri);
        if(image.getDrawable() == null) image.setImageResource(R.mipmap.image_default_photo);


        photoButton = (Button) findViewById(R.id.details_add_photo);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (camIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;

                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        //TODO - !!! ? !!!
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        //Get the File's URI, add as extra for camera to write in, start camera.
                        Uri photoURI = FileProvider.getUriForFile(DetailsActivity.this, "bg.o.sim.fileprovider", photoFile);
                        camIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(camIntent, REQUEST_CODE_TAKE_PHOTO);
                    }
                }
            }
        });

        address.setText(details.getAddress());
        country.setText(details.getCountry());

        //TODO - set label before values, denoting what they show.
        latitude.setText(""+details.getLatLng().latitude);
        longitude.setText(""+details.getLatLng().longitude);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        Uri imageUri = Uri.parse(getFilesDir() + "/" + getString(R.string.FILE_NAME_MARKER_IMAGE) + ((MarkerDetail) marker.getTag()).getId());
        image.setImageURI(null); //Set to null, before resetting. Otherwise, it will see the same URI and won't change visualization
        image.setImageURI(imageUri);
    }

    private File createImageFile() throws IOException {

        String fileName = getString(R.string.FILE_NAME_MARKER_IMAGE) + ((MarkerDetail) marker.getTag()).getId();

        File image = new File(getFilesDir(), fileName);

        return image;
    }
}
