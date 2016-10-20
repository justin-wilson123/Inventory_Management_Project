package views;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import models.User;

/**
 * Customizes row info in JList
 * Don't want to use part's toString() in JLists. Only want to see each person's full name.
 * @author Justin Wilson
 *
 */
public class UserListCellRenderer implements ListCellRenderer<User> {
	/**
	 * Can use default rendered to keep the visual parts I like (e.g., row height, highlight color, etc.)
	 */
	private final DefaultListCellRenderer DEFAULT_RENDERER = new DefaultListCellRenderer();
	
	@Override
	public Component getListCellRendererComponent(JList<? extends User> list, User value, int index,
			boolean isSelected, boolean cellHasFocus) {
		JLabel renderer = (JLabel) DEFAULT_RENDERER.getListCellRendererComponent(list, value.getFullname(), index, isSelected, cellHasFocus);
		return renderer;
	}

}
