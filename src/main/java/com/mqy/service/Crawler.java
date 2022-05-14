package com.mqy.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.http.HttpClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.PostConstruct;
import java.applet.AppletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ma.qiyue on 2022/5/14 0:09
 */
@Service
@EnableScheduling
@SpringBootApplication
public class Crawler{
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Crawler.class, args);
    }

    public static String path = "D:\\BackGround";
    public static String today = "\\today";
    public static String before = "\\before";

    @Scheduled(cron = "0 0/3 * * * ?")
    public void test(){
        System.out.println(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
    }
    public  boolean check(String date) throws Exception{
        try{
            File file = new File(path + today);
            if (file.isDirectory()){
                File[] list = file.listFiles();
                if (list == null || list.length == 0) return false;
                for (int i = 0; i < list.length; i++) {
                    String name = list[i].getAbsolutePath();
                    if (name.contains(date)){
                        System.out.println("今日已下载,无需重复执行");
                        return true;
                    }
                }
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }
    public void movePic(){
        File file = new File(path);
        if (!file.exists()){
            file.mkdir();
        }
        file = new File(path + before);
        if (!file.exists()){
            file.mkdir();
        }
        file = new File(path + today);
        if (!file.exists()){
            file.mkdir();
        }
        if (file.isDirectory()){
            File[] list = file.listFiles();
            if (list == null || list.length == 0) return;
            for (int i = 0; i < list.length; i++) {
                File pic = list[i];
                String name = pic.getAbsolutePath();
                String newName = name.replaceAll("today","before");
                pic.renameTo(new File(newName));
            }
        }
    }
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void getPic() throws Exception{
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (check(date)) return;
        movePic();
        System.setProperty("webdriver.chrome.driver", ".\\chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("headless");
        options.addArguments("disable-gpu");
        String proxy = new RestTemplate().getForObject("http://webapi.http.zhimacangku.com/getip?num=1&type=1&pro=0&city=0&yys=0&port=1&time=1&ts=0&ys=0&cs=0&lb=1&sb=0&pb=4&mr=1&regions=",String.class);
        proxy = proxy.replaceAll("\r\n","");
        options.addArguments("--proxy-server=http://"+proxy);
        WebDriver driver = new ChromeDriver(options);
//        WebDriver driver = new ChromeDriver();
        driver.get("https://huaban.com");
        String title = driver.getTitle();
        System.out.print(title);
        Thread.sleep(15*1000);
        WebElement login = driver.findElement(By.className("lUDCpxvZ"));
        login.click();
        WebElement username = driver.findElement(By.id("email"));
        WebElement password = driver.findElement(By.id("password"));
        username.sendKeys("18825197213");
        password.sendKeys("wuwangyu875");
        WebElement login2 = driver.findElement(By.cssSelector("button[class='ant-btn ant-btn-primary CtUqjjIt']"));
        login2.click();
        Thread.sleep(15*1000);
        WebElement close = driver.findElement(By.className("ant-modal-close"));
        close.click();
        Thread.sleep(15*1000);
        List<WebElement> imgs1 = driver.findElements(By.cssSelector("img[class='flu_QVTL']"));
        Thread.sleep(15*1000);
        List<String> list = new ArrayList<>();
        for (WebElement item : imgs1){
            String pic = item.getAttribute("src").replaceAll("_fw240/format/webp","");
            list.add(pic);
            System.out.println(pic);
        }

//        String pageSource = driver.getPageSource();
//        System.out.println(pageSource);

        for (String item : list){
            URL url = new URL(item);
            InputStream is = url.openStream();
            String file = path +today+ "\\" +date +"_"+item.replaceAll("https:.+?huaban.com/","")+ ".jpg";
            FileOutputStream fos = new FileOutputStream(file);
            byte buf[] = new byte[1024];
            int length = 0;
            while ((length = is.read(buf)) != -1) {
                fos.write(buf, 0, length);
            }
            fos.close();
            is.close();
            System.out.println(file + "下载成功");
        }
    }
}
