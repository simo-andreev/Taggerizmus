package bg.o.sim.taggerizmus;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.Date;

public class PhotoDialog extends Dialog {

    private File imageFile;
    private ImageButton deleteButton, returnButton;
    private ImageView photo;
    private TextView date;
    private Context c;
    private PhotoDialogActionListener listener;


    public PhotoDialog(@NonNull Context context, File imageFile, PhotoDialogActionListener listener) {
        super(context);

        if (imageFile == null || !imageFile.exists() || imageFile.isDirectory())
            throw new IllegalArgumentException("The File parameter MUST be an image file!");
        if (listener == null)
            throw new IllegalArgumentException("The listener parameter MUST be non-null!");

        this.c = context;
        this.listener = listener;
        this.imageFile = imageFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_photo);

        deleteButton = (ImageButton) findViewById(R.id.dialog_photo_delete);
        returnButton = (ImageButton) findViewById(R.id.dialog_photo_return);

        photo = (ImageView) findViewById(R.id.dialog_photo_image);
        photo.setImageURI(Uri.fromFile(imageFile));

        date = (TextView) findViewById(R.id.dialog_photo_date);
        date.setText((new Date(Long.parseLong(imageFile.getName())).toString()));   //TODO add DateFormat, to make it a bit more sightly

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(c)
                        .setIcon(R.mipmap.ic_delete)
                        .setMessage(c.getString(R.string.message_confirm_delete))
                        .setPositiveButton("Yes", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listener.reactToDeletion(imageFile);
                                PhotoDialog.this.dismiss();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }


    public interface PhotoDialogActionListener {
        void reactToDeletion(File imageFile);
    }
}

