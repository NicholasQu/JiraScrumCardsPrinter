#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# import xlrd
import sys,csv
# import codecs
from datetime import date,datetime
from pyh import *
page = PyH('JIRA看板')

#默认只打印3列
PRT_COL = 3
NEW_LINE = ''
SPRINT_DATE = '0901'

class Utils():
    def __init__(self):
        self.p = 0;
    def add(self,param):
        self.p += param
        return self.p

utils = Utils()

# def readExcel():
#     '''Read'''
#     filePath = r"/Users/Nicholas/Documents/人员通讯信息——曲健2017.8.1.xlsx"
#     # 打开文件
#     wb = xlrd.open_workbook(filePath)

#     # 打开第一个sheet
#     sheet0 = wb.sheets()[0]
#     v = sheet0.cell(1,1).value

#     print(v)


def ReadFile(filePath,encoding):  
    with codecs.open(filePath,"r",encoding) as f:  
        return f.read()


def WriteFile(filePath,u,encoding):  
    with codecs.open(filePath,"w",encoding) as f:  
        f.write(u)


''''' 
定义GBK_2_UTF8方法，用于转换文件存储编码 
'''  
def GBK_2_UTF8(src,dst):  
    content = ReadFile(src,encoding='gbk')  
    WriteFile(dst,content,encoding='utf_8') 


def readCsv():
    srcPath = "/Users/Nicholas/Desktop/JIRA-ETS源数据2017.8.23.csv"
    dstPath = "/Users/Nicholas/Desktop/JIRA-ETS源数据2017.8.23-UT8.csv"
    # GBK_2_UTF8(srcPath,dstPath);
    
    # 预处理csv
    # 1. 按照类型（story,任务,子任务）,parentid排序
    csvFile = open(srcPath, encoding='GBK',mode='r')
    csvData = csv.reader(csvFile, delimiter=',')
    # sort by team, issuetype, parentid
    sortedlist = sorted(csvData, key = lambda x: (x[53],x[4], x[3]))

    csvFile.close()

    # with open(dstPath, "w", newline = '') as f:
    #     fileWriter = csv.writer(f, delimiter=',')
    #     for row in sortedlist:
    #         if (row[0]=='主题') or ('ETS 0901 Sprint' not in row):
    #             continue
    #         fileWriter.writerow(row)
    # f.close()

    # 渲染出html页面和table的引用
    outerTab = renderHTMLTab()
    trLine  = 0

    i = 0
    for row in sortedlist:
        if (row[0]=='主题') or ('ETS 0901 Sprint' not in row):
            continue
        
        issuename   = row[0]
        issuekey    = row[1]
        issueid     = row[2]
        parentid    = row[3]
        issuetype   = row[4]
        owner       = row[14]
        estimate    = row[35]
        team        = row[53]
        epiclink    = row[57]

        i += 1

        if (i % PRT_COL == 1):
            trLine = outerTab << tr()
        
        parentRow = 0
        if (issuetype == 'Story') or (issuetype == r'任务'):
            parentRow = findNameByKey(sortedlist, epiclink)
        else:
            parentRow = findNameById(sortedlist, parentid)

        if parentRow is None: 
            parentRow = ['','','','','','']
        
        renderCard(trLine, team, issuetype, issuekey, issuename, \
                    parentRow[4], parentRow[1], parentRow[0], owner, estimate )

        # if (issuetype == 'Story') or (issuetype == r'任务'):
        #     renderStory(trLine,issuename, epiclink)
        # elif (issuetype == r'子任务'):
        #     renderSubTask(trLine, parentid, issuename, owner, estimate)
    
    page.printOut('abc.html')  


def findNameById(list, id):
    for row in list:
        if (row[2] == id):
            return row


def findNameByKey(list, key):
    for row in list:
        if (row[1] == key):
            return row


