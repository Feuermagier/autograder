package de.firemage.autograder.core;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

abstract class Workaround extends Charset {
    public Workaround() {
        super(Charset.defaultCharset().name(), Charset.defaultCharset().aliases().toArray(new String[0]));
    }

    public Workaround(String canonicalName, String[] aliases) {
        super(canonicalName, aliases);
    }
}

/**
 * This class is a workaround for the fact that {@link Charset} is not serializable.
 * It is used to make {@link SourceInfo} serializable.
 */
public final class SerializableCharset extends Workaround implements Serializable {
    private final String name;

    public SerializableCharset() {
        this(Charset.defaultCharset());
    }

    public SerializableCharset(String name) {
        this(Charset.forName(name));
    }

    SerializableCharset(Charset charset) {
        super(charset.name(), charset.aliases().toArray(new String[0]));

        this.name = charset.name();
    }

    private Charset toCharset() {
        return Charset.forName(this.name);
    }

    @Override
    public boolean contains(Charset cs) {
        return this.toCharset().contains(cs);
    }

    @Override
    public CharsetDecoder newDecoder() {
        return this.toCharset().newDecoder();
    }

    @Override
    public CharsetEncoder newEncoder() {
        return this.toCharset().newEncoder();
    }
}
