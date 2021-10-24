package snownee.kaleido.hub.util;

import net.minecraft.util.text.ITextComponent;

// Copied from Vanila's IProgressUpdate because some metheds of it are client only
public interface IProgressUpdate {
	void progressStartNoAbort(ITextComponent pComponent);

	void progressStart(ITextComponent pComponent);

	void progressStage(ITextComponent pComponent);

	/**
    * Updates the progress bar on the loading screen to the specified amount.
    */
	void progressStagePercentage(int pProgress);

	void stop();
}