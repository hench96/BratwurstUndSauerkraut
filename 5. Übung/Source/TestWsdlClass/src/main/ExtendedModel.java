package main;

/**
 * 
 * @author Felix Wackernagel
 *
 */
public class ExtendedModel extends SimpleModel {
	
	public enum MyEnum {
		ONE, TWO;
	}

	public SimpleModel toSimpleModel() {
		return this;
	}
	
	public static ExtendedModel fromSimpleModel( SimpleModel model ) {
		return new ExtendedModel();
	}
	
	public long getLong() {
		return 0l;
	}
	
	public void setLong( long value ) {
		
	}
	
	public float getFloat() {
		return 0f;
	}

	public void setFloat( float value ) {
		
	}
	
	public MyEnum getEnum() {
		return MyEnum.ONE;
	}
	
	public void setEnum( MyEnum value ) {
		
	}
	
	public String[] getArray() {
		return new String[0];
	}
	
	public void setArray( String[] value ) {
		
	}
}
