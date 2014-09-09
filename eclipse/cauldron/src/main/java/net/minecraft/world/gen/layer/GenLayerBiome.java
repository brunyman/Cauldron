package net.minecraft.world.gen.layer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.WeightedRandom;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.BiomeManager.BiomeEntry;

public class GenLayerBiome extends GenLayer
{
    private List<BiomeEntry> desertBiomes = new ArrayList<BiomeEntry>();
    private List<BiomeEntry> warmBiomes = new ArrayList<BiomeEntry>();
    private List<BiomeEntry> coolBiomes = new ArrayList<BiomeEntry>();
    private List<BiomeEntry> icyBiomes = new ArrayList<BiomeEntry>();
    
    private static final String __OBFID = "CL_00000555";

    public GenLayerBiome(long p_i2122_1_, GenLayer p_i2122_3_, WorldType p_i2122_4_)
    {
        super(p_i2122_1_);
        
        this.parent = p_i2122_3_;
        
        this.desertBiomes.addAll(BiomeManager.desertBiomes);
        this.warmBiomes.addAll(BiomeManager.warmBiomes);
        this.coolBiomes.addAll(BiomeManager.coolBiomes);
        this.icyBiomes.addAll(BiomeManager.icyBiomes);
        
        if (p_i2122_4_ == WorldType.DEFAULT_1_1)
        {
            desertBiomes.add(new BiomeEntry(BiomeGenBase.desert, 10));
            desertBiomes.add(new BiomeEntry(BiomeGenBase.forest, 10));
            desertBiomes.add(new BiomeEntry(BiomeGenBase.extremeHills, 10));
            desertBiomes.add(new BiomeEntry(BiomeGenBase.swampland, 10));
            desertBiomes.add(new BiomeEntry(BiomeGenBase.plains, 10));
            desertBiomes.add(new BiomeEntry(BiomeGenBase.taiga, 10));
        }
        else
        {
            desertBiomes.add(new BiomeEntry(BiomeGenBase.desert, 30));
            desertBiomes.add(new BiomeEntry(BiomeGenBase.savanna, 20));
            desertBiomes.add(new BiomeEntry(BiomeGenBase.plains, 10));
        }
    }

    public int[] getInts(int p_75904_1_, int p_75904_2_, int p_75904_3_, int p_75904_4_)
    {
        int[] aint = this.parent.getInts(p_75904_1_, p_75904_2_, p_75904_3_, p_75904_4_);
        int[] aint1 = IntCache.getIntCache(p_75904_3_ * p_75904_4_);

        for (int i1 = 0; i1 < p_75904_4_; ++i1)
        {
            for (int j1 = 0; j1 < p_75904_3_; ++j1)
            {
                this.initChunkSeed((long)(j1 + p_75904_1_), (long)(i1 + p_75904_2_));
                int k1 = aint[j1 + i1 * p_75904_3_];
                int l1 = (k1 & 3840) >> 8;
                k1 &= -3841;

                if (isBiomeOceanic(k1))
                {
                    aint1[j1 + i1 * p_75904_3_] = k1;
                }
                else if (k1 == BiomeGenBase.mushroomIsland.biomeID)
                {
                    aint1[j1 + i1 * p_75904_3_] = k1;
                }
                else if (k1 == 1)
                {
                    if (l1 > 0)
                    {
                        if (this.nextInt(3) == 0)
                        {
                            aint1[j1 + i1 * p_75904_3_] = BiomeGenBase.mesaPlateau.biomeID;
                        }
                        else
                        {
                            aint1[j1 + i1 * p_75904_3_] = BiomeGenBase.mesaPlateau_F.biomeID;
                        }
                    }
                    else
                    {
                        aint1[j1 + i1 * p_75904_3_] = ((BiomeEntry)WeightedRandom.getItem(this.desertBiomes, (int)(this.nextLong(WeightedRandom.getTotalWeight(this.desertBiomes) / 10) * 10))).biome.biomeID;
                    }
                }
                else if (k1 == 2)
                {
                    if (l1 > 0)
                    {
                        aint1[j1 + i1 * p_75904_3_] = BiomeGenBase.jungle.biomeID;
                    }
                    else
                    {
                        aint1[j1 + i1 * p_75904_3_] = ((BiomeEntry)WeightedRandom.getItem(this.warmBiomes, (int)(this.nextLong(WeightedRandom.getTotalWeight(this.warmBiomes) / 10) * 10))).biome.biomeID;
                    }
                }
                else if (k1 == 3)
                {
                    if (l1 > 0)
                    {
                        aint1[j1 + i1 * p_75904_3_] = BiomeGenBase.megaTaiga.biomeID;
                    }
                    else
                    {
                        aint1[j1 + i1 * p_75904_3_] = ((BiomeEntry)WeightedRandom.getItem(this.coolBiomes, (int)(this.nextLong(WeightedRandom.getTotalWeight(this.coolBiomes) / 10) * 10))).biome.biomeID;
                    }
                }
                else if (k1 == 4)
                {
                    aint1[j1 + i1 * p_75904_3_] = ((BiomeEntry)WeightedRandom.getItem(this.icyBiomes, (int)(this.nextLong(WeightedRandom.getTotalWeight(this.icyBiomes) / 10) * 10))).biome.biomeID;
                }
                else
                {
                    aint1[j1 + i1 * p_75904_3_] = BiomeGenBase.mushroomIsland.biomeID;
                }
            }
        }

        return aint1;
    }
}