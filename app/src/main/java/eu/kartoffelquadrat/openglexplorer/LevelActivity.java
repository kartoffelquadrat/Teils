package eu.kartoffelquadrat.openglexplorer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import eu.kartoffelquadrat.openglexplorer.R;

public class LevelActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set to undecorated fullscreen
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_level);

        // Check for level defined by user, and highlight default level otherwise
        SharedPreferences sharedPref = getSharedPreferences("level", Context.MODE_PRIVATE);
        int defaultValue = getResources().getInteger(R.integer.default_level);
        int selectedLevel = sharedPref.getInt(getString(R.string.selected_level), defaultValue);
        highlightLevel(selectedLevel);
    }

    private void highlightLevel(int level) {
        // Deselect everything, to get rid of whatever is currently selected
        ImageView tutorialTriangle = (ImageView) findViewById(R.id.tutorialTriangle);
        tutorialTriangle.setImageResource(R.drawable.triangle);
        ImageView easyTriangle = (ImageView) findViewById(R.id.easyTriangle);
        easyTriangle.setImageResource(R.drawable.triangle);
        ImageView normalTriangle = (ImageView) findViewById(R.id.normalTriangle);
        normalTriangle.setImageResource(R.drawable.triangle);
        ImageView toughTriangle = (ImageView) findViewById(R.id.toughTriangle);
        toughTriangle.setImageResource(R.drawable.triangle);
        ImageView insaneTriangle = (ImageView) findViewById(R.id.insaneTriangle);
        insaneTriangle.setImageResource(R.drawable.triangle);

        // Select the user's custom choice
        switch (level) {
            case 0:
                tutorialTriangle.setImageResource(R.drawable.triangle_filled);
                break;
            case 1:
                easyTriangle.setImageResource(R.drawable.triangle_filled);
                break;
            case 2:
                normalTriangle.setImageResource(R.drawable.triangle_filled);
                break;
            case 3:
                toughTriangle.setImageResource(R.drawable.triangle_filled);
                break;
            case 4:
                insaneTriangle.setImageResource(R.drawable.triangle_filled);
                break;
            default:
                throw new RuntimeException("Invalid level: " + level);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_level, menu);
        return true;
    }

    private void changeLevel(int level) {
        // Give haptic feedback
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(10);

        // Update UI
        highlightLevel(level);

        // Save user selection in shared prefs
        SharedPreferences sharedPref = getSharedPreferences("level", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.selected_level), level);
        editor.commit();
    }

    public void onSetTutorial(View v) {
        changeLevel(0);
    }

    public void onSetEasy(View v) {
        changeLevel(1);
    }

    public void onSetNormal(View v) {
        changeLevel(2);
    }

    public void onSetTough(View v) {
        changeLevel(3);
    }

    public void onSetInsane(View v) {
        changeLevel(4);
    }
}
