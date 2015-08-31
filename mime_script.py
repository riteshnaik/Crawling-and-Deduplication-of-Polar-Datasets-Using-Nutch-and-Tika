import sys
import re

def main(argv):
    mime_dict = {}
    fail = {}
    not_include_list = '^notfound|.*null.*|.*exception.*|.*robots_denied.*|.*denied.*|.*mp4.*|.*mpeg.*'
    count = 1
    output = open(argv[1], 'w')
    input = open(argv[0], 'r', errors='ignore')
    input.readline()
        
    for line in input:
            list = line.split(',')
            if len(list) == 12 :
                url = line.split(',')[0]
                status = line.split(',')[2].strip()
                if status == '"db_fetched"':
                    mime_type = line.split(',')[-2].split('|')[0].split(':')[-1]
                    if mime_type not in mime_dict and not re.search(not_include_list, mime_type):
                        mime_dict[mime_type] = 1
                    if mime_type in mime_dict:
                         mime_dict[mime_type] = mime_dict[mime_type] + 1
                else:
                    failure_status = line.split(',')[-2].split(':')[-1]
                    output.write('URL: '+url+','+failure_status+'\n')
                    if re.match(".*robots_denied.*", failure_status):
                        if failure_status not in fail :
                            fail[failure_status] = {}
                            fail[failure_status]['status'] = status
                            fail[failure_status]['url'] = url
                            fail[failure_status]['Count'] = 1
                        if failure_status in fail :
                            fail[failure_status]['Count'] = fail[failure_status]['Count'] + 1
                    if re.match(".*exception.*", failure_status):
                        reason = line.split(',')[-1].split(':')[1].strip()
                        output.write('URL: '+url+','+reason+'\n')
                        if reason not in fail :
                            fail[reason] = {}
                            fail[reason]['status'] = status
                            fail[reason]['url'] = url
                            fail[reason]['Count'] = 1
                        if reason in fail :
                            fail[reason]['Count'] = fail[reason]['Count'] + 1
                
            if len(list) == 13 :
                status = line.split(',')[2].strip()
                if status == '"db_fetched"':
                    mime_type = line.split(',')[-3].split('|')[0].split(':')[-1]
                    if mime_type not in mime_dict and not re.search(not_include_list, mime_type):
                        mime_dict[mime_type] = 'true'
                    if mime_type in mime_dict:
                         mime_dict[mime_type] = mime_dict[mime_type] + 1
                else:
                    failure_status = line.split(',')[-3].split(':')[-1]
                    if re.match(".*robots_denied.*", failure_status):
                        output.write('URL: '+url+','+failure_status+'\n')
                        if failure_status not in fail :
                            fail[failure_status] = {}
                            fail[failure_status]['status'] = status
                            fail[failure_status]['url'] = url
                            fail[failure_status]['Count'] = 1
                        if failure_status in fail :
                            fail[failure_status]['Count'] = fail[failure_status]['Count'] + 1
                            
                    if re.match(".*exception.*", failure_status):
                        reason = line.split(',')[-2].split(':')[1].strip()
                        if reason != "success(1)" :
                            output.write('URL: '+url+','+reason+'\n')
                        if reason not in fail.keys() :
                            fail[reason] = {}
                            fail[reason]['status'] = status
                            fail[reason]['url'] = url
                            fail[reason]['Count'] = 1
                        if reason in fail :
                           fail[reason]['Count'] = fail[reason]['Count'] + 1
       
    print('Length: ' + str(len(mime_dict.keys())))
    for mimeType in mime_dict.keys():
        print(str(count) +': ' + mimeType + ' : '+str(mime_dict[mimeType]))
        count = count + 1
    count = 1
    for reason in fail.keys():
        print(str(count) +': ' + reason + ' : '+str(fail[reason]['Count']))
        count = count + 1
if __name__=="__main__":
    main(sys.argv[1:])
