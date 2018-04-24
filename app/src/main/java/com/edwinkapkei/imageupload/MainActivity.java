package com.edwinkapkei.imageupload;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.edwinkapkei.imageupload.base.AppHelper;
import com.edwinkapkei.imageupload.base.MyApplication;
import com.edwinkapkei.imageupload.base.VolleyMultipartRequest;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ImageView mAvatar;
    private Uri mCropImageUri;
    private LinearLayout mContainer;

    private SessionManager mSessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initiate shared preferences handler class
        mSessionManager = new SessionManager(this);

        mContainer = findViewById(R.id.container);
        mAvatar = findViewById(R.id.profile_avatar);
        ImageView cameraAction = findViewById(R.id.camera_action);

        //Check if avatar previously uploaded in preferences and load url
        if (!"hello".equalsIgnoreCase(mSessionManager.getUrl())) {
            //Picasso library to display images
            Picasso.get().load(mSessionManager.getUrl()).placeholder(R.drawable.lissa).into(mAvatar);
        }

        cameraAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }

    /**
     * Show image chooser options
     * Uses https://github.com/ArthurHub/Android-Image-Cropper library
     * to generate square images.
     * Replace with your own if you don't need the image cropper library
     */
    private void selectImage() {
        CropImage.startPickImageActivity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                }
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }

        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri uri = result.getUri();
                try {
                    //Uses https://github.com/zetbaitsu/Compressor library to compress selected image
                    File file = new Compressor(this).compressToFile(new File(uri.getPath()));
                    Picasso.get().load(file).into(mAvatar);
                    Toast.makeText(this, "Compressed", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed Compress", Toast.LENGTH_SHORT).show();
                    Picasso.get().load(uri).into(mAvatar);
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        uploadAvatar();
                    }
                }, 1000);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //TODO handle cropping error
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // required permissions granted, start crop image activity
            startCropImageActivity(mCropImageUri);
        } else {
            Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Start crop image activity for the given image.
     */
    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAllowFlipping(false)
                .setActivityTitle("Crop Image")
                .setCropMenuCropButtonIcon(R.drawable.ic_check)
                .setAllowRotation(true)
                .setInitialCropWindowPaddingRatio(0)
                .setFixAspectRatio(true)
                .setAspectRatio(1, 1)
                .setOutputCompressQuality(80)
                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    /**
     * Upload image selected using volley
     */
    private void uploadAvatar() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading Avatar...");
        progressDialog.show();

        final String id = "1";
        String url = "http://dev.flairtips.com/admin/mobile/upload_avatar.php";
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                progressDialog.dismiss();
                String resultResponse = new String(response.data);
                try {
                    JSONObject obj = new JSONObject(resultResponse);
                    if (!obj.getBoolean("error")) {
                        String avatar = obj.getString("avatar");
                        mSessionManager.setUrl(avatar);
                        // Picasso.get().load(avatar).placeholder(R.drawable.lissa).into(mAvatar);
                        Toast.makeText(MainActivity.this, "Avatar Changed", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Response: " + resultResponse);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "JSON Error: " + e);
                    showUploadSnackBar();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.d(TAG, "Volley Error: " + error);
                showUploadSnackBar();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                if (mAvatar == null) {
                    Log.i(TAG, "avatar null");
                }
                params.put("avatar", new DataPart("img_" + id + ".jpg", AppHelper.getFileDataFromDrawable(getApplicationContext(), mAvatar.getDrawable()), "image/jpg"));
                return params;
            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MyApplication.getInstance().addToRequestQueue(multipartRequest);
    }

    /**
     * SnackBar to retry in case of network issues
     */
    private void showUploadSnackBar() {
        Snackbar.make(mContainer, "Network Error. Failed to upload avatar", Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadAvatar();
                    }
                }).show();
    }
}
