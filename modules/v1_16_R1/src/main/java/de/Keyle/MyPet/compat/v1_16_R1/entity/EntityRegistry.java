/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2020 Keyle
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

package de.Keyle.MyPet.compat.v1_16_R1.entity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.Util;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.MyPetMinecraftEntity;
import de.Keyle.MyPet.api.entity.MyPetType;
import de.Keyle.MyPet.api.util.Compat;
import de.Keyle.MyPet.api.util.ReflectionUtil;
import lombok.SneakyThrows;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Compat("v1_16_R1")
public class EntityRegistry extends de.Keyle.MyPet.api.entity.EntityRegistry {

    BiMap<MyPetType, Class<? extends EntityMyPet>> entityClasses = HashBiMap.create();
    @SuppressWarnings("rawtypes")
    Map<MyPetType, EntityTypes> entityTypes = new HashMap<>();

    @SuppressWarnings("unchecked")
    protected void registerEntityType(MyPetType petType, String key, RegistryBlocks<EntityTypes<?>> entityRegistry) {
        EntitySize size = entityRegistry.get(new MinecraftKey(key.toLowerCase())).l();
        entityTypes.put(petType, IRegistry.a(entityRegistry, "mypet_" + key.toLowerCase(), EntityTypes.Builder.a(EnumCreatureType.CREATURE).b().a().a(size.width, size.height).a(key)));
        registerDefaultAttributes(entityTypes.get(petType), (EntityTypes<? extends EntityLiving>) ReflectionUtil.getFieldValue(EntityTypes.class, null, ("" + petType.getTypeID()).toUpperCase()));
        overwriteEntityID(entityTypes.get(petType), getEntityTypeId(petType, entityRegistry), entityRegistry);
    }

    @SneakyThrows
    public static void registerDefaultAttributes(EntityTypes<? extends EntityLiving> customType, EntityTypes<? extends EntityLiving> rootType) {
        MyAttributeDefaults.registerCustomEntityTypes(customType, rootType);
    }

    @SuppressWarnings("unchecked")
    protected void registerEntity(MyPetType type, RegistryBlocks<EntityTypes<?>> entityRegistry) {
        Class<? extends EntityMyPet> entityClass = ReflectionUtil.getClass("de.Keyle.MyPet.compat.v1_16_R1.entity.types.EntityMy" + type.name());
        entityClasses.put(type, entityClass);
        String key = type.getTypeID().toString();
        registerEntityType(type, key, entityRegistry);
    }

    public MyPetType getMyPetType(Class<? extends EntityMyPet> clazz) {
        return entityClasses.inverse().get(clazz);
    }

    @Override
    public MyPetMinecraftEntity createMinecraftEntity(MyPet pet, org.bukkit.World bukkitWorld) {
        EntityMyPet petEntity = null;
        Class<? extends MyPetMinecraftEntity> entityClass = entityClasses.get(pet.getPetType());
        World world = ((CraftWorld) bukkitWorld).getHandle();

        try {
            Constructor<?> ctor = entityClass.getConstructor(World.class, MyPet.class);
            Object obj = ctor.newInstance(world, pet);
            if (obj instanceof EntityMyPet) {
                petEntity = (EntityMyPet) obj;
            }
        } catch (Exception e) {
            MyPetApi.getLogger().info(ChatColor.RED + Util.getClassName(entityClass) + "(" + pet.getPetType() + ") is no valid MyPet(Entity)!");
            e.printStackTrace();
        }

        return petEntity;
    }

    @Override
    public boolean spawnMinecraftEntity(MyPetMinecraftEntity entity, org.bukkit.World bukkitWorld) {
        if (entity != null) {
            World world = ((CraftWorld) bukkitWorld).getHandle();
            return world.addEntity(((EntityMyPet) entity), CreatureSpawnEvent.SpawnReason.CUSTOM);
        }
        return false;
    }

    @Override
    public void registerEntityTypes() {
        RegistryBlocks<EntityTypes<?>> entityRegistry = getRegistry(IRegistry.ENTITY_TYPE);
        for (MyPetType type : MyPetType.all()) {
            registerEntity(type, entityRegistry);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getEntityType(MyPetType petType) {
        return (T) this.entityTypes.get(petType);
    }

    @Override
    public void unregisterEntityTypes() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public RegistryBlocks<EntityTypes<?>> getRegistry(RegistryBlocks registryMaterials) {
        if (!registryMaterials.getClass().getName().equals(RegistryBlocks.class.getName())) {
            MyPetApi.getLogger().info("Custom entity registry found: " + registryMaterials.getClass().getName());
            for (Field field : registryMaterials.getClass().getDeclaredFields()) {
                if (field.getType() == RegistryMaterials.class) {
                    field.setAccessible(true);
                    try {
                        RegistryBlocks<EntityTypes<?>> reg = (RegistryBlocks<EntityTypes<?>>) field.get(registryMaterials);

                        if (!reg.getClass().getName().equals(RegistryBlocks.class.getName())) {
                            reg = getRegistry(reg);
                        }

                        return reg;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return registryMaterials;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void overwriteEntityID(EntityTypes types, int id, RegistryBlocks<EntityTypes<?>> entityRegistry) {
        RegistryID<EntityTypes> registryID = (RegistryID<EntityTypes>) ReflectionUtil.getFieldValue(RegistryMaterials.class, entityRegistry, "b");
        int[] RegistryID_c = (int[]) ReflectionUtil.getFieldValue(RegistryID.class, registryID, "c");
        Object[] RegistryID_b = (Object[]) ReflectionUtil.getFieldValue(RegistryID.class, registryID, "b");
        int RegistryID_d = (MathHelper.g(System.identityHashCode(types)) & 2147483647) % RegistryID_b.length;
        int bVal = this.RegistryID_b(RegistryID_b, types, RegistryID_d);
        RegistryID_c[bVal] = id;
    }

    private int RegistryID_b(Object[] b, Object k0, int i) {
        int j;

        for (j = i; j < b.length; ++j) {
            if (b[j] == k0) {
                return j;
            } else if (b[j] == null) {
                return -1;
            }
        }
        for (j = 0; j < i; ++j) {
            if (b[j] == k0) {
                return j;
            } else if (b[j] == null) {
                return -1;
            }
        }
        return -1;
    }

    protected int getEntityTypeId(MyPetType type, RegistryBlocks<EntityTypes<?>> entityRegistry) {
        EntityTypes<?> types = entityRegistry.get(new MinecraftKey(type.getTypeID().toString()));
        return entityRegistry.a(types);
    }
}