import javax.vecmath.Point3f;
 
public class LineSegment {

	int layer;
	int toolhead = 0; //DEFAULT TOOLHEAD ASSUMED TO BE 0!
	float speed;
	Point3f first, second;
	
	public LineSegment (Point3f a,Point3f b, int layernum, float speedz)
	{
		first = a;
		second = b;
		layer = layernum;
		speed = speedz;
	}
	public LineSegment(float x1, float y1, float z1, float x2, float y2, float z2, int layernum, float speedz)
	{
		first = new Point3f(x1, y1, z1);
		second = new Point3f(x2, y2, z2);
		layernum = layer;
		speed = speedz;
	}
	public LineSegment (Point3f a,Point3f b, int layernum, float speedz, int toolheadz)
	{
		first = a;
		second = b;
		layer = layernum;
		speed = speedz;
		toolhead = toolheadz;
	}
	public LineSegment(float x1, float y1, float z1, float x2, float y2, float z2, int layernum, float speedz, int toolheadz)
	{
		first = new Point3f(x1, y1, z1);
		second = new Point3f(x2, y2, z2);
		layernum = layer;
		speed = speedz;
		toolhead = toolheadz;
	}
	public Point3f[] getPointArray()
	{
		Point3f[] pointarr = { first, second };
		return pointarr;
	}
	public float getSpeed()
	{
		return speed;
	}
	public int getLayer()
	{
		return layer;
	}
	
}
