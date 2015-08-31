CSCI 572: Assignment I Report
Crawling and Deduplication of Polar Datasets Using Nutch and Tika

Team Members:
Ritesh Naik, Ananya Acharya, Anuraj Shetty, Namrata Malarout and Shambhavi Punja

==============================================
LIST OF FILES (directory structure maintained)
==============================================
1. nutch/src/plugin/dedupnear/src/java/org/apache/nutch/parse/dedupnear/DedupNearParser.java
2. nutch/src/plugin/dedupnear/build.xml
3. nutch/src/plugin/dedupnear/ivy.xml
4. nutch/src/plugin/dedupnear/plugin.xml
5. nutch/src/plugin/dedupxact/src/java/org/apache/nutch/parse/dedupxact/DedupXactParser.java
6. nutch/src/plugin/dedupxact/build.xml
7. nutch/src/plugin/dedupxact/ivy.xml
8. nutch/src/plugin/dedupxact/plugin.xml
9. nutch/src/plugin/build.xml
10. nutch/build/plugins/dedupnear/tika-app-1.7.jar
11. nutch/build/plugins/dedupxact/tika-app-1.7.jar

12. nutch/runtime/local/conf/nutch-site.xml
13. nutch/runtime/local/conf/regex-urlfilter.txt
14. nutch.runtime/local/conf/nutch-default.xml

15. Deduplication.java
16. mime_script.py

DUPLICATE_URL_LIST
16. duplicates_amd_plain.txt
17. duplicates_amd_selenium.txt
18. duplicates_acadis_selenium.txt
19. duplicates_acadis_plain.txt

REPORT
20. crawl1_mimetypes.txt
21. crawl2_mimetypes.txt
22. unfetched_urls_crawl1.txt
23. unfetched_urls_crawl2

24. Acharya_Ananya_NUTCH.pdf
25. D3_cluster.png

26. CRAWL_DATA folder contains the dump of all the crawled data.

===========================
BUILD AND EXECUTION STEPS
===========================
A. MIME type generation script
------------------------------
This python script is used to generate unique MIME types, unique failures and all the failed URLs which were unfetched by nutch. This is extracted from the crawldb. Outputfile will contain 100+ urls.

$ python3 <path to dumpfile from crawldb> <outputfile>

---------------------
B. Deduplication.java
---------------------
This program finds exact duplicates using the content of each url from the dump file generated from segmentdb of nutch crawl. To compile and execute this program:
$ javac Deduplication.java
$ java Deduplication <path to dump file from segment> > <output_filename.txt>

*KINDLY NOTE: Since our crawl data did not have metadata of any URLs we could not test our java code to extract list of near duplicates from crawl data. However, we directly implemented this algorithm in our URLfilter plugin for identifying near duplicates, which uses the metadata of the URLs during nutch crawl (on the fly).

---------------------
C. URL filter plugins
---------------------
There are two URL filter plugins 
1. DedupNearParser - Identify and output near duplicates using the metadata of each url as and when it is crawled by nutch (on the fly).
2. DedupXactParser - Identify and output exact duplicates using the content of each url as and when it is crawled by nutch (on the fly).

Step 1: In nutch/src/plugin copy and paste the two folders: 'dedupnear' and 'dedupxact' which contains the plugin folder hierarchy along with the build.xml, ivy.xml, plugin.xml and the respective java file of the plugin.

Step 2: Copy and paste build.xml into nutch/src/plugin. This contain the property tp deploy plugin directory for ant.

Step 3. Create folder nutch/build/plugins/dedupnear/ and place tika-app-1.7.jar here.
Create folder nutch/build/plugins/dedupxact/ and place tika-app-1.7.jar here.

Step 4. run $ant runtime in the nutch directory

Step 5: Replace the following files in nutch/runtime/local/conf/ folder -
nutch-site.xml
regex-urlfilter.txt
nutch-default.xml

Step 6. Depending on the URL that is being crawled, update regex-filter.txt file by commenting and/or uncommenting the appropriate regex. (Add new regex if crawling a different URL).

Step 7. Execute nutch crawl in nutch/runtime/local:
$ bin/crawl <path to seed url> <path to crawl destination> <number of rounds/depth>

------------------------------------
D. SAMPLE OUTPUT OF URLFILTER PLUGIN
------------------------------------
Since we could not crawl the 3 URLs given in the assignment to get the result of our URLfilter plugin to perform deduplication, we crawled http://espncricinfo.com/ for a depth of 1 to test our deduplication urlfilter plugin. With just 1 round, our plugin was able to fetch both near duplicates (using metadata of the url) and exact duplicates (using content of the url) and our plugin distinctly identified duplicates that had DIFFERENT URLs but the same content/metadata and also duplicates that had the same url and content/metadata.

We could implement only identification of these duplicates and were not able to exclude/filter-out these URLs from nutch's fetchlist as our plugin would filter the URL before nutch got to process these urls into its segments.

