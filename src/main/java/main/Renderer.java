package main;

import lwjglutils.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import transforms.*;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.glClearColor;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

/**
 * @author PGRF FIM UHK
 * @version 2.0
 * @since 2019-09-02
 */
public class Renderer extends AbstractRenderer {

    private int calcShader;
    private int calcLocUnit;
    private int calcLocDamp;

    private int renderShader;
    private int locViewProj;

    private OGLBuffers buffers;

    private Camera camera;
    private Mat4 projection;

    private OGLRenderTarget calcTarget;

    private final double CAM_SPEED = 0.5;

    private OGLTexture2D waveHeightTexture;
    private int GRID_SIZE = 100;
    private float DAMPING = 0.99f;

    private int iteration = 0;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        // Set the clear color
        glClearColor(0.1f, 0.1f, 0.1f, 0.0f);
        textRenderer = new OGLTextRenderer(width, height);
        glEnable(GL_DEPTH_TEST); // zapne z-test (z-buffer) - až po new OGLTextRenderer
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE); // vyplnění přivrácených i odvrácených stran

        calcShader = ShaderUtils.loadProgram("/shaders/calculate");
        calcLocUnit = glGetUniformLocation(calcShader, "unit");
        calcLocDamp = glGetUniformLocation(calcShader, "damping");

        renderShader = ShaderUtils.loadProgram("/shaders/render");
        locViewProj = glGetUniformLocation(renderShader, "viewProj");

        calcTarget = new OGLRenderTarget(GRID_SIZE, GRID_SIZE);

        camera = new Camera()
                .withPosition(new Vec3D(0.5, -0.5, 0.5))
                .withAzimuth(Math.PI / 2)
                .withZenith(-0.5);

        projection = new Mat4PerspRH(
                Math.PI / 3,
                LwjglWindow.HEIGHT / (float) LwjglWindow.WIDTH,
                0.1, // 0.1
                100 // 50
        );

        createNewWaveTexture(); //
        createNewWave(GRID_SIZE / 2, GRID_SIZE / 2);
    }

    @Override
    public void display() {
        iteration++;
        createBuffers();
        calculateWave();
        render();
    }

    private void createBuffers() {
        buffers = GridFactory.generateGrid(GRID_SIZE, GRID_SIZE);
    }

    private void calculateWave() {
        glUseProgram(calcShader);

        calcTarget.bind(); // kresli do tagretu

        glClear(GL_DEPTH_BUFFER_BIT);

        glUniform1f(calcLocUnit, 1.0f / GRID_SIZE);
        glUniform1f(calcLocDamp, DAMPING);

        waveHeightTexture.bind(calcShader, "waveHeightTexture", 0); // textura vlny z předchozího renderu, přístup z shaderu
        buffers.draw(GL_TRIANGLES, calcShader); // kreslíme reálně

        waveHeightTexture = calcTarget.getColorTexture(); // předáme referenci na texturu
    }

    private void render() {
        glUseProgram(renderShader);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, width, height);

        glClearColor(0, 0, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniformMatrix4fv(locViewProj, false, camera.getViewMatrix().mul(projection).floatArray()); // do shaderu transformační matici

        waveHeightTexture.bind(renderShader, "waveHeightTexture", 0); // bind texturu, ta nová

        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

        buffers.draw(GL_TRIANGLES, renderShader);

        // create and draw text
        drawText();
    }

    private void drawText() {
        glEnable(GL_DEPTH_TEST); // zapnout z-test (kvůli textRenderer)
        textRenderer.clear();
        textRenderer.addStr2D(3, 15, "[WASD|LSHIFT|LCTRL|MOUSE] Movement");
        textRenderer.addStr2D(3, 30, "[R] Reset");
        textRenderer.addStr2D(3, 45, "[T] Add random wave");
        textRenderer.addStr2D(3, 60, "[UP|DOWN] Increase/decrease grid size (" + GRID_SIZE + ")");
        textRenderer.addStr2D(3, 75, "[RIGHT|LEFT] Increase/decrease wave damping (" + String.format("%.3f", DAMPING) + ")");

        textRenderer.addStr2D(width - 90, height - 3, " (c) PGRF UHK");
        textRenderer.draw();
    }

    private void createNewWaveTexture() {
        /*
         * Implementation of https://www.gdcvault.com/play/203/Fast-Water-Simulation-for-Games
         *
         * Using texture with 2 components:
         * u = height at the given point
         * v = wave strength at the given point
         *
         * This texture is filled within the 'calculate' shader and is then used within the 'render' shader.
         */
        var initUV = new OGLTexImageFloat(GRID_SIZE, GRID_SIZE, 2);
        waveHeightTexture = new OGLTexture2D(initUV); // vytvoření textury z grafiky
    }

    private void createNewWave(int x, int y) {
        var image = waveHeightTexture.getTexImage(new OGLTexImageFloat.Format(2));
        image.setPixel(x, y, 0.5f); // spíše set point
        waveHeightTexture.setTexImage(image);
    }

    private void createNewRandomWave() {
        var x = new Random().nextInt(GRID_SIZE);
        var y = new Random().nextInt(GRID_SIZE);
        createNewWave(x, y);
    }

    @Override
    public GLFWCursorPosCallback getCursorCallback() {
        return cursorPosCallback;
    }

    @Override
    public GLFWMouseButtonCallback getMouseCallback() {
        return mouseButtonCallback;
    }

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

    private double oldMx, oldMy;
    private boolean mousePressed;

    private final GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (mousePressed) {
                camera = camera.addAzimuth(Math.PI / 2 * (oldMx - x) / LwjglWindow.WIDTH);
                camera = camera.addZenith(Math.PI / 2 * (oldMy - y) / LwjglWindow.HEIGHT);
                oldMx = x;
                oldMy = y;
            }
        }
    };

    private final GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                double[] xPos = new double[1];
                double[] yPos = new double[1];
                glfwGetCursorPos(window, xPos, yPos);
                oldMx = xPos[0];
                oldMy = yPos[0];
                mousePressed = action == GLFW_PRESS;
            }
        }
    };

    private final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_W:
                        camera = camera.forward(CAM_SPEED);
                        break;
                    case GLFW_KEY_A:
                        camera = camera.left(CAM_SPEED);
                        break;
                    case GLFW_KEY_S:
                        camera = camera.backward(CAM_SPEED);
                        break;
                    case GLFW_KEY_D:
                        camera = camera.right(CAM_SPEED);
                        break;
                    case GLFW_KEY_LEFT_SHIFT:
                        camera = camera.up(CAM_SPEED);
                        break;
                    case GLFW_KEY_LEFT_CONTROL:
                        camera = camera.down(CAM_SPEED);
                        break;
                    case GLFW_KEY_R:
                        createNewWaveTexture();
                        break;
                    case GLFW_KEY_T:
                        createNewRandomWave();
                        break;
                    case GLFW_KEY_UP:
                        GRID_SIZE = Math.min(GRID_SIZE + 10, 1000);
                        break;
                    case GLFW_KEY_DOWN:
                        GRID_SIZE = Math.max(GRID_SIZE - 10, 50);
                        break;
                    case GLFW_KEY_RIGHT:
                        DAMPING = Math.min(DAMPING + 0.001f, 0.999f);
                        break;
                    case GLFW_KEY_LEFT:
                        DAMPING = Math.max(DAMPING - 0.001f, 0.9f);
                        break;
                }
            }
        }
    };

}
