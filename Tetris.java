
/*
	Tetris game written by Elliott Gorman.

*/



import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JPanel;
import javax.swing.Timer;

import testingenv.Tetris;

import java.lang.Math;


public class Tetris extends Frame {
	
	// Canvas size
	int CanvasSizeX = 450;
	int CanvasSizeY = 600;
	
	// Window border offsets
	int WinBorderOffsetX = 16;
	int WinBorderOffsetY = 39;
	
   public static void main(String[] args) {new Tetris();}

   Tetris() {
      //super("RedRect");
      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {System.exit(0);}
      });
      setSize(CanvasSizeX + WinBorderOffsetX, CanvasSizeY + WinBorderOffsetY);
      add("Center", new TetrisGame());
      setVisible(true);
   }
}

class TetrisGame extends JPanel {
   
	// Canvas Size
	int CanvasSizeX = 450;
	int CanvasSizeY = 600;
	
	// Canvas bounds
	static int CanvasLeftBound = 25;
	static int CanvasRightBound = 275;
	
	// Each Tetris square will be 25x25 pixels.
	// Therefore, our play area is (25*10, 25*20) pixels
	int playAreaX = 250;
	int playAreaY = 500;
	int playAreaPaddingX = 25;
	int playAreaPaddingY = 25;
	
	// Let's create bounds for the play area.
	int playAreaRightBound = playAreaPaddingX + playAreaX;
	int playAreaTopBound = playAreaPaddingY; // Note anything > than this is okay
											   // but < this is out of bounds
	int playAreaLeftBound = playAreaPaddingX;
	int playAreaBottomBound = playAreaPaddingY + playAreaY;
	
	// Information about the next piece that is dropping
	boolean NextPiece = true;
	static int NextPieceIndex = 0;
	
	// List of all game pieces in play and types of game pieces
	ArrayList<TetrisPiece> GamePieces = new ArrayList<>();
	ArrayList<TetrisSquare> TetrisSquares = new ArrayList<>();
	Shape LegalShapes[] = {
		Shape.I,
		Shape.J,
		Shape.L,
		Shape.O,
		Shape.S,
		Shape.T,
		Shape.Z
	};
	
	// GameBoard
	boolean GameBoard[][] = new boolean[20][10];
	
	// Bool to check if this is the FirstRNF run
	boolean FirstRNF = true;
	boolean FirstPC = true;
	
	// Boolean to check if we transformed this frame
	boolean Transformed = false;
	
	// Variables to aide in Isotropic Mapping
	static int centerX, centerY;
	static float pixelSize, rWidth = 450.0F, rHeight = 600.0F;
	
	
	// Current level, and scoring information
	int CurrentLevel = 0;
	int NumClearedLines = 0;
	int Score = 0;
	String strLevel = "Level : ";
	String strLines = "Lines : ";
	String strScore = "Score : ";
	
	// Boolean for when a piece is allowed to move down
	// This is used to slow down how fast the tetris piece
	// will move downwards
	boolean PieceReadyToMoveDown = true;
	
	// Boolean to check if this piece is moving down quickly 
	// (down arrow held) or not
	boolean IsPieceMovingQuickly = false;
	
	// Tetris Piece that comes next
	TetrisPiece UpcomingPiece;
	
	
	
	
	
