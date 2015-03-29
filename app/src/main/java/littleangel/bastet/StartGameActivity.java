package littleangel.bastet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartGameActivity extends Activity {

	MainGame mainView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();

        if (MainGame.startNewGame) { // default is start new game
			MainGame.gameMode = intent.getStringExtra("gameMode");
		}
        mainView = new BastetGame(this);
		setContentView(mainView);
	}

    @Override
    protected void onPause() { // Activate this when the game view is about to quit, eg back button pressed
        super.onPause();
        if (!mainView.lose) { // If the game is not lost , then start the pause routines. Otherwise let it is.
            mainView.pauseGame(true);
        }
    }

}