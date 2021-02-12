package software.tachyon.starfruit.module.utility;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.ConfirmScreenActionC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import software.tachyon.starfruit.StarfruitMod;
import software.tachyon.starfruit.module.ModuleInfo;
import software.tachyon.starfruit.module.ModuleInfo.Category;
import software.tachyon.starfruit.module.StatefulModule;
import software.tachyon.starfruit.module.event.RecvPacketEvent;
import software.tachyon.starfruit.module.event.SendPacketEvent;
import software.tachyon.starfruit.module.event.player.InventoryUpdateEvent;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import static net.minecraft.item.Items.*;

// TODO(haze): Fix bug where accessing items from other containers other than the player inventory
// results in ghost items
// TODO(haze): Add heuristics for damage, enchantments (enchantments > damage)
public class AutoArmor extends StatefulModule {

  enum Type {
    LEATHER(0), GOLD(1), MISC(2), IRON(3), DIAMOND(4);

    final int leverage;

    Type(int leverage) {
      this.leverage = leverage;
    }

    enum BeatAnswer {
      YES, NO, SAME
    }

    BeatAnswer beats(Type other) {
      if (this.leverage == other.leverage)
        return BeatAnswer.SAME;
      else if (this.leverage < other.leverage)
        return BeatAnswer.NO;
      else
        return BeatAnswer.YES;
    }
  }

  class QueueArmorTask {
    public final ItemStack stack;
    public final int residingSlot;

    QueueArmorTask(ItemStack stack, int slot) {
      this.stack = stack;
      this.residingSlot = slot;
    }
  }

  final Queue<QueueArmorTask> alternateInventoryQueue;

  public AutoArmor(Integer initialKeyCode) {
    super(initialKeyCode,
        ModuleInfo.init().name("AutoArmor").category(Category.UTILITY).hidden(true).build());
    this.alternateInventoryQueue = new LinkedList<>();
  }

  Optional<Type> isAnyLeatherArmorItem(Item item) {
    if (item == LEATHER_HELMET || item == LEATHER_CHESTPLATE || item == LEATHER_LEGGINGS
        || item == LEATHER_BOOTS) {
      return Optional.of(Type.LEATHER);
    }
    return Optional.empty();
  }

  Optional<Type> isAnyGoldArmorItem(Item item) {
    if (item == GOLDEN_HELMET || item == GOLDEN_CHESTPLATE || item == GOLDEN_LEGGINGS
        || item == GOLDEN_BOOTS) {
      return Optional.of(Type.GOLD);
    }
    return Optional.empty();
  }

  Optional<Type> isAnyIronArmorItem(Item item) {
    if (item == IRON_HELMET || item == IRON_CHESTPLATE || item == IRON_LEGGINGS
        || item == IRON_BOOTS) {
      return Optional.of(Type.IRON);
    }
    return Optional.empty();
  }

  Optional<Type> isAnyDiamondArmorItem(Item item) {
    if (item == DIAMOND_HELMET || item == DIAMOND_CHESTPLATE || item == DIAMOND_LEGGINGS
        || item == DIAMOND_BOOTS) {
      return Optional.of(Type.DIAMOND);
    }
    return Optional.empty();
  }

  Optional<Type> isAnyMiscArmorItem(Item item) {
    if (item == TURTLE_HELMET || item == CHAINMAIL_BOOTS || item == CHAINMAIL_HELMET
        || item == CHAINMAIL_LEGGINGS || item == CHAINMAIL_CHESTPLATE) {
      return Optional.of(Type.MISC);
    }
    return Optional.empty();
  }

  Optional<Type> getArmorPieceType(Item item) {
    return isAnyDiamondArmorItem(item).or(() -> isAnyIronArmorItem(item))
        .or(() -> isAnyGoldArmorItem(item)).or(() -> isAnyLeatherArmorItem(item))
        .or(() -> isAnyMiscArmorItem(item));
  }

  boolean isChestplate(Item item) {
    return item == DIAMOND_CHESTPLATE || item == IRON_CHESTPLATE || item == GOLDEN_CHESTPLATE
        || item == LEATHER_CHESTPLATE || item == CHAINMAIL_CHESTPLATE;
  }

  boolean isLeggings(Item item) {
    return item == DIAMOND_LEGGINGS || item == IRON_LEGGINGS || item == GOLDEN_LEGGINGS
        || item == LEATHER_LEGGINGS || item == CHAINMAIL_LEGGINGS;
  }

  boolean isHelmet(Item item) {
    return item == DIAMOND_HELMET || item == IRON_HELMET || item == GOLDEN_HELMET
        || item == LEATHER_HELMET || item == CHAINMAIL_HELMET || item == TURTLE_HELMET;
  }

  boolean isBoots(Item item) {
    return item == DIAMOND_BOOTS || item == IRON_BOOTS || item == GOLDEN_BOOTS
        || item == LEATHER_BOOTS || item == CHAINMAIL_BOOTS;
  }

  boolean beats(int checkSlot, Type type, ItemStack with) {
    final ItemStack residing = StarfruitMod.minecraft.player.inventory.getArmorStack(checkSlot);
    if (residing.getItem() == AIR)
      return true;
    // can only have armor equipped
    final Type residingType = getArmorPieceType(residing.getItem()).get();
    switch (type.beats(residingType)) {
      case SAME:
        // more heuristics
        return false;
      case NO:
        return false;
      case YES:
        return true;
    }
    return false;
  }

