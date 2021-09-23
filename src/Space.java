import java.util.ArrayList;
import java.util.List;

public class Space
{
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
			System.err.println("Space.java : comps List empty");
	}
}
