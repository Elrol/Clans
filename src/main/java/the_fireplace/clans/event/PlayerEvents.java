package the_fireplace.clans.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.commands.CommandClan;
import the_fireplace.clans.util.CapHelper;
import the_fireplace.clans.util.PlayerClanCapability;
import the_fireplace.clans.util.TextStyles;

public class PlayerEvents {
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        //noinspection ConstantConditions
        assert Clans.CLAN_DATA_CAP != null;
        if(!event.getPlayer().world.isRemote && event.getPlayer() instanceof EntityPlayerMP && event.getPlayer().getCapability(Clans.CLAN_DATA_CAP).isPresent()) {
            PlayerClanCapability c = CapHelper.getPlayerClanCapability(event.getPlayer());
            if ((c.getDefaultClan() != null && ClanCache.getClanById(c.getDefaultClan()) == null) || (c.getDefaultClan() == null && !ClanCache.getClansByPlayer(event.getPlayer().getUniqueID()).isEmpty()) || (c.getDefaultClan() != null && !ClanCache.getClansByPlayer(event.getPlayer().getUniqueID()).contains(ClanCache.getClanById(c.getDefaultClan()))))
                CommandClan.updateDefaultClan((EntityPlayerMP)event.getPlayer(), null);
        }
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        if(Clans.cfg.showDefaultClanInChat && event.getPlayer() != null) {
            PlayerClanCapability playerClanCap = CapHelper.getPlayerClanCapability(event.getPlayer());
            if(playerClanCap.getDefaultClan() != null) {
                NewClan playerDefaultClan = ClanCache.getClanById(playerClanCap.getDefaultClan());
                if(playerDefaultClan != null)
                    event.setComponent(new TextComponentString('<'+playerDefaultClan.getClanName()+"> ").setStyle(new Style().setColor(playerDefaultClan.getTextColor())).appendSibling(event.getComponent().setStyle(TextStyles.RESET)));
                else
                    CommandClan.updateDefaultClan(event.getPlayer(), null);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(CapHelper.getPlayerClanCapability(event.getPlayer()).getClaimWarning()) {
            CapHelper.getPlayerClanCapability(event.getPlayer()).setClaimWarning(false);
            Timer.prevChunkXs.remove(event.getPlayer());
            Timer.prevChunkZs.remove(event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerChangedDimensionEvent event) {
        if(CapHelper.getPlayerClanCapability(event.getPlayer()).getClaimWarning()) {
            CapHelper.getPlayerClanCapability(event.getPlayer()).setClaimWarning(false);
            Timer.prevChunkXs.remove(event.getPlayer());
            Timer.prevChunkZs.remove(event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        //noinspection ConstantConditions
        assert Clans.CLAN_DATA_CAP != null;
        if(!event.getPlayer().world.isRemote && event.getPlayer() instanceof EntityPlayerMP) {
            PlayerClanCapability c = CapHelper.getPlayerClanCapability(event.getPlayer());
            assert c != null;
            if ((c.getDefaultClan() != null && ClanCache.getClanById(c.getDefaultClan()) == null) || (c.getDefaultClan() == null && !ClanCache.getClansByPlayer(event.getPlayer().getUniqueID()).isEmpty()) || (c.getDefaultClan() != null && !ClanCache.getClansByPlayer(event.getPlayer().getUniqueID()).contains(ClanCache.getClanById(c.getDefaultClan()))))
                CommandClan.updateDefaultClan((EntityPlayerMP)event.getPlayer(), null);
        }
    }
}
