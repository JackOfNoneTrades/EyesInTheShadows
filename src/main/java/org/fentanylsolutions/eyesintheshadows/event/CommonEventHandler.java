package org.fentanylsolutions.eyesintheshadows.event;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;
import org.fentanylsolutions.eyesintheshadows.aitasks.FleeEyes;
import org.fentanylsolutions.eyesintheshadows.entity.entities.EntityEyes;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CommonEventHandler {

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityCreature) {
            handleAITasks((EntityCreature) event.entity, event);
        }
    }

    private void handleAITasks(EntityCreature e, Event ev) {
        if (e instanceof EntityWolf) {
            // e.tasks.addTask(2, new EntityAIAttackOnCollide((EntityCreature) e, EntityEyes.class, 1.0D, false));
            // e.targetTasks.addTask(2, new EntityAINearestAttackableTarget((EntityCreature) e, EntityEyes.class, 0,
            // true));

            /////// e.targetTasks.addTask(2, new TargetEyesTamed((EntityTameable) e, EntityEyes.class, 0, true));
        }
        for (Class c : EyesInTheShadows.varInstanceCommon.entitiesAttackingEyesList) {
            if (e.getClass()
                .getCanonicalName()
                .equals(c.getCanonicalName())) {
                // e.targetTasks.addTask(2, new EntityAINearestAttackableTargetDormant(e, 2, true));

                // e.targetTasks.addTask(2, new EntityAINearestAttackableTarget(e, EntityEyes.class, 2, true));
                // e.targetTasks.addTask(4, new EntityAIAttackOnCollide(e, EntityEyes.class, 1.0D, true));

                e.tasks.addTask(4, new EntityAIAttackOnCollide(e, EntityEyes.class, 1.0D, true));
                e.targetTasks.addTask(2, new EntityAINearestAttackableTarget(e, EntityEyes.class, 0, false));
                if (e.getEntityAttribute(SharedMonsterAttributes.attackDamage) == null) {
                    e.getAttributeMap()
                        .registerAttribute(SharedMonsterAttributes.attackDamage);
                    e.getEntityAttribute(SharedMonsterAttributes.attackDamage)
                        .setBaseValue(1);
                }
            }
        }
        for (Class c : EyesInTheShadows.varInstanceCommon.entitiesFleeingEyesList) {
            if (e.getClass()
                .getCanonicalName()
                .equals(c.getCanonicalName())) {
                e.targetTasks.addTask(3, new FleeEyes(e, EntityEyes.class, 6.0F, 1.0D, 1.2D));
            }
        }
    }
}
