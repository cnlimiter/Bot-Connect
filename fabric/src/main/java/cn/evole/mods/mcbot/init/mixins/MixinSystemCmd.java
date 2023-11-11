//#if MC >= 11903
package cn.evole.mods.mcbot.init.mixins;

import cn.evole.mods.mcbot.McBot;
import cn.evole.mods.mcbot.init.config.ModConfig;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.charset.StandardCharsets;

/**
 * Author cnlimiter
 * CreateTime 2023/5/19 0:54
 * Name MixinSystemCmd
 * Description
 */

@Mixin(value = Commands.class)
public abstract class MixinSystemCmd {
    @Unique
    private static void say_register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register((Commands.literal("say").requires((CommandSourceStack) -> CommandSourceStack.hasPermission(2))).then(Commands.argument("message", MessageArgument.message()).executes((commandContext) -> {
            MessageArgument.resolveChatMessage(commandContext, "message", (playerChatMessage) -> {
                CommandSourceStack CommandSourceStack = commandContext.getSource();
                PlayerList playerList = CommandSourceStack.getServer().getPlayerList();
                /////////////////////////
                if (FabricLoader.getInstance().isModLoaded("mcbot")
                        && ModConfig.INSTANCE != null
                        && ModConfig.INSTANCE.getStatus().isSChatEnable()
                        && ModConfig.INSTANCE.getStatus().isSEnable()
                        && ModConfig.INSTANCE.getCmd().isMcPrefixOn()) {
                    if (ModConfig.INSTANCE.getCommon().isGuildOn() && !ModConfig.INSTANCE.getCommon().getChannelIdList().isEmpty()) {
                        for (String id : ModConfig.INSTANCE.getCommon().getChannelIdList())
                            McBot.bot.sendGuildMsg(ModConfig.INSTANCE.getCommon().getGuildId(),
                                    id,
                                    String.format("[" + ModConfig.INSTANCE.getCmd().getMcPrefix() + "] %s", new String(playerChatMessage.decoratedContent().getString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8)));
                    } else {
                        for (long id : ModConfig.INSTANCE.getCommon().getGroupIdList())
                            McBot.bot.sendGroupMsg(id,
                                    String.format("[" + ModConfig.INSTANCE.getCmd().getMcPrefix() + "] %s", new String(playerChatMessage.decoratedContent().getString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8)),
                                    true);
                    }
                }
                /////////////////////////
                playerList.broadcastChatMessage(playerChatMessage, CommandSourceStack, ChatType.bind(ChatType.SAY_COMMAND, CommandSourceStack));
            });
            return 1;
        })));
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/SayCommand;register(Lcom/mojang/brigadier/CommandDispatcher;)V"))
    private void sayRedirect(CommandDispatcher<CommandSourceStack> dispatcher) {
        say_register(dispatcher);
    }
}
//#endif