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
