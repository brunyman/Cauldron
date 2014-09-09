package net.minecraft.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import org.bukkit.craftbukkit.util.CraftMagicNumbers; // CraftBukkit
import net.minecraft.entity.player.EntityPlayerMP; // Spigot
// Cauldron start
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockMushroom;

import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.world.StructureGrowEvent;
// Cauldron end

public final class ItemStack
{
    public static final DecimalFormat field_111284_a = new DecimalFormat("#.###");
    public int stackSize;
    public int animationsToGo;
    private Item field_151002_e;
    public NBTTagCompound stackTagCompound;
    int itemDamage;

    // Cauldron - due to a bug in Gson(https://code.google.com/p/google-gson/issues/detail?id=440), a stackoverflow 
    //         can occur when gson attempts to resolve a field of a class that points to itself.
    //         As a temporary workaround, we will prevent serialization for this object until the bug is fixed.
    //         This fixes EE3's serialization of ItemStack.
    private transient EntityItemFrame itemFrame;
    public static EntityPlayer currentPlayer; // Cauldron - reference to current player calling onItemUse
    private static final String __OBFID = "CL_00000043";

    public ItemStack(Block p_i1876_1_)
    {
        this(p_i1876_1_, 1);
    }

    public ItemStack(Block p_i1877_1_, int p_i1877_2_)
    {
        this(p_i1877_1_, p_i1877_2_, 0);
    }

    public ItemStack(Block p_i1878_1_, int p_i1878_2_, int p_i1878_3_)
    {
        this(Item.getItemFromBlock(p_i1878_1_), p_i1878_2_, p_i1878_3_);
    }

    public ItemStack(Item p_i1879_1_)
    {
        this(p_i1879_1_, 1);
    }

    public ItemStack(Item p_i1880_1_, int p_i1880_2_)
    {
        this(p_i1880_1_, p_i1880_2_, 0);
    }

    public ItemStack(Item p_i1881_1_, int p_i1881_2_, int p_i1881_3_)
    {
        this.field_151002_e = p_i1881_1_;
        this.stackSize = p_i1881_2_;
        this.itemDamage = p_i1881_3_;

        if (this.itemDamage < 0)
        {
            this.itemDamage = 0;
        }
    }

    public static ItemStack loadItemStackFromNBT(NBTTagCompound p_77949_0_)
    {
        ItemStack itemstack = new ItemStack();
        itemstack.readFromNBT(p_77949_0_);
        return itemstack.getItem() != null ? itemstack : null;
    }

    private ItemStack() {}

    public ItemStack splitStack(int p_77979_1_)
    {
        ItemStack itemstack = new ItemStack(this.field_151002_e, p_77979_1_, this.itemDamage);

        if (this.stackTagCompound != null)
        {
            itemstack.stackTagCompound = (NBTTagCompound)this.stackTagCompound.copy();
        }

        this.stackSize -= p_77979_1_;
        return itemstack;
    }

