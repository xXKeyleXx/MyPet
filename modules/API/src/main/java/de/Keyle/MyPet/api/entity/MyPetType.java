/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2016 Keyle
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

package de.Keyle.MyPet.api.entity;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.entity.types.*;
import de.Keyle.MyPet.api.exceptions.MyPetTypeNotFoundException;

import java.util.LinkedList;
import java.util.List;

public enum MyPetType {
    Bat("BAT", 65, MyBat.class),
    Blaze("BLAZE", 61, MyBlaze.class),
    CaveSpider("CAVE_SPIDER", 59, MyCaveSpider.class),
    Chicken("CHICKEN", 93, MyChicken.class),
    Cow("COW", 92, MyCow.class),
    Creeper("CREEPER", 50, MyCreeper.class),
    EnderDragon("ENDER_DRAGON", 63, MyEnderDragon.class),
    Enderman("ENDERMAN", 58, MyEnderman.class),
    Endermite("ENDERMITE", 67, "1.8", MyEndermite.class),
    Ghast("GHAST", 56, MyGhast.class),
    Giant("GIANT", 53, MyGiant.class),
    Guardian("GUARDIAN", 68, "1.8", MyGuardian.class),
    Horse("HORSE", 100, MyHorse.class, "EntityHorse"),
    IronGolem("IRON_GOLEM", 99, MyIronGolem.class, "VillagerGolem"),
    MagmaCube("MAGMA_CUBE", 62, MyMagmaCube.class, "LavaSlime"),
    Mooshroom("MUSHROOM_COW", 96, MyMooshroom.class, "MushroomCow"),
    Ocelot("OCELOT", 98, MyOcelot.class, "Ozelot"),
    Pig("PIG", 90, MyPig.class),
    PigZombie("PIG_ZOMBIE", 57, MyPigZombie.class),
    PolarBear("POLAR_BEAR", 102, "1.10", MyPolarBear.class),
    Rabbit("RABBIT", 101, "1.8", MyRabbit.class),
    Sheep("SHEEP", 91, MySheep.class),
    Silverfish("SILVERFISH", 60, MySilverfish.class),
    Skeleton("SKELETON", 51, MySkeleton.class),
    Slime("SLIME", 55, MySlime.class),
    Snowman("SNOWMAN", 97, MySnowman.class, "SnowMan"),
    Spider("SPIDER", 52, MySpider.class),
    Squid("SQUID", 94, MySquid.class),
    Witch("WITCH", 66, MyWitch.class),
    Wither("WITHER", 64, MyWither.class, "WitherBoss"),
    Wolf("WOLF", 95, MyWolf.class),
    Villager("VILLAGER", 120, MyVillager.class),
    Zombie("ZOMBIE", 54, MyZombie.class);

    private String bukkitName;
    private String minecraftName;
    private String minecraftVersion = null;
    private int typeID;
    private Class<? extends MyPet> mypetClass;

    MyPetType(String typeName, int typeID, Class<? extends MyPet> mypetClass) {
        this.bukkitName = typeName;
        this.typeID = typeID;
        this.minecraftName = name();
        this.mypetClass = mypetClass;
    }

    MyPetType(String typeName, int typeID, String minecraftVersion, Class<? extends MyPet> mypetClass) {
        this(typeName, typeID, mypetClass);
        this.minecraftVersion = minecraftVersion;
    }

    MyPetType(String bukkitName, int typeID, Class<? extends MyPet> mypetClass, String minecraftName) {
        this(bukkitName, typeID, mypetClass);
        this.minecraftName = minecraftName;
    }

    public String getBukkitName() {
        return bukkitName;
    }

    public String getMinecraftName() {
        return minecraftName;
    }

    public int getTypeID() {
        return typeID;
    }

    public Class<? extends MyPet> getMyPetClass() {
        return mypetClass;
    }

    public boolean checkMinecraftVersion() {
        return minecraftVersion == null || MyPetApi.getCompatUtil().compareWithMinecraftVersion(this.minecraftVersion) >= 0;
    }

    public static List<MyPetType> all() {
        List<MyPetType> all = new LinkedList<>();
        for (MyPetType t : values()) {
            if (t.checkMinecraftVersion()) {
                all.add(t);
            }
        }
        return all;
    }

    public static MyPetType byEntityTypeName(String name) {
        return byEntityTypeName(name, true);
    }

    @Deprecated
    public static MyPetType byEntityTypeName(String name, boolean versionCheck) {
        for (MyPetType t : values()) {
            if (t.getBukkitName().equalsIgnoreCase(name)) {
                if (!versionCheck || t.checkMinecraftVersion()) {
                    return t;
                }
                break;
            }
        }
        throw new MyPetTypeNotFoundException(name);
    }

    public static MyPetType byName(String name) {
        return byName(name, true);
    }

    @Deprecated
    public static MyPetType byName(String name, boolean versionCheck) {
        for (MyPetType t : values()) {
            if (!versionCheck || t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        throw new MyPetTypeNotFoundException(name);
    }
}