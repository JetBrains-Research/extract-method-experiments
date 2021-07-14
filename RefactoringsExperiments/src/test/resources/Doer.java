abstract class Doer{
    private int a;
    private int b;

    public int performOperation(){
        return a;
    }

    public int getA(){
        return a;
    }

    public int getB(){
        return b;
    }

    class Multiplier extends Doer{
        @Override
        public int performOperation(){
            return a * b;
        }
    }
    class aritheticSumComputer extends Doer{
        @Override
        public int performOperation() {
            if (a < 0) return 0;

            for(int i )

        }
    }
}