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
