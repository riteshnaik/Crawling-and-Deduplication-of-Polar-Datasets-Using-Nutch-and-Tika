import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Deduplication{
	
	/*Calculates Simhash value of the document*/
	public static long SimHash64(String[] words) throws UnsupportedEncodingException{
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			int[] vector = new int[64];
			int offset = 0;
			String[] features = getFeatures(words);//Constructs Tri-gram feature vector from the content
			for(String feature: features){
				byte[] md5Hash = md.digest(feature.getBytes("utf-8"));//Gets 128 bit MD5 Hash value of each feature in the feature vector
				/*Converts 128 bit MD5 to simhash*/
				for(int i=0;i<md5Hash.length-8;i++){
					for(int j=0;j<8;j++){
						if((md5Hash[offset+i] >> (7-j) & 0x01) == 1){
							vector[i * 8 + j] = vector[i * 8 + j] + 1;
						}else{
							vector[i * 8 + j] = vector[i * 8 + j] - 1;
						}
					}
					offset = offset + 1;
				}
				offset = 0;
			}
			
			byte[] simHashBytes = new byte[8];
			
			for(int i=0;i < simHashBytes.length;i++){
				for(int j=0;j<8;j++){
					if(vector[i * 8 + j] > 0){
						simHashBytes[i] |= 1 << (7-j);
					}
				}
			}
			
			long simHash = ByteBuffer.wrap(simHashBytes).getLong();
			return simHash;
			
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
	/*Calculates hamming distance between two simhash values*/
	public static int hamming_distance(long x, long y){
		long val = x ^ y;
		int  dist = 0;
		while(val != 0){
			dist++;
	        val &= val - 1;
		}
		return dist;
	}
	
	/*Converts document into Tri-gram features(Shingling)*/
	public static String[] getFeatures(String[] doc){
		List<String> list =new ArrayList<String>();
		if(doc.length == 0 || doc == null){
			return null;
		}
		if(doc.length == 1){
			list.add("FIRST_"+"SECOND_"+"_"+doc[0]);
		}
		
		if(doc.length == 2){
			list.add("FIRST_"+doc[0]+"_"+doc[1]);
		}
		for(int i=0;i<doc.length;i++){
			if(i + 2 < doc.length){
				list.add(doc[i]+"_"+doc[i+1]+"_"+doc[i+2]);
			}
		}
		return list.toArray(new String[list.size()]);
	}
	
	public static void main(String[] args){
		try {
			/*Maintains hashmap of URL and simhash of URL's content*/
			Map<Long,String> unsortMap = new HashMap<Long,String>();
			String line = null;
        	String URL = "";
			FileReader fileReader = new FileReader(args[0]);//Reads crawled data
			
	        BufferedReader bufferedReader = new BufferedReader(fileReader);

	            while((line = bufferedReader.readLine()) != null) {
	            	String anchor = line.split(" ")[0];
	            	if(anchor.equals("URL::")){
	            		URL = line;
	            	}
	            	if(line.equals("ParseText::")){
	            		String[] words = bufferedReader.readLine().split(" ");
	            		Long simHash64 = SimHash64(words);
	  
	            		int dist = -1;
	            		int flag = 0;
	            		for (Map.Entry<Long,String> entry : unsortMap.entrySet()) {
	        				dist = hamming_distance(entry.getKey(),simHash64);
	        				if(dist == 0){
	        		            		System.out.println("Distance between " + unsortMap.get(entry.getKey())+" and " + URL + " is " + dist);
	        					flag = 1;
	        					break;
	        				}
	        			}
	        			if(flag == 0){
	        				unsortMap.put(simHash64,URL);		
	        			}
	            		
	            	}
	            }
	            bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}