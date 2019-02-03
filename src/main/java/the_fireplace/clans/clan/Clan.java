package the_fireplace.clans.clan;

import com.google.common.collect.Maps;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import the_fireplace.clans.Clans;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Clan implements Serializable {
	private static final long serialVersionUID = 0x2ACA77AC;

	private String clanName, clanBanner;
	private String description = "This is a new clan.";
	private HashMap<UUID, EnumRank> members;
	private UUID clanId;
	private float homeX, homeY, homeZ;
	private boolean hasHome = false;
	private int homeDimension;
	private int claimCount = 0;
	private boolean isOpclan = false;
	private int rent = 0;

	public Clan(String clanName, UUID leader){
		this(clanName, leader, null);
	}

	public Clan(String clanName, UUID leader, @Nullable String banner){
		this.clanName = clanName;
		this.members = Maps.newHashMap();
		this.members.put(leader, EnumRank.LEADER);
		if(banner != null)
			this.clanBanner = banner;
		do{
			this.clanId = UUID.randomUUID();
		} while(!ClanDatabase.addClan(this.clanId, this));
		Clans.getPaymentHandler().ensureAccountExists(clanId);
		Clans.getPaymentHandler().addAmount(Clans.cfg.formClanBankAmount, clanId);
		ClanCache.purgePlayerCache(leader);
	}

	/**
	 * Generate OpClan
	 */
	Clan(){
		this.clanName = "Server";
		this.description = "Server Operator Clan";
		this.members = Maps.newHashMap();
		this.clanId = UUID.fromString("00000000-0000-0000-0000-000000000000");
		while(!ClanDatabase.addClan(this.clanId, this))
			this.clanId = UUID.randomUUID();
		this.isOpclan = true;
	}

	public HashMap<UUID, EnumRank> getMembers() {
		return members;
	}

	public HashMap<EntityPlayerMP, EnumRank> getOnlineMembers(MinecraftServer server, ICommandSender sender) {
		HashMap<EntityPlayerMP, EnumRank> online = Maps.newHashMap();
		if(isOpclan)
			return online;
		for(Map.Entry<UUID, EnumRank> member: getMembers().entrySet()) {
			EntityPlayerMP memberMP;
			try {
				memberMP = CommandBase.getPlayer(server, sender, member.getKey().toString());
			} catch(CommandException e) {
				continue;
			}
			online.put(memberMP, member.getValue());
		}
		return online;
	}

	public UUID getClanId() {
		return clanId;
	}

	public String getClanName() {
		return clanName;
	}

	public void setClanName(String clanName) {
		ClanCache.removeName(this.clanName);
		this.clanName = clanName;
		ClanCache.addName(this);
		ClanDatabase.save();
	}

	public String getClanBanner() {
		return clanBanner;
	}

	public void setClanBanner(String clanBanner) {
		if(isOpclan)
			return;
		ClanCache.removeBanner(this.clanBanner);
		ClanCache.addBanner(clanBanner);
		this.clanBanner = clanBanner;
		ClanDatabase.save();
	}

	public void setHome(BlockPos pos, int dimension) {
		if(isOpclan)
			return;
		this.homeX = pos.getX();
		this.homeY = pos.getY();
		this.homeZ = pos.getZ();
		this.hasHome = true;
		this.homeDimension = dimension;
		ClanDatabase.save();
	}

	public boolean hasHome() {
		return hasHome;
	}

	public void unsetHome() {
		hasHome = false;
		homeX = homeY = homeZ = 0;
		homeDimension = 0;
		//No need to save here because subClaimCount is always called after this.
	}

	public BlockPos getHome() {
		return new BlockPos(homeX, homeY, homeZ);
	}

	public int getHomeDim() {
		return homeDimension;
	}

	public int getClaimCount() {
		return claimCount;
	}

	public void addClaimCount() {
		claimCount++;
		ClanDatabase.save();
	}

	public void subClaimCount() {
		claimCount--;
		ClanDatabase.save();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		ClanDatabase.save();
	}

	public int getMemberCount(){
		return members.size();
	}

	public void addMember(UUID player) {
		if(isOpclan)
			return;
		this.members.put(player, EnumRank.MEMBER);
		ClanCache.purgePlayerCache(player);
		ClanDatabase.save();
	}

	public boolean removeMember(UUID player) {
		if(isOpclan)
			return false;
		boolean removed = this.members.remove(player) != null;
		if(removed) {
			ClanCache.purgePlayerCache(player);
			ClanDatabase.save();
		}
		return removed;
	}

	public boolean demoteMember(UUID player) {
		if(isOpclan || !members.containsKey(player))
			return false;
		else {
			if(members.get(player) == EnumRank.ADMIN){
				members.put(player, EnumRank.MEMBER);
				ClanCache.updateRank(player, EnumRank.MEMBER);
				ClanDatabase.save();
				return true;
			} else return false;
		}
	}

	public boolean promoteMember(UUID player) {
		if(isOpclan || !members.containsKey(player))
			return false;
		else {
			if(members.get(player) == EnumRank.ADMIN) {
				if(!Clans.cfg.multipleClanLeaders) {
					UUID leader = null;
					for(UUID member: members.keySet())
						if(members.get(member) == EnumRank.LEADER) {
							leader = member;
							break;
						}
					if(leader != null) {
						members.put(leader, EnumRank.ADMIN);
						ClanCache.updateRank(leader, EnumRank.ADMIN);
					}
				}
				members.put(player, EnumRank.LEADER);
				ClanCache.updateRank(player, EnumRank.LEADER);
				ClanDatabase.save();
				return true;
			} else if(members.get(player) == EnumRank.MEMBER) {
				members.put(player, EnumRank.ADMIN);
				ClanCache.updateRank(player, EnumRank.ADMIN);
				ClanDatabase.save();
				return true;
			} return false;
		}
	}

	public boolean isOpclan(){
		return isOpclan;
	}

	public int getRent() {
		return rent;
	}

	public void setRent(int rent) {
		this.rent = rent;
	}
}
