package edu.kit.informatik;

import edu.kit.informatik.cmd.CommandBuilder;
import edu.kit.informatik.cmd.CommandException;
import edu.kit.informatik.cmd.CommandResolver;
import edu.kit.informatik.model.Collisions;
import edu.kit.informatik.model.Vector;
import edu.kit.informatik.model.rollingstock.*;
import edu.kit.informatik.model.track.Track;
import edu.kit.informatik.model.train.Train;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * The user interface. Contains the main loop, listens for commands and invokes the appropriate
 * actions on the Simulation object.
 *
 * @author ukidf
 * @version 2.0
 */
public final class UI {

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    private static final String OK = "OK";
    private static final String AT = "at";
    private static final String TRAIN = "Train";
    private static final String STEP_COMMAND = "step";
    private static final String TRAIN_COLLISION = "Crash of train ";

    private static final String EXIT_COMMAND = "exit";
    private static final String PUT_COMMAND = "put train";
    private static final String ADD_TRACK_COMMAND = "add track";
    private static final String ADD_SWITCH_COMMAND = "add switch";
    private static final String CREATE_ENGINE_COMMAND = "create engine";
    private static final String CREATE_TRAIN_SET_COMMAND = "create train-set";
    private static final String CREATE_COACH_COMMAND = "create coach";
    private static final String DELETE_TRACK_COMMAND = "delete track";
    private static final String LIST_TRACKS_COMMAND = "list tracks";
    private static final String NO_TRACK_EXISTS = "No track exists";
    private static final String SET_SWITCH_COMMAND = "set switch";
    private static final String POSITION = "position";
    private static final String LIST_ENGINES_COMMAND = "list engines";
    private static final String NO_ENGINE_EXISTS = "No engine exists";
    private static final String LIST_COACHES_COMMAND = "list coaches";
    private static final String NO_COACH_EXISTS = "No coach exists";
    private static final String LIST_TRAIN_SETS_COMMAND = "list train-sets";
    private static final String NO_TRAIN_SET_EXISTS = "No train-set exists";
    private static final String DELETE_ROLLING_STOCK_COMMAND = "delete rolling stock";
    private static final String ADD_TRAIN_COMMAND = "add train";
    private static final String ADDED_TO_TRAIN = "added to train";
    private static final String DELETE_TRAIN_COMMAND = "delete train";
    private static final String LIST_TRAINS_COMMAND = "list trains";
    private static final String NO_TRAIN_EXISTS = "No train exists";
    private static final String SHOW_TRAIN_COMMAND = "show train";
    private static final String IN_DIRECTION = "in direction";


    /**
     * The application's main method. Instantiates this class and calls the run method:
     *
     * @param args Any command line parameters (will be ignored)
     */
    public static void main(String[] args) {
        new UI().run();
    }

    /**
     * Creates a Vector from the given coordinates
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @return A vector created from the strings
     * @throws CommandException If the Strings cannot be parsed to integers
     */
    private static Vector toVector(String x, String y) throws CommandException {
        return new Vector(toInt(x), toInt(y));
    }

