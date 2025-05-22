package org.fentanylsolutions.eyesintheshadows.client;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import org.fentanylsolutions.eyesintheshadows.EyesInTheShadows;

public class EyesRenderer extends RenderLiving {

    public EyesRenderer(ModelBase par1ModelBase, float parShadowSize) {
        super(par1ModelBase, parShadowSize);
        this.shadowSize = 0;
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return EyesInTheShadows.varInstanceClient.eyesTexture;
    }

    @Override
    public void doRender(EntityLivingBase p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
        super.doRender(p_76986_1_, p_76986_2_, p_76986_4_,  p_76986_6_, p_76986_8_, p_76986_9_);
    }

    @Override
    public void rotateCorpse(EntityLivingBase p_77043_1_, float p_77043_2_, float p_77043_3_, float p_77043_4_)
    {
        // Despite the name, this function rotates the opengl context even when the entity is alive
        // We want the eyes to render face to all players, except if an eye aggroes someoone
        // If we leave the original rotateCorpse and try to undo its stuff in the model, there will be jitter
    }
}
