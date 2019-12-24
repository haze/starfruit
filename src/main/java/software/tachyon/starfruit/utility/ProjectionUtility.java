package software.tachyon.starfruit.utility;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import software.tachyon.starfruit.StarfruitMod;

import java.nio.FloatBuffer;
import java.util.Optional;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
// import static org.lwjgl.util.glu.Project.gluProject;

public final class ProjectionUtility {
    private static final int PROJECTION_MATRIX_PLANE_RATIO_INDEX = 10;
    private static final int PROJECTION_MATRIX_FAR_PLANE_INDEX = 14;

    private static final int VIEWPORT_MATRIX_X_OFFSET_INDEX = 0;
    private static final int VIEWPORT_MATRIX_Y_OFFSET_INDEX = 1;
    private static final int VIEWPORT_MATRIX_WIDTH_INDEX = 2;
    private static final int VIEWPORT_MATRIX_HEIGHT_INDEX = 3;

    private static final float PLANE_RATIO = -1F;
    private static final float FAR_PLANE_DISTANCE = -6E7F * MathHelper.SQUARE_ROOT_OF_TWO * 2F;

    private static final int SCREEN_COORDS_X_INDEX = 0;
    private static final int SCREEN_COORDS_Y_INDEX = 1;
    private static final int SCREEN_COORDS_Z_INDEX = 2;

    // private static final IntBuffer VIEWPORT =
    // GlAllocationUtils.allocateByteBuffer(16 << 2).asIntBuffer();
    private static final int[] VIEWPORT = new int[4];
    private static final FloatBuffer MODELVIEW = GlAllocationUtils.allocateFloatBuffer(16);
    private static final FloatBuffer PROJECTION = GlAllocationUtils.allocateFloatBuffer(16);

    private static final Vector3f SCREEN_COORDS = new Vector3f();

    private static int scaledWidth, scaledHeight;
    private static double cameraX, cameraY, cameraZ;

    public static void updateViewport(Window window) {
        scaledWidth = window.getScaledWidth();
        scaledHeight = window.getScaledHeight();

        Camera camera = StarfruitMod.minecraft.gameRenderer.getCamera();
        Vec3d cameraPos = camera == null ? Vec3d.ZERO : camera.getPos();

        cameraX = cameraPos.x;
        cameraY = cameraPos.y;
        cameraZ = cameraPos.z;

        glGetFloatv(GL_MODELVIEW_MATRIX, MODELVIEW);
        glGetFloatv(GL_PROJECTION_MATRIX, PROJECTION);

        PROJECTION.put(PROJECTION_MATRIX_PLANE_RATIO_INDEX, PLANE_RATIO);
        PROJECTION.put(PROJECTION_MATRIX_FAR_PLANE_INDEX, FAR_PLANE_DISTANCE);

        // VIEWPORT.put(VIEWPORT_MATRIX_Y_OFFSET_INDEX, scaledHeight);
        // VIEWPORT.put(VIEWPORT_MATRIX_WIDTH_INDEX, scaledWidth);
        // VIEWPORT.put(VIEWPORT_MATRIX_HEIGHT_INDEX, -scaledHeight);
        VIEWPORT[VIEWPORT_MATRIX_X_OFFSET_INDEX] = scaledHeight;
        VIEWPORT[VIEWPORT_MATRIX_Y_OFFSET_INDEX] = scaledHeight;
        VIEWPORT[VIEWPORT_MATRIX_WIDTH_INDEX] = scaledWidth;
        VIEWPORT[VIEWPORT_MATRIX_HEIGHT_INDEX] = -scaledHeight;
    }

    public static Optional<Vector3f> project(double x, double y, double z, boolean allowOffScreen) {
        final Matrix4f m = new Matrix4f();
        final Vector3f out = new Vector3f();
        System.out.printf("%f, %f, %f\n", cameraX, cameraY, cameraZ);
        m.perspective((float) Math.toRadians(45), (float) scaledWidth / scaledHeight, 0.1f, 100f)
                .project((float) (x - cameraX), (float) (y - cameraY), (float) (z - cameraZ), VIEWPORT, out);
        if (out.z > 0) {
            if (!allowOffScreen)
                return Optional.empty();
            // out.set(scaledWidth - out.x, scaledHeight - out.y, out.z);
        }
        return Optional.of(out);
    }
}
