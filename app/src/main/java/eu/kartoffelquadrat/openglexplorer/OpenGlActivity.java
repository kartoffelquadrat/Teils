package eu.kartoffelquadrat.openglexplorer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import eu.kartoffelquadrat.openglexplorer.model.Model;


public class OpenGlActivity extends Activity implements CountDowner {

    private RasterSurfaceView mGLView;

    // Animation duration in milliseconds
    public static final int ANIMATION_DURATION = 500;
    public static final String INITIAL_MESSAGE = "";

    private TextView countDownView;
    private int currentCounter;

    // local reference to gyro buffer - required to deactivate it of activity is left
    AzimuthPitchRollBuffer gyroBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Model model = generateModel();

        // Start buffering gyro sensor-values
        gyroBuffer = new AzimuthPitchRollBuffer(getApplicationContext());

        // set to undecorated fullscreen
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //hideNavigationBar();
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // The opengl content is not directly added to the activity, but as an element to the layout within the activity. This way we can additionally display textual overlays.
        mGLView = new RasterSurfaceView(this, model, (Vibrator) getSystemService(Context.VIBRATOR_SERVICE), gyroBuffer, this);
        setContentView(R.layout.activity_open_gl);

        // Add OpenGL View and textual overlay to empty layout.
        FrameLayout layout = (FrameLayout) findViewById(R.id.frameLayout);
        layout.addView(mGLView);

        // Initialize CountDown text field
        countDownView = new TextView(this);
        countDownView.setText(INITIAL_MESSAGE);
        countDownView.setPadding(0, 60, 0, 0);
        countDownView.setTextSize(100);
        countDownView.setGravity(Gravity.CENTER_HORIZONTAL);
        countDownView.setTextColor(Color.WHITE);
        layout.addView(countDownView);

        // Not that the views are loaded, trigger initial animation
        mGLView.launchInitialAnimation();
    }

    /**
     * Generates a Model matching the user's degree of difficulty
     */
    private Model generateModel() {

        // Look up user-selected difficulty (value 0-4, 0 being easiest, 4 hardest)
        SharedPreferences sharedPref = getSharedPreferences("level", Context.MODE_PRIVATE);
        int defaultValue = getResources().getInteger(R.integer.default_level);
        int selectedLevel = sharedPref.getInt(getString(R.string.selected_level), defaultValue);

        // A value between 0 and 1. One of the factors that sets the game difficulty: higher -> harder.
        double influenceProbability = 0.1 + (selectedLevel * 0.12);
        currentCounter = 12 + 16 - (selectedLevel * 4);

        // Create new Model, for the actual game data
        return new Model(influenceProbability);
    }

    @Override
    public void init() {
        countDownView.setText(Integer.toString(currentCounter));
    }

    @Override
    public void tickDown() {
        currentCounter--;
        countDownView.setText(Integer.toString(currentCounter));
    }

    @Override
    public boolean isOver() {
        return currentCounter == 0;
    }

    @Override
    public void persist()
    {
        // Find out current level
        SharedPreferences sharedPref = getSharedPreferences("level", Context.MODE_PRIVATE);
        int defaultValue = getResources().getInteger(R.integer.default_level);
        int selectedLevel = sharedPref.getInt(getString(R.string.selected_level), defaultValue);

        // Check the current best score for the current level (key for the hiscore "scoreX" where x is the level)
        int defaultHighScore = Integer.MIN_VALUE;
        String scoreId = "score"+selectedLevel;
        int currentHighScore = sharedPref.getInt(scoreId, defaultHighScore);

        // update highscore if countDown value is more than currentHighScore
        if(currentCounter > currentHighScore)
        {
            Toast.makeText(getApplicationContext(), "New High Score!",
                    Toast.LENGTH_SHORT).show();

            // Save user selection in shared prefs
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(scoreId, currentCounter);
            editor.commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        gyroBuffer.deactivate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gyroBuffer.reactivate();
    }
}
