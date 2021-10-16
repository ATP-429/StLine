import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import Utility.Vector2i;

public class Main extends Canvas implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener //Basically, 'extends Canvas' just makes any Main object a painting canvas, that is, we can draw on it
{
	private static final long serialVersionUID = 1L;
	
	private static final int WIDTH = 800, HEIGHT = 600;
	private static final int RENDER_WIDTH = 800, RENDER_HEIGHT = 600;
	
	private final int DEFAULT_PPU = 50;
	
	private Camera cam;
	private Space space;
	private JFrame frame;
	
	private volatile boolean[] keys = new boolean[200];
	
	private ACTION action = ACTION.IDLE;
	
	private Component comp;
	
	private MouseEvent prevMouseE;
	
	private Menu menu;
	
	private Vector2i prevSnap;
	private Vector2i snap; //Coordinates of the point to which the mouse will snap to, in space
	
	public static void main(String[] args)
	{
		Main game = new Main();
		game.addMouseListener(game);
		game.addMouseMotionListener(game);
		game.addMouseWheelListener(game);
		game.addKeyListener(game);
		game.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		try
		{
			game.init();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public void init() throws InterruptedException
	{
		cam = new Camera();
		cam.calibrate(RENDER_WIDTH, RENDER_HEIGHT, DEFAULT_PPU);
		space = new Space();
		frame = new JFrame(); //Creates a window
		frame.setResizable(false); //Now window cannot be resized by moving its borders
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Adds 'this' object to the frame. 'this' refers to the object that is executing this function, that is, the 'game' object we declared above in main(). 
		frame.add(this); //Basically, it's adding the 'game' object (Which is just a canvas) to the window, so anything we draw on the canvas will now be visible on the frame
		
		menu = new Menu();
		//menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
		
		menu.setPreferredSize(new Dimension(WIDTH / 3, HEIGHT));
		menu.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		menu.setVisible(true);
		menu.getVerticalScrollBar().setUnitIncrement(16);
		
		frame.add(menu, BorderLayout.LINE_END);
		
		frame.pack(); //Resizes the window so that all components inside it are at or above their preferred size. Since the preferred size of canvas is WIDTH x HEIGHT (As defined in main() function), the client area gets a size of WIDTH x HEIGHT
		
		//setLocationRelativeTo just sets the window's position on the screen with respect to another component 
		frame.setLocationRelativeTo(null); //If the argument is 'null', it just puts the window at the centre of the screen
		
		frame.setVisible(true); //Makes our frame visible
		
		this.requestFocusInWindow(); //We need to do this otherwise the canvas isn't able to listen to key events without us having to click on the canvas to give it focus first
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
		bg.translate(RENDER_WIDTH / 2, RENDER_HEIGHT / 2);
		bg.scale(1.0, -1.0);
		
		//Set background to white
		bg.setBackground(Color.WHITE);
		bg.clearRect(-RENDER_WIDTH / 2, -RENDER_HEIGHT / 2, RENDER_WIDTH, RENDER_HEIGHT); //Since we've changed coordinate system, we need to now pass the bottom-left coord of rectangle
		
		bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //Turn anti-aliasing on
		bg.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE); //Just makes the graphics more accurate
		
		cam.renderGrid(bg);
		cam.render(space, bg);
		
		if (snap != null)
			cam.renderSnap(snap, bg);
		
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
			//If user has not pressed shift key to override the snap, snap to required point
			if (!keys[KeyEvent.VK_SHIFT])
				comp.startAt(space.snapFrom(cam.getAbsoluteLocation(getPixelRelativeTo(e))));
			else
				comp.startAt(cam.getAbsoluteLocation(getPixelRelativeTo(e)));
			action = ACTION.DRAWING;
			space.push(comp);
		}
		else if (e.getButton() == MouseEvent.BUTTON3 || e.getButton() == MouseEvent.BUTTON2)
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
			else
			{
				menu.add(comp);
				menu.revalidate();
			}
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
				//If user has not pressed shift key to override the snap, snap to required point
				if (!keys[KeyEvent.VK_SHIFT])
					comp.endAt(space.snapFrom(cam.getAbsoluteLocation(getPixelRelativeTo(e))));
				else
					comp.endAt(cam.getAbsoluteLocation(getPixelRelativeTo(e)));
				break;
			
			case MOVING:
				//Get difference between the prevMouse coordinates and current mouse coords, in space, and then move camera's position by that difference.
				//This makes it so that the mouse pointer remains at the same location in space, while the camera's position changes.
				Vector2i prev = cam.getAbsoluteLocation(getPixelRelativeTo(prevMouseE));
				Vector2i next = cam.getAbsoluteLocation(getPixelRelativeTo(e));
				cam.setPos(cam.getPos().subtract(next.subtract(prev)));
				break;
		}
		
