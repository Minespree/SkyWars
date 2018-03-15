package net.minespree.skywars.game.decay;

import net.minespree.cartographer.util.GameLocation;
import net.minespree.skywars.game.SkyWarsMapData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NormalIslandDecay implements IslandDecay {

    private SkyWarsMapData data;
    private Location centre;
    private double radius;

    public NormalIslandDecay(SkyWarsMapData data, GameLocation centre, double radius) {
        this.data = data;
        this.centre = centre.toLocation();
        this.radius = radius;
    }

    @Override
    public void step() {
        radius--;
        if(radius > 0.0) {
            check(getCircumference(radius));
        }
    }

    public void check(List<Block> blocks) {
        for (Block block : blocks) {
            Optional<Block> highest = getHighest(block);
            if(highest.isPresent()) {
                MaterialData data = highest.get().getState().getData();
                block.setType(Material.AIR);
                block.getWorld().spawnFallingBlock(highest.get().getLocation(), data.getItemType(), data.getData());
            }
            for (int i = 0; i < 255; i++) {
                Block b = block.getWorld().getBlockAt(block.getX(), i, block.getZ());
                if(b.getType() != Material.AIR) {
                    b.setType(Material.AIR);
                }
            }
        }
    }

    public List<Block> getCircumference(double radius) {
        List<Block> locations = new ArrayList<>();
        for (int i = 0; i < 360; i++) {
            double angle = (i * Math.PI / 180);
            int x = (int) (radius * Math.cos(angle));
            int z = (int) (radius * Math.sin(angle));
            Block block = centre.getWorld().getBlockAt(centre.getBlockX() + x, centre.getBlockY(), centre.getBlockZ() + z);
            for (BlockFace face : BlockFace.values()) {
                if(face != BlockFace.UP && face != BlockFace.DOWN) {
                    locations.add(block.getRelative(face));
                }
            }
        }
        return locations;
    }

    public Optional<Block> getHighest(Block block) {
        Block highest = null;
        for (int y = 0; y < 256; y++) {
            Block b = block.getWorld().getBlockAt(block.getX(), y, block.getZ());
            if(b.getType() != Material.AIR) {
                highest = b;
            }
        }
        return Optional.ofNullable(highest);
    }

}
