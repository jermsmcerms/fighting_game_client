package com.rose.main;

import com.badlogic.gdx.Game;
import com.rose.network.ConnectState;
import com.rose.tests.TestGame;
import com.rose.network.Client;
import com.rose.management.AppPreferences;
import com.rose.screens.EndScreen;
import com.rose.screens.LoadingScreen;
import com.rose.screens.ApplicationScreen;
import com.rose.screens.MenuScreen;
import com.rose.screens.PreferencesScreen;

import java.io.IOException;

public class Rose extends Game {
	private EndScreen endScreen;
	private ApplicationScreen applicationScreen;
	private PreferencesScreen preferencesScreen;
	private MenuScreen menuScreen;
	private AppPreferences appPreferences;
	private boolean trainingMode;

	private TestGame testGame;

	public enum ScreenType {
		MENU, PREFERENCES, APPLICATION, ENDGAME, TEST
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
						applicationScreen = new ApplicationScreen(this);
					} else {
						Client client;
						client = new Client();
						while (!(client.getCurrentStatus() == ConnectState.Running)) {
							client.doPoll(0);
						}
						applicationScreen = new ApplicationScreen(this, client);
					}
				}
				setScreen(applicationScreen);
				break;
			}
			case TEST: {
				if (testGame == null) {
					testGame = new TestGame(this);
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
		LoadingScreen loadingScreen = new LoadingScreen(this);
		appPreferences = new AppPreferences();
		setScreen(loadingScreen);
	}
}
