/*\
 * Name: Thien Vu
 * ID: 103-09-263
 * Date: 11-2-2021
 * Assignment #3: An Intelligent Othello Player
 * Desc: A game of Othello, along with an AI to play with if you have no friends :(
\*/
import java.io.*;
import java.util.*;
//the game
public class Othello {
	//main
	public static void main(String[] args) throws IOException {
		//get input
		Scanner input = new Scanner(System.in);
		//menu selection
		while (true) {
			System.out.println("[1] Play with a friend (or yourself)");
			System.out.println("[2] Play with an AI, you go first");
			System.out.println("[3] Play with an AI, AI goes first");
			System.out.println("[0] Exit");
			System.out.print("Input: ");
			String selection = input.nextLine();
			switch(selection) {
			//pvp
			case "1":
				runGame(input, false, false);
				break;
			//pve
			case "2":
				runGame(input, true, false);
				break;
			//evp
			case "3":
				runGame(input, true, true);
				break;
			//quit
			case "0":
				System.out.println("Sayonara!");
				input.close();
				System.exit(0);
			default:
				System.out.println("Invalid input.");
			}
			
		}	

	}

	//set up the board
	static char[][] othelloStart() {
		char[][] board = new char[8][8];
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				board[i][j] = '.';
			}
		}
		board[3][3] = 'W';
		board[3][4] = 'B';
		board[4][3] = 'B';
		board[4][4] = 'W';
		return board;
	}

	//display the board
	static void printB(char[][] board) {
		System.out.println("  0  1  2  3  4  5  6  7");
		for (int i = 0; i < board.length; i++) {
			System.out.print((char)(i + 'a') + " ");
			for (int j = 0; j < board[0].length; j++) {
				System.out.print(board[i][j] + "  ");
			}
			System.out.println("");
		}
	}



	//parse player input
	static int[] parseInput(String[] position) {
		int[] yx = new int[2];
		//check if input is limited to 2 chars, 
		if (position.length != 2) {
			setInv(yx);
			return yx;
		}
		//try to turn inputs into numerical x and y
		try {
			yx[0] = position[0].charAt(0) - 'a';
			yx[1] = Integer.parseInt(position[1]);
		}
		//if it fails make it all -1
		catch(Exception e) {
			setInv(yx);
		}
		//check if x and y are out of range
		for (int i = 0; i < yx.length; i++)
			if ((yx[i] < 0) || (yx[i] > 7))
				setInv(yx);
		//return yx
		return yx;
	}

	//check legality of player move
	static int[] checkLegal(char[][] board, int[] yx, char oppPlayer, char currPlayer, boolean flipping) {
		boolean legal = false;
		//check if input is invalid
		if (checkInv(yx))
			return yx;
		//check if position on board is occupied
		if(board[yx[0]][yx[1]] != '.') 
			setInv(yx);
		//check surrounding positions for the inverse of the current player
		boolean[] surrounded = checkSurr(board, yx, oppPlayer, currPlayer);
		for (int i = 0; i < surrounded.length; i++)
			if (surrounded[i]) {
				if (flipping)
					flipDirection(board, yx, currPlayer, i);
				legal = true;
			}
		if (!legal)
			setInv(yx);
		return yx;
	}

	//place the disc
	static boolean placeDisc(char[][] board, int[] yx , char currPlayer, char oppPlayer, List<int[]> legalMoves) {
		boolean isMoveLegal = false;
		//if yx is -1, input is invalid or move is illegal. dont do anything.
		if (checkInv(yx)){
			return false;
		}
		for (int i = 0; i < legalMoves.size(); i++) {
			if (yx[0] == legalMoves.get(i)[0] && yx[1] == legalMoves.get(i)[1]) {
				isMoveLegal = true;
				//used checkLegal to also just flip every possible points too
				checkLegal(board, yx, oppPlayer, currPlayer, true);
				board[yx[0]][yx[1]] = currPlayer;
			}
		}

		return isMoveLegal;
	}

	//check if there is an opposing piece next to position
	//returns true if there is one
	static boolean[] checkSurr(char[][] board, int[] yx, char oppPlayer, char currPlayer) {
		boolean[] surrounded = new boolean[board.length];
		for (int i = 0; i < surrounded.length; i++)
			surrounded[i] = false;
		if (checkInv(yx))
			return surrounded;

		//if even one of these is good the whole thing is good
		int[] yxDir = new int[2];
		for (int i = 0; i < surrounded.length; i++) {
			setMode(i, yxDir);
			if ((yx[0] + yxDir[0] >= 0 && yx[0] + yxDir[0] < board.length)
					&&
					(yx[1] + yxDir[1] >= 0 && yx[1] + yxDir[1] < board.length)) {
				surrounded[i] = (board[yx[0] + yxDir[0]][yx[1] + yxDir[1]] == oppPlayer);
				if (surrounded[i])
					surrounded[i] = checkDirection(board, yx, currPlayer, i);
			}
		}
		return surrounded;
	}

	//checking directions to see if it can flank pieces
	//goes in a direction until it either finds an empty position, a boundary
	//or a position that the player holds
	static boolean checkDirection(char[][] board, int[]yx, char currPlayer, int mode) {
		int [] yxDir = new int[2];
		setMode(mode, yxDir);
		for (int x = yxDir[1], y = yxDir[0];
				//while x and y are within bounds
				(yx[0] + y >= 0 && yx[0] + y < board.length)
				&&
				(yx[1] + x >= 0 && yx[1] + x < board.length);){
			//if it hits an empty position, return false
			if (board[yx[0] + y][yx[1] + x] == '.')
				return false;
			//if it hits a position it controls, return true
			if (board[yx[0] + y][yx[1] + x] == currPlayer)
				return true;
			//add xdir to x and ydir to y
			y += yxDir[0];																										
			x += yxDir[1];

		}
		return false;
	}

	//flips positions to be current color
	static void flipDirection(char[][] board, int[]yx, char currPlayer, int mode) {
		int [] yxDir = new int[2];
		setMode(mode, yxDir);
		for (int x = 0, y = 0;
				//while x and y are within bounds
				(yx[0] + y >= 0 && yx[0] + y < board.length)
				&&
				(yx[1] + x >= 0 && yx[1] + x < board.length);){
			//add xdir to x and ydir to y
			y += yxDir[0];
			x += yxDir[1];
			//if it hits a position it controls, return true
			if (board[yx[0] + y][yx[1] + x] == currPlayer)
				break;
			board[yx[0] + y][yx[1] + x] = currPlayer;
		}

	}

	//checks if current player can even make a move
	public static List<int[]> moveable(char[][] board, char currPlayer, char oppPlayer) {
		int[] xy = new int[2];
		int[] legalPos;
		List<int[]> legalMoves = new ArrayList<int[]>();
		for (int i = 0; i < board.length; i++)
			for (int j = 0; j < board[0].length; j++) {
				if (board[i][j] == '.') {
					xy[0] = i;
					xy[1] = j;
					legalPos = checkLegal(board, xy, oppPlayer, currPlayer, false);
					if (legalPos[0] != -1 && legalPos[1] != -1) {
						int[] savePos = new int[2];
						savePos[0] = xy[0];
						savePos[1] = xy[1];
						legalMoves.add(savePos);
					}
				}
			}
		return legalMoves;
	}

	//modes: tl = 0
	//		 tm = 2 
	//		 tr = 3
	//		 ml = 4
	//		 mr = 5
	//		 bl = 6
	//		 bm = 7
	//		 br = 8
	static void setMode(int mode, int[]yxDir) {
		switch(mode) {
		case 0:
			yxDir[0] = -1; yxDir[1] = -1; break;
		case 1:
			yxDir[0] = -1; yxDir[1] = 0; break;
		case 2:
			yxDir[0] = -1; yxDir[1] = 1; break;
		case 3:
			yxDir[0] = 0; yxDir[1] = -1; break;
		case 4:
			yxDir[0] = 0; yxDir[1] = 1; break;
		case 5:
			yxDir[0] = 1; yxDir[1] = -1; break;
		case 6:
			yxDir[0] = 1; yxDir[1] = 0; break;
		case 7:
			yxDir[0] = 1; yxDir[1] = 1; break;			
		}
	}
	
	//set movement to be invalid
	static void setInv(int[] yx) {
		yx[0] = -1;
		yx[1] = -1;
	}

	//check if its already invalid
	static boolean checkInv(int[] yx) {
		if ((yx[0] == -1) || (yx[1] == -1))
			return true;
		return false;
	}


	//run game
	//modes: 1 = with a friend
	//		 2 = with an AI
	static void runGame(Scanner input, boolean aiPlay, boolean aiFirst) {
		//var declare
		char currPlayer = 'B';
		char oppPlayer = 'W';
		char tempSwap;
		boolean runningGame = true;
		boolean turnAI = aiFirst;
		//int[][] hBoard = defaultHeuristics();
		char[][]board = othelloStart();
		int numUnable = 0;
		int numW, numB;
		//main loop
		while(runningGame) {
			printB(board);
			List<int[]> legalMoves = moveable(board, currPlayer, oppPlayer);
			//check if current player is able to make move, 
			//switch player if current player is unable to
			if (legalMoves.size() == 0) {
				System.out.println(currPlayer + " is unable to make a move. Switching turns.");
				tempSwap = currPlayer;
				currPlayer = oppPlayer;
				oppPlayer = tempSwap;
				numUnable += 1;
			}
			//if current player is able to,
			//make a move
			else {
				numUnable = 0;
				int[]yx;
				//if ai mode and ai turn
				if (aiPlay && turnAI) {
					System.out.println(currPlayer + "(AI) turn.");
					//get 
					boolean ynGet = false;
					boolean debug = false;
					while (!ynGet) {
						System.out.print("Debug? [y/n] ");
						String yn = input.nextLine().strip();
						if (yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("n")) {
							ynGet = true;
							if(yn.equalsIgnoreCase("y")) {
								debug = true;
							}
						}
						else {
							System.out.println("Invalid input. Please try again.");
						}
					}
					ynGet = false;
					//get prune or not
					boolean prune = true;
					while (!ynGet) {
						System.out.print("Prune? [y/n] ");
						String yn = input.nextLine().strip();
						if (yn.equalsIgnoreCase("y") || yn.equalsIgnoreCase("n")) {
							ynGet = true;
							if(yn.equalsIgnoreCase("n")) {
								prune = false;
							}
						}
						else {
							System.out.println("Invalid input. Please try again.");
						}
					}
					//make tree for possible moves
					Tree moveTree = new Tree(board, currPlayer, oppPlayer);
					yx = moveTree.makeBestMove(prune);
					if (debug) {
						String moveList = "Possible moves:\n";
							for (int i = 0; i < moveTree.root.children.size(); i++) {
								moveList += (char)(moveTree.root.children.get(i).currentMove[0] + 'a') + ""
										+ moveTree.root.children.get(i).currentMove[1] + " Heuristic value: " + 
										moveTree.root.children.get(i).heuristic + "\n";
							}
							System.out.print(moveList);
							System.out.println("States evaluated: " + moveTree.statesEval);
						}
					System.out.println("Position: " + (char)(yx[0] + 'a') + "" + yx[1]);
				}
				//else
				else {
					System.out.println(currPlayer + "(Human) turn.");
					System.out.print("Position: ");
					yx = parseInput(input.nextLine().split(""));
				}
				//have him make a move, and determine is move is legal
				boolean swap = placeDisc(board, yx, currPlayer, oppPlayer, legalMoves);
				//if legal, swap players
				if (swap) {
					tempSwap = currPlayer;
					currPlayer = oppPlayer;
					oppPlayer = tempSwap;
					turnAI = !turnAI;
				}
				//if not, have him retry
				else {
					System.out.println("Invalid play.");
				}
			}
			//if both players are unable to make a move
			//end the game
			if (numUnable == 2) {
				System.out.println("No players are able to make a move, ending game.");
				runningGame = false;
			}
		}
		//get number of discs each player has
		numW = numB = 0;
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				if (board[i][j] == 'W') {
					numW += 1;
				}
				if (board[i][j] == 'B') {
					numB += 1;
				}
			}
		}
		//winner selection
		if (numW > numB) {
			System.out.println("The winner is white.");
		}
		else if (numB > numW) {
			System.out.println("The winner is black.");
		}
		else {
			System.out.println("It is a tie.");
		}
	}


}


