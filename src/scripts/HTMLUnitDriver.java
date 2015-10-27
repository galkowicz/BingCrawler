package scripts;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.*;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HTMLUnitDriver {

    public static DB db = new DB();

    private static final String folderPath = "C:\\Users\\orian\\Pictures\\Bing";


    public static void main(String[] args) throws IOException, SQLException {


        String URL = "http://www.istartedsomething.com/bingimages/#none";
     //   db.runSql2("TRUNCATE Record;");
        filterBing(URL);

     }

    private static void checkExist(String url) throws SQLException, IOException {   // check if link already in database


        String sql = "select * from Record where URL = '"+url+"'";
        ResultSet rs = db.runSql(sql);

        String s ;


        if(rs.next()){  //true when string is already on database
           s = rs.getString(2);                                          // getString(2) -> the url
        }else{
            sql = "INSERT INTO  `webcrawler`.`Record` " + "(`URL`) VALUES " + "(?);";
            PreparedStatement stmt = db.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, url);
            stmt.execute();
            s=getImgExactUrl(url);
            if(!s.isEmpty())
            downloadPhoto(s);  // if not in database than download

        }

    }


    private static void filterBing(String url) throws IOException, SQLException { // gets just the imges links

        Document doc = Jsoup.connect(url).get();

        Elements questions = doc.select("a[href]");

        for (Element link : questions) {

            if (link.attr("href").matches("[#][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][-][a][u]")) {
                System.out.println("links from filter - -"+link.attr("abs:href"));
                checkExist(link.attr("abs:href"));
            }

        }

    }

    private static String getImgExactUrl(String site) {

        System.out.println("image url in getImgExactUrl+ +"+ site);
        WebDriver driver = new HtmlUnitDriver(true);
        String img = "";

        driver.get(site);
        String pageSource = driver.getPageSource();

        Document doc = Jsoup.parse(pageSource);
        //System.out.println(doc);

        Elements castsImageUrl = doc.select("p.image > img");
        for (Element el : castsImageUrl) img = el.attr("src");
        System.out.println(img);
        return img;
    }

    private static void downloadPhoto(String src) throws IOException {

        System.out.println("in downloadPhoto - -"+src);

        String folder = null;

        //Exctract the name of the image from the src attribute
        int indexname = src.lastIndexOf("/");

        if (indexname == src.length()) {
            src = src.substring(1, indexname);
        }

        indexname = src.lastIndexOf("/");
        String name = src.substring(indexname, src.length());

        System.out.println(name);

        //Open a URL Stream
        URL url = new URL(src);
        InputStream in = url.openStream();

        OutputStream out = new BufferedOutputStream(new FileOutputStream( folderPath+ name));

        for (int b; (b = in.read()) != -1;) {
            out.write(b);
        }
        out.close();
        in.close();

    }
}
