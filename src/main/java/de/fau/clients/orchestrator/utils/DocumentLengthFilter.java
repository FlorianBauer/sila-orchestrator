package de.fau.clients.orchestrator.utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * A DocumentFilter for widgets which limits the amount of input characters.
 *
 * Example:
 * <code>((AbstractDocument) strField.getDocument()).setDocumentFilter(new DocumentLengthFilter(len));</code>
 */
public class DocumentLengthFilter extends DocumentFilter {

    /**
     * The character input limit (inclusive).
     */
    private final int charLimit;

    /**
     * Creates a length constraint <code>DocumentFilter</code>.
     *
     * @param maxLength The maximal character input limit (inclusive).
     */
    public DocumentLengthFilter(int maxLength) {
        this.charLimit = maxLength;
    }

    /**
     * Gets the character limit.
     *
     * @return The character input limit (inclusive).
     */
    public int getCharLimit() {
        return charLimit;
    }

    @Override
    public void insertString(DocumentFilter.FilterBypass fb,
            int offset,
            String str,
            AttributeSet attrs) throws BadLocationException {
        final int len = fb.getDocument().getLength() + str.length();
        if (len < charLimit) {
            super.insertString(fb, offset, str, attrs);
        }
    }

    @Override
    public void replace(DocumentFilter.FilterBypass fb,
            int offset,
            int length,
            String str,
            AttributeSet attrs) throws BadLocationException {
        final int len = fb.getDocument().getLength() + length;
        if (len < charLimit) {
            super.replace(fb, offset, length, str, attrs);
        }
    }
}
