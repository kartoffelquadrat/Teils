package eu.kartoffelquadrat.openglexplorer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


public class ScoresActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set to undecorated fullscreen
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Load basic hiScore UI
        setContentView(R.layout.activity_scores);

        // Update Scores in UI according to saved hiscores
        updateAllScores();
    }

    private void updateAllScores() {
        // Load highscores from shared preferences
        String[] scores = new String[5];
        for (int i = 0; i < scores.length; i++) {
            SharedPreferences sharedPref = getSharedPreferences("level", Context.MODE_PRIVATE);
            int defaultValue = Integer.MIN_VALUE;
            int hiScore = sharedPref.getInt("score" + i, defaultValue);
            scores[i] = (hiScore == Integer.MIN_VALUE ? "-" : Integer.toString(hiScore));
        }
        ((TextView) findViewById(R.id.scoreTutorial)).setText(scores[0]);
        ((TextView) findViewById(R.id.scoreEasy)).setText(scores[1]);
        ((TextView) findViewById(R.id.scoreNormal)).setText(scores[2]);
        ((TextView) findViewById(R.id.scoreTough)).setText(scores[3]);
        ((TextView) findViewById(R.id.scoreInsane)).setText(scores[4]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scores, menu);
        return true;
    }
}
