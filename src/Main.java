import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import Utility.Vector2i;

public class Main extends Canvas implements MouseListener, MouseMotionListener, MouseWheelListener //Basically, 'extends Canvas' just makes any Main object a painting canvas, that is, we can draw on it
{
	private static final long serialVersionUID = 1L;
	
	private static final int WIDTH = 800, HEIGHT = 600;
	private static final int RENDER_WIDTH = 800, RENDER_HEIGHT = 600;
	
	private Camera cam;
	private Space space;
	private JFrame frame;
	
	private final int DEFAULT_PPU = 50;
	
	private ACTION action = ACTION.IDLE;
	
	private Component comp;
	
	private MouseEvent prevMouseE;
	
	//Entry-point of program
	public static void main(String[] args)
	{
		Main game = new Main(); //Making an object of Main class
		//Thread.sleep() in init() function can throw an exception, so we need to catch it here to compile without errors.
		//This isn't that important, so ignore this
		game.addMouseListener(game);
		game.addMouseMotionListener(game);
		game.addMouseWheelListener(game);
		try
		{
			game.init(); //Goes to init() function inside 'game' object
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public void init() throws InterruptedException //Thread.sleep() throws an InterruptedException, so we have to make the function do so too, or surround Thread.sleep() with try catch. Again, ignore this
	{
		cam = new Camera();
		cam.calibrate(WIDTH, HEIGHT, DEFAULT_PPU);
		space = new Space();
		frame = new JFrame(); //Creates a window
		frame.setSize(new Dimension(WIDTH, HEIGHT)); //Sets window's size
		frame.setResizable(false); //Now window cannot be resized by moving its borders
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Adds 'this' object to the frame. 'this' refers to the object that is executing this function, that is, the 'game' object we declared above in main(). 
		frame.add(this); //Basically, it's adding the 'game' object (Which is just a canvas) to the window, so anything we draw on the canvas will now be visible on the frame
		
		//setLocationRelativeTo just sets the window's position on the screen with respect to another component 
		frame.setLocationRelativeTo(null); //If the argument is 'null', it just puts the window at the centre of the screen
		
		frame.setVisible(true); //Makes our frame visible
	}
	
	//We need to do this because when repaint() is called, the original update() method of Canvas class clears the screen before calling paint()
	//We never want to clear the screen. Explanation as to why given in paint() method
	@Override
	public void update(Graphics g)
	{
		paint(g);
	}
	
	@Override
	public void paint(Graphics g)
	{
		/* Instead of drawing on the main screen directly, we first draw on a new BufferedImage we create
		 * This is because if we clear the previous screen and directly draw on it, we will see flickering of the screen.
		 * For eg, let's say we were drawing a moving circle on the screen
		 * If we clear the screen to white and then draw the circle directly, the user will be able to temporarily see the cleared screen before circle is drawn on it
		 * This causes flickering of the objects on our screen.
		 * However, if we first draw the circle on another image, and then just copy that image over to the main screen, the user will never see a cleared screen.
		 * This is what we're doing below.
		 */
		BufferedImage bufferImg = new BufferedImage(RENDER_WIDTH, RENDER_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D bg = (Graphics2D) bufferImg.getGraphics();
		
		//Make the coordinate system match cartesian coordinate system
		bg.translate(WIDTH / 2, HEIGHT / 2);
		bg.scale(1.0, -1.0);
		
		//Set background to white
		bg.setBackground(Color.WHITE);
		bg.clearRect(-WIDTH / 2, -HEIGHT / 2, WIDTH, HEIGHT); //Since we've changed coordinate system, we need to now pass the bottom-left coord of rectangle
		
		bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //Turn anti-aliasing on
		bg.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE); //Just makes the graphics more accurate
		
		cam.renderGrid(bg);
		cam.render(space, bg);
		
		//Draws bufferImg on our original canvas
		g.drawImage(bufferImg, 0, 0, WIDTH, HEIGHT, null);
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		if (e.getButton() == MouseEvent.BUTTON1)
		{
			comp = new Line();
			comp.startAt(cam.getAbsoluteLocation(getPixelRelativeTo(e)));
			action = ACTION.DRAWING;
			space.push(comp);
		}
		else if (e.getButton() == MouseEvent.BUTTON3)
		{
			action = ACTION.MOVING;
		}
		prevMouseE = e;
	}
	
	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (action == ACTION.DRAWING)
		{
			if (comp.isInvalid()) //If component that user has drawn is not valid (Eg. if line's start pos and end pos is same), remove that element
				space.pop();
			comp = null;
		}
		action = ACTION.IDLE;
		prevMouseE = e;
	}
	
	@Override
	public void mouseEntered(MouseEvent e)
	{
		
	}
	
	@Override
	public void mouseExited(MouseEvent e)
	{
		
	}
	
	@Override
	public void mouseDragged(MouseEvent e)
	{
		switch (action)
		{
			case DRAWING:
				comp.endAt(cam.getAbsoluteLocation(getPixelRelativeTo(e)));
				break;
			
			case MOVING:
				Vector2i prev = cam.getAbsoluteLocation(getPixelRelativeTo(prevMouseE));
				Vector2i next = cam.getAbsoluteLocation(getPixelRelativeTo(e));
				cam.setPos(cam.getPos().subtract(next.subtract(prev)));
				break;
		}
		prevMouseE = e;
		repaint();
	}
	
	@Override
	public void mouseMoved(MouseEvent e)
	{
		
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		Vector2i origPos = cam.getAbsoluteLocation(getPixelRelativeTo(e));
		if (e.getWheelRotation() < 0)
			cam.zoomIn();
		else
			cam.zoomOut();
		cam.calibrate(WIDTH, HEIGHT, cam.getPPU());
		
		//We want the location of the coordinate the mouse was pointing to in space, to remain at the same location on screen even after zoom, to provide a good zooming exp for the user
		Vector2i newPos = cam.getAbsoluteLocation(getPixelRelativeTo(e));
		cam.setPos(cam.getPos().add(origPos.subtract(newPos)));
		
		repaint();
	}
	
	//Returns Vector2i of coords of pixel relative to centre of screen, IN CARTESIAN COORDS
	private Vector2i getPixelRelativeTo(int x, int y)
	{
		return new Vector2i(x - WIDTH / 2, HEIGHT / 2 - y);
	}
	
	//Returns Vector2i of coords of pixel relative to centre of screen, IN CARTESIAN COORDS, if you pass the MouseEvent that generated the click on that pixel
	private Vector2i getPixelRelativeTo(MouseEvent e)
	{
		return getPixelRelativeTo(e.getX(), e.getY());
	}
}

enum ACTION
{
	IDLE, DRAWING, MOVING;
}