    public Item getItem()
    {
        return this.field_151002_e;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex()
    {
        return this.getItem().getIconIndex(this);
    }

    @SideOnly(Side.CLIENT)
    public int getItemSpriteNumber()
    {
        return this.getItem().getSpriteNumber();
    }

    public boolean tryPlaceItemIntoWorld(EntityPlayer p_77943_1_, World p_77943_2_, int p_77943_3_, int p_77943_4_, int p_77943_5_, int p_77943_6_, float p_77943_7_, float p_77943_8_, float p_77943_9_)
    {
        // Cauldron start - handle all placement events here
        int meta = this.getItemDamage();
        int size = this.stackSize;
        NBTTagCompound nbt = null;
        if (this.getTagCompound() != null)
        {
            nbt = (NBTTagCompound)this.getTagCompound().copy();
        }

        if (!(this.getItem() instanceof ItemBucket)) // if not bucket
        {
            p_77943_2_.captureBlockStates = true;
            if (this.getItem() instanceof ItemDye && this.getItemDamage() == 15) 
            {
                Block block= p_77943_2_.getBlock(p_77943_3_, p_77943_4_, p_77943_5_);
                if (block != null && (block instanceof BlockSapling || block instanceof BlockMushroom))
                {
                    p_77943_2_.captureTreeGeneration = true;
                }
            }
        }
        currentPlayer = p_77943_1_;
        boolean flag = this.getItem().onItemUse(this, p_77943_1_, p_77943_2_, p_77943_3_, p_77943_4_, p_77943_5_, p_77943_6_, p_77943_7_, p_77943_8_, p_77943_9_);
        p_77943_2_.captureBlockStates = false;
        currentPlayer = null;
        if (flag && p_77943_2_.captureTreeGeneration && p_77943_2_.capturedBlockStates.size() > 0) 
        {
            p_77943_2_.captureTreeGeneration = false;
            Location location = new Location(p_77943_2_.getWorld(), p_77943_3_, p_77943_4_, p_77943_5_);
            TreeType treeType = BlockSapling.treeType;
            BlockSapling.treeType = null;
            List<BlockState> blocks = (List<BlockState>) p_77943_2_.capturedBlockStates.clone();
            p_77943_2_.capturedBlockStates.clear();
            StructureGrowEvent event = null;
            if (treeType != null)
            {
                event = new StructureGrowEvent(location, treeType, false, (Player) p_77943_1_.getBukkitEntity(), blocks);
                org.bukkit.Bukkit.getPluginManager().callEvent(event);
            }
            if (event == null || !event.isCancelled())
            {
                for (BlockState blockstate : blocks) 
                {
                    blockstate.update(true);
                }
            }

            return flag;
        }

        if (flag)
        {
            org.bukkit.event.block.BlockPlaceEvent placeEvent = null;
            List<BlockState> blockstates = (List<BlockState>) p_77943_2_.capturedBlockStates.clone();
            p_77943_2_.capturedBlockStates.clear();
            if (blockstates.size() > 1) 
            {
                placeEvent = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockMultiPlaceEvent(p_77943_2_, p_77943_1_, blockstates, p_77943_3_, p_77943_4_, p_77943_5_);
            } 
            else if (blockstates.size() == 1)
            {
                placeEvent = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockPlaceEvent(p_77943_2_, p_77943_1_, blockstates.get(0), p_77943_3_, p_77943_4_, p_77943_5_);
            }
            if (placeEvent != null && (placeEvent.isCancelled() || !placeEvent.canBuild()))
            {
                flag = false; // cancel placement
                // revert back all captured blocks
                for (BlockState blockstate : blockstates)
                {
                    p_77943_2_.restoringBlockStates = true;
                    blockstate.update(true, false); // restore blockstate
                    p_77943_2_.restoringBlockStates = false;
                }
                // make sure to restore stack after cancel
                this.setItemDamage(meta);
                this.stackSize = size;
                if (nbt != null)
                {
                    this.setTagCompound(nbt);
                }
            }
            else
            {
                // drop items
                for (int i = 0; i < p_77943_2_.capturedItems.size(); i++)
                {
                    p_77943_2_.spawnEntityInWorld(p_77943_2_.capturedItems.get(i));
                }

                for (BlockState blockstate : blockstates)
                {
                    int x = blockstate.getX();
                    int y = blockstate.getY();
                    int z = blockstate.getZ();
                    int metadata = p_77943_2_.getBlockMetadata(x, y, z);
                    int updateFlag = ((CraftBlockState)blockstate).getFlag();
                    Block oldBlock = CraftMagicNumbers.getBlock(blockstate.getTypeId());
                    Block newBlock = p_77943_2_.getBlock(x, y, z);
                    if (newBlock != null && !(newBlock.hasTileEntity(metadata))) { // Containers get placed automatically
                        newBlock.onBlockAdded(p_77943_2_, x, y, z);
                    }

                    p_77943_2_.markAndNotifyBlock(x, y, z, null, oldBlock, newBlock, updateFlag);
                }
                p_77943_1_.addStat(StatList.objectUseStats[Item.getIdFromItem(this.field_151002_e)], 1);
            }
        }
        p_77943_2_.capturedBlockStates.clear();
        p_77943_2_.capturedItems.clear();
        // Cauldron end

        return flag;
    }

    public float func_150997_a(Block p_150997_1_)
    {
        return this.getItem().func_150893_a(this, p_150997_1_);
    }

    public ItemStack useItemRightClick(World p_77957_1_, EntityPlayer p_77957_2_)
    {
        return this.getItem().onItemRightClick(this, p_77957_1_, p_77957_2_);
    }

    public ItemStack onFoodEaten(World p_77950_1_, EntityPlayer p_77950_2_)
    {
        return this.getItem().onEaten(this, p_77950_1_, p_77950_2_);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound p_77955_1_)
    {
        p_77955_1_.setShort("id", (short)Item.getIdFromItem(this.field_151002_e));
        p_77955_1_.setByte("Count", (byte)this.stackSize);
        p_77955_1_.setShort("Damage", (short)this.itemDamage);

        if (this.stackTagCompound != null)
        {
            p_77955_1_.setTag("tag", this.stackTagCompound);
        }

        return p_77955_1_;
    }

    public void readFromNBT(NBTTagCompound p_77963_1_)
    {
        this.field_151002_e = Item.getItemById(p_77963_1_.getShort("id"));
        this.stackSize = p_77963_1_.getByte("Count");
        this.itemDamage = p_77963_1_.getShort("Damage");

        if (this.itemDamage < 0)
        {
            this.itemDamage = 0;
        }

        if (p_77963_1_.hasKey("tag", 10))
        {
            this.stackTagCompound = p_77963_1_.getCompoundTag("tag");
        }
    }

    public int getMaxStackSize()
    {
        return this.getItem().getItemStackLimit(this);
    }

    public boolean isStackable()
    {
        return this.getMaxStackSize() > 1 && (!this.isItemStackDamageable() || !this.isItemDamaged());
    }

    public boolean isItemStackDamageable()
    {
        return this.field_151002_e.getMaxDamage(this) <= 0 ? false : !this.hasTagCompound() || !this.getTagCompound().getBoolean("Unbreakable");
    }

    public boolean getHasSubtypes()
    {
        return this.field_151002_e.getHasSubtypes();
    }

    public boolean isItemDamaged()
    {
        return this.isItemStackDamageable() && getItem().isDamaged(this);
    }

    public int getItemDamageForDisplay()
    {
        return getItem().getDisplayDamage(this);
    }

    public int getItemDamage()
    {
        return getItem().getDamage(this);
    }

    public void setItemDamage(int p_77964_1_)
    {
        getItem().setDamage(this, p_77964_1_);
    }

    public int getMaxDamage()
    {
        return getItem().getMaxDamage(this);
    }

    // Spigot start

    /**
     * Attempts to damage the ItemStack with par1 amount of damage, If the ItemStack has the Unbreaking enchantment
     * there is a chance for each point of damage to be negated. Returns true if it takes more damage than
     * getMaxDamage(). Returns false otherwise or if the ItemStack can't be damaged or if all points of damage are
     * negated.
     */
    public boolean attemptDamageItem(int p_96631_1_, Random p_96631_2_)
    {
        return isDamaged(p_96631_1_, p_96631_2_, null);
    }

    public boolean isDamaged(int p_96631_1_, Random p_96631_2_, EntityLivingBase entitylivingbase)
    {
        // Spigot end
        if (!this.isItemStackDamageable())
        {
            return false;
        }
        else
        {
            if (p_96631_1_ > 0)
            {
                int j = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, this);
                int k = 0;

                for (int l = 0; j > 0 && l < p_96631_1_; ++l)
                {
                    if (EnchantmentDurability.negateDamage(this, j, p_96631_2_))
                    {
                        ++k;
                    }
                }

                p_96631_1_ -= k;

                // Spigot start
                if (entitylivingbase instanceof EntityPlayerMP)
                {
                    org.bukkit.craftbukkit.inventory.CraftItemStack item = org.bukkit.craftbukkit.inventory.CraftItemStack.asCraftMirror(this);
                    org.bukkit.event.player.PlayerItemDamageEvent event = new org.bukkit.event.player.PlayerItemDamageEvent((org.bukkit.entity.Player) entitylivingbase.getBukkitEntity(), item, p_96631_1_);
                    org.bukkit.Bukkit.getServer().getPluginManager().callEvent(event);

                    if (event.isCancelled())
                    {
                        return false;
                    }

                    p_96631_1_ = event.getDamage();
                }

                // Spigot end

                if (p_96631_1_ <= 0)
                {
                    return false;
                }
            }

            setItemDamage(getItemDamage() + p_96631_1_); //Redirect through Item's callback if applicable.
            return getItemDamage() > getMaxDamage();
        }
    }

