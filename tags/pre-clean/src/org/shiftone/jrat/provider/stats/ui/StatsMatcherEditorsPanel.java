package org.shiftone.jrat.provider.stats.ui;


import org.shiftone.jrat.util.log.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;


/**
 * @author Jeff Drost
 */
public class StatsMatcherEditorsPanel extends JPanel {

    private static final Logger LOG = Logger.getLogger(StatsMatcherEditorsPanel.class);

    public StatsMatcherEditorsPanel() {

        Border border = BorderFactory.createTitledBorder("Filter Statistics");

        setBorder(border);
        setLayout(new GridLayout(0, 1, 0, 5));
    }
}
