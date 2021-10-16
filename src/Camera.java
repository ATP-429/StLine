import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

import Utility.Vector2i;

public class Camera
{
	private Vector2i pos = new Vector2i(); //Centre of the bounding square of the cam
	private double xUnitsOnScreen, yUnitsOnScreen; //Number of units between centre of cam and any one edge of the bounding square of this camera
	private double ppu; //pixelsPerUnit, that is, size of one unit square in terms of pixels on screen
	
	private double fontHeight = 0.4; //Height of font in units IN SPACE DIMENSIONS
	private double fontOffset = 0.1; //Offset of font from the axes IN SPACE DIMENSIONS
	private double snapRadius = 0.4; //GRAPHICAL RADIUS of snap IN SPACE DIMENSIONS
	private double origRadius = 1; //GRAPHICAL RADIUS of square at origin
	
	private Color gridColor = getColor(0xFFAAAAAA);
	private Color coordsColor = getColor(0xFF000000);
	private Color origColor = getColor(0xAA000000);
	
	private double fontSize;
	private Font font;
	private double fontWidth;
	
	public Camera()
	{
		
	}
	
	public Camera(double xUnitsOnScreen, double yUnitsOnScreen, double ppu)
	{
		this.setXUnitsOnScreen(xUnitsOnScreen);
		this.setYUnitsOnScreen(yUnitsOnScreen);
		this.ppu = ppu;
		update();
	}
	
	//Configures the camera so that it fills up the whole screen, if ppu is passed to it
	public void calibrate(int WIDTH, int HEIGHT, double ppu)
	{
		this.ppu = ppu;
		this.setXUnitsOnScreen((double) (WIDTH / 2) / ppu); //Calculates number of units btwn centre and 'higher' dimension. [pixels / pixels per unit = units]
		this.setYUnitsOnScreen((double) (HEIGHT / 2) / ppu);
		update();
	}
	
	//Returns absolute coordinate of pixel in Space, if coordinate of a pixel is passed wrt centre of cam
	public Vector2i getAbsoluteLocation(Vector2i pixel)
	{
		return new Vector2i(pixel.x / ppu, pixel.y / ppu).add(pos);
	}
	
	public double getXUnitsOnScreen()
	{
		return xUnitsOnScreen;
	}
	
	public double getYUnitsOnScreen()
	{
		return yUnitsOnScreen;
	}
	
	public void setXUnitsOnScreen(double xUnitsOnScreen)
	{
		this.xUnitsOnScreen = xUnitsOnScreen;
	}
	
	public void setYUnitsOnScreen(double yUnitsOnScreen)
	{
		this.yUnitsOnScreen = yUnitsOnScreen;
	}
	
	public Vector2i getPos()
	{
		return this.pos;
	}
	
	public void setPos(Vector2i pos)
	{
		this.pos = pos;
	}
	
	public double getPPU()
	{
		return this.ppu;
	}
	
	public void setPPU(double ppu)
	{
		this.ppu = ppu;
	}
	
	public void render(Space space, Graphics2D bg)
	{
		translate(bg);
		
		for (Component comp : space.getComps())
		{
			if (!comp.isInvalid())
				comp.render(bg, this);
		}
		
		reset(bg);
	}
	
