package com.example.photofilters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

import javax.microedition.khronos.opengles.GL;

/**
 * This is an example activity that uses the Sceneform UX package to make common Augmented Faces
 * tasks easier.
 */
public class AugmentedFacesActivity extends AppCompatActivity {
  private static final String TAG = AugmentedFacesActivity.class.getSimpleName();

  private static final double MIN_OPENGL_VERSION = 3.0;

  private FrameLayout frameLayout;

  private FaceArFragment arFragment;

  private ModelRenderable faceRegionsRenderable = null;
  private Texture faceMeshTexture = null;

  private Button recommend;
  private ImageButton changeInterest;

  private String SpinnerselectedItem;

    private ArrayList<ModelRenderable> MASKS = new ArrayList<ModelRenderable>();
    private ArrayList<ModelRenderable> GLASSES = new ArrayList<ModelRenderable>();
    private ArrayList<ModelRenderable> BEARDS = new ArrayList<ModelRenderable>();
    private ArrayList<ModelRenderable> MOUSTACHES = new ArrayList<ModelRenderable>();
    private ArrayList<ModelRenderable> FACES = new ArrayList<ModelRenderable>();
    private ArrayList<ModelRenderable> HATS = new ArrayList<ModelRenderable>();
    private ArrayList<ModelRenderable> OTHERS = new ArrayList<ModelRenderable>();

  private static int mMask = 0;
  private int mMaskLoaded = 0;
  private volatile boolean isMaskChanged = false;

  private boolean changeModel = false;

  ImageButton back, next, capture;

  private final HashMap<AugmentedFace, AugmentedFaceNode> faceNodeMap = new HashMap<>();

  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  // FutureReturnValueIgnored is not valid
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

   /* if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    } */

    setContentView(R.layout.activity_face_mesh);

      back = findViewById(R.id.back);
      next = findViewById(R.id.next);
      recommend = findViewById(R.id.recommend);
      changeInterest = findViewById(R.id.changeInterest);
      capture = findViewById(R.id.capture);

      next.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              changeModel = !changeModel;

              mMask = (mMask+1) % MASKS.size();
              isMaskChanged = true;

              faceRegionsRenderable = MASKS.get(mMask);

