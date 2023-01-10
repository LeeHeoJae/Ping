package leeheojae.ping;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;

public class Ping extends JavaPlugin implements Listener {
    //우 좌
    HashSet<Player> set=new HashSet<Player>();

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this,this);
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event){
        Player player= event.getPlayer();
        if(!player.isSneaking()) return;
        if(event.hasItem()) return;
        if(this.set.contains(player)){
            if(event.getAction()!=Action.LEFT_CLICK_BLOCK) return;
            this.set.remove(player);
            event.setCancelled(true);
            Block block=event.getClickedBlock();
            Location location=new Location(block.getWorld(),block.getX(),block.getY(),block.getZ());
            Entity entity=player.getWorld().spawnEntity(location,EntityType.SHULKER);
            ((LivingEntity)entity).addPotionEffects(new HashSet<PotionEffect>(){{
                add(new PotionEffect(PotionEffectType.GLOWING,40,1,true,false));
                add(new PotionEffect(PotionEffectType.INVISIBILITY,40,1,true,false));
            }});
            ((LivingEntity) entity).setAI(false);
            entity.setGravity(false);
            entity.setSilent(true);
            Bukkit.getScheduler().scheduleSyncDelayedTask(this,new KillEntityTask(entity),40);
        }
        else{//우
            if(event.getAction()!=Action.RIGHT_CLICK_BLOCK&&event.getAction()!=Action.RIGHT_CLICK_AIR) return;
            this.set.add(player);
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, new DeleteTask(this.set,player),10);
        }

    }

    @EventHandler
    public void onClick(PlayerInteractEntityEvent event){ //우클릭
        Player player= event.getPlayer();
        if(!player.isSneaking()) return;
        if(!player.getInventory().getItemInMainHand().getType().isEmpty()) return;
        this.set.add(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(this,new DeleteTask(this.set,player),10);
    }

    @EventHandler
    public void onClick(EntityDamageByEntityEvent event){ //좌클릭
        if(event.getDamager().getType()!=EntityType.PLAYER) return;
        Player player= ((Player) event.getDamager());
        if(!this.set.contains(player)) return;
        if(!player.isSneaking()) return;
        event.setCancelled(true);
        this.set.remove(player);
        Entity entity=event.getEntity();
        ((LivingEntity)entity).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,100,1,true,false));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player= event.getPlayer();
        this.set.remove(player);
    }
}

class DeleteTask implements Runnable {
    private HashSet<Player> set;
    private final Player player;
    DeleteTask(HashSet<Player> set,Player player){
        this.set=set;
        this.player=player;
    }

    @Override
    public void run() {
        if(!this.set.contains(this.player)) return;
        this.set.remove(this.player);
    }
}
class KillEntityTask implements Runnable{
    private Entity entity;
    KillEntityTask(Entity entity){
        this.entity=entity;
    }
    @Override
    public void run() {
        this.entity.remove();
    }
}