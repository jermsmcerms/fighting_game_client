package com.fighting_game;

import com.badlogic.gdx.Game;
import com.fighting_game.screens.ApplicationScreen;
import com.fighting_game.screens.LoadingScreen;
import com.fighting_game.screens.MainMenuScreen;

public class Main extends Game {
	private ApplicationScreen applicationScreen;
	private MainMenuScreen menuScreen;


	public enum ScreenType {
		MENU, PREFERENCES, APPLICATION, ENDGAME, SYNCTEST;
	}

	public void  changeScreen(ScreenType screen) {
		switch(screen) {
			case MENU: {
				if (applicationScreen != null) {
					applicationScreen = null;
				}

				if (menuScreen == null) {
					menuScreen = new MainMenuScreen(this);
				}
				setScreen(menuScreen);
				break;
			}
			case APPLICATION: {
				if (menuScreen != null) {
					menuScreen = null;
				}
				if (applicationScreen == null) {
					applicationScreen = new ApplicationScreen(this);
				}
				setScreen(applicationScreen);
				break;
			}
		}
	}

	@Override
	public void create() {
		LoadingScreen loadingScreen = new LoadingScreen(this);
		setScreen(loadingScreen);
	}
}
