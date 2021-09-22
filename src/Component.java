import java.awt.Graphics2D;

import Utility.Vector2i;

public interface Component
{
	public abstract void render(Graphics2D bg, Camera cam);
	public abstract void startAt(Vector2i start); //The absolute coordinate in space, at which the mouse was first clicked should be passed here
	public abstract void endAt(Vector2i end); //The absolute coordinate in space, at which the mouse was released after the first click should be passed here
}
