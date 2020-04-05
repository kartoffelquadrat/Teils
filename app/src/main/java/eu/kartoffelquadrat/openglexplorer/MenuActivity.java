package eu.kartoffelquadrat.openglexplorer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;


public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set to undecorated fullscreen
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_menu, menu);
        return true;
    }

    public void onLaunchGame(View v) {

        // Trigger haptic user feedback
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(10);

        // Also provide optical feedback by filling the triangle.
        ImageView playTriangle = (ImageView) findViewById(R.id.playTriangle);
        playTriangle.setImageResource(R.drawable.triangle_filled);

        // Then redirect to OpenGlActivity to start a new game
        Intent gameIntent = new Intent(MenuActivity.this, OpenGlActivity.class);
        startActivity(gameIntent);
    }

    public void onChangeLevel(View v) {

        // Trigger haptic user feedback
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(10);

        // Also provide optical feedback by filling the triangle.
        ImageView levelTriangle = (ImageView) findViewById(R.id.levelTriangle);
        levelTriangle.setImageResource(R.drawable.triangle_filled);

        // Then redirect to the Level-Activity
        Intent levelIntent = new Intent(MenuActivity.this, LevelActivity.class);
        startActivity(levelIntent);
    }

    public void onScores(View v) {

        // Trigger haptic user feedback
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(10);

        // Also provide optical feedback by filling the triangle.
        ImageView scoresTriangle = (ImageView) findViewById(R.id.scoresTriangle);
        scoresTriangle.setImageResource(R.drawable.triangle_filled);

        // Then redirect to the Scores-Activity
        Intent scoreIntent = new Intent(MenuActivity.this, ScoresActivity.class);
        startActivity(scoreIntent);
    }

    public void onAbout(View v) {

        // Trigger haptic user feedback
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(10);

        // Also provide optical feedback by filling the triangle.
        ImageView aboutTriangle = (ImageView) findViewById(R.id.aboutTriangle);
        aboutTriangle.setImageResource(R.drawable.triangle_filled);

        // Then redirect to the About-Activity
        Intent aboutIntent = new Intent(MenuActivity.this, AboutActivity.class);
        startActivity(aboutIntent);
    }

    public void onQuit(View v) {

        // Trigger haptic user feedback
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(10);

        // Also provide optical feedback by filling the triangle.
        ImageView quitTriangle = (ImageView) findViewById(R.id.quitTriangle);
        quitTriangle.setImageResource(R.drawable.triangle_filled);

        finishAffinity();
    }

    /**
     * Required to blank out the filled triangles again when user navigates back to this activity
     */
    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();

        ImageView playTriangle = (ImageView) findViewById(R.id.playTriangle);
        playTriangle.setImageResource(R.drawable.triangle);

        ImageView levelTriangle = (ImageView) findViewById(R.id.levelTriangle);
        levelTriangle.setImageResource(R.drawable.triangle);

        ImageView scoresTriangle = (ImageView) findViewById(R.id.scoresTriangle);
        scoresTriangle.setImageResource(R.drawable.triangle);

        ImageView aboutTriangle = (ImageView) findViewById(R.id.aboutTriangle);
        aboutTriangle.setImageResource(R.drawable.triangle);

        ImageView quitTriangle = (ImageView) findViewById(R.id.quitTriangle);
        quitTriangle.setImageResource(R.drawable.triangle);
    }
}
