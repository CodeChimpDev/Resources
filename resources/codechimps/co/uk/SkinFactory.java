package resources.codechimps.co.uk;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
 
import net.minecraft.server.v1_7_R3.Packet;
import net.minecraft.server.v1_7_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
 
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
 
public class SkinFactory {
 
	//Usage:
	//public SkinFactory factory;
	//factory.setSkin(this, player, "player_name_of_skin_change");
	
	//Example Usage:
	//public SkinFactory factory;
	//factory.setSkin(this, "CodeChimp", "md_5");
	
    public static void setSkin(Plugin plugin, final Player p, final String toSkin) {
        new BukkitRunnable() {
 
            @Override
            public void run() {
                try {
                    Packet packet = new PacketPlayOutNamedEntitySpawn(((CraftPlayer) p).getHandle());
 
                    Field gameProfileField = PacketPlayOutNamedEntitySpawn.class.getDeclaredField("b");
                    gameProfileField.setAccessible(true);
 
                    @SuppressWarnings("deprecation")
                    GameProfile profile = new GameProfile(Bukkit.getOfflinePlayer(p.getName()).getUniqueId(), p.getName());
                    fixSkin(profile, toSkin);
 
                    gameProfileField.set(packet, profile);
 
                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        if (pl.equals(p)) {
                            continue;
                        }
                        ((CraftPlayer) pl).getHandle().playerConnection.sendPacket(packet);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }
 
    @SuppressWarnings({ "deprecation", "resource" })
    private static void fixSkin(GameProfile profile, String skinOwner) {
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + Bukkit.getOfflinePlayer(skinOwner).getUniqueId().toString().replace("-", ""));
            URLConnection uc = url.openConnection();
 
            // Parse it
            Scanner scanner = new Scanner(uc.getInputStream(), "UTF-8").useDelimiter("\\A");
            String json = scanner.next();
 
            JSONArray properties = (JSONArray) ((JSONObject) new JSONParser().parse(json)).get("properties");
            for (int i = 0; i < properties.size(); i++) {
                JSONObject property = (JSONObject) properties.get(i);
                String name = (String) property.get("name");
                String value = (String) property.get("value");
                String signature = property.containsKey("signature") ? (String) property.get("signature") : null;
                if (signature != null) {
                    profile.getProperties().put(name, new Property(name, value, signature));
                } else {
                    profile.getProperties().put(name, new Property(value, name));
                }
            }
        } catch (Exception e) {
        }
    }
}