  void equipDirect(ItemStack stack, int from) {
    final ClientPlayerInteractionManager manager = StarfruitMod.minecraft.interactionManager;
    final ClientPlayerEntity player = StarfruitMod.minecraft.player;
    final int inventorySyncId = player.playerScreenHandler.syncId;
    manager.clickSlot(inventorySyncId, from, 0, SlotActionType.QUICK_MOVE, player);
  }

  void equipSwap(int from, int to) {
    // pick up original
    this.leftClickSlot(from);
    // swap with equipped
    this.leftClickSlot(to);
    // place back in original spot
    this.leftClickSlot(from);
  }

  void quickMoveSlot(int slot) {
    this.clickSlot(slot, SlotActionType.QUICK_MOVE);
  }

  void leftClickSlot(int slot) {
    this.clickSlot(slot, SlotActionType.PICKUP);
  }

  void clickSlot(int slot, SlotActionType type) {
    final ClientPlayerEntity player = StarfruitMod.minecraft.player;
    final ClientPlayerInteractionManager manager = StarfruitMod.minecraft.interactionManager;
    final int inventorySyncId;
    if (this.isInAcceptableInventory() && player.currentScreenHandler != null) {
      inventorySyncId = player.currentScreenHandler.syncId;
    } else {
      inventorySyncId = player.playerScreenHandler.syncId;
    }
    manager.clickSlot(inventorySyncId, slot, 0, type, player);
  }

  boolean isInAcceptableInventory() {
    final Screen curScreen = StarfruitMod.minecraft.currentScreen;
    return curScreen instanceof HandledScreen && !(curScreen instanceof InventoryScreen);
  }

  void equip(ItemStack stack, int inventory, int from, int to, int armorItemStackIndex) {
    System.out.println("AutoArmor.equip()");
    final ClientPlayerEntity player = StarfruitMod.minecraft.player;
    final ClientPlayNetworkHandler netHandler = StarfruitMod.minecraft.getNetworkHandler();
    short s = player.currentScreenHandler.getNextActionId(player.inventory);
    if (!isInAcceptableInventory()) {
      // move item to player inv
      this.quickMoveSlot(from);
      // this.moveToPlayerInventory(from);
    } else {
      // (picked up / given), swap rn
      if (player.inventory.getArmorStack(armorItemStackIndex).getItem() == AIR) {
        equipDirect(stack, from);
      } else {
        equipSwap(from, to);
      }
    }
    netHandler.sendPacket(new ConfirmScreenActionC2SPacket(0, s, true));
  }

  final int ARMOR_HELMET_SLOT = 3;
  final int ARMOR_CHEST_SLOT = 2;
  final int ARMOR_LEG_SLOT = 1;
  final int ARMOR_BOOT_SLOT = 0;

  final int HELMET_SLOT = 5;
  final int CHEST_SLOT = 6;
  final int LEG_SLOT = 7;
  final int BOOT_SLOT = 8;

  @Handler
  <T extends PacketListener> void recvPacketListener(RecvPacketEvent<T> event) {
    if (event.getPacket() instanceof CloseHandledScreenC2SPacket) {
      System.out.println(event.getPacket().toString());
      while (!this.alternateInventoryQueue.isEmpty()) {
        final QueueArmorTask task = this.alternateInventoryQueue.poll();
        // items are already in our inventory,
      }
    }
  }

  @Handler
  <T extends PacketListener> void sendPacketListener(SendPacketEvent<T> event) {
    if (event.getPacket() instanceof ClickSlotC2SPacket) {
      final ClickSlotC2SPacket packet = (ClickSlotC2SPacket) event.getPacket();
      System.out.printf("syncID: %d\n", packet.getSyncId());
      System.out.printf("stack: %s\n", packet.getStack().toString());
      System.out.printf("button: %d\n", packet.getClickData());
      System.out.printf("slot: %d\n", packet.getSlot());
      System.out.printf("actionType: %s\n", packet.getActionType().toString());
      System.out.printf("transID: %d\n", packet.getActionId());
      System.out.println();
    }
  }

  @Handler
  void onInventoryUpdate(InventoryUpdateEvent event) {
    final ItemStack stack = event.getItemStack();
    final Item item = stack.getItem();
    if (item == AIR || event.getSlot() == -1)
      return;
    try {
      System.out
          .println("picked up item: " + event.getItemStack().toString() + ", " + event.getSlot());
      getArmorPieceType(item).ifPresent(type -> {
        final int slot = event.getSlot();
        final int inventory = event.getId();
        if (isHelmet(item) && beats(ARMOR_HELMET_SLOT, type, stack)) {
          equip(stack, inventory, slot, HELMET_SLOT, ARMOR_HELMET_SLOT);
        } else if (isChestplate(item) && beats(ARMOR_CHEST_SLOT, type, stack)) {
          equip(stack, inventory, slot, CHEST_SLOT, ARMOR_CHEST_SLOT);
        } else if (isLeggings(item) && beats(ARMOR_LEG_SLOT, type, stack)) {
          equip(stack, inventory, slot, LEG_SLOT, ARMOR_LEG_SLOT);
        } else if (isBoots(item) && beats(ARMOR_BOOT_SLOT, type, stack)) {
          equip(stack, inventory, slot, BOOT_SLOT, ARMOR_BOOT_SLOT);
        }
      });
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
