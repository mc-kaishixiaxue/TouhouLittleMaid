package com.github.tartaricacid.touhoulittlemaid.client.event;

import com.github.tartaricacid.touhoulittlemaid.client.sound.data.MaidSoundInstance;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class MaidSoundFreqEvent {
    @SubscribeEvent
    public static void onPlaySoundEvent(PlaySoundEvent event) {
        if (event.getSound() instanceof MaidSoundInstance maidSoundInstance) {
            EntityMaid maid = maidSoundInstance.getMaid();
            if (maid == null) {
                event.setSound(null);
                return;
            }
            double soundFrequency = maid.getConfigManager().getSoundFreq();
            if (soundFrequency < 1 && !maidSoundInstance.isTestSound()) {
                if (Math.random() > soundFrequency) {
                    event.setSound(null);
                }
            }
        }
    }
}