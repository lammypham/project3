package project3;

import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.*;

import javax.sound.sampled.ReverbType;



public class Project3 {
	private Map<Integer, Jobs> _jobsMap = new HashMap<Integer, Jobs>();
	private Map<Integer, List<Double>> _jobsVector = new HashMap<Integer, List<Double>>();
	Set<String> st = new HashSet<String>();
	public void execute(String inputDir, String outputFile) throws Exception
	{
		File inDir = new File(inputDir);
		File outFile = new File(outputFile + "/output.tsv");
		
		System.out.println("Processing Jobs");
		processJobs(inDir);
		System.out.println("Finished Jobs");
		System.out.println("Processing Cluster");
		

		
			for (Integer m: _jobsMap.keySet())
			{
				Jobs job = _jobsMap.get(m);
				String[] arr = job.getDesc().split(" ");
				//double[] wordVector = new double[st.size()]; // size of vector based on all unique terms
				for (String s: st)
				{
							if(job.getDesc().contains(s))
							{
								double freq = termFreq(arr, s);
								double inverse = IDF(_jobsMap,s);
								double tf_idf = freq * inverse;
								
								List<Double> lst = _jobsVector.get(m);
									if(lst == null)
									{
										lst = new ArrayList<Double>();
										_jobsVector.put(m, lst);
									}
								lst.add(tf_idf);	
							}
							else
							{
								double tf_idf = 0;
							
								List<Double> lst = _jobsVector.get(m);
								if(lst == null)
								{
									lst = new ArrayList<Double>();
									_jobsVector.put(m, lst);
								}
								lst.add(tf_idf);
							
							}

				}
				System.out.println(job.getDesc());
				//System.out.println(_jobsVector.get(m));
				System.out.println(_jobsVector.get(m).size());
			}
			int vectorSize = _jobsVector.values().iterator().next().size();
			int flag = 0;
			int kVal = 3;
			double max = Math.pow(10,10);
			double distance = 0.0;
			boolean convergence = true;
			int x = 0;
			int total = _jobsMap.size();
			int clusterID = 0;
			ArrayList<Centroid> centroid = new ArrayList<Centroid>();
			
			for(int i=0; i < kVal; i++)
			{
				double d = 0.005;
				List<Double> xVal = new ArrayList<Double>();
				for(int j=0; j < vectorSize; j++)
				{
					xVal.add(d);							
				}
				
				d+= .02;
				
				Centroid c = new Centroid(xVal);
				centroid.add(c);
			}

			
			ArrayList<Data> dataList = new ArrayList<Data>();
			
			double min = 99999.9;
			int idx = 0;
			for(Integer jobId : _jobsVector.keySet())
			{
				Jobs j = _jobsMap.get(jobId);
				List<Double> lst = _jobsVector.get(jobId);
				Data data = new Data(lst);
				dataList.add(data);
				min = 999999.9;
				for(int i = 0; i < kVal; i ++)
				{
					distance = calculateDist(lst, centroid.get(i));
					if (distance < min)
					{
						min = distance;
						clusterID = i;
					}
					
				}
				data.setCluster(clusterID);
				idx++;
				int vsize = data.getData().size();
				for (int k = 0; k < kVal ; k++)
				{
					double[] totalVector = new double[vsize];
					double vectorCount = 0.0;
					for (int n = 0; n < dataList.size(); n ++)
					{
						if (data.getCluster() == k)
						{
							totalVector[n] += dataList.get(n).getData().get(n);
							vectorCount++;
						}
					}
					
					if(vectorCount > 0)
					{
						double[] newCentroid = new double[vsize];
						List<Double> temp = new ArrayList<Double>();
						for (int c = 0; c < vsize; c++)
						{
							newCentroid[c] = totalVector[c]/vectorCount;
							temp.add(newCentroid[c]);
						}
						
						
						centroid.get(k).setNewCentroid(temp);
					}

				}
				System.out.println(String.format("cluster: %d - lst: %d-  distance: %f - min: %f", data.getCluster(), lst.size(), distance, min));
			}
			
	
	}

