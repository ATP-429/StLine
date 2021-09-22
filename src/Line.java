import java.awt.Color;
import java.awt.Graphics2D;

import Utility.Vector2i;

public class Line implements Component
{
	Vector2i start, end;
	
	@Override
	public void render(Graphics2D bg, Camera cam)
	{
		//Coordinates of bounding lines of camera (Camera is basically a square with 'pos' being centre of that square)
		double xLeft = cam.getPos().x - cam.getUnitsOnScreen();
		double xRight = cam.getPos().x + cam.getUnitsOnScreen();
		double yUp = cam.getPos().y + cam.getUnitsOnScreen();
		double yDown = cam.getPos().y - cam.getUnitsOnScreen();
		
		/* Calculates yLeft and yRight using y = mx + c formula
		 * First it finds slope and c by using the two points of the line. [If y-y0 = m(x-x0), then y = mx + (y0-mx0) => c = y0 - mx0]
		 * Then it finds yLeft and yRight by plugging in xLeft and xRight into y = mx + c
		 */
		double slope = (end.y - start.y) / (end.x - start.x);
		double c = start.y - slope * start.x;
		double yLeft = xLeft * slope + c;
		double yRight = xRight * slope + c;
		bg.setColor(Color.RED);
		bg.drawLine((int) (xLeft * cam.getPPU()), (int) (yLeft * cam.getPPU()), (int) (xRight * cam.getPPU()), (int) (yRight * cam.getPPU()));
	}
	
	@Override
	public void startAt(Vector2i start)
	{
		this.start = start;
	}
	
	@Override
	public void endAt(Vector2i end)
	{
		this.end = end;
	}
}
