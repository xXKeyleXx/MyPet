/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2018 Keyle
 * MyPet is licensed under the GNU Lesser General Public License.
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.util.hooks;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.PlayerExperienceGainEvent;
import com.sucy.skill.api.event.PlayerExperienceLostEvent;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.Configuration;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.player.MyPetPlayer;
import de.Keyle.MyPet.api.util.ReflectionUtil;
import de.Keyle.MyPet.api.util.hooks.PluginHook;
import de.Keyle.MyPet.api.util.hooks.PluginHookName;
import de.Keyle.MyPet.api.util.hooks.types.PlayerVersusPlayerHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;

@PluginHookName("SkillAPI")
public class SkillApiHook implements PluginHook, PlayerVersusPlayerHook {

    boolean hasFriendly = false;

    @Override
    public boolean onEnable() {
        Bukkit.getPluginManager().registerEvents(this, MyPetApi.getPlugin());
        try {
            //noinspection unchecked
            ReflectionUtil
                    .getClass("com.sucy.skill.data.GroupSettings")
                    .getDeclaredMethod("isFriendly");
            hasFriendly = true;
        } catch (Exception ignored) {
        }
        return true;
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void on(PlayerExperienceGainEvent event) {
        if (Configuration.Hooks.SkillAPI.GRANT_EXP) {
            Player player = event.getPlayerData().getPlayer();
            if (MyPetApi.getPlayerManager().isMyPetPlayer(player)) {
                MyPetPlayer petPlayer = MyPetApi.getPlayerManager().getMyPetPlayer(player);
                if (petPlayer.hasMyPet()) {
                    MyPet myPet = petPlayer.getMyPet();
                    if (Configuration.Skilltree.PREVENT_LEVELLING_WITHOUT_SKILLTREE && myPet.getSkilltree() == null) {
                        if (!myPet.autoAssignSkilltree()) {
                            return;
                        }
                    }
                    myPet.getExperience().addExp(event.getExp() * Configuration.Hooks.SkillAPI.EXP_PERCENT / 100);
                }
            }
        }
    }

    @EventHandler
    public void on(PlayerExperienceLostEvent event) {
        if (Configuration.Hooks.SkillAPI.GRANT_EXP) {
            Player player = event.getPlayerData().getPlayer();
            if (MyPetApi.getPlayerManager().isMyPetPlayer(player)) {
                MyPetPlayer petPlayer = MyPetApi.getPlayerManager().getMyPetPlayer(player);
                if (petPlayer.hasMyPet()) {
                    MyPet myPet = petPlayer.getMyPet();
                    if (Configuration.Skilltree.PREVENT_LEVELLING_WITHOUT_SKILLTREE && myPet.getSkilltree() == null) {
                        if (!myPet.autoAssignSkilltree()) {
                            return;
                        }
                    }
                    //TODO -> removeExp
                    myPet.getExperience().removeCurrentExp(event.getExp() * Configuration.Hooks.SkillAPI.EXP_PERCENT / 100);
                }
            }
        }
    }

    @Override
    public boolean canHurt(Player attacker, Player defender) {
        if (!hasFriendly || !SkillAPI.getSettings().isWorldEnabled(defender.getWorld())) {
            return true;
        }

        final PlayerData attackerData = SkillAPI.getPlayerData(attacker);
        final PlayerData defenderData = SkillAPI.getPlayerData(defender);

        for (final String group : SkillAPI.getGroups()) {
            final boolean friendly = SkillAPI.getSettings().getGroupSettings(group).isFriendly();
            if (friendly) {
                final PlayerClass attackerClass = attackerData.getClass(group);
                final PlayerClass defenderClass = defenderData.getClass(group);

                if (defenderClass != null && attackerClass != null) {
                    if (attackerClass.getData().getRoot() == defenderClass.getData().getRoot()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}