package bg.o.sim.taggerizmus;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


//TODO !!! EDIT MARKER LOCATION !!!

/**
 * Displays a location-marker's details and provides an interface for modifying said details.
 */
public class DetailsActivity extends AppCompatActivity implements PhotoDialog.PhotoDialogActionListener, InputDialog.InputCommunicator {

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

    private ImageButton photoButton, moveMarker;

    private Marker marker;
    private MarkerDetail details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        dbAdapter = DbAdapter.getInstance(this);

        View.OnClickListener photoListener = new View.OnClickListener() {
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
        };


        long id = getIntent().getLongExtra(getString(R.string.EXTRA_MARKER), -1);
        if (id == -1)
            finish(); //TODO - ! decide on how to notify user... gently... that something went wrong. !
        marker = dbAdapter.getMarker(id);
        if (marker == null)
            finish(); //TODO - ! decide on how to notify user... gently... that something ELSE went wrong. !
        details = (MarkerDetail) marker.getTag(); //TODO - this needs a check too :/

        IMAGE_FOLDER = getString(R.string.FILE_MARKER_IMAGE_FOLDER) + details.getId();

        address = (TextView) findViewById(R.id.details_address);
        country = (TextView) findViewById(R.id.details_country);

        latitude = (TextView) findViewById(R.id.details_lng);
        longitude = (TextView) findViewById(R.id.details_lat);

        defaultPhoto = (ImageView) findViewById(R.id.details_default_photo);
        defaultPhoto.setOnClickListener(photoListener);

        photoPager = (ViewPager) findViewById(R.id.details_photo_pager);
        images = new ArrayList<>();

        final File imageDir = new File(getFilesDir() + IMAGE_FOLDER);

        if (imageDir.isFile()) imageDir.delete();
        if (!imageDir.exists()) imageDir.mkdirs();

        if (imageDir.listFiles().length > 0) {
            images.addAll(Arrays.asList(imageDir.listFiles()));
            defaultPhoto.setVisibility(View.GONE);
            photoPager.setVisibility(View.VISIBLE);
        } else {
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
                return view.equals((View) object);
            }

            @Override
            public Object instantiateItem(ViewGroup container, final int position) {
                ImageView imageView = new ImageView(DetailsActivity.this);
                imageView.setImageURI(Uri.fromFile(images.get(position)));

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PhotoDialog dialog = new PhotoDialog(DetailsActivity.this, images.get(position), DetailsActivity.this); //TODO - could combine the Context and Listener in 1 param.
                        dialog.show();
                        Window window = dialog.getWindow();
                        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    }
                });

                container.addView(imageView);
                return imageView;
            }

            @Override
            public int getItemPosition(Object object) {
                return images.contains(object) ? images.indexOf(object) : POSITION_NONE;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                photoPager.removeView((View) object);
            }
        });


        photoButton = (ImageButton) findViewById(R.id.details_add_photo);
        photoButton.setOnClickListener(photoListener);

        moveMarker = (ImageButton) findViewById(R.id.details_move_marker); //TODO - listsner

        address.setText(details.getAddress());
        country.setText(details.getCountry());

        latitude.setText(String.format("lat: %.2f", details.getLatLng().latitude));
        longitude.setText(String.format("lng: %.2f", details.getLatLng().longitude));

        View.OnClickListener infoEditListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte field = 1;
                switch (v.getId()) {
                    case R.id.details_address:
                        field = InputDialog.FIELD_TYPE_ADDRESS;
                        break;
                    case R.id.details_country:
                        field = InputDialog.FIELD_TYPE_COUNTRY;
                        break;
                }
                InputDialog dialog = new InputDialog(DetailsActivity.this, field);
                dialog.show();
            }
        };

        address.setOnClickListener(infoEditListener);
        country.setOnClickListener(infoEditListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        File dir = new File(getFilesDir() + "/" + IMAGE_FOLDER);
        images.add(dir.listFiles()[dir.listFiles().length - 1]);
        photoPager.getAdapter().notifyDataSetChanged();
        if (photoPager.getVisibility() == View.GONE) {
            defaultPhoto.setVisibility(View.GONE);
            photoPager.setVisibility(View.VISIBLE);
        }
    }

    private File createImageFile() throws IOException {

        String fileName = (System.currentTimeMillis() + "");

        // Evaluates to sth like /images/marker_8/1495144334391
        File image = new File(getFilesDir() + IMAGE_FOLDER, fileName);

        return image;
    }

    @Override
    public void reactToDeletion(File imageFile) {
        images.remove(imageFile);
        photoPager.getAdapter().notifyDataSetChanged();
        imageFile.delete();
        if (images.size() == 0) {
            defaultPhoto.setVisibility(View.VISIBLE);
            photoPager.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateMarkerAddress(String newAddress) {
        address.setText(newAddress);
        details.setAddress(newAddress);
        dbAdapter.updateMarkerAddress(newAddress, details.getId());
    }

    @Override
    public void updateMarkerCountry(String newCountry) {
        country.setText(newCountry);
        details.setCountry(newCountry);
        dbAdapter.updateMarkerCountry(newCountry, details.getId());
    }
}

class InputDialog extends Dialog {

    interface InputCommunicator {
        void updateMarkerAddress(String newAddress);

        void updateMarkerCountry(String newCountry);
    }

    public static final byte FIELD_TYPE_ADDRESS = 1;    //and enum might be easier to use on method calls AND is safer in terms of values passed.
    public static final byte FIELD_TYPE_COUNTRY = 2;


    private byte field_type;
    private Context c;

    private EditText input;
    private Button cancel, submit;

    public InputDialog(@NonNull Context context, byte field_type) {
        super(context);
        if (context == null || !(context instanceof InputCommunicator))
            throw new IllegalArgumentException("Calling Activity MUST implement InputCommunicator!");
        this.field_type = field_type;
        this.c = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_input);
        setTitle(c.getString(R.string.input_new_value));

        input = (EditText) findViewById(R.id.dialog_input_field);

        cancel = (Button) findViewById(R.id.dialog_input_cancel);
        submit = (Button) findViewById(R.id.dialog_input_save);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = input.getText().toString();
                if (str.isEmpty()) {
                    input.requestFocus();
                    input.setError(c.getString(R.string.error_field_blank));
                } else {
                    switch (field_type) {
                        case FIELD_TYPE_ADDRESS:
                            ((InputCommunicator) c).updateMarkerAddress(str);
                            break;
                        case FIELD_TYPE_COUNTRY:
                            ((InputCommunicator) c).updateMarkerCountry(str);
                            break;
                    }
                    dismiss();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
