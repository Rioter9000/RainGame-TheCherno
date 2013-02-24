package net.babbster.rain;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

import javax.swing.JFrame;

import net.babbster.rain.graphics.Screen;
import net.babbster.rain.input.Keyboard;

/**
 * Game class
 * @author tarball
 */
public class Game extends Canvas implements Runnable {
	private static final long serialVersionUID = 1L;
	public static int width = 300;
	public static int height = width / 16 * 9;
	public static int scale = 3;
	public static String title = "Rain"; 
	
	int xoffval = 0;
	int yoffval = 0;
	
	private Thread thread;
	private JFrame frame;	
	private boolean running = false;
	
	private Screen screen;
	private Keyboard key;
	
	/* IMAGES */
	//BufferedImage is a raster storage class
	private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	
	//CTRL + SHIFT + O will import a missing package
	//this pulls the pixels out of the BufferedImage so that we can manipulate them
	private int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

	/**
	 * Constructor
	 */
	public Game() {
		Dimension size = new Dimension(width * scale, height * scale);
		
		this.setPreferredSize(size);
		screen = new Screen(this.width, this.height);
		frame = new JFrame();
		key = new Keyboard();
		this.addKeyListener(key);
		
	}

	/**
	 * Start game thread
	 */
	public synchronized void start() {
		running = true;
		thread = new Thread(this, "Game");
		thread.start();
	}

	/**
	 * Stop game thread
	 */
	public synchronized void stop() {
		running = false;
		try {
			thread.join();
		} catch (InterruptedException error) {
			error.printStackTrace();
		}
	}

	/**
	 * The work that happens in the game thread
	 */
	@Override
	public void run() {
		
		//let's create a timer object to control game logic
		long lastTime = System.nanoTime();
		long timer = System.currentTimeMillis();
		final double nano = 1000000000.0 / 60.0;
		double delta = 0.0;
		
		int frames = 0;
		int updates = 0;
		
		// Game loop
		while (running) {
			
			//get delta
			long now = System.nanoTime();
			delta += (now - lastTime) / nano; 
			lastTime = now;			
			
			//determine if 1/60 of a second has transpired
			while(delta >= 1){
				//update: we do the tick or update method here - this is game logic
				update();
				updates++;
				delta--;
			}
			//render - we update the graphics here
			render();
			frames++;
			if(System.currentTimeMillis() - timer >= 1000){
				timer = System.currentTimeMillis();
				frame.setTitle(this.title + "  |  " + "Updates: " + updates + " FPS: " + frames);
				frames = updates = 0;
			}
		}
	}
	
	/**
	 * render - all drawing work
	 */
	public void render(){
		//create the back buffer
		BufferStrategy buffer = this.getBufferStrategy();
		if(buffer == null){
			//the 3 represents triple buffering
			this.createBufferStrategy(3);
			return;
		}
		
		//clear and render the backbuffer
		screen.clear();
		//update the map movement with rendering
		screen.render(xoffval, yoffval);

		for(int i = 0; i < pixels.length; i++){
			this.pixels[i] = screen.pixels[i];
		}		
		
		
		//gets the non-rendered drawing buffer for modification
		Graphics g = buffer.getDrawGraphics();
		
		g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
		
		//release resources
		g.dispose();
		//and show the back buffer
		buffer.show();
		
	}
	
	/**
	 * update - game logic
	 */
	public void update(){
		
		//get key updates to increment or decrement x and y offsets
		key.update();
		
		if(key.up){
			yoffval--;	
		}
		if(key.down){
			yoffval++;
		}
		if(key.left){
			xoffval--;	
		}
		if(key.right){
			xoffval++;
		}
		
	}

	/**
	 * Entry point 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Game game = new Game();
		game.frame.setResizable(false);
		game.frame.setTitle(game.title);
		game.frame.add(game);
		game.frame.pack();
		game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		game.frame.setLocationRelativeTo(null);
		game.frame.setVisible(true);
		game.start();
	}
}
