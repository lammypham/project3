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
			double SSE = 0.0;
			double d = 0.090564875;
			for(int i=0; i < kVal; i++)
			{
				
				List<Double> xVal = new ArrayList<Double>();
				for(int j=0; j < vectorSize; j++)
				{
					xVal.add(d);							
				}
				
				d = d/ kVal;
				//d+= .06;
				
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
					double[] totalVector = new double[vectorSize];
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
						double[] newCentroid = new double[vectorSize];
						List<Double> temp = new ArrayList<Double>();
						for (int c = 0; c < vectorSize; c++)
						{
							newCentroid[c] = totalVector[c]/vectorCount;
							temp.add(newCentroid[c]);
						}
						
						
						centroid.get(k).setNewCentroid(temp);
					}

				}
				if(distance > 0.0)
				{
					SSE = SSE + Math.pow(distance, 2);
				}
			
				System.out.println(String.format("cluster: %d - lst: %d-  distance: %f - min: %f", data.getCluster(), lst.size(), distance, min));
			}
			System.out.println(String.format("SSE: %f",SSE));
			while(convergence)
			{
				
					idx = 2;
					for (int k = 0; k < kVal; k++)
					{
						double[]totalVector = new double[vectorSize];
						double vectorCount = 0.0;
						for(int n = 0; n < dataList.size(); n ++)
						{
							if(dataList.get(n).getCluster() == k )
							{
								totalVector[n] += dataList.get(n).getData().get(n);
								vectorCount ++;
							}
						}
						if(vectorCount > 0)
						{
							double[] newCentroid = new double[vectorSize];
							List<Double> temp = new ArrayList<Double>();
							for(int c = 0; c < vectorSize; c++)
							{
								newCentroid[c] = totalVector[c]/vectorCount;
								temp.add(newCentroid[c]);
							}
							centroid.get(k).setNewCentroid(temp);
						}
					}
					
					convergence = false;
					
					for(int p = 0; p < dataList.size(); p ++)
					{
						Data tmp = dataList.get(p);
						min = 999999.9;
						for(int i = 0; i < kVal; i ++)
						{
							distance = calculateDist(tmp.getData(), centroid.get(i));
							if (distance < min)
							{
								min = distance;
								clusterID = i;
							}
							
						}
						tmp.setCluster(clusterID);
						//System.out.println(String.format("cluster: %d - lst: %d-  distance: %f - min: %f", tmp.getCluster(), tmp.getData().size(), distance, min));

						if(tmp.getCluster() != clusterID)
						{
							tmp.setCluster(clusterID);
							convergence = true;
						}
					}
					

				
				System.out.println("iteration: " + idx);
				
				idx++;
				return;
			}
			
	
	}

	private boolean excludeWord(String w)
	{
		String[] exclude = {"a","about","above","after","again","against","all","am","any","aren't","across", "an", "and", "are", "as", "at", "be","been","because", "before",
				"being","below","between",
				"but", "by","can","can't","cannot","could","couldn't","did","didn't","do","does","doing","don't","during","down","each","few","from","further",
				"for","had","hadn't","has","hasn't","how","however","have", "if", "in", "into", "is", "it","isn't", "let's","me",
				"more","most","mustn't", "my","myself","nor","no", "not", "of", "on", "or", 
				"only","other","ought","ours","our","ourselves","out","own","same","she","hire","time","jobs","service",
				"such","should","shouldn't", "so","some","than","them","themselves","theirs",
				"that", "the", "their", "then", "there", "these","they'd","they'll","those","through","thorough","too",
				"under","until","up","very","we","we'd","when","while","why","won't","would","wouldn't","men","women","who","set","hourly","hour","now",
				"long","term","full","love","help","make","find","seek","search","searching","position","apply","applied","applies","terms","fully","made","found","finds",
				"dedicates","dedicate","dedicated","looks","look","looking","join","joins","joined","require","requires","required","need","needs",
				"commit","committed","commits","many","main","mains","seeking","prepare","prepares","prepared","preparing","?",",",":","%","$","(",")","@","!","*",
				"...","/","","0","1","2","3","4","5","6","7","8","9","0","must","meet","meeting","size","within","may","take","taken","took","needed","maintains","maintain",
				"must","around","day","us","one","\t", "for:","Opportunity:","Classification:","Responsibilities:","RESPONSIBILITIES:",
				"include","limited","limits","follow","following:","following","followed","follows","strong","start","end","life","range","resume","telephone","work","behavior","behaviors",
				"behaving","behaves","behave","change","changes","changing","appropiate","allows","also","amongst","anybody","anyways","appropriate",
				"aside","available","because","before","com","consider","certain","definitely","don't","differen't","each","et","fifth","follows",
				"four","gets","goes","greetings","has","he","her","herin","him","how","i'm","immediate","indicate","instead","it","itself",
				"later","least","likely","more","nd","of","nothing","others","ourselves","own",
				"they", "this", "to", "was", "will", "with","you","your","&", "", "/","-","per","rep","staff","general","assistant",
				"according","actually","afterwards","allow","all","alone","already","appreciate","been","became","co","changes","five","first","former","hello","ie","ignored","inc","inc.",
				"yes","wish","whom","whose","went","title:","Compensation:","administrative","assistantdepartment:","to:","currently","throughout","located","employed","go","getter",
				"working","work","works","worked","plus","waste","new","room","copy","real","bring","brings","brought","order","orders","ordered",
				"responsible","website","support","calls","manner","owners","team","including","contract","ensure","ensured","ensures","ensuring",
				"asssist","local","family","suburbs","special","project","exciting","opportunity","deliver","delievered","delivers","signed",
				"Â","sells","sell","selling","inch","wide","mile","deep","process","culture","busy","calendar","able","interest","interested",
				"consider","considers","considered","add","adds","addition","like","likely","likelihood","space","part","moving","fast","pace",
				"well","known",};
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
			if(excludeWord(ar[i].toLowerCase()))
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
			if (idx ++ > 500)
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
