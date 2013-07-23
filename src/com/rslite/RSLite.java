package com.rslite;

import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.rslite.Settings.Setting;
import com.rslite.loader.JavaAppletLoader;
import com.rslite.loader.ParamaterParser;
import com.rslite.utils.ScreenshotTool;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;
import javax.sound.sampled.*;

/**
 * Class RSLite, the client's main class.
 * 
 * @author Nicole <nicole@rune-server.org> This file is protected by The BSD
 *         License, You should have recieved a copy named "BSD License.txt"
 */

public class RSLite {

	private static ExecutorService worker = Executors.newSingleThreadExecutor();

	/**
	 * Grabs screen size (for fullscreen) at startup
	 */
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

	/**
	 * Boolean that stores whether the fullscreen mode is toggled
	 */
	private static boolean isFullScreen = false;

	/**
	 * Fullscreen cached window
	 */
	public static Window window;

	/**
	 * The main applet panel, the rs applet is added to this, so it is not
	 * reloaded on fullscreen toggle.
	 */
	public static JPanel appletPanel = new JPanel();

	/**
	 * Simple, the frame title.
	 */
	private static String frameTitle = "RS Lite";

	/**
	 * The main content frame, for holding the applet itself non fullscreen
	 */
	public static Frame frame;

	/**
	 * RS Properties storage.
	 */
	public HashMap<String, String> props;

	/**
	 * The hide menu option
	 */
	private MenuItem hide;
	
	private CheckboxMenuItem mute;
	
	private Image logo;

	/**
	 * Tray icon
	 */
	private static TrayIcon icon;

	/**
	 * RS Applet loader
	 */
	private static JavaAppletLoader loader;

	/**
	 * Main entry point
	 * 
	 * @param args
	 *            The commandline arguments
	 */
	public static void main(String args[]) {
		new RSLite();
	}

	/**
	 * The constructor..
	 */
	private RSLite() {
		/**
		 * Set it to use the system look and feel, instead of java's default,
		 * All dialogues etc will use this
		 */
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		/**
		 * Load from the settings file
		 */
		Settings.loadSettings();
		/**
		 * Parse the runescape params and set the field containing them
		 */
		try {
			ParamaterParser parser = new ParamaterParser();
			props = parser.parseParamaters();
		} catch (IOException e) {
			e.printStackTrace();
		}
		init();
	}

	/**
	 * Initiates the Applet
	 */
	private void init() {
		try {
			logo = Toolkit.getDefaultToolkit().getImage(getClass().getResource("resources/icon.png"));
			icon = new TrayIcon(logo);
			icon.setImageAutoSize(true);
			icon.setPopupMenu(createMenu());
			icon.setToolTip("RSLite Runescape loader");
			SystemTray.getSystemTray().add(icon);
			double currver = this.getCurrentVersion();
			if (currver > Constants.VERSION) {
				icon.displayMessage("Update available",
						"An RSLite update is available! Current version: "
								+ currver + " Download it from "
								+ Constants.WEBSITE_URL, MessageType.INFO);
				frameTitle += " (Update available)";
			}
			loader = new JavaAppletLoader(new URL(props.get("url")), "Rs2Applet", props);
			Frame[] frames = Frame.getFrames(); //Retrieve RuneScape popup frame
			Boolean frameFound = false;
			for(Frame aFrame : frames)
			{
				if(aFrame.getTitle().equals("RuneScape"))
				{
					frame = aFrame;
					frameFound = true;
					break;
				}
			}
			
			printFrames();
			
			if(!frameFound)
			{
				frame = new Frame();
				frame.setLayout(new BorderLayout());
				frame.setResizable(true);
				appletPanel.setLayout(new BorderLayout());
				appletPanel.add(loader.getApplet());
				appletPanel.setPreferredSize(new Dimension(765, 503));
				frame.add(appletPanel, BorderLayout.CENTER);
			}
			frame.setTitle(frameTitle);
			frame.setIconImage(logo);
			frame.addWindowListener(
				new WindowAdapter() {
				@Override
					public void windowClosed(WindowEvent e) {
						exit();
					}
				}
			);
			
			frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
			frame.pack();
			frame.setVisible(true);
			
			Component[] components = frame.getComponents();
			System.out.println(components[0]);
			components[0].setLocation(0, -100);
			
			KeyboardFocusManager.getCurrentKeyboardFocusManager()
					.addKeyEventDispatcher(new KeyListener());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Hide/show the frame
	 */
	public void hide() {
		frame.setVisible(!frame.isVisible());
		hide.setLabel(frame.isVisible() ? "Hide" : "Show");
	}
	
	private String multiplyString(String str, int count) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < count; ++i) {
			sb.append(str);
		}
		return sb.toString();
	}
	
