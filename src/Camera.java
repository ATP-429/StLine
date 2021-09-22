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
	}
}