    public void damageItem(int p_77972_1_, EntityLivingBase p_77972_2_)
    {
        if (!(p_77972_2_ instanceof EntityPlayer) || !((EntityPlayer)p_77972_2_).capabilities.isCreativeMode)
        {
            if (this.isItemStackDamageable())
            {
                if (this.attemptDamageItem(p_77972_1_, p_77972_2_.getRNG()))
                {
                    p_77972_2_.renderBrokenItemStack(this);
                    --this.stackSize;

                    if (p_77972_2_ instanceof EntityPlayer)
                    {
                        EntityPlayer entityplayer = (EntityPlayer)p_77972_2_;
                        entityplayer.addStat(StatList.objectBreakStats[Item.getIdFromItem(this.field_151002_e)], 1);

                        if (this.stackSize == 0 && this.getItem() instanceof ItemBow)
                        {
                            entityplayer.destroyCurrentEquippedItem();
                        }
                    }

                    if (this.stackSize < 0)
                    {
                        this.stackSize = 0;
                    }

                    // CraftBukkit start - Check for item breaking
                    if (this.stackSize == 0 && p_77972_2_ instanceof EntityPlayer)
                    {
                        org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerItemBreakEvent((EntityPlayer) p_77972_2_, this);
                    }

                    // CraftBukkit end
                    this.itemDamage = 0;
                }
            }
        }
    }

