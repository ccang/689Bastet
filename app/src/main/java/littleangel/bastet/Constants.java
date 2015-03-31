package littleangel.bastet;

import android.content.SharedPreferences;

public class Constants {
    // Hard-coded constants
    public final static String GAMEMODE1 = "OnePlayer";
    public final static String GEOMETRIC_DROP_MODE = "Exponential";
    public final static String LINEAR_DROP_MODE = "Linear";

    // Language Specifics
    // START ENGLISH
    public class EN {
        // For Main Screen
        public final static String NEW_GAME = "New Game";
        public final static String LOAD_GAME = "Return to game";
        // For Name-setting Dialog Box
        public final static String DIALOG_MESSAGE = "Please enter your name";
        public final static String DIALOG_SAVE = "Save";
        // Text displayed when clearing 1-4 lines
        public final static String SINGLE_TEXT = "Single";
        public final static String DOUBLE_TEXT = "Double";
        public final static String TRIPLE_TEXT = "Triple";
        public final static String TETRIS_TEXT = "Tetris";
        // Text displayed on right
        public final static String RECORD_HIGH = "High: ";
        public final static String CURRENT_SCORE = "Score: ";
        public final static String CURRENT_LINE = "Lines: ";
        public final static String CURRENT_LEVEL = "Level: ";
        public final static String REMAIN_LINE = "LevelUp: ";
        public final static String NEXT_BLOCK = "Won't give: ";
        public final static String COMBO = "Combo";
        // ENDGAME
        public final static String END_RECORD_1 = "New";
        public final static String END_RECORD_2 = "Record!";

    }   // END ENGLISH

    // START TRAD CHINESE
    public class ZH_HK {
       // For Main Screen
       public final static String NEW_GAME = "開始玩";
       public final static String LOAD_GAME = "回到遊戲";
       // For Name-setting Dialog Box
       public final static String DIALOG_MESSAGE = "請輸入你的名字";
       public final static String DIALOG_SAVE = "儲存";
       // Text displayed when clearing 1-4 lines
       public final static String SINGLE_TEXT = "消去1行";
       public final static String DOUBLE_TEXT = "消去2行";
       public final static String TRIPLE_TEXT = "消去3行";
       public final static String TETRIS_TEXT = "消去4行";
       // Text displayed on right
       public final static String RECORD_HIGH = "紀錄: ";
       public final static String CURRENT_SCORE = "分數: ";
       public final static String CURRENT_LINE = "行數: ";
       public final static String CURRENT_LEVEL = "難度: ";
       public final static String REMAIN_LINE = "升級: ";
       public final static String NEXT_BLOCK = "死都唔俾你: ";
       public final static String COMBO = "連續消去";
       // ENDGAME
       public final static String END_RECORD_1 = "恭喜!";
       public final static String END_RECORD_2 = "新紀錄!";
    }  // End Trad Chinese


    // Main screen setting the spinner
    public final static String DEFAULT_START_LEVEL = "5";
    // Highscore screen
    public final static String DEFAULT_HIGHSCORE_NAME="banghead";
    // For drawing game area
    public final static double GHOST_SHAPE_MARGIN = 0.15;
    // Initializing the game settings
    public final static int[][] LEVEL_SHAPE_PROBABILITY =
           {{45,20,15,10,9,1,0},
            {50,20,15,10,5,0,0},
            {55,20,15,9,1,0,0},
            {60,20,15,5,0,0,0},
            {65,20,14,1,0,0,0},
            {70,23,7,0,0,0,0},
            {75,24,1,0,0,0,0}};
    public final static String DROP_MODE_DEFAULT = GEOMETRIC_DROP_MODE;
	public final static int SLACK_LENGTH_DEFAULT = 1000;
	public final static int DRAG_SENSITIVITY_DEFAULT = 60;
	public final static int LINES_PER_LEVEL_DEFAULT = 7;
	public final static double DEFAULT_GRAVITY_DEFAULT = 0.033;
	public final static double SOFT_DROP_SPEED_DEFAULT = 1.1;
	public final static double GRAVITY_ADD_PER_LEVEL_DEFAULT = 0.025;
	public final static double GRAVITY_MULTIPLY_PER_LEVEL_DEFAULT = 1.38;
    // misc constants
	public final static int[][] kickX = { { 0, -1, -1, 0, -1 },
			{ 0, 1, 1, 0, 1 }, { 0, 1, 1, 0, 1 }, { 0, -1, -1, 0, -1 } };
	public final static int[][] kickY = { { 0, 0, 1, -2, -2 },
			{ 0, 0, -1, 2, 2 }, { 0, 0, 1, -2, -2 }, { 0, 0, -1, 2, 2 } };

	public final static int[][] iBlockkickX = { { 0, -2, 1, -2, 1 },
			{ 0, -1, 2, -1, 2 }, { 0, 2, -1, 2, -1 }, { 0, 1, -2, 1, -2 } };
	public final static int[][] iBlockkickY = { { 0, 0, 0, -1, 2 },
			{ 0, 0, 0, 2, -1 }, { 0, 0, 0, 1, -2 }, { 0, 0, 0, -2, 1 } };

    // Set Theme
    public final static String LIGHT_THEME = "Light";
    public final static String DARK_THEME = "Dark";
    // Function for setting Light/Dark theme
	public static int getTheme(SharedPreferences settings) {
		String theme = settings.getString("theme", Constants.DARK_THEME);
		if (theme.equals(LIGHT_THEME)) {
			return R.style.LightTheme;
		} else if (theme.equals(DARK_THEME)) {
			return R.style.DarkTheme;
		}
		return R.style.LightTheme;
	}
}
