package main;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.apache.axis.wsdl.Java2WSDL;

public class WindowHandler extends JFrame{

	private static final long serialVersionUID = 1L;
	
	private boolean isSetUp = false;

	public WindowHandler() {
		super("Wsdl Tester");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(true);
		setLocationRelativeTo(null);
		setLayout(new GridBagLayout());
		setMinimumSize(new Dimension(300, 150));
		setVisible(true);
	}
	
	public void setUpLayout(){
		
		if (isSetUp) return;
		
		JButton generate = new JButton("WSDL ereugen");
		generate.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Java2WSDL.main(new String[]{"-o mod.wsdl", "-l .",  ExtendedModel.class.getName()});
			}
		});
		
		JTextPane text = new JTextPane();
		text.setEditable(false);
		
		JButton choose =new JButton("WSDL zum Vergleichen wählen.");
		choose.addActionListener(new FileChecker(text));
		
		float weightY = 0.05f;
		
		GridBagConstraints genConst = new GridBagConstraints();
		genConst.gridx = 0;
		genConst.gridy = 0;
		genConst.fill = GridBagConstraints.BOTH;
		genConst.weightx = 1;
		genConst.weighty = weightY;
		genConst.insets = new Insets(4, 4, 2, 2);
		GridBagConstraints chConst = new GridBagConstraints();
		chConst.gridx = 1;
		chConst.gridy = 0;
		chConst.fill = GridBagConstraints.BOTH;
		chConst.weightx = 1;
		chConst.weighty = weightY;
		chConst.insets = new Insets(4, 2, 2, 4);
		GridBagConstraints textConst = new GridBagConstraints();
		textConst.gridy = 1;
		textConst.gridwidth = 2;
		textConst.fill = GridBagConstraints.BOTH;
		textConst.weightx = 1;
		textConst.weighty = 1 - weightY;
		textConst.insets = new Insets(2, 4, 4, 4);
		
		add(generate, genConst);
		add(choose, chConst);
		add(new JScrollPane(text, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), textConst);
		
		setSize(1280, 720);
		setLocationRelativeTo(null);
	}
	
}
