package vrm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by domas on 17.3.4.
 */
public class SourceParser {
    private RealMachine rm;
    public SourceParser(RealMachine rm) {
        this.rm = rm;
    }

    public List<Word> stringToWords(String string){
        if (string.length() % Word.WORD_LENGTH != 0) {
            // TODO add error handling or ignore extra bytes
        }
        List<Word> commands = new ArrayList<>();
        for (int i = 0; i < string.length(); i += Word.WORD_LENGTH) {
            commands.add(new Word(string.substring(i, i + Word.WORD_LENGTH)));
        }
        return commands;
    }

    public void stringToMem(RealMachine rm, String code, int location) {
        List<Word> words = stringToWords(code);
        for (int i = 0; i < words.size(); i++){
            rm.MEMORY[location + i] = words.get(i);
        }
    }
}
