package com.xstudios.salvage.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.xstudios.salvage.game.GDXRoot;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		// Resolution 16:9
		config.setWindowedMode(1280,720);
		config.disableAudio(true);
		new Lwjgl3Application(new GDXRoot(), config);
	}
}
