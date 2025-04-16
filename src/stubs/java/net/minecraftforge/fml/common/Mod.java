package net.minecraftforge.fml.common;

public @interface Mod {
	String value() default "";

	String modid() default "";
	String guiFactory() default "";
	boolean useMetadata() default false;
}
