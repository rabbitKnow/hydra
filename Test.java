import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class Test {
	public static ByteBuffer getByteBuffer(String str)  
    {  
        return ByteBuffer.wrap(str.getBytes());  
    }  
	public static String getString(ByteBuffer buffer)  
    {  
        Charset charset = null;  
        CharsetDecoder decoder = null;  
        CharBuffer charBuffer = null;  
        try  
        {  
            charset = Charset.forName("UTF-8");  
            decoder = charset.newDecoder();  
            // charBuffer = decoder.decode(buffer);//用这个的话，只能输出来一次结果，第二次显示为空  
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());  
            
            return charBuffer.toString();  
        }  
        catch (Exception ex)  
        {  
            ex.printStackTrace();  
            return "error";  
        }  
    }  
	public static void testchannel() throws Exception{
		RandomAccessFile file =new RandomAccessFile("/home/cokez/test.file","rw");
		FileChannel fc=file.getChannel();
		for(int i=0;i<10000;i++)
			Tools.writeFString(fc, "lala");
	}
	public static void testarg(long i){
		System.out.println("testarg"+i);
	}
	public static void main(String args[]) throws  Exception{
		
		
		File confDir=new File("/home/cokez/metaData/");
		File[] list=confDir.listFiles();
		int l=list.length;
		System.out.println(l);
		for(int i=0;i<list.length;i++){
			list[i].delete();
		}
		System.out.println("clean the index file");
		
		
		
	}
	
}
