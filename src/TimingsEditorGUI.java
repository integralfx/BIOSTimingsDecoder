import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TimingsEditorGUI extends JFrame
{
	public static void main(String[] args)
	{
		new TimingsEditorGUI();
	}

	public TimingsEditorGUI()
	{
		super("Timings Editor");

		add_menu_bar();

		BoxLayout layout = new BoxLayout(main_panel, BoxLayout.Y_AXIS);
		main_panel.setLayout(layout);

		main_panel.add(lbl_file);

		add_indices_panel();

		add_timings_panel();

		pack();
		setResizable(false);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		// open in center of screen
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
		Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
		int x = (int)(rect.getMaxX() - getWidth()) / 2;
		int y = (int)(rect.getMaxY() - getHeight()) / 2;
		setLocation(x, y);
	}

	private void add_menu_bar()
	{
		JMenuBar menu_bar = new JMenuBar();

		ActionListener listener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser fc = new JFileChooser();

				// start in current directory
				Path path = Paths.get("");
				fc.setCurrentDirectory(path.toAbsolutePath().toFile());

				if(e.getSource() == menu_item_open)
				{
					if(fc.showOpenDialog(main_panel) == JFileChooser.APPROVE_OPTION)
					{
						File file = fc.getSelectedFile();

						lbl_file.setText(file.getName());

						timings_editor = new TimingsEditor(file.getAbsolutePath());
						timings = timings_editor.get_timings();

						update_indices_cbox();
						update_timings_text((byte)1);
					}
				}
				else if(e.getSource() == menu_item_saveas)
				{
					if(timings_editor == null)
					{
						show_error_dialog("Please open a BIOS first");
						return;
					}

					if(fc.showSaveDialog(main_panel) == JFileChooser.APPROVE_OPTION)
					{
						String bios_file_path = fc.getSelectedFile().getAbsolutePath();

						if(timings_editor.save_bios(bios_file_path))
							show_success_dialog("Successfully saved to " + bios_file_path);
						else show_error_dialog("Failed to save BIOS");
					}
				}
			}

			private void show_error_dialog(String msg)
			{
				JOptionPane.showMessageDialog(
					main_panel,
					msg,
					"Error",
					JOptionPane.ERROR_MESSAGE
				);
			}
			
			private void show_success_dialog(String msg)
			{
				JOptionPane.showMessageDialog(
					main_panel,
					msg,
					"Success",
					JOptionPane.INFORMATION_MESSAGE
				);
			}
		};

		JMenu menu_file = new JMenu("File");
		menu_bar.add(menu_file);

		menu_item_open = new JMenuItem("Open");
		menu_item_open.addActionListener(listener);
		menu_file.add(menu_item_open);

		menu_item_saveas = new JMenuItem("Save As");
		menu_item_saveas.addActionListener(listener);
		menu_file.add(menu_item_saveas);

		setJMenuBar(menu_bar);
	}

	private void add_indices_panel()
	{
		JPanel panel = new JPanel(new FlowLayout());

		JLabel lbl_indices = new JLabel("RAM IC Index: ");
		panel.add(lbl_indices);

		cbox_indices = new JComboBox();
		panel.add(cbox_indices);

		main_panel.add(panel);
	}

	private void update_indices_cbox()
	{
		if(timings == null || timings.isEmpty())
			return;

		// get unique indices
		ArrayList<String> indices = new ArrayList<>();
		for(TimingsEditor.ATOM_VRAM_TIMING_ENTRY e : timings)
		{
			String index = String.valueOf(Byte.toUnsignedInt(e.ucIndex));

			if(!indices.contains(index)) 
				indices.add(index);
		}

		DefaultComboBoxModel model = new DefaultComboBoxModel(indices.toArray(new String[indices.size()]));
		cbox_indices.setModel(model);
		
		cbox_indices.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				byte selected = (byte)Integer.parseInt(cbox_indices.getSelectedItem().toString());
				update_timings_text(selected);
			}
		});
	}

	private void add_timings_panel()
	{
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);

		for(int f : frequencies)
		{
			JPanel panel_row = new JPanel(new FlowLayout());

			JLabel lbl_frequency = new JLabel(String.format("%d MHz: ", f));
			lbl_frequency.setPreferredSize(new Dimension(70, lbl_frequency.getPreferredSize().height));
			panel_row.add(lbl_frequency);

			JTextArea txt_timings = new JTextArea(1, 20);
			txt_timings.getDocument().addDocumentListener(new DocumentListener()
			{
				@Override
				public void removeUpdate(DocumentEvent e) 
				{
					update(e);
				}
			
				@Override
				public void insertUpdate(DocumentEvent e) 
				{
					update(e);
				}
			
				@Override
				public void changedUpdate(DocumentEvent e) 
				{
		
				}
		
				public void update(DocumentEvent e)
				{
					String input = txt_timings.getText();
		
					if(input.isEmpty()) return;
		
					if(input.matches("^[0-9A-Fa-f]{96}$"))
					{
						// find the frequency
						int curr_freq = 0;
						for(HashMap.Entry<Integer, JTextArea> entry : freq_text.entrySet())
						{
							if(e.getDocument() == entry.getValue().getDocument())
							{
								curr_freq = entry.getKey();
								break;
							}
						}
		
						byte selected_index = (byte)Integer.parseInt(cbox_indices.getSelectedItem().toString());
		
						TimingsEditor.ATOM_VRAM_TIMING_ENTRY new_timings = null;
						// find the timings
						for(TimingsEditor.ATOM_VRAM_TIMING_ENTRY t : timings)
						{
							if(t.ulClkRange == curr_freq * 100 && t.ucIndex == selected_index)
							{
								new_timings = t;
								break;
							}
						}
		
						byte[] new_timings_bytes = string_to_bytes(input);
						System.arraycopy(new_timings_bytes, 0, new_timings.ucLatency, 0, new_timings_bytes.length);
						timings_editor.set_timings(new_timings);
					}
				}
		
				private byte[] string_to_bytes(String s)
				{
					int len = s.length();
					byte[] bytes = new byte[len / 2];
					for(int i = 0; i < len ; i += 2)
					{
						bytes[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) +
												Character.digit(s.charAt(i + 1), 16));
					}
		
					return bytes;
				}
			});
			JScrollPane scroll = new JScrollPane(txt_timings, JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
												 JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			panel_row.add(scroll);
			freq_text.put(f, txt_timings);

			panel.add(panel_row);
		}

		update_timings_text((byte)1);

		main_panel.add(panel);
	}

	private void update_timings_text(byte ram_ic_index)
	{
		if(timings == null || timings.isEmpty())
			return;

		// fill in timings which are found in the bios
		for(int f : frequencies)
		{
			TimingsEditor.ATOM_VRAM_TIMING_ENTRY curr_timings = null;
			boolean found = false;
			for(TimingsEditor.ATOM_VRAM_TIMING_ENTRY e : timings)
			{
				// ulClkRange is in kHz
				if(e.ulClkRange == f * 100 && e.ucIndex == ram_ic_index)
				{
					curr_timings = e;
					found = true; break;
				}
			}

			// fill in timings
			if(found)
			{
				String str = "";
				for(byte b : curr_timings.ucLatency)
					str += String.format("%02X", b);

				freq_text.get(f).setText(str);
				freq_text.get(f).setCaretPosition(0);
				freq_text.get(f).setEnabled(true);
			}
			else 
			{
				freq_text.get(f).setText("No timings for this frequency");
				freq_text.get(f).setEnabled(false);
			}
		}
	}

	private Container main_panel = getContentPane();
	private JMenuItem menu_item_open, menu_item_saveas;
	private TimingsEditor timings_editor;
	private ArrayList<TimingsEditor.ATOM_VRAM_TIMING_ENTRY> timings;
	// stores the JTextField for each frequency
	private HashMap<Integer, JTextArea> freq_text = new HashMap<>();
	private JComboBox cbox_indices = new JComboBox();
	private JLabel lbl_file = new JLabel("No BIOS opened");

	// frequencies for strap (in MHz)
	private static final int[] frequencies = { 400, 800, 900, 1000, 1125, 1250, 1375, 1425, 1500, 1625, 1750 };
}