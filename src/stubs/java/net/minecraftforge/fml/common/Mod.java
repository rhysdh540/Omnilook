package net.minecraftforge.fml.common;

public @interface Mod {
	String value() default "";

	String modid() default "";
	boolean useMetadata() default false;
}