	public TetrisGame() {
		
		// Register custom font
	     try {
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("C:\\Users\\Elliott Gorman\\eclipse-workspace\\4361 Tetris\\src\\PressStart2P-Regular.ttf")));
			
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// Attach Listener for game loop
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                TetrisGameLoop();
            }
        });
	}
	
	private void TetrisGameLoop() {
		// Register a KeyBoard Listener
		new IsKeyPressed().KeyListener();
		
		// Loop
        Timer MainLoopTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	// Ready the next frame for painting
                ReadyNextFrame();
                
        		// Draw the next frame
        		repaint();
            }
        });
        MainLoopTimer.start();
        
        // Timer for piece moving down
        Timer YAxisDelayTimer = new Timer(750, new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		// Every tick flick the PieceReadyToMoveDown
        		PieceReadyToMoveDown = true;
        	}
        });
        YAxisDelayTimer.start();
		
	}// End TetrisGameLoop()
	
	void initgr() {
		Dimension d = getSize();
		int maxX = d.width - 1;
		int maxY = d.height - 1;
		pixelSize = Math.max(rWidth / maxX, rHeight / maxY);
		centerX = maxX / 2; 
		centerY = maxY / 2;
	}
	
	// Isotropic mapping functions
	static int iX(float x) { return Math.round(centerX + x / pixelSize); }
	static int iY(float y) { return Math.round(centerY - y / pixelSize); }
	static float fx(int x) { return (x - centerX) * pixelSize; }
	static float fy(int y) { return (centerY - y) * pixelSize; }
	
	public static void isoRect(Graphics g, float x, float y, float w, float h) {
		// Calculate width and heights
		int width = Math.abs(iX(x) - iX(x+w));
		int height = Math.abs(iY(y) - iY(y-h));
		g.drawRect(iX(x), iY(y), width, height);
	}
	
	public static void isoFillRect(Graphics g, float x, float y, float w, float h) {
		// Calculate width and heights
		int width = Math.abs(iX(x) - iX(x+w));
		int height = Math.abs(iY(y) - iY(y-h));
		g.fillRect(iX(x), iY(y), width, height);
	}
	
	
	boolean CanMoveLeftRight = false;
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Initialize basic graphics settings
		//GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		g.setFont(new Font("Press Start 2P", Font.PLAIN, 14));
		if (FirstPC) {
			
			FirstPC = false;
		}
		
		// Draw Background color
		Color curColor = g.getColor();
		g.setColor(Color.GRAY);
		isoFillRect(g, -225, 300, 450, 600);
		g.setColor(curColor);

		// Recalculate isotropic grid coordinates
		initgr();
		
		// Draw Play area rectangle
		isoRect(g, -(CanvasSizeX/2) + playAreaPaddingX, (CanvasSizeY/2) - playAreaPaddingY, playAreaX, playAreaY);
		
		// Redraw game pieces
		UpdateGamePieces(g);
		
		// Draw all tetris squares
		for (int i = 0; i < TetrisSquares.size(); i++) {
			TetrisSquares.get(i).DrawSelf(g);
		}
		
	    // Draw next shape outer rectangle
	    isoRect(g, 75, 275, 125, 100);
	    
	    // Draw current level, num lines, and score
	    g.drawString(strLevel + CurrentLevel, iX(75), iY(0));
	    g.drawString(strLines + NumClearedLines, iX(75), iY(-25));
	    g.drawString(strScore + Score, iX(75), iY(-50));

	    // Draw the quit button
	    isoRect(g, 105, -155, 60, 30);
	    g.drawString("QUIT", iX(115), iY(-175));
	    
	} // End paint()
	
	
	private void UpdateGamePieces(Graphics g) {
		
		// Draw all game pieces
		for (int i = 0; i < GamePieces.size(); i++) {
			GamePieces.get(i).DrawPiece(g);
		}
		
		// Draw Upcoming game piece
		if (UpcomingPiece != null)
			UpcomingPiece.DrawPiece(g, 125, 225);
		
	}
	
	private void ReadyNextFrame() {
		
		if (FirstRNF) {
			// Initialize GameBoard
			for (int i = 0; i < GameBoard.length; i++) {
				for (int j = 0; j < GameBoard[0].length; j++) {
					GameBoard[i][j] = false;
				}
			}
			FirstRNF = false;
			
			// Initialize next Tetris Piece
			int rand = ThreadLocalRandom.current().nextInt(0, 7);
			UpcomingPiece = new TetrisPiece(LegalShapes[rand], 25);
		}
		
		if (NextPiece) {	
			// Before doing anything with the next piece, ensure that 
			// we do not need to remove any rows.
			RemoveLinesIfFull();
			
			// Set next game piece and get a new one for next iter
			int rand = ThreadLocalRandom.current().nextInt(0, 7);
			GamePieces.add(UpcomingPiece);
			UpcomingPiece = new TetrisPiece(LegalShapes[rand], 25);
			//GamePieces.add(new TetrisPiece(Shape.O, 25));
			
			// Get information and set starting values for new piece
			NextPieceIndex = GamePieces.size() - 1;
			GamePieces.get(NextPieceIndex).SetXY(-75, 250);
			GamePieces.get(NextPieceIndex).CalculatePoints();
			NextPiece = false;
		}
		
		
		if (IsKeyPressed.IsDebugKeyHeld()) {
			GamePieces.remove(NextPieceIndex);
		}
		
		// If restart is pressed, restart game
		if (IsKeyPressed.IsRestartHeld()) {
			GamePieces.clear();
			TetrisSquares.clear();
			
			for (int i = 0; i < GameBoard.length; i++)
				for (int j = 0; j < GameBoard[i].length; j++)
					GameBoard[i][j] = false;
			
			NextPiece = true;
			return;
		}
		
		
		if (IsKeyPressed.IsSpacePressed() && GamePieces.get(NextPieceIndex).shape != Shape.O) {
			// Transform
			GamePieces.get(NextPieceIndex).TransformPiece(RotateDirection.CW);
			//repaint();
			
			// Set transformed to true
			Transformed = true;
			
			IsKeyPressed.spacePressed = false;
		}
		
		// These variables are used to determine the offset in X from the starting
		// position of the current game piece
		int currentPieceOffsetX = 0;
		
		if (IsKeyPressed.IsRightArrowHeld()) {
			TetrisPiece thisPiece = GamePieces.get(NextPieceIndex);

			// Move to the right if not going out of bounds
			if (thisPiece.rightMostBound + thisPiece.thisX < 50) {	// TODO Need to change this bound
				
				// If we're in bounds, make sure we're not going to hit another
				// piece. If not, then we're good.
				if (CanMovePieceLeftRight(thisPiece, 1)) {
					thisPiece.SetXY(thisPiece.thisX + 25, thisPiece.thisY);
					currentPieceOffsetX -= 1;
				}
			}
		} // End if IsRightArrowHeld()
		
		if (IsKeyPressed.IsLeftArrowHeld()) {
			TetrisPiece thisPiece = GamePieces.get(NextPieceIndex);
			// Move to the left if not going out of bounds
			if (thisPiece.leftMostBound + thisPiece.thisX > -200) {		// TODO Need to change this bound
				if (CanMovePieceLeftRight(thisPiece, -1)) {
					thisPiece.SetXY(thisPiece.thisX - 25, thisPiece.thisY);
					currentPieceOffsetX += 1;
				}
			}
		} // End if IsLeftArrowHeld()
		
		if (IsKeyPressed.IsDownArrowHeld()) {
			IsPieceMovingQuickly = true;
		} else {
			// If down arrow is not held, piece is not moving quickly
			IsPieceMovingQuickly = false;
		}
		
		// Get the current game piece
		TetrisPiece currentPiece = GamePieces.get(NextPieceIndex);
		
		// Make sure piece stops when hitting the lower bounds
		// and setup to create new piece
		if (currentPiece.thisY + currentPiece.bottomMostBound <= -200) {	// TODO Need to change this bound, remove hardcoded values
			NextPiece = true;
			// Relinquish this piece
			RelinquishPiece(currentPiece);
		}
		else if (!CanMovePieceDown(currentPiece, currentPieceOffsetX)) {
			NextPiece = true;
			
			// Relinquish this piece
			RelinquishPiece(currentPiece);
		}
		else {
			// Otherwise, tick game piece downward if ready, and we're not moving
			// down quickly
			if (PieceReadyToMoveDown || IsPieceMovingQuickly)
				currentPiece.SetXY(currentPiece.thisX, currentPiece.thisY - 25);
		}
		
		// Make sure our coordinates are up to date
		currentPiece.CalculatePoints();
		
		// Update the GameBoard
		UpdateGameBoard(currentPiece, currentPieceOffsetX);
		
		//PrintGameBoard();
		
		// Reset Transformed and piece moving downward
		Transformed = false;
		PieceReadyToMoveDown = false;
		
	} // End ReadyNextFrame()
	
	// This method destroys a Tetris piece and takes it's 
	// coordinates 
	private void RelinquishPiece(TetrisPiece piece) {
		
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			int thisSquareX = piece.CurrentCoordinates[i];
			int thisSquareY = piece.CurrentCoordinates[j];
			
			TetrisSquares.add(new TetrisSquare(thisSquareX, thisSquareY, piece.color));
		}
		
		// Remove this piece from the game pieces
		GamePieces.remove(piece);
		
	} // End RelinquishPiece()
	
	
	
	private void UpdateGameBoard(TetrisPiece piece, int XOffset) {
		// For the current game piece, update it's position
		// on the game board
		
		// Remove old position from board
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			int thisSquareX;
			int thisSquareY;
			
			if (Transformed == true) {
				thisSquareX = piece.OldCoordinates[i] + XOffset;
				thisSquareY = piece.OldCoordinates[j];
				// TODO jump

				// Set previous position (y-1) as false
				GameBoard[piece.OldCoordinates[j]][piece.OldCoordinates[i]] = false;
			} else {
				thisSquareX = piece.CurrentCoordinates[i] + XOffset;
				thisSquareY = piece.CurrentCoordinates[j];
				
				
				// If we hit the bottom of the board, we can still move left/right if possible.
				// Therefore, we want to to only set (x, y), otherwise we're moving down as well
				// and we want to set (x, y-1)
				if (piece.thisY + piece.bottomMostBound <= -200) {
					GameBoard[thisSquareY-1][thisSquareX] = false;
					GameBoard[thisSquareY][thisSquareX] = false;
				} else {
					// Set previous position (y-1) as false
					
					// Since the y-axis ticks slower than the x-axis, only look 1 space backwards
					// if on this call we're downwards
					if (PieceReadyToMoveDown || IsPieceMovingQuickly) {
						GameBoard[thisSquareY-1][thisSquareX] = false;
					} else {
						// Otherwise, only update x-axis
						GameBoard[thisSquareY][thisSquareX] = false;
					}

				}
			}
			
		} // End for i,j (removing old position from board)
		
		// Place new position on board
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			int thisSquareX = piece.CurrentCoordinates[i];
			int thisSquareY = piece.CurrentCoordinates[j];

			// Set new position as true
			GameBoard[thisSquareY][thisSquareX] = true;
		}
	} // End UpdateGameBoard();
	
	private boolean CanMovePieceDown(TetrisPiece piece, int XOffset) {
		// A piece can move down if it's current Y-positions
		// + 1 are not filled on the game board.
		// If any of them are, the piece can not shift down

		// Get X-values and Y-values of this piece's squares
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			int thisSquareX = piece.CurrentCoordinates[i];
			int thisSquareY = piece.CurrentCoordinates[j];
			
			// If Any (x, y+1) is true, then return false
			if (GameBoard[thisSquareY+1][thisSquareX] == true) {
				// Make sure this coordinate is not part of this piece
				if (!IsCoordPartOfSelf(piece, thisSquareX, thisSquareY+1, XOffset)) {
					// Return false and create a new piece.
					return false;
				}
			} 
			
		} // End for i,j
		
		// Otherwise, we can move down
		return true;
	} // End CanMovePieceDown()
	
	// This checks to see if x, y is a coordinate of one of piece's squares
	private boolean IsCoordPartOfSelf(TetrisPiece piece, int x, int y, int XOffset) {
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			int thisSquareX = piece.CurrentCoordinates[i] + XOffset;
			int thisSquareY = piece.CurrentCoordinates[j];

			// If x, y is one of piece's coordinates, then we are looking at 
			// ourselves. Return false.
			
			if (Transformed) {
				// If the shape has been transformed, make sure it's not checking any
				// of its' previous points
				if ((piece.OldCoordinates[i]) == x && piece.OldCoordinates[j] == y)
					return true;
				
			} else {
				if (thisSquareX == x && thisSquareY == y)
					return true;
			}
		}

		return false;
	}
	
	// This method verifies that a Tetris piece can move to the right
	// or left.
	private boolean CanMovePieceLeftRight(TetrisPiece piece, int XOffset) {
		
		// Verify there is nothing at piece.coordinates+XOffset
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			int thisSquareX = piece.CurrentCoordinates[i];
			int thisSquareY = piece.CurrentCoordinates[j];
			
			// See if the square is true
			if (GameBoard[thisSquareY][thisSquareX+XOffset] == true) {
				// Make sure this coordinate is not part of this piece
				if (!IsCoordPartOfSelf(piece, thisSquareX+XOffset, thisSquareY, 0)) {
					// Return false and create a new piece.
					return false;
				}
			}
			
		} // End for i,j
		
		return true;
		
	} // End CanMovePieceLeftRight()
	
	
	// This method checks to see if any of the rows are filled and if they
	// are, it removes that row and shifts all remaining squares down.
	private void RemoveLinesIfFull() {
		
		// Count for number of lines removed
		int NumLinesRemoved = 0;
		
		// Check if a row is full
		for (int i = 0; i < GameBoard.length; i++) {
			int numFilled = 0;
			for (int j = 0; j < GameBoard[i].length; j++) {
				if (GameBoard[i][j] == true) {
					numFilled++;
				}
			}
			
			// If numFilled is == GameBoard[i].length, then the row is full
			if (numFilled == GameBoard[i].length) {
				// Remove row i
				RemoveRow(i);
				NumLinesRemoved++;
			}
		} // End for i < GameBoard.length
		
		// Update Score and lines removed if we cleared lines
		if (NumLinesRemoved > 0) {
			Score += 40 * (CurrentLevel + NumLinesRemoved);
			NumClearedLines += NumLinesRemoved;
			
			// For every 10 lines cleared, increase the level by 1
			if ( (int) (NumClearedLines / 10) > CurrentLevel )
				CurrentLevel = (int) (NumClearedLines / 10);
		}
		
	} // End RemoveLinesIfFull()
	
	// Removes all blocks in a given row and shifts all blocks
	// above down by 1
	@SuppressWarnings("unchecked")
	private void RemoveRow(int row) {
		
		// TODO
		/*
		 * Working, but it's pretty brute-forced, I'd like to come back
		 * and improve this algorithm if I have time.
		 * 
		 * I think I can improve the second set of for loops.
		 */
		ArrayList<TetrisSquare> tempSquares = (ArrayList<TetrisSquare>) TetrisSquares.clone();
		
		// Remove all squares at row from Tetris Squares
		for (int i = 0; i < tempSquares.size(); i++) {
			TetrisSquare NextSquare = tempSquares.get(i);
			
			if (NextSquare.y == row) {
				TetrisSquares.remove(NextSquare);
				GameBoard[NextSquare.y][NextSquare.x] = false; 
			}
		}
		
		// Reset tempSquares to get rid of 
		tempSquares = (ArrayList<TetrisSquare>) TetrisSquares.clone();
		
		// Move all squares above row down 1 space, starting at
		// the bottommost row from row
		for (int j = row - 1; j >= 0; j--) {
			for (int i = 0; i < tempSquares.size(); i++) {
				TetrisSquare NextSquare = tempSquares.get(i);
				
				// If this square is at the next row, move it down
				if (NextSquare.y == j) {
					GameBoard[NextSquare.y][NextSquare.x] = false;
					NextSquare.SetXY(NextSquare.x, NextSquare.y + 1);
					GameBoard[NextSquare.y][NextSquare.x] = true; 
				}
				
			} // End for i = 0
		} // End for j = row
		
		TetrisSquares = (ArrayList<TetrisSquare>) tempSquares.clone();
		
	} // End RemoveRow()
	
	
	private void PrintGameBoard() {
		for (int i = 0; i < GameBoard.length; i++) {
			for (int j = 0; j < GameBoard[0].length; j++) {
				System.out.print((GameBoard[i][j] == true ? "1" : "0" ) + " ");
			}
			System.out.println();
		}
		
		System.out.println("\n");
	}
	
	
} // End class TetrisGame



