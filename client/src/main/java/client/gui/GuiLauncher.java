package client.gui;

import common.auth.Credentials;

import javax.swing.*;

public class GuiLauncher {
    public static void main(String[] args){
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore){}

        SwingUtilities.invokeLater(() -> {
            AuthDialog authDialog = new AuthDialog(null);
            authDialog.setVisible(true);

            Credentials credentials = authDialog.getCredentials();
            if (credentials == null){
                System.exit(0);
            }

            client.NetworkManager networkManager = authDialog.getNetwork();
            MainWindow mainWindow = new MainWindow(credentials, networkManager);
            mainWindow.setVisible(true);
        });
    }
}
