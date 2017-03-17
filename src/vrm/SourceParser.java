package vrm;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

/**
 * Created by domas on 17.3.4.
 */
public class SourceParser {
    private RealMachine rm;
    public SourceParser(RealMachine rm) {
        this.rm = rm;
    }

    public List<Word> stringToWords(String string){
        if (string.length() % Word.MAX_LENGTH != 0) {
            // TODO add error handling or ignore extra bytes
        }
        List<Word> commands = new ArrayList<>();
        for (int i = 0; i < string.length(); i += Word.MAX_LENGTH) {
            commands.add(new Word(string.substring(i, i + Word.MAX_LENGTH)));
        }
        return commands;
    }

    public void stringToMem(RealMachine rm, String code, int location) {
        List<Word> words = stringToWords(code);
        for (int i = 0; i < words.size(); i++){
            rm.memory.replace(location + i, words.get(i));
        }
    }

    public static List<Word> fileToWords (String fileName) throws IOException{
        List<Word> words = new ArrayList<Word>();
        // HACK String.format is needed because for some reason test file gets modified:
        // following spaces are are removed "HALT " -> "HALT".
        // So I added right padding.
        Files.lines(Paths.get(fileName)).forEach(line -> words.add(new Word(String.format("%1$-5s", line))));
        return words;
    }

    // for testing
    public static void main(String[] args) {
        try {
            List<Word> words = fileToWords("Code examples/TestCode");
            RealMachine rm = new RealMachine(new Memory(1000));
            System.out.println(words);
            for (int i = 0; i < words.size(); i++){
                rm.memory.replace(i, words.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