// This class represents a single tetris square
class TetrisSquare {
	
	// Location on the GameBoard (NOT TRADITIONAL X,Y coordinates)
	int x;
	int y;
	
	// Color of this piece
	Color color;

	public TetrisSquare(int setX, int setY, Color c) {
		x = setX;
		y = setY;
		color = c;
	}
	
	public void SetXY(int setX, int setY) {
		x = setX;
		y = setY;
	}
	
	public void DrawSelf(Graphics g) {
		// Save draw color
		Color savedDrawColor = g.getColor();
		g.setColor(color);
		
		int ScaleFactor = 25;
		// TODO jump
		int actualX = (x-8)*ScaleFactor;
		int actualY = ((-y)+11)*ScaleFactor;

		// draw rectangle
		TetrisGame.isoFillRect(g, actualX, actualY, ScaleFactor, ScaleFactor);
		
		// Draw border of rectangle with black
		g.setColor(Color.black);
		TetrisGame.isoRect(g, actualX, actualY, ScaleFactor, ScaleFactor);
		
		// Restore previous draw color
		g.setColor(savedDrawColor);
	}
	
}










// Key Listener Class
class IsKeyPressed {
	// Note this is moreso implemented as "when pressed"
    public static volatile boolean spacePressed = false;
    
    // Note these are moreso implemented "while held"
    public static volatile boolean rightArrowPressed = false;
    public static volatile boolean leftArrowPressed = false;
    public static volatile boolean debugTPressed = false;
    public static volatile boolean downArrowPressed = false;
    public static volatile boolean rKeyHeld = false;
    
