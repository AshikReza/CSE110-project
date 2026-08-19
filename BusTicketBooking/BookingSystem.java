import javax.swing.*;
import java.awt.*;

/**
 * BookingSystem — entry point.
 * Sets up Nimbus dark theme BEFORE creating any frames, then launches LoginFrame.
 *
 * To run:
 *   javac -encoding UTF-8 *.java
 *   java -Dfile.encoding=UTF-8 BookingSystem
 */
public class BookingSystem {

    public static void main(String[] args) {

        // 1. Install Nimbus L&F (much more customisable than Windows native)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // 2. Global dark-theme overrides — must be set BEFORE any frame is created
        Color bg      = new Color(15,  23,  42);
        Color surface = new Color(30,  41,  59);
        Color accent  = new Color(56, 189, 248);
        Color text    = new Color(226, 232, 240);
        Color muted   = new Color(100, 116, 139);

        UIManager.put("nimbusBase",                 new Color(20,  35,  60));
        UIManager.put("nimbusBlueGrey",             surface);
        UIManager.put("control",                    surface);
        UIManager.put("nimbusLightBackground",      bg);
        UIManager.put("text",                       text);
        UIManager.put("nimbusSelectedText",         Color.WHITE);
        UIManager.put("nimbusSelectionBackground",  new Color(56, 189, 248, 110));
        UIManager.put("nimbusFocus",                accent);
        UIManager.put("info",                       surface);
        UIManager.put("infoText",                   text);

        // Table
        UIManager.put("Table.background",           bg);
        UIManager.put("Table.foreground",           text);
        UIManager.put("Table.gridColor",            surface);
        UIManager.put("TableHeader.background",     surface);
        UIManager.put("TableHeader.foreground",     accent);

        // Tab pane
        UIManager.put("TabbedPane.background",         surface);
        UIManager.put("TabbedPane.tabAreaBackground",  surface);
        UIManager.put("TabbedPane.contentAreaColor",   bg);
        UIManager.put("TabbedPane.foreground",         text);
        UIManager.put("TabbedPane.selectedForeground", accent);
        UIManager.put("TabbedPane.selected",           bg);
        UIManager.put("TabbedPane.unselectedBackground", surface);
        UIManager.put("TabbedPane.contentOpaque",      Boolean.FALSE);
        UIManager.put("TabbedPane.focus",              accent);

        // Dialogs / option panes
        UIManager.put("Panel.background",              bg);
        UIManager.put("OptionPane.background",         surface);
        UIManager.put("OptionPane.messageForeground",  text);
        UIManager.put("Button.background",             surface);
        UIManager.put("Button.foreground",             text);
        UIManager.put("TextField.background",          bg);
        UIManager.put("TextField.foreground",          text);
        UIManager.put("TextField.caretForeground",     accent);
        UIManager.put("PasswordField.background",      bg);
        UIManager.put("PasswordField.foreground",      text);
        UIManager.put("ComboBox.background",           bg);
        UIManager.put("ComboBox.foreground",           text);
        UIManager.put("ScrollPane.background",         bg);
        UIManager.put("Viewport.background",           bg);
        UIManager.put("Label.foreground",              text);
        UIManager.put("CheckBox.background",           surface);
        UIManager.put("CheckBox.foreground",           muted);

        // 3. Launch the login window on the Event Dispatch Thread
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
