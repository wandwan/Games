package Chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
public class Board {
	final static int KING = 5;
	final static int QUEEN = 4;
	final static int BISHOP = 3;
	final static int KNIGHT = 2;
	final static int ROOK = 1;
	final static int PAWN = 6;
	long hash;
	static long[] rand;
	static BufferedImage[] promos;
	//ID of Every single piece (what type of piece is it?)
	int[] ID;
	//Location of Every single piece 1-16 is for White, 17-32 is for black
	//Pieces are stored in the order of Rook, Knight, Bishop, Queen, King, Bishop, Knight, Rook, 8 pawns, and repeated for black
	int[] loc;
	static BufferedImage[] imgs;
	//all the knight moves, KR = Knight Row move, KC = Knight Column move
	public static final int[] KR = {-2,-2,-1,-1,1,1,2,2};
	public static final int[] KC = {-1,1,-2,2,-2,2,-1,1};
	int[][] board;
	boolean[] canCastle;
	int prev = -1;
	//white to play
	boolean white;
	public Board(Board b) {
		loc = Arrays.copyOf(b.loc, b.loc.length);
		ID = Arrays.copyOf(b.ID, b.ID.length);
		canCastle = Arrays.copyOf(b.canCastle, b.canCastle.length);
		board = new int[8][8];
		for(int i = 0; i < 8; i++)
			for(int j = 0; j < 8; j++)
				board[i][j] = b.board[i][j];
		white = b.white;
		prev = b.prev;
	}
	public Board() {
		generateRand();
		loc = new int[33];
		board = new int[8][8];
		ID = new int[33];
		canCastle = new boolean[4];
		Arrays.fill(canCastle, true);
		white = true;
		for(int i = 0; i < 2; i++) {
			ID[1 + 16 * i] = ROOK;
			ID[2 + 16 * i] = KNIGHT;
			ID[3 + 16 * i] = BISHOP;
			ID[4 + 16 * i] = QUEEN;
			ID[5 + 16 * i] = KING;
			ID[6 + 16 * i] = BISHOP;
			ID[7 + 16 * i] = KNIGHT;
			ID[8 + 16 * i] = ROOK;
			for(int j = 1; j < 9; j++)
				ID[i * 16 + j + 8] = PAWN;
		}
		for(int i = 0; i < 8; i++) {
			//board contains loc indices
			board[0][i] =  (i + 1);
			board[1][i] =  (9 + i);
			board[6][i] =  (25 + i);
			board[7][i] =  (17 + i);
			//loc contains location of piece by formula (row of piece * 8) + (column of piece)
			loc[i + 1] =  i;
			loc[9 + i] =  (8 + i);
			loc[17 + i] =  (56 + i);
			loc[25 + i] =  (48 + i);
		}
		for(int i = 1; i < loc.length; i++)
			if(loc[i] != -1)
				hash ^= rand[ID[i] - 1 + (i > 16 ? 6 : 0) * loc[i]];
		for(int i = 0; i < canCastle.length; i++)
			hash ^= rand[768 + i];
		hash ^= rand[772];
	}
	public static void generateRand() {
		rand = new long[781];
		Random r = new Random(427482634798123764l);
		for(int i = 0; i < rand.length; i++)
			rand[i] = r.nextLong();
	}
	public void init() {
		generateImages();
		initPromos();
	}
	public static void initPromos() {
		promos = new BufferedImage[8];
		try {
			promos[0] = ImageIO.read(new File("WhiteQueen.png"));
			promos[1] = ImageIO.read(new File("WhiteRook.png"));
			promos[2] = ImageIO.read(new File("WhiteBishop.png"));
			promos[3] = ImageIO.read(new File("WhiteKnight.png"));
			promos[4] = ImageIO.read(new File("BlackQueen.png"));
			promos[5] = ImageIO.read(new File("BlackRook.png"));
			promos[6] = ImageIO.read(new File("BlackBishop.png"));
			promos[7] = ImageIO.read(new File("BlackKnight.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void generateImages() {
		imgs = new BufferedImage[33];
		try {
			imgs[1] = ImageIO.read(new File("WhiteRook.png"));
			imgs[2] = ImageIO.read(new File("WhiteKnight.png"));
			imgs[3] = ImageIO.read(new File("WhiteBishop.png"));
			imgs[4] = ImageIO.read(new File("WhiteQueen.png"));
			imgs[5] = ImageIO.read(new File("WhiteKing.png"));
			imgs[6] = ImageIO.read(new File("WhiteBishop.png"));
			imgs[7] = ImageIO.read(new File("WhiteKnight.png"));
			imgs[8] = ImageIO.read(new File("WhiteRook.png"));
			for(int i = 9; i < 17; i++)
				imgs[i] = ImageIO.read(new File("WhitePawn.png"));
			imgs[17] = ImageIO.read(new File("BlackRook.png"));
			imgs[18] = ImageIO.read(new File("BlackKnight.png"));
			imgs[19] = ImageIO.read(new File("BlackBishop.png"));
			imgs[20] = ImageIO.read(new File("BlackQueen.png"));
			imgs[21] = ImageIO.read(new File("BlackKing.png"));
			imgs[22] = ImageIO.read(new File("BlackBishop.png"));
			imgs[23] = ImageIO.read(new File("BlackKnight.png"));
			imgs[24] = ImageIO.read(new File("BlackRook.png"));
			for(int i = 25; i < 33; i++)
				imgs[i] = ImageIO.read(new File("BlackPawn.png"));
		}catch (IOException ex) {
            System.err.println(ex.getStackTrace());
       }
	}
	public void drawPiece(Graphics2D g, int x, int y, int piece, JPanel jf) {
		if(x != -1 && piece != -1)
			g.drawImage(imgs[board[loc[piece] >>> 3][loc[piece] % 8]], x, y, jf);
	}
	public void drawPromote(Graphics2D g, int piece, int size, int xOff, int yOff, JPanel jp) {
		if(promos[1].getWidth() != size) {
			for(int i = 1; i < 33; i++) {
				BufferedImage after = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
				AffineTransform at = new AffineTransform();
				at.scale((double) size / promos[i].getWidth(), (double) size / promos[i].getHeight());
				AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
				after = scaleOp.filter(promos[i], after);
				promos[i] = after;
			}
		}
		g.setStroke(new java.awt.BasicStroke(5));
		g.setColor(Color.black);
		int x = (loc[piece] % 8) * size + xOff;
		int y = (7 - (loc[piece] >>> 3)) * size + yOff;
		g.drawRect(x, y, size, size * 4);
		g.setColor((loc[piece] % 8 + 7 - (loc[piece] >>> 3)) % 2 == 0 ? new Color(83, 124, 73) : new Color(255, 233, 175));
		g.fillRect(x + 3, y + 2, size - 5, size - 2);
		g.fillRect(x + 3, y + size * 2, size - 5, size);
		g.setColor((loc[piece] % 8 + 7 - (loc[piece] >>> 3)) % 2 == 0 ? new Color(255, 233, 175) : new Color(83, 124, 73));
		g.fillRect(x + 3, y + size, size - 5, size);
		g.fillRect(x + 3, y + size * 3, size - 5, size - 2);
		for(int i = 0; i < 4; i++)
			g.drawImage(promos[i + (isWhite(piece) ? 0 : 4)], x + 1, y + size * i + 2, jp);
	}
	public void drawBoard(Graphics2D g, int size, int xOff, int yOff, int borderSize, JPanel jp, int exclude, int selected, boolean state) {
		if(imgs[1].getWidth() != size) {
			for(int i = 1; i < 33; i++) {
				BufferedImage after = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
				AffineTransform at = new AffineTransform();
				at.scale((double) size / imgs[i].getWidth(), (double) size / imgs[i].getHeight());
				AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
				after = scaleOp.filter(imgs[i], after);
				imgs[i] = after;
			}
		}
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 4; j++) {
				g.setColor(new Color(83, 124, 73));
				g.fillRect(j * 2 * size + xOff + size * (i & 1), i * size + yOff, size, size);
				g.setColor(new Color(255, 233, 175));
				g.fillRect(j * 2 * size + xOff + size * ((i & 1) ^ 1), i * size + yOff, size, size);
			}
		}
		if(selected != -1 && state) {
			g.setColor(new Color(100,120,240));
			g.fillRect((loc[selected] % 8) * size + xOff,(7 - loc[selected] / 8) * size + yOff, size, size);
			ArrayList<int[]> arr = getLegalMoves(selected);
			for(int i = 0; i < arr.size(); i++)
				g.fillOval(arr.get(i)[1]* size + xOff + size / 5, (7 - arr.get(i)[0]) * size + yOff + size / 5, size * 3 / 5, size * 3 / 5);
		}
		g.setStroke(new java.awt.BasicStroke(borderSize));
		g.setColor(Color.BLACK);
		g.drawRoundRect(xOff - borderSize / 2 + 1, yOff - borderSize / 2, size * 8 + borderSize - 1, size * 8 + borderSize - 1, 5, 5);
		for(int i = 0; i < 8; i++)
			for(int j = 0; j < 8; j++) {
				if(imgs[board[i][j]] != null && board[i][j] != exclude)
					g.drawImage(imgs[board[i][j]], j * size + xOff + 1, (7 - i) * size + yOff + 1, jp);
			}
	}
	public boolean isStalemate() {
		int t = 0;
		if(!white) t = 16;
		for(int i = 1 + t; i < 16 + t; i++)
			if(loc[i] != -1 && getLegalMoves(i).size() != 0)
				return false;
		if(!inCheck(white))
			return true;
		return false;
	}
	public boolean isMate() {
		int t = 0;
		if(!white) t = 16;
		for(int i = 1 + t; i < 16 + t; i++)
			if(loc[i] != -1 && getLegalMoves(i).size() != 0)
				return false;
		if(inCheck(white))
			return true;
		return false;
	}
	public boolean isLegal() {
		//king can't be next to king
		if(Math.abs((loc[5] >>> 3) - (loc[5 + 16]>>> 3)) < 2 && Math.abs((loc[5] & 7) - (loc[5 + 16] & 7)) < 2)
			return false;
		//king can't be in check and it's opponents move
		if(inCheck(!white))
			return false;
		return true;
	}
	// white == 0 black == 16 in loc
	public boolean isWhite(int piece) {
		return piece < 17;
	}
	public boolean inCheck(boolean white) {
		int r, c;
		int t = 0;
		if(!white)
			t = 16;
		r = loc[KING + t] >>> 3;
		c = loc[KING + t] & 7;
		for(int i = 0; i < KR.length; i++)
			if(inBounds(r + KR[i], c + KC[i]) && ID[board[r + KR[i]][c + KC[i]]] == KNIGHT && isWhite(board[r + KR[i]][c + KC[i]]) != white)
				return true;
		if(helper(KING + t, BISHOP, 1, -1)) return true;
		if(helper(KING + t, BISHOP, 1, 1)) return true;
		if(helper(KING + t, BISHOP, -1, -1)) return true;
		if(helper(KING + t, BISHOP, -1, 1)) return true;
		if(helper(KING + t, ROOK, 1, 0)) return true;
		if(helper(KING + t, ROOK, -1, 0)) return true;
		if(helper(KING + t, ROOK, 0, 1)) return true;
		if(helper(KING + t, ROOK, 0, -1)) return true;
		if(white && (inBounds(r + 1, c + 1) && ID[board[r + 1][c + 1]] == PAWN && !isWhite(board[r + 1][c + 1]) || 
					 inBounds(r + 1, c - 1) && ID[board[r + 1][c - 1]] == PAWN && !isWhite(board[r + 1][c - 1])))
			return true;
		if(!white && (inBounds(r - 1, c + 1) && ID[board[r - 1][c + 1]] == PAWN && isWhite(board[r - 1][c + 1]) || 
					  inBounds(r - 1, c - 1) && ID[board[r - 1][c - 1]] == PAWN && isWhite(board[r - 1][c - 1])))
			return true;
		return false;
	}
	private boolean helper(int king, int other, int sign, int sign1) {
		int[] pLoc = check(king, null, sign, sign1);
		if(pLoc != null && (ID[board[pLoc[0]][pLoc[1]]] == other || ID[board[pLoc[0]][pLoc[1]]] == QUEEN))
			return true;
		return false;
	}
	public boolean inBounds(int r, int c) {
		if(r < 8 && r > -1 && c < 8 && c > -1)
			return true;
		return false;
	}
	public synchronized void unMove(int r, int c, int piece, int r1, int c1, int piece1, int cc, int prev1) {
		for(int i = 0; i < 4; i++) {
			if((cc & (1 << i)) == (1 << i) != canCastle[i]) {
				hash ^= rand[768 + i];
				canCastle[i] = (cc & (1 << i)) == (1 << i);
			}
		}
		hash ^= rand[772];
		white = !white;
		if(prev != -1)
			hash ^= rand[773 + (loc[prev] & 7)];
		prev = prev1;
		board[r][c] = piece;
		board[loc[piece] >>> 3][loc[piece] & 7] = 0;
		hash ^= rand[(ID[piece] - 1 + (piece > 16 ? 6 : 0)) * loc[piece]];
		loc[piece] = r * 8 + c;
		hash ^= rand[(ID[piece] - 1 + (piece > 16 ? 6 : 0)) * loc[piece]];
		if(piece1 != -1) {
			if(loc[piece1] != -1) {
				board[loc[piece1] >>> 3][loc[piece1] & 7] = 0;
				hash ^= rand[(ID[piece1] - 1 + (piece1 > 16 ? 6 : 0)) * loc[piece1]];
			}
			loc[piece1] = Math.abs(r1) * 8 + c1;
			board[Math.abs(r1)][c1] = piece1;
			hash ^= rand[(ID[piece1] - 1 + (piece1 > 16 ? 6 : 0)) * loc[piece1]];
		}

		if(prev != -1)
			hash ^= rand[773 + (loc[prev] & 7)];
		if(r1 < 0) {
			hash ^= rand[(ID[piece] - 1 + (piece > 16 ? 6 : 0)) * loc[piece]];
			ID[piece] = PAWN;
			hash ^= rand[(ID[piece] - 1 + (piece > 16 ? 6 : 0)) * loc[piece]];
		}
	}
	public synchronized int[] move(int r1, int c1, int piece, int pawnSpecial) {
		int[] move = new int[8];
		int r = loc[piece] >>> 3;
		int c = loc[piece] & 7;
		move[6] = (canCastle[3] ? 8 : 0) + (canCastle[2] ? 4 : 0) + (canCastle[1] ? 2 : 0) + (canCastle[0] ? 1 : 0);
		move[0] = r;
		move[1] = c;
		move[2] = piece;
		move[7] = prev;
		if(prev != -1)
			hash ^= rand[773 + (loc[prev] & 7)];
		if(board[r1][c1] != 0) {
			move[3] = r1;
			move[4] = c1;
			move[5] = board[r1][c1];
			hash ^= rand[(ID[board[r1][c1]] - 1 + (board[r1][c1] > 16 ? 6 : 0)) * loc[board[r1][c1]]];
			loc[board[r1][c1]] = -1;
		} else move[5] = -1;
		hash ^= rand[(ID[piece] - 1 + (piece > 16 ? 6 : 0)) * loc[piece]];
		loc[piece] = (r1 * 8 + c1);
		hash ^= rand[(ID[piece] - 1 + (piece > 16 ? 6 : 0)) * loc[piece]];
		board[r1][c1] = piece;
		board[r][c] = 0;
		if(ID[piece] == KING && c - c1 == -2) {
			move[3] = r;
			move[4] = c + 3;
			move[5] = board[r][c + 3];
			hash ^= rand[(ID[board[r][c + 3]] - 1 + (board[r][c + 3] > 16 ? 6 : 0)) * loc[board[r][c + 3]]];
			board[r][c + 1] = board[r][c + 3];
			board[r][c + 3] = 0;
			loc[board[r][c + 1]] =  (r * 8 + c + 1);
			hash ^= rand[(ID[board[r][c + 1]] - 1 + (board[r][c + 1] > 16 ? 6 : 0)) * loc[board[r][c + 1]]];
			prev = -1;
			hash ^= rand[772];
			white = !white;
			return move;
		} else if(ID[piece] == KING && c - c1 == 2) {
			move[3] = r;
			move[4] = c - 4;
			move[5] = board[r][c - 4];
			hash ^= rand[(ID[board[r][c - 4]] - 1 + (board[r][c - 4] > 16 ? 6 : 0)) * loc[board[r][c - 4]]];
			board[r][c - 1] = board[r][c - 4];
			board[r][c - 4] = 0;
			loc[board[r][c - 1]] =  (r * 8 + c - 1);
			hash ^= rand[(ID[board[r][c - 1]] - 1 + (board[r][c - 1] > 16 ? 6 : 0)) * loc[board[r][c - 1]]];
			prev = -1;
			hash ^= rand[772];
			white = !white;
			return move;
		}
		
		//if piece is king or rook, (implemented for castling)
		if((white && ID[piece] == KING || piece == 1) && canCastle[0]) {
			hash ^= rand[768];
			canCastle[0] = false;
		}
		if((white && ID[piece] == KING || piece == 8) && canCastle[1]) {
			hash ^= rand[769];
			canCastle[1] = false;
		}
		if((!white && ID[piece] == KING || piece == 17) && canCastle[2]) {
			hash ^= rand[770];
			canCastle[2] = false;
		}
		if((!white && ID[piece] == KING || piece == 24) && canCastle[3]) {
			hash ^= rand[771];
			canCastle[3] = false;
		}
		if(pawnSpecial == 0) {
			move[3] = loc[prev] >>> 3;
			move[4] = loc[prev] & 7;
			move[5] = board[move[3]][move[4]];
			hash ^= rand[(ID[prev] - 1 + (prev > 16 ? 6 : 0)) * loc[prev]];
			board[loc[prev] >>> 3][loc[prev] & 7] = 0;
			loc[prev] = -1;
			prev = -1;
			hash ^= rand[772];
			white = !white;
			return move;
		} else if(pawnSpecial != -1) {
			move[3] = -move[3];
			hash ^= rand[(ID[piece] - 1 + (piece > 16 ? 6 : 0)) * loc[piece]];
			promote(piece, pawnSpecial);
			hash ^= rand[(ID[piece] - 1 + (piece > 16 ? 6 : 0)) * loc[piece]];
		}
		//if piece is pawn and moves two squares allow en passant
		if(ID[piece] == PAWN && isWhite(piece) && r1 - r == 2 || !isWhite(piece) && ID[piece] == PAWN && r - r1 == 2) {
			prev = piece;
			hash ^= rand[773 + (loc[prev] & 7)];
		}
		else prev = -1;
		hash ^= rand[772];
		white = !white;
		return move;
	}
	public void promote(int piece, int to) {
		ID[piece] = to;
	}
	public void promoteDraw(int piece, int to) {
		try {
			if(isWhite(piece)) {
				switch(to) {
				case QUEEN: imgs[piece] = ImageIO.read(new File("WhiteQueen.png")); break;
				case ROOK: imgs[piece] = ImageIO.read(new File("WhiteRook.png")); break;
				case BISHOP: imgs[piece] = ImageIO.read(new File("WhiteBishop.png")); break;
				case KNIGHT: imgs[piece] = ImageIO.read(new File("WhiteKnight.png")); break;
				}
			} else {
				switch(to) {
				case QUEEN: imgs[piece] = ImageIO.read(new File("BlackQueen.png")); break;
				case ROOK: imgs[piece] = ImageIO.read(new File("BlackRook.png")); break;
				case BISHOP: imgs[piece] = ImageIO.read(new File("BlackBishop.png")); break;
				case KNIGHT: imgs[piece] = ImageIO.read(new File("BlackKnight.png")); break;
				}
			}
			} catch(IOException e) {
				e.printStackTrace();
			}
	}
	public boolean checkEnPassant(int r1, int c1, int piece) {
		int r = loc[piece] >>> 3;
		int c = loc[piece] & 7;
		loc[prev] = -1;
		board[r][c1] = 0;
		board[r][c] = 0;
		loc[piece] =  (r1 * 8 + c1);
		white = !white;
		boolean ans = !isLegal();
		board[r][c] =  piece;
		loc[piece] =  (r * 8 + c);
		board[r][c1] = prev;
		loc[prev] =  (r * 8 + c1);
		board[r1][c1] = 0;
		white = !white;
		if(ans)
			return false;
		return true;
	}
	public boolean checkMove(int r1, int c1, int piece) {
		int t = board[r1][c1];
		int r = loc[piece] >>> 3;
		int c = loc[piece] & 7;
		if(board[r1][c1] != 0 && isWhite(board[r1][c1]) == isWhite(piece))
			return false;
		if(t != 0)
			loc[t] = -1;
		board[r][c] = 0;
		loc[piece] = (r1 * 8 + c1);
		board[r1][c1] =  piece;
		white = !white;
		boolean ans = isLegal();
		board[r][c] =  piece;
		loc[piece] =  (r * 8 + c);
		board[r1][c1] = t;
		if(t != 0)
			loc[t] = (r1 * 8 + c1);
		white = !white;
		if(!ans)
			return false;
		return true;
	}
	public synchronized ArrayList<int[]> getAllLegalMoves() {
		ArrayList<int[]> arr = new ArrayList<int[]>();
		for(int i = 1; i < 17; i++) {
			if(loc[i + (white? 0 : 16)] != -1)
				getLegalMoves(i + (white? 0 : 16), arr);
		}
		return arr;
	}
	public ArrayList<int[]> getLegalMoves(int piece, ArrayList<int[]> legal) {
		int r = loc[piece] >>> 3;
		int c = loc[piece] & 7;
		if(ID[piece] == PAWN) {
			int t = 1;
			if(!white) t = -1;
			if(board[r + t][c] == 0) {
				// move one move forward
				if(checkMove(r + t, c, piece)) {
					if(r + t == 7 || r + t == 0) {
						legal.add(new int[] {r + t, c, piece, 1});
						legal.add(new int[] {r + t, c, piece, 2});
						legal.add(new int[] {r + t, c, piece, 3});
						legal.add(new int[] {r + t, c, piece, 4});
					} else
						legal.add(new int[] {r + t, c, piece});
				}
				// move two moves forward
				if((r == 1 && isWhite(piece) || r == 6 && !isWhite(piece)) && board[r + 2 * t][c] == 0 && checkMove(r + 2 * t, c, piece))
					legal.add(new int[] {r + 2 * t,c, piece});
			}
			//captures
			if(inBounds(r + t,c - 1) && board[r + t][c - 1] != 0 && checkMove(r + t, c - 1, piece))
				legal.add(new int[] {r + t,c - 1, piece});
			if(inBounds(r + t,c + 1) && board[r + t][c + 1] != 0 && checkMove(r + t, c + 1, piece))
				legal.add(new int[] {r + t,c + 1, piece});
			
			//en Passant
			if(prev != -1 && inBounds(r + t,c - 1) && board[r][c - 1] == prev && checkEnPassant(r + t, c - 1, piece))
				legal.add(new int[] {r + t,c - 1, piece, 0});
			if(prev != -1 && inBounds(r + t,c + 1) && board[r][c + 1] == prev && checkEnPassant(r + t, c + 1, piece))
				legal.add(new int[] {r + t,c + 1, piece, 0});
		}  else if(ID[piece] == KNIGHT) {
			for(int i = 0; i < KR.length; i++)
				if(inBounds(r + KR[i], c + KC[i]) && checkMove(r + KR[i], c + KC[i], piece))
					legal.add(new int[] {r + KR[i], c + KC[i], piece});
		} else if(ID[piece] == KING) {
			for(int i = -1; i < 2; i++)
				for(int j = -1; j < 2; j++) {
					if(i == 0 && j == 0) continue;
					if(inBounds(r + i, c + j) && checkMove(r + i, j + c, piece))
						legal.add(new int[] {r + i, c + j, piece});
				}
			//castle queenside
			if((loc[piece] & 7) != 4)
				return legal;
			if(white) {
				if(canCastle[0] && !inCheck(white) && board[r][c - 3] == 0 && board[r][c - 2] == 0 && board[r][c - 1] == 0 && checkMove(r, c - 1, piece) && checkMove(r, c - 2, piece))
					legal.add(new int[] {r, c - 2, piece});
				if(canCastle[1] && !inCheck(white) && board[r][c + 1] == 0 && board[r][c + 2] == 0 && checkMove(r, c + 1, piece) && checkMove(r, c + 2, piece))
					legal.add(new int[] {r, c + 2, piece});
			} else {
				if(canCastle[2] && !inCheck(white) && board[r][c - 3] == 0 && board[r][c - 2] == 0 && board[r][c - 1] == 0 && checkMove(r, c - 1, piece) && checkMove(r, c - 2, piece))
					legal.add(new int[] {r, c - 2, piece});
				if(canCastle[3] &&  !inCheck(white) && board[r][c + 1] == 0 && board[r][c + 2] == 0 && checkMove(r, c + 1, piece) && checkMove(r, c + 2, piece))
					legal.add(new int[] {r, c + 2, piece});
			}
		}
		else {
			if(ID[piece] == BISHOP || ID[piece] == QUEEN) {
				check(piece, legal, 1, 1);
				check(piece, legal, 1, -1);
				check(piece, legal, -1, 1);
				check(piece, legal, -1, -1);
			}
			if(ID[piece] == ROOK || ID[piece] == QUEEN) {
				check(piece, legal, 1, 0);
				check(piece, legal, -1, 0);
				check(piece, legal, 0, -1);
				check(piece, legal, 0, 1);
			}
		}
		return legal;
	}
	public synchronized ArrayList<int[]>  getLegalMoves(int piece) {
		ArrayList<int[]> legal = new ArrayList<int[]>();
		getLegalMoves(piece, legal);
		return legal;
	}
	private int[] check(int piece, ArrayList<int[]> legal, int sign, int sign1) {
		int r1, c1;
		for(int i = 1; i < 8; i++) {
			r1 = (loc[piece] >>> 3) + i * sign;
			c1 = (loc[piece] & 7) + i * sign1;
			if(!checkLine(r1, c1, piece, legal)) {
				if(inBounds(r1,c1) && isWhite(board[r1][c1]) != isWhite(piece))
					return new int[]{r1,c1};
				return null;
			}
		}
		return null;
	}
	private boolean checkLine(int r1, int c1, int piece, ArrayList<int[]> legal) {
		if(inBounds(r1, c1)) {
			if(board[r1][c1] == 0) {
				if(legal != null && checkMove(r1,c1,piece))
					legal.add(new int[] {r1,c1, piece});
				return true;
			} else if(legal != null && checkMove(r1,c1,piece))
					legal.add(new int[] {r1,c1, piece});
		}
		return false;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Board other = (Board) obj;
		if (!Arrays.equals(ID, other.ID))
			return false;
		if (!Arrays.deepEquals(board, other.board))
			return false;
		if (!Arrays.equals(canCastle, other.canCastle))
			return false;
		if (!Arrays.equals(loc, other.loc))
			return false;
		if (prev != other.prev)
			return false;
		if (white != other.white)
			return false;
		return true;
	}
	@Override
	public String toString() {
		String s = "";
		for(int[] e: board)
			s = s + Arrays.toString(e) + "\n";
		s += "\n";
		return s;
	}
} 
