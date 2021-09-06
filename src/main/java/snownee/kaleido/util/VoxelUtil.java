package snownee.kaleido.util;

import java.awt.geom.Point2D;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

@Deprecated
public final class VoxelUtil {
	private VoxelUtil() {
	}

	public static AxisAlignedBB rotate(AxisAlignedBB aabb, Direction facing) {
		Point2D.Double pointMin = rotate(new Point2D.Double(aabb.minX, aabb.minZ), facing);
		Point2D.Double pointMax = rotate(new Point2D.Double(aabb.maxX, aabb.maxZ), facing);
		return new AxisAlignedBB(pointMin.x, aabb.minY, pointMin.y, pointMax.x, aabb.maxY, pointMax.y);
	}

	public static VoxelShape rotate(VoxelShape shape, Direction facing) {
		if (shape.isEmpty() || shape == VoxelShapes.block())
			return shape;
		VSBuilder builder = new VSBuilder();
		shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
			Point2D.Double pointMin = rotate(new Point2D.Double(Math.min(x1, x2), Math.min(z1, z2)), facing);
			Point2D.Double pointMax = rotate(new Point2D.Double(Math.max(x1, x2), Math.max(z1, z2)), facing);
			builder.add(VoxelShapes.box(pointMin.x, Math.min(y1, y2), pointMin.y, pointMax.x, Math.max(y1, y2), pointMax.y));
		});
		return builder.get();
	}

	public static Point2D.Double rotate(Point2D.Double point, Direction facing) {
		double x = point.x - 0.5;
		double y = point.y - 0.5;
		Point2D.Double pointNew = new Point2D.Double();
		pointNew.x = facing.get2DDataValue() % 2 == 0 ? x : y;
		if (facing.get2DDataValue() < 2) {
			pointNew.x *= -1;
		}
		pointNew.y = facing.get2DDataValue() % 2 == 0 ? y : x;
		if (facing.get2DDataValue() == 1 || facing.get2DDataValue() == 2) {
			pointNew.y *= -1;
		}
		pointNew.x += 0.5;
		pointNew.y += 0.5;
		return pointNew;
	}

	private static class VSBuilder {
		VoxelShape shape = VoxelShapes.empty();

		public void add(VoxelShape newShape) {
			shape = VoxelShapes.or(shape, newShape);
		}

		public VoxelShape get() {
			return shape.optimize();
		}
	}

	//    public static int rayTraceByDistance(EntityPlayer player, List<AxisAlignedBB> aabbs)
	//    {
	//        Vec3d posPlayer = player.getPositionEyes(1);
	//        List<AxisAlignedBB> sorted = Lists.newArrayList(aabbs);
	//        sorted.sort(Comparator.comparingDouble(o -> getCenter(o).squareDistanceTo(posPlayer)));
	//        for (AxisAlignedBB aabb : sorted)
	//        {
	//            if (rayTrace(player, aabb) != null)
	//            {
	//                return aabbs.indexOf(aabb);
	//            }
	//        }
	//        return -1;
	//    }
	//
	//    public static RayTraceResult rayTrace(EntityPlayer player, AxisAlignedBB aabb)
	//    {
	//        Vec3d posPlayer = player.getPositionEyes(1);
	//        double distance = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
	//        Vec3d posEnd = posPlayer.add(player.getLookVec().scale(distance));
	//        return aabb.calculateIntercept(posPlayer, posEnd);
	//    }
	//
	//    public static Vec3d getCenter(AxisAlignedBB aabb)
	//    {
	//        return new Vec3d(aabb.minX + (aabb.maxX - aabb.minX) * 0.5D, aabb.minY + (aabb.maxY - aabb.minY) * 0.5D, aabb.minZ + (aabb.maxZ - aabb.minZ) * 0.5D);
	//    }
}
