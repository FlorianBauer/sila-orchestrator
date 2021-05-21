package de.fau.clients.orchestrator.utils;

import javax.swing.JComponent;
import javax.swing.JToolTip;

/**
 * Custom ToolTip class for a better distinction between application tool-tips and SiLA element
 * descriptions.This class also contains various static utility functions for tool-tip formatting
 * and rendering.
 */
@SuppressWarnings("serial")
public class SilaDescriptionToolTip extends JToolTip {

    /**
     * HTML styling which defines the rendering of the tool-tip shown by hovering over an component.
     */
    public final static String HTML_FONT_SIZE = " style=\"font-size:15pt\"";
    public final static String HTML_WIDTH = " width=\"600\"";
    public final static int MAX_CHARS_PER_LINE = 100;
    public final static String HTML_FORMAT_SUFFIX = "</p></html>";

    public SilaDescriptionToolTip(final JComponent comp) {
        setFont(getFont().deriveFont(15.0f));
        setComponent(comp);
    }

    /**
     * Utility function to format a description-string by adding styling information for a
     * consistent tool-tip rendering.
     *
     * @param desc The description text to be formatted.
     * @return The formatted string or an empty string.
     */
    public static String formatToolTipString(final String desc) {
        if (desc == null) {
            return "";
        } else if (desc.length() < MAX_CHARS_PER_LINE) {
            return "<html><p" + HTML_FONT_SIZE + ">" + desc + HTML_FORMAT_SUFFIX;
        } else {
            return "<html><p" + HTML_FONT_SIZE + HTML_WIDTH + ">" + desc + HTML_FORMAT_SUFFIX;
        }
    }

    /**
     * Utility function to format a description-string with a bold headline by adding styling
     * information for a consistent tool-tip rendering.
     *
     * @param headline The headline of the tool-tip (must not be null).
     * @param text The description text for the tool-tip.
     * @return The tool-tip with applied HTML formatting or null if no text was given.
     */
    public static String formatHeadlineToolTip(final String headline, final String text) {
        if (text != null) {
            final String widthStr = (text.length() > MAX_CHARS_PER_LINE) ? HTML_WIDTH : "";
            return "<html><p" + widthStr + ">"
                    + "<b>" + headline + "</b><br>"
                    + text + HTML_FORMAT_SUFFIX;
        }
        return null;
    }
}
