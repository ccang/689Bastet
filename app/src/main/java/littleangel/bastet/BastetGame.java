package littleangel.bastet;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.preference.PreferenceManager;

public class BastetGame extends MainGame {

	static long linesCleared = 0; // The total number of lines cleared
	static long linesClearedFloor = 0;
	static double gravityAddPerLevel = Constants.GRAVITY_ADD_PER_LEVEL_DEFAULT;
	static double gravityMultiplyPerLevel = Constants.GRAVITY_MULTIPLY_PER_LEVEL_DEFAULT;
	static int linesPerLevel = Constants.LINES_PER_LEVEL_DEFAULT;
	static String dropSpeedMode = Constants.DROP_MODE_DEFAULT;
	static int startingLevel = 0;
	
	public BastetGame(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onTick() {
	}

	@Override
    // Drawing Line cleared text box
    //        levelInfoYStarting = squareSide * 10;
    // auxInfoXStarting = numSquaresX * squareSide - squareSide * 1 / 2;
    // auxInfoYStarting = squareSide * 12;
	public void printAuxText(Canvas canvas) {
        canvas.drawText(CURRENT_LINE, auxBoxXStarting + mainFieldShiftX, lineInfoYStarting + mainFieldShiftY, paint);
		canvas.drawText(CURRENT_LEVEL, auxBoxXStarting + mainFieldShiftX, LevelInfoYStarting + mainFieldShiftY, paint);
        canvas.drawText(REMAIN_LINE, auxBoxXStarting + mainFieldShiftX,  LevelInfoYStarting+squareSide*2 + mainFieldShiftY, paint);
		changePaintSettings("info");
        canvas.drawText("" + linesCleared, auxInfoXStarting, lineInfoYStarting + mainFieldShiftY, paint);
		canvas.drawText(auxText, auxInfoXStarting, LevelInfoYStarting + mainFieldShiftY, paint);
        canvas.drawText(""+ (linesCleared-linesClearedFloor) + " / " + linesPerLevel, auxInfoXStarting, LevelInfoYStarting +squareSide*2 + mainFieldShiftY, paint);
		changePaintSettings("normal");
	}

	@Override
	public long getHighScore(SharedPreferences settings, String hiScorePos) {
		return getLongFromSettings(0, hiScorePos, settings);
	}

	@Override
	public void onShapeLocked() {
		changeGravity();
	}

	public void changeGravity() {
		while (linesCleared >= linesClearedFloor + linesPerLevel) {
			if (level != Integer.MAX_VALUE - 1) {
				level = level + 1;
			}
			linesClearedFloor = linesClearedFloor + linesPerLevel;
			if (gravityAdd < 20) {
				modifyGravityAdd();
			}
		}
		auxText = "" + level;
	}
	
	public void modifyGravityAdd() {
		if (dropSpeedMode.equals(Constants.GEOMETRIC_DROP_MODE)) {
			gravityAdd = defaultGravity
					* (Math.pow(gravityMultiplyPerLevel, level) - 1);
		}
        else if (dropSpeedMode.equals(Constants.LINEAR_DROP_MODE)) {
			gravityAdd = level * gravityAddPerLevel;
		}
		if (gravityAdd > 20) {
			gravityAdd = 20;
		}
	}

	@Override
	public void updateHighScore() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		editHighScore(settings, linesCleared);
	}

	@Override
	public void onNewGame() {
		linesCleared = 0;
		linesClearedFloor = 0;
		level = startingLevel;
		auxText = "" + level;
		modifyGravityAdd();
	}

	@Override
	public void onScore(int currentDrop) {
		linesCleared = linesCleared + currentDrop;
	}

	@Override
	public void onGetSettings(SharedPreferences settings) {
		dropSpeedMode = settings.getString("dropSpeedMode", dropSpeedMode);
		if (dropSpeedMode.equals(Constants.GEOMETRIC_DROP_MODE)) {
			gravityMultiplyPerLevel = getDoubleFromSettings(gravityMultiplyPerLevel, "gravityMultiplyPerLevel",	settings);
		}
        else if (dropSpeedMode.equals(Constants.LINEAR_DROP_MODE)) {
			gravityAddPerLevel = getDoubleFromSettings(gravityAddPerLevel,"gravityAddPerLevel", settings);
		}
		linesPerLevel = getIntFromSettings(linesPerLevel, "linesPerLevel", settings);
		startingLevel = getIntFromSettings(startingLevel, "startingLevel", settings);
		startingLevel = Math.max(0, startingLevel);
		linesPerLevel = Math.max(1, linesPerLevel);
	}

}
