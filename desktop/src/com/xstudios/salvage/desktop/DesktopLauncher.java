package com.xstudios.salvage.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.xstudios.salvage.game.GDXRoot;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		// Resolution 16:9
		config.width = 960;
		config.height = 540;

		new LwjglApplication(new GDXRoot(), config);
	}
}
