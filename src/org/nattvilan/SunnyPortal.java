package org.nattvilan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
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

/**
 * Simple class for showing how to read data from Sunny Portal without using the
 * web interface manually.
 * 
 * @author Samuel Erdtman
 * @author Elias Erdtman
 * 
 */
public class SunnyPortal {

    /** User name for login request */
    private static final String USERNAME      = "username";
    /** Password for login request */
    private static final String PASSWORD      = "password";
    /** Sunny Portal address */
    private static final String HOST          = "https://www.sunnyportal.com";
    /** Login path, used for posting login data */
    private static final String LOGIN         = HOST + "/Templates/UserProfile.aspx";
    /** Select date path, used to select dates */
    private static final String SELECT_DATE   = HOST + "/FixedPages/InverterSelection.aspx";
    /** Download path, used for download requests */
    private static final String DOWNLOAD_FILE = HOST + "/Templates/DownloadDiagram.aspx?down=diag";

    /**
     * Main
     * 
     * @param args
     *            not used at the moment
     * @throws ClientProtocolException
     *             in case of problems
     * @throws IOException
     *             in case of problems
     */
    public static void main(String[] args) throws ClientProtocolException, IOException {

        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).setRedirectStrategy(new LaxRedirectStrategy()).build();

        System.out.println("========================= Login =======================================");
        login(httpclient);

        System.out.println("========================= Open inverter ===============================");
        openInverter(httpclient);

        Calendar start = Calendar.getInstance();
        start.set(2013, Calendar.SEPTEMBER, 27);
        Calendar end = Calendar.getInstance();
        end.set(2013, Calendar.OCTOBER, 5);
        System.out.println("========================= Fetch files =================================");
        while (!start.after(end)) {
            int day = start.get(Calendar.DAY_OF_MONTH);
            int year = start.get(Calendar.YEAR);
            int month = start.get(Calendar.MONTH) + 1;

            System.out.println("Getting: " + day + "-" + month + "-" + year);

            setDate(httpclient, month + "/" + day + "/" + year);
            getFile(httpclient, month + "-" + day + "-" + year + ".csv");

            start.add(Calendar.DATE, 1);
        }
    }

    /**
     * Sends the download request and writes the response to a file
     * 
     * @param httpclient
     *            client to use, important that it is initialized by logging in
     *            and selecting date
     * @param filename
     *            where to save file
     * @throws IOException
     *             in case of problems
     * @throws ClientProtocolException
     *             in case of problems
     * @throws FileNotFoundException
     *             in case of problems
     */
    private static void getFile(CloseableHttpClient httpclient, String filename) throws IOException, ClientProtocolException, FileNotFoundException {
        HttpGet get = new HttpGet(DOWNLOAD_FILE);
        CloseableHttpResponse response = httpclient.execute(get);
        HttpEntity entity = response.getEntity();
        File file = new File(filename);
        FileOutputStream fos = new FileOutputStream(file, false);
        InputStream is = entity.getContent();
        int len = 0;
        byte[] buff = new byte[1024];
        while ((len = is.read(buff)) != -1) {
            fos.write(buff, 0, len);
        }
        fos.close();
    }

    /**
     * Posts the date to Sunny Portal in preparation for a download
     * 
     * @param httpclient
     *            the client to select date for
     * @param date
     *            the date for data to fetch format must be "MM/DD/YYYY"
     * @throws IOException
     *             in case of problems
     * @throws ClientProtocolException
     *             in case of problems
     */
    private static void setDate(CloseableHttpClient httpclient, String date) throws IOException, ClientProtocolException {
        HttpPost httpost = new HttpPost(SELECT_DATE);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("__EVENTTARGET", "ctl00$ContentPlaceHolder1$UserControlShowInverterSelection1$LinkButton_TabFront3"));
        nvps.add(new BasicNameValuePair("ctl00$ContentPlaceHolder1$UserControlShowInverterSelection1$_datePicker$textBox", date));
        nvps.add(new BasicNameValuePair("ctl00$HiddenPlantOID", "f7cee014-4c40-4ff7-a5d7-2b6b50193de0"));
        httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
        CloseableHttpResponse response = httpclient.execute(httpost);
        HttpEntity entity = response.getEntity();
        EntityUtils.consume(entity);
    }

    /**
     * Helper method to send request that opens the inverter page
     * 
     * @param httpclient
     *            client to put on inverter page
     * @throws IOException
     *             in case of problems
     * @throws ClientProtocolException
     *             in case of problems
     */
    private static void openInverter(CloseableHttpClient httpclient) throws IOException, ClientProtocolException {
        HttpPost httpost = new HttpPost(LOGIN);
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("__EVENTTARGET", "ctl00$NavigationLeftMenuControl$0_6"));
        httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
        CloseableHttpResponse response = httpclient.execute(httpost);
        HttpEntity entity = response.getEntity();
        EntityUtils.consume(entity);
    }

    /**
     * Helper method for doing initial login
     * 
     * @param httpclient
     *            the client to login
     * @throws IOException
     *             in case of problems
     * @throws ClientProtocolException
     *             in case of problems
     */
    private static void login(CloseableHttpClient httpclient) throws IOException, ClientProtocolException {
        HttpPost httpost = new HttpPost("https://www.sunnyportal.com/Templates/Start.aspx");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("ctl00$ContentPlaceHolder1$Logincontrol1$txtUserName", USERNAME));
        nvps.add(new BasicNameValuePair("ctl00$ContentPlaceHolder1$Logincontrol1$txtPassword", PASSWORD));
        nvps.add(new BasicNameValuePair("__EVENTTARGET", "ctl00$ContentPlaceHolder1$Logincontrol1$LoginBtn"));
        httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
        CloseableHttpResponse response = httpclient.execute(httpost);
        HttpEntity entity = response.getEntity();
        EntityUtils.consume(entity);
    }
}
