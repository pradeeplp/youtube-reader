package ytcapsdowner.vackosar.com.ytcapsdowner;

import android.text.Html;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        String punctuated = downloadCaps();
        System.out.println(punctuated);
    }

    private String downloadCaps() throws IOException {
        String videoInfo = convertStreamToString(new URL("http://www.youtube.com/get_video_info?video_id=6Mfw_LUwo08").openConnection().getInputStream());
        String captionTracks = extractTokenValue("caption_tracks", videoInfo);
        String url = extractTokenValue("u", captionTracks);
        String subs = convertStreamToString(new URL(url).openConnection().getInputStream());
        String text = extractText(subs);
        return punctuate(text);
    }

    private String punctuate(String text) throws IOException {
        byte[] data = ("text=" + text).getBytes();
        HttpURLConnection conn= (HttpURLConnection) new URL("http://bark.phon.ioc.ee/punctuator").openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Length", Integer.toString(data.length));
        conn.setUseCaches(false);
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.write(data);
        wr.close();
        return convertStreamToString(conn.getInputStream());
    }

    private String extractText(String subs) {
        return StringEscapeUtils.unescapeHtml((subs.replaceAll("</text>", " ").replaceAll("<[^>]*>", ""))).replaceAll("<[^>]*>", "").replaceAll("&#39;", "'");
    }

    private String extractTokenValue(String name, String tokens) throws UnsupportedEncodingException {
        for (String token: tokens.split("&")) {
            if (token.startsWith(name + "=")) {
                return URLDecoder.decode(token.split("=")[1], "UTF-8");
            }
        }
        throw new RuntimeException("Token not found");
    }
    static String convertStreamToString(java.io.InputStream is) throws IOException {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        String output = s.hasNext() ? s.next() : "";
        is.close();
        return output;
    }
}