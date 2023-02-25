package org.shiftone.jrat.desktop.action.file;

import org.shiftone.jrat.desktop.DesktopPreferences;
import org.shiftone.jrat.desktop.util.Errors;
import org.shiftone.jrat.util.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

/**
 * @author (jeff@shiftone.org) Jeff Drost
 */
public class ClearPreferencesAction extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(ClearPreferencesAction.class);
    private final Component component;

    public ClearPreferencesAction(Component component) {
        super("Clear All Preferences");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        this.component = component;
    }

    public void actionPerformed(ActionEvent event) {
        try {
            Preferences desktop = Preferences.userNodeForPackage(DesktopPreferences.class);
            Preferences jrat = desktop.parent();
            // leaving the shiftone node
            jrat.removeNode();
        } catch (Exception e) {
            Errors.showError(component, e, "Failed to clear preferences");
        }
    }
}
