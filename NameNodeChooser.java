import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * 
 * @author cokez
 * 
 */

public class NameNodeChooser extends Thread {
	int totalPointNum;
	NameNodePoint[] NNT;
	List<NameNodePoint> namenodeList;
	static String ips[];
	double maxThreshold;
	int initNNStartNum;
	int currentNNNum;
	boolean shouldRun;
	long timeInterval = 10;
	double balanceThreshold;
	FileWrite fw;

	public NameNodeChooser(int num) {
		totalPointNum = num;// default is 100
		init();

	}

	public NameNodeChooser() {

		totalPointNum = 100;// default is 100
		init();

	}

	public void init() {
		try {
			fw = new FileWrite();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		initNNStartNum = 3;
		balanceThreshold = 0.05;
		timeInterval = 10;
		shouldRun = true;
		maxThreshold = 0.8;
		initTable();
	}

	public void initTable() {
		/*
		 * init the nnc
		 */
		namenodeList = new ArrayList<NameNodePoint>();
		NNT = new NameNodePoint[totalPointNum];

		// add the namenode readed to the list
		readNamenodeConf();
		int index = 0;
		int serverNum = namenodeList.size();
		for (int i = 0; i < serverNum; i++) {
			int unitLength = (totalPointNum - index) / (serverNum - i);
			for (int j = 0; j < unitLength; j++) {
				NNT[index] = namenodeList.get(i);// add the index to the table
				namenodeList.get(i).addLocation(index); // add the index to the
														// namenode list
				// System.out.println(namenodeList.get(i).name);
				index++;
			}
		}

	}

	/**
	 * read the namenode ip and port ,now for test auto generate
	 */
	public void readNamenodeConf() {
		// for test
		int NNNum = 20;
		produceTest(NNNum);
		for (int i = 0; i < initNNStartNum; i++) {
			namenodeList.add(new NameNodePoint(ips[i]));

		}
		currentNNNum = initNNStartNum;

	}

	public void printTable() {
		for (int i = 0; i < NNT.length; i++) {
			System.out.println(NNT[i].toString());
		}
	}

	// for test
	public static void produceTest(int num) {
		ips = new String[num];
		String single = "192.168.1.";
		for (int i = 0; i < num; i++) {
			ips[i] = single + i;
		}
	}

	public long bytes2Long(byte[] bytes) {
		long result = 0;
		for (int i = 0; i < 7; i++) {
			result +=bytes[i] << (i * 8);
		}
		return result;

	}

	public static String getFilePath() {
		return null;
	}

	public NameNodePoint[] getNNT() {
		return NNT;
	};

	@Override
	public void run() {
		while (shouldRun) {
			try {
				Thread.sleep(timeInterval * 1000);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			double sum = 0;
			for (NameNodePoint nn : namenodeList) {
				double value = nn.report();
				System.out.println("nn total sum:"+nn.totalFileNum);

				sum += value;

			}
			TreeSet<NameNodePoint> balanceSet = new TreeSet<NameNodePoint>();
			// check if nn value >balance threshold

			balanceSet.addAll(namenodeList);
			fw.writeLine("before the balace:");
			fw.writeLine(
					String.format("%.2f",sum / namenodeList.size()*100));
			
			fw.writeLine(
						String.format("%.2f",balanceSet.first().getLastLoadValue()*100));
			fw.writeLine(
					String.format("%.2f",balanceSet.last().getLastLoadValue()*100));
			
			
			// if the average >threshold ,add server
			while (sum / namenodeList.size() >= maxThreshold) {

				addNameNode();
			}
			balanceSet.addAll(namenodeList);
			double averageLoad = sum / namenodeList.size();
			System.out.println("averaged load:"+averageLoad);
			

			System.out.println("list size is " + namenodeList.size()
					+ " set size is " + balanceSet.size());

			// print the statistic before the balance

			

			while (balanceSet.last().getLastLoadValue() - averageLoad > balanceThreshold) {

				NameNodePoint maxLoadNN = balanceSet.last();
				NameNodePoint minLoadNN = balanceSet.first();
				// remove before balance the load
				balanceSet.remove(minLoadNN);
				balanceSet.remove(maxLoadNN);

				long location = maxLoadNN.chooseLocation2Move();
				NNT[(int) location] = minLoadNN;
				minLoadNN.addLocation(location);
				maxLoadNN.deleteLocaiton(location);
				minLoadNN.getMovedLocation(maxLoadNN, location);

				balanceSet.add(minLoadNN);
				balanceSet.add(maxLoadNN);
				// System.out.println("balace:");
				// for (NameNodePoint nn : balanceSet) {
				// System.out.println(nn + " loadBalance value is "
				// + nn.getLastLoadValue());
				// }

			}

			// print the statistic after the balance
			fw.writeLine("after the balace:");
			fw.writeLine(
					String.format("%.2f",averageLoad*100));
			
			fw.writeLine(
						String.format("%.2f",balanceSet.first().getLastLoadValue()*100));
			fw.writeLine(
					String.format("%.2f",balanceSet.last().getLastLoadValue()*100));
		}

	}

	public boolean addNameNode() {

		namenodeList.add(new NameNodePoint(ips[currentNNNum++]));
		return true;

	}

	public class FileWrite {
		String path;
		FileWriter fw;

		FileWrite() throws IOException {
			path = "/home/cokez/experResult_" + System.currentTimeMillis();
			init();
		}

		FileWrite(String file) throws IOException {
			path = file + "_" + System.currentTimeMillis();
			init();
		}

		void init() throws IOException {
			fw = new FileWriter(path);
		}

		void writeLine(String info) {
			try {
				fw.write(info + '\n');
				fw.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String args[]) {

		NameNodeChooser chooser = new NameNodeChooser(1000);
		Client client = new Client(chooser);
		client.start();
		chooser.start();

		// chooser.printTable();

	}
}
