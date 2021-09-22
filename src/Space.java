import java.util.ArrayList;
import java.util.List;

public class Space
{
	private List<Component> comps = new ArrayList<Component>();
	
	public void add(Component comp)
	{
		comps.add(comp);
	}
	
	public List<Component> getComps()
	{
		return comps;
	}
}