    /**
     * Converts the given String to a signed integer behaving like Integer.parseInt
     * (Essentially a wrapper around Integer.parseInt to throw a CommandException instead of a NumberFormatException
     * in case of wrong input format)
     *
     * @param string The string that should be parsed
     * @return An integer from the given string
     * @throws CommandException If the string cannot be parsed
     */
    private static int toInt(String string) throws CommandException {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException ex) {
            throw new CommandException("A number isn't formatted correctly or to large", ex);
        }
    }

    /**
     * Converts the given String to a signed short behaving like Short.parseShort
     * (Essentially a wrapper around Short.parseShort to throw a CommandException instead of a NumberFormatException
     * in case of wrong input format)
     *
     * @param string The string that should be parsed
     * @return A short from the given string
     * @throws CommandException If the string cannot be parsed
     */
    private static short toShort(String string) throws CommandException {
        try {
            return Short.parseShort(string);
        } catch (NumberFormatException ex) {
            throw new CommandException("A number isn't formatted correctly or to large", ex);
        }
    }

    /**
     * Converts the given String case-sensitive to a boolean value while mapping 'true' to true and 'false' to false
     *
     * @param string The string that should be parsed
     * @return A boolean value from the string
     * @throws CommandException If the given string cannot be converted to a boolean value
     */
    private static boolean toBoolean(String string) throws CommandException {
        // Boolean.toBoolean cannot be used here because it is case insensitive
        if (string.equals(TRUE)) {
            return true;
        } else if (string.equals(FALSE)) {
            return false;
        } else {
            throw new CommandException("A boolean isn't formatted correctly");
        }
    }

    /**
     * Initializes the game and listens for user input (the core loop)
     */
    public void run() {
        Simulation simulation = new Simulation();
        CommandResolver resolver = setupCommands(simulation);

        do {
            try {
                resolver.execute(Terminal.readLine());
            } catch (CommandException e) {
                Terminal.printError(e.getMessage());
            }
        } while (resolver.isRunning());
    }

    private CommandResolver setupCommands(Simulation simulation) {
        CommandResolver resolver = new CommandResolver();

        setupTrackCommands(simulation, resolver);
        setupRollingStockCommands(simulation, resolver);
        setupTrainCommands(simulation, resolver);

        resolver.register(
            CommandBuilder.command(STEP_COMMAND).sep(StringConstants.SPACE).addIntParameter().build(),
            matcher -> {
                Collisions collisions = simulation.doSteps(toShort(matcher.group(1)));
                Map<String, Integer> lines = new HashMap<>();
                collisions.forEachCollision(collision -> {
                    StringBuilder text = new StringBuilder(TRAIN_COLLISION);
                    collision.stream()
                        .mapToInt(Train::getId).sorted()
                        .forEachOrdered(id -> text.append(id).append(StringConstants.COMMA));
                    lines.put(text.substring(0, text.length() - 1),
                        collision.stream().mapToInt(Train::getId).min().orElseThrow(IllegalStateException::new));
                });

                simulation.getTrains().stream().filter(Train::isPlacedOnTrack).forEach(train -> {
                    lines.put(TRAIN + StringConstants.SPACE + train.getId()
                        + StringConstants.SPACE + AT + StringConstants.SPACE + train.getHeadPosition(), train.getId());
                });
                lines.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEachOrdered(line -> {
                    Terminal.printLine(line.getKey());
                });
                if (lines.isEmpty()) {
                    Terminal.printLine(OK);
                }
                return true;
            });

        resolver.register(CommandBuilder.command(EXIT_COMMAND).build(), matcher -> false);

        return resolver;
    }

    private void setupTrackCommands(Simulation simulation, CommandResolver resolver) {
        resolver.register(
            CommandBuilder.command(ADD_TRACK_COMMAND).sep(StringConstants.SPACE)
                .addPointParameter().sep(StringConstants.SPACE + StringConstants.ARROW + StringConstants.SPACE)
                .addPointParameter().build(),
            matcher -> {
                Track track = simulation.addStandardTrack(
                    toVector(matcher.group(1), matcher.group(2)),
                    toVector(matcher.group(3), matcher.group(4)));
                Terminal.printLine(track.getId());
                return true;
            });
        resolver.register(
            CommandBuilder.command(ADD_SWITCH_COMMAND).sep(StringConstants.SPACE)
                .addPointParameter().sep(StringConstants.SPACE + StringConstants.ARROW + StringConstants.SPACE)
                .addPointParameter().sep(StringConstants.COMMA).addPointParameter().build(),
            matcher -> {
                Track track = simulation.addSwitch(
                    toVector(matcher.group(1), matcher.group(2)),
                    toVector(matcher.group(3), matcher.group(4)),
                    toVector(matcher.group(5), matcher.group(6)));
                Terminal.printLine(track.getId());
                return true;
            });
        resolver.register(
            CommandBuilder.command(DELETE_TRACK_COMMAND).sep(StringConstants.SPACE)
                .addIntParameter().build(),
            matcher -> {
                simulation.removeTrack(toInt(matcher.group(1)));
                Terminal.printLine(OK);
                return true;
            });
        resolver.register(
            CommandBuilder.command(LIST_TRACKS_COMMAND).build(),
            matcher -> {
                simulation.getTracks().stream().sorted(Comparator.comparingInt(Track::getId))
                    .forEach(Terminal::printLine);
                if (simulation.getTracks().size() == 0) {
                    Terminal.printLine(NO_TRACK_EXISTS);
                }
                return true;
            });
        resolver.register(
            CommandBuilder.command(SET_SWITCH_COMMAND).sep(StringConstants.SPACE)
                .addIntParameter().sep(StringConstants.SPACE + POSITION + StringConstants.SPACE)
                .addPointParameter().build(),
            matcher -> {
                simulation.setSwitch(toInt(matcher.group(1)), toVector(matcher.group(2), matcher.group(3)));
                Terminal.printLine(OK);
                return true;
            });
    }

    private void setupRollingStockCommands(Simulation simulation, CommandResolver resolver) {
        resolver.register(
            CommandBuilder.command(CREATE_ENGINE_COMMAND).sep(StringConstants.SPACE)
                .addEnumParameter(Engine.Type.class).sep(StringConstants.SPACE)
                .addStringParameter().sep(StringConstants.SPACE)
                .addStringParameter().sep(StringConstants.SPACE)
                .addIntParameter().sep(StringConstants.SPACE)
                .addBooleanParameter().sep(StringConstants.SPACE)
                .addBooleanParameter().build(),
            matcher -> {
                Engine engine = simulation.createEngine(Engine.Type.valueOf(matcher.group(1).toUpperCase()),
                    matcher.group(2), matcher.group(3), toInt(matcher.group(4)),
                    toBoolean(matcher.group(5)), toBoolean(matcher.group(6)));
                Terminal.printLine(engine.getId());
                return true;
            });
        resolver.register(
            CommandBuilder.command(LIST_ENGINES_COMMAND).build(),
            matcher -> {
                simulation.getEngines().stream().sorted(Comparator.comparing(RollingStock::getId))
                    .forEachOrdered(Terminal::printLine);
                if (simulation.getEngines().size() == 0) {
                    Terminal.printLine(NO_ENGINE_EXISTS);
                }
                return true;
            });
        resolver.register(
            CommandBuilder.command(CREATE_COACH_COMMAND).sep(StringConstants.SPACE)
                .addEnumParameter(Coach.Type.class).sep(StringConstants.SPACE)
                .addIntParameter().sep(StringConstants.SPACE)
                .addBooleanParameter().sep(StringConstants.SPACE)
                .addBooleanParameter().build(),
            matcher -> {
                Coach coach = simulation.createCoach(Coach.Type.valueOf(matcher.group(1).toUpperCase()),
                    toInt(matcher.group(2)), toBoolean(matcher.group(3)), toBoolean(matcher.group(4)));
                Terminal.printLine(coach.getNumericalId());
                return true;
            });
        resolver.register(
            CommandBuilder.command(LIST_COACHES_COMMAND).build(),
            matcher -> {
                simulation.getCoaches().stream().sorted(Comparator.comparing(RollingStock::getId))
                    .forEachOrdered(Terminal::printLine);
                if (simulation.getCoaches().size() == 0) {
                    Terminal.printLine(NO_COACH_EXISTS);
                }
                return true;
            });
        resolver.register(
            CommandBuilder.command(CREATE_TRAIN_SET_COMMAND).sep(StringConstants.SPACE)
                .addStringParameter().sep(StringConstants.SPACE)
                .addStringParameter().sep(StringConstants.SPACE)
                .addIntParameter().sep(StringConstants.SPACE)
                .addBooleanParameter().sep(StringConstants.SPACE)
                .addBooleanParameter().build(),
            matcher -> {
                TrainSet trainSet = simulation.createTrainSet(matcher.group(1), matcher.group(2),
                    toInt(matcher.group(3)), toBoolean(matcher.group(4)), toBoolean(matcher.group(5)));
                Terminal.printLine(trainSet.getId());
                return true;
            });
        resolver.register(
            CommandBuilder.command(LIST_TRAIN_SETS_COMMAND).build(),
            matcher -> {
                simulation.getTrainSets().stream().sorted(Comparator.comparing(PoweredRollingStock::getId))
                    .forEachOrdered(Terminal::printLine);
                if (simulation.getTrainSets().size() == 0) {
                    Terminal.printLine(NO_TRAIN_SET_EXISTS);
                }
                return true;
            });
        resolver.register(
            CommandBuilder.command(DELETE_ROLLING_STOCK_COMMAND).sep(StringConstants.SPACE)
                .addStringParameter().build(),
            matcher -> {
                simulation.deleteRollingStock(matcher.group(1));
                Terminal.printLine(OK);
                return true;
            });
    }

    private void setupTrainCommands(Simulation simulation, CommandResolver resolver) {
        resolver.register(
            CommandBuilder.command(ADD_TRAIN_COMMAND).sep(StringConstants.SPACE)
                .addIntParameter().sep(StringConstants.SPACE).addStringParameter().build(),
            matcher -> {
                Train train = simulation.addTrain(toInt(matcher.group(1)), matcher.group(2));
                RollingStock rollingStock = simulation.findRollingStockById(matcher.group(2));
                Terminal.printLine(
                    rollingStock.getTypeString() + StringConstants.SPACE
                        + matcher.group(2)
                        + StringConstants.SPACE + ADDED_TO_TRAIN + StringConstants.SPACE
                        + train.getId());
                return true;
            });
        resolver.register(
            CommandBuilder.command(DELETE_TRAIN_COMMAND).sep(StringConstants.SPACE).addIntParameter().build(),
            matcher -> {
                simulation.removeTrain(toInt(matcher.group(1)));
                Terminal.printLine(OK);
                return true;
            });
        resolver.register(
            CommandBuilder.command(LIST_TRAINS_COMMAND).build(),
            matcher -> {
                simulation.getTrains().stream().sorted(Comparator.comparingInt(Train::getId))
                    .forEach(Terminal::printLine);
                if (simulation.getTrains().size() == 0) {
                    Terminal.printLine(NO_TRAIN_EXISTS);
                }
                return true;
            });

        resolver.register(
            CommandBuilder.command(SHOW_TRAIN_COMMAND).sep(StringConstants.SPACE)
                .addIntParameter().build(),
            matcher -> {
                Terminal.printLine(simulation.showTrain(toInt(matcher.group(1))));
                return true;
            });

        resolver.register(
            CommandBuilder.command(PUT_COMMAND).sep(StringConstants.SPACE)
                .addIntParameter().sep(StringConstants.SPACE + AT + StringConstants.SPACE)
                .addPointParameter().sep(StringConstants.SPACE + IN_DIRECTION + StringConstants.SPACE)
                .addIntParameter().sep(StringConstants.COMMA).addIntParameter().build(),
            matcher -> {
                simulation.putTrain(
                    toInt(matcher.group(1)),
                    toVector(matcher.group(2), matcher.group(3)),
                    toInt(matcher.group(4)),
                    toInt(matcher.group(5)));
                Terminal.printLine(OK);
                return true;
            });
    }
}
