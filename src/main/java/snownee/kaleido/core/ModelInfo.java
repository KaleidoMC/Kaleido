package snownee.kaleido.core;

import javax.annotation.Nullable;

import com.google.gson.annotations.Expose;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.kaleido.Kaleido;
import snownee.kaleido.core.behavior.Behavior;
import snownee.kaleido.core.behavior.NoneBehavior;
import snownee.kaleido.core.block.MasterBlock;
import snownee.kaleido.core.client.KaleidoClient;
import snownee.kiwi.Kiwi;
import snownee.kiwi.util.NBTHelper;

public class ModelInfo {

    @OnlyIn(Dist.CLIENT)
    public static ModelInfo read(PacketBuffer buf) {
        ModelInfo info = new ModelInfo();
        info.id = buf.readResourceLocation();
        info.setLocked(!buf.readBoolean());
        info.useAO = buf.readBoolean();
        info.reward = buf.readBoolean();
        info.price = buf.readByte();

        info.opposite = buf.readBoolean();
        return info;
    }

    @OnlyIn(Dist.CLIENT)
    private IBakedModel[] bakedModel = new IBakedModel[4];
    @Expose
    public Behavior behavior = NoneBehavior.INSTANCE;
    public ResourceLocation id;
    private boolean locked = true;
    @Expose
    public int price = 1;
    @Expose
    public boolean reward;
    private String translationKey;

    @Expose
    public boolean useAO = true;
    @Expose
    public boolean opposite; // temp

    public ResourceLocation getAdvancementId() {
        return new ResourceLocation(Kaleido.MODID, id.toString().replace(':', '/'));
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public IBakedModel getBakedModel(Direction direction) {
        int i = direction.getHorizontalIndex();
        if (i == -1) {
            return null;
        }
        if (bakedModel[i] == null) {
            bakedModel[i] = KaleidoClient.getModel(id, direction);
        }
        return bakedModel[i];
    }

    public String getTranslationKey() {
        if (translationKey == null) {
            translationKey = Util.makeTranslationKey("kaleido.decor", id);
        }
        return translationKey;
    }

    public boolean grant(ServerPlayerEntity player) {
        Advancement advancement = Kiwi.getServer().getAdvancementManager().getAdvancement(getAdvancementId());
        if (advancement != null) {
            PlayerAdvancements playerAdvancements = player.getAdvancements();
            AdvancementProgress progress = playerAdvancements.getProgress(advancement);
            for (String s : progress.getRemaningCriteria()) {
                playerAdvancements.grantCriterion(advancement, s);
            }
            return true;
        }
        return false;
    }

    public boolean isAdvancementDone(ServerPlayerEntity player) {
        Advancement advancement = Kiwi.getServer().getAdvancementManager().getAdvancement(getAdvancementId());
        return player.getAdvancements().getProgress(advancement).isDone();
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isLockedServer(ServerPlayerEntity player) {
        if (Kiwi.getServer().isServerOwner(player.getGameProfile())) {
            return locked;
        } else {
            return !isAdvancementDone(player);
        }
    }

    public ItemStack makeItem() {
        return makeItem(1);
    }

    public ItemStack makeItem(int size) {
        NBTHelper data = NBTHelper.of(new ItemStack(CoreModule.STUFF, size));
        data.setString(MasterBlock.NBT_ID, id.toString());
        return data.getItem();
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void write(PacketBuffer buf, ServerPlayerEntity player) {
        buf.writeResourceLocation(id);
        buf.writeBoolean(isLockedServer(player));
        buf.writeBoolean(useAO);
        buf.writeBoolean(reward);
        buf.writeByte(price);

        buf.writeBoolean(opposite);
    }

}
