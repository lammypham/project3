package project3;

import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;



public class Project3 {
	private Map<Integer, Jobs> _jobsMap = new HashMap<Integer, Jobs>();
	private Map<Integer, List<Double>> _jobsVector = new HashMap<Integer, List<Double>>();
	
	public void execute(String inputDir, String outputFile) throws Exception
	{
		File inDir = new File(inputDir);
		File outFile = new File(outputFile + "/output.tsv");
		
		System.out.println("Processing Jobs");
		processJobs(inDir);
		
		/*
		//build clustering algorithm
		String[] exclude = {"the","staff","per","on","as","be", "to","is","for", "�", "and", "in", "a", "can", "or", "with","at","of","not","&", "","an", "/","-","rep","general","assistant","you","are"};
		//stem words, learn to use RE and stem the tags
		String last = null;
		ArrayList<String> descList = null;
		for (Integer i : _jobsMap.keySet())
		{
			Jobs job = _jobsMap.get(i);
			
			String desc = job.getDesc().toLowerCase();
			desc = desc.replaceAll("\\<.*?>", "").replace("\\r","").replace("\\n","").replace("//r","");
			//descList.add(desc);
			//descList.remove
			String[] ar = desc.split(" ");
			String space = " ";
			String word = " ";
			for (int j = 0; j <ar.length; j++)
			{
				String w = ar[j];
				boolean cont = true;
				for (int k = 0; k < exclude.length ; k ++)
				{
					if (w.equalsIgnoreCase(exclude[k]))
					{
						cont = false;
						break;
					}
				}
				if(cont)
				{
					String b = base(w).toLowerCase();
					word = word.concat(b);
					word = word.concat(space);
					
					
				}
				
			}
			last = word;
			//System.out.println(word);
			
		}
		System.out.println(last);
		System.out.println(last.length());
		*/
		
		for(Integer n : _jobsMap.keySet())
		{
			Jobs j = _jobsMap.get(n);
			String[] ar = j.getDesc().split(" ");
			for(String w : ar)
			{
				if(!excludeWord(w))
				{
					double freq = termFreq(ar, w);
					double inv = IDF(_jobsMap, w);
					double tfidf = freq * inv;
					
					List<Double> lst = _jobsVector.get(n);
					if(lst == null)
					{
						lst = new ArrayList<Double>();
						_jobsVector.put(n, lst);
					}
					lst.add(tfidf);
				}
			}
			System.out.println(j.getDesc());
			System.out.println(_jobsVector.get(n));
		}
		
	}

	private boolean excludeWord(String w)
	{
		String[] exclude = {"the","staff","per","on","as","be", "to","is","for", "�", "and", "in", "a", "can", "or", "with","at","of","not","&", "","an", "/","-","rep","general","assistant","you","are"};
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
	
	private void processJobs(File inputDir) throws Exception
	{
		File jobsFile = new File(inputDir, "jobs.tsv");
		BufferedReader br = new BufferedReader(new FileReader(jobsFile));
		String line = br.readLine();
		while((line = br.readLine()) != null)
		{
			String[] ar = line.split("\t");
			Jobs jobs = new Jobs(Integer.valueOf(ar[0]));
			if(ar[1] != null)
			{
				String desc = ar[1].replaceAll("\\<.*?>", "").replace("\\r","").replace("\\n","").replace("//r","");
				jobs.setDesc(desc);
			}
			
			if (ar.length > 2)
			{
				jobs.setReqs(ar[2]);
			}
			_jobsMap.put(jobs.getId(), jobs);
		}
		br.close();
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
			String[] words = j.getDesc().split(" ");
			for (String s:words)
			{
				if (s.equalsIgnoreCase(w))
				{
					x++;
					break;
				}
			}

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
