import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.json.*;

public class tweets_k_means{

	//read data into map 
	public static Map<String, String> loadJsonMap(String FilePath) throws IOException {
	File file = new File(FilePath);
	Map<String,String> dataset = new HashMap();
	try
	{	
		InputStream is = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while((line=reader.readLine())!=null){
			StringReader sr = new StringReader(line);
			JsonReader rdr = Json.createReader(sr);
			JsonObject obj = rdr.readObject();
			String t2 = obj.getJsonString("text").toString();
			String id2 = obj.getJsonString("id_str").toString();
			String text = t2.substring(1, t2.length()-1);//get text
			String id = id2.substring(1, id2.length()-1);//get text id
			dataset.put(id, text);
		}
		reader.close();
        is.close();
	}catch(Exception e) {
	       	 e.printStackTrace();
	        }
		//System.out.println(dataset);
		return dataset;
	}
	//read initial centroids to array list
	public static ArrayList<String> loadSeeds(String FilePath) throws IOException{
		ArrayList<String> centroids = new ArrayList<>();
		File file = new File(FilePath);
		try{
			InputStream is = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while((line = reader.readLine())!=null){
				String[] id = line.split(",");
				centroids.add(id[0]);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		//System.out.println(centroids);
		return centroids;
	}
	//get centroid tweets
	public static ArrayList<String> getCentroids(ArrayList<String> seeds, Map<String, String> dataset, int k_value)
	{
		Random rand = new Random();
		ArrayList<String> centroids = new ArrayList<>();
		ArrayList<String> tweets = new ArrayList<>();
		while(centroids.size()<k_value) //randomly generalize number from 0 to 24
		{
			int seedID = rand.nextInt(25);
			if(!centroids.contains(seeds.get(seedID)))
			{centroids.add(seeds.get(seedID));}
		}
		for(int i = 0; i<centroids.size();i++)
		{
			tweets.add(dataset.get(centroids.get(i)));
		}
		return tweets;
	}
	//clustering
	public static Map<Integer, ArrayList<String>> tweetCluster(Map<String, String> data, ArrayList<String> centroids)
	{
		String tweetid = null;
		int clusterID = 0;
		Map<Integer, ArrayList<String>> cluster = null;
		for(int k = 0; k<25;k++){
			cluster = new HashMap();
			cluster.clear();
				for(int i = 1; i<= centroids.size(); i++){
				cluster.put(i,new ArrayList<String>());
				}
				for(Entry<String, String> entry:data.entrySet())
				{	
					float min = Float.MAX_VALUE;
		            String tweet = entry.getValue();
		            ArrayList<String> sepTweet = Seperate(tweet);
					for(int j =0; j<centroids.size();j++){ 
						String centroid = centroids.get(j);
						ArrayList<String> sepCentroid =Seperate(centroid);
			            float dist = JaccardDist(sepCentroid,sepTweet);
			            if(dist<min)
			            {
			            	min = dist;
			            	tweetid = entry.getKey();
			            	clusterID = j+1;
			            }
			        }
					cluster.get(clusterID).add(tweetid);
					//System.out.println(cluster);
				}
				centroids  = calMeanCentroids(cluster,data);
				//System.out.println(cluster);
		}
		//printMap(cluster);
		return cluster;
	}
	//calculate mean centroids
	public static ArrayList<String> calMeanCentroids(Map<Integer,ArrayList<String>> cluster, Map<String, String> data)
	{
		ArrayList<String> meanCentroids = new ArrayList<>();
		String id = null;
		for(int i = 1; i<=cluster.size();i++){
			int cSize = cluster.get(i).size(); //calculate the ith cluster's size
			float min = Float.MAX_VALUE;
			for(int j = 0; j<cSize ;j++)
			{	
				float sum = 0;
				String jid =cluster.get(i).get(j); //tweet id in ith cluster
				String jtweet = data.get(jid); //get the tweet from cluster
				ArrayList<String> sepjtweet =Seperate(jtweet);
				for(int k = 0; k<cSize;k++)
				{
					String kid =cluster.get(i).get(k); //tweet id in ith cluster
					String ktweet = data.get(kid); //get the tweet from cluster
					ArrayList<String> sepktweet =Seperate(ktweet);
					float dist = JaccardDist(sepjtweet,sepktweet);
					sum +=dist;
				}
				if(sum<min)
				{
					min = sum;
					id = cluster.get(i).get(j);
				}
			}
			meanCentroids.add(data.get(id));
		}
		//System.out.println(meanCentroids);
		return meanCentroids;
	}
	//calculate the jaccard distance
	public static float JaccardDist(ArrayList<String> centroid, ArrayList<String> tweet)
	{
		int count = 0; //count overlapping word
		float size = centroid.size()+tweet.size();//get sum of sizes of two string sets
		for(int i = 0; i<tweet.size();i++)
		{
			String word = tweet.get(i);
			if(centroid.contains(word))
			{
				count++;
			}
		}
		float dist = 1-(count/(size-count));
		return dist;
	}
	//calculate the sse
	public static float calSSE(Map<String,String> data, Map<Integer, ArrayList<String>> cluster)
	{
		ArrayList<String> centroidTweets = calMeanCentroids(cluster,data);
		float sumSEE = 0;
		for(int i = 0; i<centroidTweets.size();i++)
		{
			String centroid = centroidTweets.get(i);
			ArrayList<String> sepcentroid =Seperate(centroid);
			int size = cluster.get(i+1).size();
			for(int j = 0; j<size;j++){
				String tweetID = cluster.get(i+1).get(j);
				String tweet = data.get(tweetID);
				ArrayList<String> septweet =Seperate(tweet);
				float SEE = (float) Math.pow(JaccardDist(sepcentroid,septweet), 2);
				sumSEE +=SEE;
				}
		}
		return sumSEE;
	}
	//print map
	public static void printMap(Map<Integer, ArrayList<String>> cluster){
	      for(Entry<Integer, ArrayList<String>> entry:cluster.entrySet()){ 
	            System.out.println(entry.getKey()+"  " + entry.getValue());
	        }
	}
	//split tweets by each word
	public static ArrayList<String> Seperate(String tweet)
	{	//split centroid string
		StringTokenizer token=new StringTokenizer(tweet);
		ArrayList<String> sep = new ArrayList<>();
		HashSet<String> set = new HashSet<>();
		while(token.hasMoreTokens()){
			String s1= token.nextToken(", \n''!|-");
			set.add(s1); //add seperate string into set to eliminate repeated words
		} 
		Iterator<String> iterator=set.iterator(); //put those word into a array list
		while(iterator.hasNext())
		{
			sep.add(iterator.next());
		}
		return sep;
	}
	public static void main(String[] args) throws IOException{
		// /Users/alexhu/Desktop/Tweets.json
		//String path = "/Users/alexhu/Desktop/Tweets.json";
		//String seeds ="/Users/alexhu/Desktop/InitialSeeds.txt";
		Scanner sc = new Scanner(System.in);
		String k = args[0];
		System.out.println("--------k value recorded--------");
		String seeds = args[1];
		ArrayList<String> seedsID = loadSeeds(seeds);
		System.out.println("--------initial seeds loaded--------");
		String FilePath = args[2];
		Map<String, String> data = loadJsonMap(FilePath);
		System.out.println("--------json data loaded--------");
		String output =args[3];
		System.out.println("--------Out put file as:" + output +"--------");
		RandomAccessFile raf = new RandomAccessFile(new File(output), "rw");
		ArrayList<String> centroids = getCentroids(seedsID, data,Integer.parseInt(k)); //get 
		Map<Integer, ArrayList<String>> cluster = tweetCluster(data,centroids);
		for(Entry<Integer, ArrayList<String>> entry:cluster.entrySet()){ 
            int key =entry.getKey();
            raf.writeBytes(String.valueOf(key)+"\t");
            ArrayList<String> value = entry.getValue();
            for(int j = 0; j<value.size();j++)
            {
            	raf.writeBytes(String.valueOf(value.get(j))+",");
            }
            raf.writeBytes("\r\n");
        }
		float SSE = calSSE(data,cluster);
		raf.writeBytes("SSE is:" + String.valueOf(SSE)+"\r\n");
	}
  }
