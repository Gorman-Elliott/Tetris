// RedRect.java: The largest possible rectangle in red.

// Copied from Section 1.1 of
//    Ammeraal, L. and K. Zhang (2007). Computer Graphics for Java Programmers, 2nd Edition,
//       Chichester: John Wiley.

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JPanel;
import javax.swing.Timer;
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
	
	// Bool to check if this is the first run
	boolean First = true;
	
	// Boolean to check if we transformed this frame
	boolean Transformed = false;
	
	// Variables to aide in Isotropic Mapping
    int centerX, centerY;
    float pixelSize;
    float rWidth = (float) CanvasSizeX;
    float rHeight = (float) CanvasSizeY;
	
	public TetrisGame() {
		
		// Attach Listener for game loop
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                TetrisGameLoop();
            }
        });
	}
	
	void initgr() {
		// Set canvas size
		Dimension d = getSize();
		CanvasSizeX = d.width;
		CanvasSizeY = d.height;
		rWidth = CanvasSizeX;
		rHeight = CanvasSizeY;
		
//		Dimension d = getSize();
		int maxX = d.width - 1;
		int maxY = d.height - 1;
		pixelSize = Math.max(rWidth / maxX, rHeight / maxY);
		centerX = maxX / 2; 
		centerY = maxY / 2;
	}
	
	
	// Isotropic mapping functions
	int iX(float x) { return Math.round(centerX + x / pixelSize); }
	int iY(float y) { return Math.round(centerY - y / pixelSize); }
	float fx(int x) {return (x - centerX) * pixelSize;}
	float fy(int y) {return (centerY - y) * pixelSize;}
	
	
	private void TetrisGameLoop() {
		// Register a KeyBoard Listener
		new IsKeyPressed().KeyListener();
		
		// Loop
        Timer timer = new Timer(140, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	// Ready the next frame for painting
                ReadyNextFrame();
                
        		// Draw the next frame
        		repaint();
            }
        });
        timer.start();
		
	}// End TetrisGameLoop()
	
	
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Recalculate isotropic grid coordinates
		initgr();
		
		// Draw GUI
		// WORKING HERE 9/2/2021
		g.drawRect(iX(-(CanvasSizeX/2) + playAreaPaddingX), iY((CanvasSizeY/2) - playAreaPaddingY), 
				   (int) (0.55556F*CanvasSizeX), (int) (0.833334F*CanvasSizeY));
		
		//g.drawRect(25, 25, playAreaX, playAreaY);
		g.drawRect(25, playAreaY + playAreaPaddingY, 25, 25);
		
		// Redraw game pieces
		UpdateGamePieces(g);
	} // End paint()
	
	
	
	private void UpdateGamePieces(Graphics g) {
		// Draw all game pieces
		for (int i = 0; i < GamePieces.size(); i++) {
			GamePieces.get(i).DrawPiece(g);
//			TetrisPiece piece = GamePieces.get(i);
//			piece.DrawPiece(g, piece.thisX, piece.thisY);
		}
	}

	private void ReadyNextFrame() {
		
		if (First) {
			// Initialize GameBoard
			for (int i = 0; i < GameBoard.length; i++) {
				for (int j = 0; j < GameBoard[0].length; j++) {
					GameBoard[i][j] = false;
				}
			}
			First = false;
		}
		
		if (NextPiece) {
			// Randomly select a new game piece
			int rand = ThreadLocalRandom.current().nextInt(0, 7);
			GamePieces.add(new TetrisPiece(LegalShapes[rand], 25));
			//GamePieces.add(new TetrisPiece(Shape.J, 25));
			
			//GamePieces.add(new TetrisPiece(Shape.S, 25));
			NextPieceIndex = GamePieces.size() - 1;
			//GamePieces.get(NextPieceIndex).SetXY( iX(-(CanvasSizeX/2) + playAreaPaddingX + 100), iY((CanvasSizeY/2) - 25) );
			//GamePieces.get(NextPieceIndex).SetXY( iX(-(CanvasSizeX/4) + playAreaPaddingX), iY((CanvasSizeY/2) - 25) );
			GamePieces.get(NextPieceIndex).SetXY((int) (0.333334F*CanvasSizeX), 25);
			GamePieces.get(NextPieceIndex).CalculatePoints();
			NextPiece = false;
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
			if (thisPiece.rightMostBound + thisPiece.thisX <= 250) {
				thisPiece.SetXY(thisPiece.thisX + 25, thisPiece.thisY);
				currentPieceOffsetX -= 1;
			}
		}
		
		if (IsKeyPressed.IsLeftArrowHeld()) {
			TetrisPiece thisPiece = GamePieces.get(NextPieceIndex);
			// Move to the left if not going out of bounds
			if (thisPiece.leftMostBound + thisPiece.thisX > 25) {
				thisPiece.SetXY(thisPiece.thisX - 25, thisPiece.thisY);
				currentPieceOffsetX += 1;
			}
		}
		
		// Get the current game piece
		TetrisPiece currentPiece = GamePieces.get(NextPieceIndex);
		
		// Make sure piece stops when hitting the lower bounds
		// and setup to create new piece
		if (currentPiece.bottomMostBound + currentPiece.thisY >= playAreaBottomBound) {
			NextPiece = true;
			//test = true;
		} 
		else if (!CanMovePieceDown(currentPiece, currentPieceOffsetX)) {
			NextPiece = true;
		} else {
			// Otherwise, tick game piece downward
			currentPiece.SetXY(currentPiece.thisX, currentPiece.thisY + 25);
		}
		
		// Make sure our coordinates are up to date
		currentPiece.CalculatePoints();
		
		// Update the GameBoard
		UpdateGameBoard(currentPiece, currentPieceOffsetX);
		
		PrintGameBoard();
		
		// Reset Transformed
		Transformed = false;
	
	} // End ReadyNextFrame()
	
	private void UpdateGameBoard(TetrisPiece piece, int XOffset) {
		// For the current game piece, update it's position
		// on the game board
		
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			int thisSquareX;
			int thisSquareY;
			
			if (Transformed == true) {
				thisSquareX = piece.OldCoordinates[i] + XOffset;
				thisSquareY = piece.OldCoordinates[j];

				// Note the below positions are offset -1 for array indexing
				// Set previous position (y-1) as false
				GameBoard[piece.OldCoordinates[j] - 1][piece.OldCoordinates[i] - 1] = false;
			} else {
				thisSquareX = piece.CurrentCoordinates[i] + XOffset;
				thisSquareY = piece.CurrentCoordinates[j];
				
				piece.PrintCoordinates();
				
				// Note the below positions are offset -1 for array indexing
				// Set previous position (y-1) as false
				GameBoard[thisSquareY-1][thisSquareX-1] = false;
				GameBoard[thisSquareY-2][thisSquareX-1] = false;
			}
			
		} // End for i,j (removing old position from board)
		
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			int thisSquareX = piece.CurrentCoordinates[i];
			int thisSquareY = piece.CurrentCoordinates[j];

			// Note the below positions are offset -1 for array indexing
			// Set new position as true
			GameBoard[thisSquareY-1][thisSquareX-1] = true;
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
			
			// If Any (x, y+1) is false, then return false
			// y-1 for array indexing
			try {
				if (GameBoard[thisSquareY][thisSquareX-1] == true) {
					// If this coordinate is part of itself, then don't move down
					if (!IsCoordPartOfSelf(piece, thisSquareX, thisSquareY+1, XOffset)) {
						// Return false and create a new piece.
						return false;
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				// If we went outof bounds, do nothing
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
			
			//System.out.println("(" + thisSquareX + ", " + thisSquareY + "), " + "(" + x + ", " + y + ")");
			
			// If x, y is one of piece's coordinates, then we are looking at 
			// ourselves. Return false.
			
			if (Transformed) {
				// If the shape has been transformed, make sure it's not checking any
				// of its' previous points
				if ((piece.OldCoordinates[i] + XOffset) == x && piece.OldCoordinates[j] == y)
					return true;
				
			} else {
				if (thisSquareX == x && thisSquareY == y)
					return true;
			}
		}
		//System.out.println("");
		return false;
	}
	
	private void PrintGameBoard() {
		for (int i = 0; i < GameBoard.length; i++) {
			for (int j = 0; j < GameBoard[0].length; j++) {
				System.out.print((GameBoard[i][j] == true ? "1" : "0" ) + " ");
			}
			System.out.println();
		}
		
		System.out.println("\n");
	}
	
//	private boolean PieceCollisionDetection(TetrisPiece piece) {
//		// Loop through current GamePieces in play ...
//		
//		/*
//		 * Our goal is to determine whether or not our current piece (piece)
//		 * has hit any of our other game pieces.
//		 * 
//		 * We can do this by checking Y-bounds. if our current piece y's 
//		 * bottom-most bound is >= another pieces topmost bound, return true
//		 */
//		
//		// Note piece's points represent (x1, y1)
//		
//		for (int i = 0; i < GamePieces.size(); i++) {
//			// Get piece to check (x2, y2)
//			TetrisPiece comparePiece = GamePieces.get(i);
//			if (comparePiece.equals(piece))
//				continue;
//			
//			for (int j = 0, p = 1; j < 8; j+=2, p+=2) {
//				if (Math.abs(piece.CurrentCoordinates[p-1] - comparePiece.CurrentCoordinates[p-1]) == 0) {
//					// Verify the x coordinate for this y coordinate is valid
//					if (piece.bottomMostBound + piece.thisY >= comparePiece.upperMostBound + comparePiece.thisY) {
//						return true;
//					}
//					
//				} // End for j, p
//				
//			}
//			
////			for (int j = 0, p = 1; j < 8; j+=2, p+=2) {
////				if (piece.CurrentCoordinates[p] + 1 >= comparePiece.CurrentCoordinates[p]) {
////					// Verify the x coordinate for this y coordinate is valid
////					if (Math.abs(piece.CurrentCoordinates[p-1] - comparePiece.CurrentCoordinates[p-1]) < 1) {
////						return true;
////					}
////					
////				}
////				
////			} // End for j, p
//		} // End for i
//		
//		// Else, return false
// 		return false;
//	}
	
} // End class TetrisGame


