package com.brov3r.protegon.modules;

import com.brov3r.protegon.Main;
import com.brov3r.protegon.utils.ModuleUtils;
import zombie.characters.IsoGameCharacter;
import zombie.characters.IsoPlayer;
import zombie.characters.skills.PerkFactory;
import zombie.core.raknet.UdpConnection;
import zombie.network.PacketTypes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Skill
 */
public class SkillModule extends Module {
    /**
     * Constructs a new {@code Module} with the specified packet type, name, and description.
     */
    public SkillModule() {
        super(PacketTypes.PacketType.SyncXP, "skill", "Skill", "Blocking skill cheating");
    }

    /**
     * Handles the processing of a packet received from a {@link UdpConnection}.
     * This method must be implemented by subclasses to define the specific behavior
     * when a packet of the associated type is received.
     *
     * @param buffer     The {@link ByteBuffer} containing the packet data.
     * @param player     The {@link IsoPlayer} object.
     * @param connection The {@link UdpConnection} from which the packet originated.
     */
    @Override
    public void handlePacket(ByteBuffer buffer, IsoPlayer player, UdpConnection connection) {
        if (player == null) return;

        short playerID = buffer.getShort();

        ArrayList<IsoGameCharacter.PerkInfo> perkListSaved = new ArrayList<>(player.getPerkList());
        HashMap<PerkFactory.Perk, Float> savedPlayerXP = new HashMap<>(player.getXp().XPMap);

        try {
            player.getXp().load(buffer, 195);
        } catch (Exception e) {
            System.out.printf("[!] Skill - Failed to load player skills '%s': %s%n", player.getUsername(), e.getMessage());
        }

        double survivedHours = player.getHoursSurvived();
        int minHours = Main.getConfig().getInt(getConfigName() + ".minHours");
        int maxLevel = Main.getConfig().getInt(getConfigName() + ".maxLevel");

        ArrayList<IsoGameCharacter.PerkInfo> perkListNew = new ArrayList<>(player.getPerkList());

        if (perkListSaved.size() != perkListNew.size()) {
            System.out.printf("[!] Skill - Player '%s' perk list sizes are inconsistent%n", player.getUsername());
            return;
        }

        for (int index = 0; index < perkListSaved.size(); index++) {
            IsoGameCharacter.PerkInfo savedPerkInfo = perkListSaved.get(index);
            IsoGameCharacter.PerkInfo newPerkInfo = perkListNew.get(index);
            String perkID = newPerkInfo.perk.getId().toLowerCase();

            if (!savedPerkInfo.perk.equals(newPerkInfo.perk)) {
                System.out.printf("[!] Skill - Player '%s' has a mismatch between old and new skills at position %s%n", player.getUsername(), index);
                continue;
            }

            Float oldXPFloat = savedPlayerXP.get(savedPerkInfo.perk);
            if (oldXPFloat == null) continue;

            if (savedPerkInfo.level < newPerkInfo.level) {
                String levelChangeMessage = String.format("[#] Skill - Player '%s' has had his skill '%s' changed: Old level: %d, New level: %d. Hours survived: %.1f",
                        player.username,
                        perkID,
                        savedPerkInfo.level,
                        newPerkInfo.level,
                        survivedHours);
                System.out.println(levelChangeMessage);
            }

            if (perkID.equalsIgnoreCase("fitness") || perkID.equalsIgnoreCase("strength")) continue;

            if (newPerkInfo.level >= maxLevel && survivedHours < minHours) {
                System.out.printf("[?] Skill - Skill mismatch noted: %s, level: %s, hours survived: %.1f%n", perkID, newPerkInfo.level, survivedHours);

                ModuleUtils.punishPlayer(getConfigName(), connection);
                return;
            }
        }
    }
}