    public void hitEntity(EntityLivingBase p_77961_1_, EntityPlayer p_77961_2_)
    {
        boolean flag = this.field_151002_e.hitEntity(this, p_77961_1_, p_77961_2_);

        if (flag)
        {
            p_77961_2_.addStat(StatList.objectUseStats[Item.getIdFromItem(this.field_151002_e)], 1);
        }
    }

    public void func_150999_a(World p_150999_1_, Block p_150999_2_, int p_150999_3_, int p_150999_4_, int p_150999_5_, EntityPlayer p_150999_6_)
    {
        boolean flag = this.field_151002_e.onBlockDestroyed(this, p_150999_1_, p_150999_2_, p_150999_3_, p_150999_4_, p_150999_5_, p_150999_6_);

        if (flag)
        {
            p_150999_6_.addStat(StatList.objectUseStats[Item.getIdFromItem(this.field_151002_e)], 1);
        }
    }

    public boolean func_150998_b(Block p_150998_1_)
    {
        return getItem().canHarvestBlock(p_150998_1_, this);
    }

    public boolean interactWithEntity(EntityPlayer p_111282_1_, EntityLivingBase p_111282_2_)
    {
        return this.field_151002_e.itemInteractionForEntity(this, p_111282_1_, p_111282_2_);
    }

    public ItemStack copy()
    {
        ItemStack itemstack = new ItemStack(this.field_151002_e, this.stackSize, this.itemDamage);

        if (this.stackTagCompound != null)
        {
            itemstack.stackTagCompound = (NBTTagCompound)this.stackTagCompound.copy();
        }

        return itemstack;
    }

    public static boolean areItemStackTagsEqual(ItemStack p_77970_0_, ItemStack p_77970_1_)
    {
        return p_77970_0_ == null && p_77970_1_ == null ? true : (p_77970_0_ != null && p_77970_1_ != null ? (p_77970_0_.stackTagCompound == null && p_77970_1_.stackTagCompound != null ? false : p_77970_0_.stackTagCompound == null || p_77970_0_.stackTagCompound.equals(p_77970_1_.stackTagCompound)) : false);
    }

    public static boolean areItemStacksEqual(ItemStack p_77989_0_, ItemStack p_77989_1_)
    {
        return p_77989_0_ == null && p_77989_1_ == null ? true : (p_77989_0_ != null && p_77989_1_ != null ? p_77989_0_.isItemStackEqual(p_77989_1_) : false);
    }

    private boolean isItemStackEqual(ItemStack p_77959_1_)
    {
        return this.stackSize != p_77959_1_.stackSize ? false : (this.field_151002_e != p_77959_1_.field_151002_e ? false : (this.itemDamage != p_77959_1_.itemDamage ? false : (this.stackTagCompound == null && p_77959_1_.stackTagCompound != null ? false : this.stackTagCompound == null || this.stackTagCompound.equals(p_77959_1_.stackTagCompound))));
    }

