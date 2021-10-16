import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import Utility.Vector2i;

public class Line implements Component
{
	Vector2i start, end;
	int col = 0xFFFF0000;
	
	@Override
	public void render(Graphics2D bg, Camera cam)
	{
		//Coordinates of bounding lines of camera (Camera is basically a square with 'pos' being centre of that square)
		double[] bounds = cam.getBounds();
		double xLeft = bounds[0], yUp = bounds[1], xRight = bounds[2], yDown = bounds[3];
		
		if (start.x != end.x)
		{
			/* Calculates yLeft and yRight using y = mx + c formula
			 * First it finds slope and c by using the two points of the line. [If y-y0 = m(x-x0), then y = mx + (y0-mx0) => c = y0 - mx0]
			 * Then it finds yLeft and yRight by plugging in xLeft and xRight into y = mx + c
			 */
			double slope = (end.y - start.y) / (end.x - start.x);
			double c = start.y - slope * start.x;
			double yLeft = xLeft * slope + c;
			double yRight = xRight * slope + c;
			bg.setColor(new Color(col));
			cam.drawLine(bg, xLeft, yLeft, xRight, yRight);
		}
		else
		{
			bg.setColor(new Color(col));
			cam.drawLine(bg, start.x, yDown, start.x, yUp);
		}
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
	
	@Override
	public boolean isInvalid()
	{
		if (start == null || end == null)
			return true;
		if (start.equals(end))
			return true;
		return false;
	}
	
	public JPanel getDetails()
	{
		JPanel details = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.CENTER, VerticalFlowLayout.TOP,20,20));
		details.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		JLabel slopeFont = new JLabel("SLOPE = " + (float) (end.y - start.y) / (end.x - start.x));
		slopeFont.setForeground(new Color(col));
		slopeFont.setFont(new Font("Serif",Font.BOLD,16));
		details.add(slopeFont);
		JLabel strPoint = new JLabel("(x1,y1)="+"("+start.x+","+start.y+")");
		strPoint.setForeground(new Color(col));
		strPoint.setFont(new Font("Calibri",Font.BOLD|Font.ITALIC,14));
		details.add(strPoint);
		JLabel endPoint = new JLabel("(x2,y2)="+"("+end.x+","+end.y+")");
		endPoint.setForeground(new Color(col));
		endPoint.setFont(new Font("Calibri",Font.BOLD|Font.ITALIC,14));
		details.add(endPoint);
		return details;
	}
}
