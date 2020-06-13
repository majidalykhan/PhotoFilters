package com.example.photofilters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.AugmentedFaceNode;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * This is an example activity that uses the Sceneform UX package to make common Augmented Faces
 * tasks easier.
 */
public class AugmentedFacesActivity extends AppCompatActivity {
  private static final String TAG = AugmentedFacesActivity.class.getSimpleName();

  private static final double MIN_OPENGL_VERSION = 3.0;

  private FrameLayout frameLayout;

  private FaceArFragment arFragment;

  private ModelRenderable faceRegionsRenderable;
  private Texture faceMeshTexture;

  private Button recommend;

  private String SpinnerselectedItem;


  private final int [][] MASKS = new int[][]{
          {R.raw.beard1}, {R.raw.beard2},
          {R.raw.fox_face},
          {R.raw.cap1}, {R.raw.cap3},
          {R.raw.facemask1}, {R.raw.facemask2}, {R.raw.facemask3}, {R.raw.facemask4}, {R.raw.facemask5},
          {R.raw.glasses1}, {R.raw.yellow_glasses},
          {R.raw.hairstyle1}, {R.raw.hairstyle2}, {R.raw.hairstyle4},
          {R.raw.hat1}, {R.raw.hat2},
          {R.raw.helmet1}, {R.raw.helmet2}, {R.raw.helmet3},
          {R.raw.moustache1}, {R.raw.moustache2},
          //glasses3, hairstyle3,
  };

    private final int [][] BEARDS = new int[][]{
            {R.raw.beard1}, {R.raw.beard2},
    };

    private final int [][] CAPS = new int[][]{
            {R.raw.cap1}, {R.raw.cap3},
    };

    private final int [][] FACES = new int[][]{
            {R.raw.facemask1}, {R.raw.facemask2}, {R.raw.facemask3}, {R.raw.facemask4}, {R.raw.facemask5},
    };

    private final int [][] GLASSES = new int[][]{
            {R.raw.yellow_glasses}, {R.raw.glasses1},
    };

    private final int [][] HAIRSTYLES = new int[][]{
            {R.raw.hairstyle1}, {R.raw.hairstyle2}, {R.raw.hairstyle4},
    };

    private final int [][] HATS = new int[][]{
            {R.raw.hat1}, {R.raw.hat2},
    };

    private final int [][] HELMETS = new int[][]{
            {R.raw.helmet1}, {R.raw.helmet2}, {R.raw.helmet3},
    };

    private final int [][] MOUSTACHES = new int[][]{
            {R.raw.moustache1}, {R.raw.moustache2},
    };

  private static int mMask = 0;
  private int mMaskLoaded = 0;
  private volatile boolean isMaskChanged = false;



  ImageButton back, next;

