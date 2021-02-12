package software.tachyon.starfruit.utility;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import software.tachyon.starfruit.StarfruitMod;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

import static org.lwjgl.opengl.GL11.GL_PROJECTION_MATRIX;
import static org.lwjgl.opengl.GL11.glGetFloatv;
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
    private static final IntBuffer VIEWPORT = GlAllocationUtils.allocateByteBuffer(32).asIntBuffer();
    private static final FloatBuffer MODELVIEW = GlAllocationUtils.allocateFloatBuffer(16);
    private static final FloatBuffer PROJECTION = GlAllocationUtils.allocateFloatBuffer(16);

    private static final FloatBuffer SCREEN_COORDS = GlAllocationUtils.allocateFloatBuffer(3);

    private static int scaledWidth, scaledHeight;
    private static double cameraX, cameraY, cameraZ;

    public static void updateViewport(MatrixStack matrixStack, Window window) {
        scaledWidth = window.getScaledWidth();
        scaledHeight = window.getScaledHeight();

        Camera camera = StarfruitMod.minecraft.gameRenderer.getCamera();
        Vec3d cameraPos = camera == null ? Vec3d.ZERO : camera.getPos();

        cameraX = cameraPos.x;
        cameraY = cameraPos.y;
        cameraZ = cameraPos.z;

        matrixStack.peek().getModel().writeToBuffer(MODELVIEW);

        glGetFloatv(GL_PROJECTION_MATRIX, PROJECTION);

        PROJECTION.put(PROJECTION_MATRIX_PLANE_RATIO_INDEX, PLANE_RATIO);
        PROJECTION.put(PROJECTION_MATRIX_FAR_PLANE_INDEX, FAR_PLANE_DISTANCE);

        VIEWPORT.put(VIEWPORT_MATRIX_Y_OFFSET_INDEX, scaledHeight);
        VIEWPORT.put(VIEWPORT_MATRIX_WIDTH_INDEX, scaledWidth);
        VIEWPORT.put(VIEWPORT_MATRIX_HEIGHT_INDEX, -scaledHeight);
    }

    private static void __gluMultMatrixVecf(FloatBuffer m, float[] in, float[] out) {
        for (int i = 0; i < 4; ++i) {
            out[i] = in[0] * m.get(m.position() + 0 + i) + in[1] * m.get(m.position() + 4 + i)
                    + in[2] * m.get(m.position() + 8 + i) + in[3] * m.get(m.position() + 12 + i);
        }
    }

    private static final float[] in = new float[4];
    private static final float[] out = new float[4];

    public static boolean gluProject(float objx, float objy, float objz, FloatBuffer modelMatrix,
            FloatBuffer projMatrix, IntBuffer viewport, FloatBuffer win_pos) {

        float[] in = ProjectionUtility.in;
        float[] out = ProjectionUtility.out;

        in[0] = objx;
        in[1] = objy;
        in[2] = objz;
        in[3] = 1.0f;

        __gluMultMatrixVecf(modelMatrix, in, out);
        __gluMultMatrixVecf(projMatrix, out, in);

        if (in[3] == 0.0)
            return false;

        in[3] = (1.0f / in[3]) * 0.5f;

        // Map x, y and z to range 0-1
        in[0] = in[0] * in[3] + 0.5f;
        in[1] = in[1] * in[3] + 0.5f;
        in[2] = in[2] * in[3] + 0.5f;

        // Map x,y to viewport
        win_pos.put(0, in[0] * viewport.get(viewport.position() + 2) + viewport.get(viewport.position() + 0));
        win_pos.put(1, in[1] * viewport.get(viewport.position() + 3) + viewport.get(viewport.position() + 1));
        win_pos.put(2, in[2]);

        return true;
    }

    public static Optional<Vector3f> project(Vector3d vec, boolean allowOffScreen) {
        return project(vec.x, vec.y, vec.z, allowOffScreen);
    }

    public static Optional<Vector3f> project(Vec3d vec, boolean allowOffScreen) {
        return project(vec.x, vec.y, vec.z, allowOffScreen);
    }

    public static Optional<Vector3f> project(Vector3f vec, boolean allowOffScreen) {
        return project((double) vec.getX(), (double) vec.getY(), (double) vec.getZ(), allowOffScreen);
    }

    public static Optional<Vector3f> project(double x, double y, double z, boolean allowOffScreen) {
        final Vector3f out = new Vector3f(-1, -1, -1);
        gluProject((float) (x - cameraX), (float) (y - cameraY), (float) (z - cameraZ), MODELVIEW, PROJECTION, VIEWPORT,
                SCREEN_COORDS);
        out.set(SCREEN_COORDS.get(SCREEN_COORDS_X_INDEX), SCREEN_COORDS.get(SCREEN_COORDS_Y_INDEX),
                SCREEN_COORDS.get(SCREEN_COORDS_Z_INDEX));

        // m.perspective((float) Math.toRadians(45), (float) scaledWidth / scaledHeight,
        // 0.1f, 100f)
        // .project((float) (x - cameraX), (float) (y - cameraY), (float) (z - cameraZ),
        // VIEWPORT, out);
        if (out.getZ() > 0) {
            if (!allowOffScreen)
                return Optional.empty();
            out.set(scaledWidth - out.getX(), scaledHeight - out.getY(), out.getZ());
        }
        return Optional.of(out);
    }
}
