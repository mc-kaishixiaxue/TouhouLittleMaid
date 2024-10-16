package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.advancements.maid.MaidEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.init.InitTrigger;
import com.github.tartaricacid.touhoulittlemaid.util.MaidRayTraceHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ItemCamera extends Item {
    private static final String MAID_INFO = "MaidInfo";

    public ItemCamera() {
        super((new Properties()).stacksTo(1).durability(50));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (handIn == InteractionHand.MAIN_HAND) {
            int searchDistance = 8;
            ItemStack camera = playerIn.getItemInHand(handIn);
            Optional<EntityMaid> result = MaidRayTraceHelper.rayTraceMaid(playerIn, searchDistance);
            if (result.isPresent()) {
                EntityMaid maid = result.get();
                if (!worldIn.isClientSide && maid.isAlive() && maid.isOwnedBy(playerIn) && !maid.isSleeping()) {
                    spawnMaidPhoto(worldIn, maid, playerIn);
                    maid.discard();
                    playerIn.getCooldowns().addCooldown(this, 20);
                    camera.hurtAndBreak(1, playerIn, (e) -> e.broadcastBreakEvent(InteractionHand.MAIN_HAND));
                    if (playerIn instanceof ServerPlayer serverPlayer) {
                        InitTrigger.MAID_EVENT.trigger(serverPlayer, MaidEvent.PHOTO_MAID);
                    }
                }
                maid.spawnExplosionParticle();
                playerIn.playSound(InitSounds.CAMERA_USE.get(), 1.0f, 1.0f);
                return InteractionResultHolder.sidedSuccess(camera, worldIn.isClientSide);
            }
        }
        return super.use(worldIn, playerIn, handIn);
    }

    private void spawnMaidPhoto(Level worldIn, EntityMaid maid, Player playerIn) {
        ItemStack photo = InitItems.PHOTO.get().getDefaultInstance();
        CompoundTag photoTag = new CompoundTag();
        CompoundTag maidTag = new CompoundTag();
        maid.setHomeModeEnable(false);
        maid.saveWithoutId(maidTag);
        maidTag.putString("id", Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(InitEntities.MAID.get())).toString());
        photoTag.put(MAID_INFO, maidTag);
        photo.setTag(photoTag);
        Containers.dropItemStack(worldIn, playerIn.getX(), playerIn.getY(), playerIn.getZ(), photo);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
        // 返回 true，阻止打开女仆 GUI
        if (stack.getItem() == this && target.isAlive() && target instanceof EntityMaid && ((EntityMaid) target).isOwnedBy(playerIn)) {
            this.use(playerIn.level, playerIn, hand);
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(stack, playerIn, target, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("tooltips.touhou_little_maid.camera.desc").withStyle(ChatFormatting.DARK_GREEN));
    }
}
