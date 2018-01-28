package main;
import org.apache.axis.wsdl.Java2WSDL;

public class Main {
	
	public static void main(String[] args){
		System.out.println("Starting build of wsdll");
		Java2WSDL.main(new String[]{"-o orig.wsdl ", "-l . ",  ExtendedModel.class.getName()});
		System.out.println("WSDL complete");
	}
	
}
