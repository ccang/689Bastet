package littleangel.bastet;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;

public abstract class MainGame extends View {
    // language specifics
    public static String singleText;
    public static String doubleText;
    public static String tripleText;
    public static String tetrisText;
    public static String RECORD_HIGH;
    public static String CURRENT_SCORE;
    public static String CURRENT_LINE;
    public static String CURRENT_LEVEL;
    public static String REMAIN_LINE;
    public static String NEXT_BLOCK;
    public static String COMBO;
    public static String END_RECORD_1;
    public static String END_RECORD_2;

	Paint paint = new Paint();
    private int exit_tap=0;
    // FOR DEBUG
    double[] DebugScore = {0,0,0,0,0,0,0};
    int debugprobability;

    // INTERNAL OPTIONS:
    static final int numSquaresX = 16; // Total number of columns
	static final int numSquaresY = 22; // total number of rows
	static final double textScaleSize = 0.8; // Text scaling
	static final double textScaleSizeAux = 0.7; // Text scaling for auxiliary text
	static final int FPS = 1000000000 / 30; // The nanoseconds per frame at which the game
	static int ORANGE; // Store the color 'orange'
	static Context mContext;

	// EXTERNAL OPTIONS:
	public static double defaultGravity = Constants.DEFAULT_GRAVITY_DEFAULT;	// The default gravity of the game value
	public static long slackLength = Constants.SLACK_LENGTH_DEFAULT * 1000000; // The Slack time in in milliseconds
	public static double softDropSpeed = Constants.SOFT_DROP_SPEED_DEFAULT; // How fast soft dropping is
	public static int dragSensitivity = Constants.DRAG_SENSITIVITY_DEFAULT; // The sensitivity of the drag gesture
	public static int textColor = Color.BLACK;

	// GAME STATICS
	static final int numberOfBlocksWidth = 10; // The number of columns of blocks in the main field
	static final int numberOfBlocksLength = 22; // The number of rows of blocks in the main field
	static final int auxBoxWidth = 4; // The width of the auxiliary boxes
	static final int auxBoxLength = 2; // The length of the auxiliary boxes

	// Game Mode
	public static String gameMode = "";

	// TIMERS:
	static long clock = System.nanoTime();
	static long slackTime = slackLength; // Tracks the real time for fps

	// POSITIONING VARIABLES:
	static int mainFieldShiftX; // How much the screen is shifted to the right
	static int mainFieldShiftY; // How much the screen is shifted downwards
    static int mainFieldStartingY; // At what Y-value do the vertical lines start at
	static int squareSide; // The size of one square
	static int auxBoxXStarting; // Where the aux boxes starts (x)
	static int nextShapeYStarting; // Where the first next box starts (y)
	static int clearInfoYStarting; // Where the clear info text starts (y)
	static int scoreInfoYStarting; // Where the score box starts (y)
    static int lineInfoYStarting; // Where the line cleared box starts (y)
	static int nextTextYStarting; // Where the "Next: " text starts (y)
	static int auxInfoXStarting; // Where the aux box starts (x)
	static int LevelInfoYStarting; // Where the aux box starts (y)
	static int highScoreYStarting; // Where the highscore text box starts(y)

	// SHAPE INFORMATION:
	static int nextShape = -1; // The NEXT shape on the playing field.
    static int currentShape = -1; // The CURRENT shape on the playing field.
    static int notGivenShape = -1; // The best shape that should not be given
	static int currentRotation = 0; // The current rotation 0 = spawn, 1 = R, 2 = 2, 3 = L;

	// FIELD INFORMATION:
	static int[][] blocks = new int[numberOfBlocksWidth][numberOfBlocksLength];
	// Array detailing the type of block in each square
    static int[][] testBlocks = new int[numberOfBlocksWidth][numberOfBlocksLength];
    // Array that shadowing the actual blocks[][] for next brick selection
	static int[][] colors = new int[numberOfBlocksWidth][numberOfBlocksLength];
	// Array detailing the colour in each square
	static int[][] nextBlocks = new int[auxBoxWidth][auxBoxLength];
    // Array detailing the next block
    static int[] playLocationX = new int[4]; // X-coords of the blocks in play
	static int[] playLocationY = new int[4]; // Y-coords of the blocks in play
    static int[] testPlayLocationX = new int[4]; // X-coords of the blocks in testing field
    static int[] testPlayLocationY = new int[4]; // Y-coords of the blocks in testing field
	static double pivotX; // Rotation Pivot: X-coordinate
	static double pivotY; // Rotation Pivot: Y-coordinate

	// MISC VARIABLES:
	static String auxText = ""; // Text for displaying the time left
	static boolean slack = false; // Whether or not slack is currently active
	public static boolean pause = false;
	// Whether or not the game is currently paused
	static boolean lose = false; // Whether or not the game is still in progress
	static boolean slackOnce = false;
	// Whether or not slack as already been activated
	static boolean turnSuccess = false; // Whether or not a turn was successful
	static boolean softDrop = false; // Whether or not softdropping is active
	static long score = 0; // The current score
	static long highScore = 0; // The highscore of the current gamemode
    static long highScoreLimit = 0; // The lowest highscore that should be beaten
	public static int level = 0; // The current level
	static String lastMove = "Nothing";
	static int combo = 0; // The current combo
	static double gravity = defaultGravity; // The current base gravity
	static double gravityAdd = 0; // The amount of gravity to add onto the base
	static double gravityTicker = 0; // The current gravity ticker
	static boolean getScreenSize = false; 	// Initial getting screen size variable
	public static boolean startNewGame = true; 	// Whether it should be a new game or not
	static ArrayList<String> clearInfo = new ArrayList<String>(); // Scoring information for clearing lines

	// Blocks Data:
	// 0 = empty space
	// 1 = active space
	// 2 = locked space
	// 3 = ghost space

	// Loading the game
	public MainGame(Context context) {
		super(context);
		mContext = context;
		// Setting the orange color since there is no default
		ORANGE = getResources().getColor(R.color.orange);
		// Create the object to receive touch input
		setOnTouchListener(getOnTouchListener());
		if (startNewGame) {
			newGame();
		} else {
			getSettings();
		}
		gravity = defaultGravity;
	}

	public abstract void onTick();

	public abstract void onShapeLocked();

	public abstract void onNewGame();

	public abstract void onScore(int currentDrop);

	public abstract void onGetSettings(SharedPreferences settings);

