package org.nattvilan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class SunnyPortalPublic {

    public static void main(String[] args) throws IOException {
        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).setRedirectStrategy(new LaxRedirectStrategy()).build();
        String viewstate;
        {
            HttpGet get = new HttpGet("http://www.sunnyportal.com/Templates/PublicPage.aspx?page=433f7377-fda7-4a12-a6dc-361b0aef84f3");
            CloseableHttpResponse response = httpclient.execute(get);
            HttpEntity entity = response.getEntity();
            BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
            String line;
            String result = "";
            while ((line = br.readLine()) != null) {
                result += line;
            }
            Document doc = Jsoup.parse(result);
            Element viewstateElement = doc.getElementById("__VIEWSTATE");
            viewstate = viewstateElement.val();

            Element CurrentPlantPowerValueElement = doc.getElementById("CurrentPlantPowerValue");
            String CurrentPlantPowerValue = CurrentPlantPowerValueElement.html();

            Element CurrentPlantPowerUnitElement = doc.getElementById("CurrentPlantPowerUnit");
            String CurrentPlantPowerUnit = CurrentPlantPowerUnitElement.html();

            System.out.println("Now producing " + CurrentPlantPowerValue + " " + CurrentPlantPowerUnit);
            br.close();
        }

        {
            HttpPost httpost = new HttpPost("http://www.sunnyportal.com/Templates/PublicPage.aspx?page=433f7377-fda7-4a12-a6dc-361b0aef84f3");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("__VIEWSTATE", viewstate));
            nvps.add(new BasicNameValuePair("__EVENTTARGET", "PublicPagePlaceholder_PageUserControl_ctl00_PublicPageLoadFixPage_energyYieldWidgetLoader_AsyncLoadControlUpdatePanel"));
            nvps.add(new BasicNameValuePair("PublicPagePlaceholder$PageUserControl$ctl00$PublicPageLoadFixPage$UserControlShowEnergyAndPower1$_datePicker$textBox", "12/6/2013")); // viktigt
            nvps.add(new BasicNameValuePair("PublicPagePlaceholder$PageUserControl$ctl00$PublicPageLoadFixPage$DashboardScriptManager",
                    "PublicPagePlaceholder$PageUserControl$ctl00$PublicPageLoadFixPage$energyYieldWidgetLoader$AsyncLoadControlUpdatePanel|PublicPagePlaceholder_PageUserControl_ctl00_PublicPageLoadFixPage_energyYieldWidgetLoader_AsyncLoadControlUpdatePanel"));

            httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            CloseableHttpResponse response = httpclient.execute(httpost);
            HttpEntity entity = response.getEntity();
            BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
            String line;
            List<String> file = new ArrayList<String>();
            while ((line = br.readLine()) != null) {
                file.add(line);
            }
            String result = StringUtils.join(file.toArray(), "\n", 2, file.size() - 2);

            Document doc = Jsoup.parse(result);
            {
                Element energyYieldValueElement = doc.getElementById("energyYieldValue");
                String energyYieldValue = energyYieldValueElement.html();
                Element energyYieldUnitElement = doc.getElementById("energyYieldUnit");
                String energyYieldUnit = energyYieldUnitElement.html();
                Element energyYieldPeriodTitleElement = doc.getElementById("PublicPagePlaceholder_PageUserControl_ctl00_PublicPageLoadFixPage_energyYieldWidgetLoader_SubControl_energyYieldWidgetContent_energyYieldPeriodTitle");
                String energyYieldPeriodTitle = energyYieldPeriodTitleElement.html();
                System.out.println("Produced " + energyYieldValue + " " + energyYieldUnit + " in " + energyYieldPeriodTitle);
            }
            {
                Element energyYieldTotalValueElement = doc.getElementById("energyYieldTotalValue");
                String energyYieldTotalValue = energyYieldTotalValueElement.html();
                Element energyYieldTotalUnitElement = doc.getElementById("energyYieldTotalUnit");
                String energyYieldTotalUnit = energyYieldTotalUnitElement.html();
                System.out.println("Totaly produced " + energyYieldTotalValue + " " + energyYieldTotalUnit);
            }

            EntityUtils.consume(entity);
        }
    }
}