	public void printFramesRecursively(Component component, int depth) {
		System.out.printf("%sComponent: %s %s %s\n", multiplyString("  ", depth), component.getClass(), component.getSize(), component.getLocation());
		if(component instanceof Container) {
			for(Component subComponent : ((Container)component).getComponents()) {
				printFramesRecursively(subComponent, depth + 1);
			}
		}
	}
	
	public void printFrames() {
		System.out.println("======");
		Frame[] frames = Frame.getFrames();
		for (Frame aFrame : frames)
		{
			System.out.print("Frame: ");
			System.out.print(aFrame.getClass() + " ");
			System.out.println(aFrame.getSize().toString());
			printFramesRecursively(aFrame, 1);
		}
	}
	
	/**
	 * Mute/unmute RuneScape
	 */
	public void mute() {
		final Component[] components = frame.getComponents();
		final Rectangle bounds = frame.getBounds();
		
		for(ComponentListener listener : components[0].getComponentListeners()) {
			components[0].removeComponentListener(listener);
		}
		
		components[0].addComponentListener(new ComponentListener(){
			//@Override
			public void componentResized(ComponentEvent ce) {
				if(!components[0].getSize().equals(new Dimension(bounds.width, bounds.height + 45))) {
					components[0].setSize(bounds.width, bounds.height + 45);
					System.out.println("Didn't match, resizing");
				} else {
					System.out.println("Already correct size");
				}
			}

			public void componentMoved(ComponentEvent ce) {
				//
			}

			public void componentShown(ComponentEvent ce) {
				//
			}

			public void componentHidden(ComponentEvent ce) {
				//
			}
		});
		
		components[0].setSize(bounds.width, bounds.height + 45);
		
		Container offsetContainer = new Container();
		offsetContainer.add(components[0]);
		
		frame.add(offsetContainer);
		
		offsetContainer.setBounds(0, -45, bounds.width, bounds.height + 45);
		
		printFrames();
		Mixer.Info[] infos = AudioSystem.getMixerInfo();
		for (Mixer.Info info: infos) {
			Mixer mixer = AudioSystem.getMixer(info);
			for (Line line : mixer.getSourceLines())
			{
				/*for (Control control : line.getControls())
				{
					System.out.println(control.toString());
				}*/
				BooleanControl bc = (BooleanControl) line.getControl(BooleanControl.Type.MUTE);
				if (bc != null) {
					bc.setValue(mute.getState()); // true to mute the line, false to unmute
				}
			}
		}
	}
	
	public void exit()
	{
		/*PopupMenu exitMenu = new PopupMenu();
		MenuItem exiting = new MenuItem("Exiting...");
		exiting.setEnabled(false);
		exitMenu.add(exiting);
		icon.setPopupMenu(exitMenu);*/
		
		RSLite.getLoader().getApplet().destroy();
		System.exit(0);
	}

