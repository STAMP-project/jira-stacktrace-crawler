# XWiki Jira Crawler

This crawler collects available stack traces from submitted issues in Jira.  

## How to work with the crawler

-  You need to set XWiki versions to crawl. For that, you can modify `versions` array in `/src/main/java/Crawler.java`
-  Run `/src/main/java/Crawler.java`
-  The collected stack traces are saved in `stack_traces/[version]/[issue_number]`