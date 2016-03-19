
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
/*
 * /Users/alexhu/Documents/2015fall/machine learning/assignment5/test_data.txt
 */
public class k_means {
	//read data x & y into an array list xy
	public static ArrayList<ArrayList<Double>> loadXY (String FilePath) throws IOException { 
		ArrayList<ArrayList<Double>> dataset = new ArrayList<>(); //arraylist for X and Y
		File file = new File(FilePath);
		try
		{	InputStream is = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = reader.readLine();
			while((line=reader.readLine())!=null){
	            String[] values=line.split("	");
	            ArrayList<Double> dataXY=new ArrayList<>();
	            for(int i=1;i<values.length;i++){
		            dataXY.add(Double.parseDouble(values[i])); //put X and Y into arraylist
	            }
	            dataset.add(dataXY);
	        }
			reader.close();
	        is.close();
		}catch(Exception e) {
       	 e.printStackTrace();
        }
		return dataset;
		//System.out.println(dataset);
	}
	//read data set ID into an array list ID
	public static ArrayList<Integer> loadID (String FilePath) throws IOException { 
		ArrayList<Integer> dataID = new ArrayList<>(); //arraylist for ID
		File file = new File(FilePath);
		try
		{	InputStream is = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = reader.readLine();
			while((line=reader.readLine())!=null){
	            String[] values=line.split("	");
	            dataID.add(Integer.parseInt(values[0])); //put id in arraylist
	        }
			reader.close();
	        is.close();
		}catch(Exception e) {
       	 e.printStackTrace();
        }
		//System.out.println(dataID);
		return dataID;
	}
	//Initialize k centroids put into an array list
	public static ArrayList<ArrayList<Double>> Kcentroids(int k,ArrayList<ArrayList<Double>> data) 
	{	  
		  ArrayList<Integer> centroidsID = new ArrayList<>();
		  ArrayList<ArrayList<Double>> centroidsSet = new ArrayList<>();
		  Random rand = new Random(); 
	      while(centroidsID.size()<k) 
	      {
		    int centroidPoint = rand.nextInt(99)+1;
		    if(!centroidsID.contains(centroidPoint))
		    {
		    	centroidsID.add(centroidPoint);
		    	centroidsSet.add(data.get(centroidPoint));
		    }
	      }
	    return centroidsSet;
	    //System.out.println("the size of map after : " + centroidsID);
	}
	
	//print map for testing
	public static void printMap(Map<Integer, ArrayList<Integer>> cluster){
	      for(Entry<Integer, ArrayList<Integer>> entry:cluster.entrySet()){ 
	            System.out.println(entry.getKey()+"  " + entry.getValue());
	        }
	}
	//calculate the distance
	public static double euclideanDistance(double x1, double y1, double x2, double y2)
	{
		double distance = Math.sqrt(Math.pow(Math.abs(x1-x2),2) + Math.pow(Math.abs(y1-y2),2));
		return distance;
	}
	