    public static boolean IsSpacePressed() {
        synchronized (IsKeyPressed.class) {
            return spacePressed;
        }
    }
    
    public static boolean IsRightArrowHeld() {
        synchronized (IsKeyPressed.class) {
            return rightArrowPressed;
        }
    }
    
    public static boolean IsLeftArrowHeld() {
        synchronized (IsKeyPressed.class) {
            return leftArrowPressed;
        }
    }
    
    public static boolean IsDebugKeyHeld() {
    	synchronized (IsKeyPressed.class) {
    		return debugTPressed;
    	}
    }
    
    public static boolean IsDownArrowHeld() {
    	synchronized (IsKeyPressed.class) {
    		return downArrowPressed;
    	}
    }
    
    public static boolean IsRestartHeld() {
    	synchronized (IsKeyPressed.class) {
    		return rKeyHeld;
    	}
    }

	public void KeyListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent ke) {
                synchronized (IsKeyPressed.class) {
                    switch (ke.getID()) {
                    case KeyEvent.KEY_PRESSED:
                        if (ke.getKeyCode() == KeyEvent.VK_SPACE) {
                        	spacePressed = true;
                        }
                        
                        else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
                        	rightArrowPressed = true;
                        }
                        else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
                        	leftArrowPressed = true;
                        }
                        else if (ke.getKeyCode() == KeyEvent.VK_T) {
                        	debugTPressed = true;
                        }
                        else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
                        	downArrowPressed = true;
                        }
                        else if (ke.getKeyCode() == KeyEvent.VK_R) {
                        	rKeyHeld = true;
                        }
                        break;

                    case KeyEvent.KEY_RELEASED:
                            if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
                            	rightArrowPressed = false;
                            }
                            
                            else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
                            	leftArrowPressed = false;
                            }
                            else if (ke.getKeyCode() == KeyEvent.VK_T) {
                            	debugTPressed = false;
                            }
                            else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
                            	downArrowPressed = false;
                            }
                            else if (ke.getKeyCode() == KeyEvent.VK_R) {
                            	rKeyHeld = false;
                            }
                        break;
                    }
                    return false;
                } // End synchronized()
            } // End dispatchKeyEvent()
        }); // End KeyEventDispatcher()
	} // End KeyListener
} // End class IsKeyPressed