// Key Listener Class
class IsKeyPressed {
	// Note this is moreso implemented as "when pressed"
    public static volatile boolean spacePressed = false;
    
    // Note these are moreso implemented "while held"
    public static volatile boolean rightArrowPressed = false;
    public static volatile boolean leftArrowPressed = false;
    
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
                        break;

                    case KeyEvent.KEY_RELEASED:
                            if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
                            	rightArrowPressed = false;
                            }
                            
                            else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
                            	leftArrowPressed = false;
                            }
                        break;
                    }
                    return false;
                } // End synchronized()
            } // End dispatchKeyEvent()
        }); // End KeyEventDispatcher()
	} // End KeyListener
} // End class IsKeyPressed


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
				ShapeMatrix[3] = 1;
				
				// Square 3
				ShapeMatrix[4] = 0;
				ShapeMatrix[5] = 2;
				
				// Square 4
				ShapeMatrix[6] = 1;
				ShapeMatrix[7] = 2;
				
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
				ShapeMatrix[5] = 1;
				
				// Square 4
				ShapeMatrix[6] = -1;
				ShapeMatrix[7] = 1;
				
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
				ShapeMatrix[3] = 1;
				
				// Square 3
				ShapeMatrix[4] = 0;
				ShapeMatrix[5] = 2;
				
				// Square 4
				ShapeMatrix[6] = -1;
				ShapeMatrix[7] = 2;
				
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
				ShapeMatrix[3] = 1;
				
				// Square 3
				ShapeMatrix[4] = 0;
				ShapeMatrix[5] = 2;
				
				// Square 4
				ShapeMatrix[6] = 0;
				ShapeMatrix[7] = 3;
				
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
				ShapeMatrix[5] = 1;
				
				// Square 4
				ShapeMatrix[6] = 1;
				ShapeMatrix[7] = 1;
				
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
				ShapeMatrix[5] = 1;
				
				// Square 4
				ShapeMatrix[6] = 1;
				ShapeMatrix[7] = 1;
				
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
	
	} // End TetrisShape()

	
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
			
			// Fill rectangle with specific color
			g.setColor(color);
			g.fillRect((thisSquareX * ScaleFactor) + thisX, (thisSquareY * ScaleFactor) + thisY, ScaleFactor, ScaleFactor);
			
			// Draw border of rectangle with black
			g.setColor(Color.black);
			g.drawRect((thisSquareX * ScaleFactor) + thisX, (thisSquareY * ScaleFactor) + thisY, ScaleFactor, ScaleFactor);
		}
		
		// Reset draw color
		g.setColor(currentDrawColor);
		
	}
	
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
			
			// Fill rectangle with specific color
			g.setColor(color);
			g.fillRect((thisSquareX * ScaleFactor) + x, (thisSquareY * ScaleFactor) + y, ScaleFactor, ScaleFactor);
			
			// Draw border of rectangle with black
			g.setColor(Color.black);
			g.drawRect((thisSquareX * ScaleFactor) + x, (thisSquareY * ScaleFactor) + y, ScaleFactor, ScaleFactor);
		}
		
		// Reset draw color
		g.setColor(currentDrawColor);
		
	}
	
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
		
		// Verify that the transformation is safe by ensuring all X-values are
		// within the play area bounds
		
		int TemporaryCoordinates[] = new int[8];
		
		System.out.println();
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			TemporaryCoordinates[i] = (ShapeMatrix[i]*ScaleFactor + thisX);
			if ( TemporaryCoordinates[i] < 25 || TemporaryCoordinates[i] >= 225 ) 
				return false;
		}
		
		return true;
		
	} // End CalculatePoints()
	
	public void CalculatePoints() {
		
		System.out.println();
		for (int i = 0, j = 1; i < 8; i+=2, j+=2) {
			CurrentCoordinates[i] = (ShapeMatrix[i] + (thisX / ScaleFactor));
			CurrentCoordinates[j] = (ShapeMatrix[j] + (thisY / ScaleFactor));
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
		
		// Set bottomMostBound to highestY+1 * ScaleFactor
		bottomMostBound = (highestY + 1) * ScaleFactor;
		
		// Set upperMostBound to lowest Y * ScaleFactor
		upperMostBound = lowestY * ScaleFactor;
		
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







































