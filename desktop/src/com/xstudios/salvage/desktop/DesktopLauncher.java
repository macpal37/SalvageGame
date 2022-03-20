package com.xstudios.salvage.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.xstudios.salvage.game.GDXRoot;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		// Resolution 16:9
		config.width = 1280;
		config.height = 720;

		new LwjglApplication(new GDXRoot(), config);
	}
}