    public boolean isItemEqual(ItemStack p_77969_1_)
    {
        return this.field_151002_e == p_77969_1_.field_151002_e && this.itemDamage == p_77969_1_.itemDamage;
    }

    public String getUnlocalizedName()
    {
        return this.field_151002_e.getUnlocalizedName(this);
    }

    public static ItemStack copyItemStack(ItemStack p_77944_0_)
    {
        return p_77944_0_ == null ? null : p_77944_0_.copy();
    }

    public String toString()
    {
        return this.stackSize + "x" + this.field_151002_e.getUnlocalizedName() + "@" + this.itemDamage;
    }

    public void updateAnimation(World p_77945_1_, Entity p_77945_2_, int p_77945_3_, boolean p_77945_4_)
    {
        if (this.animationsToGo > 0)
        {
            --this.animationsToGo;
        }

        this.field_151002_e.onUpdate(this, p_77945_1_, p_77945_2_, p_77945_3_, p_77945_4_);
    }

    public void onCrafting(World p_77980_1_, EntityPlayer p_77980_2_, int p_77980_3_)
    {
        p_77980_2_.addStat(StatList.objectCraftStats[Item.getIdFromItem(this.field_151002_e)], p_77980_3_);
        this.field_151002_e.onCreated(this, p_77980_1_, p_77980_2_);
    }

    public int getMaxItemUseDuration()
    {
        return this.getItem().getMaxItemUseDuration(this);
    }

    public EnumAction getItemUseAction()
    {
        return this.getItem().getItemUseAction(this);
    }

    public void onPlayerStoppedUsing(World p_77974_1_, EntityPlayer p_77974_2_, int p_77974_3_)
    {
        this.getItem().onPlayerStoppedUsing(this, p_77974_1_, p_77974_2_, p_77974_3_);
    }

    public boolean hasTagCompound()
    {
        return this.stackTagCompound != null;
    }

    public NBTTagCompound getTagCompound()
    {
        return this.stackTagCompound;
    }

    public NBTTagList getEnchantmentTagList()
    {
        return this.stackTagCompound == null ? null : this.stackTagCompound.getTagList("ench", 10);
    }

    public void setTagCompound(NBTTagCompound p_77982_1_)
    {
        // Cauldron - do not alter name of compound. Fixes Ars Magica 2 Spellbooks
        this.stackTagCompound = p_77982_1_;
    }

    public String getDisplayName()
    {
        String s = this.getItem().getItemStackDisplayName(this);

        if (this.stackTagCompound != null && this.stackTagCompound.hasKey("display", 10))
        {
            NBTTagCompound nbttagcompound = this.stackTagCompound.getCompoundTag("display");

            if (nbttagcompound.hasKey("Name", 8))
            {
                s = nbttagcompound.getString("Name");
            }
        }

        return s;
    }

    public ItemStack setStackDisplayName(String p_151001_1_)
    {
        if (this.stackTagCompound == null)
        {
            this.stackTagCompound = new NBTTagCompound();
        }

        if (!this.stackTagCompound.hasKey("display", 10))
        {
            this.stackTagCompound.setTag("display", new NBTTagCompound());
        }

        this.stackTagCompound.getCompoundTag("display").setString("Name", p_151001_1_);
        return this;
    }

    public void func_135074_t()
    {
        if (this.stackTagCompound != null)
        {
            if (this.stackTagCompound.hasKey("display", 10))
            {
                NBTTagCompound nbttagcompound = this.stackTagCompound.getCompoundTag("display");
                nbttagcompound.removeTag("Name");

                if (nbttagcompound.hasNoTags())
                {
                    this.stackTagCompound.removeTag("display");

                    if (this.stackTagCompound.hasNoTags())
                    {
                        this.setTagCompound((NBTTagCompound)null);
                    }
                }
            }
        }
    }

    public boolean hasDisplayName()
    {
        return this.stackTagCompound == null ? false : (!this.stackTagCompound.hasKey("display", 10) ? false : this.stackTagCompound.getCompoundTag("display").hasKey("Name", 8));
    }