class DestroyedTetrisPiece {
	
}

//Enumerator for Tetris Shapes
enum Shape {
	L,
	S,
	J,
	I,
	Z,
	O,
	T
}

// Note the above are only defined
// for 90-deg rotation
enum RotateDirection {
	CW,
	CCW
}

// NOTE:
//	This class only supports rotating 90-deg
//  clockwise or counter clockwise as that
//	is all that is needed for Tetris
class TetrisPiece {
	
	// Static Rotation Matrices
	static int RotateClockwise[] = {0, -1, 1, 0};
	static int RotateCClockwise[] = {0, 1, -1, 0};
	// Note the above are really:
	//	cw = [ 0, 1]  ccw = [0, -1]
	//       [-1, 0]        [1,  0]
	//
	
	// Each tetris piece has 4 squares that can be rotated
	// Defining them as a matrix now will aid in transformations
	// later.
	
	// Matrices are represented as:
	//
	// [x1]
	// [y1] = starting coordinates of square (relative
	// [x2]	  (to each other; unit vector based)
	// ...
	//
	int ShapeMatrix[] = new int[8];
	
	// This tracks all coordinates of all points
	// in ShapeMatrix[]
	int CurrentCoordinates[] = new int[8];
	
	// This pieces coordinates before a transformation
	// take place
	int OldCoordinates[] = new int[8];
	