	private boolean excludeWord(String w)
	{
		String[] exclude = {"a","about","above","after","again","against","all","am","any","aren't","across", "an", "and", "are", "as", "at", "be","been","because", "before",
				"being","below","between",
				"but", "by","can","can't","cannot","could","couldn't","did","didn't","do","does","doing","don't","during","down","each","few","from","further",
				"for","had","hadn't","has","hasn't","how","however","have", "if", "in", "into", "is", "it",
				"no", "not", "of", "on", "or", "such",
				"that", "the", "their", "then", "there", "these",
				"they", "this", "to", "was", "will", "with","you","your","&", "", "/","-","per","rep","staff","general","assistant"};
		boolean result = false;
		for(String s : exclude)
		{
			if(s.equalsIgnoreCase(w))
			{
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	private String removeWords(String desc) throws Exception
	{
		String[] ar = desc.split(" ");
		for(int i=0; i < ar.length; i++)
		{
			if(excludeWord(ar[i]))
				ar[i] = "";
		}
		StringBuilder sb = new StringBuilder();
		for(int i=0; i < ar.length; i++)
		{
			if(i > 0)
				sb.append(" ");
			sb.append(ar[i]);
			//for debugging
			if(i > 5)
				break;
		}
		
		return sb.toString();
	}
	
	private void processJobs(File inputDir) throws Exception
	{
		File jobsFile = new File(inputDir, "jobs.tsv");
		BufferedReader br = new BufferedReader(new FileReader(jobsFile));
		String line = br.readLine();
		int idx = 0;
		while((line = br.readLine()) != null)
		{
			String[] ar = line.split("\t");
			Jobs jobs = new Jobs(Integer.valueOf(ar[0]));
			if(ar[1] != null)
			{
				String desc = ar[1].replaceAll("\\<.*?>", "").replace("\\r","").replace("\\n","").replace("//r","");
				jobs.setDesc(removeWords(desc));
				
				String[] splitDesc = jobs.getDesc().split(" ");
				st.addAll(Arrays.asList(splitDesc));
			}
			
			if (ar.length > 2)
			{
				jobs.setReqs(ar[2]);
			}
			_jobsMap.put(jobs.getId(), jobs);
			if (idx ++ > 250)
			{
				break;
			}
		}
		br.close();
	}
	
	private double calculateDist(List<Double> lst, Centroid centroid)
	{
		double sum = 0.0;
		for(int i = 0; i <lst.size(); i++)
		{
			//if(i < centroid.getCentroid().size())
				sum = sum + Math.pow((lst.get(i) - centroid.getCentroid().get(i)), 2);
		}
		return Math.sqrt(sum);
	}
	
	private String base(String str)
	{
		String[] suffix = {"ing","able","ational","tional","ate","ive","ful","ation","ator","ment","tions","ess","ist","/r" };
		String s = str;
		for(int i=0; i < suffix.length; i++)
		{
			boolean b = str.toLowerCase().endsWith(suffix[i]);
			if(b)
			{
				s = str.substring(0, str.length() - suffix[i].length());
			}
		}
		return s;
	}
	
	static double termFreq(String [] record, String w)
	{
		double x = 0;
		for(String s: record)
		{
			if(s.equalsIgnoreCase(w))
			{
				x++;
			}
		}
		return x/record.length;
	}
	
	static double IDF (Map<Integer, Jobs> records, String w)
	{
		double x = 0;
		for(Integer k : records.keySet())
		{
			Jobs j = records.get(k);
			if(j.getDesc().contains(w))
				x++;
		}
		return Math.log(records.size()/x);
	}
	
	
	public static void main(String[] args)throws Exception {
		if(args.length < 2)
		{
			System.out.println("usage: Project3 input-dir output-file");
			System.exit(1);
		}
		new Project3().execute(args[0], args[1]);
		
		System.out.println("Finish");
	}
}
