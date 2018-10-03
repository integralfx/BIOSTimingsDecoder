import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
        super("Timings Decoder");

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
                if(should_update_input_text)
                    update_input_text();
            }
        };

        add_input_panel();
        add_timings_panel(new TimingsPanelInfo("SEQ_WR_CTL_D1", TimingsDecoder.SEQ_WR_CTL_D1_FORMAT.names, 80), 1, 0);
        add_timings_panel(new TimingsPanelInfo("SEQ_WR_CTL_2", TimingsDecoder.SEQ_WR_CTL_2_FORMAT.names, 100), 1, 1);
        add_timings_panel(new TimingsPanelInfo("SEQ_PMG_TIMING", TimingsDecoder.SEQ_PMG_TIMING_FORMAT.names, 120), 2, 0);
        add_timings_panel(new TimingsPanelInfo("SEQ_RAS_TIMING", TimingsDecoder.SEQ_RAS_TIMING_FORMAT.names, 60), 2, 1);
        TimingsPanelInfo[] tp = new TimingsPanelInfo[2];
        tp[0] = new TimingsPanelInfo("SEQ_CAS_TIMING", TimingsDecoder.SEQ_CAS_TIMING_FORMAT.names, 60);
        tp[1] = new TimingsPanelInfo("SEQ_MISC_TIMING", TimingsDecoder.SEQ_MISC_TIMING_FORMAT_R9.names, 70);
        add_timings_panel(tp, 3, 0);
        tp[0] = new TimingsPanelInfo("SEQ_MISC_TIMING2", TimingsDecoder.SEQ_MISC_TIMING2_FORMAT.names, 80);
        tp[1] = new TimingsPanelInfo("ARB_DRAM_TIMING", TimingsDecoder.ARB_DRAM_TIMING_FORMAT.names, 80);
        add_timings_panel(tp, 3, 1);
        add_timings_panel(new TimingsPanelInfo("ARB_DRAM_TIMING2", TimingsDecoder.ARB_DRAM_TIMING2_FORMAT.names, 70), 4, 0);
        add_seq_misc_timings_panel(4, 1, 100);

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
        JPanel panel = new JPanel(new FlowLayout());

        JLabel lbl_input = new JLabel("Input timings:");
        panel.add(lbl_input);

        txt_input = new JTextArea(1, 50);
        JScrollPane scroll = new JScrollPane(txt_input, JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
                                             JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel.add(scroll);

        JButton btn_decode = new JButton("Decode");
        btn_decode.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String input = txt_input.getText();
                if(input.isEmpty() || !input.matches("^[0-9A-Fa-f]{96}$"))
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            txt_input.setBackground(invalid_color);
                        }
                    });
                }
                else
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            txt_input.setBackground(Color.WHITE);
                        }
                    });
                    
                    decoded_timings = TimingsDecoder.decode_timings(input);

                    if(should_update_timings_text)
                        update_all_timings_text();
                }
            }
        });
        panel.add(btn_decode);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = padding;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        main_panel.add(panel, gbc);
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
        for(int i = 0; i < tp.timings_names.length; i++)
        {
            JPanel panel_row = new JPanel(new FlowLayout());

            JLabel lbl_row = new JLabel(tp.timings_names[i] + ":");
            lbl_row.setPreferredSize(new Dimension(tp.label_width, lbl_row.getPreferredSize().height));
            panel_row.add(lbl_row);
            
            JTextField txt_row = new JTextField(2);
            txt_row.getDocument().addDocumentListener(timings_doc_listener);
            if(tp.timings_names[i].startsWith("Pad"))
                txt_row.setEditable(false);
            panel_row.add(txt_row);

            gbc = new GridBagConstraints();
            gbc.gridx = i / 5; gbc.gridy = (i % 5) + 1; // + 1 as title takes up a row
            panel.add(panel_row, gbc);
            timings_textfields.put(tp.timings_names[i], txt_row);
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
            for(int i = 0; i < tp.timings_names.length; i++)
            {
                JPanel panel_row = new JPanel(new FlowLayout());
    
                JLabel lbl_row = new JLabel(tp.timings_names[i] + ":");
                lbl_row.setPreferredSize(new Dimension(tp.label_width, lbl_row.getPreferredSize().height));
                panel_row.add(lbl_row);
                
                JTextField txt_row = new JTextField(2);
                txt_row.getDocument().addDocumentListener(timings_doc_listener);
                if(tp.timings_names[i].startsWith("Pad"))
                    txt_row.setEditable(false);
                panel_row.add(txt_row);
    
                gbc = new GridBagConstraints();
                gbc.gridx = i / 5; gbc.gridy = (i % 5) + 1; // + 1 as title takes up a row
                panel.add(panel_row, gbc);
                timings_textfields.put(tp.timings_names[i], txt_row);
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

    private void add_seq_misc_timings_panel(int r, int c, int label_width)
    {
        final String[] mc_seq = {
            "MC_SEQ_MISC1", "MC_SEQ_MISC3", "MC_SEQ_MISC8"
        };

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

        // add each timing
        for(int i = 0; i < mc_seq.length; i++)
        {
            JPanel panel_row = new JPanel(new FlowLayout());

            JLabel lbl_row = new JLabel(mc_seq[i] + ":");
            lbl_row.setPreferredSize(new Dimension(label_width, lbl_row.getPreferredSize().height));
            panel_row.add(lbl_row);
            
            JTextField txt_row = new JTextField(8);
            txt_row.getDocument().addDocumentListener(timings_doc_listener);
            panel_row.add(txt_row);

            gbc = new GridBagConstraints();
            gbc.gridx = i / 5; gbc.gridy = (i % 5) + 1; // + 1 as title takes up a row
            panel.add(panel_row, gbc);
            timings_textfields.put(mc_seq[i], txt_row);
        }

        panel.setBorder(new LineBorder(Color.GRAY));

        int width = panel.getPreferredSize().width,
            height = panel_title.getPreferredSize().height;
        panel_title.setPreferredSize(new Dimension(width, height));

        gbc = new GridBagConstraints();
        gbc.gridx = c; gbc.gridy = r;
        gbc.insets = padding;
        main_panel.add(panel, gbc);
    }

    /*
     * updates each of the timings as the user changes
     * the input hex string
     */
    private void update_all_timings_text()
    {
        update_timings_text(decoded_timings.SEQ_WR_CTL_D1.get_timings());
        update_timings_text(decoded_timings.SEQ_WR_CTL_2.get_timings());
        update_timings_text(decoded_timings.SEQ_PMG_TIMING.get_timings());
        update_timings_text(decoded_timings.SEQ_RAS_TIMING.get_timings());
        update_timings_text(decoded_timings.SEQ_CAS_TIMING.get_timings());
        update_timings_text(decoded_timings.SEQ_MISC_TIMING.get_timings());
        update_timings_text(decoded_timings.SEQ_MISC_TIMING2.get_timings());
        update_timings_text(decoded_timings.ARB_DRAM_TIMING.get_timings());
        update_timings_text(decoded_timings.ARB_DRAM_TIMING2.get_timings());
        update_seq_misc_timings_text();
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
            // https://stackoverflow.com/a/11120747
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    should_update_timings_text = false;
                    txt.setText(String.format("%d", e.getValue().intValue()));
                }
            });
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    should_update_timings_text = true;
                }
            });
        }
    }

    private void update_seq_misc_timings_text()
    {
        LinkedHashMap<String, Long> mc_seq = new LinkedHashMap<>();
        mc_seq.put("MC_SEQ_MISC1", decoded_timings.SEQ_MISC1);
        mc_seq.put("MC_SEQ_MISC3", decoded_timings.SEQ_MISC3);
        mc_seq.put("MC_SEQ_MISC8", decoded_timings.SEQ_MISC8);

        for(Map.Entry<String, Long> e : mc_seq.entrySet())
        {
            JTextField txt = timings_textfields.get(e.getKey());
            // https://stackoverflow.com/a/11120747
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    should_update_timings_text = false;
                    txt.setText(String.format("0x%08X", e.getValue().intValue()));
                }
            });
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    should_update_timings_text = true;
                }
            });
        }
    }

    /*
     * updates the input hex string as the user
     * changes each of the timings
     */
    private void update_input_text()
    {
        boolean valid = true;

        if(!update_timings(decoded_timings.SEQ_WR_CTL_D1, decoded_timings.SEQ_WR_CTL_D1.getClass().getFields()))
            valid = false;
        if(!update_timings(decoded_timings.SEQ_WR_CTL_2, decoded_timings.SEQ_WR_CTL_2.getClass().getFields()))
            valid = false;
        if(!update_timings(decoded_timings.SEQ_PMG_TIMING, decoded_timings.SEQ_PMG_TIMING.getClass().getFields()))
            valid = false;
        if(!update_timings(decoded_timings.SEQ_RAS_TIMING, decoded_timings.SEQ_RAS_TIMING.getClass().getFields()))
            valid = false;
        if(!update_timings(decoded_timings.SEQ_CAS_TIMING, decoded_timings.SEQ_CAS_TIMING.getClass().getFields()))
            valid = false;
        if(!update_timings(decoded_timings.SEQ_MISC_TIMING, decoded_timings.SEQ_MISC_TIMING.getClass().getFields()))
            valid = false;
        if(!update_timings(decoded_timings.SEQ_MISC_TIMING2, decoded_timings.SEQ_MISC_TIMING2.getClass().getFields()))
            valid = false;
        if(!update_timings(decoded_timings.ARB_DRAM_TIMING, decoded_timings.ARB_DRAM_TIMING.getClass().getFields()))
            valid = false;
        if(!update_timings(decoded_timings.ARB_DRAM_TIMING2, decoded_timings.ARB_DRAM_TIMING2.getClass().getFields()))
            valid = false;
        if(!validate_mc_seq("MC_SEQ_MISC1"))
            valid = false;
        if(!validate_mc_seq("MC_SEQ_MISC3"))
            valid = false;
        if(!validate_mc_seq("MC_SEQ_MISC8"))
            valid = false;

        if(valid)
        {
            // https://stackoverflow.com/a/11120747
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    should_update_input_text = false;
                    txt_input.setText(TimingsDecoder.encode_timings(decoded_timings));
                    txt_input.setCaretPosition(0);
                }
            });
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    should_update_input_text = true;
                }
            });
        }
    }

    /*
     * validates MC_SEQ_MISCN timings
     * sets the JTextField to invalid_color
     * and returns false, if input is invalid
     * otherwise, returns true
     */
    private boolean validate_mc_seq(String name)
    {
        JTextField txt = timings_textfields.get(name);
        String str = txt.getText();
        boolean valid = true;

        if(!str.isEmpty()) 
        {
            if(!str.matches("0x[0-9A-Fa-f]{8}"))
            {
                txt.setBackground(invalid_color);
                valid = false;
            }
            else
            {
                txt.setBackground(Color.WHITE);
                // remove leading 0x
                decoded_timings.SEQ_MISC1 = Long.valueOf(str.substring(2), 16);
            }
        }

        return valid;
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
        /*
        // this blocks but idk why
        RunnableFuture rf = new FutureTask<Boolean>(new Callable<Boolean>()
        {
            @Override
            public Boolean call()
            {

            }
        });

        SwingUtilities.invokeLater(rf);

        try {
            return (boolean)rf.get();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
        */

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
    private final Container main_panel = getContentPane();
    private TimingsDecoder.VBIOS_STRAP_R9 decoded_timings = new TimingsDecoder.VBIOS_STRAP_R9();
    // stores the name of the timing and the JTextField associated with it
    private HashMap<String, JTextField> timings_textfields = new HashMap<>();
    private JTextArea txt_input;
    /* 
     * flag to distinguish between programmatic and user changes
     * i.e. calling setText() and the user actually inputting something
     */
    private boolean should_update_input_text = true,
                    should_update_timings_text = true;
    private final DocumentListener timings_doc_listener;
}