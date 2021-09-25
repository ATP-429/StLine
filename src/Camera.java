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
	
	public Camera()
	{
		
	}
	
	public Camera(double xUnitsOnScreen, double yUnitsOnScreen, double ppu)
	{
		this.setXUnitsOnScreen(xUnitsOnScreen);
		this.setYUnitsOnScreen(yUnitsOnScreen);
		this.ppu = ppu;
	}
	
	//Configures the camera so that it fills up the whole screen, if ppu is passed to it
	public void calibrate(int WIDTH, int HEIGHT, double ppu)
	{
		this.ppu = ppu;
		this.setXUnitsOnScreen((double) (WIDTH / 2) / ppu); //Calculates number of units btwn centre and 'higher' dimension. [pixels / pixels per unit = units]
		this.setYUnitsOnScreen((double) (HEIGHT / 2) / ppu);
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
			bg.setColor(new Color(0xFFAAAAAA));
			drawLine(bg, x, yDown, x, yUp);
		}
		
		//Draw horizontal lines
		for (double y = (int) yDown; y < yUp; y++)
		{
			bg.setColor(new Color(0xFFAAAAAA));
			drawLine(bg, xLeft, y, xRight, y);
		}
		
		//Draw x and y axes
		bg.setColor(new Color(0xFF000000));
		bg.setStroke(new BasicStroke(3));
		drawLine(bg, 0, yDown, 0, yUp); //x-axis
		drawLine(bg, xLeft, 0, xRight, 0); //y-axis
		bg.setStroke(new BasicStroke(1));
		
		//DRAW GRID COORDS
		//NOTE: We have to use bg.scale(1.0,-1.0) because otherwise string gets drawn upside down
		double fontSize = 72.0 * fontHeight / Toolkit.getDefaultToolkit().getScreenResolution();
		Font font = new Font("Consolas", Font.PLAIN, (int) (fontSize * this.getPPU()));
		bg.setColor(new Color(0xFF000000));
		bg.setFont(font);
		
		//Gets the width and height of the font IN SPACE UNITS
		double width = font.getStringBounds("A", new FontRenderContext(new AffineTransform(), true, true)).getWidth() / this.getPPU();
		double height = font.getStringBounds("A", new FontRenderContext(new AffineTransform(), true, true)).getHeight() / this.getPPU();
		
		//Draw grid coords of x-axis
		if (0 < yUp) //If text is below the top of the screen
		{
			if (0 - fontHeight - 2 * fontOffset > yDown) //If text is above the bottom of the screen, that is, it is visible in the screen and should be drawn as such
			{
				for (int x = (int) xLeft; x < xRight; x++)
				{
					if (x != 0)
					{
						String str = "" + x;
						bg.scale(1.0, -1.0);
						drawString(bg, str, x - str.length() * width / 2, 0 + fontOffset + fontHeight); //Drawing string at y = 0 and with given offset
						bg.scale(1.0, -1.0);
					}
				}
			}
			else //If text is below the bottom of the screen
			{
				for (int x = (int) xLeft; x < xRight; x++)
				{
					if (x != 0)
					{
						String str = "" + x;
						bg.scale(1.0, -1.0);
						drawString(bg, str, x - str.length() * width / 2, -yDown - fontOffset); //Drawing string at yDown [Since we've scaled y-axis with -1, we need to do -yDown and -offset]
						bg.scale(1.0, -1.0);
					}
				}
			}
		}
		else //If text is above the top of the screen
		{
			for (int x = (int) xLeft; x < xRight; x++)
			{
				if (x != 0)
				{
					String str = "" + x;
					bg.scale(1.0, -1.0);
					drawString(bg, str, x - str.length() * width / 2, -yUp + fontHeight + fontOffset);
					bg.scale(1.0, -1.0);
				}
			}
		}
		
		//Draw grid coords of y-axis
		if (0-width-fontOffset > xLeft) //If text is to the right of the left side of screen
		{
			if (0 < xRight) //If text is to the left of the right side of screen, that is, it is visible in the screen and should be drawn as such
			{
				for (int y = (int) yDown; y < yUp; y++)
				{
					if (y != 0)
					{
						String str = "" + y;
						bg.scale(1.0, -1.0);
						drawString(bg, str, 0 - width*str.length() - fontOffset, -y + fontOffset); //Drawing string at x = 0 and with given offset
						bg.scale(1.0, -1.0);
					}
				}
			}
			else //If text is to the right of the right side of screen
			{
				for (int y = (int) yDown; y < yUp; y++)
				{
					if (y != 0)
					{
						String str = "" + y;
						bg.scale(1.0, -1.0);
						drawString(bg, str, xRight - width - fontOffset, -y + fontOffset);
						bg.scale(1.0, -1.0);
					}
				}
			}
		}
		else //If text is to the left of the left side of screen
		{
			for (int y = (int) yDown; y < yUp; y++)
			{
				if (y != 0)
				{
					String str = "" + y;
					bg.scale(1.0, -1.0);
					drawString(bg, str, xLeft+fontOffset, -y + fontOffset);
					bg.scale(1.0, -1.0);
				}
			}
		}
		
		reset(bg);
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
	}
	
	public void zoomOut()
	{
		if (ppu > 8)
			ppu -= 5;
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
}
