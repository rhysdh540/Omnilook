package cpw.mods.fml.common;

public @interface Mod {
	String modid();
	boolean useMetadata() default false;
}
