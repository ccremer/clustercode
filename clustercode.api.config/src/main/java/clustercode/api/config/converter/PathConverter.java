package clustercode.api.config.converter;

import clustercode.impl.util.FilesystemProvider;
import org.aeonbits.owner.Converter;

import java.lang.reflect.Method;
import java.nio.file.Path;

public class PathConverter implements Converter<Path> {

    @Override
    public Path convert(Method method, String input) {
        return FilesystemProvider.getInstance().getPath(input);
    }

}
