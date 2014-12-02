package project3;

import java.util.ArrayList;
import java.util.List;

public class Centroid {
	private List<Double> _x;
	
	public Centroid(List<Double> x)
	{
		_x = x;
	}
	
	public List<Double> getCentroid()
	{
		return _x;
	}
	
	public void setNewCentroid(List<Double> x)
	{
		_x = x;
	}

}