def renderHTMLTab():
    # 添加头信息
    page.head.addObj(charset)
    page.head.addObj("<style>body { font-family: Arial, Helvetica, sans-serif; font-size:14px; }" \
        + nl \
        # + "table tr td { border:1px solid #0094ff; }</style>")
        + ".bordercls {border:2px solid #212121;} </style>")


    # 添加table
    # 添加table
    outerTab = page << table(width="1300px", style="border-spacing:2px;")
    outerTab << tr() << td(width="300px") + td(width="300px") + td(width="300px") + td(width="300px")
    
    return outerTab
    # page.printOut('a.html')
    # page.printOut()
    

def renderCard(outerTab, team, issuetype, issuekey, issuename, parenttype, parentkey, parentname, owner, estimate):

    global NEW_LINE

    if (utils.add(1) % PRT_COL == 1):
        NEW_LINE = outerTab << tr()
    
    #story
    condition = 0   
    tagbgcolor = "#88ca79"

    if (issuetype == r'任务'):
        condition = 1
        tagbgcolor ="#d3b1e2"
    elif (issuetype == r'子任务'):
        condition = 2
        tagbgcolor = "#e6a988"
    
    cardTab = NEW_LINE << td(width="300px") << table(width="100%", bgcolor=tagbgcolor) 
    cardTab << tr() << td(width="70%") + td(width="30%")
    #标题是故事名称
    cardTab << tr() << td(colspan="2", height="50px", align="center", cl="bordercls") << b(showDesc(team, parenttype,parentkey,parentname))

    if condition == 0 :
        cardTab << tr() \
                  << td(rowspan="3", colspan="2", height="150px", align="left", valign="middle", cl="bordercls") \
                    << span(showDesc('', issuetype, issuekey, issuename))

    elif condition == 1 or condition == 2:
        ttr = cardTab << tr() 
        ttr << td(rowspan="3", height="150px", cl="bordercls") << span(showDesc('', issuetype, issuekey, issuename))
        ttr << td(height="50px", cl="bordercls") << span(owner)
        cardTab <<  tr() << td(rowspan="2", cl="bordercls") << span(estimate)

# 拼装标题或者卡片描述
def showDesc(team, issuetype, issuekey, issuename):
    if (issuetype is None) or (issuetype == ''):
        return '-~~-'
    elif (team != ''):
        return team +" 团队<br/>[" + issuetype + "][" + issuekey + "]: " + issuename
    else:
        return "[" + issuetype + "][" + issuekey + "]: " + issuename
    
from jira import JIRA
from jira.resources import *
from jira.resources import Board
from jira.resources import GreenHopperResource
from jira.resources import Sprint

PRJ_ETS = "ETS"
# http://99.48.46.160:8080/rest/greenhopper/1.0/rapidviews/viewsData
JIRA_BOARD_URL="rapidviews/viewsData"
# http://99.48.46.160:8080/rest/greenhopper/1.0/xboard/plan/backlog/data.json?rapidViewId=97&selectedProjectKey=ETS
JIRA_BACKLOG_URL="xboard/plan/backlog/data.json?rapidViewId={boardId}&selectedProjectKey={projectName}"
JIRA_SUBTASK_URL="xboard/issue/details.json?rapidViewId={boardId}&issueIdOrKey={backlogKey}&loadSubtasks=true"

