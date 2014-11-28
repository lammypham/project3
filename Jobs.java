package project3;

public class Jobs {
	private Integer _id;
	private String _description;
	private String _requirements;
	public Jobs (Integer id)
	{
		_id = id;
	}
	
	public Integer getId()
	{
		return _id;
	}
	
	public void setDesc(String desc)
	{
		_description = desc;
	}
	
	public String getDesc()
	{
		return _description;
	}
	
	public void setReqs(String reqs) 
	{
		_requirements= reqs;
	}
	
	public String getReqs()
	{
		return _requirements;
	}
}
