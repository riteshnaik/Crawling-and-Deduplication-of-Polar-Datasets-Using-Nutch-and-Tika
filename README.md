# Crawling-and-Deduplication-of-Polar-Datasets-Using-Nutch-and-Tika

#Politeness Configurations:
To deal with politeness, properties were added to the file nutch-site.xml. We labeled our
nutch bot (http.agent.name) as CS572 RASNA, the agent description
(http.agent.description) was set to a string saying that we are crawling as a part of an
assignment and our email-id’s were mentioned under agent email (http.agent.email). The
value for number of requests per second (fetcher.thread.per.host) was kept as default.

#URL Filter Configurations:
The file regex-urlfilter.txt was edited to enable url filtering. The following regular expressions
were added to the file ( instead of +.):
+^http://([a-z0-9]*\.)*gcmd.gsfc.nasa.gov/
+^http://([a-z0-9]*\.)*nsidc.org/acadis/
+^https://([a-z0-9]*\.)*aoncadis.org/
Also for crawling AMD we commented the regular expression that was helping to skip
urls with certain characters . (-[?*!@=])

#First Crawl of AMD, ADE and ACADIS
MIME types can be found in the attached file. (crawl1_mimetypes.txt)
Most of them were web pages (txt/html). We didn’t fetch any science data files such as HDF,
NetCDF and Grib files. This was because these files were present behind forms making
them inaccessible to nutch.

##Crawl Statistics:
    CrawlDb statistics start: crawldata/crawldb
    Statistics for CrawlDb: crawldata/crawldb
    TOTAL urls: 223331
    retry 0: 222950
    retry 1: 380
    retry 2: 1
    min score: 0.0
    avg score: 3.4562038E-4
    max score: 1.082
    status 1 (db_unfetched): 194007
    status 2 (db_fetched): 23519
    status 3 (db_gone): 608
    status 4 (db_redir_temp): 2785
    status 5 (db_redir_perm): 133
    status 7 (db_duplicate): 2279
    CrawlDb statistics: done

#Second Crawl of AMD, ADE and ACADIS with enhanced Tika and Nutch Selenium
MIME types can be found in the attached file. (crawl2_mimetypes.txt). We added the
protocol-httpclient plugin in the property plugin.includes in nutch-site.xml.

##Did the Selenium plugin help?
The selenium plugin is used to manipulate AJAX enabled web pages during repetitive web
crawls. The Selenium plugin parses the JavaScript, and passes the content to the
consecutive rounds in the Nutch crawl. This helps as the AJAX/JavaScript dependent
content is being stored in the database

##Did you get more data after installing the Tika updates and recrawling?
By the time we had reached the second crawl the ADE site was already down so we could
only crawl AMD and ACADIS.
We observed that the first crawl of ACADIS we came across total of 4982 URLs and fetched
1976 URLs. In the crawl with enhanced Tika and Selenium we came across 8927 URLs and
fetched 3838 URLs. So we got more data than the previous crawl for the ACADIS website.
Whereas the data we got from the AMD site went down in the second crawl. This is was
because many webpages showed “404-You have selected a page within the GCMD web
site which does not exist.” The fetch for these pages failed with a HTTP code of 503.
Because of this the number of URLs in the queue in the consecutive rounds decreased
significantly.

##Do you think you achieved good coverage of the 3 repositories?
Given the time and circumstances we managed to crawl a good amount of data from the
given sites.

##Are the unfetched URLs from crawl1 still present? Did the enhanced Tika parsing assist with that?
The URLs in the first crawl are still present in the second crawl. The enhanced Tika parsing helped us find some unfetched URLs from the first crawl which were fetched in the second.
    
    For e.g.
        ● https://www.aoncadis.org/scienceKeywordTopic/Human%20Dimensions.html
          1st crawl status: not found(14)
        ● https://www.aoncadis.org/project/sustaining_and_amplifying_the_itex_aon_through_
          automation_and_increased_interdisciplinarity_of_observations.html
          1st crawl status: exception 16
        ● https://www.aoncadis.org/dataset/Zamora2011.dif
          1st crawl status: tempmoved
    The above URLs were fetched in the second crawl.
        ● https://www.aoncadis.org/ac/guest/secure/registration.html
          1st crawl status: denied
          2nd crawl status: db gone
          
##Crawl Statistics:
    CrawlDb statistics start: crawldata/crawldb
    Statistics for CrawlDb: crawldata/crawldb
    TOTAL urls: 74948
    retry 0: 73934
    retry 1: 668
    retry 2:346
    min score: 0.0
    avg score: 1.5776574E-4
    max score: 1.071
    status 1 (db_unfetched): 62987
    status 2 (db_fetched): 8267
    status 3 (db_gone): 241
    status 4 (db_redir_temp): 3424
    status 5 (db_redir_perm): 2
    status 7 (db_duplicate): 27
    CrawlDb statistics: done

