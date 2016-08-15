import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class FileNameGenerator{
		private static final int DEFAULT_FILES_PER_DIRECTORY = 32;
		  
		  private int[] pathIndecies = new int[20]; // this will support up to 32**20 = 2**100 = 10**30 files
		  private String baseDir;
		  private String currentDir;
		  private int filesPerDirectory;
		  private long fileCount;

		  FileNameGenerator(String baseDir) {
		    this(baseDir, DEFAULT_FILES_PER_DIRECTORY);
		  }
		  
		  FileNameGenerator(String baseDir, int filesPerDir) {
		    this.baseDir = baseDir;
		    this.filesPerDirectory = filesPerDir;
		    reset();
		  }

		  String getNextDirName(String prefix) {
		    int depth = 0;
		    while(pathIndecies[depth] >= 0)
		      depth++;
		    int level;
		    for(level = depth-1; 
		        level >= 0 && pathIndecies[level] == filesPerDirectory-1; level--)
		      pathIndecies[level] = 0;
		    if(level < 0)
		      pathIndecies[depth] = 0;
		    else
		      pathIndecies[level]++;
		    level = 0;
		    String next = baseDir;
		    while(pathIndecies[level] >= 0)
		      next = next + "/" + prefix + pathIndecies[level++];
		    return next; 
		  }

		  synchronized String getNextFileName(String fileNamePrefix) {
		    long fNum = fileCount % filesPerDirectory;
		    if(fNum == 0) {
		      currentDir = getNextDirName(fileNamePrefix + "Dir");
		    }
		    String fn = currentDir + "/" + fileNamePrefix + fileCount;
		    fileCount++;
		    return fn;
		  }

		  private synchronized void reset() {
		    Arrays.fill(pathIndecies, -1);
		    fileCount = 0L;
		    currentDir = "";
		  }

		  synchronized int getFilesPerDirectory() {
		    return filesPerDirectory;
		  }

		  synchronized String getCurrentDir() {
		    return currentDir;
		  }
		  public static void main(String args[]){
			  FileNameGenerator nameGenerator = new FileNameGenerator("/test");
			  long odd = 0;
			  long even=0;
			  for(int i=0;i<60000;i++){
				  try {
						MessageDigest md = MessageDigest.getInstance("SHA-1");
						long hashCode = bytes2Long(md.digest(nameGenerator.getNextFileName("").getBytes()));// use
																					// the
																					// last
																					// 8
																					// byte
																					// as
																					// the
																					// hash
																					// to
						//System.out.println("hashCode"+hashCode);
						if(hashCode%2==1||hashCode%2==-1){
							odd++;
						}
						else{
							even++;
						}
							

					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			  }
			  System.out.println("odd:"+odd+"even:"+even);
		  }
		  public static long bytes2Long(byte[] bytes) {
			  
				long result = 0 ;
				
				for (int i = 0; i < 8; i++) {
					result +=bytes[i] << (i * 8);
				}
				System.out.println("result:"+result);
				return result;

			}
	}