package Chess;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;

public class Runner {
	static JFrame jf;
	public static void main(String[] args) {
//		Board b = new Board();
//		System.out.println(b.hash);
//		DFS(new Board(), 6, new HashMap<Long, Board>());
		jf = new JFrame();
		BoardPanel u = new BoardPanel(jf);
		u.setBackground(new java.awt.Color(255, 255, 255));
		jf.add(u);
		jf.setContentPane(u);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
        jf.pack();
        jf.setVisible(true);
	}
//	public static void DFS(Board b, int depth, HashMap<Long, Board> hash) {
//		if(depth == 0)
//			return;
//		ArrayList<int[]> legals = b.getAllLegalMoves();
//		long temp = b.hash;
//		if(hash.containsKey(b.hash) && !b.equals(hash.get(b.hash)))
//			System.out.println("Collision");
//		if(!hash.containsKey(b.hash))
//			hash.put(b.hash, b);
//		for(int[] e: legals) {
//			int[] um = b.move(e[0], e[1], e[2], e.length > 3 ? e[3] : -1);
//			DFS(b, depth - 1, hash);
//			b.unMove(um[0], um[1], um[2], um[3], um[4], um[5], um[6], um[7]);
//			if(temp != b.hash) {
//				System.out.println("broke");
//			}
//		}
//		if(!hash.containsKey(b.hash)) {
//			System.out.println("broke1");
//		} else if(!hash.get(b.hash).equals(b)) {
//			System.out.println("broke2");
//		}
//	}
}
class Action extends MouseAdapter {
	Board b;
	BoardPanel panel;
	boolean state;
	public Action(Board b, BoardPanel panel) {
		this.b = b;
		this.panel = panel;
		state = true;
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		if(!state)
			return;
		if(panel.selected != -1) {
			panel.exclude = panel.selected;
			panel.x = e.getX();
			panel.y = e.getY();
		}
		panel.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(!state)
			return;
		move(e);
	}
	private void move(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int j = (x - panel.xOff) / panel.size;
		int i = 7 - ((y - panel.yOff) / panel.size);
		if(!b.inBounds(i, j))
			return;
		if(b.board[i][j] != 0 && b.board[i][j] < 17 == b.white)
			panel.selected = b.board[i][j];
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(!state)
			return;
		move(e);
		panel.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(!state)
			return;
		if(panel.selected == -1)
			return;
		int x = e.getX();
		int y = e.getY();
		boolean moved = false;
		int j = (x - panel.xOff) / panel.size;
		int i = 7 - ((y - panel.yOff) / panel.size);
		if(i < 8 && i > -1 && j < 8 && j > -1) {
			ArrayList<int[]> arr = b.getLegalMoves(panel.selected);
			for(int[] a1: arr)
				if(a1[0] == i && a1[1] == j) {
					SwingUtilities.invokeLater(() -> b.move(a1[0], a1[1], a1[2], a1.length == 4 ? a1[3] : -1));
					moved = true;
					if(b.ID[panel.selected] == Board.PAWN && (i == 0 || i == 7)) {
						panel.x = -1;
						panel.y = -1;
						panel.repaint();
						state = false;
						panel.screen = new PromoteScreen(panel.xOff, panel.yOff, panel.size, b, panel.selected);
						panel.pl = new PromoListener(panel.xOff, panel.yOff, panel.size, b, panel.selected, panel);
						panel.addMouseListener(panel.pl);
						return;
					}
				}
		}
		panel.exclude = -1;
		panel.selected = -1;
		panel.repaint();
		if(moved) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
//					System.out.println(Engine.Eval(b));
					Engine e = new Engine(new Board(b));
					e.search(8);
					System.out.println(e.transposed + " trans");
					int[] a1 = e.getMove();
					b.move(a1[0], a1[1], a1[2], a1.length == 4 ? a1[3] : -1);
				}
			});
		}
		panel.repaint();
	}
	
}
class PromoteScreen {
	int xOff, yOff, size;
	Board b;
	volatile int selected;
	Action listener;
	public PromoteScreen(int xOff, int yOff, int size, Board b, int selected) {
		super();
		this.xOff = xOff;
		this.yOff = yOff;
		this.size = size;
		this.b = b;
		this.selected = selected;
	}
}
class PromoListener extends MouseAdapter {
	public PromoListener(int xOff, int yOff, int size, Board b, int selected, BoardPanel jp) {
		super();
		this.xOff = xOff;
		this.yOff = yOff;
		this.size = size;
		this.b = b;
		this.selected = selected;
		this.jp = jp;
	}
	int xOff, yOff, size;
	Board b;
	int selected;
	BoardPanel jp;
	@Override
	public void mouseClicked(MouseEvent e) {
		int y = e.getY();
		int i = (y - yOff) / size;
		switch(i - (7 - b.loc[selected] / 8)) {
		case 0: b.promote(selected, Board.QUEEN); b.promoteDraw(selected, Board.QUEEN); break;
		case 1: b.promote(selected, Board.ROOK); b.promoteDraw(selected, Board.ROOK); break;
		case 2: b.promote(selected, Board.BISHOP); b.promoteDraw(selected, Board.BISHOP); break;
		case 3: b.promote(selected, Board.KNIGHT); b.promoteDraw(selected, Board.KNIGHT); break;
		default: return;
		}
		jp.exclude = -1;
		jp.selected = -1;
		jp.a.state = true;
		jp.removeMouseListener(this);
		jp.pl = null;
		jp.screen = null;
		jp.repaint();
	}
}
class BoardPanel extends JPanel {
	Board b;
	int xOff, yOff, size;
	int x, y;
	boolean moved;
	volatile int selected, exclude;
	Action a;
	PromoListener pl;
	PromoteScreen screen;
	public BoardPanel(JFrame frame) {
		exclude = -1;
		selected = -1;
		xOff = 40;
		yOff = 40;
		size = 60;
		x = -1;
		y = -1;
		b = new Board();
		b.init();
		a = new Action(b, this);
		addMouseListener(a);
		addMouseMotionListener(a);
		repaint();
	}
	@Override
	protected void paintComponent(Graphics g) {
		 super.paintComponent(g);
		 b.drawBoard((Graphics2D) g, size, xOff, yOff, 10, this, exclude, selected, a.state);
		 b.drawPiece((Graphics2D) g, x, y, exclude, this);
		 if(screen != null)
			 b.drawPromote((Graphics2D) g, selected, size, xOff, yOff, this);
	}
}