	public void tick() {
		if (!lose && !pause) {
			long temp = System.nanoTime();
			long dtime = temp - clock;
			if (dtime > FPS) {
				clock = clock + FPS;
				onTick();
				slack();
				gravity();
			}
			coloring();
			ghostShape();
		}
	}

	public abstract void printAuxText(Canvas canvas);

	public abstract long getHighScore(SharedPreferences settings, String highScorePos);

	@Override
	public void onDraw(Canvas canvas) {

		// Get the screen size and adjust the game screen proportionally
		if (getScreenSize) {
			int width = this.getMeasuredWidth();
			int height = this.getMeasuredHeight();
			getLayout(width, height);
			paint.setTextSize((float) (squareSide * textScaleSize));
		}

		paint.setColor(textColor);
		paint.setTextSize((float) (squareSide * textScaleSizeAux));

        // Drawing "High Score: " text box
        canvas.drawText(RECORD_HIGH, auxBoxXStarting + mainFieldShiftX, highScoreYStarting + mainFieldShiftY, paint);
        changePaintSettings("info");
        canvas.drawText(""+highScore, auxInfoXStarting, highScoreYStarting + mainFieldShiftY, paint);
        changePaintSettings("normal");

		// Drawing "Next: " text box
		canvas.drawText(NEXT_BLOCK, auxBoxXStarting + mainFieldShiftX*2, nextTextYStarting + mainFieldShiftY, paint);

		// Drawing Score text box
		canvas.drawText(CURRENT_SCORE, auxBoxXStarting + mainFieldShiftX, scoreInfoYStarting + mainFieldShiftY, paint);
		changePaintSettings("info");
		canvas.drawText("" + score, auxInfoXStarting, scoreInfoYStarting + mainFieldShiftY, paint);
		changePaintSettings("normal");

        // Drawing Level text box
		printAuxText(canvas);

		// Drawing clearInfo text box
		for (int xx = 0; xx < clearInfo.size(); xx++) {
			canvas.drawText(clearInfo.get(xx), auxBoxXStarting + mainFieldShiftX, clearInfoYStarting+mainFieldShiftY+squareSide * xx, paint);
		}

        // Output the DEBUG information
        // (0,1,2,3,4,5,6) = (I, J, L, T, Z, S, O)
        paint.setTextSize((float) (squareSide * textScaleSize*0.7));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        canvas.drawText("I" + nf.format(DebugScore[0]) + "; J" + nf.format(DebugScore[1]) +
                        "; L" + nf.format(DebugScore[2]) + "; T" + nf.format(DebugScore[3]) +
                        "; Z" + nf.format(DebugScore[4]) + "; S" + nf.format(DebugScore[5]) + "; O" + nf.format(DebugScore[6]),
                mainFieldShiftX, (numberOfBlocksLength-1)*squareSide+ mainFieldShiftY , paint);
        canvas.drawText("P"+debugprobability, auxBoxXStarting + mainFieldShiftX, (numberOfBlocksLength)*squareSide, paint);
        paint.setTextSize((float) (squareSide * textScaleSize));

		tick();

		// Drawing columns for the main field

		for (int xx = mainFieldShiftX; xx <= squareSide * numberOfBlocksWidth	+ mainFieldShiftX; xx += squareSide) {
            // First and Last columns stick out
            if ((xx == mainFieldShiftX) || (xx==squareSide * numberOfBlocksWidth	+ mainFieldShiftX)) {
                canvas.drawLine(xx, 0, xx, squareSide	* (numberOfBlocksLength - 2) + mainFieldShiftY, paint);
            } else {
                canvas.drawLine(xx, mainFieldStartingY, xx, squareSide * (numberOfBlocksLength - 2) + mainFieldShiftY, paint);
            }
		}

		// Drawing rows for the main field
		for (int xx = mainFieldShiftY; xx <= squareSide * (numberOfBlocksLength - 2) + mainFieldShiftY; xx += squareSide) {
			canvas.drawLine(mainFieldShiftX, xx, squareSide	* numberOfBlocksWidth + mainFieldShiftX, xx, paint);
		}

		// Coloring the main field
		for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
			for (int yy = 0; yy < numberOfBlocksLength; yy++) {
                // if that block is part of a falling piece of locked pieces
				if (blocks[xx][yy] != 0 & blocks[xx][yy] != 3) {
					paint.setColor(chooseColor(colors[xx][yy]));
					canvas.drawRect(xx * squareSide + mainFieldShiftX,
                            (yy - 2)* squareSide + mainFieldShiftY,
                            xx * squareSide + squareSide + mainFieldShiftX,
                            (yy - 2)* squareSide + squareSide + mainFieldShiftY, paint);
				}
                // if that block is part of ghost shape, the shadow of falling block,
                if (blocks[xx][yy] == 3) {
                    paint.setColor(Color.GRAY);
                    canvas.drawRect( (int) (xx*squareSide + mainFieldShiftX + squareSide*Constants.GHOST_SHAPE_MARGIN),
                            (int) ((yy-2)*squareSide + mainFieldShiftY + squareSide*Constants.GHOST_SHAPE_MARGIN),
                            (int) (xx*squareSide + mainFieldShiftX + squareSide*(1-Constants.GHOST_SHAPE_MARGIN/2)),
                            (int) ((yy-2)*squareSide + mainFieldShiftY + squareSide*(1-Constants.GHOST_SHAPE_MARGIN/2)), paint);
                }
			}
		}

        // Drawing the 'next' block on the 'next' block
		drawBoxShape(canvas, notGivenShape, nextBlocks, auxBoxXStarting+(int)(squareSide*0.6)+mainFieldShiftX, nextShapeYStarting+mainFieldShiftY);