    @SideOnly(Side.CLIENT)
    public List getTooltip(EntityPlayer p_82840_1_, boolean p_82840_2_)
    {
        ArrayList arraylist = new ArrayList();
        String s = this.getDisplayName();

        if (this.hasDisplayName())
        {
            s = EnumChatFormatting.ITALIC + s + EnumChatFormatting.RESET;
        }

        int i;

        if (p_82840_2_)
        {
            String s1 = "";

            if (s.length() > 0)
            {
                s = s + " (";
                s1 = ")";
            }

            i = Item.getIdFromItem(this.field_151002_e);

            if (this.getHasSubtypes())
            {
                s = s + String.format("#%04d/%d%s", new Object[] {Integer.valueOf(i), Integer.valueOf(this.itemDamage), s1});
            }
            else
            {
                s = s + String.format("#%04d%s", new Object[] {Integer.valueOf(i), s1});
            }
        }
        else if (!this.hasDisplayName() && this.field_151002_e == Items.filled_map)
        {
            s = s + " #" + this.itemDamage;
        }

        arraylist.add(s);
        this.field_151002_e.addInformation(this, p_82840_1_, arraylist, p_82840_2_);

        if (this.hasTagCompound())
        {
            NBTTagList nbttaglist = this.getEnchantmentTagList();

            if (nbttaglist != null)
            {
                for (i = 0; i < nbttaglist.tagCount(); ++i)
                {
                    short short1 = nbttaglist.getCompoundTagAt(i).getShort("id");
                    short short2 = nbttaglist.getCompoundTagAt(i).getShort("lvl");

                    if (Enchantment.enchantmentsList[short1] != null)
                    {
                        arraylist.add(Enchantment.enchantmentsList[short1].getTranslatedName(short2));
                    }
                }
            }

            if (this.stackTagCompound.hasKey("display", 10))
            {
                NBTTagCompound nbttagcompound = this.stackTagCompound.getCompoundTag("display");

                if (nbttagcompound.hasKey("color", 3))
                {
                    if (p_82840_2_)
                    {
                        arraylist.add("Color: #" + Integer.toHexString(nbttagcompound.getInteger("color")).toUpperCase());
                    }
                    else
                    {
                        arraylist.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("item.dyed"));
                    }
                }

                if (nbttagcompound.func_150299_b("Lore") == 9)
                {
                    NBTTagList nbttaglist1 = nbttagcompound.getTagList("Lore", 8);

                    if (nbttaglist1.tagCount() > 0)
                    {
                        for (int j = 0; j < nbttaglist1.tagCount(); ++j)
                        {
                            arraylist.add(EnumChatFormatting.DARK_PURPLE + "" + EnumChatFormatting.ITALIC + nbttaglist1.getStringTagAt(j));
                        }
                    }
                }
            }
        }

        Multimap multimap = this.getAttributeModifiers();

        if (!multimap.isEmpty())
        {
            arraylist.add("");
            Iterator iterator = multimap.entries().iterator();

            while (iterator.hasNext())
            {
                Entry entry = (Entry)iterator.next();
                AttributeModifier attributemodifier = (AttributeModifier)entry.getValue();
                double d0 = attributemodifier.getAmount();

                if (attributemodifier.getID() == Item.field_111210_e)
                {
                    d0 += (double)EnchantmentHelper.func_152377_a(this, EnumCreatureAttribute.UNDEFINED);
                }

                double d1;

                if (attributemodifier.getOperation() != 1 && attributemodifier.getOperation() != 2)
                {
                    d1 = d0;
                }
                else
                {
                    d1 = d0 * 100.0D;
                }

                if (d0 > 0.0D)
                {
                    arraylist.add(EnumChatFormatting.BLUE + StatCollector.translateToLocalFormatted("attribute.modifier.plus." + attributemodifier.getOperation(), new Object[] {field_111284_a.format(d1), StatCollector.translateToLocal("attribute.name." + (String)entry.getKey())}));
                }
                else if (d0 < 0.0D)
                {
                    d1 *= -1.0D;
                    arraylist.add(EnumChatFormatting.RED + StatCollector.translateToLocalFormatted("attribute.modifier.take." + attributemodifier.getOperation(), new Object[] {field_111284_a.format(d1), StatCollector.translateToLocal("attribute.name." + (String)entry.getKey())}));
                }
            }
        }

        if (this.hasTagCompound() && this.getTagCompound().getBoolean("Unbreakable"))
        {
            arraylist.add(EnumChatFormatting.BLUE + StatCollector.translateToLocal("item.unbreakable"));
        }

