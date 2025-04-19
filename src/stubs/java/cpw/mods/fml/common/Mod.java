package cpw.mods.fml.common;

public @interface Mod {
	String modid();
	String guiFactory() default "";
	boolean useMetadata() default false;
}
