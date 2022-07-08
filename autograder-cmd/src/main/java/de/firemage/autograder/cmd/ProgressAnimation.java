package de.firemage.autograder.cmd;

public class ProgressAnimation {
    private final Thread outputThread;
    private String text;
    private String lastLine = "";

    public ProgressAnimation(String initialText) {
        this.text = initialText;
        this.outputThread = new Thread(() -> {
            int step = 0;

            while (!Thread.currentThread().isInterrupted()) {
                String output = "";
                output += switch (step) {
                    case 0 -> "[ - ]";
                    case 1 -> "[ \\ ]";
                    case 2 -> "[ | ]";
                    case 3 -> "[ / ]";
                    default -> throw new IllegalStateException();
                };
                output += " " + text;
                step = (step + 1) % 4;
                print(output);

                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        this.outputThread.setDaemon(true);
    }

    public ProgressAnimation() {
        this("");
    }

    public void start() {
        this.outputThread.start();
    }

    public void updateText(String text) {
        this.text = text;
    }

    public void finish(String finalText) {
        this.outputThread.interrupt();
        print(finalText);
        CmdUtil.println();
    }

    private void print(String line) {
        //clear the last line if longer
        if (lastLine.length() > line.length()) {
            String temp = "";
            temp += " ".repeat(lastLine.length());
            if (temp.length() > 1) {
                CmdUtil.print("\r" + temp);
            }
        }
        CmdUtil.print("\r" + line);
        lastLine = line;
    }
}
