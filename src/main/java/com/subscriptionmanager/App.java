package com.subscriptionmanager;

import com.subscriptionmanager.model.Subscription;
import com.subscriptionmanager.model.SubscriptionManager;
import com.subscriptionmanager.storage.SubscriptionStore;
import com.subscriptionmanager.ui.MainWindow;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Launches the Subscription Manager desktop app. */
public final class App {

    private App() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::start);
    }

    private static void start() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to the default cross-platform look and feel.
        }

        SubscriptionStore store = SubscriptionStore.defaultStore();
        List<Subscription> subscriptions = loadOrRecover(store);

        new MainWindow(new SubscriptionManager(subscriptions), store).setVisible(true);
    }

    /**
     * Loads saved subscriptions. If the file can't be read, it is moved aside (never
     * overwritten) and the app starts with an empty list.
     */
    private static List<Subscription> loadOrRecover(SubscriptionStore store) {
        try {
            return store.load();
        } catch (IOException e) {
            Path file = store.getFile();
            Path backup = file.resolveSibling(file.getFileName() + ".broken-" + System.currentTimeMillis());
            String outcome;
            try {
                Files.move(file, backup);
                outcome = "The file was moved to:\n" + backup + "\n\nStarting with an empty list.";
            } catch (IOException moveError) {
                JOptionPane.showMessageDialog(null,
                        "Could not read your saved subscriptions from:\n" + file + "\n\n" + e.getMessage()
                                + "\n\nThe app will close so that the file is not overwritten.",
                        "Subscription Manager", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return List.of();
            }
            JOptionPane.showMessageDialog(null,
                    "Could not read your saved subscriptions:\n" + e.getMessage() + "\n\n" + outcome,
                    "Subscription Manager", JOptionPane.WARNING_MESSAGE);
            return new ArrayList<>();
        }
    }
}
