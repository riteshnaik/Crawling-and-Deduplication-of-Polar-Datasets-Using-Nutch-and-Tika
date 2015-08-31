package org.apache.nutch.parse.dedupxact;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.net.URLFilter;
import org.apache.nutch.plugin.Extension;
import org.apache.nutch.plugin.PluginRepository;
import org.apache.nutch.util.URLUtil;
import org.apache.nutch.util.domain.DomainSuffix;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.*;
import org.apache.tika.sax.XHTMLContentHandler;
import org.apache.tika.sax.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.tika.parser.html.HtmlParser;

import java.io.*;
import java.lang.System.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.ByteBuffer;
import java.util.*;

public class DedupXactParser implements URLFilter {

	private static final Logger LOG = LoggerFactory.getLogger(DedupXactParser.class);

	// read in attribute "file" of this plugin.
	private static String attributeFile = null;
	private Configuration conf;
	private String domainFile = null;
	private Set<String> domainSet = new LinkedHashSet<String>();

	/**
	* Default constructor.
	*/
	public DedupXactParser() {

	}

	private void readConfiguration(Reader configReader) throws IOException {

	    // read the configuration file, line by line
	    BufferedReader reader = new BufferedReader(configReader);
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	      if (StringUtils.isNotBlank(line) && !line.startsWith("#")) {
		// add non-blank lines and non-commented lines
		domainSet.add(StringUtils.lowerCase(line.trim()));
	      }
	    }
	}

	public void setConf(Configuration conf) {
	    this.conf = conf;

	    // get the extensions for domain urlfilter
	    String pluginName = "urlfilter-domain";
	    Extension[] extensions = PluginRepository.get(conf)
		.getExtensionPoint(URLFilter.class.getName()).getExtensions();
	    for (int i = 0; i < extensions.length; i++) {
	      Extension extension = extensions[i];
	      if (extension.getDescriptor().getPluginId().equals(pluginName)) {
		attributeFile = extension.getAttribute("file");
		break;
	      }
	    }

	    // handle blank non empty input
	    if (attributeFile != null && attributeFile.trim().equals("")) {
	      attributeFile = null;
	    }

	    if (attributeFile != null) {
	      if (LOG.isInfoEnabled()) {
		LOG.info("Attribute \"file\" is defined for plugin " + pluginName
		    + " as " + attributeFile);
	      }
	    } else {
	      if (LOG.isWarnEnabled()) {
		LOG.warn("Attribute \"file\" is not defined in plugin.xml for plugin "
		    + pluginName);
	      }
	    }

	    // domain file and attribute "file" take precedence if defined
	    String file = conf.get("urlfilter.domain.file");
	    String stringRules = conf.get("urlfilter.domain.rules");
	    if (domainFile != null) {
	      file = domainFile;
	    } else if (attributeFile != null) {
	      file = attributeFile;
	    }
	    Reader reader = null;
	    if (stringRules != null) { // takes precedence over files
	      reader = new StringReader(stringRules);
	    } else {
	      reader = conf.getConfResourceAsReader(file);
	    }
	    try {
	      if (reader == null) {
		reader = new FileReader(file);
	      }
	      readConfiguration(reader);
	    } catch (IOException e) {
	      LOG.error(org.apache.hadoop.util.StringUtils.stringifyException(e));
	    }
	}

	public Configuration getConf() {
	    return this.conf;
	}

	public static int hamming_distance(long x, long y){
		long val = x ^ y;
		int  dist = 0;
		while(val != 0){
			dist++;
	        val &= val - 1;
		}
		return dist;
	}

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

	public long SimHash64(String[] words) throws UnsupportedEncodingException{
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			int[] vector = new int[64];
			int offset = 0;
			String[] features = getFeatures(words);
			for(String feature: features){
				byte[] md5Hash = md.digest(feature.getBytes("utf-8"));
				for(int i=0;i<md5Hash.length-8;i++){
					//System.out.println(offset+i);
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
			// Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}

	public String filter(String url) {
		Map<Long,String> simhashMap = null; // map to store Simhash as keys and urls as value pairs.
		Map<String,Boolean> URLMap = null;
		int duplicatePresent = 0; // Flag to check if any duplicate was found.
		try{
			System.out.println("Exact-Deduplicator:: Processing: " + url);

			// De-Serialize hashmap object
			File hashFile = new File("Exact_Deduplicator_hashmap.ser");
			if (hashFile.isFile() && hashFile.canRead()) {
			// if simhash serialized object is present
				FileInputStream fis = new FileInputStream(hashFile);
				ObjectInputStream ois = new ObjectInputStream(fis);
         			simhashMap = (HashMap) ois.readObject(); // Load into simhashMap
         			ois.close();
         			fis.close();
			}else{
			// Else create new simhashMap
				simhashMap = new HashMap<Long,String>();			
			}
		
			File URLFile = new File("Exact_Deduplicator_urlmap.ser");
			if (URLFile.isFile() && URLFile.canRead()) {
			// if simhash serialized object is present
				FileInputStream fis = new FileInputStream(URLFile);
				ObjectInputStream ois = new ObjectInputStream(fis);
         			URLMap = (HashMap) ois.readObject(); // Load into simhashMap
         			ois.close();
         			fis.close();
			}else{
			// Else create new simhashMap
				URLMap = new HashMap<String,Boolean>();			
			}
			
			// Write all the exact duplicate found to file.
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("Exact_Duplicates_sameURL.txt", true)));
			PrintWriter diffURL = new PrintWriter(new BufferedWriter(new FileWriter("Exact_Duplicates_diffURL.txt", true)));
			PrintWriter debug = new PrintWriter(new BufferedWriter(new FileWriter("Exact_Duplicates_debug.txt", true))); // Debug file to store log
			
			// Create contenthandlers to obtain required content from the URL using tika
			URL u = new URL(url);
			InputStream input = u.openStream();
			LinkContentHandler linkHandler = new LinkContentHandler(); //for extracting links
			ContentHandler textHandler = new BodyContentHandler(-1); //strips out all the text on a page
			ToHTMLContentHandler toHTMLHandler = new ToHTMLContentHandler(); //for extracting specific blocks of text
			TeeContentHandler teeHandler = new TeeContentHandler(linkHandler, textHandler, toHTMLHandler); //passes data from the HtmlParser to ContentHandlers that it has been initialized with.
			
			// Parse data
			Metadata metadata = new Metadata();
			ParseContext parseContext = new ParseContext();
			HtmlParser parser = new HtmlParser();
			parser.parse(input, teeHandler, metadata, parseContext);

			debug.println("=========================Deduplicator:: Processing: " + url +"=========================");
			//debug.println("URL:: "+url);
			//debug.println("ParseText::\n" + textHandler.toString().replaceAll("\\s+"," "));
			
			// Performing Simhash
			String contentString = textHandler.toString().replaceAll("\\s+"," ");
			String[] doc = contentString.split(" ");
			Long simHash64 = SimHash64(doc); // SimHash64() is the function to get 64bit simhash value. Passing text content of the url.
			debug.println("SIMHASH:: "+simHash64);
			int dist = -1; // Distance is the hamming distance between the two simhash values being compared.

			// Check the current url's simhash value against all the simhashes present in the map so far.
			for (Map.Entry<Long,String> entry : simhashMap.entrySet()) {
				dist = hamming_distance(entry.getKey(),simHash64); // function to get hamming distance between simhashes
				if(dist == 0){
				// if exact duplicate is found.
					duplicatePresent = 1; //	
					if(simhashMap.get(entry.getKey()).equals(url))
					{ // if the urls and the content are the same
						if(!URLMap.containsKey(url))
						{
							URLMap.put(url,true);
				    			writer.println("Exact_Deduplicator::" + simhashMap.get(entry.getKey())+" and " + url + " are exact duplicates");
							System.out.println("SAME Exact_Deduplicator::" + simhashMap.get(entry.getKey())+" and " + url + " are exact duplicates");
						}
					}
					else	// if the urls are different and the content is the same.
					{
						diffURL.println(simhashMap.get(entry.getKey())+" and " + url + " are exact duplicates");
						System.out.println("DIFF Exact_Deduplicator::" + simhashMap.get(entry.getKey())+" and " + url + " are exact duplicates");
					}
					break;
				}
			}
			if(duplicatePresent == 0){
			// If current url's content is not a duplicate of any urls crawled so far, then add its key value pair to simhashMap
				simhashMap.put(simHash64,url);		
			}
			// Close files
			diffURL.close();
			writer.close();
			debug.close();
		}catch(Exception ex){
		 	System.out.println("Exception: " + ex.getMessage());
			return url;
		}finally{
			try{
				// Serialize the hashMap object for next iteration of the crawl.
				FileOutputStream fos = new FileOutputStream("Exact_Deduplicator_hashmap.ser");
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(simhashMap);
				oos.close();
				fos.close();
	
				// Serialize the hashMap object for next iteration of the crawl.
				fos = new FileOutputStream("Exact_Deduplicator_urlmap.ser");
				oos = new ObjectOutputStream(fos);
				oos.writeObject(URLMap);
				oos.close();
				fos.close();
				//System.out.println("Finished processing: " + url);
					
			}catch(IOException ioe){
				//ioe.printStackTrace();
				//System.out.println("----------------------------------CAUGHT EXCEPTION FINALLY------------------------------------------");
				return url;
			}	
		}
		return url;
	}

}