	public void renderGrid(Graphics2D bg)
	{
		translate(bg);
		
		//Coordinates of bounding lines of camera (Camera is basically a square with 'pos' being centre of that square)
		double[] bounds = this.getBounds();
		double xLeft = bounds[0], yUp = bounds[1], xRight = bounds[2], yDown = bounds[3];
		
		//Draw vertical lines
		for (double x = (int) xLeft; x < xRight; x++)
		{
			bg.setColor(gridColor);
			drawLine(bg, x, yDown, x, yUp);
		}
		
		//Draw horizontal lines
		for (double y = (int) yDown; y < yUp; y++)
		{
			bg.setColor(gridColor);
			drawLine(bg, xLeft, y, xRight, y);
		}
		
		//Draw x and y axes
		bg.setColor(Color.BLACK);
		bg.setStroke(new BasicStroke(3));
		drawLine(bg, 0, yDown, 0, yUp); //x-axis
		drawLine(bg, xLeft, 0, xRight, 0); //y-axis
		bg.setStroke(new BasicStroke(1));
		
		//DRAW GRID COORDS
		//NOTE: We have to use bg.scale(1.0,-1.0) because otherwise string gets drawn upside down
		bg.setColor(coordsColor);
		bg.setFont(font);
		
		//Draw grid coords of x-axis
		if (0 < yUp) //If text is below the top of the screen
		{
			if (0 - fontHeight - fontOffset > yDown) //If text is above the bottom of the screen, that is, it is visible in the screen and should be drawn as such
				renderHorCoords(0 - fontHeight, xLeft, xRight, bg);
			else //If text is below the bottom of the screen
				renderHorCoords(yDown + fontOffset, xLeft, xRight, bg);
		}
		else //If text is above the top of the screen
		{
			renderHorCoords(yUp - fontHeight, xLeft, xRight, bg);
		}
		
		//Draw grid coords of y-axis
		if (0 - fontWidth - 2 * fontOffset > xLeft) //If text is to the right of the left side of screen
		{
			if (0 < xRight) //If text is to the left of the right side of screen, that is, it is visible in the screen and should be drawn as such
				renderVerCoordsToLeftOf(0 - fontOffset, yDown, yUp, bg);
			else //If text is to the right of the right side of screen
				renderVerCoordsToLeftOf(xRight - fontOffset, yDown, yUp, bg);
		}
		else //If text is to the left of the left side of screen
		{
			renderVerCoordsToRightOf(xLeft + fontOffset, yDown, yUp, bg);
		}
		
		//Draw a grey square at origin. If origin is outside frame, draw it just like how we are drawing the grid coordinates above
		double xSq = -origRadius/2, ySq = -origRadius/2; //Coordinates of bottom-left point of grey square
		if (-origRadius / 2 < xLeft)
			xSq = xLeft;
		else if (+origRadius / 2 > xRight)
			xSq = xRight - origRadius;
		
		if (+origRadius / 2 > yUp)
			ySq = yUp-origRadius;
		else if (-origRadius / 2 < yDown)
			ySq = yDown;
		
		bg.setColor(origColor);
		this.fillRect(bg, xSq, ySq, origRadius, origRadius);
		
		
		reset(bg);
	}
	
	//Renders horizontal coords given the y coordinate of the base line and the starting and ending coords
	private void renderHorCoords(double yBase, double xLeft, double xRight, Graphics2D bg)
	{
		for (int x = (int) (xLeft - 1); x <= xRight + 1; x++)
		{
			if (x != 0)
			{
				String str = "" + x;
				drawString(bg, str, x - str.length() * fontWidth / 2, yBase);
			}
		}
	}
	
	//Renders vertical coords given the x coordinate of the rhs of the text and the starting and ending coords
	private void renderVerCoordsToLeftOf(double xRight, double yDown, double yUp, Graphics2D bg)
	{
		for (int y = (int) (yDown - 1); y <= yUp + 1; y++)
		{
			if (y != 0)
			{
				String str = "" + y;
				drawString(bg, str, xRight - str.length() * fontWidth, y);
			}
		}
	}
	
	//Renders vertical coords given the x coordinate of the rhs of the text and the starting and ending coords
	private void renderVerCoordsToRightOf(double xLeft, double yDown, double yUp, Graphics2D bg)
	{
		for (int y = (int) (yDown - 1); y <= yUp + 1; y++)
		{
			if (y != 0)
			{
				String str = "" + y;
				drawString(bg, str, xLeft, y);
			}
		}
	}
	
