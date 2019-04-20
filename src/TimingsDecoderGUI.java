import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class TimingsDecoderGUI extends JFrame
{
    public static void main(String[] args)
    {
        new TimingsDecoderGUI();
    }

    public TimingsDecoderGUI()
    {
        super("Timings Decoder v1.1.3");

        main_panel.setLayout(new GridBagLayout());

        timings_doc_listener = new DocumentListener()
        {
            @Override
            public void removeUpdate(DocumentEvent e)
            {
                changedUpdate(e);
            }
        
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                changedUpdate(e);
            }
        
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                if(should_update)
                    update_output_text();
            }
        };

        add_input_panel();
        add_timings_panel(new TimingsPanelInfo("SEQ_WR_CTL_D1", TimingsDecoder.SEQ_WR_CTL_D1_FORMAT.names, 90), 2, 0);
        add_timings_panel(new TimingsPanelInfo("SEQ_WR_CTL_2", TimingsDecoder.SEQ_WR_CTL_2_FORMAT.names, 100), 2, 1);
        add_timings_panel(new TimingsPanelInfo("SEQ_PMG_TIMING", TimingsDecoder.SEQ_PMG_TIMING_FORMAT.names, 120), 3, 0);
        TimingsPanelInfo[] tp = new TimingsPanelInfo[2];
        tp[0] = new TimingsPanelInfo("SEQ_RAS_TIMING", TimingsDecoder.SEQ_RAS_TIMING_FORMAT.names, 70);
        tp[1] = new TimingsPanelInfo("SEQ_CAS_TIMING", TimingsDecoder.SEQ_CAS_TIMING_FORMAT.names, 50);
        add_timings_panel(tp, 3, 1);
        tp[0] = new TimingsPanelInfo("SEQ_MISC_TIMING", TimingsDecoder.SEQ_MISC_TIMING_FORMAT_R9.names, 70);
        tp[1] = new TimingsPanelInfo("SEQ_MISC_TIMING2", TimingsDecoder.SEQ_MISC_TIMING2_FORMAT.names, 70);
        add_timings_panel(tp, 4, 0);
        tp[0] = new TimingsPanelInfo("ARB_DRAM_TIMING", TimingsDecoder.ARB_DRAM_TIMING_FORMAT.names, 80);
        tp[1] = new TimingsPanelInfo("ARB_DRAM_TIMING2", TimingsDecoder.ARB_DRAM_TIMING2_FORMAT.names, 80);
        add_timings_panel(tp, 4, 1);
        add_mc_seq_panel(5, 0);

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

    private void add_input_panel()
    {
        JPanel p = new JPanel(new FlowLayout());

        JLabel lbl_input = new JLabel("Input:");
        p.add(lbl_input);

        JRadioButton rdo_r9 = new JRadioButton("R9"),
                     rdo_rx = new JRadioButton("RX");
        rdo_r9.setSelected(true);
        ButtonGroup grp = new ButtonGroup();
        grp.add(rdo_r9);
        grp.add(rdo_rx);
        p.add(rdo_r9);
        p.add(rdo_rx);

        txt_input = new JTextArea(1, 50);
        JScrollPane scroll = new JScrollPane(txt_input, JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
                                             JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        p.add(scroll);

        JButton btn_decode = new JButton("Decode");
        btn_decode.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String input = txt_input.getText();
                if(input.isEmpty() || !input.matches("^[0-9A-Fa-f]{96}$"))
                {
                    txt_output.setBackground(invalid_color);
                }
                else
                {
                    txt_output.setBackground(Color.WHITE);
                    txt_output.setText(input);
                    
                    if(is_r9_timings)
                        r9_timings = TimingsDecoder.decode_r9_timings(input);
                    else
                        rx_timings = TimingsDecoder.decode_rx_timings(input);

                    should_update = false;
                    update_all_timings_text();
                    should_update = true;
                }
            }
        });
        p.add(btn_decode);

        ActionListener listener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                is_r9_timings = rdo_r9.isSelected();
            }
        };
        rdo_r9.addActionListener(listener);
        rdo_rx.addActionListener(listener);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = padding;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        main_panel.add(p, gbc);

        p = new JPanel(new FlowLayout());

        JLabel lbl_output = new JLabel("Output:");
        p.add(lbl_output);

        txt_output = new JTextField(65);
        txt_output.setEditable(false);
        p.add(txt_output);

        gbc.gridy = 1;
        main_panel.add(p, gbc);
    }

    private boolean is_main_timing(String timing)
    {
        for(String t : main_timings)
        {
            if(t.equals(timing)) 
                return true;
        }

        return false;
    }

    private void add_timings_panel(TimingsPanelInfo tp, int row, int col)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JPanel panel_title = new JPanel(new BorderLayout());
        panel_title.setBackground(title_color);
        JLabel lbl_title = new JLabel(tp.title, SwingConstants.CENTER);
        panel_title.add(lbl_title);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(panel_title, gbc);

        // add each timing
        int count = 0;
        for(String timing : tp.timings_names)
        {
            if(timing.startsWith("Pad"))
                continue;

            JPanel panel_row = new JPanel(new FlowLayout());

            JLabel lbl_row = new JLabel(timing + ":");
            lbl_row.setPreferredSize(new Dimension(tp.label_width, lbl_row.getPreferredSize().height));
            Font normal = lbl_row.getFont().deriveFont(Font.PLAIN);
            if(is_main_timing(timing)) 
                normal = new Font(normal.getFontName(), Font.BOLD, normal.getSize() + 1);
            lbl_row.setFont(normal);
            panel_row.add(lbl_row);
            
            JTextField txt_row = new JTextField(2);
            txt_row.getDocument().addDocumentListener(timings_doc_listener);
            panel_row.add(txt_row);

            gbc = new GridBagConstraints();
            gbc.gridx = count / 5; gbc.gridy = (count % 5) + 1; // + 1 as title takes up a row
            panel.add(panel_row, gbc);
            timings_textfields.put(timing, txt_row);
            count++;
        }

        panel.setBorder(new LineBorder(Color.GRAY));

        int width = panel.getPreferredSize().width,
            height = panel_title.getPreferredSize().height;
        panel_title.setPreferredSize(new Dimension(width, height));

        gbc = new GridBagConstraints();
        gbc.gridx = col; gbc.gridy = row;
        gbc.insets = padding;
        main_panel.add(panel, gbc);
    }

    /*
     * adds panels with the information in timings_panels in
     * the same row and column
     */
    private void add_timings_panel(TimingsPanelInfo[] timings_panels, int row, int col)
    {
        JPanel panel_parent = new JPanel(new FlowLayout());

        // create each panel
        for(TimingsPanelInfo tp : timings_panels)
        {
            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
    
            // add title
            JPanel panel_title = new JPanel(new BorderLayout());
            panel_title.setBackground(title_color);
            JLabel lbl_title = new JLabel(tp.title, SwingConstants.CENTER);
            panel_title.add(lbl_title);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0; gbc.gridy = 0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            panel.add(panel_title, gbc);
    
            // add each timing
            int count = 0;
            for(String timing : tp.timings_names)
            {
                if(timing.startsWith("Pad"))
                    continue;

                JPanel panel_row = new JPanel(new FlowLayout());

                JLabel lbl_row = new JLabel(timing + ":");
                lbl_row.setPreferredSize(new Dimension(tp.label_width, lbl_row.getPreferredSize().height));
                // main timings are bold
                Font normal = lbl_row.getFont().deriveFont(Font.PLAIN);
                if(is_main_timing(timing)) 
                    normal = new Font(normal.getFontName(), Font.BOLD, normal.getSize() + 1);
                lbl_row.setFont(normal);
                panel_row.add(lbl_row);
                
                JTextField txt_row = new JTextField(2);
                txt_row.getDocument().addDocumentListener(timings_doc_listener);
                panel_row.add(txt_row);

                gbc = new GridBagConstraints();
                gbc.gridx = count / 5; gbc.gridy = (count % 5) + 1; // + 1 as title takes up a row
                panel.add(panel_row, gbc);
                timings_textfields.put(timing, txt_row);
                count++;
            }
    
            panel.setBorder(new LineBorder(Color.GRAY));

            int width = panel.getPreferredSize().width,
                height = panel_title.getPreferredSize().height;
            panel_title.setPreferredSize(new Dimension(width, height));

            panel_parent.add(panel);
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col; gbc.gridy = row;
        gbc.insets = padding;
        main_panel.add(panel_parent, gbc);
    }

    private void add_mc_seq_panel(int row, int col)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        JPanel panel_title = new JPanel(new BorderLayout());
        panel_title.setBackground(title_color);
        JLabel lbl_title = new JLabel("MC_SEQ_MISC", SwingConstants.CENTER);
        panel_title.add(lbl_title);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(panel_title, gbc);

        // WL
        JPanel panel_row = new JPanel(new FlowLayout());
        JLabel lbl_row = new JLabel("WL:");
        lbl_row.setPreferredSize(new Dimension(30, lbl_row.getPreferredSize().height));
        Font normal = lbl_row.getFont().deriveFont(Font.PLAIN);
        lbl_row.setFont(normal);
        panel_row.add(lbl_row);
        JTextField txt_row = new JTextField(2);
        txt_row.getDocument().addDocumentListener(timings_doc_listener);
        panel_row.add(txt_row);
        gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(panel_row, gbc);
        timings_textfields.put("WL", txt_row);

        // CL
        panel_row = new JPanel(new FlowLayout());
        lbl_row = new JLabel("CL:");
        lbl_row.setPreferredSize(new Dimension(30, lbl_row.getPreferredSize().height));
        lbl_row.setFont(normal);
        panel_row.add(lbl_row);
        txt_row = new JTextField(2);
        txt_row.getDocument().addDocumentListener(timings_doc_listener);
        panel_row.add(txt_row);
        gbc = new GridBagConstraints();
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(panel_row, gbc);
        timings_textfields.put("CL", txt_row);

        // WR
        panel_row = new JPanel(new FlowLayout());
        lbl_row = new JLabel("WR:");
        lbl_row.setPreferredSize(new Dimension(30, lbl_row.getPreferredSize().height));
        lbl_row.setFont(normal);
        panel_row.add(lbl_row);
        txt_row = new JTextField(2);
        txt_row.getDocument().addDocumentListener(timings_doc_listener);
        panel_row.add(txt_row);
        gbc = new GridBagConstraints();
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(panel_row, gbc);
        timings_textfields.put("WR", txt_row);

        // TRAS
        panel_row = new JPanel(new FlowLayout());
        lbl_row = new JLabel("TRAS:");
        lbl_row.setPreferredSize(new Dimension(40, lbl_row.getPreferredSize().height));
        lbl_row.setFont(normal);
        panel_row.add(lbl_row);
        txt_row = new JTextField(2);
        txt_row.getDocument().addDocumentListener(timings_doc_listener);
        panel_row.add(txt_row);
        gbc = new GridBagConstraints();
        gbc.gridx = 3; gbc.gridy = 1;
        panel.add(panel_row, gbc);
        timings_textfields.put("TRAS", txt_row);

        panel.setBorder(new LineBorder(Color.GRAY));

        int width = panel.getPreferredSize().width,
            height = panel_title.getPreferredSize().height;
        panel_title.setPreferredSize(new Dimension(width, height));

        gbc = new GridBagConstraints();
        gbc.gridx = col; gbc.gridy = row;
        gbc.insets = padding;
        main_panel.add(panel, gbc);
    }

    /*
     * updates each of the timings as the user changes
     * the input hex string
     */
    private void update_all_timings_text()
    {
        if(is_r9_timings)
        {
            update_timings_text(r9_timings.SEQ_WR_CTL_D1.get_timings());
            update_timings_text(r9_timings.SEQ_WR_CTL_2.get_timings());
            update_timings_text(r9_timings.SEQ_PMG_TIMING.get_timings());
            update_timings_text(r9_timings.SEQ_RAS_TIMING.get_timings());
            update_timings_text(r9_timings.SEQ_CAS_TIMING.get_timings());
            update_timings_text(r9_timings.SEQ_MISC_TIMING.get_timings());
            update_timings_text(r9_timings.SEQ_MISC_TIMING2.get_timings());
            update_timings_text(r9_timings.ARB_DRAM_TIMING.get_timings());
            update_timings_text(r9_timings.ARB_DRAM_TIMING2.get_timings());
            update_seq_misc_timings_text();
        }
        else
        {
            update_timings_text(rx_timings.SEQ_WR_CTL_D1.get_timings());
            update_timings_text(rx_timings.SEQ_WR_CTL_2.get_timings());
            update_timings_text(rx_timings.SEQ_PMG_TIMING.get_timings());
            update_timings_text(rx_timings.SEQ_RAS_TIMING.get_timings());
            update_timings_text(rx_timings.SEQ_CAS_TIMING.get_timings());
            update_timings_text(rx_timings.SEQ_MISC_TIMING.get_timings());
            update_timings_text(rx_timings.SEQ_MISC_TIMING2.get_timings());
            update_timings_text(rx_timings.ARB_DRAM_TIMING.get_timings());
            update_timings_text(rx_timings.ARB_DRAM_TIMING2.get_timings());
            update_seq_misc_timings_text();
        }
    }

    /*
     * updates the JTextField associated with the timing in timings
     */
    private void update_timings_text(LinkedHashMap<String, Byte> timings)
    {
        for(Map.Entry<String, Byte> e : timings.entrySet())
        {
            if(e.getKey().startsWith("Pad"))
                continue;

            JTextField txt = timings_textfields.get(e.getKey());
            txt.setText(String.format("%d", Byte.toUnsignedInt(e.getValue())));
        }
    }

    private void update_seq_misc_timings_text()
    {
        LinkedHashMap<String, Integer> mc_seq = new LinkedHashMap<>();
        if(is_r9_timings)
        {
            mc_seq.put("WL", Byte.toUnsignedInt(r9_timings.SEQ_MISC1.WL));

            int cl = 5 + ((Byte.toUnsignedInt(r9_timings.SEQ_MISC1.CL) & 0xF) | 
                     (Byte.toUnsignedInt(r9_timings.SEQ_MISC8.CLEHF) << 4));
            mc_seq.put("CL", cl);

            int wr = 4 + ((r9_timings.SEQ_MISC1.WR & 0xF) |
                     (Byte.toUnsignedInt(r9_timings.SEQ_MISC8.WREHF) << 4));
            mc_seq.put("WR", wr);

            mc_seq.put("TRAS", Byte.toUnsignedInt(r9_timings.SEQ_MISC3.TRAS));
        }
        else
        {
            mc_seq.put("WL", Byte.toUnsignedInt(rx_timings.SEQ_MISC1.WL));
            
            int cl = 5 + ((Byte.toUnsignedInt(rx_timings.SEQ_MISC1.CL) & 0xF) | 
                     (Byte.toUnsignedInt(rx_timings.SEQ_MISC8.CLEHF) << 4));
            mc_seq.put("CL", cl);

            int wr = 4 + ((rx_timings.SEQ_MISC1.WR & 0xF) |
                     (Byte.toUnsignedInt(rx_timings.SEQ_MISC8.WREHF) << 4));
            mc_seq.put("WR", wr);

            mc_seq.put("TRAS", Byte.toUnsignedInt(rx_timings.SEQ_MISC3.TRAS));
        }

        for(Map.Entry<String, Integer> e : mc_seq.entrySet())
        {
            JTextField txt = timings_textfields.get(e.getKey());
            txt.setText(String.format("%d", e.getValue()));
        }
    }

    /*
     * updates the input hex string as the user
     * changes each of the timings
     */
    private void update_output_text()
    {
        boolean valid = true;

        if(is_r9_timings)
        {
            if(!update_timings(r9_timings.SEQ_WR_CTL_D1, r9_timings.SEQ_WR_CTL_D1.getClass().getFields()))
                valid = false;
            if(!update_timings(r9_timings.SEQ_WR_CTL_2, r9_timings.SEQ_WR_CTL_2.getClass().getFields()))
                valid = false;
            if(!update_timings(r9_timings.SEQ_PMG_TIMING, r9_timings.SEQ_PMG_TIMING.getClass().getFields()))
                valid = false;
            if(!update_timings(r9_timings.SEQ_RAS_TIMING, r9_timings.SEQ_RAS_TIMING.getClass().getFields()))
                valid = false;
            if(!update_timings(r9_timings.SEQ_CAS_TIMING, r9_timings.SEQ_CAS_TIMING.getClass().getFields()))
                valid = false;
            if(!update_timings(r9_timings.SEQ_MISC_TIMING, r9_timings.SEQ_MISC_TIMING.getClass().getFields()))
                valid = false;
            if(!update_timings(r9_timings.SEQ_MISC_TIMING2, r9_timings.SEQ_MISC_TIMING2.getClass().getFields()))
                valid = false;
            if(!update_timings(r9_timings.ARB_DRAM_TIMING, r9_timings.ARB_DRAM_TIMING.getClass().getFields()))
                valid = false;
            if(!update_timings(r9_timings.ARB_DRAM_TIMING2, r9_timings.ARB_DRAM_TIMING2.getClass().getFields()))
                valid = false;
            if(!update_mc_seq_timings())
                valid = false;
        }
        else
        {
            if(!update_timings(rx_timings.SEQ_WR_CTL_D1, rx_timings.SEQ_WR_CTL_D1.getClass().getFields()))
                valid = false;
            if(!update_timings(rx_timings.SEQ_WR_CTL_2, rx_timings.SEQ_WR_CTL_2.getClass().getFields()))
                valid = false;
            if(!update_timings(rx_timings.SEQ_PMG_TIMING, rx_timings.SEQ_PMG_TIMING.getClass().getFields()))
                valid = false;
            if(!update_timings(rx_timings.SEQ_RAS_TIMING, rx_timings.SEQ_RAS_TIMING.getClass().getFields()))
                valid = false;
            if(!update_timings(rx_timings.SEQ_CAS_TIMING, rx_timings.SEQ_CAS_TIMING.getClass().getFields()))
                valid = false;
            if(!update_timings(rx_timings.SEQ_MISC_TIMING, rx_timings.SEQ_MISC_TIMING.getClass().getFields()))
                valid = false;
            if(!update_timings(rx_timings.SEQ_MISC_TIMING2, rx_timings.SEQ_MISC_TIMING2.getClass().getFields()))
                valid = false;
            if(!update_timings(rx_timings.ARB_DRAM_TIMING, rx_timings.ARB_DRAM_TIMING.getClass().getFields()))
                valid = false;
            if(!update_timings(rx_timings.ARB_DRAM_TIMING2, rx_timings.ARB_DRAM_TIMING2.getClass().getFields()))
                valid = false;
            if(!update_mc_seq_timings())
                valid = false;
        }

        if(valid)
        {   
            if(is_r9_timings)
                txt_output.setText(TimingsDecoder.encode_r9_timings(r9_timings));
            else
                txt_output.setText(TimingsDecoder.encode_rx_timings(rx_timings));

            txt_output.setCaretPosition(0);
        }
    }

    private boolean update_mc_seq_timings()
    {
        String[] names = { "WL", "CL", "WR", "TRAS" };

        for (String name : names)
        {
            boolean valid = true;
            JTextField txt = timings_textfields.get(name);
            if (txt == null) return false;
            String str = txt.getText();

            try {
                int value = Integer.parseInt(str), min = 0, max;
    
                if (is_r9_timings)
                {
                    if (name.equals("WL"))
                    {
                        max = r9_timings.SEQ_MISC1.get_sizes().get("WL");
                        max = (int)Math.round(Math.pow(2, max)) - 1;

                        if (value < min || value > max) 
                            valid = false;
                        else r9_timings.SEQ_MISC1.WL = (byte)value;
                    }
                    else if (name.equals("CL"))
                    {
                        min = 6;
                        max = r9_timings.SEQ_MISC1.get_sizes().get("CL") +
                              r9_timings.SEQ_MISC8.get_sizes().get("CLEHF");
                        max = 5 + (int)Math.round(Math.pow(2, max)) - 1;
    
                        if (value < min || value > max) 
                            valid = false;
                        else r9_timings.set_mc_cl(value);
                    }
                    else if (name.equals("WR"))
                    {
                        min = 5;
                        max = r9_timings.SEQ_MISC1.get_sizes().get("WR") +
                              r9_timings.SEQ_MISC8.get_sizes().get("WREHF");
                        max = 4 + (int)Math.round(Math.pow(2, max)) - 1;
    
                        if (value < min || value > max) 
                            valid = false;
                        else r9_timings.set_mc_wr(value);
                    }
                    else
                    {
                        max = r9_timings.SEQ_MISC3.get_sizes().get("TRAS");
                        max = (int)Math.round(Math.pow(2, max)) - 1;
    
                        if (value < min || value > max) 
                            valid = false;
                        else r9_timings.SEQ_MISC3.TRAS = (byte)value;
                    }
                }
                else 
                {
                    if (name.equals("WL"))
                    {
                        max = rx_timings.SEQ_MISC1.get_sizes().get("WL");
                        max = (int)Math.round(Math.pow(2, max)) - 1;

                        if (value < min || value > max) 
                            valid = false;
                        else rx_timings.SEQ_MISC1.WL = (byte)value;
                    }
                    else if (name.equals("CL"))
                    {
                        min = 6;
                        max = rx_timings.SEQ_MISC1.get_sizes().get("CL") +
                              rx_timings.SEQ_MISC8.get_sizes().get("CLEHF");
                        max = 5 + (int)Math.round(Math.pow(2, max)) - 1;
    
                        if (value < min || value > max) 
                            valid = false;
                        else rx_timings.set_mc_cl(value);
                    }
                    else if (name.equals("WR"))
                    {
                        min = 5;
                        max = rx_timings.SEQ_MISC1.get_sizes().get("WR") +
                              rx_timings.SEQ_MISC8.get_sizes().get("WREHF");
                        max = 5 + (int)Math.round(Math.pow(2, max)) - 1;
    
                        if (value < min || value > max) 
                            valid = false;
                        else rx_timings.set_mc_wr(value);
                    }
                    else
                    {
                        max = rx_timings.SEQ_MISC3.get_sizes().get("TRAS");
                        max = (int)Math.round(Math.pow(2, max)) - 1;
    
                        if (value < min || value > max) 
                            valid = false;
                        else rx_timings.SEQ_MISC3.TRAS = (byte)value;
                    }
                }
    
                if (!valid)
                {
                    txt.setBackground(invalid_color);
                    return false;
                }
                else txt.setBackground(Color.WHITE);
            }
            catch(NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    /*
     * updates obj's fields with the values
     * the user input
     * sets the JTextField background to invalid_color
     * and returns false, if input is invalid
     * otherwise, returns true
     */
    private boolean update_timings(Object obj, Field[] fields)
    {
        try
        {
            Method get_sizes_method = obj.getClass().getMethod("get_sizes");
            LinkedHashMap<String, Integer> sizes = (LinkedHashMap<String, Integer>)get_sizes_method.invoke(obj);

            // loop over timings member variables
            for(Field f : fields)
            {
                if(f.getName().startsWith("Pad") || f.getName() == "names") 
                    continue;

                JTextField txt = timings_textfields.get(f.getName());
                String input = txt.getText();
    
                if(!input.isEmpty())
                {
                    if(!input.matches("[0-9]+"))
                    {
                        txt.setBackground(invalid_color);
                        return false;
                    }

                    int val = Integer.valueOf(input),
                        max_val = (int)Math.pow(2, sizes.get(f.getName())) - 1;
        
                    if(val > max_val)
                    {
                        txt.setBackground(invalid_color);
                        return false;
                    }
                    else
                    {
                        txt.setBackground(Color.WHITE);
                        f.setByte(obj, (byte)val);
                    }
                }
            }

            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private class TimingsPanelInfo
    {
        public String title;
        public String[] timings_names;
        public int label_width;

        TimingsPanelInfo(String title, String[] timings_names, int label_width)
        {
            this.title = title;
            this.timings_names = timings_names;
            this.label_width = label_width;
        }
    }

    private final Insets padding = new Insets(5, 5, 5, 5);
    private final Color title_color = new Color(0xFFAFAFFF),
                        invalid_color = new Color(0xFFFFAFAF);
    // from TechPowerUp VGA BIOS database
    private final String[] main_timings = { "TRCDW", "TRCDWA", "TRCDR", "TRCDRA", "TRC", "TCL", "TRFC" };
    private final Container main_panel = getContentPane();
    private TimingsDecoder.VBIOS_STRAP_R9 r9_timings = new TimingsDecoder.VBIOS_STRAP_R9();
    private TimingsDecoder.VBIOS_STRAP_RX rx_timings = new TimingsDecoder.VBIOS_STRAP_RX();
    // stores the name of the timing and the JTextField associated with it
    private HashMap<String, JTextField> timings_textfields = new HashMap<>();
    private JTextArea txt_input;
    private JTextField txt_output;
    private final DocumentListener timings_doc_listener;
    /*
     * to differentiate between setText() and 
     * the user inputting something
     * should_update should be false when calling setText()
     * and true when the user inputs something
     */
    private boolean should_update = false;
    private boolean is_r9_timings = true;
}