	/**
	 * Setup the menu for the tray icon
	 * 
	 * @return The newly created menu
	 */
	public PopupMenu createMenu() {
		PopupMenu menu = new PopupMenu();
		hide = new MenuItem("Hide");
		hide.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hide();
			}
		});
		menu.add(hide);
		
		mute = new CheckboxMenuItem("Mute");
		mute.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				mute();
			}
		});
		menu.add(mute);
		/*MenuItem screenshot = new MenuItem("Screenshot");
		screenshot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				screenshot();
			}
		});
		menu.add(screenshot);*/
		MenuItem item = new MenuItem("Exit");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		menu.add(item);
				
		return menu;
	}

	/**
	 * Change the upload method
	 */
	public static void changeUploadSettings() {
		switch (Settings.getIntSetting("uploadmethod")) {
		case 0:
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File(Settings
					.getSetting("screenshot_loc")));
			chooser.setDialogTitle("Select a screenshot location: ");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				Settings.remove(Setting.FILE_LOCATION);
				Settings.put(Setting.FILE_LOCATION, chooser.getSelectedFile()
						.getPath() + "\\");
				JOptionPane.showMessageDialog(frame,
						"Screenshot location changed to:\n"
								+ chooser.getSelectedFile().getPath(),
						"Screenshot location changed",
						JOptionPane.INFORMATION_MESSAGE);
				Settings.writeSettings(false);
			} else {
				JOptionPane.showMessageDialog(frame,
						"No folder selection was made!", "ERROR!",
						JOptionPane.ERROR_MESSAGE);
			}
			break;
		case 1:
			String apikey = JOptionPane
					.showInputDialog("Input your Imgur api key:");
			if (apikey == null || apikey.equals("")) {
				JOptionPane.showMessageDialog(frame,
						"Invalid api key entered!", "Invalid api key",
						JOptionPane.ERROR_MESSAGE);
			} else {
				Settings.put(Setting.IMGUR_KEY, apikey);
				Settings.writeSettings(false);
			}
			break;
		}
	}

	/**
	 * Take a screenshot
	 * 
	 * @param hide
	 */
	public static void screenshot() {
		final UploadMethod uploadmethod = UploadMethod.values()[Settings
				.getIntSetting("uploadmethod")];
		if (!Settings.contains(Setting.IMGUR_KEY)
				&& uploadmethod == UploadMethod.IMGUR) {
			JOptionPane
					.showMessageDialog(
							frame,
							"You are using a method that uses an API Key, please press f11 to configure it.",
							"Error", JOptionPane.ERROR_MESSAGE);
		} else {
			worker.execute(new Runnable() {
				public void run() {
					ScreenshotTool.createScreenshot(uploadmethod);
				}
			});
		}
	}

	/**
	 * @return True, if rslite is running in fullscreen
	 */
	public static boolean isFullscreen() {
		return isFullScreen;
	}

	/**
	 * Get the RS Loader
	 * 
	 * @return The loader instance
	 */
	public static JavaAppletLoader getLoader() {
		return loader;
	}

	/**
	 * Get the TrayIcon instance (The system tray icon)
	 * 
	 * @return The instance
	 */
	public static TrayIcon getIcon() {
		return icon;
	}

	/**
	 * Toggle fullscreen mode
	 */
	public static void toggleFullscreen() {
		/*if (!isFullScreen) {
			frame.remove(appletPanel);
			window.add(appletPanel);
			window.setVisible(true);
			frame.setTitle("RS Lite - Fullscreen");
			isFullScreen = true;
		} else {
			window.remove(appletPanel);
			frame.add(appletPanel, BorderLayout.CENTER);
			window.setVisible(false);
			frame.pack();
			frame.setTitle(frameTitle);
			isFullScreen = false;
		}*/
	}

	/**
	 * Read the current version from the website
	 * 
	 * @return The current RSLite version
	 */
	public double getCurrentVersion() {
		return 0;
		/*try {
			BufferedReader r = new BufferedReader(new InputStreamReader(
					new URL("http://rslite.tk/ver.txt").openStream()));
			return Double.parseDouble(r.readLine());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Constants.VERSION;*/
	}

	/**
	 * The enum containing the current upload types
	 * 
	 * @author Nikki
	 * 
	 */
	public enum UploadMethod {
		FILE, IMGUR
	}
}
