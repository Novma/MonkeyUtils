/**
 * utilizza classi instabili della libreria paper
 */


package io.github.Novma.monkeyUtils.command;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CambioAccount {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("cambioaccount")
                .requires(sender -> sender.getSender().hasPermission("")) //permissions
                .then(Commands.argument("from", StringArgumentType.string())
                        .then(Commands.argument("to", StringArgumentType.string())
                                .executes(CambioAccount::runLogic))
                );
    }

    private static int runLogic(CommandContext<CommandSourceStack> ctx) {
        //get players
        Player from = Bukkit.getPlayerExact(ctx.getArgument("from", String.class));
        Player to = Bukkit.getPlayerExact(ctx.getArgument("to", String.class));
        //check if null
        if (from == null) {
            ctx.getSource().getSender().sendRichMessage("<red>Il primo player non esiste");
            return Command.SINGLE_SUCCESS;
        } else if (to == null) {
            ctx.getSource().getSender().sendRichMessage("<red>Il secondo player non esiste");
            return Command.SINGLE_SUCCESS;
        }
        //get UUID
        UUID ufrom = from.getUniqueId();
        UUID uto = to.getUniqueId();
        if (ufrom.compareTo(uto) == 0) {
            ctx.getSource().getSender().sendRichMessage("<red>I due player coincidono");
            return Command.SINGLE_SUCCESS;
        }

        //change permissions - LuckPerms
        CambioAccount.luckPerms(ctx, ufrom, uto);

        //change island properties
        CambioAccount.superiorSkyblock(ctx, ufrom, uto);



        return Command.SINGLE_SUCCESS;
    }

    //LuckPerms - permissions
    public static void luckPerms(CommandContext<CommandSourceStack> ctx, UUID ufrom, UUID uto) {
        //get LuckPerms
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") instanceof LuckPerms lp) { //check if null
            //get users
            CompletableFuture<User> futureFrom = lp.getUserManager().loadUser(ufrom);
            CompletableFuture<User> futureTo = lp.getUserManager().loadUser(uto);
            //wait futures
            futureFrom.thenCombineAsync(futureTo, (from, to) -> {
                //clear permissions to
                to.data().clear();

                //copy nodes from - to
                for (Node node : from.data().toCollection()) {
                    to.data().add(node);
                }

                //set primary group
                to.setPrimaryGroup(from.getPrimaryGroup());

                return to;
            }).thenAcceptAsync(to -> lp.getUserManager().saveUser(to)); //save user data
        } else {
            ctx.getSource().getSender().sendRichMessage("<red>LuckPerms non caricato!");
        }
    }

    //SuperiorSkyblock
    public static void superiorSkyblock(CommandContext<CommandSourceStack> ctx, UUID ufrom, UUID uto) {
        //check SuperiorSkyblock
        if (Bukkit.getPluginManager().getPlugin("SuperiorSkyblock2") != null) {
            //get users
            SuperiorPlayer from = SuperiorSkyblockAPI.getPlayer(ufrom);
            SuperiorPlayer to = SuperiorSkyblockAPI.getPlayer(uto);

        }
    }
}
