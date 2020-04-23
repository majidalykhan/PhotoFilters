package com.example.photofilters;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Application;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.deeparteffects.sdk.android.DeepArtEffectsClient;
import com.deeparteffects.sdk.android.model.Result;
import com.deeparteffects.sdk.android.model.Styles;
import com.deeparteffects.sdk.android.model.UploadRequest;
import com.deeparteffects.sdk.android.model.UploadResponse;
import com.google.android.gms.tasks.OnCompleteListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.amazonaws.regions.Regions.EU_WEST_1;

public class ArtisticStyle extends AppCompatActivity {

    private static final String TAG = ArtisticStyle.class.getSimpleName();

    private String API_KEY = "reH1y2wICx9m6AZVdXGeg2nPECCLDyWeac3GPTMv"; //Your key here
    private String ACCESS_KEY = "AKIA3XE3HF7SRUFGBWWP"; //Your key here
    private String SECRET_KEY = "TJWgmGqI2VW+wlig7X7zC01iK5ErjGr/2fi/uZkR"; //Your key here

    private static final int REQUEST_GALLERY = 100;
    private static final int CHECK_RESULT_INTERVAL_IN_MS = 2500;
    private static final int IMAGE_MAX_SIDE_LENGTH = 800;

    private Activity mActivity;
    private Bitmap mImageBitmap;
    private TextView mStatusText;
    private ImageView mImageView;
    private ImageView selectedImage;
    private ImageView invisibleImage;
    private ProgressBar mProgressbarView;
    private ImageButton btnSave;
    private TextView saveText;
    private boolean isProcessing = false;

    private RecyclerView recyclerView;
    private DeepArtEffectsClient deepArtEffectsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artistic_style);

        mActivity = this;

        ApiClientFactory factory = new ApiClientFactory()
                .apiKey(API_KEY)
                .credentialsProvider(new AWSCredentialsProvider() {
                    @Override
                    public AWSCredentials getCredentials() {
                        return new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
                    }

                    @Override
                    public void refresh() {
                    }
                }).region(EU_WEST_1.getName());
        deepArtEffectsClient = factory.build(DeepArtEffectsClient.class);

        mStatusText = (TextView) findViewById(R.id.statusText);
        mProgressbarView = (ProgressBar) findViewById(R.id.progressBar);
        selectedImage = (ImageView) findViewById(R.id.selectedImage);
        mImageView = (ImageView) findViewById(R.id.imageView);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        invisibleImage= (ImageView) findViewById(R.id.invisibleImage);

        btnSave = (ImageButton) findViewById(R.id.btnSave);
        saveText = (TextView) findViewById(R.id.saveText);

        ImageButton btnGallery = (ImageButton) findViewById(R.id.btnGallery);


        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setItemAnimator(new DefaultItemAnimator());



        selectedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_GALLERY);
                }
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryAddPic();
                Toast.makeText(ArtisticStyle.this, "Picture Saved", Toast.LENGTH_LONG).show();
            }
        });


        loadingStyles();
    }

    private void loadingStyles() {
        mStatusText.setText("Loading styles...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Styles styles = deepArtEffectsClient.stylesGet();
                final StyleAdapter styleAdapter = new StyleAdapter(
                        getApplicationContext(),
                        styles,
                        new StyleAdapter.ClickListener() {
                            @Override
                            public void onClick(String styleId) {
                                if (!isProcessing) {
                                    if (mImageBitmap != null) {
                                        Log.d(TAG, String.format("Style with ID %s clicked.", styleId));
                                        isProcessing = true;
                                        mProgressbarView.setVisibility(View.VISIBLE);
                                        btnSave.setVisibility(View.GONE);
                                        saveText.setVisibility(View.GONE);
                                        uploadImage(styleId);
                                    } else {
                                        Toast.makeText(mActivity, "Please choose a picture first",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setAdapter(styleAdapter);
                        mProgressbarView.setVisibility(View.GONE);
                        mStatusText.setText("");
                    }
                });
            }
        }).start();
    }

    private class ImageReadyCheckTimer extends TimerTask {
        private String mSubmissionId;

        public ImageReadyCheckTimer(String submissionId) {
            mSubmissionId = String.valueOf(submissionId);
        }

        @Override
        public void run() {
            try {
                final Result result = deepArtEffectsClient.resultGet(mSubmissionId);
                String submissionStatus = result.getStatus();
                Log.d(TAG, String.format("Submission status is %s", submissionStatus));
                if (submissionStatus.equals(SubmissionStatus.FINISHED)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(mActivity).load(result.getUrl()).fitCenter()
                                    .transition(new DrawableTransitionOptions().crossFade()).into(selectedImage);
                            mProgressbarView.setVisibility(View.GONE);
                            selectedImage.setVisibility(View.VISIBLE);
                            btnSave.setVisibility(View.VISIBLE);
                            saveText.setVisibility(View.VISIBLE);
                            mStatusText.setText("");

                        }
                    });
                    isProcessing = false;
                    cancel();
                }
            } catch (Exception e) {
                cancel();
            }
        }
    }

    private void uploadImage(final String styleId) {
        mStatusText.setText("Uploading picture...");
        Log.d(TAG, String.format("Upload image with style id %s", styleId));
        new Thread(new Runnable() {
            @Override
            public void run() {
                UploadRequest uploadRequest = new UploadRequest();
                uploadRequest.setStyleId(styleId);
                uploadRequest.setImageBase64Encoded(convertBitmapToBase64(mImageBitmap));
                UploadResponse response = deepArtEffectsClient.uploadPost(uploadRequest);
                String submissionId = response.getSubmissionId();
                Log.d(TAG, String.format("Upload complete. Got submissionId %s", response.getSubmissionId()));
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new ImageReadyCheckTimer(submissionId),
                        CHECK_RESULT_INTERVAL_IN_MS, CHECK_RESULT_INTERVAL_IN_MS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mStatusText.setText("Picture processing...");
                    }
                });
            }
        }).start();
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");

        //Handle own activity result
        switch (requestCode) {
            case REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {
                    mImageBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(data.getData(),
                            this.getContentResolver(), IMAGE_MAX_SIDE_LENGTH);
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selected = BitmapFactory.decodeStream(imageStream);
                        invisibleImage.setVisibility(View.GONE);
                        selectedImage.setImageBitmap(mImageBitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(ArtisticStyle.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    }

                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void galleryAddPic() {
        selectedImage.buildDrawingCache();
        Bitmap bm=selectedImage.getDrawingCache();

        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

    }

    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Pictures/PhotoFilters/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

}
