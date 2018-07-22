
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.pagefactory.ByChained;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {
    // Do not collect issues for older issues
    public static String[] versions = {"7.0","8.0"};
    // A regex for detecting stack traces
    public static Pattern stackTracePattern = Pattern.compile(".*(\\w\\.\\w+)+Exception.*\\s+(at \\w+(\\.\\w+)+[(]\\w+\\.\\w+:\\d+[)]\\s+)+");
    public static String searchLink = "http://jira.xwiki.org/issues/?jql=project%20%3D%20XWIKI%20AND%20affectedVersion%20%3D%20";
    public static FileWriter writer;
    public static void main(String args[]){

        // Set Chrome as the web driver
        System.setProperty("webdriver.chrome.driver", "driver/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);
        WebDriver driver = new ChromeDriver(options);
        WebDriver searchDriver = new ChromeDriver();
        System.out.println("Drivers are ready!");

        for (String version : versions){
            File versionDir = new File(Paths.get("stack_traces",version).toString());
            if (!versionDir.exists()) {
                versionDir.mkdir();
            }
            int issueStartPoint = 0;
            while(true) {
                searchDriver.get(searchLink + version + "+&startIndex=" + (issueStartPoint*50));
                System.out.println("Issues for version " + version + ", page number " + ((issueStartPoint % 50) + 1) + " is loaded.");
                List<WebElement> issuekeys = searchDriver.findElements(new ByChained(By.className("splitview-issue-link")));
                for (WebElement key : issuekeys) {
                    String href = key.getAttribute("href");
                    driver.get(href);
                    System.out.println("Visiting page "+href);
                    // Detect Stack trace in a page
                    try {
                        WebElement userContentBlock = driver.findElement(By.className("user-content-block"));
                        String userConten = userContentBlock.getText();
                        Matcher matcher = stackTracePattern.matcher(userConten);
                        while (matcher.find()) {
                            String [] hrefArr = href.split("/");
                            File stacktraceTXTFile = new File(Paths.get("stack_traces",version,hrefArr[hrefArr.length - 1]+".txt").toString());
                            try {
                                writer = new FileWriter(stacktraceTXTFile,true);
                                writer.write("____________________________"+System.getProperty("line.separator"));
                                writer.write(userConten.substring(matcher.start(), matcher.end())+System.getProperty("line.separator"));
                                writer.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }catch (NoSuchElementException e) {}
                }

                try {
                    searchDriver.findElement(By.cssSelector("a[data-start-index='" + ((issueStartPoint + 1) * 50) + "']"));
                    issueStartPoint++;
                } catch (NoSuchElementException e) {
                    break;
                }
            }
        }
        searchDriver.quit();
        driver.quit();

    }
}
