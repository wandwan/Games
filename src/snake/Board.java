package snake;

public class Board {
	final int width = 100, height = 100;
	private volatile Object[][] board;
	int l, u;
	public Board(int w, int h) {
		board = new Object[width][height];
		l = (w % width) / 2;
		u = (h % height) / 2;
	}
	public synchronized void setBoard(int x, int y, Object o) {
		board[x][y] = o;
	}
	public synchronized boolean setIfNotThere(int x, int y, Object o) {
		if(board[x][y] == null) {
			board[x][y] = o;
			return true;
		}
		return false;
	}
	public Object get(int x, int y) {
		return board[x][y];
	}
}
