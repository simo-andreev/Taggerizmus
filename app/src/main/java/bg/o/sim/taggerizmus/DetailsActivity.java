package bg.o.sim.taggerizmus;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Displays a location-marker's details and provides an interface for modifying said details.
 */
public class DetailsActivity extends AppCompatActivity {

    private static final byte REQUEST_CODE_TAKE_PHOTO = 1;
    //Camera activity should return the photo in extras, under this key.
    private static final String CAM_RETURN_KEY = "data";

    private static String IMAGE_FOLDER;

    private DbAdapter dbAdapter;

    private TextView address;
    private TextView country;

    private TextView longitude;
    private TextView latitude;

    private ImageView defaultPhoto;
    private ViewPager photoPager;
    private ArrayList<File> images;

    private Button photoButton;

    private Marker marker;
    private MarkerDetail details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        dbAdapter = DbAdapter.getInstance(this);


        long id = getIntent().getLongExtra(getString(R.string.EXTRA_MARKER), -1);
        if (id == -1) finish(); //TODO - ! decide on how to notify user... gently... that something went wrong. !
        marker = dbAdapter.getMarker(id);
        if (marker == null) finish(); //TODO - ! decide on how to notify user... gently... that something ELSE went wrong. !
        details = (MarkerDetail) marker.getTag(); //TODO - this needs a check too :/

        IMAGE_FOLDER = getString(R.string.FILE_MARKER_IMAGE_FOLDER) + details.getId();



        address = (TextView) findViewById(R.id.details_address);
        country = (TextView) findViewById(R.id.details_country);

        latitude = (TextView) findViewById(R.id.details_lon);
        longitude = (TextView) findViewById(R.id.details_lat);

        defaultPhoto = (ImageView) findViewById(R.id.details_default_photo);
        photoPager = (ViewPager) findViewById(R.id.details_photo_pager);
        images = new ArrayList<>();

        final File imageDir = new File(getFilesDir() + IMAGE_FOLDER);

        if (imageDir.isFile()) imageDir.delete();
        if (!imageDir.exists()) imageDir.mkdirs();

        if (imageDir.listFiles().length > 0) {
            images.addAll(Arrays.asList(imageDir.listFiles()));
            defaultPhoto.setVisibility(View.GONE);
            photoPager.setVisibility(View.VISIBLE);
        }
        else {
            photoPager.setVisibility(View.GONE);
            defaultPhoto.setVisibility(View.VISIBLE);
        }

        photoPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return images.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view.equals((View)object);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ImageView imageView = new ImageView(DetailsActivity.this);
                imageView.setImageURI(Uri.fromFile(images.get(position)));
                container.addView(imageView);
                return imageView;
            }
        });


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

        File dir = new File(getFilesDir() + "/" + IMAGE_FOLDER);
        images.add(dir.listFiles()[dir.listFiles().length-1]);
        photoPager.getAdapter().notifyDataSetChanged();
        if (photoPager.getVisibility() == View.GONE){
            defaultPhoto.setVisibility(View.GONE);
            photoPager.setVisibility(View.VISIBLE);
        }
    }

    private File createImageFile() throws IOException {

        // Evaluates to sth like images/marker_1/1495144334391
        String fileName = (System.currentTimeMillis() + "");
        File image = new File(getFilesDir() + IMAGE_FOLDER, fileName);

        return image;
    }
}