The output of our test crawl is as follows:
---------------------------------------------------------
Exact_Duplicates_diffURL.txt
---------------------------------------------------------
http://stats.espncricinfo.com/ci/engine/records/index.html and http://www.espncricinfo.com/ci/engine/records/index.html are exact duplicates
http://www.espncricinfo.com/ci/content/zones/insights and http://www.espncricinfo.com/insights are exact duplicates
http://www.espncricinfo.com/ci/content/zones/insights and http://www.espncricinfo.com/ci/content/url/834953.html are exact duplicates
http://www.espncricinfo.com/ci/content/site/ontheroad/index.html and http://www.espncricinfo.com/ci/content/url/830565.html are exact duplicates

-----------------------------------------------------
Near_Duplicates_diffURL.txt
-----------------------------------------------------
http://stats.espncricinfo.com/ci/engine/records/index.html and http://www.espncricinfo.com/ci/engine/records/index.html are near duplicates
http://www.espncricinfo.com/ci/content/zones/insights and http://www.espncricinfo.com/insights are near duplicates
http://www.espncricinfo.com/ci/content/zones/insights and http://www.espncricinfo.com/ci/content/url/834953.html are near duplicates
http://www.espncricinfo.com/ci/content/site/ontheroad/index.html and http://www.espncricinfo.com/ci/content/url/830565.html are near duplicates

-------------------------------------------------------
Exact_Duplicates_sameURL.txt
-----------------------------------------------------
Exact_Deduplicator::http://www.espncricinfo.com/ and http://www.espncricinfo.com/ are exact duplicates
Exact_Deduplicator::http://www.espncricinfo.com/icc-cricket-world-cup-2015/content/series/509587.html and http://www.espncricinfo.com/icc-cricket-world-cup-2015/content/series/509587.html are exact duplicates
Exact_Deduplicator::http://www.espncricinfo.com/ci/content/match/fixtures_futures.html and http://www.espncricinfo.com/ci/content/match/fixtures_futures.html are exact duplicates
Exact_Deduplicator::http://www.espncricinfo.com/ci/content/story/news.html and http://www.espncricinfo.com/ci/content/story/news.html are exact duplicates
Exact_Deduplicator::http://www.espncricinfo.com/ci/content/video_audio/index.html and http://www.espncricinfo.com/ci/content/video_audio/index.html are exact duplicates
Exact_Deduplicator::http://www.espncricinfo.com/blogs/content/story/blogs/cordon.html and http://www.espncricinfo.com/blogs/content/story/blogs/cordon.html are exact duplicates
Exact_Deduplicator::http://www.espncricinfo.com/ci/content/image/index.html and http://www.espncricinfo.com/ci/content/image/index.html are exact duplicates
Exact_Deduplicator::http://www.espncricinfo.com/ci/content/stats/index.html and http://www.espncricinfo.com/ci/content/stats/index.html are exact duplicates
Exact_Deduplicator::http://www.espncricinfo.com/ci/engine/series/index.html and http://www.espncricinfo.com/ci/engine/series/index.html are exact duplicates
Exact_Deduplicator::http://shop.espncricinfo.com/ and http://shop.espncricinfo.com/ are exact duplicates
Exact_Deduplicator::http://stats.espncricinfo.com/ci/engine/records/index.html and http://stats.espncricinfo.com/ci/engine/records/index.html are exact duplicates

--------------------------------------------------------
Near_Duplicates_sameURL.txt
-------------------------------------------------------
http://www.espncricinfo.com/ and http://www.espncricinfo.com/ are near duplicates
http://www.espncricinfo.com/icc-cricket-world-cup-2015/content/series/509587.html and http://www.espncricinfo.com/icc-cricket-world-cup-2015/content/series/509587.html are near duplicates
http://www.espncricinfo.com/ci/content/match/fixtures_futures.html and http://www.espncricinfo.com/ci/content/match/fixtures_futures.html are near duplicates
http://www.espncricinfo.com/ci/content/story/news.html and http://www.espncricinfo.com/ci/content/story/news.html are near duplicates
http://www.espncricinfo.com/ci/content/video_audio/index.html and http://www.espncricinfo.com/ci/content/video_audio/index.html are near duplicates
http://www.espncricinfo.com/blogs/content/story/blogs/cordon.html and http://www.espncricinfo.com/blogs/content/story/blogs/cordon.html are near duplicates
http://www.espncricinfo.com/ci/content/image/index.html and http://www.espncricinfo.com/ci/content/image/index.html are near duplicates
http://www.espncricinfo.com/ci/content/stats/index.html and http://www.espncricinfo.com/ci/content/stats/index.html are near duplicates
http://www.espncricinfo.com/ci/engine/series/index.html and http://www.espncricinfo.com/ci/engine/series/index.html are near duplicates
http://shop.espncricinfo.com/ and http://shop.espncricinfo.com/ are near duplicates
http://stats.espncricinfo.com/ci/engine/records/index.html and http://stats.espncricinfo.com/ci/engine/records/index.html are near duplicates

---------------------
E. D3 extra credit
---------------------
We dumped the segments from our crawls using the following command:
bin/nutch dump -outputDir <Name of Output folder> -segment <Path to segment directory>

From the files dumped, we extracted the images and renamed them as their file names were too long. We created a new directory tika-image-similarity
containing the extracted data. Then we followed the instructions given in the documentation https://github.com/chrismattmann/tika-img-similarity to visualize in D3.