		// Displaying the text or graphic to indicate game status if needed
		// When the game is lost
		// When the game is paused
		if (lose || pause) {
			changePaintSettings("big on");
			int length = this.getMeasuredHeight();
			int width = this.getMeasuredWidth();
			if (lose) {
                if (score > highScoreLimit) { // Highscore!
                    paint.setColor(Color.GREEN);
                    canvas.drawText(END_RECORD_1, width/2, mainFieldShiftY + length/3, paint);
                    canvas.drawText(END_RECORD_2, width/2, mainFieldShiftY + length*2/3, paint);
                } else { // normal Game Over
                    // Change the font settings
                    paint.setColor(Color.GRAY);
                    // Display and align the needed text
                    canvas.drawText("GAME", width/2, mainFieldShiftY + length/3, paint);
                    canvas.drawText("OVER", width/2, mainFieldShiftY + length*2/3, paint);
                    // Revert text settings to normal
                }
			} else if (pause) {
				// Change the font settings
				paint.setColor(textColor);
				// Display and align the needed text
				canvas.drawText("PAUSED", width / 2, mainFieldShiftY + length/ 2, paint);
				// Revert text settings to normal
			}
			changePaintSettings("big off");
		}
        invalidate();
	}

    // Draw the next block on the 'next' box
	public void drawBoxShape(Canvas canvas, int targetShape, int[][] targetBlocks, int shiftX, int shiftY) {
		paint.setColor(chooseColor(targetShape));
		for (int xx = 0; xx < auxBoxWidth; xx++) {
			for (int yy = 0; yy < auxBoxLength; yy++) {
				if (targetBlocks[xx][yy] == 1) {
					canvas.drawRect(xx*squareSide+shiftX, yy*squareSide+shiftY, (xx+1)*squareSide+shiftX, (yy+1)*squareSide+shiftY, paint);
				}
			}
		}
	}

	public void changePaintSettings(String setting) {
		if (setting.equals("info")) {
			paint.setTextSize((float) (squareSide * textScaleSize));
			paint.setTextAlign(Paint.Align.RIGHT);
			paint.setTypeface(Typeface.DEFAULT_BOLD);
		} else if (setting.equals("normal")) {
			paint.setTextAlign(Paint.Align.LEFT);
			paint.setTypeface(Typeface.DEFAULT);
			paint.setTextSize((float) (squareSide * textScaleSizeAux));
		} else if (setting.equals("big on")) {
			paint.setTextSize((float) (squareSide * 4));
			paint.setShadowLayer((float) 5, 0, 0, Color.BLACK);
			paint.setTextAlign(Paint.Align.CENTER);
		} else if (setting.equals("big off")) {
			paint.setShadowLayer((float) 0, 0, 0, Color.BLACK);
			paint.setTextSize((float) (squareSide * textScaleSize));
			paint.setTextAlign(Paint.Align.LEFT);
		}
	}

	public void getSettings() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		onGetSettings(settings);
		if (startNewGame) {
			getScreenSize = true;
		}
		highScore = getHighScore(settings, "HISCORE1V");
        highScoreLimit = getHighScore(settings, "HISCORE3V");

		// Get the theme to set the textcolor
		int theme = Constants.getTheme(settings);
		if (theme == R.style.LightTheme) {
		    textColor = Color.BLACK;
            setBackgroundColor(Color.WHITE);
		} else if (theme == R.style.DarkTheme) {
		 	textColor = Color.WHITE;
            setBackgroundColor(Color.BLACK);
		}
	}

	public int getIntFromSettings(int variable, String text,
			SharedPreferences settings) {
		try {
			return Integer.parseInt(settings.getString(text,
					String.valueOf(variable)));
		} catch (Exception e) {
			System.err.println("Error getting " + text
					+ ". Reverting to default value.");
		}
		return variable;
	}

	public double getDoubleFromSettings(double variable, String text,
			SharedPreferences settings) {
		try {
			return Double.parseDouble(settings.getString(text,
					String.valueOf(variable)));
		} catch (Exception e) {
			System.err.println("Error getting " + text
					+ ". Reverting to default value.");
		}
		return variable;
	}

	public long getLongFromSettings(long variable, String text,
			SharedPreferences settings) {
		try {
			return Long.parseLong(settings.getString(text,
					String.valueOf(variable)));
		} catch (Exception e) {
			System.err.println("Error getting " + text
					+ ". Reverting to default value.");
		}
		return variable;
	}

	// Generates new minos.
	// Follows the 7-bag system, which means that there will be ALL the blocks..
	// ...found in each "bag" of 7 pieces.
    // (0,1,2,3,4,5,6) = (I, J, L, T, Z, S, O)
	public void pickShape() {
        if (!lose) {
            bastetShape();
            // Transfer the Shape ID to actual shape on play area
            updateNewShape(currentShape);
            // Transfer the Shape ID to actual shape on 'next'
			updateBoxShape(nextBlocks, notGivenShape);
			slackOnce = false;
			shapeDown();
		}
	}

    private void bastetShape() {
        int[] testHeight= new int[numberOfBlocksWidth]; // the calculated height of each column in the test field
        int totalTestHeight, testBump,testClearLine, testHole; // indice for evaluation of the test field
        int rotationMax; // number of maximum rotation trial for each shape's evaluation
        double[] shapeScore= {0,0,0,0,0,0,0}; // score for a particular shape. Remember (0,1,2,3,4,5,6) = (I, J, L, T, Z, S, O)
        double[][] shapeProbability= Constants.LEVEL_SHAPE_PROBABILITY; // hard-coded probability according to sorted shapeScore.
        int probability=(int)(Math.random()*100)+1; // a lucky draw of blocks, biased by the weight defined above
        double tmpvalue; // for bubble sort
        int tmpid;// for bubble sort
        int[] shape_id = {0,1,2,3,4,5,6}; // for matching the shapeProbability
        int trial=0; // number of trial falling of blocks in the test field

        // find the number of holes in the current playing field
        int origHoles=0;
        for (int yy = 0; yy < numberOfBlocksLength - 1; yy++) {
            for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
                if ((blocks[xx][yy] != 0) && (blocks[xx][yy + 1] == 0)) {
                    if ((xx>0) && (blocks[xx-1][yy] != 0)) {
                        origHoles++;
                    }
                    if ((xx<(numberOfBlocksWidth-1)) && (blocks[xx+1][yy] != 0)) {
                        origHoles++;
                    }
                    origHoles++;
                }
            }
        }
        // try to drop every shape in every possible position to see the results
        // iterate through each shape
        for (int shapeID=0; shapeID<7; shapeID++) {
            trial=0;
            // set the max rotation trial for the shape being tested
            if (shapeID == 0 || shapeID == 4 || shapeID == 5) {// I, Z and S: only need to evaulate 2 rotation positions
                rotationMax = 2;
            } else if (shapeID == 6) { // O shape need only 1 position
                rotationMax = 1;
            } else {
                rotationMax = 4; // J, L and T shapes
            }

            // iterate through every positions
            for (int droppos = 0; droppos < numberOfBlocksWidth; droppos++) {
                // iterate through every rotations
                for (int rotationID = 1; rotationID <= rotationMax; rotationID++) {
                    // copy the playing field into a testing field
                    for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
                        System.arraycopy(blocks[xx], 0, testBlocks[xx], 0, numberOfBlocksLength);
                    }
                    // try to place the shape on the top of each line. if the shape extended beyond the x-width, then it is an illegal placement and ignored.
                    if (testNewShape(shapeID, rotationID, droppos)) {
                        testShapeDown(); // move down the shape in testing field
                        // Start the indice scoring: (1) Aggregate Height (2) Complete Lines (3)  Holes (4) Bumpiness
                        // (1) and (4) implemented by array testheight, (2) by scanning the testblocks for full lines; (3) holes by scanning the testblock for holes
                        // Reference: https://codemyroad.wordpress.com/2013/04/14/tetris-ai-the-near-perfect-player/

                        // Calculate indice (2) and clear the filled lines
                        testClearLine = clearLines(testBlocks);

                        // Calculate the heights for each column, count from the top, because there may be holes in the middle
                        for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
                            testHeight[xx] = numberOfBlocksLength;
                            for (int yy = 0; yy < numberOfBlocksLength; yy++) {
                                if (testBlocks[xx][yy] == 0) {
                                    testHeight[xx]--;
                                } else {
                                    break;
                                }
                            }
                        }
                        // Calculate indice (1)
                        totalTestHeight = 0;
                        for (int i = 0; i < numberOfBlocksWidth; i++) {
                            totalTestHeight += testHeight[i];
                        }
                        // Calculate indice (4)
                        testBump = 0;
                        for (int i = 0; i < numberOfBlocksWidth - 1; i++) {
                            testBump += Math.abs(testHeight[i] - testHeight[i + 1]);
                        }

                        // Calculate indice (3)
                        testHole = 0;
                        for (int yy = 0; yy < numberOfBlocksLength - 1; yy++) {
                            for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
                                if ((testBlocks[xx][yy] != 0) && (testBlocks[xx][yy + 1] == 0)) {
                                    if ((xx>0) && (testBlocks[xx-1][yy] != 0)) {
                                        testHole++;
                                    }
                                    if ((xx<(numberOfBlocksWidth-1)) && (testBlocks[xx+1][yy] != 0)) {
                                        testHole++;
                                    }
                                    testHole++;
                                }
                            }
                        }
                        // adjust the score of the shape after this test
                        shapeScore[shapeID] += ((-0.1) * totalTestHeight + 8000 * testClearLine + (-2) * (testHole-origHoles) + (-0.1) * testBump);
                        trial++;
                    } // End IF
                } // end interation of rotations
            } // end interation of position
            shapeScore[shapeID] /= trial; // calculate the mean score since the testing times of every shape is different
        } // end interation of shapes

        // FOR DEBUG
        System.arraycopy(shapeScore, 0, DebugScore, 0, 7);
        debugprobability=probability;

        // sort the shapeScore from low to high.
        for (int i=1; i<7; i++) {
            tmpvalue = shapeScore[i];
            tmpid=shape_id[i];
            int j = i;
            while ((j > 0) && (shapeScore[j - 1] > tmpvalue)) {
                shapeScore[j] = shapeScore[j - 1];
                shape_id[j] = shape_id[j - 1];
                j = j - 1;
            }
            shapeScore[j] = tmpvalue;
            shape_id[j] =tmpid;
        }
        // determine the output shapes
        int p=0;
        int l=0;
        if (level<6) {l=level;} else {l=6;}
        while (probability>0) {
            probability-=shapeProbability[l][p];
            p++;
        }
        currentShape =shape_id[p-1];
        notGivenShape=shape_id[6];
    }

    private boolean testNewShape(int thisShape, int thisRotation, int xx) {
        boolean success=false;
        if (thisShape == 0) {// Block I: straight vertical
            if (thisRotation == 1) {
                if ((3+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx, 1 + xx, 2 + xx, 3 + xx}, new int[]{0, 0, 0, 0})) {
                        success = true;
                    }
                }
            } else if (thisRotation == 2) {
                if (placeTestShape(new int[]{xx, xx, xx, xx}, new int[]{0, 1, 2, 3})) {
                    success = true;
                }
            }
        } else if (thisShape == 1) {// Block J
            if (thisRotation==1) {
                if ((2+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx, xx, 1 + xx, 2 + xx}, new int[]{0,1, 1, 1})) {
                        success = true;
                    }
                }
            } else if (thisRotation==2) {
                if ((1+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx, 1 + xx, xx, xx}, new int[]{0,0, 1, 2})) {
                        success = true;
                    }
                }
            } else if (thisRotation==3) {
                if ((2+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx, 1 + xx, 2 + xx, 2 + xx}, new int[]{0,0, 0, 1})) {
                        success = true;
                    }
                }
            } else if (thisRotation==4) {
                if ((1+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx, 1 + xx, 1 + xx, 1 + xx}, new int[]{0,1, 2, 2})) {
                        success = true;
                    }
                }
            }
        } else if (thisShape == 2) {// Block L
            if (thisRotation==1) {
                if ((2+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx,  xx, 1 + xx, 2 + xx}, new int[]{0,1, 0,0})) {
                        success = true;
                    }
                }
            } else if (thisRotation==2) {
                if ((1+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx, 1 + xx, 1 + xx, 1 + xx}, new int[]{0,0,1,2})) {
                        success = true;
                    }
                }
            } else if (thisRotation==3) {
                if ((2+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx, 1 + xx, 2 + xx, 2 + xx}, new int[]{1,1, 0, 1})) {
                        success = true;
                    }
                }
            } else if (thisRotation==4) {
                if ((1+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx, xx, xx, 1 + xx}, new int[]{0,1, 2, 2})) {
                        success = true;
                    }
                }
            }
        } else if (thisShape == 3) {// Block T, T-shape
            if (thisRotation==1) {
                if ((2+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx, 1 + xx, 1 + xx, 2 + xx}, new int[]{0,0, 1,0})) {
                        success = true;
                    }
                }
            } else if (thisRotation==2) {
                if ((1+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx,  xx,  xx, 1 + xx}, new int[]{0,1,2,1})) {
                        success = true;
                    }
                }
            } else if (thisRotation==3) {
                if ((2+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{ xx, 1 + xx, 1 + xx, 2 + xx}, new int[]{1,0, 1, 1})) {
                        success = true;
                    }
                }
            } else if (thisRotation==4) {
                if ((1+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx, 1 + xx, 1 + xx, 1 + xx}, new int[]{1,0, 1, 2})) {
                        success = true;
                    }
                }
            }
        } else if (thisShape == 4) { // Block Z, inverse-S shape
            if (thisRotation==1) {
                if ((2+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx, 1 + xx, 1 + xx, 2 + xx}, new int[]{0,0, 1,1})) {
                        success = true;
                    }
                }
            } else if (thisRotation==2) {
                if ((1+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx, xx, 1 + xx, 1 + xx}, new int[]{1,2,0,1})) {
                        success = true;
                    }
                }
            }
        } else if (thisShape == 5) {//Block S, S shape
            if (thisRotation==1) {
                if ((2+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx, 1 + xx, 1 + xx, 2 + xx}, new int[]{1,0, 1,0})) {
                        success = true;
                    }
                }
            } else if (thisRotation==2) {
                if ((1+xx)<numberOfBlocksWidth) {
                    if (placeTestShape(new int[]{xx,  xx, 1 + xx, 1 + xx}, new int[]{0,1,1,2})) {
                        success = true;
                    }
                }
            }
        } else if (thisShape == 6) {// Block O, square shape
            if ((1+xx)<numberOfBlocksWidth) {
                if (placeTestShape(new int[]{xx, xx, 1+xx, 1+xx}, new int[]{0, 1, 0, 1})) {
                    success = true;
                }
            }
        }
        return success;
    }

    private boolean placeTestShape(int[] x, int[] y) {
        // see if the placement of block will overlap with occupied spaces. If so, fail to place and return false to "testnewshape"
        for (int i = 0; i < x.length; i++) {
            if (testBlocks[x[i]][y[i]] == 2) {
                return false;
            }
        }

        for (int i = 0; i < x.length; i++) {
            testBlocks[x[i]][y[i]] = 1;
            testPlayLocationX[i] = x[i];
            testPlayLocationY[i] = y[i];
        }
        return true;
    }

    private void testShapeDown() {
        // move down the shape in test field until not able to move
        while (canFallDown(testPlayLocationX, testPlayLocationY, testBlocks)) {
            for (int xx = 0; xx < testPlayLocationX.length; xx++) {
                testBlocks[testPlayLocationX[xx]][testPlayLocationY[xx]] = 0;
            }
            for (int xx = 0; xx < testPlayLocationX.length; xx++) {
                testBlocks[testPlayLocationX[xx]][testPlayLocationY[xx]+1] = 1;
                // Update current test-Tetrimino location
                testPlayLocationY[xx] = testPlayLocationY[xx] + 1;
            }
        }
        // Set current test squares to inactive
        for (int xx = 0; xx < testPlayLocationY.length; xx++) {
                testBlocks[testPlayLocationX[xx]][testPlayLocationY[xx]] = 2;
        }
    }

    public void updateNewShape(int thisShape) {
		if (thisShape == 0) { // Block I: straight vertical
			placeShapeAndDetectLose(new int[] { 3, 4, 5, 6 }, new int[] { 1, 1, 1, 1 });
			pivotX = 4.5;
			pivotY = 1.5;
		} else if (thisShape == 1) { // Block J
			placeShapeAndDetectLose(new int[] { 3, 3, 4, 5 }, new int[] { 0, 1,	1, 1 });
			pivotX = 4;
			pivotY = 1;
		} else if (thisShape == 2) { // Block L
			placeShapeAndDetectLose(new int[] { 5, 3, 4, 5 }, new int[] { 0, 1,	1, 1 });
			pivotX = 4;
			pivotY = 1;
		} else if (thisShape == 3) { // Block T, T-shape
			placeShapeAndDetectLose(new int[] { 4, 3, 4, 5 }, new int[] { 0, 1,	1, 1 });
			pivotX = 4;
			pivotY = 1;
		} else if (thisShape == 4) { // Block Z
            placeShapeAndDetectLose(new int[] { 3, 4, 4, 5 }, new int[] { 0, 0,	1, 1 });
			pivotX = 4;
			pivotY = 1;
		} else if (thisShape == 5) { // Block S
            placeShapeAndDetectLose(new int[] { 4, 5, 3, 4 }, new int[] { 0, 0,	1, 1 });
			pivotX = 4;
			pivotY = 1;
		} else if (thisShape == 6) { // Block O, square shape
			placeShapeAndDetectLose(new int[] { 4, 5, 4, 5 }, new int[] { 0, 0,	1, 1 });
			pivotX = 4.5;
			pivotY = 0.5;
		}
		currentRotation = 0;
	}

	public void updateBoxShape(int[][] x, int y) {
		for (int xx = 0; xx < auxBoxWidth; xx++)
			for (int yy = 0; yy < auxBoxLength; yy++)
				x[xx][yy] = 0;

		if (y == 0) {
			for (int xx = 0; xx <= 3; xx++)
				x[xx][1] = 1;
		} else if (y == 1) {
			for (int xx = 0; xx <= 2; xx++)
				x[xx][1] = 1;
			x[0][0] = 1;
		} else if (y == 2) {
			for (int xx = 0; xx <= 2; xx++)
				x[xx][1] = 1;
			x[2][0] = 1;
		} else if (y == 3) {
			for (int xx = 0; xx <= 2; xx++)
				x[xx][1] = 1;
			x[1][0] = 1;
		} else if (y == 5) {
			x[1][0] = 1;
			x[2][0] = 1;
			x[0][1] = 1;
			x[1][1] = 1;
		} else if (y == 4) {
			x[0][0] = 1;
			x[1][0] = 1;
			x[1][1] = 1;
			x[2][1] = 1;
		} else if (y == 6) {
			x[0][0] = 1;
			x[1][0] = 1;
			x[0][1] = 1;
			x[1][1] = 1;
		}
	}

	public void placeShapeAndDetectLose(int[] x, int[] y) {
		lose = false;
		for (int xx = 0; xx < x.length; xx++) {
			if (blocks[x[xx]][y[xx]] == 2) {
				lose = true;
				break;
			}
		}
		if (lose) {
			updateHighScore();
		}
		for (int xx = 0; xx < x.length; xx++) {
			blocks[x[xx]][y[xx]] = 1;
			playLocationX[xx] = x[xx];
			playLocationY[xx] = y[xx];
		}
	}

	// Checks if there is anything below the active shape.
	// If there is: Freeze the active shape
	// If there isn't: Move the shape down.
	public void shapeDown() {
		coloring();

		boolean move = canFallDown(playLocationX, playLocationY, blocks);

		if (!move && !slackOnce) {
			activateSlack();
		}

		// Detect
		if (!move && !slack) {
			int currentDrop; // The number of lines cleared
			// Set current squares to inactive
			for (int xx = 0; xx < playLocationY.length; xx++) {
				blocks[playLocationX[xx]][playLocationY[xx]] = 2;
			}

			currentDrop = clearLines(blocks);
			softDrop = false;
			gravityTicker = 0.0;
			gravity = defaultGravity;

			scoring(currentDrop);
			// Scoring System end.

			onShapeLocked();
			pickShape();
		} else if (move) {
			// If slack is still activated, cancel the slack
			slackOnce = false;
			for (int xx = 0; xx < playLocationX.length; xx++) {
				blocks[playLocationX[xx]][playLocationY[xx]] = 0;
			}
			for (int xx = 0; xx < playLocationX.length; xx++) {
				blocks[playLocationX[xx]][playLocationY[xx] + 1] = 1;

				// Update current Tetrimino location
				playLocationY[xx] = playLocationY[xx] + 1;
				lastMove = "Drop";
			}
			pivotY = pivotY + 1;
		}
		highScore = Math.max(highScore, score);
	}

	public void activateSlack() {
		slackOnce = true;
		slack = true;
		slackTime = slackLength;
	}

	public boolean canFallDown(int[] locationX, int[] locationY, int[][] targetField) {
		for (int xx = 0; xx < locationY.length; xx++) {
			if (locationY[xx] + 1 >= numberOfBlocksLength) {
				return false;
			}
		}
		for (int xx = 0; xx < locationY.length; xx++) {
			if (targetField[locationX[xx]][locationY[xx] + 1] == 2) {
				return false;
			}
		}
		return true;
	}

	public int clearLines(int[][] targetBlock) {
		int counter = 0;
		for (int yy = numberOfBlocksLength - 1; yy > 0; yy--) {
			boolean line = true;
			do {
				for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
					if (targetBlock[xx][yy] == 0) {
						line = false;
						break;
					}
				}
				if (line) {
					for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
                        targetBlock[xx][yy] = 0;
					}
					for (int xy = yy; xy > 0; xy--) {
						for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
                            targetBlock[xx][xy] = targetBlock[xx][xy - 1];
                            if (targetBlock==blocks) {
                                colors[xx][xy] = colors[xx][xy - 1];
                            }
						}
					}
					for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
                        targetBlock[xx][0] = 0;
					}
					counter++;
				}
			} while (line);
		}
		return counter;
	}

	public void scoring(int currentDrop) {
		// Scoring Information System Startup
		if (currentDrop > 0) {
			clearInfo.clear();
			if (currentDrop == 1) {
				clearInfo.add(singleText);
			} else if (currentDrop == 2) {
				clearInfo.add(doubleText);
			} else if (currentDrop == 3) {
				clearInfo.add(tripleText);
			} else if (currentDrop == 4) {
				clearInfo.add(tetrisText);
			}
		}

		// Scoring System
		long addScore = 0;

		if (currentDrop == 1) {
			addScore = 10;
		} else if (currentDrop == 2) {
			addScore = 30;
		} else if (currentDrop == 3) {
			addScore = 100;
		} else if (currentDrop == 4) {
			addScore = 300;
		}

		if (currentDrop > 0) {
			if (combo > 0) {
				clearInfo.add(combo + COMBO);
				addScore = addScore + 5 * combo;
			}
			combo = combo + 1;
		} else {
			combo = 0;
		}

        // Adjust for level
        addScore = (level+1)*(level+1)*addScore;

		if (addScore > 0) {
			clearInfo.add("+" + addScore);
		}

		score += addScore;
		onScore(currentDrop);
	}

	// Gravity falling mechanic.
	// totalGrav is incremented by gravity every tick.
	// when totalGrav is higher than 1, the shape moves down 1 block.
	public void gravity() {
		gravityTicker = gravityTicker + gravity + gravityAdd;
		while (gravityTicker >= 1) {
			gravityTicker = gravityTicker - 1;
			shapeDown();
		}
	}

	public void slack() {
		if (slack) {
			slackTime = slackTime - FPS;
			if (slackTime < 0) {
				slack = false;
			}
		}
	}

	public abstract void updateHighScore();

	public void editHighScore(SharedPreferences settings, long totalClearLine) {

        if ((score > highScoreLimit) && (lose)) {
            writeHighScore(settings, score, totalClearLine);
        }
        if (score > highScore) {
            highScore = score;
        }
	}

    public void writeHighScore(SharedPreferences settings, long hiscore, long totalClearLine) {
        String hiscoren[] = new String[3];
        long hiscorev[] = new long[3];
        long hiscorel[] = new long[3];
        int pos = 3;

        // load the detail highscore table
        hiscorev[0] = getLongFromSettings(0, "HISCORE1V", settings);
        hiscorev[1] = getLongFromSettings(0, "HISCORE2V", settings);
        hiscorev[2] = getLongFromSettings(0, "HISCORE3V", settings);
        hiscorel[0] = getLongFromSettings(0, "HISCORE1L", settings);
        hiscorel[1] = getLongFromSettings(0, "HISCORE2L", settings);
        hiscorel[2] = getLongFromSettings(0, "HISCORE3L", settings);
        hiscoren[0] = settings.getString("HISCORE1N", "");
        hiscoren[1] = settings.getString("HISCORE2N", "");
        hiscoren[2] = settings.getString("HISCORE3N", "");

        // Find the position to insert
        for (int i = 0; i <= 2; i++) {
            if (score > hiscorev[i]) {
                pos = i;
                break;
            }
        }
        // move the lower highscore down
        for (int i = 1; i>=pos; i--) {
            hiscorev[i + 1] = hiscorev[i];
            hiscorel[i + 1] = hiscorel[i];
            hiscoren[i + 1] = hiscoren[i];
        }
        // insert the current highscore
        hiscorev[pos] = hiscore;
        hiscorel[pos] = totalClearLine;
        hiscoren[pos] = settings.getString("PlayerName", "LittleAngel");

        SharedPreferences.Editor editor = settings.edit();
        editor.putString("HISCORE1V", String.valueOf(hiscorev[0]));
        editor.putString("HISCORE2V", String.valueOf(hiscorev[1]));
        editor.putString("HISCORE3V", String.valueOf(hiscorev[2]));
        editor.putString("HISCORE1L", String.valueOf(hiscorel[0]));
        editor.putString("HISCORE2L", String.valueOf(hiscorel[1]));
        editor.putString("HISCORE3L", String.valueOf(hiscorel[2]));
        editor.putString("HISCORE1N", hiscoren[0]);
        editor.putString("HISCORE2N", hiscoren[1]);
        editor.putString("HISCORE3N", hiscoren[2]);
        editor.commit();
    }

	public void ghostShape() {
		boolean move;
		int[] tempPlayLocationX = new int[4];
		int[] tempPlayLocationY = new int[4];
		for (int xx = 0; xx < numberOfBlocksWidth; xx++)
			for (int yy = 0; yy < numberOfBlocksLength; yy++)
				if (blocks[xx][yy] == 3)
					blocks[xx][yy] = 0;
		for (int xx = 0; xx < playLocationY.length; xx++) {
			tempPlayLocationX[xx] = playLocationX[xx];
			tempPlayLocationY[xx] = playLocationY[xx];
		}
		do {
			move = canFallDown(tempPlayLocationX, tempPlayLocationY, blocks);
			if (!move) {
				for (int xx = 0; xx < playLocationY.length; xx++) {
					if (blocks[tempPlayLocationX[xx]][tempPlayLocationY[xx]] != 1
							& blocks[tempPlayLocationX[xx]][tempPlayLocationY[xx]] != 2) {
						blocks[tempPlayLocationX[xx]][tempPlayLocationY[xx]] = 3;
					}
				}
			} else {
				for (int xx = 0; xx < playLocationY.length; xx++) {
					tempPlayLocationY[xx] = tempPlayLocationY[xx] + 1;
				}
			}
		} while (move);
	}

	public void moveLeft() {
		boolean move = true;
		// Shape cannot go out of bounds
		for (int xx = 0; xx < playLocationY.length; xx++)
			if (playLocationX[xx] - 1 < 0)
				move = false;
		// Shape cannot overlap another shape
		if (move)
			for (int xx = 0; xx < playLocationY.length; xx++)
				if (blocks[playLocationX[xx] - 1][playLocationY[xx]] == 2)
					move = false;
		// Move and reset slack
		if (move) {
			for (int xx = 0; xx < playLocationY.length; xx++) {
				blocks[playLocationX[xx]][playLocationY[xx]] = 0;
			}
			for (int xx = 0; xx < playLocationY.length; xx++) {
				blocks[playLocationX[xx] - 1][playLocationY[xx]] = 1;
				playLocationX[xx] = playLocationX[xx] - 1;
			}
			pivotX = pivotX - 1;
		}
	}

	public void moveRight() {
		boolean move = true;
		// Shape cannot go out of bounds
		for (int xx = 0; xx < playLocationY.length; xx++)
			if (playLocationX[xx] + 1 >= numberOfBlocksWidth)
				move = false;
		// Shape cannot overlap another shape
		if (move)
			for (int xx = 0; xx < playLocationY.length; xx++)
				if (blocks[playLocationX[xx] + 1][playLocationY[xx]] == 2)
					move = false;
		// Move and reset slack
		if (move) {
			for (int xx = 0; xx < playLocationY.length; xx++) {
				blocks[playLocationX[xx]][playLocationY[xx]] = 0;
			}
			for (int xx = 0; xx < playLocationY.length; xx++) {
				blocks[playLocationX[xx] + 1][playLocationY[xx]] = 1;
				playLocationX[xx] = playLocationX[xx] + 1;
			}
			pivotX = pivotX + 1;
		}
	}

	public void shapeTurn() {
		turnSuccess = false;
		int[] dLocationX = new int[4];
		int[] dLocationY = new int[4];
		for (int xx = 0; xx < dLocationX.length; xx++) {
			dLocationX[xx] = (int) (-(playLocationY[xx] - pivotY) - (playLocationX[xx] - pivotX));
			dLocationY[xx] = (int) ((playLocationX[xx] - pivotX) - (playLocationY[xx] - pivotY));
		}
		turnShape(dLocationX, dLocationY, currentRotation, false);
		// Reset slack if turn is successful
		if (turnSuccess) {
			slackOnce = false;
			currentRotation = (currentRotation + 1) % 4;
			lastMove = "Turn";
		}
	}

    public void shapeTurnCC() {
        turnSuccess = false;
        int[] dLocationX = new int[4];
        int[] dLocationY = new int[4];
        for (int xx = 0; xx < dLocationX.length; xx++) {
            dLocationX[xx] = (int) ((playLocationY[xx] - pivotY) - (playLocationX[xx] - pivotX));
            dLocationY[xx] = (int) (-(playLocationX[xx] - pivotX) - (playLocationY[xx] - pivotY));
        }
        turnShape(dLocationX, dLocationY, ((currentRotation + 3) % 4), true);
        // Reset slack if turn is successful
        if (turnSuccess) {
            slackOnce = false;
            currentRotation = (currentRotation + 3) % 4;
            lastMove = "Turn";
        }
    }

    public void turnShape(int[] x, int[] y, int tableIndex, boolean counterclockwise) {
		for (int i = 0; i < 5; i++) {
			int checkX;
			int checkY;
			if (currentShape == 0) {
				checkX = Constants.iBlockkickX[tableIndex][i];
				checkY = Constants.iBlockkickY[tableIndex][i];
			} else {
				checkX = Constants.kickX[tableIndex][i];
				checkY = Constants.kickY[tableIndex][i];
			}

			if (counterclockwise) {
				checkX = -checkX;
				checkY = -checkY;
			}

			boolean ok = true;

			for (int xx = 0; xx < playLocationX.length; xx++) {
				if (playLocationX[xx] + x[xx] + checkX > numberOfBlocksWidth - 1
						| playLocationX[xx] + x[xx] + checkX < 0) {
					ok = false;
				}
			}

			for (int xx = 0; xx < playLocationY.length; xx++) {
				if (playLocationY[xx] + y[xx] - checkY >= 22
						| playLocationY[xx] + y[xx] - checkY < 0) {
					ok = false;
				}
			}

			try {
				if (ok) {
					if (blocks[playLocationX[0] + x[0] + checkX][playLocationY[0]
							+ y[0] - checkY] != 2
							& blocks[playLocationX[1] + x[1] + checkX][playLocationY[1]
									+ y[1] - checkY] != 2
							& blocks[playLocationX[2] + x[2] + checkX][playLocationY[2]
									+ y[2] - checkY] != 2
							& blocks[playLocationX[3] + x[3] + checkX][playLocationY[3]
									+ y[3] - checkY] != 2) {
						for (int xx = 0; xx < playLocationY.length; xx++) {
							blocks[playLocationX[xx]][playLocationY[xx]] = 0;
						}
						for (int xx = 0; xx < playLocationY.length; xx++) {
							blocks[playLocationX[xx] + x[xx] + checkX][playLocationY[xx]
									+ y[xx] - checkY] = 1;
							playLocationX[xx] = playLocationX[xx] + x[xx]
									+ checkX;
							playLocationY[xx] = playLocationY[xx] + y[xx]
									- checkY;
						}
						pivotX = pivotX + checkX;
						pivotY = pivotY - checkY;
						turnSuccess = true;
					}
				}
			} catch (Exception e) {}

			if (turnSuccess) {
			//	if (i != 0) {
			//		kick = true;
			//	}
				break;
			}
		}
	}

	public void coloring() {
		for (int xx = 0; xx < playLocationX.length; xx++)
			colors[playLocationX[xx]][playLocationY[xx]] = currentShape;

	}

	public void pauseGame(boolean changeTo) {
		pause = changeTo;
		clock = System.nanoTime();
		updateHighScore();
	}

	// Controls
	public OnTouchListener getOnTouchListener() {
		return new OnTouchListener() {
			float x;
			float y;
			float prevY;
			boolean turn;
			boolean ignoreInputs;
			float startingX;
			float startingY;

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (!pause & !lose) {
					switch (arg1.getAction()) {

					case MotionEvent.ACTION_DOWN:
						x = arg1.getX();
						y = arg1.getY();
						startingX = x;
						startingY = y;
						prevY = y;
						turn = true;
						ignoreInputs = false;
						return true;
					case MotionEvent.ACTION_MOVE:
						x = arg1.getX();
						y = arg1.getY();
						if (!ignoreInputs) {
							if (x - startingX > squareSide) {
								startingX = x;
								moveRight();
								turn = false;
							} else if (x - startingX < -squareSide) {
								startingX = x;
								moveLeft();
								turn = false;
							} else if (y - startingY > dragSensitivity
									& !ignoreInputs) {
								startingY = y;
								gravity = softDropSpeed;
								softDrop = true;
								turn = false;
							} else if (startingY - y > dragSensitivity / 2
									& !ignoreInputs) {
								startingY = y;
								gravity = defaultGravity;
								softDrop = false;
								turn = false;
							}
						}
						prevY = y;
						coloring();
						return true;
                        case MotionEvent.ACTION_UP:
                            x = arg1.getX();
                            y = arg1.getY();
                            if (turn) {
                                if (x < squareSide * numberOfBlocksWidth * 0.5) {
                                    shapeTurnCC();
                                } else {
                                    shapeTurn();
                                }
                            }
                            if (softDrop) {
                                gravity = defaultGravity;
                                softDrop = false;
                            }
                            coloring();
                            return true;
                    }
				}
                if (lose) { //quit to mainmenu
                    if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                        exit_tap += 1;
                    }
                    if (exit_tap >= 2) {
                        startNewGame = true; // so when next time load this activity a new game is started
                        synchronized (this) {
                            ((Activity) mContext).finish();
                        }
                        return true;
                    }
                    return true;
                }
                if (pause) {
                    pauseGame(false);
                }
				return false;
			}

		};
	}

	public void getLayout(int width, int height) {
		squareSide = (int) Math.min(width / numSquaresX, height / numSquaresY);
		auxBoxXStarting = (int)(squareSide * (numberOfBlocksWidth + 0.4));
        nextTextYStarting = squareSide * 1;
        nextShapeYStarting = squareSide * 2;
        highScoreYStarting = squareSide * 6;
        scoreInfoYStarting = squareSide * 8;
        lineInfoYStarting = squareSide * 10;
        auxInfoXStarting = numSquaresX*squareSide - squareSide/2;
        LevelInfoYStarting = squareSide * 12; // for level and
		clearInfoYStarting = squareSide * 16;
        mainFieldStartingY = (int)(squareSide*2.5);
        mainFieldShiftX = squareSide / 2;
        mainFieldShiftY = squareSide / 2 + squareSide*2;

		getScreenSize = false;
	}

	public int chooseColor(int x) {
		if (x == 0) { // I
			return Color.CYAN;
		} else if (x == 1) { // L
			return Color.BLUE;
		} else if (x == 2) { // J
			return ORANGE;
		} else if (x == 3) { // T
			return Color.MAGENTA;
		} else if (x == 4) { //Z
			return Color.GREEN;
		} else if (x == 5) { //S
			return Color.RED;
		} else { // O
			return Color.YELLOW;
		}
	}

    public void getLanguageConst() {
        if (true) { // now only ZH_HK availble. Future: extend this to EN or even ZH_CN
            singleText = Constants.ZH_HK.singleText;
            doubleText = Constants.ZH_HK.doubleText;
            tripleText = Constants.ZH_HK.tripleText;
            tetrisText = Constants.ZH_HK.tetrisText;
            RECORD_HIGH = Constants.ZH_HK.RECORD_HIGH;
            CURRENT_SCORE = Constants.ZH_HK.CURRENT_SCORE;
            CURRENT_LINE = Constants.ZH_HK.CURRENT_LINE;
            CURRENT_LEVEL = Constants.ZH_HK.CURRENT_LEVEL;
            REMAIN_LINE = Constants.ZH_HK.REMAIN_LINE;
            NEXT_BLOCK = Constants.ZH_HK.NEXT_BLOCK;
            COMBO = Constants.ZH_HK.COMBO;
            END_RECORD_1 = Constants.ZH_HK.END_RECORD_1;
            END_RECORD_2 = Constants.ZH_HK.END_RECORD_2;
        }
    }

	public void newGame() {
		if (!startNewGame) {
			updateHighScore();
		}
        getLanguageConst();
		getSettings();
		// Reset Variables
		for (int xx = 0; xx < numberOfBlocksWidth; xx++) {
			for (int yy = 0; yy < numberOfBlocksLength; yy++) {
				blocks[xx][yy] = 0;
				colors[xx][yy] = 0;
				if (xx < auxBoxWidth & yy < auxBoxLength) {
                    nextBlocks[xx][yy] = 0;
                }
			}
		}
		blocks = new int[numberOfBlocksWidth][numberOfBlocksLength];
		colors = new int[numberOfBlocksWidth][numberOfBlocksLength];
		nextBlocks = new int[auxBoxWidth][auxBoxLength];
		playLocationX = new int[4];
		playLocationY = new int[4];
		nextShape = -1;
        notGivenShape = -1;
        currentShape = -1;
		score = 0;
		lastMove = "Nothing";
		gravityTicker = 0;
		gravityAdd = 0;
		clearInfo.clear();
		pause = false;
		lose = false;
		clock = System.nanoTime();
		auxText = "";
		onNewGame();
		// Pick the new shape
		pickShape();
		startNewGame = false;
	}

}
