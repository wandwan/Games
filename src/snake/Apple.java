package snake;

import java.util.Random;

public class Apple {
	static Random rand = new Random(System.nanoTime());
	static int l, r;
	private Apple(int l, int r) {
		Apple.l = l;
		Apple.r = r;
	}
	static boolean spawnApple(Board b, Snake s) {
		for(int i = 0; i < b.width; i++)
			for(int j = 0; j < b.height; j++)
				if(rand.nextInt() < Math.max(Integer.MAX_VALUE / b.width / b.height, 100))
					return b.setIfNotThere(i, j, new Apple(i,j));
		return false;
	}
}
class AppleRunner implements Runnable {
	Board b; Snake s;
	boolean paused;
	public AppleRunner(Board b, Snake s) {
		super();
		this.b = b;
		this.s = s;
	}

	@Override
	public void run() {
		while(true) {
			if(paused || b.get(Apple.l, Apple.r) instanceof Apple)
				try {
					wait();
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			else while(!Apple.spawnApple(b,s)) {}
		}
	}
}