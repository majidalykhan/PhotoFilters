package com.example.photofilters;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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


  private final int [][] MASKS = new int[][]{
          {R.raw.fox_face},
          {R.raw.beard1}, {R.raw.beard2},
          {R.raw.cap1}, {R.raw.cap3},
          {R.raw.facemask1}, {R.raw.facemask2}, {R.raw.facemask3}, {R.raw.facemask4}, {R.raw.facemask5},
          {R.raw.glasses1}, {R.raw.yellow_glasses},
          {R.raw.hairstyle1}, {R.raw.hairstyle2}, {R.raw.hairstyle4},
          {R.raw.hat1}, {R.raw.hat2},
          {R.raw.helmet1}, {R.raw.helmet2}, {R.raw.helmet3},
          {R.raw.moustache1}, {R.raw.moustache2},
          //glasses3, hairstyle3,
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

    changeFilter(0);

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

  /**
   * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
   * on this device.
   *
   * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
   *
   * <p>Finishes the activity if Sceneform can not run
   */
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