#Algorithm for detecting exact duplicates
The algorithm below is used for the data that data we parsed from the crawl.

First we constructed tri-gram shingling feature vector from the parsed text of each document. Each element of the feature vector was then hashed using MD-5l, which are 128 bits. We used random chunks of 64-bits from the 128 bits as the signature of the document. We used the SimHash algorithm to detect the duplicates.

    The SimHash algorithm is:
        1. Define a fingerprint size = 64 bits
        2. Create an array V[] filled with this size of zeros
        3. For each element in the dataset, we create a unique hash with md5. Then choose random chunk 
           of 64-bits as the signature of the document.
        4. For each hash, for each bit i in this hash:
            ○ If the bit is 0, we add 1 to V[i]
            ○ If the bit is 1, we take 1 from V[i]
        5. For each i
            ○ If V[i] > 0, i = 1
            ○ If V[i] < 0, i = 0
            It gives us a fingerprint characterizing our text, an approximation of the text data. This 
            fingerprint is a binary number, for instance: 10101011100010001010000101111100.
        6. Then we sort the documents based on their SimHash values.
        7. Now, to find the similarity between two adjacent fingerprints, say A and B we perform a XOR
           operation (find the hamming distance).
           simhash(A)∩simhash(B)
           we only have to use a XOR operation:
           10101011100010001010000101111100 XOR 10101011100010011110000101111110 
           = 00000000000000010100000000000010
           Here, the 1 in the XOR result are the differences between the two fingerprints.To get an idea
           of the difference between the original texts, we count the number of 1 in the XOR result and 
           divide it by the total size of the fingerprint. This is our similarity index.
           
#Algorithm for detecting near duplicates
We used extracted metadata as a feature vector to detect near duplicates. Each element of
the feature vector was used in calculating simhash using the above mentioned simhash
algorithm and stored in a HashMap. Now the fingerprint is compared to the previously stored
URLs. Near duplicates are identified with hamming distance 0.

#URL Filter Plugin
As every URL is crawled, its fingerprint is calculated using the above mentioned simhash
algorithm and stored in a HashMap. Now the fingerprint is compared to the previously stored
URLs. If it’s a match with hamming distance 0 then Url filter writes it to the file as near and
exact duplicates respectively.

We have two filters, one for exact (uses content) and one for near duplicates (uses metadata).
Each filter generates two files-
● List of duplicate URLs with same URL and content.
- Exact_Duplicates_sameURL.txt and Near_Duplicates_sameURL.txt
● List of duplicate URLs with different URL but having the exact same content.
- Exact_Duplicates_diffURL.txt and Near_Duplicates_diffURL.txt

##How we arrived at the above algorithm:
We went over the slides, papers and did some research on the web. We came across the concept of SimHash and incorporated it.

##What worked about it? What didn’t?
The URL filter is able to successfully identify duplicates. But when we try to drop the URL if it is a duplicate, it isn’t passed to Nutch to write to the segment for the next round of the crawl.

##Number of exact duplicate URL’s detected:
● 3445 duplicate urls from the first crawl data.
● 596 duplicate urls from the second (with selenium) crawl data.

##Number of near duplicate URL’s detected:
Our near deduplication plugin identifies near duplicate data on the fly while nutch is crawling using the metadata of the URLs. However the crawled data of the three assigned URLs did not contain sufficient metadata to perform this deduplication. Hence to test our plugin we crawled espncricinfo site with 1 depth. Sample of near duplicates found is mentioned in the
ReadME provided.

#Experience with Apache Nutch and Apache Tika
The installation of all the softwares was a challenge. There was no documentation about dependencies of web browsers with selenium. It works only with versions 29 or lower. Allocating a display using xvfb didn’t work on machines enabled with jlx. Using Tika with Nutch was very useful for content and metadata extraction. The functions available from Tika helped us extract the right information and design our plugins. There were a lot of plugins available with Nutch to customize our crawl.

#D3 Visualization
(The screenshot of the clustered data can be found in the submission folder.)
We have 170 image files from our crawl but to get a better understanding of the clustering we decided to use 74 image files.
We found 5 clusters. In cluster 1, there are two images which have a similar background. In cluster 2 and cluster 4, we come across all the images that are used by the website to represent the various fields of environmental studies. In cluster 3, we find the logo and header images which are used as standard graphics by the website for every webpage.We observed that the duplicate images are clustered together. Although most of the clustering makes sense , there were a few images that didn’t seem to belong to the clusters they were represented in.An interesting observation about the d3 graph is that clusters 2 and 4 are conceptually the same but are still represented separately.
