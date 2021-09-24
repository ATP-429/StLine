import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class Menu extends JPanel
{
	private List<Component> comps = new ArrayList<Component>();
	
	public void add(Component comp)
	{
		comps.add(comp);
	}
	
	public void remove(Component comp)
	{
		comps.remove(comp);
	}
}
