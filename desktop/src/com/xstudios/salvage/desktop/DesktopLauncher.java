package com.xstudios.salvage.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.xstudios.backend.GDXApp;
import com.xstudios.backend.GDXAppSettings;
import com.xstudios.salvage.game.GDXRoot;

public class DesktopLauncher {
	public static void main (String[] arg) {
		GDXAppSettings config = new GDXAppSettings();
		config.useHDPI = false;
		new GDXApp( new GDXRoot(), config );
	}
}
