import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class Menu extends JScrollPane
{
	private List<Component> comps;
	private JPanel view;
	
	public Menu()
	{
		super();
		comps = new ArrayList<Component>();
		view = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.CENTER, VerticalFlowLayout.TOP));
		this.setViewportView(view);
	}
	
	//public Menu(LayoutManager layout)
	//{
	//super(layout);
	//}
	
	public void add(Component comp)
	{
		comps.add(comp);
		JPanel details = comp.getDetails();
		details.setPreferredSize(new Dimension(this.getPreferredSize().width-30, 300));
		view.add(details, BorderLayout.SOUTH);
	}
	
	public void remove(Component comp)
	{
		comps.remove(comp);
	}
}
