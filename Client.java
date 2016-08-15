import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;





public class Client extends Thread{
	FileNameGenerator nameGenerator ;
	long threadNum;
	long opsNum;
	List<StatsDaemon> daemons;
	NameNodeChooser chooserServer;
	NameNodePoint[] NNT;
	public Client(){
		init();
		
	}
	public Client(NameNodeChooser chooser){
		chooserServer=chooser;
		this.NNT=chooserServer.getNNT();
		init();
	}
	
	//set the test info(thread num,inode num)
	public void init(){
		nameGenerator = new FileNameGenerator("/test");
		
		threadNum=6L;
		opsNum=500000000L;
		
		
		//init the thread
		daemons=new ArrayList<StatsDaemon>();
		long residue=opsNum;
		for(int i=0;i<threadNum;i++){
			
			long opNum=residue/(threadNum-i);
			residue-=opNum;
			daemons.add(new StatsDaemon(opNum));
			
		}
		
		
	}
	public class StatsDaemon extends Thread{

		long opNum;
		public StatsDaemon(long aopNum){
			opNum=aopNum;
			System.out.println("opNum"+aopNum);
		}
		public void run() {
			for (int i = 0; i < opNum; i++) {
				createFile(nameGenerator.getNextFileName(""));
			}
		}
	}
	public long bytes2Long(byte[] bytes) {
		long result = 0;
		for (int i = 0; i < 7; i++) {
			result = +bytes[i] << i * 8;
		}
		return result;

	}
	public boolean createFile(String fileName) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			long hashCode = bytes2Long(md.digest(fileName.getBytes()));// use
																		// the
																		// last
																		// 8
																		// byte
																		// as
																		// the
																		// hash
																		// to

			int offset = (int) (hashCode % (long)  NNT.length);
			if (offset < 0)
				offset += NNT.length;// cancel the negative num
			
			//if create file fail ,update the NNT
			while(!NNT[offset].createFile(fileName, offset)){
				this.NNT=chooserServer.getNNT();
			}
			
			/*System.out.println("filename:" + fileName + " offset" + offset
					+ " hashcode:" + hashCode);*/

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		for(StatsDaemon t:daemons){
			t.start();
		}
		
	}


}
