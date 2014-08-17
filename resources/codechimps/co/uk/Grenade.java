package resources.codechimps.co.uk;

import org.bukkit.Effect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;

public class Grenade {
	
	/*
	 * Grenade Code
	 * 
	 */
	
	@EventHandler
	public void GrenadeEvent(ProjectileHitEvent event) { //Grenade
	    if (event.getEntity().getType() == EntityType.SNOWBALL) {
	        Snowball sb = (Snowball) event.getEntity();
	            if (sb.getShooter() instanceof Player) {
	            	//            createExplosion(X, Y, Z, Power (F), Fire (true/false), Break Blocks (true/false);
	                sb.getWorld().createExplosion(sb.getLocation().getX(), sb.getLocation().getY(), sb.getLocation().getZ(), 3F, false, false);
	                sb.getWorld().playEffect(sb.getLocation(), Effect.SMOKE, 200);
	            }
	    }
	}

}
