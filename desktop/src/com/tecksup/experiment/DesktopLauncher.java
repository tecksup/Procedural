package com.tecksup.experiment;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.tecksup.experiment.data.dcputils.DcpTexturePackerManager;

public class DesktopLauncher {
	public static void main (String[] arg) {
		//The Image Packing that happens on startup
		DcpTexturePackerManager texturePackerManager = new DcpTexturePackerManager();
		texturePackerManager.checkWhetherToPackImages();

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Project Experiment");
		new Lwjgl3Application(new Experiment(), config);
	}
}