		updateSnap(e);
		
		prevMouseE = e;
		repaint();
	}
	
	@Override
	public void mouseMoved(MouseEvent e)
	{
		updateSnap(e);
		prevMouseE = e;
	}
	
	private void updateSnap(MouseEvent e)
	{
		Vector2i point = cam.getAbsoluteLocation(getPixelRelativeTo(e));
		Vector2i snap = space.snapFrom(point);
		if (!snap.equals(point) && !keys[KeyEvent.VK_SHIFT])
			this.snap = snap;
		else
			this.snap = null;
		
		if (change(this.snap, prevSnap))
			repaint();
		
		prevSnap = this.snap;
	}
	
	private boolean change(Vector2i snap, Vector2i prevSnap)
	{
		//Basically, if snap is different from prevSnap, repaint() the screen. We need to check all these null values because it gives an error otherwise
		if ((snap == null && prevSnap != null) || (snap != null && prevSnap == null))
			return true;
		else if (snap == null && prevSnap == null)
			return false;
		else if (!snap.equals(prevSnap))
			return true;
		return false;
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		Vector2i origPos = cam.getAbsoluteLocation(getPixelRelativeTo(e));
		if (e.getWheelRotation() < 0)
			cam.zoomIn();
		else
			cam.zoomOut();
		cam.calibrate(RENDER_WIDTH, RENDER_HEIGHT, cam.getPPU());
		
		//We want the location of the coordinate the mouse was pointing to in space, to remain at the same location on screen even after zoom, to provide a good zooming exp for the user
		Vector2i newPos = cam.getAbsoluteLocation(getPixelRelativeTo(e));
		cam.setPos(cam.getPos().add(origPos.subtract(newPos)));
		
		repaint();
	}
	
	@Override
	public void keyTyped(KeyEvent e)
	{
		
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		keys[e.getKeyCode()] = true;
		if (e.getKeyCode() == KeyEvent.VK_SHIFT)
			updateSnap(prevMouseE);
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
			frame.dispose();
	}
	
	@Override
	public void keyReleased(KeyEvent e)
	{
		keys[e.getKeyCode()] = false;
	}
	
	//Returns Vector2i of coords of pixel relative to centre of screen, IN CARTESIAN COORDS
	private Vector2i getPixelRelativeTo(int x, int y)
	{
		Vector2i pixel = getPixelInOrigScreen(x, y);
		return new Vector2i(pixel.x - RENDER_WIDTH / 2, RENDER_HEIGHT / 2 - pixel.y);
	}
	
	//Returns Vector2i of coords of pixel relative to centre of screen, IN CARTESIAN COORDS, if you pass the MouseEvent that generated the click on that pixel
	private Vector2i getPixelRelativeTo(MouseEvent e)
	{
		return getPixelRelativeTo(e.getX(), e.getY());
	}
	
	//Returns Vector2i of the coords that the pixel would be at, if screen resolution was RENDER_WIDTH x RENDER_HEIGHT
	//Basically finds the ratio of x and y coords [Eg if WIDTH = 1000 and x = 500, then we just return RENDER_WIDTH*0.5]
	private Vector2i getPixelInOrigScreen(int x, int y)
	{
		return new Vector2i((double) x / WIDTH * RENDER_WIDTH, (double) y / HEIGHT * RENDER_HEIGHT);
	}
}

enum ACTION
{
	IDLE, DRAWING, MOVING;
}