  private final HashMap<AugmentedFace, AugmentedFaceNode> faceNodeMap = new HashMap<>();

  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  // FutureReturnValueIgnored is not valid
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    }

    setContentView(R.layout.activity_face_mesh);
    arFragment = (FaceArFragment) getSupportFragmentManager().findFragmentById(R.id.face_fragment);

    back = findViewById(R.id.back);
    next = findViewById(R.id.next);

    recommend = findViewById(R.id.recommend);

    arscene();

    changeFilter(0);

    userInterest();

    listeners();

  }

  public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
    if (ArCoreApk.getInstance().checkAvailability(activity)
        == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
      Log.e(TAG, "Augmented Faces requires ARCore.");
      Toast.makeText(activity, "Augmented Faces requires ARCore", Toast.LENGTH_LONG).show();
      activity.finish();
      return false;
    }
    String openGlVersionString =
        ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
            .getDeviceConfigurationInfo()
            .getGlEsVersion();
    if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
      Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
      Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
          .show();
      activity.finish();
      return false;
    }
    return true;
  }

  private void listeners(){

    next.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        mMask = (mMask+1) % MASKS.length;
        isMaskChanged = true;

        changeFilter(mMask);

        Toast.makeText(AugmentedFacesActivity.this, "Next Pressed", Toast.LENGTH_LONG).show();
      }
    });

    back.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        --mMask;
        if (mMask < 0) mMask = MASKS.length - 1;
        isMaskChanged = true;

        changeFilter(mMask);


        Toast.makeText(AugmentedFacesActivity.this, "Back Pressed", Toast.LENGTH_LONG).show();

      }
    });


    recommend.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        if(SpinnerselectedItem == "Beard"){

            Random rand = new Random();

            recommendFilterBeard(rand.nextInt());


          Toast.makeText(getApplication().getBaseContext(), "Beard Selected", Toast.LENGTH_SHORT).show();
        }
        else if(SpinnerselectedItem == "Cap"){

            Random rand = new Random();

            recommendFilterCap(rand.nextInt());

          Toast.makeText(getApplication().getBaseContext(), "Cap Selected", Toast.LENGTH_SHORT).show();
        }
        else if(SpinnerselectedItem == "Face"){

            Random rand = new Random();

            recommendFilterFace(rand.nextInt());

          Toast.makeText(getApplication().getBaseContext(), "Cap Selected", Toast.LENGTH_SHORT).show();
        }
        else if(SpinnerselectedItem == "Glasses"){

            Random rand = new Random();

            recommendFilterGlasses(rand.nextInt());

          Toast.makeText(getApplication().getBaseContext(), "Cap Selected", Toast.LENGTH_SHORT).show();
        }
        else if(SpinnerselectedItem == "Hairstyle"){

            Random rand = new Random();

            recommendFilterHairstyle(rand.nextInt());

          Toast.makeText(getApplication().getBaseContext(), "Cap Selected", Toast.LENGTH_SHORT).show();
        }
        else if(SpinnerselectedItem == "Hat"){

            Random rand = new Random();

            recommendFilterHat(rand.nextInt());

          Toast.makeText(getApplication().getBaseContext(), "Cap Selected", Toast.LENGTH_SHORT).show();
        }
        else if(SpinnerselectedItem == "Helmet"){

            Random rand = new Random();

            recommendFilterHelmet(rand.nextInt());

          Toast.makeText(getApplication().getBaseContext(), "Cap Selected", Toast.LENGTH_SHORT).show();
        }
        else if(SpinnerselectedItem == "Moustache"){

            Random rand = new Random();

            recommendFilterMoustache(rand.nextInt());

          Toast.makeText(getApplication().getBaseContext(), "Cap Selected", Toast.LENGTH_SHORT).show();
        }
        else {
          Toast.makeText(getApplication().getBaseContext(), "Select interest first", Toast.LENGTH_SHORT).show();
          userInterest();
        }

      }
    });

  }

  private void arscene() {


    ArSceneView sceneView = arFragment.getArSceneView();

    // This is important to make sure that the camera stream renders first so that
    // the face mesh occlusion works correctly.
    sceneView.setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST);

    Scene scene = sceneView.getScene();

    scene.addOnUpdateListener(
            (FrameTime frameTime) -> {
              if (faceRegionsRenderable == null || faceMeshTexture == null) {
                return;
              }

              Collection<AugmentedFace> faceList =
                      sceneView.getSession().getAllTrackables(AugmentedFace.class);

              // Make new AugmentedFaceNodes for any new faces.
              for (AugmentedFace face : faceList) {
                if (!faceNodeMap.containsKey(face)) {
                  AugmentedFaceNode faceNode = new AugmentedFaceNode(face);
                  faceNode.setParent(scene);
                  faceNode.setFaceRegionsRenderable(faceRegionsRenderable);
                  faceNode.setFaceMeshTexture(faceMeshTexture);
                  faceNodeMap.put(face, faceNode);
                }
              }

              // Remove any AugmentedFaceNodes associated with an AugmentedFace that stopped tracking.
              Iterator<Map.Entry<AugmentedFace, AugmentedFaceNode>> iter =
                      faceNodeMap.entrySet().iterator();
              while (iter.hasNext()) {
                Map.Entry<AugmentedFace, AugmentedFaceNode> entry = iter.next();
                AugmentedFace face = entry.getKey();
                if (face.getTrackingState() == TrackingState.STOPPED) {
                  AugmentedFaceNode faceNode = entry.getValue();
                  faceNode.setParent(null);
                  iter.remove();
                }
              }
            });

  }

  private void changeFilter(int maskNumber){


    int [] mask = MASKS[maskNumber];

    InputStream stream = getApplicationContext().getResources().openRawResource(mask[0]);

    ModelRenderable.builder()
            .setSource(this, mask[0])
            .build()
            .thenAccept(
                    modelRenderable -> {
                      faceRegionsRenderable = modelRenderable;
                      modelRenderable.setShadowCaster(false);
                      modelRenderable.setShadowReceiver(false);
                    });

      ModelRenderable.builder()
              .setSource(this, mask[1])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[2])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[3])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[4])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[5])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[6])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[7])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });


      ModelRenderable.builder()
              .setSource(this, mask[8])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[9])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[10])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[11])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[12])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[13])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[14])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[15])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[16])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[17])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[18])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[19])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[20])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, mask[21])
              .build()
              .thenAccept(
                      modelRenderable -> {
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

    // Load the face mesh texture.
    Texture.builder()
            .setSource(this, R.drawable.sample)
            .build()
            .thenAccept(texture -> faceMeshTexture = texture);

  }

  private void userInterest(){

    String[] selectInterest = { "Beard", "Cap", "Face", "Glasses", "Hairstyle", "Hat", "Helmet", "Moustache"};

    LayoutInflater inflater = getLayoutInflater();
    View alertLayout = inflater.inflate(R.layout.spinner_layout, null);
    final Spinner spSex = (Spinner) alertLayout.findViewById(R.id.spinner);

    final List<String> interestList = new ArrayList<>(Arrays.asList(selectInterest));

    // Initializing an ArrayAdapter
    @SuppressLint({"NewApi", "LocalSuppress"}) final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
            Objects.requireNonNull(this),R.layout.support_simple_spinner_dropdown_item,interestList);

    spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
    spSex.setAdapter(spinnerArrayAdapter);


    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle("Choose type of filter you are interested in");
    alert.setView(alertLayout);
    alert.setCancelable(false);
    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Toast.makeText(getApplication().getBaseContext(), "Recommendations will not be available", Toast.LENGTH_SHORT).show();

      }
    });

    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {

        SpinnerselectedItem = String.valueOf(spSex.getSelectedItem());

        Toast.makeText(getBaseContext(), SpinnerselectedItem, Toast.LENGTH_SHORT).show();

      }
    });
    AlertDialog dialog = alert.create();
    dialog.show();
  }


    private void recommendFilterBeard(int maskNumber)
    {
       // int mask = new Random().nextInt(BEARDS[maskNumber].length);

        int [] mask = BEARDS[maskNumber];

        //InputStream stream = getApplicationContext().getResources().openRawResource(mask[0]);

        ModelRenderable.builder()
                .setSource(this, mask[0])
                .build()
                .thenAccept(
                        modelRenderable -> {
                            faceRegionsRenderable = modelRenderable;
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        // Load the face mesh texture.
        Texture.builder()
                .setSource(this, R.drawable.sample)
                .build()
                .thenAccept(texture -> faceMeshTexture = texture);


    }

    private void recommendFilterCap(int maskNumber)
    {
        int mask = new Random().nextInt(CAPS[maskNumber].length);

        //InputStream stream = getApplicationContext().getResources().openRawResource(mask[0]);

        ModelRenderable.builder()
                .setSource(this, mask)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            faceRegionsRenderable = modelRenderable;
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        // Load the face mesh texture.
        Texture.builder()
                .setSource(this, R.drawable.sample)
                .build()
                .thenAccept(texture -> faceMeshTexture = texture);


    }

    private void recommendFilterFace(int maskNumber)
    {
        int mask = new Random().nextInt(FACES[maskNumber].length);

        //InputStream stream = getApplicationContext().getResources().openRawResource(mask[0]);

        ModelRenderable.builder()
                .setSource(this, mask)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            faceRegionsRenderable = modelRenderable;
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        // Load the face mesh texture.
        Texture.builder()
                .setSource(this, R.drawable.sample)
                .build()
                .thenAccept(texture -> faceMeshTexture = texture);


    }

    private void recommendFilterGlasses(int maskNumber)
    {
        int mask = new Random().nextInt(GLASSES[maskNumber].length);

        //InputStream stream = getApplicationContext().getResources().openRawResource(mask[0]);

        ModelRenderable.builder()
                .setSource(this, mask)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            faceRegionsRenderable = modelRenderable;
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        // Load the face mesh texture.
        Texture.builder()
                .setSource(this, R.drawable.sample)
                .build()
                .thenAccept(texture -> faceMeshTexture = texture);


    }

    private void recommendFilterHairstyle(int maskNumber)
    {
        int mask = new Random().nextInt(HAIRSTYLES[maskNumber].length);

        //InputStream stream = getApplicationContext().getResources().openRawResource(mask[0]);

        ModelRenderable.builder()
                .setSource(this, mask)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            faceRegionsRenderable = modelRenderable;
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        // Load the face mesh texture.
        Texture.builder()
                .setSource(this, R.drawable.sample)
                .build()
                .thenAccept(texture -> faceMeshTexture = texture);


    }

    private void recommendFilterHat(int maskNumber)
    {
        int mask = new Random().nextInt(HATS[maskNumber].length);

        //InputStream stream = getApplicationContext().getResources().openRawResource(mask[0]);

        ModelRenderable.builder()
                .setSource(this, mask)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            faceRegionsRenderable = modelRenderable;
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        // Load the face mesh texture.
        Texture.builder()
                .setSource(this, R.drawable.sample)
                .build()
                .thenAccept(texture -> faceMeshTexture = texture);


    }

    private void recommendFilterHelmet(int maskNumber)
    {
        int mask = new Random().nextInt(HELMETS[maskNumber].length);

        //InputStream stream = getApplicationContext().getResources().openRawResource(mask[0]);

        ModelRenderable.builder()
                .setSource(this, mask)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            faceRegionsRenderable = modelRenderable;
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        // Load the face mesh texture.
        Texture.builder()
                .setSource(this, R.drawable.sample)
                .build()
                .thenAccept(texture -> faceMeshTexture = texture);


    }

    private void recommendFilterMoustache(int maskNumber)
    {
        int mask = new Random().nextInt(MOUSTACHES[maskNumber].length);

        //InputStream stream = getApplicationContext().getResources().openRawResource(mask[0]);

        ModelRenderable.builder()
                .setSource(this, mask)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            faceRegionsRenderable = modelRenderable;
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        // Load the face mesh texture.
        Texture.builder()
                .setSource(this, R.drawable.sample)
                .build()
                .thenAccept(texture -> faceMeshTexture = texture);


    }


  @Override
  public void onBackPressed() {
    super.onBackPressed();
    Intent i = new Intent(AugmentedFacesActivity.this, Dashboard.class);
    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(i);
    finish();
  }


}
