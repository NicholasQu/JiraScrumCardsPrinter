#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os, sys
from pyh import *
page = PyH('JIRA看板')

#默认只打印3列
PRT_COL = 2
NEW_LINE = ''

BOARD_PREFIX = ''  #看板前缀
try:
    BOARD_PREFIX = sys.argv[1]
except Exception as e:
    BOARD_PREFIX = 'ETS-'

PRJ_NAME = ''  #过滤工程名字
try:
    PRJ_NAME = sys.argv[2]
except Exception as e:
    PRJ_NAME = 'ETS'

SPRINT_DATE = ''
try:
    SPRINT_DATE = sys.argv[3]
except Exception as e:
    SPRINT_DATE = '0901'

class Utils():
    def __init__(self):
        self.p = 0;
    def add(self,param):
        self.p += param
        return self.p

utils = Utils()


def renderHTMLTab():
    # 添加头信息
    page.head.addObj(charset)
    page.head.addObj("<style>body { font-family: Arial, Helvetica, sans-serif; font-size:16px; }" \
        + nl \
        + " table { page-break-inside:auto } " \
        + " tr    { page-break-inside:avoid; page-break-after:auto } " \
        + " td    { page-break-inside:avoid; page-break-after:auto } " \
        + " thead { display:table-header-group } " \
        + " tfoot { display:table-header-group } " \
        + ".bordercls {border:2px solid #212121;} </style>")

    # 添加table
    # 添加table
    outerTab = page << table(width="900px", style="border-spacing:2px;")
    outerTab << tr() << td(width="500px") + td(width="500px")
    
    return outerTab

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
    
    cardTab = NEW_LINE << td(width="500px") << table(width="100%", bgcolor=tagbgcolor) 
    cardTab << tr() << td(width="70%") + td(width="30%")
    #标题是故事名称
    cardTab << tr() << td(colspan="2", height="80px", align="center", cl="bordercls") << b(showDesc(team, parenttype,parentkey,parentname))

    if condition == 0 :
        cardTab << tr() \
                  << td(rowspan="3", colspan="2", height="200px", align="left", valign="middle", cl="bordercls") \
                    << span(showDesc('', issuetype, issuekey, issuename))

    elif condition == 1 or condition == 2:
        ttr = cardTab << tr() 
        ttr << td(rowspan="3", height="200px", cl="bordercls") << span(showDesc('', issuetype, issuekey, issuename))
        ttr << td(height="80px", cl="bordercls") << span(owner)
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

# http://99.48.46.160:8080/rest/greenhopper/1.0/
JIRA_BOARD_URL="rapidviews/viewsData"
JIRA_BACKLOG_URL="xboard/plan/backlog/data.json?rapidViewId={boardId}&selectedProjectKey={projectName}"
JIRA_SUBTASK_URL="xboard/issue/details.json?rapidViewId={boardId}&issueIdOrKey={backlogKey}&loadSubtasks=true"
# http://99.48.46.160:8080/rest/api/2/
JIRA_ISSUE_DETAIL= "issue/{issuekey}"

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
    jira_greenhopper = JIRA(options=myoption1, basic_auth=('nicholas.qu','******'))

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
    # jira_api2 = JIRA(options=myoption2, basic_auth=('nicholas.qu','******'))
    


    ############## TEST CODE HERE ####################
    # issue_url = JIRA_ISSUE_DETAIL.replace("{issuekey}", 'ETS-294')
    # issue_json = jira_greenhopper._get_json(issue_url, base=JIRA.JIRA_BASE_URL)
    # print(issue_url)
    # print(issue_json)
    # try:
    #     estimate = issue_json['fields']['timetracking']['originalEstimate']
    # except Exception as e:
    #     estimate = ''

    # print(estimate)
    # return
    #############################################
    boards_json = jira_greenhopper._get_json(JIRA_BOARD_URL, base=GreenHopperResource.AGILE_BASE_URL)

    filtered_boards = []
    for board in boards_json['views']:
        if (board['name'].startswith(BOARD_PREFIX)):
            filtered_boards.append(board)

    for board in filtered_boards:
        
        board_id = board['id']
        board_name = board['name']

        backlogs_url = JIRA_BACKLOG_URL.replace("{projectName}",PRJ_NAME).replace("{boardId}", str(board_id))
        backlogs_json = jira_greenhopper._get_json(backlogs_url, base=GreenHopperResource.AGILE_BASE_URL)
        
        # 打印团队
        team = PRJ_NAME
        if (PRJ_NAME.startswith('ETS')):
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

            # 获取 issue 详情
            backlog_issue_json = jira_greenhopper.issue(backlogkey)
            # backlog_issue_url = JIRA_ISSUE_DETAIL.replace("{issuekey}", backlogkey)
            # backlog_issue_json = jira_greenhopper._get_json(backlog_issue_url, base=JIRA.JIRA_BASE_URL)
            
            estimate = ''
            try:
                estimate = backlog_issue_json.fields.timetracking.originalEstimate
                # estimate = backlog_issue_json['fields']['timetracking']['originalEstimate']
            except Exception as e:
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

                # 获取 issue 详情
                sub_issue_json = jira_greenhopper.issue(issuekey)
                # sub_issue_url = JIRA_ISSUE_DETAIL.replace("{issuekey}", issuekey)
                # sub_issue_json = jira_greenhopper._get_json(sub_issue_url, base=JIRA.JIRA_BASE_URL)

                try:
                    estimate = sub_issue_json.fields.timetracking.originalEstimate
                    # estimate = sub_issue_json['fields']['timetracking']['originalEstimate']
                except Exception as e:
                    estimate = ''

                # 打印子任务
                renderCard(outerTab, team, issuetype, issuekey, issuename, \
                        backlogtype, backlogkey, backlogname, owner, estimate )

    output_path = os.getcwd() + '/Jira-' + PRJ_NAME + '.html'
    page.printOut(output_path)
    print("生成打印HTML：" + output_path)


if __name__ == '__main__':
    jira()