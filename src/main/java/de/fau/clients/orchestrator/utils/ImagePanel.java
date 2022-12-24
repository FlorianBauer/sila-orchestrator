package de.fau.clients.orchestrator.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * A <code>JPanel</code> which views the contained image. The size of this component is determined
 * by the provided <code>BufferedImage</code>.
 *
 * @see BufferedImage
 */
@SuppressWarnings("serial")
public class ImagePanel extends JPanel {

    private final BufferedImage img;

    /**
     * Constructor.
     *
     * @param img The image to show.
     */
    public ImagePanel(final BufferedImage img) {
        this.img = img;
        final Dimension dim = new Dimension(img.getWidth() + 1, img.getHeight() + 1);
        this.setMaximumSize(dim);
        this.setPreferredSize(dim);
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(img, null, 0, 0);
    }
}
