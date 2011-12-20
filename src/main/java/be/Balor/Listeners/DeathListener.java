/************************************************************************
 * This file is part of DeathTP+.									
 ************************************************************************/
package be.Balor.Listeners;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

import be.Balor.Workers.LocaleWorker;
import be.Balor.Workers.TombWorker;
import be.Balor.bukkit.Tomb.Tomb;
import be.Balor.bukkit.Tomb.TombPlugin;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;

/**
 * @author lonelydime (some modifications by Balor)
 * 
 */
public class DeathListener extends EntityListener {
	protected HashMap<String, String> lastPlayerDmg = new HashMap<String, String>();
	protected String beforeDamage = "";
	protected TombWorker worker = TombWorker.getInstance();

        @Override
	public void onEntityDeath(EntityDeathEvent event) {
		beforeDamage = "";
		try {
			if (event.getEntity() instanceof Player) {
				Player player = (Player) event.getEntity();
				if (worker.hasTomb(player.getName())) {
                                    
                                        if(!lastPlayerDmg.containsKey(player))
                                            return; //no damage recorded.  just get out of here and don't bother.
					
                                        String damagetype = lastPlayerDmg.get(player.getName());
					String[] howtheydied;

					howtheydied = damagetype.split(":");
					Tomb tomb = worker.getTomb(player.getName());
					String signtext;

					if (howtheydied[0].equals("PVP"))
						signtext = LocaleWorker.getInstance().getPvpLocale(howtheydied[2]);
					else
						signtext = LocaleWorker.getInstance().getLocale(
								howtheydied[0].toLowerCase());
					int deathLimit = worker.getConfig().getInt("maxDeaths", 0);
					tomb.addDeath();
					if (deathLimit != 0 && (tomb.getDeaths() % deathLimit) == 0) {
						tomb.resetTombBlocks();
						player.sendMessage(worker.graveDigger
								+ "You've reached the number of deaths before tomb reset.("
								+ ChatColor.DARK_RED + deathLimit + ChatColor.WHITE
								+ ") All your tombs are now destroyed.");
					} else {
						tomb.setReason(signtext);
						tomb.setDeathLoc(player.getLocation());
						tomb.updateDeath();
					}
				}

			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {

		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (worker.hasTomb(player.getName()))
				lastDamageDone(player, event);
		}
	}

	/**
	 * Process the damage event, used when the player die.
	 * 
	 * @param player
	 * @param event
	 */
	public void lastDamageDone(Player player, EntityDamageEvent event) {
		String lastdamage = event.getCause().name();
                
		if (event instanceof EntityDamageByEntityEvent) {
                    
                        EntityDamageByEntityEvent mobevent = (EntityDamageByEntityEvent) event;
			Entity attacker = mobevent.getDamager();
                                                
                        if (attacker instanceof Projectile) {
                                Projectile p = (Projectile) attacker;
                                if(p.getShooter() == null)
                                    lastdamage = attacker.toString();
                                else
                                    lastdamage = p.getShooter().toString();
			}
                        
                        else if (attacker instanceof Player) {
				Player pvper = (Player) attacker;
				String usingitem = pvper.getItemInHand().getType().name();
				if (usingitem == "AIR") {
					usingitem = "fist";
				}
				usingitem = usingitem.toLowerCase();
				usingitem = usingitem.replace("_", " ");
				lastdamage = "PVP:" + usingitem + ":" + pvper.getName();
			}
                        
                        else if (attacker instanceof Wolf) {
                                lastdamage = "CraftWolf";
                        }
                        
                        else if (attacker instanceof LivingEntity) {
                                lastdamage = attacker.toString();
                        }
		}
                
                if(lastdamage.contains("Craft")) {
//                    TombPlugin.slog(player, "Before regex: " + lastdamage);
                    //(?<=Craft)\S*
                    Pattern p = Pattern.compile("(?<=Craft)\\S*");
                    Matcher m = p.matcher(lastdamage);
                    if(m.find()){
                        lastdamage = m.group();
                    }
//                    TombPlugin.slog(player, "After regex:" + lastdamage);
                }
                
                lastPlayerDmg.put(player.getName(), lastdamage);
		beforeDamage = lastdamage;
                //TombPlugin.slog(player, "registered: " + lastdamage);
	}
}