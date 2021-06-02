package snake;

import java.awt.Point;
import java.util.LinkedList;

public class Snake {
	int size;
	LinkedList<Point> body;
	Board b;
	int[] dir;
	public Snake(Board b) {
		this.b = b;
		body = new LinkedList<Point>();
		dir = new int[2];
		body.add(new Point(b.width / 2, b.height / 2));
		b.setBoard(b.width / 2,b.height / 2, body.peekFirst());
		size = 1;
	}
	private boolean check(Point p) {
		if(p.x >= b.width || p.x < 0 || p.y < 0 || p.y >= b.height || b.get(p.x,p.y) instanceof Point)
			return false;
		return true;
	}
	public boolean move() {
		Point p = new Point(body.peekFirst().x + dir[0], body.peekFirst().y + dir[1]);
		if(!check(p)) return false;
		body.addFirst(p);
		if(b.get(p.x, p.y) instanceof Apple) {
			size++;
			b.setBoard(p.x,p.y, p);
			notifyAll();
			return true;
		}
		b.setBoard(p.x,p.y, p);
		b.setBoard(body.peekLast().x, body.pollLast().y, null);
		return true;
	}
	public void changeDir(int h, int v) {
		dir[0] = h;
		dir[1] = v;
	}
}
