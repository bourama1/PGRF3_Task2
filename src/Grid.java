import lwjglutils.OGLBuffers;

import java.sql.SQLOutput;

public class Grid {

    /**
     * GL_TRIANGLES
     *
     * @param m vertex count in row
     * @param n vertex count in column
     */
    private OGLBuffers buffers;

    public Grid(final int m, final int n) {
        float[] vertices = new float[2 * m * n];
        int[] indices = new int[3 * 2 * (m - 1) * (n - 1)];

        // vertices <0;1>
        int index = 0;
        for (int i = 0; i < m; i += 1) {
            for (int j = 0; j < n; j += 1) {
                System.out.println("x: " + j / (float) (n - 1));
                System.out.println("y: " + i / (float) (m - 1));
                System.out.println("-");
                // Naplnit vertices
                vertices[index++] = j / (float) (n - 1);
                vertices[index++] = i / (float) (m - 1);
            }
            System.out.println("----------");
        }

        System.out.println("Indices");
        // Indices
        // TODO: pozor na orientaci
        int indicesIndex = 0;
        for (int i = 0; i < m - 1; i++) {
            int offset = (i * m);
            for (int j = 0; j < n - 1; j++) {
                int a = offset + j;
                int b = offset + j + n;
                int c = offset + j + 1;
                int d = offset + j + n + 1;

                // ABC
                System.out.println(a);
                indices[indicesIndex++] = a;
                System.out.println(b);
                indices[indicesIndex++] = b;
                System.out.println(c);
                indices[indicesIndex++] = c;

                System.out.println("-");
                // BCD
                System.out.println(b);
                indices[indicesIndex++] = b;
                System.out.println(c);
                indices[indicesIndex++] = c;
                System.out.println(d);
                indices[indicesIndex++] = d;

                System.out.println("-----");
            }
            System.out.println("---------------------");
        }


        OGLBuffers.Attrib[] attribs = new OGLBuffers.Attrib[] {
                new OGLBuffers.Attrib("inPosition", 2),
        };

        buffers = new OGLBuffers(vertices, attribs, indices);

    }

    public OGLBuffers getBuffers() {
        return buffers;
    }
}
