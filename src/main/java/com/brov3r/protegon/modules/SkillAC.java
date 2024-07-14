package com.brov3r.protegon.modules;

import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.AntiCheatUtils;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.skills.PerkFactory;
import zombie.core.raknet.UdpConnection;
import zombie.network.ZomboidNetData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Handles skill-related packets for anti-cheat purposes.
 */
public class SkillAC {
    /**
     * Handles the skill packet for anti-cheat actions.
     *
     * @param packet           The ZomboidNetData packet containing skill information.
     * @param player           The IsoPlayer who sent the packet.
     * @param playerConnection The UdpConnection of the player.
     */
    public static void handlePacket(ZomboidNetData packet, IsoPlayer player, UdpConnection playerConnection) {
        if (!Main.getConfig().getBoolean("skillAntiCheat.isEnable") || AntiCheatUtils.isPlayerHasRights(player)) return;

        ByteBuffer byteBuffer = packet.buffer;
        short playerID = byteBuffer.getShort();

        ArrayList<IsoGameCharacter.PerkInfo> perkListSaved = new ArrayList<>(player.getPerkList());
        HashMap<PerkFactory.Perk, Float> savedPlayerXP = new HashMap<>(player.getXp().XPMap);

        try {
            player.getXp().load(byteBuffer, 195);
        } catch (Exception e) {
            System.out.printf("[!] Не удалось загрузить навыки игрока '%s': %s%n", player.getUsername(), e.getMessage());
        }

        double survivedHours = player.getHoursSurvived();
        int minHours = Main.getConfig().getInt("skillAntiCheat.minHours");
        int maxLevel = Main.getConfig().getInt("skillAntiCheat.maxLevel");

        ArrayList<IsoGameCharacter.PerkInfo> perkListNew = new ArrayList<>(player.getPerkList());

        if (perkListSaved.size() != perkListNew.size()) {
            System.out.printf("[!] AC - Player '%s' perk list sizes are inconsistent%n", player.getUsername());
            return;
        }

        for (int index = 0; index < perkListSaved.size(); index++) {
            IsoGameCharacter.PerkInfo savedPerkInfo = perkListSaved.get(index);
            IsoGameCharacter.PerkInfo newPerkInfo = perkListNew.get(index);
            String perkID = newPerkInfo.perk.getId().toLowerCase();

            if (!savedPerkInfo.perk.equals(newPerkInfo.perk)) {
                System.out.printf("[!] AC - Player '%s' has a mismatch between old and new skills at position %s%n", player.getUsername(), index);
                continue;
            }

            Float oldXPFloat = savedPlayerXP.get(savedPerkInfo.perk);
            if (oldXPFloat == null) continue;

            if (savedPerkInfo.level < newPerkInfo.level) {
                String levelChangeMessage = String.format("[#] AC - Player '%s' has had his skill '%s' changed: Old level: %d, New level: %d. Hours survived: %.1f",
                        player.username,
                        perkID,
                        savedPerkInfo.level,
                        newPerkInfo.level,
                        survivedHours);
                System.out.println(levelChangeMessage);
            }

            if (perkID.equalsIgnoreCase("fitness") || perkID.equalsIgnoreCase("strength")) continue;

            if (newPerkInfo.level >= maxLevel && survivedHours < minHours) {
                String punishText = Main.getConfig().getString("skillAntiCheat.punishText")
                        .replace("<SKILL>", perkID)
                        .replace("<LEVEL>", String.valueOf(newPerkInfo.level))
                        .replace("<HOURS>", String.format("%.1f", survivedHours));

                AntiCheatUtils.punishPlayer(
                        Main.getConfig().getInt("skillAntiCheat.punishType"),
                        player,
                        punishText);
                return;
            }
        }
    }
}