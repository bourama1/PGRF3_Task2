import lwjglutils.OGLBuffers;

public class Grid {
    private final OGLBuffers buffers;
    private float[] vertices;
    private int[] indices;

    /**
     * @param m vertex count in row
     * @param n vertex count in column
     *          Creates a new vertex buffer and index buffer for grid based on type of topology
     */
    public Grid(final int m, final int n) {
        createVB(m, n);
        createIB(m, n);

        OGLBuffers.Attrib[] attrs = new OGLBuffers.Attrib[]{
                new OGLBuffers.Attrib("inPosition", 2),
        };

        this.buffers = new OGLBuffers(vertices, attrs, indices);
    }

    /**
     * @param m vertex count in row
     * @param n vertex count in column
     *          Creates a new vertex buffer
     */
    private void createVB(final int n, final int m) {
        vertices = new float[2 * m * n];

        // Vertices <0;1>
        int index = 0;
        for (int i = 0; i < m; i += 1) {
            for (int j = 0; j < n; j += 1) {
                vertices[index++] = j / (float) (n - 1);
                vertices[index++] = i / (float) (m - 1);
            }
        }
    }

    /**
     * GL_TRIANGLE_STRIP
     *
     * @param m vertex count in row
     * @param n vertex count in column
     *          Creates a new index buffer for strip topology
     */
    private void createIB(int m, int n) {
        indices = new int[2 * m * (n - 1) + (n - 2)];

        // Indices
        int index = 0;
        for (int i = 0; i < m - 1; i++) {
            int offset = i * m;
            for (int j = 0; j < n - 1; j++) {
                if (j == 0) {
                    indices[index++] = j + offset;
                    indices[index++] = (j + n) + offset;
                }
                indices[index++] = (j + 1) + offset;
                indices[index++] = (j + n + 1) + offset;
            }
            if (i != m - 2)
                indices[index++] = 65535;
        }
    }

    public OGLBuffers getBuffers() {
        return buffers;
    }
}