//AI STUFF
//node
class Node {
	Node parent;
	List<int[]> legalMoves;
	List<Node> children;
	char[][] board;
	int[] currentMove;
	char currPlayer;
	char oppPlayer;
	int heuristic;
	int bestChildIndx;
	//make an empty node
	public Node() {
		children = new ArrayList<Node>();
	}
	
	//make a node
	public Node(int[] position) {
		currentMove = position;
	}
	
	//copy a given board to the node
	public void copyBoard(char[][]pBoard){
		board = new char[pBoard.length][pBoard[0].length];
		for (int i = 0; i < board.length; i++) 
			for (int j = 0; j < board[0].length; j++)
				board[i][j] = pBoard[i][j];
	}

}

//tree
class Tree {
	Node root;
	int statesEval = 0;
	int maxDepth = 7;
	int counter = 0;
	int[][] hBoard = defaultHeuristics();
	//make tree
	public Tree(char[][] board, char currPlayer, char oppPlayer) {
		makeRoot(board, currPlayer, oppPlayer);
	}
	
	//setup the root
	public void makeRoot(char[][] board, char currPlayer, char oppPlayer) {
		root = new Node();
		root.currPlayer = currPlayer;
		root.oppPlayer = oppPlayer;
		root.board = board;
		//get legal moves for root
		root.legalMoves = Othello.moveable(root.board, root.currPlayer, root.oppPlayer);
		makeChildren(root, maxDepth, root.legalMoves);
	}
	
