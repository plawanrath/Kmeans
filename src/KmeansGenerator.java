import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

public class KmeansGenerator {
	public final static int K = 17;
	public final static int MAX_ITERATIONS = 25;
	public final static double VERY_LARGE_VALUE = 500000.0;
	HashMap<Integer, ArrayList<Integer>> pointsInClusters = new HashMap<Integer, ArrayList<Integer>>();
	
	public int[] getRandomIds(Point[] sampleSpace,int k,int min,int max)
	{
		HashSet<Integer> used = new HashSet<Integer>();
		int[] res = new int[k];
		int x;
		for(int i=0;i<K;i++)
		{
			do
			{
				Random r = new Random();
				x = r.nextInt((max - min) + 1) + min;
			} while(used.contains(x));
			res[i] = x;
			used.add(x);
		}
		return res;
	}
	
	public Point[] getCentroids(int[] res,Point[] sampleSpace)
	{
		Point[] cPoints = new Point[K];
		for(int i=0;i<K;i++)
		{
			cPoints[i] = sampleSpace[res[i]-1];
		}
		return cPoints;
	}
	
	public double euclidian(double x1,double y1,double x2,double y2)
	{
		double xdiff = x2 - x1;
		double ydiff = y2 - y1;
		double xSq = Math.pow(xdiff, 2);
		double ySq = Math.pow(ydiff, 2);
		double result = Math.sqrt(xSq+ySq);
		return result;
	}
	
	public int getMin(double[] array)
	{
		double minVal = array[0];
		int minIndex = 0;
		for(int i=1;i<array.length;i++)
		{
			if(array[i] <= minVal)
			{
				minVal = array[i];
				minIndex = i;
			}
		}
		return minIndex;
	}
	
	public int getClusterForPoint(Point[] centroid, Point p)
	{
		double[] distances = new double[K];
		for(int i=0;i<centroid.length;i++)
			distances[i] = this.euclidian(centroid[i].getX(), centroid[i].getY(), p.getX(), p.getY());
		int indexOfMin = this.getMin(distances);
		return indexOfMin;
	}
	
	public boolean checkInCentroidList(Point[] centroid,Point P)
	{
		for(int i=0;i<centroid.length;i++)
		{
			if(centroid[i].getId() == P.getId())
				return true;
		}
		return false;
	}
		
	public void putIntoCentroids(Point[] centroids,Point[] resP)
	{
		for(int i=0;i<resP.length;i++)
		{
			int indexOfCentroid = this.getClusterForPoint(centroids, resP[i]);
			int centroidID = centroids[indexOfCentroid].getId();
			int pointID = resP[i].getId();
			ArrayList<Integer> temp;
			if(this.pointsInClusters.get(centroidID) != null)
			{
				temp = this.pointsInClusters.get(centroidID);	
				temp.add(pointID);
			}
			else
			{
				temp = new ArrayList<Integer>();
				temp.add(pointID);
			}
			this.pointsInClusters.put(centroidID, temp);
		}
	}

	private double getAvg(ArrayList<Double> myList)
	{
		int size = myList.size();
		double sum = 0.0;
		for(int i = 0;i<size;i++)
			sum = sum + myList.get(i);
		double avg = sum/size;
		double res = Math.round(avg * 10000) / 10000.0;
		return res;
	}
	
	private Point getAverageOfPoints(int centroidId, ArrayList<Integer> listOfPoints, Point[] sampleSpace)
	{
		Point temp = new Point();
		temp.setId(centroidId);
		ArrayList<Double> xValues = new ArrayList<Double>();
		ArrayList<Double> yValues = new ArrayList<Double>();
		for(int i=0;i<listOfPoints.size();i++)
		{
			int tempId = listOfPoints.get(i);
			xValues.add(sampleSpace[tempId-1].getX());
			yValues.add(sampleSpace[tempId-1].getY());
		}
		double avgX = this.getAvg(xValues);
		double avgY = this.getAvg(yValues);
		temp.setX(avgX);
		temp.setY(avgY);
		return temp;
	}
	
