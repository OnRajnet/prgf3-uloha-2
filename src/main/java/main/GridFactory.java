package main;

import lwjglutils.OGLBuffers;

/**
 * @author PGRF FIM UHK
 * @version 2.0
 * @since 2019-09-02
 */
public class GridFactory {

    /**
     * @param m počet vrcholů v řádku
     * @param n počet vrcholů ve sloupci
     * @return
     */
    public static OGLBuffers generateGrid(int m, int n) {
        float[] vb = generateVertexBuffer(m, n);

        int[] ib = new int[(m - 1) * (n - 1) * 2 * 3];
        int index = 0;
        for (int i = 0; i < n - 1; i++) {
            int row = i * m;
            for (int j = 0; j < m - 1; j++) {
                ib[index++] = j + row;
                ib[index++] = j + 1 + row;
                ib[index++] = j + m + row;

                ib[index++] = j + m + row;
                ib[index++] = j + 1 + row;
                ib[index++] = j + m + 1 + row;
            }
        }

        return getBuffers(vb, ib);
    }

    // https://www.learnopengles.com/tag/triangle-strips/
    public static OGLBuffers generateStripGrid(int m, int n) {
        float[] vb = generateVertexBuffer(m, n);

        int numStripsRequired = n - 1;
        int numDegensRequired = 2 * (numStripsRequired - 1);
        int verticesPerStrip = 2 * m;

        int[] ib = new int[(verticesPerStrip * numStripsRequired) + numDegensRequired];
        int index = 0;
        for (int i = 0; i < n - 1; i++) {
            if (i > 0) {
                // Degenerate begin: repeat first vertex
                ib[index++] = i * n;
            }
            for (int j = 0; j < m; j++) {
                // One part of the strip
                ib[index++] = (i * n) + j;
                ib[index++] = ((i + 1) * n) + j;
            }
            if (i < n - 2) {
                // Degenerate end: repeat last vertex
                ib[index++] = ((i + 1) * n) + m - 1;
            }
        }

        return getBuffers(vb, ib);
    }

    private static float[] generateVertexBuffer(int m, int n) {
        float[] vb = new float[m * n * 2];
        int index = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                vb[index++] = j / (float) (m - 1);
                vb[index++] = i / (float) (n - 1);
            }
        }
        return vb;
    }

    private static OGLBuffers getBuffers(float[] vb, int[] ib) {
        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2)
        };
        return new OGLBuffers(vb, attributes, ib);
    }

}
