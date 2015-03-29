package littleangel.bastet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {

    // language specific variables
    private static String NEW_GAME;
    private static String LOAD_GAME;
    private static String DIALOG_MESSAGE;
    private static String DIALOG_SAVE;
    // other variables
    public String level_str=Constants.DEFAULT_START_LEVEL;
    // Widget handling variables
    public Spinner level_spinner;
    public ArrayAdapter adapter01;
    public Button buttonGame, buttonHigh, buttonChangeName, buttonAbout;
    public Button high_back, high_reset;
    public int tap_reset_count;
    public TextView[] hiscorevText = new TextView[3];
    public TextView[] hiscorenText = new TextView[3];
    public TextView[] hiscorelText = new TextView[3];
    // variable to access stored preferences
    SharedPreferences settings;

    // Entry point of the entire app as defined by AndroidManifest
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        // get the language
        if (true) { // now only ZH_HK availble. Future: extend this to EN or even ZH_CN
            NEW_GAME = Constants.ZH_HK.NEW_GAME;
            LOAD_GAME = Constants.ZH_HK.LOAD_GAME;
            DIALOG_MESSAGE = Constants.ZH_HK.DIALOG_MESSAGE;
            DIALOG_SAVE = Constants.ZH_HK.DIALOG_SAVE;
        }
        jumpToLayoutMain(); // to Show the main menu
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set the appropriate text of buttonGame (start/load button)
        // Put here again because this activity may be loaded new or loaded after quitting the game activity
        upDateButton();
    }

    // Suppress the options menu (set to show no content)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Fix the screen configuration to be Portrait
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        try {
            super.onConfigurationChanged(newConfig);
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                // land
                // set to portrait
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                // port
            }
        }
        catch (Exception ex) {
        }
    }

    private void upDateButton() {// Update the start/load button's text
        if (MainGame.startNewGame) {
            buttonGame.setText(NEW_GAME);
        } else {
            buttonGame.setText(LOAD_GAME);
        }
    }

    // Show the main layout (activity_main) and respond to actions
    public void jumpToLayoutMain() {
        setContentView(R.layout.activity_main);
        level_spinner = (Spinner) findViewById(R.id.level_spinner); // Level Chooser
        buttonGame = (Button) findViewById(R.id.buttonGame); // Start
        buttonHigh = (Button) findViewById(R.id.buttonHigh); // Highscore
        buttonChangeName = (Button) findViewById(R.id.buttonChangeName);
        buttonAbout = (Button) findViewById(R.id.buttonAbout); // About

        upDateButton(); // Update the start/load button's text

        //Setup the content of adapter for spinner
        adapter01 = ArrayAdapter.createFromResource(this, R.array.level_spinner, R.layout.spinner_main);
        // Setup the style of adapter
        adapter01.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Adapt the adapter to the level spinner
        level_spinner.setAdapter(adapter01);
        //set the default spinner value
        int spinnerPosition = adapter01.getPosition(level_str);
        level_spinner.setSelection(spinnerPosition);
        // setup the event handler for the spinner
        level_spinner.setOnItemSelectedListener(new SpinnerXMLSelectedListener());
        // let the spinner be visible
        level_spinner.setVisibility(View.VISIBLE);

        // Setup the event handlers for the Buttons in the main screen
        buttonGame.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                jumpToLayoutGame();
            }
        });
        buttonHigh.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                jumpToLayoutHigh();
            }
        });
        buttonChangeName.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                showNameDialog();
            }
        });
        buttonAbout.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                jumpToLayoutAbout();
            }
        });

    }
    // Layout Main: Get the value from the Spinner of Level Selection
    public class SpinnerXMLSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            // Extract the level from spinner
            level_str = level_spinner.getSelectedItem().toString();
            // Write the selected level to the shared preferences
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("startingLevel", level_str);
            // actual write
            editor.commit();
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    // Responsible for showing the name-changing dialog
    private void showNameDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);

        // set dialog title
        alertDialogBuilder.setTitle(DIALOG_MESSAGE);
        // Load the saved username to the textbox
        input.setText(settings.getString("PlayerName", ""));
        // Set max length = 15
        input.addTextChangedListener(new MagicTextLengthWatcher(15));
        // input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(15)});
        // Set up the input area
        alertDialogBuilder.setView(input);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(DIALOG_SAVE, new DialogInterface.OnClickListener() {
                    // if this button is clicked
                    public void onClick(DialogInterface dialog, int id) {
                        // Write the inputted name to the shared preferences
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("PlayerName", input.getText().toString());
                        // actual write
                        editor.commit();
                        // close the dialog
                        dialog.cancel();
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    // Show the gaming layout (game) and respond to actions
    public void jumpToLayoutGame() {
        Intent intent = new Intent(this, StartGameActivity.class);
        if (MainGame.startNewGame){ // default is true
            intent.putExtra("gameMode", Constants.GAMEMODE1);
        } /*else { // if there is a paused game
            // insert any additional paramters here
        }*/
        startActivity(intent);
    }

    // Show the "About" layout
    public void jumpToLayoutAbout() {
        setContentView(R.layout.about);
        high_back = (Button) findViewById(R.id.about_back);
        // Setup the event handler for the back button
        high_back.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                jumpToLayoutMain();
            }
        });
    }

   // Show the highscore layout (high) and respond to actions
    public void jumpToLayoutHigh() {
        setContentView(R.layout.high);
        // Assign the object handlers with the buttons
        high_back = (Button) findViewById(R.id.high_back);
        high_reset = (Button) findViewById(R.id.high_reset);
        // Print out the highscores
        showHighScore();
        // Setup the event handler for the highscore reset button
        high_reset.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                if (tap_reset_count<2) { // user need to tap more
                    tap_reset_count++;
                } else { // reset highscore
                    // Write to the shared preferences
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("HISCORE1V", "20");
                    editor.putString("HISCORE2V", "10");
                    editor.putString("HISCORE3V", "0");
                    editor.putString("HISCORE1L", "0");
                    editor.putString("HISCORE2L", "0");
                    editor.putString("HISCORE3L", "0");
                    editor.putString("HISCORE1N", "banghead");
                    editor.putString("HISCORE2N", "banghead");
                    editor.putString("HISCORE3N", "banghead");
                    editor.commit();
                    tap_reset_count=0;
                    // Re-print the highscores
                    showHighScore();
                }
            }
        });
        // Setup the event handler for the back button
        high_back.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                jumpToLayoutMain();
            }
        });
    }

    private void showHighScore() {
        // Assign to the text areas in high.xml
        hiscorevText[0] = (TextView) findViewById(R.id.hiscore1v);
        hiscorevText[1] = (TextView) findViewById(R.id.hiscore2v);
        hiscorevText[2] = (TextView) findViewById(R.id.hiscore3v);
        hiscorenText[0] = (TextView) findViewById(R.id.hiscore1n);
        hiscorenText[1] = (TextView) findViewById(R.id.hiscore2n);
        hiscorenText[2] = (TextView) findViewById(R.id.hiscore3n);
        hiscorelText[0] = (TextView) findViewById(R.id.hiscore1l);
        hiscorelText[1] = (TextView) findViewById(R.id.hiscore2l);
        hiscorelText[2] = (TextView) findViewById(R.id.hiscore3l);
        // Extract the highscore informations from Default Settings
        String hiscoren[] = new String[3];
        String hiscorev[] = new String[3];
        String hiscorel[] = new String[3];
        // load the detail highscore table
        hiscorev[0] = settings.getString("HISCORE1V", "");
        hiscorev[1] = settings.getString("HISCORE2V", "");
        hiscorev[2] = settings.getString("HISCORE3V", "");
        hiscorel[0] = settings.getString("HISCORE1L", "");
        hiscorel[1] = settings.getString("HISCORE2L", "");
        hiscorel[2] = settings.getString( "HISCORE3L", "");
        hiscoren[0] = settings.getString("HISCORE1N", "");
        hiscoren[1] = settings.getString("HISCORE2N", "");
        hiscoren[2] = settings.getString("HISCORE3N", "");
        // Print out the highscores
        for (int i=0; i<3; i++) {
            hiscorevText[i].setText(hiscorev[i]);
            hiscorenText[i].setText(hiscoren[i]);
            hiscorelText[i].setText(hiscorel[i]);
        }
    }
}

