package net.minecraftforge.gradle.common.util.func;

import java.io.IOException;

@FunctionalInterface
public interface IOConsumer<T> {

    public void accept(T value) throws IOException;

}