	public Point[] recomputeCentroids(Point[] resP)
	{
		Point[] tempC = new Point[K];
		HashMap<Integer,ArrayList<Integer>> temp = this.pointsInClusters;
		Iterator mp = temp.entrySet().iterator();
		int i = 0;
		while(mp.hasNext())
		{
			Entry pair = (Entry) mp.next();
			Point avgPoint;
			ArrayList<Integer> pointIds = (ArrayList<Integer>) pair.getValue();
			if(pointIds.size() > 1)
			{
				avgPoint = this.getAverageOfPoints(i, pointIds, resP);
			}
			else
			{
				int Id = pointIds.get(0);
				avgPoint = resP[Id-1];
				avgPoint.setId(i);
			}
			tempC[i] = avgPoint;
			i++;
		}
		System.out.println();
		return tempC;
	}
	
	public boolean compareArrayLists(ArrayList<Integer> l1, ArrayList<Integer> l2)
	{
		for(int i=0;i<l1.size();i++)
		{
			if(!l1.contains(l2.get(i)))
				return false;
		}
		return true;
	}
	
	public boolean compareHashMaps(HashMap<Integer,ArrayList<Integer>> h1, HashMap<Integer,ArrayList<Integer>> h2)
	{
		Iterator mp = h1.entrySet().iterator();
		while(mp.hasNext())
		{
			Entry pair = (Entry) mp.next();
			ArrayList<Integer> s1 = (ArrayList<Integer>) pair.getValue();
			ArrayList<Integer> s2 = (ArrayList<Integer>) pair.getValue();
			if(!compareArrayLists(s1,s2))
				return false;
		}
		return true;
	}
	
	public ArrayList<Double> SSE(Point[] centroids,HashMap<Integer,ArrayList<Integer>> cluster,Point[] allPoints)
	{
		ArrayList<Double> res = new ArrayList<Double>();
		for(int i=0;i<centroids.length;i++)
		{
			double result = 0.0;
			double centX = centroids[i].getX();
			double centY = centroids[i].getY();
			ArrayList<Integer> pointIds = cluster.get(i);
			for(int j=0;j<pointIds.size();j++)
			{
				int id = pointIds.get(j);
				double px = allPoints[id-1].getX();
				double py = allPoints[id-1].getY();
				double rval = this.euclidian(centX, centY, px, py);
				rval = Math.pow(rval, 2);
				result = result + rval;
			}
			res.add(result);
		}
		return res;
	}
	
	public static void main(String[] args) {
		HashMap<Integer, ArrayList<Integer>> previousClusters,newClusters;
		ReadPoints rp = new ReadPoints("test_data.txt");
		KmeansGenerator kmg = new KmeansGenerator();
		Point[] resP = rp.getPoints();
		int[] ids = kmg.getRandomIds(resP, K, 1, rp.getPointCount());
		Point[] centroids = kmg.getCentroids(ids, resP);
		kmg.putIntoCentroids(centroids, resP);
		System.out.println();
		previousClusters = kmg.pointsInClusters;
		Point[] newCentroids = kmg.recomputeCentroids(resP);
		for(int i=0;i<KmeansGenerator.MAX_ITERATIONS;i++)
		{
			kmg.pointsInClusters.clear();
			kmg.putIntoCentroids(newCentroids, resP);
			newClusters = kmg.pointsInClusters;
			if(kmg.compareHashMaps(previousClusters, newClusters) == true)
				break;
			newCentroids = kmg.recomputeCentroids(resP);
		}
		Iterator mp = kmg.pointsInClusters.entrySet().iterator();
		System.out.println("Cluster ID\tList of points");
		while(mp.hasNext())
		{
			Entry pair = (Entry) mp.next();
			int clusterid = (int) pair.getKey();
			System.out.print(clusterid+1 + "\t\t");
			ArrayList<Integer> pointIds = (ArrayList<Integer>) pair.getValue();
			for(int i=0;i<pointIds.size()-1;i++)
				System.out.print(pointIds.get(i) + ",");
			System.out.print(pointIds.get(pointIds.size()-1));
			System.out.println();
		}
		System.out.println();
		ArrayList<Double> sseValues = kmg.SSE(newCentroids, kmg.pointsInClusters, resP);
		System.out.println("SSE");
		for(int i=0;i<sseValues.size();i++)
		{
			System.out.println(i+1 + " = " + sseValues.get(i));
		}
	}
}
