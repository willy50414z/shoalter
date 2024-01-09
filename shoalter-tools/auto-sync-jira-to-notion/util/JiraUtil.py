# import the installed Jira library
from jira import JIRA

jiraOptions = {'server': "https://hongkongtv.atlassian.net/"}

class JiraUtil:
    def __init__(self):
        self.jiraObj = JIRA(options=jiraOptions, basic_auth=(
            "willy.cheng@shoalter.com", "ATATT3xFfGF0NOjlDBqH8C5zTPx_WX9cDY940p7NVPjN-XjHcO3REXGMc0L7HlNlbCPc-fSG9eC8Bjg3gD6xqRosbt1fSOv7zUHIfeU-r2sDw_ymcp_BLjzadlv4QwjJpHxFZ8Q9uRy4NXVAnYlkV-poRtALJ4S9iYi2kSh8dJ6HCSHuyNYu15k=33059B12"))

    def getIncompletedTask(self):
        #assemble filter
        assignees = ['TW - IT - BE - Willy Cheng', 'TW - IT - BE - Jayce Wu', 'TW - IT - BE - Ainsley Wang', 'TW - IT - BE - JOHN CHANG','TW - IT - BE - Luke Chen']
        assignee_query = ', '.join([f'"{assignee}"' for assignee in assignees])

        devPICs = ['TW - IT - BE - Willy Cheng', 'TW - IT - BE - Jayce Wu', 'TW - IT - BE - Ainsley Wang', 'TW - IT - BE - JOHN CHANG','TW - IT - BE - Luke Chen']
        devPIC_query = ', '.join([f'"{devPIC}"' for devPIC in devPICs])

        statuses = ['Done', 'Cancelled', 'Pending UAT', 'Launch Ready', 'Closed']
        status_query = ', '.join([f'"{status}"' for status in statuses])

        jql_query = f'("Development PIC" IN ({devPIC_query}) OR assignee IN ({assignee_query})) AND status not in ({status_query})'

        #fetch data
        issues = self.jiraObj.search_issues(jql_str=jql_query)
        return issues

#get single ticket
#singleIssue = jira.issue('SI-18')
#print('{}: {}:{}'.format(singleIssue.key,
#                         singleIssue.fields.summary,
#                         singleIssue.fields.reporter.displayName))
                         
#get issue by project
#for singleIssue in jira.search_issues(jql_str='project = SI'):
#    print('{}: {}:{}'.format(singleIssue.key, singleIssue.fields.summary,
#                             singleIssue.fields.reporter.displayName))
#statuses = jira.statuses()
#
## Print the names of the statuses
#for status in statuses:
#    print(status.name)
  
#get issue by assignee   





##https://hongkongtv.atlassian.net/rest/api/latest/issue/EER-86
#for issue in issues:
#    issue_key = issue.key
#    issue_assignee = issue.fields.assignee
#    issue_type = issue.fields.issuetype.name
#    summary = issue.fields.summary
#    status = issue.fields.status
#    parentKey = issue.fields.parent.key
#    subtask = issue.fields.subtasks
#    print(f"key: {issue_key}\tIssue_type: {issue_type}\tSummary: {summary}\tStatus: {status}\tissue_assignee: {issue_assignee}\tparentKey: {parentKey}\tsubtask: {subtask}")
    
#key: EER-120    Issue_type: 子任務      Summary: deployment to staging  Status: 待辦事項        issue_assignee: TW - IT - BE - Willy Cheng      parentKey: EER-96       subtask: []
#key: EER-119    Issue_type: 子任務      Summary: deployment to dev      Status: 待辦事項        issue_assignee: TW - IT - BE - Willy Cheng      parentKey: EER-96       subtask: []
#key: EER-118    Issue_type: 子任務      Summary: create project skeleton for checkout-service   Status: 待辦事項        issue_assignee: TW - IT - BE - Willy Cheng      parentKey: EER-96       subtask: []
#key: EER-110    Issue_type: 子任務      Summary: deployment to staging  Status: 待辦事項        issue_assignee: TW - IT - BE - Willy Cheng      parentKey: EER-92       subtask: []
#key: EER-109    Issue_type: 子任務      Summary: deployment to dev      Status: 待辦事項        issue_assignee: TW - IT - BE - Willy Cheng      parentKey: EER-92       subtask: []
#key: EER-108    Issue_type: 子任務      Summary: create project skeleton for cart-service       Status: 待辦事項        issue_assignee: TW - IT - BE - Willy Cheng      parentKey: EER-92       subtask: []
#key: EER-107    Issue_type: 子任務      Summary: deployment to staging  Status: 待辦事項        issue_assignee: TW - IT - BE - Willy Cheng      parentKey: EER-86       subtask: []
#key: EER-106    Issue_type: 子任務      Summary: deployment to dev      Status: 待辦事項        issue_assignee: TW - IT - BE - Willy Cheng      parentKey: EER-86       subtask: []
#key: EER-89     Issue_type: 子任務      Summary: create project skeleton for  address-service   Status: 進行中  issue_assignee: TW - IT - BE - Willy Cheng      parentKey: EER-86       subtask: []
#key: EER-86     Issue_type: 新功能      Summary: [BE][App][delivery address] Get customer delivery address data from hybris DB via new API gateway      Status: Waiting for Development issue_assignee: TW - IT - BE - Willy Cheng      parent
#Key: EER-64     subtask: [<JIRA Issue: key='EER-89', id='89110'>, <JIRA Issue: key='EER-90', id='89111'>, <JIRA Issue: key='EER-106', id='89350'>, <JIRA Issue: key='EER-107', id='89351'>]
#key: EER-50     Issue_type: 子任務      Summary: Study mobile landing page API  Status: 待辦事項        issue_assignee: TW - IT - BE - Willy Cheng      parentKey: EER-48       subtask: []
#key: EER-49     Issue_type: 子任務      Summary: Study PC landing page header and footer API    Status: 待辦事項        issue_assignee: TW - IT - BE - Willy Cheng      parentKey: EER-48       subtask: [] 