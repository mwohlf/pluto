package net.wohlfart.pluto.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import net.wohlfart.pluto.Pluto;

public class DesktopLauncher {

    public static void main(String[] arg) {
        new DesktopLauncher().runLwjgl3();
    }

    private void runLwjgl3() {
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setDecorated(true);
        config.setTitle("Pluto");
        config.setResizable(true);
        config.setWindowedMode(1024, 600);
        //config.enableGLDebugOutput(true, System.out);
        config.useVsync(false);
        // this starts the loop
        new Lwjgl3Application(new Pluto(), config);

    }
    /*
    private void runLwjgl2() {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.vSyncEnabled = false; // Setting to false disables vertical sync
        config.foregroundFPS = 0; // Setting to 0 disables foreground fps throttling
        config.backgroundFPS = 0; // Setting to 0 disables background fps throttling
        config.height = 600;
        config.width = 1024;
        //config.overrideDensity = 128;
        new LwjglApplication(new Pluto(), config);
    }
    */
}