	//calculate the mean centorids
	public static ArrayList<ArrayList<Double>> calMeanCentroids(Map<Integer, ArrayList<Integer>> cluster,ArrayList<ArrayList<Double>> data)
	{
		ArrayList<ArrayList<Double>> centroidsSet = new ArrayList<>();
		for(int k = 1; k<=cluster.size();k++)
		{	
			ArrayList<Double> meanPoints = new ArrayList<>();
			double xSum = 0;
			double ySum = 0;
			int size = cluster.get(k).size();
			//System.out.println(size);
			for(int i = 0 ; i <size; i++)
			{
				int id = cluster.get(k).get(i) -1;
				xSum += data.get(id).get(0);
				ySum += data.get(id).get(1);
			}
			double x = xSum/size; //get the mean value of x
			double y = ySum/size; //get the mean value of y
			meanPoints.add(x); //add x to array list
			meanPoints.add(y);//add y to array list 
			centroidsSet.add(meanPoints);
		}
		return centroidsSet;
	}
	//start k-means
	public static Map<Integer, ArrayList<Integer>> kClustering(ArrayList<ArrayList<Double>> centroidsSet, ArrayList<ArrayList<Double>> data)
	{
		int clusterID = 0; //map for cluster
		Map<Integer, ArrayList<Integer>> cluster = null;
		//initialize map
		//System.out.println(centroidsSet);
		//start clustering
		for(int k = 0; k <25; k++) 
		{
			cluster = new HashMap();
			for(int i = 1; i <centroidsSet.size()+1; i++)
			{
				cluster.put(i, new ArrayList<Integer>());
			}
			for(int i = 0; i < data.size(); i++)
			{
				double min = Double.MAX_VALUE;
				double x2 = data.get(i).get(0); //get x label from data set as in x2
				double y2 = data.get(i).get(1);	//get y label from data set as in y2
				for(int j = 0; j <centroidsSet.size(); j++){
					double x1 = centroidsSet.get(j).get(0); //get centroids's x label as in x1
					double y1 = centroidsSet.get(j).get(1);//get centroids's y label as in y1
					double distance = euclideanDistance(x1,y1,x2,y2); //calculate the euclidean distance
					if(distance < min) 
					{
						min = distance;
						clusterID = j+1;
					}
				}
				int id = i+1;
				cluster.get(clusterID).add(id);
			}
		centroidsSet = calMeanCentroids(cluster,data);
		}
		return cluster;
	}
	
	//calculate the sse
	public static double SSE(ArrayList<ArrayList<Double>> centroidsSet, ArrayList<ArrayList<Double>> data,Map<Integer, ArrayList<Integer>> cluster ){
		double sseSum = 0;
		for(int j = 0; j <centroidsSet.size(); j++){
				double x1 = centroidsSet.get(j).get(0); //get centroids's x label as in x1
				double y1 = centroidsSet.get(j).get(1);//get centroids's y label as in y1
				int cID = j+1; //cluster ID
					for(int k = 0; k<cluster.get(cID).size();k++)
					{
						int id = cluster.get(cID).get(k)-1;
						double x2 = data.get(id).get(0);//get x label from data set as in x2
						double y2 = data.get(id).get(1);	//get y label from data set as in y2
						double distSquare = Math.pow(euclideanDistance(x1,y1,x2,y2), 2); 
						sseSum += distSquare; //calculate sse
					}
			}
		return sseSum;
	}
	
	public static void main(String[] args) throws IOException 
		{
			
			//"/Users/alexhu/Desktop/test_data.txt";
			Scanner sc = new Scanner(System.in);
			String k = args[0];
			System.out.println("--------k value recorded--------");
			String FilePath = args[1];
			ArrayList<ArrayList<Double>> dataset = loadXY(FilePath);
			System.out.println("--------File loaded--------");
			String output =args[2];
			System.out.println("--------Out put file as:" + output +"--------");
			RandomAccessFile raf = new RandomAccessFile(new File(output), "rw");
			//get centorids 
			ArrayList<ArrayList<Double>> centroids = Kcentroids(Integer.parseInt(k),dataset); 
			//initializing k-mean
			Map<Integer, ArrayList<Integer>> kClustering = kClustering(centroids,dataset);
			for(Entry<Integer, ArrayList<Integer>> entry:kClustering.entrySet()){ 
	            int key =entry.getKey();
	            raf.writeBytes(String.valueOf(key)+"\t");
	            ArrayList<Integer> value = entry.getValue();
	            for(int j = 0; j<value.size();j++)
	            {
	            	raf.writeBytes(String.valueOf(value.get(j))+",");
	            }
	            raf.writeBytes("\r\n");
	        }
			//calculate mean centroids
			ArrayList<ArrayList<Double>> meancentroids = calMeanCentroids(kClustering,dataset);
			double sseSum = SSE(meancentroids,dataset,kClustering);
			raf.writeBytes("SSE is:" + String.valueOf(sseSum)+"\r\n");
			raf.writeBytes("\r\n");
		}
	}