	//make children recursively
	//copies board from parent
	//makes play from list
	//makes children til maxDepth reached
	//leaf nodes get a heuristic value
	public void makeChildren(Node node, int maxDepth, List<int[]>legalMoves) {
		for (int i = 0; i < legalMoves.size(); i++) {
			counter++;
			//make new child
			Node child = new Node();
			child.currentMove = legalMoves.get(i);
			//switch up child players
			child.currPlayer = node.oppPlayer;
			child.oppPlayer = node.currPlayer;
			//get copy board of current to board of child
			child.copyBoard(node.board);
			//make move from list of legal moves
			Othello.placeDisc(child.board,  child.currentMove, node.currPlayer, node.oppPlayer, legalMoves);
			//if its not going to be a leaf node
			if (maxDepth != 0) {
				makeChildren(child, maxDepth - 1, Othello.moveable(child.board, child.currPlayer, child.oppPlayer));
			}
			//if it is
			else {
				child.heuristic = hBoard[child.currentMove[0]][child.currentMove[1]] + (new Random().nextInt(2) * 2 - 2) + evalH(child);
			}
			node.children.add(child);
		}
		
	}
	
	//default heuristics 
	//taken from figure 3 in 5.2 of
	//https://courses.cs.washington.edu/courses/cse573/04au/Project/mini1/RUSSIA/Final_Paper.pdf
	//used as a base for heuristic analysis
	static int[][] defaultHeuristics() {	
		//board setup
		 int[][] hBoard = {
				{ 4,-3, 2, 2, 2, 2,-3, 4},
				{-3,-4,-1,-1,-1,-1,-4,-3},
				{ 2,-1, 1, 0, 0, 1,-1, 2},
				{ 2,-1, 0, 1, 1, 0,-1, 2},
				{ 2,-1, 0, 1, 1, 0,-1, 2},
				{ 2,-1, 1, 0, 0, 1,-1, 2},
				{-3,-4,-1,-1,-1,-1,-4,-3},
				{ 4,-3, 2, 2, 2, 2,-3, 4}
		};
		return hBoard;
	}
	//display heuristics board
	static void printH(int[][] board) {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[0].length; j++) {
				//System.out.print(board[i][j] + "  ");
				System.out.format("%3d", board[i][j]);
			}
			System.out.println("");
		}
		System.out.println("\n");
	}
	
	//gets the best choice of action
	public int minimax(Node node, int depth, boolean maximP, int[][]hBoard, int alpha, int beta, boolean prune) {
		statesEval++;
		//if its a leaf, return heuristic given
		if (node.children.size() == 0) {
			int h = node.heuristic;
			
			return h;
		}
		//if maximizing, get max of children
		if (maximP) {
			int maxH = Integer.MIN_VALUE;
			for (int i = 0; i < node.children.size(); i++) {
				//recursive call
				int eval = minimax(node.children.get(i), depth - 1, false, hBoard, alpha, beta, prune);
				maxH = Math.max(maxH, eval);
				alpha = Math.max(alpha, eval);
				node.heuristic = maxH;
				
				if ((beta <= alpha) && prune) {
					node.heuristic = beta;
					return beta;
				}
				
			}
			return maxH;
		}
		//if minimizing, get min of children
		else {
			int minH = Integer.MAX_VALUE;
			for (int i = 0; i < node.children.size(); i++) {
				//recursive call
				int eval = minimax(node.children.get(i), depth - 1, true, hBoard, alpha, beta, prune);
				minH = Math.min(minH, eval);
				beta = Math.min(beta, eval);
				node.heuristic = minH;
				if ((beta <= alpha) && prune) {
					node.heuristic = beta;
					return beta;
				}
				
			}
			return minH;
		}
	}
	
	//makes the best move
	public int[] makeBestMove(boolean prune) {
		int[] bestMove = new int[2];
		//check if one of the moves is a corner move
		for (int i = 0; i < root.legalMoves.size(); i++) {
			if (((root.legalMoves.get(i)[0] == 0) || (root.legalMoves.get(i)[0] == 7)) 
					&&
			   ((root.legalMoves.get(i)[0] == 0) || (root.legalMoves.get(i)[0] == 7))){
				return root.legalMoves.get(i);
			}
		}
		//if none, find best play
		minimax(root, maxDepth, true, hBoard, Integer.MIN_VALUE, Integer.MAX_VALUE, prune);
		for (int i = 0; i < root.children.size(); i++) {
			if (root.heuristic == root.children.get(i).heuristic) {
				bestMove = root.children.get(i).currentMove;
				break;
			}
		}
		return bestMove;
	}	
	
	public int evalH(Node node) {
		int mobility = Othello.moveable(node.board, node.currPlayer, node.oppPlayer).size();
		int cCount = 0;
		int oCount = 0;
		int cCorner = 0;
		int oCorner = 0;
		for (int i = 0; i < node.board.length; i++) {
			for (int j = 0; j < node.board[0].length; j++) {
				if (node.board[i][j] == node.currPlayer) {
					cCount++;
				}
				if (node.board[i][j] == node.oppPlayer) {
					oCount++;
				}
				if ((i == 0 || i == 7) && (j == 0 || j == 7)) {
					if (node.board[i][j] == node.currPlayer) {
						cCorner++;
					}
					if (node.board[i][j] == node.oppPlayer) {
						oCorner++;
					}
				}
				
			}
		}
		int coinDiff = cCount - oCount;
		int cornDiff = 3*(cCorner - oCorner);
		return coinDiff + cornDiff + mobility;
	}
}