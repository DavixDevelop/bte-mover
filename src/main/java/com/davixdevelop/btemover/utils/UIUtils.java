package com.davixdevelop.btemover.utils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

/**
 * Represents an UI utility class with methods for manipulating the UI
 *
 * @author DavixDevelop
 */
public class UIUtils {
    /**
     * Copied from:
     * https://stackoverflow.com/questions/3953208/value-change-listener-to-jtextfield
     * @author Boann (https://stackoverflow.com/users/964243/boann)
     *
     * Installs a listener to receive notification when the text of any
     * {@code JTextComponent} is changed. Internally, it installs a
     * {@link DocumentListener} on the text component's {@link Document},
     * and a {@link PropertyChangeListener} on the text component to detect
     * if the {@code Document} itself is replaced.
     *
     * @param textComponent any text component, such as a {@link JTextField}
     *        or {@link JTextArea}
     * @param change a listener to receive {@link ChangeEvent}s
     *        when the text is changed; the source object for the events
     *        will be the text component
     * @throws NullPointerException if either parameter is null
    */
    public static void addTextChangeListener(JTextComponent textComponent, ChangeListener change){
        Objects.requireNonNull(textComponent);
        Objects.requireNonNull(change);
        DocumentListener dl = new DocumentListener() {
            private int lastChange = 0;
            private int lastNotifiedChange = 0;
            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lastChange++;
                SwingUtilities.invokeLater(() -> {
                    if(lastNotifiedChange != lastChange){
                        lastNotifiedChange = lastChange;
                        change.stateChanged(new ChangeEvent(textComponent));
                    }
                });
            }
         };

        textComponent.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
            Document oldDocument = (Document)e.getOldValue();
            Document newDocument = (Document)e.getNewValue();

            if(oldDocument != null) oldDocument.removeDocumentListener(dl);
            if(newDocument != null) newDocument.addDocumentListener(dl);

            dl.changedUpdate(null);
        });
        Document document = textComponent.getDocument();
        if (document != null) document.addDocumentListener(dl);

     }

    /**
     * Loop's through container components and set's the background color to it, if the component is a JPanel
     * @param container The container of components
     * @param backgroundColor The desired background color
     */
     public static void changeComponentsBackground(Container container, Color backgroundColor){
         Component[] components = container.getComponents();
         for (int i = 0; i < components.length; i++) {
             if(Objects.equals(components[i].getClass().getName(), "javax.swing.JPanel")){
                 components[i].setBackground(backgroundColor);
                 changeComponentsBackground((Container) components[i], backgroundColor);
             }

         }
     }
}
