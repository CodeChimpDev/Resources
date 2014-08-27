package resources.codechimps.co.uk;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;

public class ProjectileLand {
	
	@SuppressWarnings("deprecation")
	public void onProjectileLand(ProjectileHitEvent event){
		//Gets EXACT location that the projectile landed
		BlockIterator iterator = new BlockIterator(event.getEntity().getWorld(), event.getEntity().getLocation().toVector(), event.getEntity().getVelocity().normalize(), 0.0D, 4);
		
		Block hitBlock = null;
		
		while (iterator.hasNext()) {
			hitBlock = iterator.next();
			
		    //Checks to see if the block that the projectile hit was a Snow Block 
			if (hitBlock.getType() == Material.SNOW_BLOCK) {
				//Plays an effect at the location
				hitBlock.getWorld().playEffect(hitBlock.getLocation(), Effect.STEP_SOUND, hitBlock.getTypeId());
				//As Bukkit has no method to remove a block, we'll just set it to air
				hitBlock.setType(Material.AIR);
				//Drops a Baked Potato at the block that was removed. Essentially replacing a Snow Block with the floating Baked Potato item
				hitBlock.getWorld().dropItemNaturally(hitBlock.getLocation(), new ItemStack(Material.BAKED_POTATO, 1));
			}
		}
	}

}