              Toast.makeText(AugmentedFacesActivity.this, "Next Pressed", Toast.LENGTH_LONG).show();
          }
      });

      back.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              changeModel = !changeModel;

              --mMask;
              if (mMask < 0) mMask = MASKS.size() - 1;
              isMaskChanged = true;

              faceRegionsRenderable = MASKS.get(mMask);

              Toast.makeText(AugmentedFacesActivity.this, "Back Pressed", Toast.LENGTH_LONG).show();

          }
      });


      recommend.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              if(SpinnerselectedItem == "Glasses"){

                  int randomNum = ThreadLocalRandom.current().nextInt(0, 3 + 1);

                  changeModel = !changeModel;

                  faceRegionsRenderable = GLASSES.get(randomNum);

                  changeInterest.setVisibility(View.VISIBLE);

                  Toast.makeText(getApplication().getBaseContext(), "Glasses Recommended", Toast.LENGTH_SHORT).show();
              }
              else if(SpinnerselectedItem == "Beard"){

                  int randomNum = ThreadLocalRandom.current().nextInt(0, 2 + 1);

                  changeModel = !changeModel;

                  faceRegionsRenderable = BEARDS.get(randomNum);

                  changeInterest.setVisibility(View.VISIBLE);

                  Toast.makeText(getApplication().getBaseContext(), "Beard Recommended", Toast.LENGTH_SHORT).show();
              }
              else if(SpinnerselectedItem == "Moustache"){

                  int randomNum = ThreadLocalRandom.current().nextInt(0, 2 + 1);

                  changeModel = !changeModel;

                  faceRegionsRenderable = MOUSTACHES.get(randomNum);

                  changeInterest.setVisibility(View.VISIBLE);

                  Toast.makeText(getApplication().getBaseContext(), "Moustache Recommended", Toast.LENGTH_SHORT).show();
              }
              else if(SpinnerselectedItem == "Face"){

                  int randomNum = ThreadLocalRandom.current().nextInt(0, 4 + 1);

                  changeModel = !changeModel;

                  faceRegionsRenderable = FACES.get(randomNum);

                  changeInterest.setVisibility(View.VISIBLE);

                  Toast.makeText(getApplication().getBaseContext(), "Face Mask Recommended", Toast.LENGTH_SHORT).show();
              }
              else if(SpinnerselectedItem == "Hat"){

                  int randomNum = ThreadLocalRandom.current().nextInt(0, 3 + 1);

                  changeModel = !changeModel;

                  faceRegionsRenderable = HATS.get(randomNum);

                  changeInterest.setVisibility(View.VISIBLE);

                  Toast.makeText(getApplication().getBaseContext(), "Hat Recommended", Toast.LENGTH_SHORT).show();
              }
              else if(SpinnerselectedItem == "Other"){

                  int randomNum = ThreadLocalRandom.current().nextInt(0, 2 + 1);

                  changeModel = !changeModel;

                  faceRegionsRenderable = OTHERS.get(randomNum);

                  changeInterest.setVisibility(View.VISIBLE);

                  Toast.makeText(getApplication().getBaseContext(), "Other filter Recommended", Toast.LENGTH_SHORT).show();
              }
              else {
                  userInterest();
              }

          }
      });

      changeInterest.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              changeInterest();
          }
      });


      capture.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              next.setVisibility(View.GONE);
              back.setVisibility(View.GONE);
              changeInterest.setVisibility(View.GONE);
              capture.setVisibility(View.GONE);
              takeScreenshot();

              Toast.makeText(getApplicationContext(), "Picture saved in Gallery", Toast.LENGTH_LONG)
                      .show();
          }
      });


      arFragment = (FaceArFragment) getSupportFragmentManager().findFragmentById(R.id.face_fragment);


      Texture.builder()
              .setSource(this, R.drawable.makeup)
              .build()
              .thenAccept(texture -> faceMeshTexture = texture);

      InitModels();
      initGlasses();
      initBeards();
      initMoustaches();
      initFaces();
      initHats();
      initOthers();

      ArSceneView sceneView = arFragment.getArSceneView();
      sceneView.setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST);

      Scene scene = sceneView.getScene();

      scene.addOnUpdateListener(
              (FrameTime frameTime) -> {
                  if (faceRegionsRenderable == null) {
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
                          faceNodeMap.put(face, faceNode);
                      }
                      else if(changeModel) {
                          faceNodeMap.get(face).setFaceRegionsRenderable(faceRegionsRenderable);
                      }
                  }

                  changeModel = false;

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

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    Intent i = new Intent(AugmentedFacesActivity.this, Dashboard.class);
    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(i);
    finish();
  }

  private void InitModels() {
      ModelRenderable.builder()
              .setSource(this, Uri.parse("crown.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          faceRegionsRenderable = modelRenderable;
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, Uri.parse("graduate.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, Uri.parse("pirateHat.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, Uri.parse("sherifHat.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, Uri.parse("longHat.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, Uri.parse("glasses1.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, Uri.parse("glasses2.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });


      ModelRenderable.builder()
              .setSource(this, Uri.parse("glasses3.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });


      ModelRenderable.builder()
              .setSource(this, Uri.parse("glasses4.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, Uri.parse("beard1.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });


      ModelRenderable.builder()
              .setSource(this, Uri.parse("beard1_black.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });


      ModelRenderable.builder()
              .setSource(this, Uri.parse("beard1_white.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, Uri.parse("mustache1.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, Uri.parse("mustache1_brown.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, Uri.parse("mustache1_white.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });


      ModelRenderable.builder()
              .setSource(this, Uri.parse("helmet1.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });


      ModelRenderable.builder()
              .setSource(this, Uri.parse("facemask1.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, Uri.parse("facemask1_black.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, Uri.parse("facemask1_red.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, Uri.parse("facemask2.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      ModelRenderable.builder()
              .setSource(this, Uri.parse("facemask3.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });


      ModelRenderable.builder()
              .setSource(this, Uri.parse("masquerade.sfb"))
              .build()
              .thenAccept(
                      modelRenderable -> {
                          MASKS.add(modelRenderable);
                          modelRenderable.setShadowCaster(false);
                          modelRenderable.setShadowReceiver(false);
                      });

      // CAPS


  }

  private void userInterest(){

        String[] selectInterest = { "Glasses", "Beard", "Moustache",  "Face", "Hat", "Other"};

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

                Toast.makeText(getBaseContext(), SpinnerselectedItem + " Selected", Toast.LENGTH_SHORT).show();

            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void changeInterest(){

        String[] selectInterest = { "Glasses", "Beard", "Moustache",  "Face", "Hat", "Other"};

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
        alert.setTitle("Change interest for type of filter you are interested in");
        alert.setView(alertLayout);
        alert.setCancelable(false);
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplication().getBaseContext(), "Interest not changed", Toast.LENGTH_SHORT).show();

            }
        });

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SpinnerselectedItem = String.valueOf(spSex.getSelectedItem());

                Toast.makeText(getBaseContext(), "Changed to " +SpinnerselectedItem, Toast.LENGTH_SHORT).show();

            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void initGlasses(){


        ModelRenderable.builder()
                .setSource(this, Uri.parse("glasses1.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            GLASSES.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        ModelRenderable.builder()
                .setSource(this, Uri.parse("glasses2.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            GLASSES.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });


        ModelRenderable.builder()
                .setSource(this, Uri.parse("glasses3.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            GLASSES.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });


        ModelRenderable.builder()
                .setSource(this, Uri.parse("glasses4.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            GLASSES.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });


    }

    private void initBeards(){

        ModelRenderable.builder()
                .setSource(this, Uri.parse("beard1.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            BEARDS.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });


        ModelRenderable.builder()
                .setSource(this, Uri.parse("beard1_black.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            BEARDS.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });


        ModelRenderable.builder()
                .setSource(this, Uri.parse("beard1_white.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            BEARDS.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

    }

    private void initMoustaches(){


        ModelRenderable.builder()
                .setSource(this, Uri.parse("mustache1.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            MOUSTACHES.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        ModelRenderable.builder()
                .setSource(this, Uri.parse("mustache1_brown.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            MOUSTACHES.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        ModelRenderable.builder()
                .setSource(this, Uri.parse("mustache1_white.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            MOUSTACHES.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

    }

    private void initFaces(){


        ModelRenderable.builder()
                .setSource(this, Uri.parse("facemask1.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            FACES.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        ModelRenderable.builder()
                .setSource(this, Uri.parse("facemask1_black.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            FACES.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        ModelRenderable.builder()
                .setSource(this, Uri.parse("facemask1_red.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            FACES.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        ModelRenderable.builder()
                .setSource(this, Uri.parse("facemask2.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            FACES.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        ModelRenderable.builder()
                .setSource(this, Uri.parse("facemask3.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            FACES.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

    }

    private void initHats(){

        ModelRenderable.builder()
                .setSource(this, Uri.parse("graduate.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            HATS.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        ModelRenderable.builder()
                .setSource(this, Uri.parse("pirateHat.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            HATS.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        ModelRenderable.builder()
                .setSource(this, Uri.parse("sherifHat.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            HATS.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

        ModelRenderable.builder()
                .setSource(this, Uri.parse("longHat.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            HATS.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

    }

    private void initOthers(){

        ModelRenderable.builder()
                .setSource(this, Uri.parse("masquerade.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            OTHERS.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });


        ModelRenderable.builder()
                .setSource(this, Uri.parse("helmet1.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            OTHERS.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });


        ModelRenderable.builder()
                .setSource(this, Uri.parse("crown.sfb"))
                .build()
                .thenAccept(
                        modelRenderable -> {
                            OTHERS.add(modelRenderable);
                            modelRenderable.setShadowCaster(false);
                            modelRenderable.setShadowReceiver(false);
                        });

    }
    
    public static int randInt(int min, int max) {

      
        Random rand = null;

        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }


    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }

    }

    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

}
