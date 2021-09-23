import java.awt.Color;
import java.awt.Graphics2D;

import Utility.Vector2i;

public class Camera
{
	private Vector2i pos = new Vector2i(); //Centre of the bounding square of the cam
	private double unitsOnScreen; //Number of units between centre of cam and any one edge of the bounding square of this camera
	private double ppu; //pixelsPerUnit, that is, size of one unit square in terms of pixels on screen
	
	public Camera()
	{
		
	}
	
	public Camera(double unitsOnScreen, double ppu)
	{
		this.setUnitsOnScreen(unitsOnScreen);
		this.ppu = ppu;
	}
	
	//Configures the camera so that it fills up the whole screen, if ppu is passed to it
	public void calibrate(int WIDTH, int HEIGHT, double ppu)
	{
		int higher = (WIDTH > HEIGHT) ? WIDTH : HEIGHT; //If HEIGHT is higher, we want the bounding square to reach till HEIGHT
		this.ppu = ppu;
		this.setUnitsOnScreen((double) (higher / 2) / ppu); //Calculates number of units btwn centre and 'higher' dimension. [pixels / pixels per unit = units]
	}
	
	//Returns absolute coordinate of pixel in Space, if coordinate of a pixel is passed wrt centre of cam
	public Vector2i getAbsoluteLocation(Vector2i pixel)
	{
		return new Vector2i(pixel.x / ppu, pixel.y / ppu).add(pos);
	}
	
	public double getUnitsOnScreen()
	{
		return unitsOnScreen;
	}
	
	public void setUnitsOnScreen(double unitsOnScreen)
	{
		this.unitsOnScreen = unitsOnScreen;
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
		//Sets centre of our drawing screen to centre of camera
		bg.translate(-pos.x * ppu, -pos.y * ppu);
		
		for (Component comp : space.getComps())
		{
			comp.render(bg, this);
		}
		
		bg.translate(pos.x * ppu, pos.y * ppu);
	}
	
	public void renderGrid(Graphics2D bg)
	{
		bg.translate(-pos.x * ppu, -pos.y * ppu);
		
		//Coordinates of bounding lines of camera (Camera is basically a square with 'pos' being centre of that square)
		double xLeft = this.getPos().x - this.getUnitsOnScreen();
		double xRight = this.getPos().x + this.getUnitsOnScreen();
		double yUp = this.getPos().y + this.getUnitsOnScreen();
		double yDown = this.getPos().y - this.getUnitsOnScreen();
		
		for (double x = (int) xLeft; x < xRight; x++)
		{
			bg.setColor(new Color(0xFFAAAAAA));
			bg.drawLine((int) (x * this.getPPU()), (int) (yDown * this.getPPU()), (int) (x * this.getPPU()), (int) (yUp * this.getPPU()));
		}
		
		for (double y = (int) yDown; y < yUp; y++)
		{
			bg.setColor(new Color(0xFFAAAAAA));
			bg.drawLine((int) (xLeft * this.getPPU()), (int) (y * this.getPPU()), (int) (xRight * this.getPPU()), (int) (y * this.getPPU()));
		}
		
		bg.translate(pos.x * ppu, pos.y * ppu);
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
}
