/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2019 Keyle
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

package de.Keyle.MyPet.api.player;

import de.Keyle.MyPet.api.Util;

public class DonateCheck {
    public enum DonationRank {
        Creator("☣"),
        Donator("❤"),
        Translator("✈"),
        Developer("✪"),
        Helper("☘"),
        Premium("$"),
        None("");

        String defaultIcon;

        DonationRank(String defaultIcon) {
            this.defaultIcon = defaultIcon;
        }

        public String getDefaultIcon() {
            return defaultIcon;
        }
    }

    public static DonationRank getDonationRank(MyPetPlayer player) {
        try {
            // Check whether this player has donated or is a helper for the MyPet project
            // returns
            //   0 for nothing
            //   1 for donator
            //   2 for developer
            //   3 for translator
            //   4 for helper
            //   5 for creator
            //   6 for premium
            // no data will be saved on the server
            String check;
            if (player.getMojangUUID() != null) {
                check = "userid=" + player.getMojangUUID();
            } else {
                check = "username=" + player.getName();
            }
            String donation = Util.readUrlContent("http://particle.mypet-plugin.de/?" + check);
            switch (donation) {
                case "1":
                    return DonationRank.Donator;
                case "2":
                    return DonationRank.Developer;
                case "3":
                    return DonationRank.Translator;
                case "4":
                    return DonationRank.Helper;
                case "5":
                    return DonationRank.Creator;
                case "6":
                    return DonationRank.Premium;
            }
        } catch (Exception ignored) {
        }
        return DonationRank.None;
    }
}