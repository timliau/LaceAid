package com.sp.laceaid.uiBottomNavBar.ar;

import android.app.Activity;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sp.laceaid.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MeasureActivity extends Activity {
    private static final String TAG = MeasureActivity.class.getSimpleName();

    private FloatingActionButton mUndo, mSave, mBack;
    private TextView mTextView;
    private GLSurfaceView mSurfaceView;
    private MainRenderer mRenderer;

    private boolean mUserRequestedInstall = true;

    private Session mSession;
    private Config mConfig;

    private List<float[]> mPoints = new ArrayList<float[]>();

    private float mLastX;
    private float mLastY;
    private boolean mPointAdded = false;

    // save data
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private String userID;

    private double mTotalDistance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBarAndTitleBar();
        setContentView(R.layout.ar_activity_measure);

        mUndo = findViewById(R.id.fab_undo);
        mSave = findViewById(R.id.fab_save);
        mBack = findViewById(R.id.fab_back);
        mTextView = (TextView) findViewById(R.id.txt_dist);
        mSurfaceView = (GLSurfaceView) findViewById(R.id.gl_surface_view);

        // grab data from firebase realtime db
        user = FirebaseAuth.getInstance().getCurrentUser();
        // need to add the url as the default location is USA not SEA
        databaseReference = FirebaseDatabase.getInstance("https://lace-aid-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        userID = user.getUid();

        mUndo.setOnClickListener(v->{
            if (!mPoints.isEmpty()) {
                mPoints.remove(mPoints.size() - 1);
                mRenderer.removePoint();
                updateDistance();
            }
        });
        
        mSave.setOnClickListener(v->{
            if(mTotalDistance == 0) {
                Toast.makeText(MeasureActivity.this, "You haven't measured the length", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MeasureActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                databaseReference.child(userID).child("footLength").setValue(mTotalDistance);
            }
        });

        mBack.setOnClickListener(v->{
            onBackPressed();
        });

        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if (displayManager != null) {
            displayManager.registerDisplayListener(new DisplayManager.DisplayListener() {
                    @Override
                public void onDisplayAdded(int displayId) {
                }

                @Override
                public void onDisplayChanged(int displayId) {
                    synchronized (this) {
                        mRenderer.onDisplayChanged();
                    }
                }

                @Override
                public void onDisplayRemoved(int displayId) {
                }
            }, null);
        }

        mRenderer = new MainRenderer(this, new MainRenderer.RenderCallback() {
            @Override
            public void preRender() throws CameraNotAvailableException {
                if (mRenderer.isViewportChanged()) {
                    Display display = getWindowManager().getDefaultDisplay();
                    int displayRotation = display.getRotation();
                    mRenderer.updateSession(mSession, displayRotation);
                }

                mSession.setCameraTextureName(mRenderer.getTextureId());

                Frame frame = mSession.update();
                if (frame.hasDisplayGeometryChanged()) {
                    mRenderer.transformDisplayGeometry(frame);
                }

                PointCloud pointCloud = frame.acquirePointCloud();
                mRenderer.updatePointCloud(pointCloud);
                pointCloud.release();

                if (mPointAdded) {
                    if (mPoints.size() < 2) {
                        List<HitResult> results = frame.hitTest(mLastX, mLastY);
                        for (HitResult result : results) {
                            Pose pose = result.getHitPose();
                            float[] points = new float[]{pose.tx(), pose.ty(), pose.tz()};
                            mPoints.add(points);
                            mRenderer.addPoint(points);
                            updateDistance();
                            break;
                        }
                    }
                    mPointAdded = false;
                }

                Camera camera = frame.getCamera();
                float[] projMatrix = new float[16];
                camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f);
                float[] viewMatrix = new float[16];
                camera.getViewMatrix(viewMatrix, 0);

                mRenderer.setProjectionMatrix(projMatrix);
                mRenderer.updateViewMatrix(viewMatrix);
            }
        });
        mSurfaceView.setPreserveEGLContextOnPause(true);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setRenderer(mRenderer);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSurfaceView.onPause();
        mSession.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (mSession == null) {
                switch (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    case INSTALLED:
                        mSession = new Session(this);
                        Log.d(TAG, "ARCore Session created.");
                        break;
                    case INSTALL_REQUESTED:
                        mUserRequestedInstall = false;
                        Log.d(TAG, "ARCore should be installed.");
                        break;
                }
            }
        }
        catch (UnsupportedOperationException e) {
            Log.e(TAG, e.getMessage());
        } catch (UnavailableApkTooOldException | UnavailableDeviceNotCompatibleException | UnavailableUserDeclinedInstallationException | UnavailableArcoreNotInstalledException | UnavailableSdkTooOldException e) {
            e.printStackTrace();
        }

        mConfig = new Config(mSession);
        if (!mSession.isSupported(mConfig)) {
            Log.d(TAG, "This device is not support ARCore.");
        }
        mSession.configure(mConfig);
        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }

        mSurfaceView.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getX();
                mLastY = event.getY();
                mPointAdded = true;
                break;
        }
        return true;
    }

    public void updateDistance() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                double totalDistance = 0.0;
                if (mPoints.size() >= 2)  {
                    for (int i = 0; i < mPoints.size() - 1; i++) {
                        float[] start = mPoints.get(i);
                        float[] end = mPoints.get(i + 1);

                        double distance = Math.sqrt(
                                (start[0] - end[0]) * (start[0] - end[0])
                                        + (start[1] - end[1]) * (start[1] - end[1])
                                        + (start[2] - end[2]) * (start[2] - end[2]));
                        totalDistance += distance;
                    }
                }
                String distanceString = String.format(Locale.getDefault(), "%.1f", totalDistance * 100)
                        + " cm";
                mTextView.setText(distanceString);
                // this is to log the distance
                mTotalDistance = Math.round(totalDistance * 100.0 * 10.0) / 10.0;
            }
        });
    }

    private void hideStatusBarAndTitleBar(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
