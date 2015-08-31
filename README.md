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
        ● https://www.aoncadis.org/project/sustaining_and_amplifying_the_itex_aon_through_automation_and_increased_interdisciplinarity_of_observations.html
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