	public void renderSnap(Vector2i snap, Graphics2D bg)
	{
		translate(bg);
		
		//Draws green sqaure around point to which mouse must be snapped to
		bg.setColor(new Color(0xFF00AA00));
		fillRect(bg, snap.x - snapRadius / 2, snap.y - snapRadius / 2, snapRadius, snapRadius);
		bg.setColor(new Color(0xFF000000));
		drawRect(bg, snap.x - snapRadius / 2, snap.y - snapRadius / 2, snapRadius, snapRadius);
		reset(bg);
	}
	
	public void zoomIn()
	{
		if (ppu < 100)
			ppu += 5;
		update();
	}
	
	public void zoomOut()
	{
		if (ppu > 8)
			ppu -= 5;
		update();
	}
	
	//Sets centre of our drawing screen to centre of camera
	public void translate(Graphics2D bg)
	{
		bg.translate(-pos.x * ppu, -pos.y * ppu);
	}
	
	//Resets drawing coords back to normal
	public void reset(Graphics2D bg)
	{
		bg.translate(pos.x * ppu, pos.y * ppu);
	}
	
	//Returns bounds of this camera, more specifically, returns the top-left and the bottom-right coordinates of the camera's view
	public double[] getBounds()
	{
		double xLeft = this.getPos().x - this.getXUnitsOnScreen();
		double xRight = this.getPos().x + this.getXUnitsOnScreen();
		double yUp = this.getPos().y + this.getYUnitsOnScreen();
		double yDown = this.getPos().y - this.getYUnitsOnScreen();
		return new double[] {xLeft, yUp, xRight, yDown};
	}
	
	//The following functions convert space coords to camera coords and draw the required components
	
	public void drawLine(Graphics2D bg, double x1, double y1, double x2, double y2)
	{
		bg.drawLine((int) (x1 * this.getPPU()), (int) (y1 * this.getPPU()), (int) (x2 * this.getPPU()), (int) (y2 * this.getPPU()));
	}
	
	public void drawRect(Graphics2D bg, double x, double y, double width, double height)
	{
		bg.drawRect((int) (x * this.getPPU()), (int) (y * this.getPPU()), (int) (int) (width * this.getPPU()), (int) (height * this.getPPU()));
	}
	
	public void fillRect(Graphics2D bg, double x, double y, double width, double height)
	{
		bg.fillRect((int) (x * this.getPPU()), (int) (y * this.getPPU()), (int) (int) (width * this.getPPU()), (int) (height * this.getPPU()));
	}
	
	public void drawOval(Graphics2D bg, double x, double y, double r1, double r2)
	{
		bg.drawOval((int) (x * this.getPPU()), (int) (y * this.getPPU()), (int) (int) (r1 * this.getPPU()), (int) (r2 * this.getPPU()));
	}
	
	public void fillOval(Graphics2D bg, double x, double y, double r1, double r2)
	{
		bg.fillOval((int) (x * this.getPPU()), (int) (y * this.getPPU()), (int) (int) (r1 * this.getPPU()), (int) (r2 * this.getPPU()));
	}
	
	public void drawString(Graphics2D bg, String str, double x, double y)
	{
		bg.drawString(str, (int) (x * this.getPPU()), (int) (y * this.getPPU()));
	}
	
	//Updates all the variables associated with the camera
	public void update()
	{
		fontSize = 72.0 * fontHeight / Toolkit.getDefaultToolkit().getScreenResolution();
		font = new Font("Consolas", Font.PLAIN, (int) (fontSize * this.getPPU()));
		AffineTransform at = new AffineTransform();
		at.scale(1.0, -1.0);
		font = font.deriveFont(at);
		//Gets the width of the font IN SPACE UNITS
		fontWidth = font.getStringBounds("A", new FontRenderContext(new AffineTransform(), true, true)).getWidth() / this.getPPU();
	}
	
	private static Color getColor(int col)
	{
		return new Color((col >> 16) & 0xFF, (col >> 8) & 0xFF, (col) & 0xFF, (col >> 24) & 0xFF);
	}
}