	// Scale factor for this shape
	int ScaleFactor = 1;
	
	// Where this piece is currently located
	int thisX, thisY = 0;
	
	// Current Bottomost bound of this piece which can change
	// on transformations
	int bottomMostBound = 0;
	int upperMostBound = 0;
	int leftMostBound = 0;
	int rightMostBound = 0;
	
	// The shape of this piece
	Shape shape;
	
	// The Color of this piece
	Color color;
	
	public TetrisPiece(Shape s, int scale) {
		
		// Save this shape
		shape = s;
		
		switch (s) {
		case L:
			// Square one has x's at [0] and y's at [1]
			// and so on... (for other squares)
			ShapeMatrix[0] = 0;
			ShapeMatrix[1] = 0;
			
			// Square 2
			ShapeMatrix[2] = 0;
			ShapeMatrix[3] = -1;
			
			// Square 3
			ShapeMatrix[4] = 0;
			ShapeMatrix[5] = -2;
			
			// Square 4
			ShapeMatrix[6] = 1;
			ShapeMatrix[7] = -2;
			
			// Set Color
			color = Color.orange;
		break;
	
		case S:
			// Square one has x's at [0] and y's at [1]
			// and so on... (for other squares)
			ShapeMatrix[0] = 0;
			ShapeMatrix[1] = 0;
			
			// Square 2
			ShapeMatrix[2] = 1;
			ShapeMatrix[3] = 0;
			
			// Square 3
			ShapeMatrix[4] = 0;
			ShapeMatrix[5] = -1;
			
			// Square 4
			ShapeMatrix[6] = -1;
			ShapeMatrix[7] = -1;
			
			// Set Color
			color = Color.green;
		break;
		
		case J:
			// Square one has x's at [0] and y's at [1]
			// and so on... (for other squares)
			ShapeMatrix[0] = 0;
			ShapeMatrix[1] = 0;
			
			// Square 2
			ShapeMatrix[2] = 0;
			ShapeMatrix[3] = -1;
			
			// Square 3
			ShapeMatrix[4] = 0;
			ShapeMatrix[5] = -2;
			
			// Square 4
			ShapeMatrix[6] = -1;
			ShapeMatrix[7] = -2;
			
			// Set Color
			color = Color.blue;
		break;
		
		case I:
			// Square one has x's at [0] and y's at [1]
			// and so on... (for other squares)
			ShapeMatrix[0] = 0;
			ShapeMatrix[1] = 0;
			
			// Square 2
			ShapeMatrix[2] = 0;
			ShapeMatrix[3] = -1;
			
			// Square 3
			ShapeMatrix[4] = 0;
			ShapeMatrix[5] = -2;
			
			// Square 4
			ShapeMatrix[6] = 0;
			ShapeMatrix[7] = -3;
			
			// Set Color
			color = Color.cyan;
		break;
		
		case O:
			// Square one has x's at [0] and y's at [1]
			// and so on... (for other squares)
			ShapeMatrix[0] = 0;
			ShapeMatrix[1] = 0;
			
			// Square 2
			ShapeMatrix[2] = 1;
			ShapeMatrix[3] = 0;
			
			// Square 3
			ShapeMatrix[4] = 0;
			ShapeMatrix[5] = -1;
			
			// Square 4
			ShapeMatrix[6] = 1;
			ShapeMatrix[7] = -1;
			
			// Set Color
			color = Color.yellow;
		break;
		
		case Z:
			// Square one has x's at [0] and y's at [1]
			// and so on... (for other squares)
			ShapeMatrix[0] = 0;
			ShapeMatrix[1] = 0;
			
			// Square 2
			ShapeMatrix[2] = -1;
			ShapeMatrix[3] = 0;
			
			// Square 3
			ShapeMatrix[4] = 0;
			ShapeMatrix[5] = -1;
			
			// Square 4
			ShapeMatrix[6] = 1;
			ShapeMatrix[7] = -1;
			
			// Set Color
			color = Color.red;
		break;
		
		case T:
			// Square one has x's at [0] and y's at [1]
			// and so on... (for other squares)
			ShapeMatrix[0] = 0;
			ShapeMatrix[1] = 0;
			
			// Square 2
			ShapeMatrix[2] = -1;
			ShapeMatrix[3] = 0;
			
			// Square 3
			ShapeMatrix[4] = 1;
			ShapeMatrix[5] = 0;
			
			// Square 4
			ShapeMatrix[6] = 0;
			ShapeMatrix[7] = 1;
			
			// Set Color
			color = Color.pink;
		break;
		
	} // End switch(Shape)
	
	// Set scale and calculate bounds
	ScaleFactor = scale;
	CalculateBounds();
	CalculatePoints();

	} // End TetrisPiece() constructor

	
	public void SetXY(int x, int y) {
		thisX = x;
		thisY = y;
		CalculatePoints();
	}
	
