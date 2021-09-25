import java.util.ArrayList;
import java.util.List;

import Utility.Vector2i;

public class Space
{
	private double snapRadius = 0.3;
	
	private List<Component> comps = new ArrayList<Component>();
	
	public void push(Component comp)
	{
		comps.add(comp);
	}
	
	public List<Component> getComps()
	{
		return comps;
	}
	
	public void pop()
	{
		if (comps.size() > 0)
			comps.remove(comps.size() - 1);
		else
			System.err.println("ERROR at Space.java.pop() : comps List empty");
	}
	
	//Returns coordinate to which the point passed must "snap" to
	public Vector2i snapFrom(Vector2i point)
	{
		Vector2i snap = point.round();
		if (snap.distanceFrom(point) < snapRadius)
			return snap;
		return point;
	}
}
