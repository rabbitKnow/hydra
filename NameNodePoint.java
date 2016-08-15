import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * manage the virtual points mapped by namenode
 * 
 * @author cokez
 * 
 */
public class NameNodePoint implements Comparable<NameNodePoint> {
	String name;
	List<Long> locationList;
	HashMap<Long, Long> locations;
	HashMap<Long, Long> lastLoad;
	long totalFileNum = 0;
	long delayTime;
	long FileNumRef;
	double arg0;
	double arg1;
	long delayRef;
	long delay;
	long lastDelay;
	double lastLoadValue;

	long lastTotalFileNum;

	public NameNodePoint() {

		init();

	}

	public NameNodePoint(String nodeName) {

		name = nodeName;
		init();
	}

	public void init() {
		lastLoadValue = 0;
		locations = new HashMap<Long, Long>();
		// set the base info
		delayRef = 10L;
		FileNumRef = 100000000L;
		arg0 = 0.1;
		arg1 = 0.9;
		locationList = new ArrayList<Long>();
		lastLoad = new HashMap<Long, Long>();

		// set the test info
		delay = 1L;
		lastDelay = 1;

	}

	public void getMovedLocation(NameNodePoint nn, Long location) {

		synchronized (locations) {

			
			Long value = nn.moveLocation(location);
			lastLoad.put(location,value);
			locations.put(location, locations.get(location) + value);
			// update the last balance info
			lastTotalFileNum = lastTotalFileNum + value;
			
			getUpdateBalanceValue();

		}

	}
	
	// test move
	public long moveLocation(long node) {
	
		Long fileCount = locations.remove(node);
		
		
		if (fileCount == null)
			return -1;
		lastTotalFileNum = lastTotalFileNum - fileCount;
		
		getUpdateBalanceValue();
		return fileCount;

	}
	

	// the return value is the inode num

	public long chooseLocation2Move() {
		long row = -1;
		long maxValue = -1;

		for (Long location : lastLoad.keySet()) {
			long value = lastLoad.get(location);
			if (value > maxValue) {
				maxValue = value;
				row = location;
			}
		}
		return row;
	}

	public boolean addLocation(long node) {
		locationList.add(node);
		locations.put(node, 0L);
		return true;
	}

	public boolean addLocation(long node, long value) {
		locationList.add(node);
		locations.put(node, value);
		return true;
	}

	public long getLocationValue(long node) {
		if (locations.get(node) == null) {
			return -1;
		}
		return locations.get(node);
	}

	public boolean deleteLocaiton(long node) {

		locationList.remove(node);
		lastLoad.remove(node);
		return true;
	}



	public String toString() {
		return "namenode ip:" + name;
	}

	public synchronized boolean createFile(String fileName, long location) {
		// verify the client NNT version
		
		if (!locationList.contains(location))
			return false;

		// execute the create operation
		try{
		locations.put(location, locations.get(location) + 1);
		}
		catch(Exception e){
			System.out.println("Exception:"+e.toString()+" location:"+location+" nn"+this);
		}
		totalFileNum++;
		return true;
	}

	// TODO report should associated with the nnc
	public double report() {

		lastLoad.clear();
		long fileSum = 0;
		for (Entry<Long, Long> unit : locations.entrySet()) {
			long fileCount = unit.getValue();
			fileSum += fileCount;
			lastLoad.put(unit.getKey(), fileCount);
		}
		lastDelay = delay;

		lastTotalFileNum = fileSum;
		System.out.println("nn file sum:"+fileSum);
		lastLoadValue = getUpdateBalanceValue();

		
		return lastLoadValue;
	}

	// update the last load value and return the value
	public double getUpdateBalanceValue() {
		lastLoadValue = arg0 * lastDelay / delayRef + arg1 * lastTotalFileNum
				/ FileNumRef;

		return lastLoadValue;
	}

	public double getLastLoadValue() {
		return lastLoadValue;
	}

	@Override
	public int compareTo(NameNodePoint o) {
		// TODO Auto-generated method stub
		double compareResult = this.lastLoadValue - o.lastLoadValue;
		if (compareResult > 0)
			return 1;
		else if (compareResult < 0)
			return -1;

		return 0;

	}

}