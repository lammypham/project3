package project3;

import java.util.List;

public class Data {

	List<Double> _data;
	int _clusterID;
	
	public Data (List<Double> data)
	{
		_data = data;
	}
	
	public List<Double> getData()
	{
		return _data;
	}
	public void setCluster(Integer cID)
	{
		_clusterID = cID;
	}
	
	public Integer getCluster()
	{
		return _clusterID;
	}
}
