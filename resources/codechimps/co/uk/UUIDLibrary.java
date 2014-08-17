package resources.codechimps.co.uk;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
 
import net.minecraft.util.com.google.gson.Gson;
 
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
 
public class UUIDLibrary {
 
//Usage:
//public UUIDLibrary UUIDLib;
//UUIDLib.getNameFromUUID(uuid);
//UUIDLib.getUUIDFromName(name);
	
//Example Usage:
//public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
//if(cmd.getName().equalsIgnoreCase("getuuid"){
//if(args.length != 0){
//sender.sendMessage("Please specify a player.");
//}else if(args.length == 0){
//String uuid = UUIDLib.getUUIDFromName(args[0]);
//sender.sendMessage("UUID: " + uuid);
//}
//}else if(cmd.getName().equalsIgnoreCase("getname"){
//if(args.length != 0){
//sender.sendMessage("Please specify a UUID.");
//}else if(args.length == 0){
//String name = UUIDLib.getNameFromUUID(args[0]);
//sender.sendMessage("Name: " + name);
//}
 
private static Gson gson = new Gson();
 
public static String getNameFromUUID(String uuid) {
String name = null;
try {
URL url = new URL("[url]https://sessionserver.mojang.com/session/minecraft/profile/[/url]" + uuid);
URLConnection connection = url.openConnection();
Scanner jsonScanner = new Scanner(connection.getInputStream(), "UTF-8");
String json = jsonScanner.next();
JSONParser parser = new JSONParser();
Object obj = parser.parse(json);
name = (String) ((JSONObject) obj).get("name");
jsonScanner.close();
} catch (Exception ex) {
ex.printStackTrace();
}
return name;
}
 
public static String getUUIDFromName(String name) {
try {
ProfileData profC = new ProfileData(name);
String UUID = null;
for (int i = 1; i <= 100; i++) {
PlayerProfile[] result = post(new URL("[url]https://api.mojang.com/profiles/page/[/url]" + i), Proxy.NO_PROXY, gson.toJson(profC).getBytes());
if (result.length == 0) {
break;
}
UUID = result[0].getId();
}
return UUID;
} catch (Exception e) {
e.printStackTrace();
}
return null;
}
 
private static PlayerProfile[] post(URL url, Proxy proxy, byte[] bytes) throws IOException {
HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
connection.setRequestMethod("POST");
connection.setRequestProperty("Content-Type", "application/json");
connection.setDoInput(true);
connection.setDoOutput(true);
 
DataOutputStream out = new DataOutputStream(connection.getOutputStream());
out.write(bytes);
out.flush();
out.close();
 
BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
StringBuffer response = new StringBuffer();
String line;
while ((line = reader.readLine()) != null) {
response.append(line);
response.append('\r');
}
reader.close();
return gson.fromJson(response.toString(), SearchResult.class).getProfiles();
}
 
private static class PlayerProfile {
private String id;
 
public String getId() {
return id;
}
}
 
private static class SearchResult {
private PlayerProfile[] profiles;
 
public PlayerProfile[] getProfiles() {
return profiles;
}
}
 
private static class ProfileData {
 
@SuppressWarnings("unused")
private String name;
@SuppressWarnings("unused")
private String agent = "minecraft";
 
public ProfileData(String name) {
this.name = name;
}
}
}