        if (p_82840_2_ && this.isItemDamaged())
        {
            arraylist.add("Durability: " + (this.getMaxDamage() - this.getItemDamageForDisplay()) + " / " + this.getMaxDamage());
        }
        ForgeEventFactory.onItemTooltip(this, p_82840_1_, arraylist, p_82840_2_);

        return arraylist;
    }

    @Deprecated
    @SideOnly(Side.CLIENT)
    public boolean hasEffect()
    {
        return hasEffect(0);
    }

    @SideOnly(Side.CLIENT)
    public boolean hasEffect(int pass)
    {
        return this.getItem().hasEffect(this, pass);
    }

    public EnumRarity getRarity()
    {
        return this.getItem().getRarity(this);
    }

    public boolean isItemEnchantable()
    {
        return !this.getItem().isItemTool(this) ? false : !this.isItemEnchanted();
    }

    public void addEnchantment(Enchantment p_77966_1_, int p_77966_2_)
    {
        if (this.stackTagCompound == null)
        {
            this.setTagCompound(new NBTTagCompound());
        }

        if (!this.stackTagCompound.hasKey("ench", 9))
        {
            this.stackTagCompound.setTag("ench", new NBTTagList());
        }

        NBTTagList nbttaglist = this.stackTagCompound.getTagList("ench", 10);
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setShort("id", (short)p_77966_1_.effectId);
        nbttagcompound.setShort("lvl", (short)((byte)p_77966_2_));
        nbttaglist.appendTag(nbttagcompound);
    }

    public boolean isItemEnchanted()
    {
        return this.stackTagCompound != null && this.stackTagCompound.hasKey("ench", 9);
    }

    public void setTagInfo(String p_77983_1_, NBTBase p_77983_2_)
    {
        if (this.stackTagCompound == null)
        {
            this.setTagCompound(new NBTTagCompound());
        }

        this.stackTagCompound.setTag(p_77983_1_, p_77983_2_);
    }

    public boolean canEditBlocks()
    {
        return this.getItem().canItemEditBlocks();
    }

    public boolean isOnItemFrame()
    {
        return this.itemFrame != null;
    }

    public void setItemFrame(EntityItemFrame p_82842_1_)
    {
        this.itemFrame = p_82842_1_;
    }

    public EntityItemFrame getItemFrame()
    {
        return this.itemFrame;
    }

    public int getRepairCost()
    {
        return this.hasTagCompound() && this.stackTagCompound.hasKey("RepairCost", 3) ? this.stackTagCompound.getInteger("RepairCost") : 0;
    }

    public void setRepairCost(int p_82841_1_)
    {
        if (!this.hasTagCompound())
        {
            this.stackTagCompound = new NBTTagCompound();
        }

        this.stackTagCompound.setInteger("RepairCost", p_82841_1_);
    }

    public Multimap getAttributeModifiers()
    {
        Object object;

        if (this.hasTagCompound() && this.stackTagCompound.hasKey("AttributeModifiers", 9))
        {
            object = HashMultimap.create();
            NBTTagList nbttaglist = this.stackTagCompound.getTagList("AttributeModifiers", 10);

            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
                AttributeModifier attributemodifier = SharedMonsterAttributes.readAttributeModifierFromNBT(nbttagcompound);

                if (attributemodifier.getID().getLeastSignificantBits() != 0L && attributemodifier.getID().getMostSignificantBits() != 0L)
                {
                    ((Multimap)object).put(nbttagcompound.getString("AttributeName"), attributemodifier);
                }
            }
        }
        else
        {
            object = this.getItem().getAttributeModifiers(this);
        }

        return (Multimap)object;
    }

    public void func_150996_a(Item p_150996_1_)
    {
        this.field_151002_e = p_150996_1_;
    }

    public IChatComponent func_151000_E()
    {
        IChatComponent ichatcomponent = (new ChatComponentText("[")).appendText(this.getDisplayName()).appendText("]");

        if (this.field_151002_e != null)
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            this.writeToNBT(nbttagcompound);
            ichatcomponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new ChatComponentText(nbttagcompound.toString())));
            ichatcomponent.getChatStyle().setColor(this.getRarity().rarityColor);
        }

        return ichatcomponent;
    }
}