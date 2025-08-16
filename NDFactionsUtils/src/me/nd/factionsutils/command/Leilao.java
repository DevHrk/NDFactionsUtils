package me.nd.factionsutils.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.nd.factionsutils.Main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

// Estrutura para armazenar dados de um leilão
class Auction {
    String seller;
    ItemStack item;
    double currentBid;
    String highestBidder;
    long endTime;
    String itemName;
    Map<String, Double> bidderAmounts; // Jogador -> valor do lance

    Auction(String seller, ItemStack item, double startingBid, String itemName) {
        this.seller = seller;
        this.item = item;
        this.currentBid = startingBid;
        this.highestBidder = null;
        this.endTime = System.currentTimeMillis() + 3600000L; // 1 minuto
        this.itemName = itemName;
        this.bidderAmounts = new HashMap<>();
    }
}

public class Leilao extends Commands implements Listener {
    private final List<Auction> auctions = new ArrayList<>();
    private final Map<String, Integer> playerPages = new HashMap<>();
    private final Map<String, Integer> pendingBids = new HashMap<>(); // Jogador -> índice do leilão
    private final Map<String, List<ItemStack>> pendingItems = new HashMap<>(); // Jogador -> itens pendentes
    private final File auctionFile;
    private final YamlConfiguration auctionConfig;
    private static final int ITEMS_PER_PAGE = 21; // 21 slots para leilões
    private static final int[] AUCTION_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    public Leilao() {
        super("leilao");
        this.auctionFile = new File(Main.get().getDataFolder(), "auctions.yml");
        this.auctionConfig = YamlConfiguration.loadConfiguration(auctionFile);
        Main.get().getServer().getPluginManager().registerEvents(this, Main.get());
        loadAuctions();
        startAuctionChecker();
        startPendingItemChecker();
    }

    @Override
    public void perform(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando só pode ser usado por jogadores!");
            return;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            openAuctionMenu(player, 0);
            return;
        }

