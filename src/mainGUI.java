import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class mainGUI extends JFrame
        implements ActionListener, ItemListener {

    JPanel _panel_gantt;
    JScrollPane _scrollpane;
    Font _font_job;

    JLabel _lbl_algorithm;
    JTextArea _fr_text;

    CheckboxGroup _cbg_algo;

    JButton _btn_run;
    JButton _btn_open;
    JButton _btn_submit;

    public mainGUI() {
        initUI();
    }

    void initUI()	{

        _panel_gantt = new JPanel();

        _scrollpane = new JScrollPane(_panel_gantt);
        _scrollpane.setBounds(20, 20, 540, 100);
        _font_job = new Font("Times New Roman", Font.PLAIN, 16);

        _lbl_algorithm = new JLabel("Algorithm.Algorithm: ");
        _lbl_algorithm.setBounds(20, 130, 100, 30);

        _fr_text = new JTextArea("");
        _fr_text.setBounds(110, 135, 100, 20);

        _btn_submit = new JButton("Submit");
        _btn_submit.setBounds(230, 130, 100, 30);
        _btn_submit.addActionListener(this);

//		_lbl_arrival = new JLabel("Arrival Time: ");
//		_lbl_arrival.setBounds(20, 160, 100, 20);
//		_txt_arrival = new JLabel();
//		_txt_arrival.setBounds(100, 160, 50, 20);
//
//		_lbl_burst = new JLabel("Burst Time: ");
//		_lbl_burst.setBounds(20, 185, 100, 20);
//		_txt_burst = new JLabel();
//		_txt_burst.setBounds(100, 185, 50, 20);
//
//
//		_lbl_priority = new JLabel("Priority: ");
//		_lbl_priority.setBounds(190, 160, 100, 20);
//		_txt_priority = new JLabel();
//		_txt_priority.setBounds(250, 160, 50, 20);


        _btn_run = new JButton("Run");
        _btn_run.setBounds(150, 200, 60, 40);
        _btn_run.addActionListener(this);

        _btn_open = new JButton("Open File");
        _btn_open.setBounds(20, 200, 100, 40);
        _btn_open.addActionListener(this);

        this.setLayout(null);

        this.add(_scrollpane);


//		this.add(_lbl_arrival);
//		this.add(_txt_arrival);
//
//		this.add(_lbl_burst);
//		this.add(_txt_burst);
//
//		this.add(_lbl_priority);
//		this.add(_txt_priority);

        this.add(_lbl_algorithm);
        this.add(_fr_text);

        this.add(_btn_run);
        this.add(_btn_open);
        this.add(_btn_submit);


        this.setSize(600, 500);
        this.setTitle("CPU Scheduling Algorithms");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public JTextArea get_fr_text() {
        return _fr_text;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == _btn_submit) {
            // set the text of the label to the text of the field
            String text = _fr_text.getText();
            System.out.println(text);

            // set the text of field to blank
            _fr_text.setText("  ");
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {

    }

    public static void main(String[] args) {
        mainGUI gui = new mainGUI();
        System.out.println(gui.get_fr_text());
    }
}
