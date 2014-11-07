package net.thirtythreeforty.pikyak;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This headless fragment will send a picture capture fragment, then
 * call the Activity to upload it to the server.
 */
public class ImageDispatcherFragment extends Fragment {
    private static final String TAG = "ImageDispatcherFragment";

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mImagePath;

    public static interface Callbacks {
        public void doUpload(String imagePath);
    }
    static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void doUpload(String imagePath) {}
    };
    private Callbacks mCallbacks = sDummyCallbacks;

    public static ImageDispatcherFragment newInstance() {
        return new ImageDispatcherFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if(resultCode != Activity.RESULT_OK) {
                // The file is empty and not needed
                new File(mImagePath).delete();
            } else {
                // Send the reply.
                mCallbacks.doUpload(mImagePath);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active mCallbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PIKYAK_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mImagePath = image.getAbsolutePath();
        return image;
    }

    public void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getActivity(), R.string.message_picture_error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error creating file for camera.", ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
}
