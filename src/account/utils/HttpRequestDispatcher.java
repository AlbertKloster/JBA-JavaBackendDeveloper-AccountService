package account.utils;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.servlet.http.HttpServletRequest;

public class HttpRequestDispatcher {

    public static String getNameFromRequest(HttpServletRequest request) {
        String upd = request.getHeader("authorization");
        return upd == null ?
                "" :
                new String(Base64.decodeBase64(upd.substring(6))).split(":")[0].toLowerCase();
    }

}
