package supercoder79.advancementfix.mixin;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

@Mixin(value = PlayerAdvancementTracker.class, priority = 999)
public abstract class MixinPlayerAdvancementTracker {

    @Shadow @Final private Set<Advancement> progressUpdates;
    @Shadow @Final private Map<Advancement, AdvancementProgress> advancementToProgress;
    @Shadow @Final private Set<Advancement> visibleAdvancements;
    @Shadow @Final private Set<Advancement> visibilityUpdates;

    @Shadow
    protected abstract boolean canSee(Advancement advancement);

    /**
     * @author SuperCoder79- fixes 2-way recursion to use a stack
     */
    @Overwrite
    private void updateDisplay(Advancement adv) {
        Advancement top = adv;
        while(top.getParent() != null) {
            top = top.getParent();
        }

        Deque<Advancement> stack = new LinkedList<>();
        stack.push(top);

        while(!stack.isEmpty()) {
            Advancement advancement = stack.pop();

            boolean bl = this.canSee(advancement);
            boolean bl2 = this.visibleAdvancements.contains(advancement);
            if (bl && !bl2) {
                this.visibleAdvancements.add(advancement);
                this.visibilityUpdates.add(advancement);
                if (this.advancementToProgress.containsKey(advancement)) {
                    this.progressUpdates.add(advancement);
                }
            } else if (!bl && bl2) {
                this.visibleAdvancements.remove(advancement);
                this.visibilityUpdates.add(advancement);
            }

            for(Advancement advancement2 : advancement.getChildren()) {
                stack.add(advancement2);
            }
        }
    }
}