def jira():
    
    # 渲染出html页面和table的引用
    outerTab = renderHTMLTab()

    myoption1 = {
        "server": "http://99.48.46.160:8080",
        "context_path": "/",
        "rest_path": "api",
        "rest_api_version": "2",
        "agile_rest_path": GreenHopperResource.GREENHOPPER_REST_PATH, #默认的option是GreenHoper
        "agile_rest_api_version": "1.0",
        "verify": True,
        "resilient": True,
        "async": False,
        "client_cert": None,
        "check_update": False,
        "headers": {
            'Cache-Control': 'no-cache',
            'Accept': 'application/json;charset=UTF-8',  # default for REST
            'Content-Type': 'application/json',  # ;charset=UTF-8',
            'X-Atlassian-Token': 'no-check'}}
    jira_greenhopper = JIRA(options=myoption1, basic_auth=('nicholas.qu','****'))

    # myoption2 = {
    #     "server": "http://99.48.46.160:8080",
    #     "context_path": "/",
    #     "rest_path": "api",
    #     "rest_api_version": "2",
    #     "agile_rest_path": 'agile', #GreenHopperResource.AGILE_BASE_REST_PATH 默认的option是GreenHoper
    #     "agile_rest_api_version": "1.0",
    #     "verify": True,
    #     "resilient": True,
    #     "async": False,
    #     "client_cert": None,
    #     "check_update": False,
    #     "headers": {
    #         'Cache-Control': 'no-cache',
    #         'Accept': 'application/json;charset=UTF-8',  # default for REST
    #         'Content-Type': 'application/json',  # ;charset=UTF-8',
    #         'X-Atlassian-Token': 'no-check'}}
    # greenhopper不支持param的条件查询，只能自己过滤
    # jira_api2 = JIRA(options=myoption2, basic_auth=('nicholas.qu','****'))
    
    boards_json = jira_greenhopper._get_json(JIRA_BOARD_URL, base=GreenHopperResource.AGILE_BASE_URL)

    filtered_boards = []
    for board in boards_json['views']:
        if (board['name'].startswith("ETS-")):
            filtered_boards.append(board)

    for board in filtered_boards:
        
        board_id = board['id']
        board_name = board['name']

        backlogs_url = JIRA_BACKLOG_URL.replace("{projectName}",PRJ_ETS).replace("{boardId}", str(board_id))
        backlogs_json = jira_greenhopper._get_json(backlogs_url, base=GreenHopperResource.AGILE_BASE_URL)
        
        # 打印团队
        team = board_name[4:7].replace("团",'').replace("队",'')

        #只取当前sprint的backlog
        backlogids = []
        sprintname = ''
        for sprint in backlogs_json['sprints']:
            if (SPRINT_DATE in sprint['name']):
                backlogids = sprint['issuesIds']
                sprintname = sprint['name']
                break

        print(r'当前正在处理:' + team + '......\n    Sprint['+ sprintname + \
                    '] 共有 ' + str(len(backlogids)) + ' 个backlog' )

        for backlog in backlogs_json['issues']:
            backlogid = backlog['id']
            # 判断是否在sprint里
            if (backlogid not in backlogids):
                continue

            backlogtype   = backlog['typeName']
            backlogkey    = backlog['key']
            backlogname   = backlog['summary']
            
            parenttype  = 'Epic'
            try:
                parentkey = backlog['epicField']['epicKey']
            except Exception as e:
                parentkey = ''
            try:
                parentname = backlog['epicField']['text']
            except Exception as e:
                parentname = ''

            try:
                owner = backlog['assigneeName']
            except Exception as e:
                owner = ''

            estimate = ''

            # 打印故事或者任务
            renderCard(outerTab, team, backlogtype, backlogkey, backlogname, \
                    parenttype, parentkey, parentname, owner, estimate )

            subtasks_url = JIRA_SUBTASK_URL.replace("{boardId}", str(board_id)).replace("{backlogKey}", backlogkey)
            subtasks_json = jira_greenhopper._get_json(subtasks_url, base=GreenHopperResource.AGILE_BASE_URL)

            backlog_detail_tabs = subtasks_json['tabs']['defaultTabs']
            subtask_tab = backlog_detail_tabs[0]
            for tab in backlog_detail_tabs:
                if (tab['tabId'] == 'SUB_TASKS'):
                    subtask_tab = tab
                    break
            
            for subtask in tab['subtaskEntries']:
                issuetype   = '子任务'
                issuekey    = subtask['key']
                issuename   = subtask['summary']
                
                try:
                    owner = subtask['assignee']['name']
                except Exception as e:
                    owner = ''

                estimate = ''

                # 打印子任务
                renderCard(outerTab, team, issuetype, issuekey, issuename, \
                        backlogtype, backlogkey, backlogname, owner, estimate )
                
    page.printOut('abc.html') 

    # myboards = jira_greenhopper.boards(name="ETS-")
    # mysprint  = ''
    # for myboard in myboards:
    #     print(str(myboard.id) + ' ' + myboard.name)
    #     mysprints = jira.sprints(myboard.id)
        
    #     for loopsprint in mysprints:
    #         if (SPRINT_DATE in loopsprint.name):
    #             mysprint = loopsprint
    #             break;
    # return

    # if mysprint == '':
    #     print(r'' + myboard.name + r' has not['+ SPRINT_DATE + '] sprint!')
    #     return

    # print('正在处理Sprint...' + mysprint.name + " [id=" + str(mysprint.id) + "]")
         
    # Issue Fields: https://developer.atlassian.com/jiradev/jira-apis/jira-rest-apis/jira-rest-api-tutorials/jira-rest-api-version-2-tutorial
    # Agile Api: https://docs.atlassian.com/jira-software/REST/cloud/
    #            https://docs.atlassian.com/jira-software/REST/
    # Rest Api: https://docs.atlassian.com/jira/REST/6.4.6/?&_ga=2.163402428.121064973.1503830222-399981431.1503830222#api/2/search-search
    # JQL: https://confluence.atlassian.com/jira/advanced-searching-179442050.html
    # fields: https://confluence.atlassian.com/jira/advanced-searching-179442050.html#AdvancedSearching-fields
    # r_json = jira._get_json("search/?jql=cf[10601]="+ r"开发运营" + " AND sprint=" + str(mysprint.id) + " order by cf[10601], issuetype ")
    # r_json = jira._get_json("search/?jql=sprint=" + str(mysprint.id) + " order by cf[10601], issuetype ")

    # curr_team = ''
    # for myissue in r_json['issues']:
        
    #     issuename   = xstr(myissue['fields']['summary'])
    #     issuekey    = xstr(myissue['key'])
    #     issueid     = xstr(myissue['id'])
    #     issuetype   = xstr(myissue['fields']['issuetype']['name'])
        
    #     try:
    #         parentid = xstr(myissue['fields']['parent']['id'])
    #     except Exception as e:
    #         parentid = ''
            
    #     try:
    #         owner   = xstr(myissue['fields']['assignee']['displayName'])
    #     except Exception as e:
    #         owner = ''

    #     try: 
    #         estimate = xstr(myissue['fields']['timeestimate'])
    #     except Exception as e:
    #         estimate = ''

    #     try:
    #         team = xstr(myissue['fields']['customfield_10601'][0]['value'])
    #     except Exception as e:
    #         team = ''

    #     try:
    #         epiclink = xstr(myissue['fields']['summary'])
    #     except Exception as e:
    #         epiclink = ''

    #     if (curr_team != team):
    #         print(r'当前正在处理:' + team + '......')
    #         curr_team = team

    #     print('issuename:'  + issuename)
    #     print('issuekey:'   + issuekey)
    #     print('issueid:'    + issueid)
    #     print('parentid:'   + parentid)
    #     print('issuetype:'  + issuetype)
    #     print('owner:'      + owner)
    #     print('estimate:'   + estimate)
    #     print('team:'       + team)
    #     print('epiclink:'   + epiclink)
    
        # i += 1

        # if (i % PRT_COL == 1):
        #     trLine = outerTab << tr()
        
        # parentRow = 0
        # if (issuetype == 'Story') or (issuetype == r'任务'):
        #     parentRow = findNameByKey(sortedlist, epiclink)
        # else:
        #     parentRow = findNameById(sortedlist, parentid)

        # if parentRow is None: 
        #     parentRow = ['','','','','','']
        

def xstr(s):
    return '' if s is None else str(s)        



if __name__ == '__main__':
    jira()