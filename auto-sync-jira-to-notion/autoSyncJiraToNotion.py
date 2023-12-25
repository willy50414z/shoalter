from util import NotionUtil
from util.JiraUtil import JiraUtil

#NotionUtil.createPage('EER-XXX', 'title','task','willy.cheng@shoalter.com')
#print(NotionUtil.findByTicketLike('MS-2831'))

excludeTicket = ["MS-1490","MS-1308"]

def createNotionTaskFromJira():
    # fetch JIRA incomplete ticket
    issues = JiraUtil().getIncompletedTask()
    issues.sort(key=lambda iss: not iss.fields.issuetype.subtask)
    #for issue in issues:
    #    print(issue.key)iddaa--ddd  sda1231

    # check is ticket exist in notion
    for issue in issues:
        # exclude dead ticket
        if issue.key in excludeTicket or issue.key.startswith('BUILD'):
            print(f"[{issue.key}] is excluded ticket")
            continue

        # only create subtask or task without subtask
        if issue.fields.issuetype.name != '大型工作' and (issue.fields.issuetype.subtask or 0 == len(issue.fields.subtasks)):

            # get default assignee
            displayName = 'TW - IT - BE - Willy Cheng'
            if issue.fields.assignee is not None:
                displayName = issue.fields.assignee.displayName


            print(f"check notion contains ticket or not, ticket[{issue.key}]")
            notionItemList = NotionUtil.findByTicketLike(NotionUtil.task_database_id, issue.key)

            if 0 == len(notionItemList):
                if issue.fields.issuetype.subtask:
                    print(f"ticket is not exist. Start to create ticket to notion, ticket[{issue.key}]")
                    NotionUtil.createTaskPage(issue.key, issue.fields.summary, issue.fields.parent.key, displayName)
                else:
                    NotionUtil.createFeaturePage(issue.key, issue.fields.summary, issue.key, displayName)
                    NotionUtil.createTaskPage(issue.key, issue.fields.summary, issue.key, displayName)

            else:
                print(f"ticket is exist, ticket[{issue.key}]")
# create notion item

# update notion status


if __name__=='__main__':
    createNotionTaskFromJira()
    # NotionUtil.deleteOutOfDateTask()
    # print(NotionUtil.findByTicketLike(NotionUtil.task_database_id, "2428"))
    # NotionUtil.update("effd895aaf8541118b5a2e5b8f54f05e")
    # title = "[IIDS] xxfcff"
    # projectCode = title[1:title.index("]")]
    # print(projectCode)

    # notionItems = NotionUtil.findAllReleases()
    # for notionItem in notionItems:
    #     print(notionItem["id"])
    #     print(notionItem["properties"]["Name"]["title"][0]["text"]["content"])
    #     print(notionItem["properties"]["Release Date"]["date"]["start"])
    #     for releaseTicket in notionItem["properties"]["ReleaseTickets"]["multi_select"]:
    #         print(releaseTicket["name"])
