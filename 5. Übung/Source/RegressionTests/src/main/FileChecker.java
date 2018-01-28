package main;

import static java.awt.Color.RED;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.predic8.wsdl.Binding;
import com.predic8.wsdl.BindingElement;
import com.predic8.wsdl.BindingInput;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.BindingOutput;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Input;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.Operation;
import com.predic8.wsdl.Output;
import com.predic8.wsdl.Part;
import com.predic8.wsdl.PortType;
import com.predic8.wsdl.WSDLParser;

public class FileChecker implements ActionListener {
	
	private StyledDocument text;
	
	public FileChecker(JTextPane txtP){
		text = txtP.getStyledDocument();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				excecCheckFiles();
			}
		}).start();
		
	}
	
	private void excecCheckFiles(){
		JFileChooser chooser = new JFileChooser("./");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new FileNameExtensionFilter("Wsdl", "wsdl"));
		chooser.setDialogTitle("Original/Korrekte wsdl");
		chooser.setAcceptAllFileFilterUsed(false);
		
		File original;
		File needsCheck;
		
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
			original  = chooser.getSelectedFile();
		} else {
			return;
		}
		
		chooser.setDialogTitle("Zu prüfende Wsdl");
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
			needsCheck = chooser.getSelectedFile();
		} else {
			return ;
		}
		
		try {
			text.remove(0, text.getLength());
		} catch (BadLocationException e1) {
		}
		
		differenceCheck(original.toPath(), needsCheck.toPath());
	}
	
	private final SimpleAttributeSet markUp = new SimpleAttributeSet();
	
	private void appendLine(String line, Color c){
		StyleConstants.setForeground(markUp, c);
		
		try {
			text.insertString(text.getLength(), line + "\n", markUp);
		} catch (BadLocationException e) {
			System.err.println("could not append text");
			System.err.println(e.getMessage());
		}
		
		StyleConstants.setForeground(markUp, Color.BLACK);
	}
	
	private void appendLine(String line){
		try {
			text.insertString(text.getLength(), line + "\n", markUp);
		} catch (BadLocationException e) {
			System.err.println("could not append text");
			System.err.println(e.getMessage());
		}
	}
	
	private void differenceCheck(Path origPath, Path toCheckPath){
		
		WSDLParser parser = new WSDLParser();
		
		SimpleAttributeSet markUp = new SimpleAttributeSet();
		
		Definitions origDef = null, checkDef = null;
		try {
			InputStream istream = Files.newInputStream(origPath);
			origDef = parser.parse(istream);
			istream.close();
			
			istream = Files.newInputStream(toCheckPath);
			checkDef = parser.parse(istream);
			istream.close();
			
		} catch (IOException e) {
			appendLine(e.getMessage(), RED);
		}
		
		if (origDef == null || checkDef == null){
			StyleConstants.setForeground(markUp, RED);
			
			appendLine("Unabled to read files", RED);

			return;
		}
		
		int diffs = checkMessages(origDef, checkDef);
		
		if (diffs > 0) appendLine("");
		
		diffs += checkPorts(origDef, checkDef);
		
		if (diffs > 0) appendLine("");
		
		diffs += checkBindings(origDef, checkDef);
		
		if (diffs == 0){
			appendLine("Files are Matching. Test passed!", new Color(0.2f, 0.5f, 0.2f));
		} else {
			appendLine("\nFiles differ. Found " + diffs + " differences.", RED);
		}
		
	}
	
	private int checkMessages(Definitions orig, Definitions check) {
		
		int diffs = 0;
		
		for (Message msg : orig.getMessages()) {
			
			Message checkMsg = check.getMessage(msg.getQname());
			
			if (checkMsg == null) {
				//does the message exists
				appendLine("Message removed : " + msg.getName(), RED);
				diffs++;
			} else {
				List<Part> origParts = msg.getParts(), checkParts = checkMsg.getParts();
				
				for (int i = 0; i < origParts.size(); i++){
					Part origPart = origParts.get(i), checkPart = checkParts.get(i);
					
					//Part not found
					if (checkPart == null){
						appendLine("Part missing: " + origPart.getName(), RED);
						diffs++;
						continue;
					}
					
					//Part changed name
					if (!origPart.getName().equals(checkPart.getName())){
						appendLine("Part name changed from " + origPart.getName() + " to " + checkPart.getName());
						diffs++;
					}
					
					//Changed Type
					if (!origPart.getTypePN().toString().equals(checkPart.getTypePN().toString())){
						appendLine("Part type changed from " + origPart.getTypePN() + " to " + checkPart.getTypePN(), Color.ORANGE);
						diffs++;
					}
				}
			}
		}
		
		return diffs;
	}
	
	private Operation findOp(String opName, List<Operation> list){
		
		opName = opName.trim();
		
		for (Operation currOp : list){
			if (currOp.getName().trim().equals(opName)){
				return currOp;
			}
		}
		
		return null;
	}
	
	private int checkPorts(Definitions orig, Definitions check) {
		int diffs = 0;
		
		for (PortType pt : orig.getPortTypes()){
			PortType checkPt = check.getPortType(pt.getQName());
			
			if (checkPt == null){
				//does port exists
				appendLine("Port missing: " + pt.getName(), RED);
				diffs++;
			} else {
				List<Operation> origOps = pt.getOperations(), checkOps = checkPt.getOperations();
				
				//port operations changed
				if (origOps.size() != checkOps.size()){
					appendLine("Operations in port " + pt.getName() + "don't match,\nshould be "
									+ origOps.size() + " but found " + checkOps.size(), RED);
					diffs++;
					continue;
				}
				
				for (Operation origOp : origOps){
					Operation checkOp = findOp(origOp.getName(), checkOps);
					
					String portName = pt.getName();
					
					if (checkOp == null){
						appendLine("Operation " + origOp.getName() + " removed or renamed from " + portName, RED);
						diffs++;
					}
					
					//Check input
					Input origIn = origOp.getInput(), checkIn = checkOp.getInput();
					
					if (!origIn.getName().equals(checkIn.getName())){
						appendLine("Name of input in port " + portName + " changed from "
									+ origIn.getName() + " to " + checkIn.getName());
						diffs++;
					}
					
					//Check messages
					String origImpl = origIn.getMessage() == null ? null : origIn.getMessage().getName(),
							checkImpl = checkIn.getMessage() == null ? null : checkIn.getMessage().getName();
					
					if (origImpl == null ?  false : !origImpl.trim().isEmpty()) //is an impl needed?
					if (checkImpl == null || checkImpl.trim().isEmpty()){
						appendLine("Message impl " + origImpl + " removed from " + portName, RED);
						diffs++;
					} else if (!origImpl.equals(checkImpl)){
						appendLine("Message of input " + origIn.getName()
									+ " changed from " + origImpl + " to " + checkImpl);
						diffs++;
					}
					
					//Check Outputs
					Output origOut = origOp.getOutput(), checkOut = checkOp.getOutput();
					
					if (!origOut.getName().equals(checkOut.getName())){
						appendLine("Name of output in port " + portName + " changed from "
									+ origOut.getName() + " to " + checkOut.getName());
						diffs++;
					}
				}
			}
		}
		
		return diffs;
	}

	private Binding findBinding(String bName, List<Binding> list){
		
		bName = bName.trim();
		
		for (Binding curr : list){
			if (bName.equals(curr.getName().trim())){
				return curr;
			}
		}
		
		return null;
	}
	
	private int checkBody(BindingElement origE, BindingElement checkE, String origBName){
		
		int diffs = 0;
		
		if (origE != null){
			if (checkE == null){
				appendLine("Binding element removed from " + origBName, RED);
				diffs++;
			} else {
				if (!origE.getEncodingStyle().equals(checkE.getEncodingStyle())){
					appendLine("Encoding of binding element " + origE.getName() + " changed from "
								+ origE.getEncodingStyle() + " to " + checkE.getEncodingStyle());
					diffs++;
				}
				if (!origE.getNamespace().equals(checkE.getNamespace())){
					appendLine("Namespace of " + origE.getName() + " changed from "
								+ origE.getNamespace() + " to " + checkE.getNamespace());
					diffs++;
				}
				if (!origE.getUse().equals(checkE.getUse())){
					appendLine("Use of " + origE.getName() + " changed from "
								+ origE.getUse() + " to "  +checkE.getUse());
				}
			}
		}
		
		return diffs;
	}
	
	private int checkBindings(Definitions orig, Definitions check){
		int diffs = 0;
		
		//final String TRANS = "transport";
		
		for (Binding origB : orig.getBindings()){

			Binding checkB = findBinding(origB.getName(), check.getBindings());
			
			if (checkB == null){
				appendLine("Binding " + origB.getName() + " not found", RED);
				diffs++;
				continue;
			}
			
			if (!origB.getTypePN().toString().equals(checkB.getTypePN().toString())){
				appendLine("Binding type of " + origB.getName() +" changed from " + origB.getTypePN() + " to " + checkB.getName(), RED);
				diffs++;
			}
			/*
			if(!origB.getBinding().getProperty(TRANS).equals(checkB.getBinding().getProperty(TRANS))){
				appendLine("SOAP transport in " + origB.getName() + " chenged from "
							+ origB.getProperty(TRANS) + " to " + checkB.getProperty(TRANS), RED);
				diffs++;
			}
			*/
			if (origB.getOperations().size() != checkB.getOperations().size()){
				appendLine("Operation count in " + origB.getName()
							+ " does not match, should be " + origB.getOperations().size()
							+ " but found " + checkB.getOperations().size());
				diffs++;
				continue;
			}
			
			for (BindingOperation origOp : origB.getOperations()){
				BindingOperation checkOp = checkB.getOperation(origOp.getName());
				
				if (checkOp == null){
					appendLine("Binding operation in " + origB.getName() + " not found", RED);
					diffs++;
					continue;
				}
				
				BindingInput origIn = origOp.getInput(), checkIn = checkOp.getInput();
				
				if (!origIn.getName().equals(checkIn.getName())){
					appendLine("Binding input in " + origB.getName() + " changed from " + origIn.getName() + " to " + checkIn.getName());
					diffs++;
				}

				diffs += checkBody(origIn.getBindingElements().get(0), checkIn.getBindingElements().get(0), origB.getName());
				
				BindingOutput origOut = origOp.getOutput(), checkOut = checkOp.getOutput();
				
				if (!origOut.getName().equals(checkOut.getName())){
					appendLine("Binding output in " + origB.getName() + " changed from " + origOut.getName() + " to " + checkOut.getName());
					diffs++;
				}
				
				diffs += checkBody(origOut.getBindingElements().get(0), checkOut.getBindingElements().get(0), origB.getName());
			}
		}
		
		return diffs;
	}
}
