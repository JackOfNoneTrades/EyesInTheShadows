package org.fentanylsolutions.eyesintheshadows.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntitySenses;

import org.fentanylsolutions.eyesintheshadows.util.TraceUtil;

public class EyeSenses extends EntitySenses {

    EntityLiving entityObj;
    /** Cache of entities which we can see */
    List<Entity> seenEntities = new ArrayList();
    /** Cache of entities which we cannot see */
    List<net.minecraft.entity.Entity> unseenEntities = new ArrayList();

    public EyeSenses(EntityLiving p_i1672_1_) {
        super(p_i1672_1_);
        this.entityObj = p_i1672_1_;
    }

    @Override
    public void clearSensingCache() {
        seenEntities.clear();
        unseenEntities.clear();
    }

    @Override
    public boolean canSee(Entity p_75522_1_) {
        if (seenEntities.contains(p_75522_1_)) {
            return true;
        } else if (unseenEntities.contains(p_75522_1_)) {
            return false;
        } else {
            entityObj.worldObj.theProfiler.startSection("canSeeEyeSenses");
            boolean flag = TraceUtil.canEntityBeSeenIgnoreWithoutBoundingBox(entityObj, p_75522_1_); // entityObj.canEntityBeSeen(p_75522_1_);
            entityObj.worldObj.theProfiler.endSection();

            if (flag) {
                seenEntities.add(p_75522_1_);
            } else {
                unseenEntities.add(p_75522_1_);
            }

            return flag;
        }
    }
}
