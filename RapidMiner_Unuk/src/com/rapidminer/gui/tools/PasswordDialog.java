/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.gui.tools;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.PasswordAuthentication;
import java.util.logging.Level;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.rapidminer.RapidMiner;
import com.rapidminer.gui.security.UserCredential;
import com.rapidminer.gui.security.Wallet;
import com.rapidminer.gui.tools.dialogs.ButtonDialog;
import com.rapidminer.tools.LogService;

/** Dialog asking for username and passwords. Answers may be cached (if chosen by user).
 * 
 * @author Simon Fischer
 *
 */
public class PasswordDialog extends ButtonDialog {

    private static final long serialVersionUID = 1L;

    private JTextField usernameField = new JTextField(20);
    private JPasswordField passwordField = new JPasswordField(20);
    private JCheckBox rememberBox = new JCheckBox(new ResourceActionAdapter("authentication.remember"));

    private PasswordDialog(UserCredential preset) {
        this("authentication", preset);
    }

    private PasswordDialog(String i18nKey, UserCredential preset) {
        super(i18nKey, (preset != null) ? preset.getURL() : null);        
        setModal(true);
        if ((preset != null) && (preset.getPassword() != null)) {
            usernameField.setText(preset.getUsername());
        }
        if ((preset!= null) && (preset.getPassword() != null))  {
        	passwordField.setText(new String(preset.getPassword()));        	
            rememberBox.setSelected(true);
        }
        String url = (preset != null) ? preset.getURL() : null;

        JPanel main = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.insets = new Insets(4,4,4,4);

        JLabel label = new ResourceLabel("authentication.username", url);
        label.setLabelFor(usernameField);
        c.gridwidth = GridBagConstraints.RELATIVE;
        main.add(label, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        main.add(usernameField, c);

        label = new ResourceLabel("authentication.password", url);
        label.setLabelFor(passwordField);
        c.gridwidth = GridBagConstraints.RELATIVE;
        main.add(label, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        main.add(passwordField, c);

        main.add(rememberBox, c);
        
        layoutDefault(main, makeCancelButton(), makeOkButton());
        
        
    }
    public PasswordAuthentication makeAuthentication() {
        return new PasswordAuthentication(usernameField.getText(), passwordField.getPassword());
    }

	public static PasswordAuthentication getPasswordAuthentication(String forUrl, boolean forceRefresh) {
        return getPasswordAuthentication(forUrl, forceRefresh, false);
    }

    public static PasswordAuthentication getPasswordAuthentication(String forUrl, boolean forceRefresh, boolean hideDialogIfPasswordKnown) {
		if (RapidMiner.getExecutionMode().isHeadless()) {
			//LogService.getRoot().warning("Cannot query password in batch mode. Password was requested for "+forUrl+".");
			LogService.getRoot().log(Level.WARNING, "com.rapidminer.gui.tools.PassworDialog.no_query_password_in_batch_mode", forUrl);
			return null;
		}
        UserCredential authentication = Wallet.getInstance().getEntry(forUrl);
        // return immediately if known and desired
        if (hideDialogIfPasswordKnown && !forceRefresh && (authentication != null) && (authentication.getPassword() != null)) {
            //LogService.getRoot().config("Reusing cached password for "+forUrl+".");
            LogService.getRoot().log(Level.CONFIG, "com.rapidminer.gui.tools.PassworDialog.reusing_cached_password", forUrl);
            return authentication.makePasswordAuthentication();
        }

        // clear cache if refresh forced
        if (forceRefresh && authentication != null) {
            //authentication = new PasswordAuthentication(authentication.getUserName(), null);
        	authentication.setPassword(null);
            Wallet.getInstance().registerCredentials(authentication);
        }
        if (authentication == null) {
        	authentication = new UserCredential(forUrl, null, null);
        }
        final PasswordDialog pd = new PasswordDialog(authentication);
        pd.setVisible(true);
        if (pd.wasConfirmed()) {
            PasswordAuthentication result = pd.makeAuthentication();
            if (pd.rememberBox.isSelected()) {
            	UserCredential userCredential = new UserCredential(forUrl, result.getUserName(), result.getPassword());
            	Wallet.getInstance().registerCredentials(userCredential);
            	Wallet.getInstance().saveCache();
            } else {
            	Wallet.getInstance().remove(forUrl);
            	Wallet.getInstance().saveCache();
            }
            
            return result;
        } else {
            return null;
        }
    }
}
