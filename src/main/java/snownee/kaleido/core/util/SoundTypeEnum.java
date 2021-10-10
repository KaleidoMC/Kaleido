package snownee.kaleido.core.util;

import net.minecraft.block.SoundType;

public enum SoundTypeEnum {
	wood(SoundType.WOOD),
	stone(SoundType.STONE),
	gravel(SoundType.GRAVEL),
	grass(SoundType.GRASS),
	metal(SoundType.METAL),
	glass(SoundType.GLASS),
	wool(SoundType.WOOL),
	sand(SoundType.SAND),
	snow(SoundType.SNOW),
	slime(SoundType.SLIME_BLOCK),
	honey(SoundType.HONEY_BLOCK),
	crop(SoundType.CROP),
	hard_crop(SoundType.HARD_CROP),
	vine(SoundType.VINE),
	chain(SoundType.CHAIN);

	public final SoundType soundType;

	SoundTypeEnum(SoundType soundType) {
		this.soundType = soundType;
	}
}
