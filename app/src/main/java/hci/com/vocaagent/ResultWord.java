package hci.com.vocaagent;

public class ResultWord {
    private Word resultWord;
    private int phaseIncrement;

    public int getPhaseIncrement() {
        return phaseIncrement;
    }

    public void setPhaseIncrement(int phaseIncrement) {
        this.phaseIncrement = phaseIncrement;
    }

    public Word getResultWord() {
        return resultWord;
    }

    public void setResultWord(Word resultWord) {
        this.resultWord = resultWord;
    }
}