	// Draws this piece at coordinates x and y on graphics context g
	public void DrawPiece(Graphics g) {
		
		// Color this game piece
		Color currentDrawColor = g.getColor();
		g.setColor(color);
		
		// Need to draw rectangles (squares)
		// Requires starting x, y and then width + height

		// i will be x, j y; loops 4 times
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			int thisSquareX = ShapeMatrix[i];
			int thisSquareY = ShapeMatrix[j];
			
			// Calculate next x and y
			int nextX = ( thisSquareX * ScaleFactor) + thisX;
			int nextY = ( thisSquareY * ScaleFactor) + thisY;
			
			// Fill rectangle with specific color
			g.setColor(color);
			TetrisGame.isoFillRect(g, nextX, nextY, ScaleFactor, ScaleFactor);
			
			// Draw border of rectangle with black
			g.setColor(Color.black);
			TetrisGame.isoRect(g, nextX, nextY, ScaleFactor, ScaleFactor);
		}
		
		// Reset draw color
		g.setColor(currentDrawColor);
		
	} // End DrawPiece(g)
	
	// Draws this piece at coordinates x and y on graphics context g
	public void DrawPiece(Graphics g, int x, int y) {
		
		// Color this game piece
		Color currentDrawColor = g.getColor();
		g.setColor(color);
		
		// Need to draw rectangles (squares)
		// Requires starting x, y and then width + height
		
		// i will be x, j y; loops 4 times
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			int thisSquareX = ShapeMatrix[i];
			int thisSquareY = ShapeMatrix[j];
			
			// Calculate next x and y
			int nextX = ( thisSquareX * ScaleFactor) + x;
			int nextY = ( thisSquareY * ScaleFactor) + y;
			
			// Fill rectangle with specific color
			g.setColor(color);
			TetrisGame.isoFillRect(g, nextX, nextY, ScaleFactor, ScaleFactor);
			
			// Draw border of rectangle with black
			g.setColor(Color.black);
			TetrisGame.isoRect(g, nextX, nextY, ScaleFactor, ScaleFactor);
		}
		
		// Reset draw color
		g.setColor(currentDrawColor);
		
	} // DrawPiece(g, x, y)
	
	// Transforms piece given a Rotation direction
	public void TransformPiece(RotateDirection dir) {
		
		// Ensure transforming is safe (within x-bounds)
		
		// Squares cannot transform
		if (shape == Shape.O)
			return;
		
		// Save the old coordinates before transforming
		OldCoordinates = CurrentCoordinates.clone();
		
		int transformation[] = new int[4];
		switch (dir) {
			case CW:
				transformation = RotateClockwise;
			break;
			
			case CCW:
				transformation = RotateCClockwise;
			break;
		}
		
		// Get rows of transformation for matrix multiplication
		int tRow1[] = {transformation[0], transformation[2]};
		int tRow2[] = {transformation[1], transformation[3]};
		
		// Result Matrix
		int ShapeMatrixTransformed[] = new int [8];
		
		// Boolean for switching to next row
		boolean nextRow = false;
		
		// Transform shape matrix; note that this is 
		// a disguised form of matrix multiplication
		for (int i = 0, j = 1, iter = 0; iter < 8; i+=2, j+=2, iter++) {
			// Note that iter is our true loop variable. 
			int thisSquareXY[] = { ShapeMatrix[i], ShapeMatrix[j] };
			
			int nextValue;
			if (!nextRow) {
				// Perform DotProduct with tRow1
				nextValue = DotProduct(tRow1, thisSquareXY);
				// Store new value
				ShapeMatrixTransformed[i] = nextValue;
			} else {
				// Perform DotProduct with tRow2
				nextValue = DotProduct(tRow2, thisSquareXY);
				// Store new value
				ShapeMatrixTransformed[j] = nextValue;
			}
			
			// Once i >= 8, i and j need to be reset
			// This will happen at loop iter = 3, and 
			// we will use tRow2 instead of tRow1
			if (i >= 6) { 
				i = 0; 
				j = 1; 
				nextRow = true; 
			}
		} // End for i,j,iter
		
		// Verify this transformation will be safe, if it will not be
		// then escape
		if (EnsureSafeTransformation(ShapeMatrixTransformed) == false) {
			return;
		} 
		
		// Otherwise, transformation is safe
		
		// Once we've performed the transformation, set it back
		ShapeMatrix = ShapeMatrixTransformed;
		
		// Calculate new coordinates
		CalculatePoints();
		CalculateBounds();
		
	} // End  TransformPiece()
	
	public boolean EnsureSafeTransformation(int sMatrix[]) {
		
		// Need to make sure we can only transform when its safe.
		
		// Verify that the transformation is safe by ensuring all X-values are
		// within the play area bounds
		int TemporaryCoordinates[] = new int[8];
		
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			TemporaryCoordinates[i] = ( sMatrix[i]*ScaleFactor ) + thisX;
			TemporaryCoordinates[j] = ( sMatrix[j]*ScaleFactor ) + thisY;
			if ( TemporaryCoordinates[i] < -200 || TemporaryCoordinates[i] > 25 ||
				 TemporaryCoordinates[j] < -225) 
				return false;
		}
		
		return true;
		
	} // End CalculatePoints()
	
	public void CalculatePoints() {

		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			// map x,y to correct coordinate planes
			// TODO jump
			// Add 8 and 11 to x, y to allow for array indexing
			CurrentCoordinates[i] = ( ShapeMatrix[i] + (thisX / ScaleFactor) ) + 8;
			CurrentCoordinates[j] = -( ShapeMatrix[j] + (thisY / ScaleFactor) ) + 11;

		}
		
	} // End CalculatePoints()
	
	public void PrintCoordinates() {
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			System.out.print("(" + CurrentCoordinates[i] + ", " + CurrentCoordinates[j] + ")");
			if (i!= 7) {
				System.out.print(", ");
			}
		}
		System.out.println();
	}
	
	public void PrintOldCoordinates() {
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			System.out.print("(" + OldCoordinates[i] + ", " + OldCoordinates[j] + ")");
			if (i!= 7) {
				System.out.print(", ");
			}
		}
		System.out.println();
	}
	
	

	// Calculates the lowermost bound for this piece
	private void CalculateBounds() {
		// Jump through the Y-values and find
		// the highest value
		int highestY = ShapeMatrix[0];
		int lowestY = ShapeMatrix[0];
		int lowestX = ShapeMatrix[0];
		int highestX = ShapeMatrix[0];
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			if (highestY < ShapeMatrix[j]) {
				highestY = ShapeMatrix[j];
			}
			
			if (lowestY > ShapeMatrix[j]) {
				lowestY = ShapeMatrix[j];
			}
			
			if (highestX < ShapeMatrix[i]) {
				highestX = ShapeMatrix[i];
			}
			
			if (lowestX > ShapeMatrix[i]) {
				lowestX = ShapeMatrix[i];
			}
		}
		
		// Set upperMostBound to highestY+1 * ScaleFactor
		upperMostBound = (highestY + 1) * ScaleFactor;
		
		// Set bottomMostBound to lowest Y * ScaleFactor
		bottomMostBound = lowestY * ScaleFactor;
		
		// Set leftMostBound to lowestX*ScaleFactor
		leftMostBound = lowestX * ScaleFactor;
		//leftMostBound = lowestX + (thisX / ScaleFactor);
		
		// Set rightMostbound to highestX+1 * ScaleFactor
		rightMostBound = (highestX + 1) * ScaleFactor;
		//rightMostBound = highestX + (thisX / ScaleFactor);
	}
	
	
	// Not this is for integers only.
	private static int DotProduct(int a[], int b[]) {
		
		// If lengths are not equal, Dot product is 
		// not a valid operations on these vectors.
		if (a.length != b.length) {
			return -1;
		}
		
		// Return value
		int Result = 0;
		
		// Perform dot product
		for (int i = 0; i < a.length; i++) {
			Result += a[i] * b[i];
		}
		
		// return
		return Result;
	} // End DotProduct()
	
	
} // End class TetrisPiece


