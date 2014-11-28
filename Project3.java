package project3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class Project3 {
	private Map<Integer, Jobs> _jobsMap = new HashMap<Integer, Jobs>();
	
	public void execute(String inputDir, String outputFile) throws Exception
	{
		File inDir = new File(inputDir);
		File outFile = new File(outputFile + "/output.tsv");
		
		System.out.println("Processing Jobs");
		processJobs(inDir);
		
		
		//build clustering algorithm
		
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
			jobs.setDesc(ar[1]);
			jobs.setReqs(ar[2]);
			
			_jobsMap.put(jobs.getId(), jobs);
		}
		br.close();
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