        if (args[0].equalsIgnoreCase("vender")) {
            if (args.length < 2) {
                player.sendMessage("§cUso: /leilao vender <preço inicial>");
                return;
            }

            try {
                double startingBid = Double.parseDouble(args[1]);
                if (startingBid <= 0) {
                    player.sendMessage("§cO preço inicial deve ser maior que 0!");
                    return;
                }

                ItemStack item = player.getInventory().getItemInHand();
                if (item == null || item.getType() == Material.AIR) {
                    player.sendMessage("§cVocê precisa segurar um item para leiloar!");
                    return;
                }

                ItemMeta meta = item.getItemMeta();
                String itemName = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : item.getType().name();
                Auction auction = new Auction(player.getName(), item.clone(), startingBid, itemName);
                auctions.add(auction);
                player.getInventory().setItemInHand(null);
                saveAuctions();
                Bukkit.broadcastMessage("§eNovo leilão iniciado por §f" + player.getName() + "§e: §f" + itemName + " §epor §a$" + startingBid);
                return;
            } catch (NumberFormatException e) {
                player.sendMessage("§cPor favor, insira um preço válido!");
            }
        } else if (args[0].equalsIgnoreCase("lance")) {
            if (args.length < 3) {
                player.sendMessage("§cUso: /leilao lance <índice> <valor>");
                return;
            }

            try {
                int index = Integer.parseInt(args[1]) - 1;
                double bid = Double.parseDouble(args[2]);
                if (index < 0 || index >= auctions.size()) {
                    player.sendMessage("§cÍndice de leilão inválido!");
                    return;
                }

                Auction auction = auctions.get(index);
                if (System.currentTimeMillis() >= auction.endTime) {
                    player.sendMessage("§cEste leilão já expirou!");
                    return;
                }
                
                if (auction.seller.equals(player.getName())) {
                    player.sendMessage("§cVocê não pode dar lance no seu próprio leilão!");
                    return;
                }

                if (bid <= auction.currentBid) {
                    player.sendMessage("§cO lance deve ser maior que o atual (§a$" + auction.currentBid + "§c)!");
                    return;
                }

                if (!Main.economy.has(player, bid)) {
                    player.sendMessage("§cVocê não tem §a$" + bid + " §cpara dar este lance!");
                    return;
                }

                if (auction.highestBidder != null) {
                    Main.economy.depositPlayer(Bukkit.getOfflinePlayer(auction.highestBidder), auction.currentBid);
                }

                Main.economy.withdrawPlayer(player, bid);
                auction.bidderAmounts.put(player.getName(), bid); // Armazena o lance do jogador
                auction.currentBid = bid;
                auction.highestBidder = player.getName();
                Bukkit.broadcastMessage("§eNovo lance de §f" + player.getName() + " §epor §a$" + bid + " §eno item §f" + auction.itemName);
                saveAuctions();
            } catch (NumberFormatException e) {
                player.sendMessage("§cPor favor, insira um índice e valor válidos!");
            }
        } else {
            player.sendMessage("§cUso: /leilao [vender <preço> | lance <índice> <valor>]");
        }
    }

    private void openAuctionMenu(Player player, int page) {
        int totalPages = (int) Math.ceil((double) auctions.size() / ITEMS_PER_PAGE);
        page = Math.max(0, Math.min(page, totalPages - 1));
        playerPages.put(player.getName(), page);

        Inventory inv = Bukkit.createInventory(null, 54, "Leilão - Página " + (page + 1));

        // Adiciona item de informação
        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§eInformações");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§eComo usar o leilão:");
        infoLore.add("");
        infoLore.add("§fPara vender:");
        infoLore.add("§7- Segure um item");
        infoLore.add("§7- Use §f/leilao vender <preço>");
        infoLore.add("");
        infoLore.add("§fPara dar lance:");
        infoLore.add("§7- Clique no item neste menu");
        infoLore.add("§7- Digite o valor no chat");
        infoLore.add("");
        infoLore.add("§cO valor será debitado!");
        infoLore.add("§cReembolsado se não vencer.");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inv.setItem(4, infoItem);

        // Verifica se não há leilões
        if (auctions.isEmpty()) {
            ItemStack noAuctionItem = new ItemStack(Material.WEB);
            ItemMeta noAuctionMeta = noAuctionItem.getItemMeta();
            noAuctionMeta.setDisplayName("§cNenhum Leilão Ativo");
            List<String> noAuctionLore = new ArrayList<>();
            noAuctionLore.add("§7No momento, não há");
            noAuctionLore.add("§7leilões em andamento.");
            noAuctionLore.add("");
            noAuctionLore.add("§7Crie um com:");
            noAuctionLore.add("§f/leilao vender <preço>");
            noAuctionMeta.setLore(noAuctionLore);
            noAuctionItem.setItemMeta(noAuctionMeta);
            inv.setItem(22, noAuctionItem);
        } else {
            // Adiciona itens do leilão nos slots especificados
            int startIndex = page * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, auctions.size());
            int slotIndex = 0;

            for (int i = startIndex; i < endIndex && slotIndex < AUCTION_SLOTS.length; i++) {
                Auction auction = auctions.get(i);
                ItemStack item = auction.item.clone();
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    List<String> lore = new ArrayList<>();
                    lore.add("");
                    lore.add("§fVendedor: §e" + auction.seller);
                    lore.add("§fLance Atual: §a$" + auction.currentBid);
                    lore.add("§fMaior Lanceador: §e" + (auction.highestBidder != null ? auction.highestBidder : "Nenhum"));
                    lore.add("§fTempo Restante: §e" + getTimeRemaining(auction.endTime));
                    lore.add("");
                    lore.add("§7Clique para dar lance!");
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inv.setItem(AUCTION_SLOTS[slotIndex++], item);
            }
        }

        // Adiciona botões de navegação
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.setDisplayName("§ePágina Anterior");
            prev.setItemMeta(prevMeta);
            inv.setItem(18, prev);
        }

        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.setDisplayName("§ePróxima Página");
            next.setItemMeta(nextMeta);
            inv.setItem(26, next);
        }

        player.openInventory(inv);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().startsWith("Leilão - Página ")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot < 0) return; // Ignore clicks outside the inventory

        int page = playerPages.getOrDefault(player.getName(), 0);

        // Navegação
        if (slot == 18 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.ARROW) {
            openAuctionMenu(player, page - 1);
            return;
        }

        if (slot == 26 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.ARROW) {
            openAuctionMenu(player, page + 1);
            return;
        }

        // Ignora clique no item de informação ou fora dos slots de leilão
        if (slot == 4 || !isAuctionSlot(slot)) return;

        // Calcula o índice do leilão com base no slot
        int slotIndex = getSlotIndex(slot);
        if (slotIndex == -1) return;
        int index = page * ITEMS_PER_PAGE + slotIndex;
        if (index >= auctions.size()) return;

        Auction auction = auctions.get(index);
        if (auction.seller.equals(player.getName())) {
            player.sendMessage("§cVocê não pode dar lance no seu próprio leilão!");
            return;
        }

        pendingBids.put(player.getName(), index);
        player.sendMessage("§eDigite o valor que deseja dar de lance.");
        player.sendMessage("§eValor atual: §f" + auction.currentBid);
        player.sendMessage("");
        player.sendMessage("§cAo dar o lance o valor é debitado da sua conta!");
        player.sendMessage("§cCaso não ganhe o leilão o dinheiro perdido retorna!");
        player.closeInventory();

        // Timeout para remover lance pendente após 10 segundos
        new BukkitRunnable() {
            @Override
            public void run() {
                if (pendingBids.remove(player.getName()) != null) {
                    player.sendMessage("§cTempo esgotado para dar lance!");
                }
            }
        }.runTaskLater(Main.get(), 200L); // 10 segundos (20 ticks/segundo * 10)
    }

    // Verifica se o slot é um dos slots de leilão
    private boolean isAuctionSlot(int slot) {
        for (int auctionSlot : AUCTION_SLOTS) {
            if (slot == auctionSlot) return true;
        }
        return false;
    }

    // Obtém o índice do slot na lista AUCTION_SLOTS
    private int getSlotIndex(int slot) {
        for (int i = 0; i < AUCTION_SLOTS.length; i++) {
            if (AUCTION_SLOTS[i] == slot) return i;
        }
        return -1;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Integer index = pendingBids.remove(player.getName());
        if (index == null) return;

        event.setCancelled(true); // Impede que a mensagem apareça no chat público
        String message = event.getMessage().trim();

        try {
            double bid = Double.parseDouble(message);
            if (index < 0 || index >= auctions.size()) {
                player.sendMessage("§cLeilão inválido ou expirado!");
                return;
            }

            Auction auction = auctions.get(index);
            if (auction.seller.equals(player.getName())) {
                player.sendMessage("§cVocê não pode dar lance no seu próprio leilão!");
                return;
            }

            if (bid <= auction.currentBid) {
                player.sendMessage("§cO lance deve ser maior que o atual (§a$" + auction.currentBid + "§c)!");
                return;
            }

            if (!Main.economy.has(player, bid)) {
                player.sendMessage("§cVocê não tem §a$" + bid + " §cpara dar este lance!");
                return;
            }

            // Processa o lance
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (auction.highestBidder != null) {
                        Main.economy.depositPlayer(Bukkit.getOfflinePlayer(auction.highestBidder), auction.currentBid);
                    }

                    Main.economy.withdrawPlayer(player, bid);
                    auction.bidderAmounts.put(player.getName(), bid); // Armazena o lance do jogador
                    auction.currentBid = bid;
                    auction.highestBidder = player.getName();
                    Bukkit.broadcastMessage("§eNovo lance de §f" + player.getName() + " §epor §a$" + bid + " §eno item §f" + auction.itemName);
                    saveAuctions();
                }
            }.runTask(Main.get()); // Executa transações na thread principal
        } catch (NumberFormatException e) {
            player.sendMessage("§cPor favor, insira um valor numérico válido!");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        deliverPendingItems(player);
    }

    private void startAuctionChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                List<Auction> toRemove = new ArrayList<>();
                for (Auction auction : auctions) {
                    if (now >= auction.endTime) {
                        if (auction.highestBidder != null) {
                            Player winner = Bukkit.getPlayer(auction.highestBidder);
                            if (winner != null && canAddToInventory(winner, auction.item)) {
                                winner.getInventory().addItem(auction.item);
                                winner.sendMessage("§eVocê venceu o leilão de §f" + auction.itemName + " §epor §a$" + auction.currentBid + "!");
                            } else {
                                addPendingItem(auction.highestBidder, auction.item, "§eVocê venceu o leilão de §f" + auction.itemName + " §epor §a$" + auction.currentBid + "!");
                            }
                            Player seller = Bukkit.getPlayer(auction.seller);
                            if (seller != null) {
                                Main.economy.depositPlayer(seller, auction.currentBid);
                                seller.sendMessage("§eSeu item §f" + auction.itemName + " §efoi vendido por §a$" + auction.currentBid + "!");
                            }
                            // Reembolsa jogadores que não venceram
                            for (Map.Entry<String, Double> entry : auction.bidderAmounts.entrySet()) {
                                String bidder = entry.getKey();
                                double amount = entry.getValue();
                                if (!bidder.equals(auction.highestBidder)) {
                                    Main.economy.depositPlayer(Bukkit.getOfflinePlayer(bidder), amount);
                                    Player bidderPlayer = Bukkit.getPlayer(bidder);
                                    if (bidderPlayer != null) {
                                        bidderPlayer.sendMessage("§eVocê não venceu o leilão de §f" + auction.itemName + ". §a$" + amount + " §efoi reembolsado!");
                                    }
                                }
                            }
                        } else {
                            Player seller = Bukkit.getPlayer(auction.seller);
                            if (seller != null && canAddToInventory(seller, auction.item)) {
                                seller.getInventory().addItem(auction.item);
                                seller.sendMessage("§eSeu item §f" + auction.itemName + " §enão foi vendido e foi devolvido!");
                            } else {
                                addPendingItem(auction.seller, auction.item, "§eSeu item §f" + auction.itemName + " §enão foi vendido e foi devolvido!");
                            }
                        }
                        toRemove.add(auction);
                    }
                }
                auctions.removeAll(toRemove);
                if (!toRemove.isEmpty()) {
                    saveAuctions();
                }
            }
        }.runTaskTimerAsynchronously(Main.get(), 0L, 200L); // Verifica a cada 10 segundos
    }

    private void startPendingItemChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (String playerName : new ArrayList<>(pendingItems.keySet())) {
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null) {
                        deliverPendingItems(player);
                    }
                }
            }
        }.runTaskTimer(Main.get(), 0L, 6000L); // Verifica a cada 5 minutos
    }

    private boolean canAddToInventory(Player player, ItemStack item) {
        Inventory inv = player.getInventory();
        int remaining = item.getAmount();
        for (int i = 0; i < inv.getSize() && remaining > 0; i++) {
            ItemStack slotItem = inv.getItem(i);
            if (slotItem == null) {
                return true; // Slot vazio
            }
            if (slotItem.isSimilar(item) && slotItem.getAmount() < slotItem.getMaxStackSize()) {
                remaining -= (slotItem.getMaxStackSize() - slotItem.getAmount());
                if (remaining <= 0) return true; // Pode empilhar
            }
        }
        return false;
    }

    private void addPendingItem(String playerName, ItemStack item, String message) {
        List<ItemStack> items = pendingItems.computeIfAbsent(playerName, k -> new ArrayList<>());
        items.add(item.clone());
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            player.sendMessage(message);
            player.sendMessage("§eO item será entregue quando você tiver espaço no inventário!");
        }
    }

    private void deliverPendingItems(Player player) {
        List<ItemStack> items = pendingItems.get(player.getName());
        if (items == null || items.isEmpty()) return;

        List<ItemStack> delivered = new ArrayList<>();
        for (ItemStack item : new ArrayList<>(items)) {
            if (canAddToInventory(player, item)) {
                player.getInventory().addItem(item);
                player.sendMessage("§eItem entregue do leilão: §f" + (item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name()));
                delivered.add(item);
            }
        }

        items.removeAll(delivered);
        if (items.isEmpty()) {
            pendingItems.remove(player.getName());
        } else {
            player.sendMessage("§cAlguns itens não foram entregues devido a falta de espaço no inventário. Tente novamente mais tarde!");
        }
    }
    
    @SuppressWarnings("unused")
    private void loadAuctions() {
        if (!auctionFile.exists()) return;
        for (String key : auctionConfig.getKeys(false)) {
            String seller = auctionConfig.getString(key + ".seller");
            ItemStack item = auctionConfig.getItemStack(key + ".item");
            double currentBid = auctionConfig.getDouble(key + ".currentBid");
            
			String highestBidder = auctionConfig.getString(key + ".highestBidder");
            long endTime = auctionConfig.getLong(key + ".endTime");
            String itemName = auctionConfig.getString(key + ".itemName");
            // Verifica se os dados essenciais estão presentes
            if (seller == null || item == null || itemName == null) {
                continue;
            }
            auctions.add(new Auction(seller, item, currentBid, itemName) {
                {
                    this.highestBidder = highestBidder;
                    this.endTime = endTime;
                }
            });
        }
    }

    private void saveAuctions() {
        auctionConfig.getKeys(false).forEach(key -> auctionConfig.set(key, null));
        for (int i = 0; i < auctions.size(); i++) {
            Auction auction = auctions.get(i);
            String key = "auction." + i;
            auctionConfig.set(key + ".seller", auction.seller);
            auctionConfig.set(key + ".item", auction.item);
            auctionConfig.set(key + ".currentBid", auction.currentBid);
            if (auction.highestBidder != null) {
                auctionConfig.set(key + ".highestBidder", auction.highestBidder);
            }
            auctionConfig.set(key + ".endTime", auction.endTime);
            auctionConfig.set(key + ".itemName", auction.itemName);
        }
        try {
            auctionConfig.save(auctionFile);
        } catch (Exception e) {
        }
    }

    private String getTimeRemaining(long endTime) {
        long remaining = (endTime - System.currentTimeMillis()) / 1000;
        if (remaining <= 0) {
            return "Expirado";
        }
        long hours = remaining / 3600;
        long minutes = (remaining % 3600) / 60;
        long seconds = remaining % 60;
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }
}