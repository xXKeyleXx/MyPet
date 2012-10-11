/*
 * Copyright (C) 2011-2012 Keyle
 *
 * This file is part of MyPet
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
 * along with MyPet. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.entity.types.sheep;

import de.Keyle.MyPet.entity.pathfinder.PathfinderGoalControl;
import de.Keyle.MyPet.entity.pathfinder.PathfinderGoalFollowOwner;
import de.Keyle.MyPet.entity.types.EntityMyPet;
import de.Keyle.MyPet.entity.types.MyPet;
import net.minecraft.server.*;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class EntityMySheep extends EntityMyPet
{
    public EntityMySheep(World world, MyPet myPet)
    {
        super(world, myPet);
        this.texture = "/mob/sheep.png";
        this.a(0.9F, 1.3F);
        this.bw = 0.25F;
        this.getNavigation().a(true);

        PathfinderGoalControl Control = new PathfinderGoalControl(myPet, 0.38F);

        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, this.d);
        this.goalSelector.a(3, Control);
        this.goalSelector.a(4, new PathfinderGoalPanic(this, 0.38F));
        this.goalSelector.a(5, new PathfinderGoalFollowOwner(this, this.bw, 5.0F, 2.0F, Control));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
    }

    @Override
    public void setMyPet(MyPet myPet)
    {
        if (myPet != null)
        {
            this.myPet = myPet;
            isMyPet = true;
            if (!isTamed())
            {
                this.setTamed(true);
                this.setPathEntity(null);
                this.setSitting(myPet.isSitting());
                this.setHealth(myPet.getHealth() >= getMaxHealth() ? getMaxHealth() : myPet.getHealth());
                this.setOwnerName(myPet.getOwner().getName());
                //this.setColor(((MySheep) myPet).getColor());
            }
        }
    }

    public int getMaxHealth()
    {
        return MySheep.getStartHP() + (isTamed() && myPet.getSkillSystem().hasSkill("HP") ? myPet.getSkillSystem().getSkill("HP").getLevel() : 0);
    }

    /**
     * Is called when player rightclicks this MyPet
     * return:
     * true: there was a reaction on rightclick
     * false: no reaction on rightclick
     */
    public boolean c(EntityHuman entityhuman)
    {
        super.c(entityhuman);

        ItemStack itemStack = entityhuman.inventory.getItemInHand();

        if (itemStack != null && itemStack.id == org.bukkit.Material.WHEAT.getId())
        {
            if (getHealth() < getMaxHealth())
            {
                if (!entityhuman.abilities.canInstantlyBuild)
                {
                    --itemStack.count;
                }
                this.heal(3, RegainReason.EATING);
                if (itemStack.count <= 0)
                {
                    entityhuman.inventory.setItem(entityhuman.inventory.itemInHandIndex, null);
                }
                this.e(true);
                return true;
            }
        }
        else if (itemStack != null && itemStack.id == 351)
        {
            if (itemStack.getData() <= 15)
            {
                ((MySheep) myPet).setColor(15 - itemStack.getData());
                return true;
            }
        }
        else if (entityhuman.name.equalsIgnoreCase(this.getOwnerName()) && !this.world.isStatic)
        {
            this.d.a(!this.isSitting());
            this.bu = false;
            this.setPathEntity(null);
        }

        return false;
    }

    public boolean k(Entity entity)
    {
        int damage = 1 + (isMyPet && myPet.getSkillSystem().hasSkill("Damage") ? myPet.getSkillSystem().getSkill("Damage").getLevel() : 0);

        return entity.damageEntity(DamageSource.mobAttack(this), damage);
    }

    @Override
    public org.bukkit.entity.Entity getBukkitEntity()
    {
        if (this.bukkitEntity == null)
        {
            this.bukkitEntity = new CraftMySheep(this.world.getServer(), this);
        }
        return this.bukkitEntity;
    }

    //Unused changed Vanilla Methods ---------------------------------------------------------------------------------------

    @Override
    protected void bd()
    {
        this.datawatcher.watch(18, this.getHealth());
    }

    protected void a()
    {
        super.a();
        this.datawatcher.a(18, this.getHealth());
    }

    // Vanilla Methods

    /**
     * Returns the default sound of the MyPet
     */
    protected String aQ()
    {
        return "mob.sheep";
    }

    /**
     * Returns the sound that is played when the MyPet get hurt
     */
    @Override
    protected String aR()
    {
        return "mob.sheep";
    }

    /**
     * Returns the sound that is played when the MyPet dies
     */
    @Override
    protected String aS()
    {
        return "mob.sheep";
    }

    public int getColor()
    {
        return this.datawatcher.getByte(16) & 15;
    }

    public void setColor(int i)
    {
        byte b0 = this.datawatcher.getByte(16);

        this.datawatcher.watch(16, (byte) (b0 & 240 | i & 15));
    }
}