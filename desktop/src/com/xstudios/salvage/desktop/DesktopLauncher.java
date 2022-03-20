package com.xstudios.salvage.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.xstudios.backend.GDXApp;
import com.xstudios.backend.GDXAppSettings;
import com.xstudios.salvage.game.GDXRoot;

public class DesktopLauncher {
	public static void main (String[] arg) {
<<<<<<< HEAD
		GDXAppSettings config = new GDXAppSettings();
		config.useHDPI = false;
		new GDXApp( new GDXRoot(), config );
=======
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;

		new LwjglApplication(new GDXRoot(), config);
>>>>>>> ce1ef31a1771b0f028ee781f41846a30b0980a12
	}
}
