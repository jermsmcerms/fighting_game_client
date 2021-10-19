package com.rose.main;

import com.badlogic.gdx.Game;
import com.rose.management.TestGame;
import com.rose.network.Client;
import com.rose.management.AppPreferences;
import com.rose.network.SyncTest;
import com.rose.screens.EndScreen;
import com.rose.screens.LoadingScreen;
import com.rose.screens.MainScreen;
import com.rose.screens.MenuScreen;
import com.rose.screens.PreferencesScreen;

import java.io.IOException;

import com.rose.data.BoxSizes;

public class Rose extends Game {
	private EndScreen endScreen;
	private MainScreen applicationScreen;
	private PreferencesScreen preferencesScreen;
	private MenuScreen menuScreen;
	private AppPreferences appPreferences;
	private boolean trainingMode;

	private TestGame testGame;

	public enum ScreenType {
		MENU, PREFERENCES, APPLICATION, ENDGAME, SYNCTEST;
    }

	public void setTrainingMode(boolean enabled) {
		trainingMode = enabled;
	}

	public void  changeScreen(ScreenType screen) {
		switch(screen) {
			case MENU: {
				if (applicationScreen != null) {
					applicationScreen = null;
				}

				if (menuScreen == null) {
					menuScreen = new MenuScreen(this);
				}
				setScreen(menuScreen);
				break;
			}
			case PREFERENCES: {
				if (preferencesScreen == null) {
					preferencesScreen = new PreferencesScreen(this);
				}
				setScreen(preferencesScreen);
				break;
			}
			case APPLICATION: {
				if (menuScreen != null) {
					menuScreen = null;
				}
				if (applicationScreen == null) {
					if (trainingMode) {
						applicationScreen = new MainScreen(this);
					} else {
						Client client = null;
						try {
							client = new Client();
							while (!client.getAllConnected()) {
								client.doPoll(0);
							}
							client.setPlayerNumber();
							applicationScreen = new MainScreen(this, client);
							client.setCallbacks(applicationScreen);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				setScreen(applicationScreen);
				break;
			}
			case SYNCTEST: {
				if (testGame == null) {
					testGame = new TestGame();
					testGame.runTestLoop();
				}
				break;
			}
			case ENDGAME: {
				if (endScreen == null) {
					endScreen = new EndScreen(this);
				}
				setScreen(endScreen);
				break;
			}
		}
	}

	public AppPreferences getPreferences() {
		return appPreferences;
	}

	@Override
	public void create() {
		BoxSizes bs = new BoxSizes();
		LoadingScreen loadingScreen = new LoadingScreen(this);
		appPreferences = new AppPreferences();
		setScreen(loadingScreen);
	}
}
