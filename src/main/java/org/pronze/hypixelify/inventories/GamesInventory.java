package org.pronze.hypixelify.inventories;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.pronze.hypixelify.Hypixelify;
import org.pronze.hypixelify.utils.ShopUtil;
import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.lib.sgui.SimpleInventories;
import org.screamingsandals.bedwars.lib.sgui.builder.FormatBuilder;
import org.screamingsandals.bedwars.lib.sgui.events.PostActionEvent;
import org.screamingsandals.bedwars.lib.sgui.inventory.GuiHolder;
import org.screamingsandals.bedwars.lib.sgui.inventory.Options;
import org.screamingsandals.bedwars.lib.sgui.utils.MapReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GamesInventory implements Listener {
    private Map<Integer, SimpleInventories> menu;
    private Map<Integer, Options> option;
    private Map<Integer, List<Player>> players;
    private Map<Integer, String> labels;

    public GamesInventory() {
        Options options = ShopUtil.generateOptions();
        options.setPrefix("Bed Wars Solo");
        option.put(1, options);
        options.setPrefix("Bed Wars Doubles");
        option.put(2, options);
        options.setPrefix("Bed Wars Solo");
        option.put(3, options);
        options.setPrefix("Bed Wars Solo");
        option.put(4, options);

        labels.put(1, "Solo");
        labels.put(2, "Double");
        labels.put(3, "Triple");
        labels.put(4, "Squad");

        Bukkit.getServer().getPluginManager().registerEvents(this, Hypixelify.getInstance());
        createData();
    }

    private void createData() {
        SimpleInventories soloMenu = new SimpleInventories(option.get(1));
        SimpleInventories doubleMenu = new SimpleInventories(option.get(2));
        SimpleInventories tripleMenu = new SimpleInventories(option.get(3));
        SimpleInventories squadMenu = new SimpleInventories(option.get(4));

        for(int i = 1; i < 5; i++){
            List<ItemStack> myCategories = ShopUtil.createCategories(Arrays.asList("§7Play Bed Wars {mode}".replace("{mode}", labels.get(i)), " ", "§eClick to play!"),
                    "§aBed Wars ({mode})".replace("{mode}", labels.get(i)),"§aMap Selector ({mode})".replace("{mode}", labels.get(i)));
            ItemStack category = myCategories.get(0);
            ItemStack category2 = myCategories.get(1);
            ItemStack category3 = myCategories.get(2);
            ItemStack category4 = myCategories.get(3);

            ArrayList<Object> Games = ShopUtil.createGamesGUI(1, Arrays.asList("§8{mode}".replace("{mode}", labels.get(i)), "", "§7Available Servers: §a1", "§7Status: §a{status}"
                    ,"§7Players:§a {players}","", "§aClick to play", "§eRight click to toggle favorite!"));
            FormatBuilder builder = ShopUtil.createBuilder(Games, category, category2, category3, category4);
            switch(i){
                case 1:
                    soloMenu.load(builder);
                    soloMenu.generateData();
                    menu.put(1, soloMenu);
                    break;
                case 2:
                    doubleMenu.load(builder);
                    doubleMenu.generateData();
                    menu.put(2, doubleMenu);
                    break;
                case 3:
                    tripleMenu.load(builder);
                    tripleMenu.generateData();
                    menu.put(3, tripleMenu);
                    break;
                case 4:
                    squadMenu.load(builder);
                    squadMenu.generateData();
                    menu.put(4, squadMenu);
                    break;
            }


        }


    }

    public void openForPlayer(Player player, int mode) {
        createData();
        menu.get(mode).openForPlayer(player);
        players.get(mode).add(player);
    }

    public void repaint(int mode) {
        for (Player player : players.get(mode)) {
            GuiHolder guiHolder = menu.get(mode).getCurrentGuiHolder(player);
            if (guiHolder == null) {
                return;
            }

            createData();
            guiHolder.setFormat(menu.get(mode));
            guiHolder.repaint();
        }
    }

    @EventHandler
    public void onPostAction(PostActionEvent event) {
        if (event.getFormat() != menu) {
            return;
        }

        Player player = event.getPlayer();
        if(event.getItem().getStack() != null)
        {
            if(event.getItem().getStack().getType().equals(Material.BARRIER)) {
                //Players.remove(player);
                player.closeInventory();
            } else if(event.getItem().getStack().getType().equals(Material.RED_BED)
                    || event.getItem().getStack().getType().equals(Material.FIREWORK_ROCKET)
                    || event.getItem().getStack().getType().equals(Material.DIAMOND)){
                player.closeInventory();
                //repaint();
                //Players.remove(player);
                List<Game> games = ShopUtil.getGamesWithSize(1);
                if(games == null || games.isEmpty())
                    return;
                for (Game game : games){
                    if(game.getStatus().equals(GameStatus.WAITING)) {
                        game.joinToGame(player);
                        break;
                    }
                }
            } else  if(event.getItem().getStack().getType().equals(Material.ENDER_PEARL)){
                player.closeInventory();
             //   repaint();
            //    Players.remove(player);
                player.performCommand("bw rejoin");
            }
        }

        MapReader reader = event.getItem().getReader();
        if (reader.containsKey("game")) {
            Game game = (Game) reader.get("game");
            Main.getGame(game.getName()).joinToGame(player);
            player.closeInventory();

           // repaint();
            //Players.remove(player);
        }
    }



}
