package net.minecraftforge.gradle.common.util.func;

import java.io.IOException;

@FunctionalInterface
public interface IOSupplier<T> {

    public T get() throws IOException;

}
