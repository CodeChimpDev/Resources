package resources.codechimps.co.uk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SMTP { static class Coder {
	
	    /*
	     * Example Usage:
	     * Email.sendEmail("smtp address", "email address", "email password", "name", "recipient", "recipient email", "subject", 
	     *                 "content - /n = new line", false);
	     */
	
        private static final char[] map1 = new char[64];
        private static final byte[] map2 = new byte[128];
        static {
            int i = 0;
            for (char c = 'A'; c <= 'Z'; c++) {
                map1[i++] = c;
            }
            for (char c = 'a'; c <= 'z'; c++) {
                map1[i++] = c;
            }
            for (char c = '0'; c <= '9'; c++) {
                map1[i++] = c;
            }
            map1[i++] = '+';
            map1[i++] = '/';
            for (i = 0; i < map2.length; i++) {
                map2[i] = -1;
            }
            for (i = 0; i < 64; i++) {
                map2[map1[i]] = (byte) i;
            }
        }
        public static String encodeString(String s) {
            return new String(encode(s.getBytes()));
        }
        public static char[] encode(byte[] in) {
            return encode(in, 0, in.length);
        }
        public static char[] encode(byte[] in, int iLen) {
            return encode(in, 0, iLen);
        }
        public static char[] encode(byte[] in, int iOff, int iLen) {
            int oDataLen = (iLen * 4 + 2) / 3;
            int oLen = ((iLen + 2) / 3) * 4;
            char[] out = new char[oLen];
            int ip = iOff;
            int iEnd = iOff + iLen;
            int op = 0;
            while (ip < iEnd) {
                int i0 = in[ip++] & 0xff;
                int i1 = ip < iEnd ? in[ip++] & 0xff : 0;
                int i2 = ip < iEnd ? in[ip++] & 0xff : 0;
                int o0 = i0 >>> 2;
                int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
                int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
                int o3 = i2 & 0x3F;
                out[op++] = map1[o0];
                out[op++] = map1[o1];
                out[op] = op < oDataLen ? map1[o2] : '=';
                op++;
                out[op] = op < oDataLen ? map1[o3] : '=';
                op++;
            }
            return out;
        }
        public static String decodeString(String s) {
            return new String(decode(s));
        }
        public static byte[] decodeLines(String s) {
            char[] buf = new char[s.length()];
            int p = 0;
            for (int ip = 0; ip < s.length(); ip++) {
                char c = s.charAt(ip);
                if (c != ' ' && c != '\r' && c != '\n' && c != '\t')
                    buf[p++] = c;
            }
            return decode(buf, 0, p);
        }
        public static byte[] decode(String s) {
            return decode(s.toCharArray());
        }
        public static byte[] decode(char[] in) {
            return decode(in, 0, in.length);
        }
        public static byte[] decode(char[] in, int iOff, int iLen) {
            if (iLen % 4 != 0)
                throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
            while (iLen > 0 && in[iOff + iLen - 1] == '=') iLen--;
            int oLen = (iLen * 3) / 4;
            byte[] out = new byte[oLen];
            int ip = iOff;
            int iEnd = iOff + iLen;
            int op = 0;
            while (ip < iEnd) {
                int i0 = in[ip++];
                int i1 = in[ip++];
                int i2 = ip < iEnd ? in[ip++] : 'A';
                int i3 = ip < iEnd ? in[ip++] : 'A';
                if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127)
                    throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
                int b0 = map2[i0];
                int b1 = map2[i1];
                int b2 = map2[i2];
                int b3 = map2[i3];
                if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0)
                    throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
                int o0 = (b0 << 2) | (b1 >>> 4);
                int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
                int o2 = ((b2 & 3) << 6) | b3;
                out[op++] = (byte) o0;
                if (op < oLen) out[op++] = (byte) o1;
                if (op < oLen) out[op++] = (byte) o2;
            }
            return out;
        }
        static String convertToHex(byte[] data) {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < data.length; i++) {
                int halfbyte = (data[i] >>> 4) & 0x0F;
                int two_halfs = 0;
                do {
                    if ((0 <= halfbyte) && (halfbyte <= 9))
                        buf.append((char) ('0' + halfbyte));
                    else
                        buf.append((char) ('a' + (halfbyte - 10)));
                    halfbyte = data[i] & 0x0F;
                } while (two_halfs++ < 1);
            }
            return buf.toString();
        }
    }
    class Response {
        public final int code;
        public final String message;
        public Response(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }
    class SMTPException extends RuntimeException {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
 
        public SMTPException(Exception e) {
            super(e);
        }
        public SMTPException(String message) {
            super(message);
        }
        public SMTPException(Response response) {
            super(String.format("%d: %s", response.code, response.message));
        }
    }
    class SMTPConnectException extends SMTPException {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
 
        public SMTPConnectException(Exception e) {
            super(e);
        }
    }
    class SMTPDisconnectedException extends SMTPException {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
 
        public SMTPDisconnectedException(Exception e) {
            super(e);
        }
        public SMTPDisconnectedException(String message) {
            super(message);
        }
    }
    class SMTPAuthenticationException extends SMTPException {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
 
        public SMTPAuthenticationException(Response response) {
            super(response);
        }
    }
    class EmailError extends RuntimeException {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
 
        public EmailError(String error) {
            super(error);
        }
    }
    class Email {
        protected Map<String, String> headers = new HashMap<String, String>();
        protected String body = "";
        protected Map.Entry<String, String> from;
        protected Map<String, String> to = new HashMap<String, String>();
        protected Map<String, String> cc = new HashMap<String, String>();
        protected Map<String, String> bcc = new HashMap<String, String>();
        final SimpleDateFormat emailFormat = new SimpleDateFormat("EEE, d MMMMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        public Email from(String email) {
            return from(null, email);
        }
        public Email to(String email) {
            return to(null, email);
        }
        public Email cc(String email) {
            return cc(null, email);
        }
        public Email bcc(String email) {
            return bcc(null, email);
        }
        public Email from(String name, String email) {
            from = new HashMap.SimpleImmutableEntry<String, String>(email, name);
            return this;
        }
        public Email to(String name, String email) {
            to.put(email, name);
            return this;
        }
        public Email cc(String name, String email) {
            cc.put(email, name);
            return this;
        }
        public Email bcc(String name, String email) {
            bcc.put(email, name);
            return this;
        }
        public Email date(Date date) {
            headers.put("Date", emailFormat.format(date));
            return this;
        }
        public Email subject(String subject) {
            headers.put("Subject", subject);
            return this;
        }
        public Email body(String body) {
            this.body = body;
            return this;
        }
        public Email add(String header, String value) {
            headers.put(header, value);
            return this;
        }
        protected String concatEmail(Map.Entry<String, String> entry) {
            String email = entry.getKey(), name = entry.getValue();
            if (name == null || name.isEmpty())
                return email;
            return String.format("\"%s\" <%s>", name, email);
        }
        protected String buildEmailList(Map<String, String> emails) {
            ArrayList<String> concated = new ArrayList<String>();
            for (Map.Entry<String, String> entry : emails.entrySet()) {
                concated.add(concatEmail(entry));
            }
            return join(", ", concated);
        }
        public List<String> getAllAddresses() {
            ArrayList<String> addresses = new ArrayList<String>();
            for (Map.Entry<String, String> entry : to.entrySet())
                addresses.add(entry.getKey());
            for (Map.Entry<String, String> entry : cc.entrySet())
                addresses.add(entry.getKey());
            for (Map.Entry<String, String> entry : bcc.entrySet())
                addresses.add(entry.getKey());
            return addresses;
        }
        public String getFromAddress() {
            return from.getKey();
        }
        public String toString() {
            StringBuilder email = new StringBuilder();
            if (from != null)
                headers.put("From", concatEmail(from));
            if (!to.isEmpty())
                headers.put("To", buildEmailList(to));
            if (!cc.isEmpty())
                headers.put("Cc", buildEmailList(cc));
            if (!headers.containsKey("Date")) {
                headers.put("Date", emailFormat.format(new Date()));
            }
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                email.append(entry.getKey());
                email.append(": ");
                email.append(entry.getValue());
                email.append("\n");
            }
            email.append("\n");
            email.append(body);
            return email.toString();
        }
    }
    public static String join(String deliminator, Iterable<String> iterable) {
        StringBuilder buffer = new StringBuilder();
        for (String string : iterable) {
            buffer.append(string).append(deliminator);
        }
        if (buffer.length() < deliminator.length())
            return "";
        return buffer.toString().substring(0, buffer.length() - deliminator.length());
    }
    protected Socket socket;
    protected BufferedReader read;
    protected OutputStreamWriter write;
    protected String heloResponse, ehloResponse;
    protected Map<String, String> esmtpFeatures;
    protected boolean esmtp = false;
    protected boolean debug = false;
    private static Pattern esmtpFeature = Pattern.compile("([A-Za-z0-9][A-Za-z0-9\\-]*) ?(.*)?");
    private static Pattern esmtpOldAuth = Pattern.compile("auth=(.*)", Pattern.CASE_INSENSITIVE);
    private static Pattern addressOnly = Pattern.compile("<(.*)>");
    public SMTP(String host) {
        connect(host);
    }
    public SMTP(String host, int port) {
        connect(host, port);
    }
    public Response connect(String host) {
        int port = 25;
        if (host.contains(":")) {
            String[] data = host.split(":");
            if (data.length == 2) {
                try {
                    int i = Integer.parseInt(data[1]);
                    port = i;
                    host = data[0];
                } catch (NumberFormatException e) {
                }
            }
        }
        return connect(host, port);
    }
    public Response connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            return getReply();
        } catch (IOException e) {
            throw new SMTPConnectException(e);
        }
    }
    protected void send(String line) {
        if (socket == null)
            throw new SMTPDisconnectedException("Not connected in the first place.");
        try {
            if (write == null)
                write = new OutputStreamWriter(socket.getOutputStream());
            write.write(line);
            if (debug) {
                java.lang.System.out.print('>');
                java.lang.System.out.print(line);
            }
            write.flush();
        } catch (IOException e) {
            throw new SMTPException(e);
        }
    }
    protected void putCommand(String cmd, String args) {
        if (args == null || args.isEmpty())
            cmd += "\r\n";
        else
            cmd += " " + args + "\r\n";
        send(cmd);
    }
    protected Response getReply() {
        try {
            if (read == null) {
                read = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
            String line;
            int code = -1;
            ArrayList<String> responses = new ArrayList<String>();
            while ((line = read.readLine()) != null) {
                if (line.length() < 4)
                    throw new SMTPDisconnectedException("Not enough data read.");
                if (debug) {
                    java.lang.System.out.print('<');
                    java.lang.System.out.println(line);
                }
                code = Integer.parseInt(line.substring(0, 3));
                responses.add(line.substring(4));
                if (line.charAt(3) != '-')
                    break;
            }
            return new Response(code, join("\n", responses));
        } catch (IOException e) {
            throw new SMTPException(e);
        }
    }
    public Response doCommand(String cmd) {
        return doCommand(cmd, null);
    }
    public Response doCommand(String cmd, String args) {
        putCommand(cmd, args);
        return getReply();
    }
    public Response helo() {
        return helo(null);
    }
    public Response helo(String name) {
        Response response = doCommand("HELO", name);
        heloResponse = response.message;
        return response;
    }
    protected void addAuth(String value) {
        if (esmtpFeatures.containsKey("auth")) {
            esmtpFeatures.put("auth", esmtpFeatures.get("auth") + " " + value);
        } else
            esmtpFeatures.put("auth", value);
    }
    public Response ehlo() {
        return ehlo(null);
    }
    public Response ehlo(String name) {
        Response response = doCommand("EHLO", name);
        ehloResponse = response.message;
        if (response.code != 250)
            return response;
        esmtp = true;
        esmtpFeatures = new HashMap<String, String>();
        for (String line : ehloResponse.split("\n")) {
            Matcher matcher = esmtpOldAuth.matcher(line);
            if (matcher.matches()) {
                addAuth(matcher.group(1));
            } else {
                matcher = esmtpFeature.matcher(line);
                if (matcher.matches()) {
                    esmtpFeatures.put(matcher.group(1).toLowerCase(), matcher.group(2));
                }
            }
        }
        return response;
    }
    public boolean hasExtension(String name) {
        return esmtp && esmtpFeatures.containsKey(name.toLowerCase());
    }
    public String help(String args) {
        return doCommand("help", args).message;
    }
    public Response rset() {
        return doCommand("rset");
    }
    public Response noop() {
        return doCommand("noop");
    }
    protected static String formatEmail(String address) {
        return String.format("<%s>", addressOnly(address));
    }
    public Response mail(String sender) {
        return mail(sender, null);
    }
    public Response mail(String sender, String options) {
        options = (options == null && esmtp) ? "" : " " + options;
        return doCommand("mail", "FROM:" + formatEmail(sender) + options);
    }
    public Response rcpt(String recipient) {
        return rcpt(recipient, null);
    }
    public Response rcpt(String recipient, String options) {
        options = (options == null && esmtp) ? "" : " " + options;
        return doCommand("rcpt", "TO:" + formatEmail(recipient) + options);
    }
    public Response data(Email email) {
        return data(email.toString());
    }
    public Response data(String data) {
        Response response = doCommand("data", null);
        if (response.code != 354)
            return response;
        data = data.replaceAll("(?:\\r\\n|\\n|\\r(?!\\n))", "\r\n").replaceAll("^\\.", "..");
        if (!data.endsWith("\r\n"))
            data += "\r\n";
        data += "." + "\r\n";
        send(data);
        return getReply();
    }
    protected static String addressOnly(String address) {
        Matcher matcher = addressOnly.matcher(address);
        return matcher.matches() ? matcher.group(1) : address;
    }
    public Response verify(String address) {
        return doCommand("vrfy", addressOnly(address));
    }
    public Response expn(String address) {
        return doCommand("expn", addressOnly(address));
    }
    public void greet(String server) {
        if (heloResponse != null || ehloResponse != null)
            return;
        int code = ehlo(server).code;
        if (code >= 200 && code < 300)
            return;
        Response response = helo(server);
        if (response.code < 200 || response.code > 299)
            throw new SMTPException(response);
    }
    protected Response authCramMD5(String user, String password) {
        Response response = doCommand("AUTH", "CRAM-MD5");
        if (response.code == 503)
            return response;
        Mac mac;
        try {
            mac = Mac.getInstance("HmacMD5");
            mac.init(new SecretKeySpec(password.getBytes(Charset.forName("UTF-8")), "HmacMD5"));
        } catch (NoSuchAlgorithmException e) {
            return response;
        } catch (InvalidKeyException e) {
            return response;
        }
        byte[] hash = mac.doFinal(Coder.decode(response.message));
        return doCommand(Coder.encodeString(user + " " + Coder.convertToHex(hash)));
    }
    protected Response authPlain(String user, String password) {
        byte[] user_ = user.getBytes();
        byte[] pass_ = password.getBytes(Charset.forName("UTF-8"));
        byte[] plain = new byte[2 + user_.length + pass_.length];
        plain[0] = 0;
        java.lang.System.arraycopy(user_, 0, plain, 1, user_.length);
        plain[user_.length + 1] = 0;
        java.lang.System.arraycopy(pass_, 0, plain, user_.length + 2, pass_.length);
        return doCommand("AUTH", "PLAIN " + new String(Coder.encode(plain)));
    }
    protected Response authLogin(String user, String password) {
        Response response = doCommand("AUTH", "LOGIN " + Coder.encodeString(user));
        if (response.code != 334)
            return response;
        return doCommand(Coder.encodeString(password));
    }
    protected static HashMap<String, Method> authMap;
    protected static List<String> authMethods = new ArrayList<String>(Arrays.asList("CRAM-MD5", "PLAIN", "LOGIN"));
    static {
        authMap = new HashMap<String, Method>();
        try {
            authMap.put("CRAM-MD5", SMTP.class.getDeclaredMethod("authCramMD5", String.class, String.class));
            authMap.put("PLAIN", SMTP.class.getDeclaredMethod("authPlain", String.class, String.class));
            authMap.put("LOGIN", SMTP.class.getDeclaredMethod("authLogin", String.class, String.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
    public Response login(String user, String password, String server) {
        greet(server);
        if (!hasExtension("auth"))
            throw new SMTPException("Authentication not supported");
        String authMethod = esmtpFeatures.get("auth").toUpperCase();
        Response response = new Response(-1, "Error");
        boolean authed = false;
        for (String method : authMethods) {
            if (!authMethod.contains(method))
                continue;
            try {
                response = (Response) authMap.get(method).invoke(this, user, password);
            } catch (IllegalAccessException e) {
                continue;
            } catch (InvocationTargetException e) {
                continue;
            }
            if (response.code == 503 || response.code == 235) {
            // Already Authenticated or Successful
                authed = true;
                break;
            }
        }
        if (!authed)
            throw new SMTPAuthenticationException(response);
        return response;
    }
    public Response starttls(String server) {
        greet(server);
        if (!hasExtension("starttls"))
            throw new SMTPException("STARTTLS not supported by server.");
        Response response = doCommand("STARTTLS");
        if (response.code != 220)
            return response;
        try {
            socket = ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(
                    socket, socket.getInetAddress().getHostAddress(), socket.getPort(), true);
            ((SSLSocket) socket).setUseClientMode(true);
            ((SSLSocket) socket).startHandshake();
        } catch (IOException e) {
            throw new SMTPException(e);
        }
        read = null;
        write = null;
        heloResponse = null;
        ehloResponse = null;
        esmtpFeatures = null;
        esmtp = false;
        return response;
    }
    public Email email() {
        return new Email();
    }
    public Response sendMail(Email mail) {
        return sendMail(mail.getFromAddress(), mail.getAllAddresses().toArray(new String[0]), mail.toString());
    }
    public Response sendMail(String from, String[] to, String mail) {
        Response response = mail(from);
        if (response.code != 250)
            return response;
        for (String recipient : to)
            rcpt(recipient);
        return data(mail);
    }
    public void close() {
        try {
            if (read != null)
                read.close();
            if (write != null)
                write.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void sendEmail(String smtpServer, String senderEmail, String senderPassword, String senderName, String recipientName, String recipientEmail, String subject, String message, boolean debug) throws SMTPException{
        SMTP smtp = new SMTP(smtpServer);
        smtp.debug = debug;
        smtp.starttls(smtpServer);
        smtp.login(senderEmail, senderPassword, smtpServer);
        Email mail = smtp.email().from(senderName, senderEmail).to(recipientName, recipientEmail).subject(subject).body(message);
        smtp.sendMail(mail);
        smtp.close();
    }
}
