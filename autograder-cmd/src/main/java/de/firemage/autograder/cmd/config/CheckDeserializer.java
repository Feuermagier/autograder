package de.firemage.autograder.cmd.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.firemage.autograder.core.check.Check;
import de.firemage.autograder.core.check.general.ConstantsInInterfaceCheck;
import de.firemage.autograder.core.check.general.CopyPasteCheck;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CheckDeserializer extends StdDeserializer<CheckFactory> {

    private final Map<String, Function<JsonNode, Check>> customFactories;
    private final Deque<String> packageStack = new ArrayDeque<>();

    public CheckDeserializer() {
        this(null);
    }

    public CheckDeserializer(Class<?> vc) {
        super(vc);
        this.packageStack.push("");
        this.customFactories = new HashMap<>();
        this.customFactories.put("ConstantsInInterfaceCheck",
            node -> new ConstantsInInterfaceCheck(node.get("ignoreIfHasMethods").asBoolean()));
        this.customFactories.put("CopyPasteCheck",
            node -> new CopyPasteCheck(node.get("tokenCount").asInt()));
    }

    @Override
    public CheckFactory deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode node = codec.readTree(p);
        if (node.isTextual()) {
            Class<?> clazz = createCheckClass(node.asText());
            try {
                Check check = (Check) clazz.getConstructor().newInstance();
                return () -> List.of(check);
            } catch (ReflectiveOperationException e) {
                throw new IOException("Check '" + node.asText() + "' seems to not have a parameterless constructor", e);
            }
        } else {
            var entry = node.fields().next();
            if (Character.isLowerCase(entry.getKey().charAt(0))) {
                this.packageStack.push(entry.getKey() + ".");
                CheckConfig config = codec.treeToValue(entry.getValue(), CheckConfig.class);
                List<Check> checks = config.getChecks().stream().flatMap(c -> c.create().stream()).toList();
                this.packageStack.pop();
                return () -> checks;
            } else {
                Check check = this.customFactories.get(entry.getKey()).apply(entry.getValue());
                return () -> List.of(check);
            }
        }
    }

    private Class<?> createCheckClass(String check) throws IOException {
        try {
            String path = "de.firemage.autograder.core.check." + String.join("", this.packageStack);
            Class<?> clazz = Class.forName(path + check);
            if (!Check.class.isAssignableFrom(clazz)) {
                throw new IOException("Unknown check '" + check + "'");
            }
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unknown check '" + check + "'", e);
        }
    }
}
