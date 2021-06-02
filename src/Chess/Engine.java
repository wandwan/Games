package Chess;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Engine {
	ConcurrentHashMap<Long, Double> trans;
	ConcurrentHashMap<Long, Integer> transDepth;
	final static int KING = 5;
	final static int QUEEN = 4;
	final static int BISHOP = 3;
	final static int KNIGHT = 2;
	final static int ROOK = 1;
	final static int PAWN = 6;
	long timea, timeb, timec, timed, timef;
	int[] pv;
	Board b;
	double rootEval;
	int transposed;
	int nodeCount;
	static double[][] pawnVal = {{9,9,9,9,9,9,9,9},
								 {3.3,3.3,3.3,3.3,3.3,3.3,3.3,3.3},
								 {2,1,1,1.5,1.5,1,1,2},
								 {1.5,1,1,1.7,1.7,1,1,1.5},
								 {1.3,1,1.3,1.7,1.7,1.3,1,1.3},
								 {1,1,1.3,1.5,1.5,1.3,1,1},
								 {1,.6,1.3,1.1,1.1,1.4,.9,1},
								 {0,0,0,0,0,0,0,0},
	};
	static double[] kingVal = {.6,.5,.4,0,0,.4,.5,.6};
	static double[] val = new double[7];
	public static void assignVal() {
		val[PAWN] = 1;
		val[BISHOP] = 3.5;
		val[KNIGHT] = 3;
		val[ROOK] = 5;
		val[QUEEN] = 9;
	}
	public double getEval() {
		return rootEval;
	}
	public int[] getMove() {
		System.out.println(Arrays.toString(pv));
		return pv;
	}
	public Engine(Board b) {
		assignVal();
		trans = new ConcurrentHashMap<Long, Double>();
		transDepth = new ConcurrentHashMap<Long, Integer>();
		this.b = b;
	}
	public Engine() {
		this(new Board());
	}
	public void search(int depth) {
		for(int i = 1; i < depth; i++) {
			System.out.println(nodeCount + " " + i);
			nodeCount = 0;
			transposed = 0;
			rootDFS(Eval(b), i, -2000.0, 2000, b);
		}
		System.out.println(nodeCount + " " + depth);
	}
	private double rootDFS(double eval1, int depth, double alpha, double beta, Board b) {
			nodeCount++;
			if(depth == 0) {
				if(b.isMate())
					eval1 = 2000 * -1;
				else if(b.isStalemate()) eval1 = 0;
				trans.put(b.hash, eval1);
				transDepth.put(b.hash, depth);
				return eval1;
			}
	        ExecutorService executor = Executors.newFixedThreadPool(6);
	        ExecutorCompletionService<Double> ecs = new ExecutorCompletionService<Double>(executor);
	        ArrayList<int[]> legals = b.getAllLegalMoves();
	        ArrayList<int[]> copy = new ArrayList<int[]>();
	        double[] evs = new double[legals.size()];
			for(int i = 0; i < legals.size(); i++) {
				int[] e = legals.get(i);
				int[] unmove = b.move(e[0], e[1], e[2], e.length == 4 ? e[3] : -1);
				Double eval = trans.get(b.hash);
				Integer d = transDepth.get(b.hash);
				if(eval != null) { 
					if(d != null && depth - 1 <= d) {
						if(-eval > alpha) {
							pv = e;
							alpha = -eval;
						}
						legals.set(i, null);
					}
					evs[i] = -eval;
				}
				else evs[i] = -Eval(b);
				b.unMove(unmove[0], unmove[1], unmove[2], unmove[3], unmove[4], unmove[5], unmove[6], unmove[7]);
			}
			while(true) {
				int best = -1;
				double score = -2001;
				for(int i = 0; i < legals.size(); i++) {
					if(legals.get(i) == null) continue;
					if(evs[i] > score) {
						best = i;
						score = evs[i];
					}
				}
				if(best == -1)
					break;
				int[] e = legals.get(best);
				legals.set(best, null);
				copy.add(e);
				Board c = new Board(b);
				c.move(e[0], e[1], e[2], e.length == 4 ? e[3] : -1);
				double aCopy = alpha;
				double bCopy = beta;
				double sCopy = -score;
				ecs.submit(new Callable<Double>() {
					@Override
					public Double call() throws Exception {
						return -DFS(sCopy, depth - 1, -bCopy, -aCopy, c);
					}});
			}
	        System.out.print(legals.size() + " ");
	        long time  = System.currentTimeMillis();
	        long time1 = time;
	        for(int i = 0; i < legals.size(); i++)
				try {
					Future<Double> ans = ecs.take();
					if(ans == null) {
//						System.out.println("broke");
						break;
					}
					else if(ans.get() > alpha) {
						alpha = ans.get();
					}
					System.out.println("finished " + i + " " + nodeCount + " " + (System.currentTimeMillis() - time));
					time = System.currentTimeMillis();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					e1.printStackTrace();
				}
	        System.out.println("time spent on: " + (System.currentTimeMillis() - time1));
	        boolean hit = false;
	        for(int[] e: copy) {
				int[] unmove = b.move(e[0], e[1], e[2], e.length == 4 ? e[3] : -1);
				if(trans.get(b.hash) == -alpha) {
					pv = e;
					hit = true;
				}
				b.unMove(unmove[0], unmove[1], unmove[2], unmove[3], unmove[4], unmove[5], unmove[6], unmove[7]);
	        }
	        if(!hit)
	        	System.out.println("broke1111");
			eval1 = alpha;
			if(legals.size() == 0) {
				if(b.isMate())
					eval1 = 2000 * -1;
				else eval1 = 0;
			}
			trans.put(b.hash, eval1);
			transDepth.put(b.hash, depth);
			return eval1;
		
	}
	private double DFS(double eval1, int depth, double alpha, double beta, Board b) {
		nodeCount++;
		if(depth == 0) {
			if(b.isMate())
				eval1 = 2000 * -1;
			else if(b.isStalemate()) eval1 = 0;
			if(trans.containsKey(b.hash))
				return eval1;
			trans.put(b.hash, eval1);
			transDepth.put(b.hash, depth);
			return eval1;
		}
		ArrayList<int[]> legals = b.getAllLegalMoves();
		double[] evs = new double[legals.size()];
		for(int i = 0; i < legals.size(); i++) {
			int[] e = legals.get(i);
			int[] unmove = b.move(e[0], e[1], e[2], e.length == 4 ? e[3] : -1);
			Double eval = trans.get(b.hash);
			Integer d = transDepth.get(b.hash);
			if(eval != null) { 
				if(d != null && depth - 1 <= d) {
					transposed++;
					alpha = Math.max(alpha, -eval);
					if(alpha >= beta) {
						eval1 = alpha;
						b.unMove(unmove[0], unmove[1], unmove[2], unmove[3], unmove[4], unmove[5], unmove[6], unmove[7]);
						if(transDepth.get(b.hash) == null || depth > transDepth.get(b.hash)) {
							trans.put(b.hash, eval1);
							transDepth.put(b.hash, depth);
						}
						return eval1;
					}
					legals.set(i, null);
				}
				evs[i] = -eval;
			}
			else evs[i] = -Eval(b);
			b.unMove(unmove[0], unmove[1], unmove[2], unmove[3], unmove[4], unmove[5], unmove[6], unmove[7]);
		}
		while(true) {
			int best = -1;
			double score = -2001;
			for(int i = 0; i < legals.size(); i++) {
				if(legals.get(i) == null) continue;
				if(evs[i] > score) {
					best = i;
					score = evs[i];
				}
			}
			if(best == -1)
				break;
			int[] e = legals.get(best);
			legals.set(best, null);
			int[] unmove = b.move(e[0], e[1], e[2], e.length == 4 ? e[3] : -1);
			alpha = Math.max(alpha, -DFS(-score, depth - 1, -beta, -alpha, b));
			b.unMove(unmove[0], unmove[1], unmove[2], unmove[3], unmove[4], unmove[5], unmove[6], unmove[7]);
			if(alpha >= beta)
				break;
		}
		eval1 = alpha;
		if(legals.size() == 0) {
			if(b.isMate())
				eval1 = 2000 * -1;
			else eval1 = 0;
		}
		Integer d = transDepth.get(b.hash);
		if(d != null && d > depth) return eval1;
		trans.put(b.hash, eval1);
		transDepth.put(b.hash, depth);
		return eval1;
	}

	//determine how good or bad a position is
	public static double Eval(Board b) {
		double eval = 0;
		eval += getSideEval(b, 0);
		eval -= getSideEval(b, 16);
		if(b.inCheck(b.white))
			eval += 4 * (b.white ? -1 : 1);
		return eval * (b.white ? 1 : -1);
	}
	private static double getSideEval(Board b, int start) {
		double eval = 0;
		boolean blackBishop = false;
		boolean whiteBishop = false;
		boolean endgame;
		int r = -1;
		int c = -1; 
		{
			endgame = true;
			int pieces = 0;
			double v = 0;
			for(int i = 1; i < 9; i++) {
				if(b.loc[i] != -1) {
					pieces++;
					v += val[b.ID[i]];
				}
			}
			if(pieces > 3 || v >= 13)
				endgame = false;
			v = 0;
			pieces = 0;
			for(int i = 1; i < 9; i++) {
				if(b.loc[i + 8] != -1) {
					pieces++;
					v += val[b.ID[i + 8]];
				}
			}
			if(pieces > 3 || v >= 13)
				endgame = false;
		}
		for(int i = 1; i < 9; i++) {
			if(b.loc[i + start] == -1)
				continue;
			if(b.loc[i + start] >> 3 != (start == 16 ? 7 : 0))
				eval += .3;
			eval += val[b.ID[i + start]];
			if(b.loc[i + 8 + start] == -1)
				continue;
			r = b.loc[i + 8 + start] >> 3;
			c = (b.loc[i + 8 + start] & 7);
			if(b.ID[i + 8 + start] == BISHOP) {
				if(((r + c) & 1) == 0)
					blackBishop = true;
				else whiteBishop = true;
			} else if(b.ID[i + 8 + start] == PAWN) {
				if(c != 7 && b.ID[b.board[r - 1][c + 1]] == PAWN && b.isWhite(b.board[r - 1][c + 1]) == (start != 16))
					eval += .3;
				if(c != 0 && b.ID[b.board[r - 1][c - 1]] == PAWN && b.isWhite(b.board[r - 1][c - 1]) == (start != 16))
					eval += .3;
				if(start == 16)
					r = 7 - r;
				eval += pawnVal[r][c];
			}
		}
		if(b.canCastle[0 + start / 16])
			eval += .3;
		if(b.canCastle[1 + start / 16])
			eval += .3;
		r = b.loc[5 + start] >> 3;
		c = b.loc[5 + start] & 7;
		if(!endgame) {
			eval += kingVal[c];
			if(start == 16) {
				if(r == 7) {
					if(b.board[r - 1][c] > 16)
						eval += .2;
					if(c < 7 && b.board[r - 1][c + 1] > 16)
						eval += .1;
					if(c > 0 && b.board[r - 1][c - 1] > 16)
						eval += .1;
				}
			} else {
				if(r == 0) { 
					if(b.board[r + 1][c] > 0 && b.board[r + 1][c] < 17)
						eval += .2;
					if(c > 0 && b.board[r + 1][c - 1] > 0 && b.board[r + 1][c - 1] < 17)
						eval += .1;
					if(c < 7 && b.board[r + 1][c + 1] > 0 && b.board[r + 1][c + 1] < 17)
						eval += .1;
				}
			}
		}
		if(blackBishop && whiteBishop)
			eval += .7;
		return eval;
	}
}